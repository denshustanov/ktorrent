package com.vntshsnd.ktorrent.peer.model.message.impl

import com.vntshsnd.ktorrent.peer.model.message.MessageId
import com.vntshsnd.ktorrent.peer.model.message.PeerWireMessage
import java.nio.ByteBuffer

open class StatusMessage(override val messageId: MessageId): PeerWireMessage {
    override fun toByteArray(): ByteArray {
        val byteBuffer = ByteBuffer.allocate(5)
        byteBuffer.putInt(1)
        byteBuffer.put(messageId.id)

        return byteBuffer.array()
    }
}

class InterestedMessage: StatusMessage(MessageId.INTERESTED)
class NotInterestedMessage: StatusMessage(MessageId.NOT_INTERESTED)
class ChokeMessage: StatusMessage(MessageId.CHOKE)
class UnchokeMessage: StatusMessage(MessageId.UNCHOKE)