package com.justcalls.ui.screens.room.components

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.justcalls.livekit.LiveKitManager
import io.livekit.android.room.track.VideoTrack
import io.livekit.android.util.flow
import io.livekit.android.renderer.TextureViewRenderer
import kotlinx.coroutines.delay

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
        var track by remember { mutableStateOf<VideoTrack?>(null) }
        
        val room = remember {
            try {
                val getRoomMethod = liveKitManager.javaClass.getMethod("getRoom")
                getRoomMethod.invoke(liveKitManager) as? io.livekit.android.room.Room
            } catch (e: Exception) {
                null
            }
        }
        
        LaunchedEffect(participantId, liveKitManager, isLocal, room) {
            if (isLocal && room != null) {
                try {
                    val localParticipant = room.localParticipant
                    localParticipant::videoTrackPublications.flow.collect { publications ->
                        val videoTrack = publications.firstOrNull()?.second as? VideoTrack
                        track = videoTrack
                    }
                } catch (e: Exception) {
                    // Ignore
                }
            } else if (!isLocal) {
                var attempts = 0
                while (attempts < 20) {
                    val foundTrack = try {
                        val managerClass = liveKitManager.javaClass
                        val getTrackMethod = managerClass.getMethod("getVideoTrack", String::class.java)
                        getTrackMethod.invoke(liveKitManager, participantId) as? VideoTrack
                    } catch (e: Exception) {
                        null
                    }
                    
                    if (foundTrack != null) {
                        track = foundTrack
                        break
                    } else {
                        attempts++
                        if (attempts < 20) {
                            delay(300)
                        }
                    }
                }
            }
        }
        
        var rendererInitialized by remember { mutableStateOf(false) }
        var currentTrack by remember { mutableStateOf<VideoTrack?>(null) }
        
        AndroidView(
            factory = { ctx ->
                val renderer = TextureViewRenderer(ctx)
                
                if (room != null) {
                    try {
                        room.initVideoRenderer(renderer)
                        rendererInitialized = true
                    } catch (e: Exception) {
                        // Ignore
                    }
                }
                
                renderer
            },
            update = { view ->
                val renderer = view
                if (!rendererInitialized && room != null) {
                    try {
                        room.initVideoRenderer(renderer)
                        rendererInitialized = true
                    } catch (e: Exception) {
                        // Ignore
                    }
                }

                if (track != null && rendererInitialized && currentTrack != track) {
                    currentTrack?.let { oldTrack ->
                        try {
                            oldTrack.removeRenderer(renderer)
                        } catch (e: Exception) {
                            // Ignore
                        }
                    }

                    try {
                        track!!.addRenderer(renderer)
                        currentTrack = track
                    } catch (e: Exception) {
                        // Ignore
                    }
                } else if (track == null && currentTrack != null) {
                    try {
                        currentTrack?.removeRenderer(renderer)
                        currentTrack = null
                    } catch (e: Exception) {
                        // Ignore
                    }
                }
            },
            modifier = modifier.fillMaxSize()
        )
        
        DisposableEffect(participantId) {
            onDispose {
                track = null
                currentTrack = null
            }
        }
    }
}

