package com.justcalls.ui.theme

import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed

fun Modifier.diagonalGradientBackground(
    colors: List<Color>
): Modifier = composed {
    this.then(
        Modifier.drawWithContent {
            val brush = Brush.linearGradient(
                colors = colors,
                start = Offset(0f, 0f),
                end = Offset(size.width, size.height)
            )
            drawRect(brush = brush)
            drawContent()
        }
    )
}
