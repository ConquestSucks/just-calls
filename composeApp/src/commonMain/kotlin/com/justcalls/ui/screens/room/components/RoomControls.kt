package com.justcalls.ui.screens.room.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.justcalls.ui.theme.AppColors
import justcalls.composeapp.generated.resources.Res

@Composable
fun RoomControls(
    isMicrophoneEnabled: Boolean,
    isCameraEnabled: Boolean,
    onMicrophoneToggle: () -> Unit,
    onCameraToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(
                    color = if (isMicrophoneEnabled) 
                        AppColors.Circle.copy(alpha = 0.2f) 
                    else 
                        Color(0xFFEF4444).copy(alpha = 0.2f)
                )
                .clickable(onClick = onMicrophoneToggle)
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            AsyncImage(
                model = Res.getUri("drawable/ic_mic_${if (isMicrophoneEnabled) "on" else "off"}.svg"),
                contentDescription = if (isMicrophoneEnabled) "Выключить микрофон" else "Включить микрофон",
                modifier = Modifier.size(32.dp)
            )
        }
        
        Spacer(modifier = Modifier.width(24.dp))
        
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(
                    color = if (isCameraEnabled) 
                        AppColors.Circle.copy(alpha = 0.2f) 
                    else 
                        Color.White.copy(alpha = 0.2f)
                )
                .clickable(onClick = onCameraToggle)
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            AsyncImage(
                model = Res.getUri("drawable/ic_camera_${if (isCameraEnabled) "on" else "off"}.svg"),
                contentDescription = if (isCameraEnabled) "Выключить камеру" else "Включить камеру",
                modifier = Modifier.size(32.dp)
            )
        }
    }
}


