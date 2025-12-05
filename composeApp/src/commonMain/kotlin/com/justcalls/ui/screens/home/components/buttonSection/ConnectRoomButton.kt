package com.justcalls.ui.screens.home.components.buttonSection

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import justcalls.composeapp.generated.resources.Res

@Composable
fun ConnectRoomButton() {
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
                onClick = {}
            )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .border(
                    width = 2.dp,
                    color = AppColors.Circle,
                    shape = RoundedCornerShape(16.dp)
                )
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
                        model = Res.getUri("drawable/ic_login.svg"),
                        contentDescription = "Войти в комнату",
                        modifier = Modifier.size(30.dp),
                    )
                }
                Spacer(Modifier.height(24.dp))
                Text(
                    text = "Войти",
                    fontSize = 12.sp,
                    color = AppColors.TextSecondary,
                    modifier = Modifier.padding(start = 4.dp)
                )
                Spacer(Modifier.width(11.dp))
                Text(
                    text = "По коду",
                    fontSize = 11.sp,
                    color = AppColors.TextSecondaryAccent,
                    modifier = Modifier.padding(start = 4.dp)
                )
            }
        }
    }
}