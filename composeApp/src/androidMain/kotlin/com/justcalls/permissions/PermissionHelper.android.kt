package com.justcalls.permissions

import android.Manifest
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.justcalls.JustCallsApplication

actual suspend fun requestCameraAndMicrophonePermissions(): Boolean {
    val context = JustCallsApplication.instance
    
    val hasCamera = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
    val hasAudio = ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
    
    if (hasCamera && hasAudio) {
        return true
    }
    
    return false
}


