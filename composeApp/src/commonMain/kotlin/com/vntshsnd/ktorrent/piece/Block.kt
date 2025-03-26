package com.vntshsnd.ktorrent.piece

const val BLOCK_SIZE = 8192

class Block(
    val size: Int = BLOCK_SIZE,
    val offset: Int = 0,
    var status: BlockStatus = BlockStatus.FREE,
    var requestTime: Long = 0,
    var data: ByteArray? = null,
) {
}