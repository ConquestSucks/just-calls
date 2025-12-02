package com.justcalls.ui.screens.room

import androidx.compose.runtime.Composable
import com.justcalls.livekit.LiveKitManager

@Composable
expect fun rememberPermissionLauncher(
    liveKitManager: LiveKitManager,
    isMicrophoneEnabled: Boolean,
    isCameraEnabled: Boolean,
    onMicrophoneChanged: (Boolean) -> Unit,
    onCameraChanged: (Boolean) -> Unit,
    onError: (String) -> Unit
): (Array<String>) -> Unit

@Composable
expect fun hasPermission(permission: String): Boolean


