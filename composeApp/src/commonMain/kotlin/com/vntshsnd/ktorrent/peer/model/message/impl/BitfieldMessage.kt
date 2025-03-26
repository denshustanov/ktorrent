package com.vntshsnd.ktorrent.peer.model.message.impl

import com.vntshsnd.ktorrent.peer.model.message.MessageId
import com.vntshsnd.ktorrent.peer.model.message.PeerWireMessage
import java.nio.ByteBuffer
import java.util.BitSet
import kotlin.experimental.or

class BitfieldMessage(val bitfield: BitSet, private val totalPieces: Int): PeerWireMessage {
    companion object {
        fun fromPayload(payload: ByteArray, totalPieces: Int) = BitfieldMessage(payload.toBitSet(), totalPieces)
    }


    override val messageId = MessageId.BITFIELD

    override fun toByteArray(): ByteArray {
        val bitfieldBytes = bitfield.toBitfieldByteArray(totalPieces)
        val byteBuffer = ByteBuffer.allocate(4 + 1 + bitfieldBytes.size)

        byteBuffer.putInt(1 + bitfield.size())
        byteBuffer.put(messageId.id)
        byteBuffer.put(bitfieldBytes)

        return byteBuffer.array()
    }
}


fun BitSet.toBitfieldByteArray(totalPieces: Int): ByteArray {
    val bytes = (totalPieces + 7) / 8

    val byteArray = ByteArray(bytes)

    for (i in 0..<10) {
        if (this[i]) {
            val byteIndex = i / 8
            byteArray[byteIndex] = byteArray[byteIndex] or (1 shl (7 - (i % 8))).toByte()
        }
    }

    return byteArray
}

fun ByteArray.toBitSet(): BitSet {
    val bitSet = BitSet(this.size * 8)

    for (byteIndex in this.indices) {
        for (bitIndex in 0 until 8) {
            val bitPosition = byteIndex * 8 + bitIndex
            if ((this[byteIndex].toInt() and (1 shl (7 - bitIndex))) != 0) {
                bitSet.set(bitPosition)
            }
        }
    }

    return bitSet
}