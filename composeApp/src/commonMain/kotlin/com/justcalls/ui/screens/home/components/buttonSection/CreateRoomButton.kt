package com.justcalls.ui.screens.home.components.buttonSection

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.justcalls.ui.theme.AppColors
import com.justcalls.ui.theme.diagonalGradientBackground
import justcalls.composeapp.generated.resources.Res

@Composable
fun CreateRoomButton(
    onClick: () -> Unit = {}
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        label = "pressScale"
    )

    Box(
        modifier = Modifier
            .scale(scale)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .diagonalGradientBackground(AppColors.GradientColors)
                .padding(22.dp, 24.dp, 0.dp, 32.dp)
        ) {
            Column {
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(AppColors.Circle)
                        .padding(10.dp)
                ) {
                    AsyncImage(
                        model = Res.getUri("drawable/ic_create.svg"),
                        contentDescription = "Создать",
                        modifier = Modifier.size(30.dp),
                    )
                }
                Spacer(Modifier.height(24.dp))
                Text(
                    text = "Создать",
                    fontSize = 12.sp,
                    color = AppColors.Primary
                )
                Spacer(Modifier.width(11.dp))
                Text(
                    text = "Новая комната",
                    fontSize = 11.sp,
                    color = AppColors.TextPrimaryAccent
                )
            }
        }
    }
}