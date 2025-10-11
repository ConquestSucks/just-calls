package com.justcalls.ui.screens.home.components.headerSection

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.justcalls.ui.theme.AppColors
import com.justcalls.ui.theme.diagonalGradientBackground

@Composable
fun UserAvatar(
    text: String
) {
    Box(
        modifier = Modifier
            .size(50.dp)
            .clip(CircleShape)
            .diagonalGradientBackground(AppColors.GradientColors),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = AppColors.Primary,
            fontSize = 18.sp,
            fontWeight = FontWeight.Normal
        )
    }
}