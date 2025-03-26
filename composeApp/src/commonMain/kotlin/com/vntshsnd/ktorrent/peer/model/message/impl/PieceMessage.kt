package com.vntshsnd.ktorrent.peer.model.message.impl

import com.vntshsnd.ktorrent.peer.model.message.MessageId
import com.vntshsnd.ktorrent.peer.model.message.PeerWireMessage
import java.nio.ByteBuffer

class PieceMessage(
    val pieceIndex: Int,
    val pieceOffset: Int,
    val data: ByteArray
): PeerWireMessage {
    companion object {
        fun fromPayload(payload: ByteArray): PieceMessage {
            val byteBuffer = ByteBuffer.wrap(payload)
            val pieceIndex = byteBuffer.getInt()
            val pieceOffset = byteBuffer.getInt()
            val data = ByteArray(byteBuffer.remaining())
            byteBuffer.get(data)

            return PieceMessage(pieceIndex, pieceOffset, data)
        }
    }

    override val messageId = MessageId.PIECE

    override fun toByteArray(): ByteArray {
        val payloadLength = 1 + 4 + 4 + data.size
        val totalLength = 4 + payloadLength
        val byteBuffer = ByteBuffer.allocate(totalLength)

        byteBuffer.putInt(payloadLength)
        byteBuffer.put(messageId.id)
        byteBuffer.putInt(pieceIndex)
        byteBuffer.putInt(pieceOffset)
        byteBuffer.put(data)

        return byteBuffer.array()
    }
}