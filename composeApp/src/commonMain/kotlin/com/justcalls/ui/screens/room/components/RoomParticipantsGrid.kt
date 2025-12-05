package com.justcalls.ui.screens.room.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.justcalls.livekit.LiveKitManager
import com.justcalls.livekit.LiveKitParticipant
import com.justcalls.ui.screens.room.Participant
import com.justcalls.ui.theme.AppColors

@Composable
fun RoomParticipantsGrid(
    participants: List<LiveKitParticipant>,
    liveKitManager: LiveKitManager,
    connectionError: String?,
    modifier: Modifier = Modifier
) {
    if (connectionError != null) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 25.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = connectionError,
                color = AppColors.Error,
                fontSize = 14.sp
            )
        }
    } else {
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = modifier.fillMaxWidth().padding(horizontal = 25.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 12.dp)
        ) {
            items(participants) { liveKitParticipant: LiveKitParticipant ->
                var videoSurface by remember(liveKitParticipant.identity, liveKitParticipant.isCameraEnabled) { 
                    mutableStateOf<Any?>(null) 
                }
                
                LaunchedEffect(liveKitParticipant.identity, liveKitParticipant.isCameraEnabled) {
                    println("[RoomParticipantsGrid] LaunchedEffect для участника ${liveKitParticipant.identity}, isCameraEnabled=${liveKitParticipant.isCameraEnabled}, isLocal=${liveKitParticipant.isLocal}")
                    if (liveKitParticipant.isCameraEnabled) {
                        println("[RoomParticipantsGrid] Запрашиваем видео поверхность для ${liveKitParticipant.identity}")
                        var attempts = 0
                        while (attempts < 15) {
                            val surface = liveKitManager.getVideoSurface(liveKitParticipant.identity)
                            if (surface != null) {
                                videoSurface = surface
                                println("[RoomParticipantsGrid] Получена видео поверхность для ${liveKitParticipant.identity} с попытки $attempts: $surface")
                                break
                            } else {
                                kotlinx.coroutines.delay(200)
                                attempts++
                            }
                        }
                        if (videoSurface == null) {
                            println("[RoomParticipantsGrid] Не удалось получить видео поверхность для ${liveKitParticipant.identity} после $attempts попыток")
                        }
                    } else {
                        println("[RoomParticipantsGrid] Камера выключена, очищаем видео поверхность")
                        videoSurface = null
                    }
                }
                
                ParticipantCard(
                    participant = Participant(
                        id = liveKitParticipant.identity,
                        name = liveKitParticipant.name,
                        isYou = liveKitParticipant.isLocal
                    ),
                    isMicrophoneEnabled = liveKitParticipant.isMicrophoneEnabled,
                    isCameraEnabled = liveKitParticipant.isCameraEnabled,
                    videoSurface = videoSurface,
                    liveKitManager = liveKitManager
                )
            }
        }
    }
}

