package com.justcalls.ui.screens.auth.domain

object AuthValidation {
    fun validateEmail(email: String): String? {
        return when {
            email.isBlank() -> "Email обязателен"
            !isValidEmail(email) -> "Неверный формат email"
            else -> null
        }
    }
    
    fun validatePassword(password: String): String? {
        return when {
            password.isBlank() -> "Пароль обязателен"
            password.length < 6 -> "Пароль должен содержать минимум 6 символов"
            else -> null
        }
    }
    
    fun validateVerificationCode(code: String): String? {
        return when {
            code.isBlank() -> "Код обязателен"
            !code.all { it.isUpperCase() && it.isLetter() && it in 'A'..'Z' || it.isDigit() } -> "Код должен содержать только заглавные латинские буквы и цифры"
            else -> null
        }
    }
    
    fun validateConfirmPassword(password: String, confirmPassword: String): String? {
        return when {
            confirmPassword.isBlank() -> "Подтвердите пароль"
            password != confirmPassword -> "Пароли не совпадают"
            else -> null
        }
    }
    
    private fun isValidEmail(email: String): Boolean {
        val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
        return emailRegex.toRegex().matches(email)
    }
}


