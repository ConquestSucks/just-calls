package com.justcalls.ui.screens.profile

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.justcalls.data.network.ApiClient
import com.justcalls.data.network.AuthService
import com.justcalls.data.storage.TokenStorage
import com.justcalls.ui.components.common.ErrorMessage
import com.justcalls.ui.components.common.LoadingSpinner
import com.justcalls.ui.screens.profile.components.EditableNameField
import com.justcalls.ui.screens.profile.components.ProfileActionButtons
import com.justcalls.ui.screens.profile.components.ProfileHeader
import com.justcalls.ui.screens.profile.components.ProfileInfoItem
import com.justcalls.ui.screens.profile.domain.ProfileHandler
import com.justcalls.ui.theme.AppColors
import com.justcalls.ui.utils.BackHandler
import com.justcalls.utils.DateFormatter
import kotlinx.coroutines.launch

@Composable
fun ProfileScreen(
    apiClient: ApiClient? = null,
    authService: AuthService? = null,
    onBackClick: () -> Unit = {},
    onLogout: () -> Unit = {}
) {
    val tokenStorage = remember { TokenStorage() }
    val finalApiClient = apiClient ?: remember { ApiClient(tokenStorage) }
    val finalAuthService = authService ?: remember { AuthService(finalApiClient, tokenStorage) }
    val coroutineScope = rememberCoroutineScope()
    val profileHandler = remember { ProfileHandler(finalAuthService, tokenStorage, onLogout, coroutineScope) }
    
    var isEditMode by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var isSaving by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var profileLoadError by remember { mutableStateOf<String?>(null) }
    
    var userId by remember { mutableStateOf<String?>(null) }
    var email by remember { mutableStateOf<String?>(null) }
    var displayName by remember { mutableStateOf("") }
    var originalDisplayName by remember { mutableStateOf("") }
    var createdAt by remember { mutableStateOf<String?>(null) }
    
    LaunchedEffect(Unit) {
        isLoading = true
        errorMessage = null
        profileLoadError = null
        
        profileHandler.loadProfile(
            onSuccess = { profileData ->
                isLoading = false
                userId = profileData.userId
                email = profileData.email
                displayName = profileData.displayName
                originalDisplayName = profileData.displayName
                createdAt = DateFormatter.formatDate(profileData.createdAt)
                profileLoadError = null
            },
            onError = { error ->
                isLoading = false
                errorMessage = error
            },
            onTimeout = { timeoutError ->
                isLoading = false
                profileLoadError = timeoutError
            }
        )
    }
    
    fun handleSave() {
        isSaving = true
        errorMessage = null
        
        profileHandler.saveProfile(
            displayName = displayName,
            onSuccess = {
                isSaving = false
                originalDisplayName = displayName
                isEditMode = false
                errorMessage = null
            },
            onError = { error ->
                isSaving = false
                errorMessage = error
            }
        )
    }
    
    fun handleCancel() {
        displayName = originalDisplayName
        isEditMode = false
        errorMessage = null
    }
    
    BackHandler(enabled = true) {
        if (isEditMode) {
            handleCancel()
        } else {
            onBackClick()
        }
    }
    
    MaterialTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = AppColors.Surface
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 25.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState())
                    ) {
                        Spacer(modifier = Modifier.height(60.dp))
                        
                        ProfileHeader(
                            username = originalDisplayName.ifEmpty { "Пользователь" },
                            accountCreated = createdAt ?: "",
                            isEditMode = isEditMode,
                            onBackClick = onBackClick,
                            onAvatarClick = {
                            },
                            onLogoutClick = {
                                coroutineScope.launch {
                                    try {
                                        finalAuthService.logout()
                                    } catch (_: Exception) {
                                    }
                                    onLogout()
                                }
                            }
                        )
                        
                        Spacer(modifier = Modifier.height(32.dp))
                        
                        ErrorMessage(message = errorMessage)
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        userId?.let {
                            ProfileInfoItem(
                                iconPath = "drawable/ic_user_id.svg",
                                label = "ID пользователя",
                                value = it
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                        }
                        
                        email?.let {
                            ProfileInfoItem(
                                iconPath = "drawable/ic_email.svg",
                                label = "Email",
                                value = it
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                        }
                        
                        if (isEditMode) {
                            EditableNameField(
                                value = displayName,
                                onValueChange = { displayName = it }
                            )
                        } else {
                            ProfileInfoItem(
                                iconPath = "drawable/ic_user.svg",
                                label = "Отображаемое имя",
                                value = displayName.ifEmpty { "Не указано" }
                            )
                        }
                        
                        if (profileLoadError != null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            ErrorMessage(message = profileLoadError)
                        }
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        createdAt?.let {
                            ProfileInfoItem(
                                iconPath = "drawable/ic_calendar.svg",
                                label = "Дата регистрации",
                                value = it
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(32.dp))
                    }
                    
                    ProfileActionButtons(
                        isEditMode = isEditMode,
                        onEditClick = {
                            isEditMode = true
                            originalDisplayName = displayName
                            errorMessage = null
                        },
                        onSaveClick = { handleSave() },
                        onCancelClick = { handleCancel() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 40.dp)
                    )
                }
                
                LoadingSpinner(
                    isLoading = isLoading,
                    message = "Загрузка профиля...",
                    spinnerColor = AppColors.Primary,
                    textColor = AppColors.Primary
                )
                
                LoadingSpinner(
                    isLoading = isSaving,
                    message = "Сохранение...",
                    spinnerColor = AppColors.Primary,
                    textColor = AppColors.Primary
                )
            }
        }
    }
}


