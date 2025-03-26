package com.vntshsnd.ktorrent.peer.model.message

interface Message {
    fun toByteArray(): ByteArray
}