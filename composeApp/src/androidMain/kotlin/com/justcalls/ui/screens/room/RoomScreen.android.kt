package com.justcalls.ui.screens.room

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
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
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val cameraGranted = permissions[Manifest.permission.CAMERA] ?: false
        val audioGranted = permissions[Manifest.permission.RECORD_AUDIO] ?: false
        
        if (cameraGranted && isCameraEnabled) {
            liveKitManager.setCameraEnabled(true)
        } else if (!cameraGranted && isCameraEnabled) {
            onCameraChanged(false)
            onError("Требуется разрешение на использование камеры")
        }
        
        if (audioGranted && isMicrophoneEnabled) {
            liveKitManager.setMicrophoneEnabled(true)
        } else if (!audioGranted && isMicrophoneEnabled) {
            onMicrophoneChanged(false)
            onError("Требуется разрешение на использование микрофона")
        }
    }
    
    return { permissions ->
        val androidPermissions = permissions.map { permission ->
            when (permission) {
                "CAMERA" -> Manifest.permission.CAMERA
                "RECORD_AUDIO" -> Manifest.permission.RECORD_AUDIO
                else -> permission
            }
        }.toTypedArray()
        launcher.launch(androidPermissions)
    }
}

@Composable
actual fun hasPermission(permission: String): Boolean {
    val context = LocalContext.current
    val androidPermission = when (permission) {
        "CAMERA" -> Manifest.permission.CAMERA
        "RECORD_AUDIO" -> Manifest.permission.RECORD_AUDIO
        else -> permission
    }
    return ContextCompat.checkSelfPermission(context, androidPermission) == PackageManager.PERMISSION_GRANTED
}

