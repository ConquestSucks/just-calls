package com.justcalls.ui.screens.room.components

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.justcalls.livekit.LiveKitManager
import livekit.org.webrtc.SurfaceViewRenderer
import livekit.org.webrtc.EglBase

@Composable
actual fun VideoSurfaceView(
    videoSurface: Any?,
    liveKitManager: LiveKitManager?,
    participantId: String,
    modifier: Modifier
) {
    val context = LocalContext.current
        val surfaceId = videoSurface as? String
        
        if (surfaceId != null && liveKitManager != null) {
            val isLocal = surfaceId.startsWith("local:")
            
            val eglBaseContext = remember {
            try {
                val getEglBaseContextMethod = liveKitManager.javaClass.getMethod("getEglBaseContext")
                getEglBaseContextMethod.invoke(liveKitManager) as? livekit.org.webrtc.EglBase.Context
            } catch (e: Exception) {
                println("[VideoSurfaceView] Не удалось получить EGL контекст из LiveKitManager: ${e.message}")
                null
            }
        }
        
        val eglBase = remember {
            if (eglBaseContext != null) {
                EglBase.create(eglBaseContext)
            } else {
                EglBase.create()
            }
        }
        
        AndroidView(
            factory = { ctx ->
                println("[VideoSurfaceView] Создание SurfaceViewRenderer для участника: $participantId, isLocal=$isLocal")
                
                val renderer = SurfaceViewRenderer(ctx)
                renderer.init(eglBase.eglBaseContext, null)
                renderer.setEnableHardwareScaler(true)
                renderer.setMirror(false)
                
                println("[VideoSurfaceView] Renderer инициализирован: eglBase=${eglBase.eglBaseContext != null}")
                
                fun addRendererToTrack() {
                    try {
                        val managerClass = liveKitManager.javaClass
                        if (isLocal) {
                            val getLocalTrackMethod = managerClass.getMethod("getLocalVideoTrack", String::class.java)
                            val track = getLocalTrackMethod.invoke(liveKitManager, participantId) as? io.livekit.android.room.track.LocalVideoTrack
                            if (track != null) {
                                println("[VideoSurfaceView] Локальный трек найден: $track")
                                track.addRenderer(renderer)
                                println("[VideoSurfaceView] Добавлен renderer к локальному треку")
                            } else {
                                println("[VideoSurfaceView] Локальный трек не найден для $participantId")
                            }
                        } else {
                            val getTrackMethod = managerClass.getMethod("getVideoTrack", String::class.java)
                            val track = getTrackMethod.invoke(liveKitManager, participantId) as? io.livekit.android.room.track.VideoTrack
                            if (track != null) {
                                println("[VideoSurfaceView] Удалённый трек найден: $track")
                                track.addRenderer(renderer)
                                println("[VideoSurfaceView] Добавлен renderer к удалённому треку")
                            } else {
                                println("[VideoSurfaceView] Удалённый трек не найден для $participantId")
                            }
                        }
                    } catch (e: Exception) {
                        println("[VideoSurfaceView] Ошибка при добавлении renderer к треку: ${e.message}")
                        e.printStackTrace()
                    }
                }
                
                addRendererToTrack()
                
                renderer.addOnAttachStateChangeListener(object : android.view.View.OnAttachStateChangeListener {
                    override fun onViewAttachedToWindow(v: android.view.View) {
                        println("[VideoSurfaceView] View attached to window, повторно добавляем renderer")
                        addRendererToTrack()
                    }
                    
                    override fun onViewDetachedFromWindow(v: android.view.View) {
                        println("[VideoSurfaceView] View detached from window")
                    }
                })
                
                renderer
            },
            update = { view ->
                println("[VideoSurfaceView] Обновление AndroidView")
            },
            modifier = modifier.fillMaxSize()
        )
        
        DisposableEffect(participantId) {
            onDispose {
                println("[VideoSurfaceView] Disposing для участника: $participantId")
            }
        }
    } else {
        println("[VideoSurfaceView] Неверный surfaceId: $surfaceId или liveKitManager is null")
    }
}

