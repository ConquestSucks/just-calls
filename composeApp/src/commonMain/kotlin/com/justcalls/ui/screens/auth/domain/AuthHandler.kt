package com.justcalls.ui.screens.auth.domain

import com.justcalls.data.models.requests.CompleteRequest
import com.justcalls.data.models.requests.SignInRequest
import com.justcalls.data.models.requests.SignUpRequest
import com.justcalls.data.network.AuthService
import com.justcalls.data.storage.RegistrationStorage
import com.justcalls.utils.NetworkErrorHandler.getErrorMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AuthHandler(
    private val state: AuthState,
    private val authService: AuthService,
    private val onAuthSuccess: () -> Unit,
    private val registrationStorage: RegistrationStorage = RegistrationStorage(),
    private val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.Main)
) {
    fun handleLogin() {
        state.loginEmailError = AuthValidation.validateEmail(state.loginEmail)
        state.loginPasswordError = AuthValidation.validatePassword(state.loginPassword)
        
        if (state.loginEmailError == null && state.loginPasswordError == null) {
            state.isLoading = true
            state.errorMessage = null
            
            coroutineScope.launch {
                val result = authService.signIn(
                    SignInRequest(
                        email = state.loginEmail,
                        password = state.loginPassword
                    )
                )
                
                state.isLoading = false
                
                result.fold(
                    onSuccess = { apiResult ->
                        if (apiResult.success && apiResult.data != null) {
            onAuthSuccess()
                        } else {
                            state.errorMessage = apiResult.error?.message ?: "Ошибка входа"
                        }
                    },
                    onFailure = { exception ->
                        state.errorMessage = getErrorMessage(exception)
                    }
                )
            }
        }
    }
    
    fun handleRegister() {
        when (state.registerStep) {
            RegisterStep.EMAIL -> {
                state.registerEmailError = AuthValidation.validateEmail(state.registerEmail)
                
                if (state.registerEmailError == null) {
                    // Проверяем, есть ли сохраненный GUID для этого email
                    // Восстанавливаем GUID из хранилища, если он не был установлен
                    if (state.registrationGuid == null) {
                        val savedGuid = registrationStorage.getRegistrationGuid(state.registerEmail)
                        if (savedGuid != null) {
                            state.registrationGuid = savedGuid
                        }
                    }
                    
                    state.isLoading = true
                    state.errorMessage = null
                    
                    coroutineScope.launch {
                        val result = authService.signUp(
                            SignUpRequest(
                                email = state.registerEmail,
                                guid = state.registrationGuid
                            )
                        )
                        
                        state.isLoading = false
                        
                        result.fold(
                            onSuccess = { apiResult ->
                                if (apiResult.success && apiResult.data != null) {
                                    val guid = apiResult.data.guid
                                    state.registrationGuid = guid
                                    // Сохраняем GUID в постоянное хранилище
                                    registrationStorage.saveRegistrationGuid(state.registerEmail, guid)
                                    state.registerStep = RegisterStep.VERIFICATION_CODE
                                } else {
                                    state.errorMessage = apiResult.error?.message ?: "Ошибка регистрации"
                                }
                            },
                            onFailure = { exception ->
                                state.errorMessage = getErrorMessage(exception)
                            }
                        )
                    }
                }
            }
            RegisterStep.VERIFICATION_CODE -> {
                state.verificationCodeError = AuthValidation.validateVerificationCode(state.verificationCode)
                
                if (state.verificationCodeError == null) {
                    state.registerStep = RegisterStep.PASSWORD
                }
            }
            RegisterStep.PASSWORD -> {
                state.registerPasswordError = AuthValidation.validatePassword(state.registerPassword)
                state.confirmPasswordError = AuthValidation.validateConfirmPassword(
                    state.registerPassword,
                    state.confirmPassword
                )
                
                if (state.registerPasswordError == null && state.confirmPasswordError == null) {
                    val guid = state.registrationGuid
                    if (guid == null) {
                        state.errorMessage = "Ошибка: не найден GUID регистрации"
                        return
                    }
                    
                    state.isLoading = true
                    state.errorMessage = null
                    
                    coroutineScope.launch {
                        val result = authService.complete(
                            CompleteRequest(
                                email = state.registerEmail,
                                guid = guid,
                                code = state.verificationCode,
                                password = state.registerPassword
                            )
                        )
                        
                        state.isLoading = false
                        
                        result.fold(
                            onSuccess = { apiResult ->
                                if (apiResult.success) {
                                    // Очищаем GUID после успешного завершения регистрации
                                    registrationStorage.clearRegistrationGuid(state.registerEmail)
                                    state.registrationGuid = null
                                    handleLogin()
                                } else {
                                    state.errorMessage = apiResult.error?.message ?: "Ошибка завершения регистрации"
                                }
                            },
                            onFailure = { exception ->
                                state.errorMessage = getErrorMessage(exception)
                            }
                        )
                    }
                }
            }
        }
    }
    
    fun handleSubmit() {
        state.errorMessage = null
        when (state.selectedTab) {
            AuthTab.LOGIN -> handleLogin()
            AuthTab.REGISTER -> handleRegister()
        }
    }
    
}


