package com.justcalls.ui.screens.room.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.justcalls.ui.screens.room.Participant
import com.justcalls.ui.theme.AppColors
import com.justcalls.ui.theme.diagonalGradientBackground
import justcalls.composeapp.generated.resources.Res

@Composable
fun ParticipantCard(
    participant: Participant,
    isMicrophoneEnabled: Boolean,
    isCameraEnabled: Boolean,
    videoSurface: Any? = null,
    liveKitManager: com.justcalls.livekit.LiveKitManager? = null,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp)
            .clip(RoundedCornerShape(16.dp))
            .diagonalGradientBackground(AppColors.GradientColors)
            .border(
                width = 1.dp,
                color = Color.White.copy(alpha = 0.2f),
                shape = RoundedCornerShape(16.dp)
            )
    ) {
        if (isCameraEnabled && videoSurface != null) {
            VideoSurfaceView(
                videoSurface = videoSurface,
                liveKitManager = liveKitManager,
                participantId = participant.id,
                modifier = Modifier.fillMaxSize()
            )
        }
        
        if (!isCameraEnabled || videoSurface == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(AppColors.Circle.copy(alpha = 0.3f))
                        .border(
                            width = 3.dp,
                            color = Color.White,
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    AsyncImage(
                        model = Res.getUri("drawable/ic_user_avatar.svg"),
                        contentDescription = "Аватар",
                        modifier = Modifier.size(48.dp)
                    )
                }
            }
        }
        
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .background(
                    color = Color.Black.copy(alpha = 0.3f),
                    shape = RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp)
                )
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${participant.name}${if (participant.isYou) " (Вы)" else ""}",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Normal
                )
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(
                                if (isMicrophoneEnabled) Color.Transparent else Color(0xFFEF4444)
                            )
                            .padding(4.dp)
                    ) {
                        AsyncImage(
                            model = Res.getUri("drawable/ic_mic_${if (isMicrophoneEnabled) "on" else "off"}.svg"),
                            contentDescription = if (isMicrophoneEnabled) "Микрофон включен" else "Микрофон выключен",
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(
                                if (isCameraEnabled) Color.Transparent else Color.White
                            )
                            .padding(4.dp)
                    ) {
                        AsyncImage(
                            model = Res.getUri("drawable/ic_camera_${if (isCameraEnabled) "on" else "off"}.svg"),
                            contentDescription = if (isCameraEnabled) "Камера включена" else "Камера выключена",
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        }
    }
}

