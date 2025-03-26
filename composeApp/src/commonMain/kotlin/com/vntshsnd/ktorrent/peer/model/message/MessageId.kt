package com.vntshsnd.ktorrent.peer.model.message

enum class MessageId(val id: Byte) {
    CHOKE(0),
    UNCHOKE(1),
    INTERESTED(2),
    NOT_INTERESTED(3),
    HAVE(4),
    BITFIELD(5),
    REQUEST(6),
    PIECE(7),
    CANCEL(8),
    PORT(9),
    KEEP_ALIVE(-1);

    companion object {
        fun fromId(id: Byte): MessageId? = entries.find { it.id == id }
    }
}