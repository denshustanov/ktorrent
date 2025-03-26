package com.vntshsnd.ktorrent

import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import java.awt.Dimension

fun main() = application {
    val state = rememberWindowState(
        width = 640.dp,
        height = 480.dp
    )
    Window(
        onCloseRequest = ::exitApplication,
        title = "KTorrent",
        state = state,
    ) {
        window.minimumSize = Dimension(640, 480)
        App()
    }
}