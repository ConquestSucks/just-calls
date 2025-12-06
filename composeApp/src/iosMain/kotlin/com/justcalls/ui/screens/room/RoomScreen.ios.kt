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
        permissions.forEach { permission ->
            when (permission) {
                "CAMERA" -> {
                    // Явно запрашиваем разрешение на камеру перед включением
                    @Suppress("UNCHECKED_CAST")
                    val manager = liveKitManager as? com.justcalls.livekit.LiveKitManager
                    manager?.requestCameraPermission { granted ->
                        if (granted) {
                            if (isCameraEnabled) {
                                liveKitManager.setCameraEnabled(true)
                            }
                        } else {
                            onCameraChanged(false)
                            onError("Требуется разрешение на использование камеры")
                        }
                    }
                }
                "RECORD_AUDIO" -> {
                    // Явно запрашиваем разрешение на микрофон перед включением
                    @Suppress("UNCHECKED_CAST")
                    val manager = liveKitManager as? com.justcalls.livekit.LiveKitManager
                    manager?.requestMicrophonePermission { granted ->
                        if (granted) {
                            if (isMicrophoneEnabled) {
                                liveKitManager.setMicrophoneEnabled(true)
                            }
                        } else {
                            onMicrophoneChanged(false)
                            onError("Требуется разрешение на использование микрофона")
                        }
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

