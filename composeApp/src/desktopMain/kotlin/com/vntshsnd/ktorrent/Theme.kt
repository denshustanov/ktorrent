package com.vntshsnd.ktorrent

import androidx.compose.material.MaterialTheme
import androidx.compose.material.Shapes
import androidx.compose.material.Typography
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@Composable
fun AppTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colors = lightColors(
            primary = Color(0xFF43A047),
            secondary = Color(0xFF66BB6A),
            background = Color(0xFFF5F5F5),
            surface = Color(0xFFFFFFFF),
            onPrimary = Color(0xFFFFFFFF),
            onSecondary = Color(0xFF1B5E20),
            error = Color(0xFFD32F2F)
        ),
        typography = Typography(),
        shapes = Shapes(),
        content = content
    )
}
