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
            if (isLocal) {
                try {
                    // Получаем локальный видео трек через getVideoTrack (аналогично Android)
                    val managerClass = liveKitManager::class
                    val getVideoTrackMethod = managerClass.members.find { it.name == "getVideoTrack" }
                    val track = getVideoTrackMethod?.call(liveKitManager, participantId) as? ObjCObject
                    videoTrack = track
                } catch (e: Exception) {
                    // Ignore
                }
            } else {
                var attempts = 0
                while (attempts < 20) {
                    try {
                        // Получаем удаленный видео трек через getVideoTrack (аналогично Android)
                        val managerClass = liveKitManager::class
                        val getVideoTrackMethod = managerClass.members.find { it.name == "getVideoTrack" }
                        val track = getVideoTrackMethod?.call(liveKitManager, participantId) as? ObjCObject
                        
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
                // Создаем VideoViewWrapper через Objective-C runtime
                val wrapperClass = objc_getClass("VideoViewWrapper")
                if (wrapperClass != null) {
                    val allocSelector = sel_registerName("alloc")
                    val initSelector = sel_registerName("initWithFrame:")
                    val allocResult = objc_msgSend(wrapperClass, allocSelector)
                    val frame = platform.CoreGraphics.CGRectMake(0.0, 0.0, 0.0, 0.0)
                    val wrapper = objc_msgSend(allocResult, initSelector, frame) as? UIView
                    
                    // Устанавливаем трек если он есть
                    if (wrapper != null && videoTrack != null) {
                        val setTrackSelector = sel_registerName("setTrack:")
                        objc_msgSend(wrapper, setTrackSelector, videoTrack)
                    }
                    
                    wrapper ?: UIView()
                } else {
                    UIView()
                }
            },
            update = { view ->
                // Обновляем трек при изменении
                if (videoTrack != null) {
                    val setTrackSelector = sel_registerName("setTrack:")
                    objc_msgSend(view, setTrackSelector, videoTrack)
                } else {
                    val setTrackSelector = sel_registerName("setTrack:")
                    objc_msgSend(view, setTrackSelector, null)
                }
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
