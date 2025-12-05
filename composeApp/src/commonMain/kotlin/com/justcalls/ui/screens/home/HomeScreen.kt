package com.justcalls.ui.screens.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.justcalls.data.models.responses.RoomDto
import com.justcalls.data.network.RoomService
import com.justcalls.ui.components.common.ErrorMessage
import com.justcalls.ui.components.common.LoadingSpinner
import com.justcalls.ui.screens.home.components.CreateRoomDialog
import com.justcalls.ui.screens.home.components.RoomListItem
import com.justcalls.ui.screens.home.components.buttonSection.ButtonSection
import com.justcalls.ui.screens.home.components.headerSection.HeaderSection
import com.justcalls.ui.screens.home.domain.RoomsHandler
import com.justcalls.ui.theme.AppColors

@Composable
fun HomeScreen(
    apiClient: com.justcalls.data.network.ApiClient? = null,
    authService: com.justcalls.data.network.AuthService? = null,
    onSettingsClick: () -> Unit = {},
    onCreateRoom: (String) -> Unit = {},
    onUnauthorized: () -> Unit = {}
) {
    var showCreateRoomDialog by remember { mutableStateOf(false) }
    var isCreatingRoom by remember { mutableStateOf(false) }
    var createRoomError by remember { mutableStateOf<String?>(null) }
    var rooms by remember { mutableStateOf<List<RoomDto>>(emptyList()) }
    var isRoomsLoading by remember { mutableStateOf(false) }
    var roomsError by remember { mutableStateOf<String?>(null) }

    val roomService = remember(apiClient, authService) {
        if (apiClient != null && authService != null) {
            RoomService(apiClient, authService)
        } else {
            null
        }
    }
    
    val roomsHandler = remember(roomService) {
        roomService?.let { RoomsHandler(it) }
    }

    LaunchedEffect(roomsHandler) {
        val handler = roomsHandler ?: return@LaunchedEffect
        isRoomsLoading = true
        roomsError = null
        
        handler.loadRooms(
            onSuccess = { loadedRooms ->
                isRoomsLoading = false
                rooms = loadedRooms
            },
            onError = { error ->
                isRoomsLoading = false
                roomsError = error
            },
            onTimeout = { timeoutError ->
                isRoomsLoading = false
                roomsError = timeoutError
            }
        )
    }
    
    MaterialTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = AppColors.Surface
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    HeaderSection(
                        apiClient = apiClient,
                        authService = authService,
                        onSettingsClick = onSettingsClick,
                        onUnauthorized = onUnauthorized
                    )
                    Column(Modifier.fillMaxSize()) {
                        ButtonSection(
                            onCreateRoomClick = { showCreateRoomDialog = true }
                        )
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 25.dp)
                        ) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Комнаты",
                                fontSize = 16.sp,
                                color = AppColors.TextHeader,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            ErrorMessage(message = roomsError)

                            when {
                                isRoomsLoading -> {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 32.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.spacedBy(12.dp)
                                        ) {
                                            CircularProgressIndicator(
                                                color = AppColors.GradientColors[0],
                                                strokeWidth = 3.dp,
                                                modifier = Modifier.size(32.dp)
                                            )
                                            Text(
                                                text = "Загрузка комнат...",
                                                color = AppColors.GradientColors[0],
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Medium
                                            )
                                        }
                                    }
                                }

                                rooms.isEmpty() && roomsError == null -> {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 32.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "Комнат пока нет",
                                            color = AppColors.TextSecondaryAccent,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Normal
                                        )
                                    }
                                }

                                !isRoomsLoading && rooms.isNotEmpty() -> {
                                    LazyColumn(
                                        modifier = Modifier.fillMaxSize(),
                                        contentPadding = PaddingValues(vertical = 8.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        items(rooms) { room ->
                                            RoomListItem(
                                                room = room,
                                                onClick = {
                                                    onCreateRoom(room.name)
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        ErrorMessage(
                            message = createRoomError,
                            modifier = Modifier.padding(horizontal = 25.dp, vertical = 8.dp)
                        )
                    }
                }
                
                LoadingSpinner(
                    isLoading = isCreatingRoom,
                    message = "Создание комнаты..."
                )
            }
        }
    }
    
    if (showCreateRoomDialog) {
        CreateRoomDialog(
            onDismiss = { showCreateRoomDialog = false },
            onCreateRoom = { roomTitle ->
                if (roomService == null) {
                    onCreateRoom(roomTitle)
                    showCreateRoomDialog = false
                    return@CreateRoomDialog
                }

                if (isCreatingRoom || roomsHandler == null) return@CreateRoomDialog

                isCreatingRoom = true
                createRoomError = null

                roomsHandler.createRoom(
                    roomTitle = roomTitle,
                    onSuccess = { roomKey ->
                        isCreatingRoom = false
                        onCreateRoom(roomKey)
                    },
                    onError = { error ->
                        isCreatingRoom = false
                        createRoomError = error
                    }
                )
            }
        )
    }
}