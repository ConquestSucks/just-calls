package com.justcalls

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.justcalls.data.network.RoomService
import com.justcalls.ui.screens.auth.AuthScreen
import com.justcalls.ui.screens.home.HomeScreen
import com.justcalls.ui.screens.profile.ProfileScreen
import com.justcalls.ui.screens.room.RoomScreen

@Composable
fun App() {
    val tokenStorage = remember { 
        com.justcalls.data.storage.TokenStorage() 
    }
    val apiClient = remember { 
        com.justcalls.data.network.ApiClient(tokenStorage) 
    }
    val authService = remember { 
        com.justcalls.data.network.AuthService(apiClient, tokenStorage) 
    }
    val roomService = remember { 
        RoomService(apiClient, authService) 
    }
    
    var isAuthenticated by remember {
        mutableStateOf(tokenStorage.getAccessToken() != null) 
    }
    var showProfile by remember { mutableStateOf(false) }
    var currentRoomName by remember { mutableStateOf<String?>(null) }
    
    fun checkAuthState() {
        val hasToken = tokenStorage.getAccessToken() != null
        if (!hasToken && isAuthenticated) {
            isAuthenticated = false
            showProfile = false
            currentRoomName = null
        } else if (hasToken && !isAuthenticated) {
            isAuthenticated = true
        }
    }
    
    LaunchedEffect(isAuthenticated) {
        checkAuthState()
    }
    
    LaunchedEffect(Unit) {
        checkAuthState()
    }
    
    if (isAuthenticated) {
        when {
            currentRoomName != null -> {
                RoomScreen(
                    roomName = currentRoomName!!,
                    onBackClick = { currentRoomName = null },
                    roomService = roomService
                )
            }
            showProfile -> {
                ProfileScreen(
                    apiClient = apiClient,
                    authService = authService,
                    tokenStorage = tokenStorage,
                    onBackClick = { 
                        showProfile = false
                    },
                    onLogout = {
                        tokenStorage.clearTokens()
                        isAuthenticated = false
                        showProfile = false
                        currentRoomName = null
                    }
                )
            }
            else -> {
                HomeScreen(
                    apiClient = apiClient,
                    authService = authService,
                    onSettingsClick = { 
                        showProfile = true
                    },
                    onCreateRoom = { roomName ->
                        currentRoomName = roomName
                    },
                    onUnauthorized = {
                        tokenStorage.clearTokens()
                        isAuthenticated = false
                        showProfile = false
                        currentRoomName = null
                    }
                )
            }
        }
    } else {
        AuthScreen(
            apiClient = apiClient,
            authService = authService,
            onAuthSuccess = {
                isAuthenticated = true
            }
        )
    }
}

