package com.justcalls.ui.components.common

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import justcalls.composeapp.generated.resources.Res

@Composable
fun IconPressableButton(
    imgPath: String,
    contentDescription: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 50.dp,
    background: Color = Color.Transparent,
    pressedBackground: Color = Color(0xFF6665F1),
    iconTint: Color? = null,
    shape: Shape = CircleShape,
    enabled: Boolean = true
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val currentBackground by animateColorAsState(targetValue = if (isPressed) pressedBackground else background)

    Box(
        modifier = modifier
            .size(size)
            .clip(shape)
            .background(color = currentBackground, shape = shape)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        AsyncImage(
            model = Res.getUri(imgPath),
            contentDescription = contentDescription,
            modifier = Modifier.size(size / 2),
            colorFilter = iconTint?.let { ColorFilter.tint(it) }
        )
    }
}


