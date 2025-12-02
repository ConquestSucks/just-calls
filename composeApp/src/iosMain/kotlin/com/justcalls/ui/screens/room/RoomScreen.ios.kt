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
    return { permissions ->
        println("[RoomScreen iOS] Запрос разрешений пока не реализован")
    }
}

@Composable
actual fun hasPermission(permission: String): Boolean {
    return false
}

