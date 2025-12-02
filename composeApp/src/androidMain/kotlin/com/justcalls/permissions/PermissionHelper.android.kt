package com.justcalls.permissions

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.ComponentActivity
import androidx.core.content.ContextCompat
import com.justcalls.JustCallsApplication
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

actual suspend fun requestCameraAndMicrophonePermissions(): Boolean {
    val context = JustCallsApplication.instance
    
    val hasCamera = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
    val hasAudio = ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
    
    if (hasCamera && hasAudio) {
        println("[PermissionHelper] Разрешения уже предоставлены")
        return true
    }
    
    println("[PermissionHelper] Разрешения не предоставлены. Нужно запросить через Activity.")
    return false
}


