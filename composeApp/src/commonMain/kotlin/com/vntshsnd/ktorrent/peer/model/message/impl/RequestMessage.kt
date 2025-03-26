package com.vntshsnd.ktorrent.peer.model.message.impl

import com.vntshsnd.ktorrent.peer.model.message.MessageId
import com.vntshsnd.ktorrent.peer.model.message.PeerWireMessage
import java.nio.ByteBuffer

class RequestMessage(val pieceIndex: Int, val pieceOffset: Int, val blockLength: Int): PeerWireMessage {
    companion object {
        fun fromPayload(payload: ByteArray): RequestMessage {
            val byteBuffer = ByteBuffer.wrap(payload)
            val pieceIndex = byteBuffer.getInt()
            val pieceOffset = byteBuffer.getInt()
            val blockLength = byteBuffer.getInt()

            return RequestMessage(pieceIndex, pieceOffset, blockLength)
        }
    }

    override val messageId = MessageId.REQUEST

    override fun toByteArray(): ByteArray {
        val byteBuffer = ByteBuffer.allocate(4 + 13)
        byteBuffer.putInt(13)
        byteBuffer.put(messageId.id)
        byteBuffer.putInt(pieceIndex)
        byteBuffer.putInt(pieceOffset)
        byteBuffer.putInt(blockLength)

        return byteBuffer.array()
    }
}