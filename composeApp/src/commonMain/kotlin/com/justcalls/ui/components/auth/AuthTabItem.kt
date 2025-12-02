package com.justcalls.ui.components.auth

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.justcalls.ui.theme.AppColors
import com.justcalls.ui.theme.diagonalGradientBackground

@Composable
fun AuthTabItem(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val density = LocalDensity.current
    var textWidth by remember { mutableStateOf(0.dp) }
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    val alpha by animateFloatAsState(
        targetValue = if (isPressed) 0.7f else 1f,
        animationSpec = tween(durationMillis = 100),
        label = "tab_alpha"
    )
    
    val underlineAlpha by animateFloatAsState(
        targetValue = if (isSelected) 1f else 0f,
        animationSpec = tween(durationMillis = 300),
        label = "underline_alpha"
    )
    
    Column(
        modifier = Modifier
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .alpha(alpha)
            .wrapContentWidth(Alignment.Start),
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = text,
            color = if (isSelected) AppColors.GradientColors[0] else AppColors.TabInactive,
            fontSize = 17.sp,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            modifier = Modifier.onGloballyPositioned { coordinates ->
                textWidth = with(density) { coordinates.size.width.toDp() }
            }
        )
        Spacer(modifier = Modifier.height(10.dp))
        Box(
            modifier = Modifier
                .width(textWidth)
                .height(3.dp)
                .alpha(underlineAlpha)
                .diagonalGradientBackground(AppColors.GradientColors)
        )
    }
}


