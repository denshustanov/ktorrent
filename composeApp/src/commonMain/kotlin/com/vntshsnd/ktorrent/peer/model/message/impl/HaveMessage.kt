package com.vntshsnd.ktorrent.peer.model.message.impl

import com.vntshsnd.ktorrent.peer.model.message.MessageId
import com.vntshsnd.ktorrent.peer.model.message.PeerWireMessage
import java.nio.ByteBuffer

class HaveMessage(val index: Int): PeerWireMessage {
    companion object {
        fun fromPayload(payload: ByteArray) = HaveMessage(ByteBuffer.wrap(payload).int)
    }

    override val messageId = MessageId.HAVE

    override fun toByteArray(): ByteArray {
        val byteBuffer = ByteBuffer.allocate(9)
        byteBuffer.putInt(5)
        byteBuffer.put(messageId.id)
        byteBuffer.putInt(index)

        return byteBuffer.array()
    }
}