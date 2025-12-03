package com.justcalls.ui.screens.home.components.headerSection

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.justcalls.data.network.ApiClient
import com.justcalls.data.network.AuthService
import com.justcalls.ui.components.common.IconPressableButton
import com.justcalls.ui.theme.AppColors

@Composable
fun HeaderSection(
    apiClient: ApiClient? = null,
    authService: AuthService? = null,
    onSettingsClick: () -> Unit = {},
    onUnauthorized: () -> Unit = {}
) {
    var displayName by remember { mutableStateOf<String?>(null) }
    
    val finalApiClient = apiClient
    val finalAuthService = authService
    
    LaunchedEffect(finalApiClient, finalAuthService) {
        if (finalApiClient == null || finalAuthService == null) return@LaunchedEffect
        
        val result = finalAuthService.getUser()
        result.fold(
            onSuccess = { apiResult ->
                if (apiResult.success && apiResult.data != null) {
                    displayName = apiResult.data.displayName
                } else {
                    if (apiResult.error?.code == "UNAUTHORIZED") {
                        onUnauthorized()
                    }
                }
            },
            onFailure = { exception ->
                val message = exception.message ?: ""
                if (message.contains("401") || message.contains("Unauthorized")) {
                    onUnauthorized()
                }
            }
        )
    }
    
    val userName = displayName ?: "Пользователь"

    Row(
        modifier = Modifier.fillMaxWidth()
            .padding(top = 40.dp, start = 25.dp, end = 25.dp, bottom = 5.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            UserAvatar(userName.take(2).uppercase())
            Spacer(modifier = Modifier.width(18.dp))
            Column {
                Text(
                    text = userName,
                    color = AppColors.TextHeader,
                    fontSize = 21.sp,
                    fontWeight = FontWeight.Normal
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier.size(10.dp)
                            .clip(CircleShape)
                            .background(AppColors.ServerStatusActive)
                    ) {}
                    Spacer(modifier = Modifier.width(7.dp))
                    Text(
                        text = "Онлайн",
                        color = AppColors.TextSecondaryAccent,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Normal
                    )
                }
            }
        }
        IconPressableButton(
            imgPath = "drawable/ic_settings.svg",
            onClick = onSettingsClick,
            contentDescription = "Настройки"
        )
    }
}