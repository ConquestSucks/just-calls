package com.justcalls.ui.screens.room

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.justcalls.livekit.LiveKitManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import platform.AVFoundation.*
import platform.Foundation.NSError
import kotlinx.cinterop.ExperimentalForeignApi

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun rememberPermissionLauncher(
    liveKitManager: LiveKitManager,
    isMicrophoneEnabled: Boolean,
    isCameraEnabled: Boolean,
    onMicrophoneChanged: (Boolean) -> Unit,
    onCameraChanged: (Boolean) -> Unit,
    onError: (String) -> Unit
): (Array<String>) -> Unit {
    val scope = remember { CoroutineScope(Dispatchers.Main) }
    
    return remember {
        { permissions ->
            scope.launch {
                permissions.forEach { permission ->
                    when (permission) {
                        "CAMERA" -> {
                            val status = AVCaptureDevice.authorizationStatusForMediaType(AVMediaTypeVideo)
                            when (status) {
                                AVAuthorizationStatusNotDetermined -> {
                                    AVCaptureDevice.requestAccessForMediaType(AVMediaTypeVideo) { granted ->
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
                                AVAuthorizationStatusAuthorized -> {
                                    if (isCameraEnabled) {
                                        liveKitManager.setCameraEnabled(true)
                                    }
                                }
                                else -> {
                                    onCameraChanged(false)
                                    onError("Требуется разрешение на использование камеры")
                                }
                            }
                        }
                        "RECORD_AUDIO" -> {
                            val audioSession = AVAudioSession.sharedInstance()
                            val currentPermission = audioSession.recordPermission
                            when (currentPermission) {
                                AVAudioSessionRecordPermissionUndetermined -> {
                                    audioSession.requestRecordPermission { granted ->
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
                                AVAudioSessionRecordPermissionGranted -> {
                                    if (isMicrophoneEnabled) {
                                        liveKitManager.setMicrophoneEnabled(true)
                                    }
                                }
                                else -> {
                                    onMicrophoneChanged(false)
                                    onError("Требуется разрешение на использование микрофона")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun hasPermission(permission: String): Boolean {
    return remember(permission) {
        when (permission) {
            "CAMERA" -> {
                val status = AVCaptureDevice.authorizationStatusForMediaType(AVMediaTypeVideo)
                status == AVAuthorizationStatusAuthorized
            }
            "RECORD_AUDIO" -> {
                val audioSession = AVAudioSession.sharedInstance()
                audioSession.recordPermission == AVAudioSessionRecordPermissionGranted
            }
            else -> false
        }
    }
}

