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
import kotlinx.cinterop.*

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
        var videoTrack by remember { mutableStateOf<platform.objc.ObjCObject?>(null) }
        
        LaunchedEffect(participantId, liveKitManager, isLocal) {
            // В Kotlin/Native рефлексия работает по-другому
            // Используем прямой вызов метода, так как getVideoTrack является публичным методом в actual классе
            if (isLocal) {
                try {
                    // Получаем локальный видео трек через getVideoTrack
                    // В iOS версии getVideoTrack доступен напрямую
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
                        // Получаем удаленный видео трек через getVideoTrack
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
                // Создаем VideoViewWrapper через Objective-C runtime
                val wrapperClass = objc_getClass("VideoViewWrapper")
                if (wrapperClass != null) {
                    val allocSelector = sel_registerName("alloc")
                    val initSelector = sel_registerName("initWithFrame:")
                    val allocFunc = platform.objc.objc_msgSend as (Any?, platform.objc.ObjCSelector) -> Any?
                    val allocResult = allocFunc(wrapperClass, allocSelector)
                    val frame = platform.CoreGraphics.CGRectMake(0.0, 0.0, 0.0, 0.0)
                    val initFunc = platform.objc.objc_msgSend as (Any?, platform.objc.ObjCSelector, platform.CoreGraphics.CGRect) -> Any?
                    val wrapper = initFunc(allocResult, initSelector, frame) as? UIView
                    
                    // Устанавливаем трек если он есть
                    if (wrapper != null && videoTrack != null) {
                        val setTrackSelector = sel_registerName("setTrack:")
                        val setTrackFunc = platform.objc.objc_msgSend as (UIView, platform.objc.ObjCSelector, platform.objc.ObjCObject?) -> Unit
                        setTrackFunc(wrapper, setTrackSelector, videoTrack)
                    }
                    
                    wrapper ?: UIView()
                } else {
                    UIView()
                }
            },
            update = { view ->
                // Обновляем трек при изменении
                val setTrackSelector = sel_registerName("setTrack:")
                val setTrackFunc = platform.objc.objc_msgSend as (UIView, platform.objc.ObjCSelector, platform.objc.ObjCObject?) -> Unit
                setTrackFunc(view, setTrackSelector, videoTrack)
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
