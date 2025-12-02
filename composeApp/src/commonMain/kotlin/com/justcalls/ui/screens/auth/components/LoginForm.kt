package com.justcalls.ui.screens.auth.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.justcalls.ui.components.forms.EmailField
import com.justcalls.ui.components.forms.PasswordField

@Composable
fun LoginForm(
    email: String,
    password: String,
    emailError: String?,
    passwordError: String?,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        EmailField(
            value = email,
            onValueChange = onEmailChange,
            errorMessage = emailError
        )
        PasswordField(
            value = password,
            onValueChange = onPasswordChange,
            errorMessage = passwordError
        )
    }
}

