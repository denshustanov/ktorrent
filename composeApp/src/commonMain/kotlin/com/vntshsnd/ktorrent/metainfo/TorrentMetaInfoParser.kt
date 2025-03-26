package com.vntshsnd.ktorrent.metainfo

import be.adaxisoft.bencode.BDecoder
import be.adaxisoft.bencode.BEncodedValue
import be.adaxisoft.bencode.BEncoder
import com.vntshsnd.ktorrent.metainfo.model.File
import com.vntshsnd.ktorrent.metainfo.model.Info
import com.vntshsnd.ktorrent.metainfo.model.TorrentMetaInfo
import java.io.InputStream
import java.math.BigInteger
import java.security.MessageDigest

class TorrentMetaInfoParser {
    fun parse(inputStream: InputStream): TorrentMetaInfo? {
        val decoded = BDecoder.decode(inputStream).value as Map<String, BEncodedValue>

        val announce = decoded["announce"]?.let { String(it.value as ByteArray) } ?: return null
        val announceList = (decoded["announce-list"]?.value as? List<BEncodedValue>)
            ?.map { it.value as List<BEncodedValue>  }
            ?.flatten()
            ?.map { String(it.value as ByteArray) }
        val info = decoded["info"]?.let { parseInfo(it) } ?: return null

        return TorrentMetaInfo(announceList ?: listOf(announce), info)
    }

    private fun parseInfo(value: BEncodedValue): Info? {
        val infoMap = value.value as Map<String, BEncodedValue>

        val encodedInfo = BEncoder.encode(infoMap).array()
        val hash = MessageDigest.getInstance("SHA-1").digest(encodedInfo)

        val name = (infoMap["name"]?.value as? ByteArray)?.let { String(it) } ?: return null
        val nameUtf8 = (infoMap["name.utf-8"]?.value as? ByteArray)?.let { String(it) }
        val pieceLength = (infoMap["piece length"]?.value as? BigInteger) ?: return null
        val pieces = (infoMap["pieces"]?.value as? ByteArray)?.toList()?.chunked(20)?.map { it.toByteArray() } ?: return null
        val length = (infoMap["length"]?.value as? BigInteger)
        val files = (infoMap["files"]?.value as? List<BEncodedValue>)?.mapNotNull {parseFile(it)} ?: listOf(
            File(
                length!!,
                listOf(name),
                nameUtf8?.let { listOf(it) },
            )
        )

        return Info(
            hash,
            name,
            nameUtf8,
            pieceLength,
            pieces,
            length,
            files
            )
    }

    private fun parseFile(value: BEncodedValue) : File? {
        val fileMap = value.value as Map<String, BEncodedValue>
        val path = fileMap["path"]?.let { parsePath(it) } ?: return null
        val pathUtf8 = fileMap["path.utf-8"]?.let { parsePath(it) }
        val length = fileMap["length"]?.value as? BigInteger ?: return null
        return File(
            length,
            path,
            pathUtf8
        )
    }

    private fun parsePath(value: BEncodedValue): List<String> {
        return (value.value as List<BEncodedValue>).map { String(it.value as ByteArray) }
    }
}