package com.justcalls.livekit.internal

import livekit.org.webrtc.EglBase

internal class EglContextManager {
    private var eglBase: EglBase? = null
    
    fun initialize() {
        if (eglBase == null) {
            eglBase = EglBase.create()
            println("[EglContextManager] EGL контекст создан")
        }
    }
    
    fun getContext(): EglBase.Context? {
        return eglBase?.eglBaseContext
    }
    
    fun release() {
        eglBase?.release()
        eglBase = null
        println("[EglContextManager] EGL контекст освобождён")
    }
}

