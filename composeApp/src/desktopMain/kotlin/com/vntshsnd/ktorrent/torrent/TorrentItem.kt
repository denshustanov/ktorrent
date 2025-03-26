package com.vntshsnd.ktorrent.torrent

import androidx.compose.foundation.layout.*
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vntshsnd.ktorrent.session.TorrentSession
import kotlinx.coroutines.delay

@Composable
fun TorrentItem(index: Int, torrentSession: TorrentSession) {
    var progress by remember { mutableStateOf(torrentSession.progress) }
    var downSpeed by remember { mutableStateOf(torrentSession.downSpeed) }
    var status by remember { mutableStateOf(torrentSession.status) }

    LaunchedEffect(Unit) {
        while (true) {
            progress = torrentSession.progress
            downSpeed = torrentSession.downSpeed
            status = torrentSession.status
            println("$progress, $downSpeed, $status")
            delay(1000)
        }
    }


    TorrentItemContextMenu(torrentSession) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.height(50.dp).padding(vertical = 8.dp, horizontal = 12.dp)
        ) {
            Text(
                text = "$index.",
                modifier = Modifier.width(30.dp),
                fontSize = 12.sp,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                modifier = Modifier.weight(0.8f),
                text = torrentSession.name,
                fontSize = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(modifier = Modifier.width(8.dp))
            Box(modifier = Modifier.height(30.dp).weight(1f)) {
                LinearProgressIndicator(
                    progress = progress.toFloat(),
                    modifier = Modifier.fillMaxSize(),
                )
                Text(
                    text = "$status Downloaded: ${"%.2f".format(progress.toFloat() * 100)}%",
                    modifier = Modifier.align(Alignment.TopCenter),
                    fontSize = 12.sp
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "${downSpeed.formatBytes()}/S",
                modifier = Modifier.weight(0.3f),
                fontSize = 12.sp,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

fun Double.formatBytes(): String {
    val suffixes = arrayOf("", "K", "M", "G", "T")

    var value = this
    var suffixIndex = 0

    while (value >= 1024 && suffixIndex < suffixes.size - 1) {
        value /= 1024
        suffixIndex++
    }

    return "%.2f ${suffixes[suffixIndex]}B".format(value)
}