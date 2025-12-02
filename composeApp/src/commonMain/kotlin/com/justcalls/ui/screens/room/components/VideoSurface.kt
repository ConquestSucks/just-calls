package com.justcalls.ui.screens.room.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.justcalls.livekit.LiveKitManager

@Composable
expect fun VideoSurfaceView(
    videoSurface: Any?,
    liveKitManager: LiveKitManager?,
    participantId: String,
    modifier: Modifier = Modifier
)

