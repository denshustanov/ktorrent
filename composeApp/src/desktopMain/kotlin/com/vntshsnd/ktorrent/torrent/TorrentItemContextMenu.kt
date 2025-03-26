package com.vntshsnd.ktorrent.torrent

import androidx.compose.foundation.ContextMenuArea
import androidx.compose.foundation.ContextMenuItem
import androidx.compose.runtime.Composable
import com.vntshsnd.ktorrent.session.TorrentSession

@Composable
fun TorrentItemContextMenu(torrentSession: TorrentSession, content: @Composable () -> Unit) {
    ContextMenuArea(items = { buildContextMenuItems(torrentSession) }) {
        content()
    }
}

private fun buildContextMenuItems(torrentSession: TorrentSession): List<ContextMenuItem> {
    return listOf(
        ContextMenuItem(onClick = {}, label = "Pause")
    )
}