package com.vntshsnd.ktorrent.session

import com.vntshsnd.ktorrent.metainfo.TorrentMetaInfoParser
import org.slf4j.LoggerFactory
import java.io.File

class SessionManager {
    companion object {
        private val log = LoggerFactory.getLogger(SessionManager::class.java)
    }


    private val metaInfoParser = TorrentMetaInfoParser()

    fun createSession(filePath: String): TorrentSession? {
        val file = File(filePath)
        val metaInfo = metaInfoParser.parse(file.inputStream()) ?: return null
        val session = TorrentSession(metaInfo)
        session.startSession()
        return session
    }
}