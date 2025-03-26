package com.vntshsnd.ktorrent.peer.model.message

interface PeerWireMessage: Message {
    val messageId: MessageId
}