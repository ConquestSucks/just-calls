package com.justcalls.ui.screens.home.components.buttonSection

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ButtonSection(
    onCreateRoomClick: () -> Unit = {}
) {
    Column(modifier = Modifier.padding(25.dp)) {
        CreateRoomButton(onClick = onCreateRoomClick)
        Spacer(modifier = Modifier.height(14.dp))
       ConnectRoomButton()
    }
}