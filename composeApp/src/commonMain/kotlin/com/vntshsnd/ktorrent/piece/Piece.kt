package com.vntshsnd.ktorrent.piece

import com.vntshsnd.ktorrent.sha1
import org.apache.commons.io.output.ByteArrayOutputStream
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import kotlin.math.ceil


class Piece(
    val index: Int,
    val length: Int,
    val hash: ByteArray
) {
    companion object {
        private val log: Logger = LoggerFactory.getLogger(Piece::class.java)
    }

    private var blocks: MutableList<Block> = generateBlocks()

    val isFull: Boolean
        get() = blocks.all { it.status == BlockStatus.DOWNLOADED }

    val remainingLength: Long
        get() = blocks.filter { it.status != BlockStatus.DOWNLOADED }.sumOf { it.size }.toLong()

    fun getFreeBlock(): Block? {
        val block = blocks.firstOrNull { it.status == BlockStatus.FREE } ?: return null
        block.status = BlockStatus.PENDING
        block.requestTime = System.currentTimeMillis()
        return block
    }

    fun updateBlocksStatus() {
        for (i in blocks.indices) {
            if (blocks[i].status == BlockStatus.PENDING && System.currentTimeMillis() - blocks[i].requestTime > 5000) {
                blocks[i] = Block(offset = i * BLOCK_SIZE)
            }
        }
    }

    fun setBlockData(offset: Int, data: ByteArray) {
        val index = offset / BLOCK_SIZE
        blocks[index].data = data
        blocks[index].status = BlockStatus.DOWNLOADED
        log.info("COMPLETED ${blocks.count { it.status == BlockStatus.DOWNLOADED }} of ${blocks.size} BLOCKS")
    }

    private fun generateBlocks(): MutableList<Block> {
        val blocksCount = ceil(length.toFloat() / BLOCK_SIZE).toInt()

        val blocksList = (0..<blocksCount - 1).map { Block(offset = it * BLOCK_SIZE) }.toMutableList()
        blocksList.add(
            Block(
                size = (length % BLOCK_SIZE).takeIf { it != 0 } ?: BLOCK_SIZE,
                offset = (blocksCount - 1) * BLOCK_SIZE)
        )
        return blocksList
    }

    fun purgeBlocks() {
        blocks.forEach { it.data = null }
    }

    fun getData(): ByteArray? {
        if (!blocks.all { it.status == BlockStatus.DOWNLOADED && it.data != null }) return null

        val dataStream = ByteArrayOutputStream()
        blocks.forEach { it.data?.let { blockBytes -> dataStream.write(blockBytes) } }
        return dataStream.toByteArray()
    }

    fun validatePiece(): Boolean {
        val data = getData() ?: return false
        val pieceValid = hash.contentEquals(data.sha1())
        if (!pieceValid) {
            blocks = generateBlocks()
            return false
        }

        return true
    }


}