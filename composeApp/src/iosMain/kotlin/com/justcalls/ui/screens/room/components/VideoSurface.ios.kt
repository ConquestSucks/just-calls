package com.justcalls.ui.screens.room.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.interop.UIKitView
import com.justcalls.livekit.LiveKitManager
import platform.objc.*
import platform.Foundation.*
import platform.UIKit.UIView
import platform.CoreGraphics.*
import kotlinx.cinterop.*
import com.justcalls.livekit.wrappers.*

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun VideoSurfaceView(
    videoSurface: Any?,
    liveKitManager: LiveKitManager?,
    participantId: String,
    modifier: Modifier
) {
    val surfaceId = videoSurface as? String
    
    if (surfaceId != null && liveKitManager != null) {
        val isLocal = surfaceId.startsWith("local:")
        var videoTrack by remember { mutableStateOf<ObjCObject?>(null) }
        
        LaunchedEffect(participantId, liveKitManager, isLocal) {
            // Используем прямой вызов метода getVideoTrack из actual класса
            if (isLocal) {
                try {
                    @Suppress("UNCHECKED_CAST")
                    val manager = liveKitManager as? com.justcalls.livekit.LiveKitManager
                    val track = manager?.getVideoTrack(participantId)
                    videoTrack = track
                } catch (e: Exception) {
                    // Ignore
                }
            } else {
                var attempts = 0
                while (attempts < 20) {
                    try {
                        @Suppress("UNCHECKED_CAST")
                        val manager = liveKitManager as? com.justcalls.livekit.LiveKitManager
                        val track = manager?.getVideoTrack(participantId)
                        
                        if (track != null) {
                            videoTrack = track
                            break
                        } else {
                            attempts++
                            if (attempts < 20) {
                                kotlinx.coroutines.delay(300)
                            }
                        }
                    } catch (e: Exception) {
                        attempts++
                        if (attempts < 20) {
                            kotlinx.coroutines.delay(300)
                        }
                    }
                }
            }
        }
        
        // Отображаем видео через VideoViewWrapper
        UIKitView(
            factory = {
                // Используем обертки из cinterop
                val frame = platform.CoreGraphics.CGRectMake(0.0, 0.0, 0.0, 0.0)
                val wrapper = com.justcalls.livekit.wrappers.VideoViewWrapper(frame) as? UIView
                
                if (wrapper != null && videoTrack != null) {
                    // setTrack принимает VideoTrack?, но VideoTrack - это forward declaration
                    // Используем ObjCObject напрямую, так как VideoTrack не экспортирован в cinterop
                    val videoViewWrapper = wrapper as? com.justcalls.livekit.wrappers.VideoViewWrapper
                    // Приводим ObjCObject? к нужному типу через unsafe cast
                    @Suppress("UNCHECKED_CAST")
                    val track = videoTrack as? ObjCObject
                    @Suppress("UNCHECKED_CAST")
                    videoViewWrapper?.setTrack(track as? Any)
                }
                
                wrapper ?: UIView()
            },
            update = { view ->
                // Обновляем трек при изменении
                val videoViewWrapper = view as? com.justcalls.livekit.wrappers.VideoViewWrapper
                @Suppress("UNCHECKED_CAST")
                val track = videoTrack as? ObjCObject
                @Suppress("UNCHECKED_CAST")
                videoViewWrapper?.setTrack(track as? Any)
            },
            modifier = modifier
        )
        
        DisposableEffect(participantId) {
            onDispose {
                videoTrack = null
            }
        }
    }
}
