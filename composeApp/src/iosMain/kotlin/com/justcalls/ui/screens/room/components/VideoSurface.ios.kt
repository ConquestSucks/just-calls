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
                // Создаем VideoViewWrapper через Objective-C runtime
                val wrapperClass = objc_getClass("VideoViewWrapper")
                if (wrapperClass != null) {
                    val allocSelector = sel_registerName("alloc")
                    val initSelector = sel_registerName("initWithFrame:")
                    
                    @Suppress("CAST_NEVER_SUCCEEDS")
                    val allocFunc: (Any?, ObjCSelector) -> Any? = objc_msgSend as (Any?, ObjCSelector) -> Any?
                    val allocResult = allocFunc(wrapperClass, allocSelector)
                    
                    var result: UIView? = null
                    memScoped {
                        val frameVar = alloc<platform.CoreGraphics.CGRectVar>()
                        frameVar.point.x = 0.0
                        frameVar.point.y = 0.0
                        frameVar.size.width = 0.0
                        frameVar.size.height = 0.0
                        
                        @Suppress("CAST_NEVER_SUCCEEDS")
                        val initFunc: (Any?, ObjCSelector, platform.CoreGraphics.CGRectVar) -> Any? = 
                            objc_msgSend as (Any?, ObjCSelector, platform.CoreGraphics.CGRectVar) -> Any?
                        result = initFunc(allocResult, initSelector, frameVar) as? UIView
                    }
                    
                    val wrapper = result
                    if (wrapper != null && videoTrack != null) {
                        val setTrackSelector = sel_registerName("setTrack:")
                        @Suppress("CAST_NEVER_SUCCEEDS")
                        val setTrackFunc: (UIView, ObjCSelector, ObjCObject?) -> Unit = 
                            objc_msgSend as (UIView, ObjCSelector, ObjCObject?) -> Unit
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
                @Suppress("CAST_NEVER_SUCCEEDS")
                val setTrackFunc: (UIView, ObjCSelector, ObjCObject?) -> Unit = 
                    objc_msgSend as (UIView, ObjCSelector, ObjCObject?) -> Unit
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
