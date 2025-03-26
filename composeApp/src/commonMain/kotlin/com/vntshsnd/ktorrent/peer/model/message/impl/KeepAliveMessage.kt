package com.vntshsnd.ktorrent.peer.model.message.impl

import com.vntshsnd.ktorrent.peer.model.message.MessageId
import com.vntshsnd.ktorrent.peer.model.message.PeerWireMessage

class KeepAliveMessage: PeerWireMessage {
    override val messageId = MessageId.KEEP_ALIVE

    override fun toByteArray(): ByteArray {
        return ByteArray(1) {0}
    }
}