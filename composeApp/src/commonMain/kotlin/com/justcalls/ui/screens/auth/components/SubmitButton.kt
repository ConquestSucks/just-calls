package com.justcalls.ui.screens.auth.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.justcalls.ui.theme.AppColors
import com.justcalls.ui.theme.diagonalGradientBackground
import com.justcalls.ui.screens.auth.domain.AuthTab
import com.justcalls.ui.screens.auth.domain.RegisterStep

@Composable
fun SubmitButton(
    tab: AuthTab,
    registerStep: RegisterStep,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1f,
        animationSpec = tween(durationMillis = 100),
        label = "button_scale"
    )
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .scale(scale)
            .clip(RoundedCornerShape(16.dp))
            .diagonalGradientBackground(AppColors.GradientColors)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = when {
                tab == AuthTab.LOGIN -> "Войти"
                tab == AuthTab.REGISTER && registerStep == RegisterStep.PASSWORD -> "Завершить регистрацию"
                else -> "Продолжить"
            },
            color = AppColors.Primary,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

