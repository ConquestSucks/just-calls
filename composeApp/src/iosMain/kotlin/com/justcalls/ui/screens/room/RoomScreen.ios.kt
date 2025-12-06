package com.justcalls.ui.screens.room

import androidx.compose.runtime.Composable
import com.justcalls.livekit.LiveKitManager

@Composable
actual fun rememberPermissionLauncher(
    liveKitManager: LiveKitManager,
    isMicrophoneEnabled: Boolean,
    isCameraEnabled: Boolean,
    onMicrophoneChanged: (Boolean) -> Unit,
    onCameraChanged: (Boolean) -> Unit,
    onError: (String) -> Unit
): (Array<String>) -> Unit {
    // На iOS LiveKit SDK автоматически запрашивает разрешения при первом использовании
    // через room.localParticipant.setCamera/setMicrophone
    // Вызываем методы LiveKit, которые запросят разрешения через систему iOS
    return { permissions ->
        permissions.forEach { permission ->
            when (permission) {
                "CAMERA" -> {
                    // LiveKit SDK запросит разрешение автоматически при вызове setCameraEnabled
                    // iOS покажет системный диалог, если разрешение еще не запрошено
                    if (isCameraEnabled) {
                        liveKitManager.setCameraEnabled(true)
                    }
                }
                "RECORD_AUDIO" -> {
                    // LiveKit SDK запросит разрешение автоматически при вызове setMicrophoneEnabled
                    // iOS покажет системный диалог, если разрешение еще не запрошено
                    if (isMicrophoneEnabled) {
                        liveKitManager.setMicrophoneEnabled(true)
                    }
                }
            }
        }
    }
}

@Composable
actual fun hasPermission(permission: String): Boolean {
    // На iOS разрешения запрашиваются автоматически через LiveKit SDK
    // Возвращаем false, чтобы всегда вызывался permissionLauncher,
    // который вызовет методы LiveKit, запрашивающие разрешения
    return false
}

