package com.vntshsnd.ktorrent.peer

import com.vntshsnd.ktorrent.peer.model.message.MessageId
import com.vntshsnd.ktorrent.piece.PieceManager
import com.vntshsnd.ktorrent.peer.model.PeerState
import com.vntshsnd.ktorrent.peer.model.message.PeerWireMessage
import com.vntshsnd.ktorrent.peer.model.message.impl.*
import com.vntshsnd.ktorrent.metainfo.model.TorrentMetaInfo
import kotlinx.coroutines.delay
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.InputStream
import java.io.OutputStream
import java.net.Socket
import java.nio.ByteBuffer
import java.util.*

class Peer(
    private val ip: String,
    private val port: Int,
    private val torrentMetaInfo: TorrentMetaInfo,
    private val myId: ByteArray,
    private val pieceManager: PieceManager,
) {
    companion object {
        val log: Logger = LoggerFactory.getLogger(Peer::class.java)
    }

    private var socket: Socket? = null
    private var inputStream: InputStream? = null
    private var outputStream: OutputStream? = null


    private var _state: PeerState = PeerState.DISCONNECTED
    private var _isChocking = true
    private var _isInterested = false
    private var _amChocking = true
    private var _amInterested = false
    private var stopped = false
    private var lastReceivedTime: Long = 0
    private var lastReceivedSize = 0
    private var lastSentMessage: Long = 0

    val downSpeed: Double get() {
        if(lastReceivedTime <= 0) return .0
        return lastReceivedSize.toDouble() /
                (System.currentTimeMillis() - lastReceivedTime) * 1000
    }

    private var pendingRequests = mutableListOf<BlockRequest>()

    private val bitfield = BitSet(torrentMetaInfo.info.pieces.size)

    val state: PeerState
        get() = _state

    val isChocking: Boolean
        get() = _isChocking

    fun canSendRequest() = pendingRequests.size < 10

    fun connect() {
        log.info("Connecting to peer $hostPortString...")
        try {
            val socket = Socket(ip, port)

            this.socket = socket

            socket.soTimeout = 15000

            val outputStream = socket.getOutputStream()
            val inputStream = socket.getInputStream()

            this.outputStream = outputStream
            this.inputStream = inputStream

//            performHandshake()

            val pstr = "BitTorrent protocol".toByteArray()
            val handshakeMessage = byteArrayOf(pstr.size.toByte()) + pstr + ByteArray(8) { 0 } +
                    torrentMetaInfo.info.hash +
                    myId

            outputStream.write(handshakeMessage)
            outputStream.flush()

            val pstrLen: Int = inputStream.read()

            val pstrResp = inputStream.readNBytes(pstrLen)

            if (!pstrResp.contentEquals(pstr)) throw HandshakeException("Invalid pstr response")
            inputStream.readNBytes(8)
            val infoHash = inputStream.readNBytes(20)
            if (!infoHash.contentEquals(torrentMetaInfo.info.hash)) throw HandshakeException("Invalid info hash")

            inputStream.readNBytes(20)
            log.info("Successfully handshaked with peer $hostPortString")

            if (pieceManager.pieces.count { it.isFull } > 0) {
                sendBitfield(pieceManager.bitField)
            }

            _state = PeerState.IDLE

        } catch (e: Exception) {
            log.info("Failed to connect to peer $hostPortString \"${e.message}\"")
            _state = if (e is HandshakeException) PeerState.HANDSHAKE_FAILED else PeerState.UNAVAILABLE
        }
    }

    fun isAvailable(): Boolean = state == PeerState.LEECHING || state == PeerState.IDLE || state == PeerState.SEEDING

    fun sendBitfield(bitfield: BitSet) {
        sendMessage(BitfieldMessage(bitfield, pieceManager.pieces.size))
    }

    fun sendUnchoke() {
        _amChocking = false
        sendMessage(UnchokeMessage())
    }

    fun hasPiece(pieceIndex: Int) = bitfield.get(pieceIndex)

    fun sendRequest(pieceIndex: Int, pieceOffset: Int, blockLength: Int) {
        if (_state == PeerState.BROCKEN_PIPE || pendingRequests.size >= 10) return

        val message = RequestMessage(pieceIndex, pieceOffset, blockLength)
        val success = sendMessage(message)
        if (!success) return

        log.info("Requesting a piece $pieceIndex from $hostPortString")
        pendingRequests.add(BlockRequest(pieceIndex, pieceOffset, System.currentTimeMillis()))
        _state = PeerState.LEECHING
    }

    private fun clearStaleRequests() {
        pendingRequests.removeAll { System.currentTimeMillis() - it.sentTime > 1000 }
    }

    suspend fun peerInteractionLoop() {
        while (!stopped) {
            clearStaleRequests()

            if (System.currentTimeMillis() - lastSentMessage > 120000) {
                sendMessage(KeepAliveMessage())
            }

            if (_state == PeerState.BROCKEN_PIPE || _state == PeerState.DISCONNECTED) {
                connect()
            }
            if (_state == PeerState.UNAVAILABLE) {
                log.info("PEER $hostPortString UNAVAILABLE")
                break
            }
            val message = readMessage()

            if (message == null) {
                delay(200)
                continue
            }

            processMessage(message)
        }
    }


    fun getAvailablePieces(): List<Int> {
        val indices = mutableListOf<Int>()
        var index = bitfield.nextSetBit(0) // Get the first set bit
        while (index != -1) { // -1 means no more set bits
            indices.add(index)
            index = bitfield.nextSetBit(index + 1) // Get next set bit
        }
        return indices
    }

    fun stop() {
        stopped = true
    }

    private suspend fun processMessage(message: PeerWireMessage) {
        when (message) {
            is BitfieldMessage -> handleBitfieldMessage(message)
            is HaveMessage -> handleHaveMessage(message)
            is PieceMessage -> handlePieceMessage(message)
            is ChokeMessage -> handleChoke()
            is UnchokeMessage -> handleUnchoke()
            is InterestedMessage -> handleInterested()
            is NotInterestedMessage -> handleNotInerested()
            is RequestMessage -> handleRequest(message)
            is KeepAliveMessage -> handleKeepAlive()
            else -> {
                // don't care
            }
        }
    }


    @OptIn(ExperimentalStdlibApi::class)
    fun readMessage(): PeerWireMessage? {
        try {
            val inputStream = this.inputStream ?: return null

            val lengthBuffer = ByteArray(4)
            val readBytes = inputStream.read(lengthBuffer, 0, 4)
            if (readBytes != 4) return null
            val length = ByteBuffer.wrap(lengthBuffer).getInt()
            if (length < 0) return null
            if (length == 0) return KeepAliveMessage()
            val message = ByteArray(length)

            if (inputStream.read(message, 0, length) != length) return null
            val messageId = MessageId.fromId(message[0]) ?: return null

            val payload = message.takeLast(length - 1).toByteArray()
            val parsedMessage = parseMessage(messageId, payload)
            log.debug(
                "Got {} message from peer {}, {}",
                messageId,
                hostPortString,
                parsedMessage.toByteArray().toHexString()
            )
            return parsedMessage
        } catch (e: Throwable) {
            log.info("Failed reading message to $hostPortString : ${e.message}")
            _state = PeerState.BROCKEN_PIPE
            return null
        }
    }

    private fun parseMessage(messageId: MessageId, payload: ByteArray): PeerWireMessage {
        return when (messageId) {
            MessageId.CHOKE -> ChokeMessage()
            MessageId.UNCHOKE -> UnchokeMessage()
            MessageId.INTERESTED -> InterestedMessage()
            MessageId.NOT_INTERESTED -> NotInterestedMessage()
            MessageId.HAVE -> HaveMessage.fromPayload(payload)
            MessageId.BITFIELD -> BitfieldMessage.fromPayload(payload, pieceManager.pieces.size)
            MessageId.REQUEST -> RequestMessage.fromPayload(payload)
            MessageId.PIECE -> PieceMessage.fromPayload(payload)
            else -> TODO()
        }
    }

    @OptIn(ExperimentalStdlibApi::class)
    private fun sendMessage(message: PeerWireMessage): Boolean {
        try {
            val outputStream = this.outputStream ?: return false
            val byteArray = message.toByteArray()
            log.debug("Sending {} to {}, {}", message.messageId, hostPortString, byteArray.toHexString())
            outputStream.write(byteArray)
            outputStream.flush()
            lastSentMessage = System.currentTimeMillis()
            return true
        } catch (e: Exception) {
            log.info("Failed sending message to $hostPortString : ${e.message}")
            _state = PeerState.BROCKEN_PIPE
            _isChocking = true
            _amChocking = true
            _amInterested = false
            _isInterested = false
        }

        return false
    }

    private class HandshakeException(message: String) : Exception(message)

    private val hostPortString: String = "$ip:$port"

    private fun handleBitfieldMessage(message: BitfieldMessage) {
        bitfield.or(message.bitfield)
        if (!_amInterested && _isChocking) {
            _amInterested = true
            sendMessage(InterestedMessage())
        }
    }

    private fun handleHaveMessage(message: HaveMessage) {
        bitfield.set(message.index)
    }

    private suspend fun handlePieceMessage(message: PieceMessage) {
        pendingRequests.removeAll { it.pieceIndex == message.pieceIndex && it.pieceOffset == message.pieceOffset }
        log.info("Got a piece ${message.pieceIndex} from $hostPortString, peer download speed: ${"%.2f".format(downSpeed)} Bps")
        lastReceivedTime = System.currentTimeMillis()
        lastReceivedSize = message.data.size
        pieceManager.savePieceBlock(message.pieceIndex, message.pieceOffset, message.data)

    }

    private fun handleChoke() {
        _isChocking = true
    }

    private fun handleUnchoke() {
        _isChocking = false
    }

    private fun handleInterested() {
        _isInterested = true
        if (_amChocking) sendUnchoke()
    }

    private fun handleNotInerested() {
        _isInterested = false
    }

    private fun handleRequest(message: RequestMessage) {
        // not implemented
    }

    private fun handleKeepAlive() {
        log.info("Got keep alive message from $hostPortString")
    }

    private data class BlockRequest(val pieceIndex: Int, val pieceOffset: Int, val sentTime: Long)
}
