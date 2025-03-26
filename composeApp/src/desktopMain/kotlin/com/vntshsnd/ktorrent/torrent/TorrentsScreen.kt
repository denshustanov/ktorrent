package com.vntshsnd.ktorrent.torrent

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.Button
import androidx.compose.material.Divider
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.input.pointer.pointerMoveFilter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vntshsnd.ktorrent.state.SessionManagerViewModel
import java.awt.FileDialog
import java.awt.Frame
import kotlin.io.path.Path

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun TorrentsScreen(viewModel: SessionManagerViewModel) {
    val sessions = viewModel.sessions

    fun createSession() {
        val fileDialog = FileDialog(Frame(), "Select a .torrent file", FileDialog.LOAD)
        fileDialog.isVisible = true
        if (fileDialog.file == null) return

        val path = Path(fileDialog.directory, fileDialog.file)

        viewModel.createSession(path.toString())
    }

    Surface(modifier = Modifier.fillMaxHeight()) {
        Column(Modifier.fillMaxWidth().padding(vertical = 12.dp)) {
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "All torrents",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                Button(onClick = { createSession() }) {
                    Text("Add torrent", fontSize = 14.sp)

                }
            }
            TorrentsListHeader()
            Divider(thickness = 1.dp, color = Color(0xFFE0E0E0))
            LazyColumn {
                itemsIndexed(sessions) { i, torrent ->
                    Column {
                        var isHovered by remember { mutableStateOf(false) }
                        Box(
                            modifier = Modifier.background(
                                if (isHovered) Color(0xFFF0F0F0)
                                else if (i % 2 == 1) Color(0xFFF5F5F5)
                                else Color.White
                            ).onPointerEvent(eventType = PointerEventType.Enter) { isHovered = true }
                                .onPointerEvent(eventType = PointerEventType.Exit) { isHovered = false }
                        ) {
                            TorrentItem(i + 1, torrent)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TorrentsListHeader() {
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "No",
            fontSize = 12.sp,
            modifier = Modifier.width(30.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "Name",
            fontSize = 12.sp,
            modifier = Modifier.weight(0.8f)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "Progress",
            fontSize = 12.sp,
            modifier = Modifier.weight(1f)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "Down. speed",
            fontSize = 12.sp,
            modifier = Modifier.weight(0.3f)
        )
    }
}

