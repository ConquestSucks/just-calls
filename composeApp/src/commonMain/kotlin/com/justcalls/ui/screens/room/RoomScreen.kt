package com.justcalls.ui.screens.room

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.justcalls.data.network.RoomService
import com.justcalls.livekit.LiveKitManager
import com.justcalls.livekit.LiveKitParticipant
import com.justcalls.ui.components.common.IconPressableButton
import com.justcalls.ui.screens.room.components.RoomControls
import com.justcalls.ui.screens.room.components.RoomParticipantsGrid
import com.justcalls.ui.screens.room.connection.RoomConnectionHandler
import com.justcalls.ui.theme.AppColors
import com.justcalls.ui.utils.BackHandler

data class Participant(
    val id: String,
    val name: String,
    val isYou: Boolean = false
)

@Composable
fun RoomScreen(
    roomName: String,
    onBackClick: () -> Unit = {},
    roomService: RoomService? = null
) {
    var isMicrophoneEnabled by remember { mutableStateOf(false) }
    var isCameraEnabled by remember { mutableStateOf(false) }
    var participants by remember { mutableStateOf<List<LiveKitParticipant>>(emptyList()) }
    var connectionError by remember { mutableStateOf<String?>(null) }
    
    val liveKitManager = remember { LiveKitManager() }
    
    val hasCameraPermission = hasPermission("CAMERA")
    val hasAudioPermission = hasPermission("RECORD_AUDIO")
    
    val permissionLauncher = rememberPermissionLauncher(
        liveKitManager = liveKitManager,
        isMicrophoneEnabled = isMicrophoneEnabled,
        isCameraEnabled = isCameraEnabled,
        onMicrophoneChanged = { isMicrophoneEnabled = it },
        onCameraChanged = { isCameraEnabled = it },
        onError = { connectionError = it }
    )
    
    LaunchedEffect(liveKitManager) {
        liveKitManager.observeParticipants { updatedParticipants: List<LiveKitParticipant> ->
            participants = updatedParticipants
            val localParticipant = updatedParticipants.find { participant: LiveKitParticipant -> participant.isLocal }
            if (localParticipant != null) {
                isMicrophoneEnabled = localParticipant.isMicrophoneEnabled
                isCameraEnabled = localParticipant.isCameraEnabled
            }
        }
    }
    
    RoomConnectionHandler(
        roomName = roomName,
        roomService = roomService,
        liveKitManager = liveKitManager,
        onConnectionError = { error -> connectionError = error }
    )
    
    BackHandler(enabled = true) {
        onBackClick()
    }
    
    MaterialTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = AppColors.Surface
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 25.dp, vertical = 20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconPressableButton(
                        imgPath = "drawable/ic_back.svg",
                        contentDescription = "Назад",
                        onClick = onBackClick
                    )
                    Text(
                        text = roomName,
                        color = AppColors.TextHeader,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(50.dp))
                }
                
                RoomParticipantsGrid(
                    participants = participants,
                    liveKitManager = liveKitManager,
                    connectionError = connectionError,
                    modifier = Modifier.weight(1f)
                )
                
                RoomControls(
                    isMicrophoneEnabled = isMicrophoneEnabled,
                    isCameraEnabled = isCameraEnabled,
                    onMicrophoneToggle = { 
                        val newValue = !isMicrophoneEnabled
                        
                        if (newValue) {
                            if (hasAudioPermission) {
                                isMicrophoneEnabled = true
                                liveKitManager.setMicrophoneEnabled(true)
                            } else {
                                permissionLauncher(arrayOf("RECORD_AUDIO"))
                            }
                        } else {
                            isMicrophoneEnabled = false
                            liveKitManager.setMicrophoneEnabled(false)
                        }
                    },
                    onCameraToggle = { 
                        val newValue = !isCameraEnabled
                        
                        if (newValue) {
                            if (hasCameraPermission) {
                                isCameraEnabled = true
                                liveKitManager.setCameraEnabled(true)
                            } else {
                                permissionLauncher(arrayOf("CAMERA"))
                            }
                        } else {
                            isCameraEnabled = false
                            liveKitManager.setCameraEnabled(false)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 25.dp, vertical = 20.dp)
                )
            }
        }
    }
}

