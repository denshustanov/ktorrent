package com.vntshsnd.ktorrent.state

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import com.vntshsnd.ktorrent.session.SessionManager
import com.vntshsnd.ktorrent.session.TorrentSession
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SessionManagerViewModel : ViewModel() {
    var sessionManager = SessionManager()
    private val _sessions = mutableStateListOf<TorrentSession>()
    val sessions: List<TorrentSession> get() = _sessions

    fun createSession(metaInfoPath: String) = sessionManager.createSession(metaInfoPath)?.let {
        _sessions.add(it)
    }
}