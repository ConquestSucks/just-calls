package com.justcalls.ui.screens.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.justcalls.data.network.ApiClient
import com.justcalls.data.network.AuthService
import com.justcalls.data.storage.RegistrationStorage
import com.justcalls.data.storage.TokenStorage
import com.justcalls.ui.components.auth.AuthTabItem
import com.justcalls.ui.components.auth.StepIndicator
import com.justcalls.ui.components.common.ErrorMessage
import com.justcalls.ui.components.common.LoadingSpinner
import com.justcalls.ui.screens.auth.components.AuthHeader
import com.justcalls.ui.screens.auth.components.LoginForm
import com.justcalls.ui.screens.auth.components.RegisterForm
import com.justcalls.ui.screens.auth.components.SubmitButton
import com.justcalls.ui.screens.auth.domain.AuthHandler
import com.justcalls.ui.screens.auth.domain.AuthState
import com.justcalls.ui.screens.auth.domain.AuthTab
import com.justcalls.ui.screens.auth.domain.RegisterStep
import com.justcalls.ui.theme.AppColors
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

@Composable
fun AuthScreen(
    apiClient: ApiClient? = null,
    authService: AuthService? = null,
    onAuthSuccess: () -> Unit = {}
) {
    val tokenStorage = remember { TokenStorage() }
    val registrationStorage = remember { RegistrationStorage() }
    val finalApiClient = apiClient ?: remember { ApiClient(tokenStorage) }
    val finalAuthService = authService ?: remember { AuthService(finalApiClient, tokenStorage) }
    val state = remember { AuthState() }
    val coroutineScope = remember { CoroutineScope(Dispatchers.Main) }
    val handler = remember { AuthHandler(state, finalAuthService, onAuthSuccess, registrationStorage, coroutineScope) }
    
    // Восстанавливаем GUID при инициализации, если email уже введен
    LaunchedEffect(state.registerEmail) {
        if (state.registerEmail.isNotBlank() && state.registrationGuid == null && state.registerStep == RegisterStep.EMAIL) {
            val savedGuid = registrationStorage.getRegistrationGuid(state.registerEmail)
            if (savedGuid != null) {
                state.registrationGuid = savedGuid
            }
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
                        .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                    Spacer(modifier = Modifier.height(80.dp))
                
                AuthHeader()
                
                    Spacer(modifier = Modifier.height(48.dp))
                
                AuthTabs(
                    selectedTab = state.selectedTab,
                    onTabSelected = { state.selectedTab = it }
                )
                
                    Spacer(modifier = Modifier.height(36.dp))
                
                if (state.selectedTab == AuthTab.REGISTER) {
                    StepIndicator(
                        currentStep = when (state.registerStep) {
                            RegisterStep.EMAIL -> 1
                            RegisterStep.VERIFICATION_CODE -> 2
                            RegisterStep.PASSWORD -> 3
                        },
                        totalSteps = 3
                    )
                        Spacer(modifier = Modifier.height(24.dp))
                }
                
                when (state.selectedTab) {
                    AuthTab.LOGIN -> {
                        LoginForm(
                            email = state.loginEmail,
                            password = state.loginPassword,
                            emailError = state.loginEmailError,
                            passwordError = state.loginPasswordError,
                            onEmailChange = { 
                                state.loginEmail = it
                                state.loginEmailError = null
                            },
                            onPasswordChange = { 
                                state.loginPassword = it
                                state.loginPasswordError = null
                            },
                            enabled = !state.isLoading
                        )
                    }
                    AuthTab.REGISTER -> {
                        RegisterForm(
                            step = state.registerStep,
                            email = state.registerEmail,
                            verificationCode = state.verificationCode,
                            password = state.registerPassword,
                            confirmPassword = state.confirmPassword,
                            emailError = state.registerEmailError,
                            verificationCodeError = state.verificationCodeError,
                            passwordError = state.registerPasswordError,
                            confirmPasswordError = state.confirmPasswordError,
                            onEmailChange = { 
                                val oldEmail = state.registerEmail
                                state.registerEmail = it
                                state.registerEmailError = null
                                
                                // Если email изменился, очищаем старый GUID
                                if (oldEmail != it) {
                                    state.registrationGuid = null
                                    // Восстанавливаем GUID для нового email, если он был сохранен
                                    val savedGuid = registrationStorage.getRegistrationGuid(it)
                                    if (savedGuid != null) {
                                        state.registrationGuid = savedGuid
                                    }
                                }
                            },
                            onVerificationCodeChange = { 
                                state.verificationCode = it
                                state.verificationCodeError = null
                            },
                            onPasswordChange = { 
                                state.registerPassword = it
                                state.registerPasswordError = null
                            },
                            onConfirmPasswordChange = { 
                                state.confirmPassword = it
                                state.confirmPasswordError = null
                            },
                            enabled = !state.isLoading
                        )
                    }
                }
                
                Spacer(modifier = Modifier.weight(1f))
                    
                    ErrorMessage(message = state.errorMessage)
                
                SubmitButton(
                    tab = state.selectedTab,
                    registerStep = state.registerStep,
                    onClick = { handler.handleSubmit() },
                    enabled = !state.isLoading
                )
                
                    Spacer(modifier = Modifier.height(32.dp))
                }
                
                LoadingSpinner(
                    isLoading = state.isLoading,
                    spinnerColor = AppColors.Primary,
                    textColor = AppColors.Primary
                )
            }
        }
    }
}

@Composable
private fun AuthTabs(
    selectedTab: AuthTab,
    onTabSelected: (AuthTab) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start
    ) {
        AuthTabItem(
            text = "Вход",
            isSelected = selectedTab == AuthTab.LOGIN,
            onClick = { onTabSelected(AuthTab.LOGIN) }
        )
        Spacer(modifier = Modifier.width(32.dp))
        AuthTabItem(
            text = "Регистрация",
            isSelected = selectedTab == AuthTab.REGISTER,
            onClick = { onTabSelected(AuthTab.REGISTER) }
        )
    }
}

