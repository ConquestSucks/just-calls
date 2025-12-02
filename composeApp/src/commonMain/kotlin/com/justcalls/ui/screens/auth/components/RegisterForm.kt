package com.justcalls.ui.screens.auth.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.justcalls.ui.components.forms.ConfirmPasswordField
import com.justcalls.ui.components.forms.EmailField
import com.justcalls.ui.components.forms.PasswordField
import com.justcalls.ui.components.forms.VerificationCodeField
import com.justcalls.ui.screens.auth.domain.RegisterStep

@Composable
fun RegisterForm(
    step: RegisterStep,
    email: String,
    verificationCode: String,
    password: String,
    confirmPassword: String,
    emailError: String?,
    verificationCodeError: String?,
    passwordError: String?,
    confirmPasswordError: String?,
    onEmailChange: (String) -> Unit,
    onVerificationCodeChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onConfirmPasswordChange: (String) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        when (step) {
            RegisterStep.EMAIL -> {
                EmailField(
                    value = email,
                    onValueChange = onEmailChange,
                    errorMessage = emailError
                )
            }
            RegisterStep.VERIFICATION_CODE -> {
                VerificationCodeField(
                    value = verificationCode,
                    onValueChange = onVerificationCodeChange,
                    errorMessage = verificationCodeError
                )
            }
            RegisterStep.PASSWORD -> {
                PasswordField(
                    value = password,
                    onValueChange = onPasswordChange,
                    errorMessage = passwordError
                )
                ConfirmPasswordField(
                    value = confirmPassword,
                    onValueChange = onConfirmPasswordChange,
                    password = password,
                    errorMessage = confirmPasswordError
                )
            }
        }
    }
}

