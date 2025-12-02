package com.justcalls.ui.screens.auth.domain

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

class AuthState {
    var selectedTab by mutableStateOf(AuthTab.LOGIN)
    var isLoading by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)
    
    var registrationGuid by mutableStateOf<String?>(null)
    
    var loginEmail by mutableStateOf("")
    var loginPassword by mutableStateOf("")
    var loginEmailError by mutableStateOf<String?>(null)
    var loginPasswordError by mutableStateOf<String?>(null)
    
    var registerStep by mutableStateOf(RegisterStep.EMAIL)
    var registerEmail by mutableStateOf("")
    var verificationCode by mutableStateOf("")
    var registerPassword by mutableStateOf("")
    var confirmPassword by mutableStateOf("")
    var registerEmailError by mutableStateOf<String?>(null)
    var verificationCodeError by mutableStateOf<String?>(null)
    var registerPasswordError by mutableStateOf<String?>(null)
    var confirmPasswordError by mutableStateOf<String?>(null)
}

enum class AuthTab {
    LOGIN,
    REGISTER
}

enum class RegisterStep {
    EMAIL,
    VERIFICATION_CODE,
    PASSWORD
}


