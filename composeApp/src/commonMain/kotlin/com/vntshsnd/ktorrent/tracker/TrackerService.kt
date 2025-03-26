package com.vntshsnd.ktorrent.tracker

import be.adaxisoft.bencode.BDecoder
import be.adaxisoft.bencode.BEncodedValue
import com.vntshsnd.ktorrent.encodeUrlString
import com.vntshsnd.ktorrent.peer.model.HostPort
import com.vntshsnd.ktorrent.metainfo.model.TorrentMetaInfo
import okhttp3.OkHttpClient
import okhttp3.Request
import org.slf4j.LoggerFactory

class TrackerService private constructor(){
    companion object {
        val instance = TrackerService()
        val log = LoggerFactory.getLogger(TrackerService::class.java)
    }

    private val httpClient = OkHttpClient()
    fun queryPeers(torrentMetaInfo: TorrentMetaInfo, peerId: ByteArray): List<HostPort> {

        val length = torrentMetaInfo.info.length
            ?: torrentMetaInfo.info.files
                .map { it.length }
                .reduce{ a, b -> a + b}

        val queryParams = mapOf(
            "info_hash" to torrentMetaInfo.info.hash.encodeUrlString(),
            "peer_id" to peerId.encodeUrlString(),
            "port" to "6881",
            "uploaded" to "i0e",
            "downloaded" to "i0e",
            "left" to "i${length}e",
        )

        return torrentMetaInfo.announces.mapNotNull{
            try {
                val url = "${it}?${queryParams.map { (k, v) -> "$k=$v" }.joinToString("&")}"

                val request = Request.Builder()
                    .url(url)
                    .get()
                    .build()

                val response = httpClient.newCall(request).execute()
                if (!response.isSuccessful) return@mapNotNull null
                val bodyStream = response.body?.bytes()?.inputStream() ?: return@mapNotNull null
                val decodedBody = BDecoder.decode(bodyStream)
                return@mapNotNull ((decodedBody.value as Map<String, BEncodedValue>)["peers"]
                    ?.value as? ByteArray)
                    ?.toList()
                    ?.chunked(6)
                    ?.map { peerChunk ->
                        val ipv4 = peerChunk.take(4).joinToString(".") { (it.toInt() and 0xFF).toString() }
                        val port = ((peerChunk[4].toInt() and 0xFF) shl 8) or
                                (peerChunk[5].toInt() and 0xFF)

                        HostPort(ipv4, port)
                    }
            } catch (e: Exception) {
                log.info(e.message)
                return@mapNotNull null
            }
        }.flatten()
    }
}