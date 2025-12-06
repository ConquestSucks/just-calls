package com.justcalls.permissions

actual suspend fun requestCameraAndMicrophonePermissions(): Boolean {
    // Not implemented for iOS yet
    return false
}


