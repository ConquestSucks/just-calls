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
                    println("RoomScreen.ios: Requesting camera permission")
                    manager?.requestCameraPermission { granted ->
                        println("RoomScreen.ios: Camera permission granted: $granted")
                        if (granted) {
                            // После получения разрешения всегда включаем камеру
                            // (пользователь нажал кнопку для включения)
                            onCameraChanged(true)
                            println("RoomScreen.ios: Calling setCameraEnabled(true)")
                            liveKitManager.setCameraEnabled(true)
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
                    println("RoomScreen.ios: Requesting microphone permission")
                    manager?.requestMicrophonePermission { granted ->
                        println("RoomScreen.ios: Microphone permission granted: $granted")
                        if (granted) {
                            // После получения разрешения всегда включаем микрофон
                            // (пользователь нажал кнопку для включения)
                            onMicrophoneChanged(true)
                            println("RoomScreen.ios: Calling setMicrophoneEnabled(true)")
                            liveKitManager.setMicrophoneEnabled(true)
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

