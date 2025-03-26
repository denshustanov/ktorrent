package com.vntshsnd.ktorrent

import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.*
import com.vntshsnd.ktorrent.state.SessionManagerViewModel
import com.vntshsnd.ktorrent.torrent.TorrentsScreen
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun App() {
    AppTheme {
       TorrentsScreen(SessionManagerViewModel())
    }
}