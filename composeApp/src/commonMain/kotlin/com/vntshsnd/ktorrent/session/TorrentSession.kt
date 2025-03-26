package com.vntshsnd.ktorrent.session

import com.vntshsnd.ktorrent.files.FileManager
import com.vntshsnd.ktorrent.peer.PeerManager
import com.vntshsnd.ktorrent.piece.PieceManager
import com.vntshsnd.ktorrent.metainfo.model.TorrentMetaInfo
import com.vntshsnd.ktorrent.peer.model.PeerState
import kotlinx.coroutines.*
import org.slf4j.LoggerFactory

class TorrentSession(val metaInfo: TorrentMetaInfo) {

    companion object {
        val log = LoggerFactory.getLogger(TorrentSession::class.java)
    }

    private val peerId = "-ZZ0007-000000000000".toByteArray()
    private val fileManager = FileManager(metaInfo, "/Users/dshstnv/Downloads")
    private val pieceManager = PieceManager(metaInfo, fileManager)
    private val peerManager = PeerManager(metaInfo, peerId, pieceManager)
    private var discoveringPeers = false

    val name: String get() = metaInfo.info.nameUtf8 ?: metaInfo.info.name
    val downSpeed: Double get() {
        return peerManager.allPeers.sumOf { it.downSpeed }
    }
    val progress: Double get() = pieceManager.progress

    val status: String
        get() {
            if (discoveringPeers) return "Discovering Peers"
            if (peerManager.allPeers.any { it.state == PeerState.LEECHING }) return "Leeching"
            if (peerManager.allPeers.all { it.state == PeerState.DISCONNECTED || it.state == PeerState.BROCKEN_PIPE }) return "Connecting to Peers"
            if (peerManager.allPeers.all { it.state == PeerState.SEEDING }) return "Seeding"
            if (peerManager.allPeers.all { it.state == PeerState.UNAVAILABLE }) return "Unable to connect to peers"
            return ""
        }

    @OptIn(DelicateCoroutinesApi::class)
    fun startSession() {
        log.info("Starting downloading ${metaInfo.info.nameUtf8 ?: metaInfo.info.name}")
        log.info("Total pieces: ${metaInfo.info.pieces.size}")
        log.info("Piece size: ${metaInfo.info.pieceLength}")

        GlobalScope.launch {
            discoveringPeers = true
            fileManager.initFiles()
            discoveringPeers = false
            peerManager.discoverPeers()
            peerManager.connectToPeers()

            delay(1000)
            sessionLoop()
        }
    }

    private suspend fun sessionLoop() {
        while (!pieceManager.allPiecesDownloaded()) {
            pieceLoop@ for (piece in pieceManager.pieces) {
                if (piece.isFull) {
                    continue@pieceLoop
                }

                piece.updateBlocksStatus()

                val block = piece.getFreeBlock() ?: continue@pieceLoop
                val peers = peerManager.allPeers.filter {
                    it.hasPiece(piece.index)
                            && !it.isChocking
                            && it.isAvailable()
                            && it.canSendRequest()
                }.sortedByDescending { it.downSpeed }

                if (peers.isEmpty()) {
                    log.info("No unchoking peers with piece ${piece.index}")
                    delay(1000)
                    continue@pieceLoop
                }

                val peer = peers[peers.indices.random()]

                peer.sendRequest(piece.index, block.offset, block.size)
                delay(200)
            }
        }

        log.info("Completed downloading!")
        peerManager.stopPeers()
    }

}