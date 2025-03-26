package com.vntshsnd.ktorrent.files

import com.vntshsnd.ktorrent.metainfo.model.TorrentMetaInfo
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.slf4j.LoggerFactory
import java.io.File
import java.io.RandomAccessFile
import kotlin.math.min

class FileManager(private val torrentMetaInfo: TorrentMetaInfo, private val downloadDir: String) {
    private val files = mutableListOf<RandomAccessFile>()

    private val mutexes = mutableMapOf<RandomAccessFile, Mutex>()

    companion object {
        private val log = LoggerFactory.getLogger(FileManager::class.java)
    }

    fun initFiles() {
        for (fileDescriptor in torrentMetaInfo.info.files) {
            val file =
                File(downloadDir, (fileDescriptor.pathUtf8 ?: fileDescriptor.path).joinToString(File.pathSeparator))

            file.parentFile?.mkdirs()
            if (!file.exists()) {
                file.createNewFile()
                log.info("Created file ${file.path}")
            }

            val raf = RandomAccessFile(file, "rw")
            raf.setLength(fileDescriptor.length.toLong())

            files.add(raf)
            mutexes[raf] = Mutex()
        }
    }

     suspend fun writeToFile(piece: Int, data: ByteArray) {
        val pieceOffset = piece * torrentMetaInfo.info.pieceLength.toInt()
        var remainingPieceData = data
        var offsetFromFileStart = pieceOffset


        for ((i, fileDescriptor) in torrentMetaInfo.info.files.withIndex()) {
            val fileLength = fileDescriptor.length.toInt()
            if (offsetFromFileStart >= fileLength) {
                offsetFromFileStart -= fileLength
                continue
            }

            val file = files[i]
            val mutex = mutexes[file] ?: break
            val bytesToWrite = min(data.size, fileLength - offsetFromFileStart)

            mutex.withLock {
                log.info("writing $bytesToWrite of ${data.size} from piece $piece")
                file.seek(offsetFromFileStart.toLong())
                file.write(remainingPieceData)
            }

            remainingPieceData = remainingPieceData.copyOfRange(bytesToWrite, remainingPieceData.size)

            if (remainingPieceData.isEmpty()) break
        }
    }
}