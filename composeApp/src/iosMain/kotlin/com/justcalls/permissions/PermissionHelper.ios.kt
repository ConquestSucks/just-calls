package com.justcalls.permissions

actual suspend fun requestCameraAndMicrophonePermissions(): Boolean {
    println("[PermissionHelper iOS] Запрос разрешений пока не реализован для iOS")
    return false
}


