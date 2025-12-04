package com.justcalls

import androidx.compose.ui.window.ComposeUIViewController

object MainViewControllerFactory {
    fun create() = ComposeUIViewController { App() }
}
