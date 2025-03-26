package com.vntshsnd.ktorrent.peer.model.message.handshake

import com.vntshsnd.ktorrent.peer.model.message.Message

const val PSTR = "BitTorrent protocol"

class HandshakeMessage(peerId: ByteArray, infoHash: ByteArray): Message {

    private val bytes = byteArrayOf(0x13) + PSTR.toByteArray() + ByteArray(8) + infoHash + peerId

    override fun toByteArray(): ByteArray {
        return bytes
    }

}