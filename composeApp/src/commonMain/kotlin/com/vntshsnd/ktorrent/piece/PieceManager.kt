package com.vntshsnd.ktorrent.piece

import com.vntshsnd.ktorrent.files.FileManager
import com.vntshsnd.ktorrent.metainfo.model.TorrentMetaInfo
import org.slf4j.LoggerFactory
import java.util.*

class PieceManager(private val torrentMetaInfo: TorrentMetaInfo, private val fileManager: FileManager) {
    companion object {
        private val log = LoggerFactory.getLogger(PieceManager::class.java)
    }

    val progress: Double get() {
        val remainingLength = pieces.sumOf { it.remainingLength }
        val totalLength = torrentMetaInfo.info.files.sumOf { it.length.toLong() }
        return (totalLength - remainingLength).toDouble() / totalLength
    }


    val pieces: List<Piece>
    init {
        val torrentFullLength = torrentMetaInfo.info.length?.toInt()
            ?: torrentMetaInfo.info
                .files.map { it.length.toInt() }.sumOf { it }

        val pieceSize = torrentMetaInfo.info.pieceLength.toInt()

        pieces = (0..<torrentMetaInfo.info.pieces.size - 1).map { index ->
            Piece(
                index = index,
                length = pieceSize,
                hash = torrentMetaInfo.info.pieces[index]
            )
        }.toMutableList()

        pieces.add(
            Piece(
                index = torrentMetaInfo.info.pieces.size - 1,
                length = (torrentFullLength % pieceSize).takeIf { it != 0 } ?: pieceSize,
                hash = torrentMetaInfo.info.pieces.last(),
            )
        )
    }


    val bitField = BitSet(torrentMetaInfo.info.pieces.size)

    fun updateBitFiled(pieceIndex: Int) {
        bitField[pieceIndex] = 1
    }

    fun allPiecesDownloaded() = pieces.all { it.validatePiece() }

    fun getPiece(index: Int) = pieces[index]

    suspend fun savePieceBlock(pieceIndex: Int, pieceOffset: Int, data: ByteArray) {
        val piece = pieces[pieceIndex]
        piece.setBlockData(pieceOffset, data)

        log.info("Downloaded ${"%.2f".format(progress*100)}%")
        if (piece.validatePiece()) {
            updateBitFiled(1)
            log.info("COMPLETED A PIECE ${pieceIndex}, TOTAL ${pieces.count { it.isFull }} / ${pieces.size}")
            val pieceData = piece.getData() ?: return
            fileManager.writeToFile(pieceIndex, pieceData)
            piece.purgeBlocks()
        }
    }
}