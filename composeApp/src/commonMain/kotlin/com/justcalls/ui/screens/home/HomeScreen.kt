package com.justcalls.ui.screens.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.justcalls.ui.screens.home.components.buttonSection.ButtonSection
import com.justcalls.ui.screens.home.components.headerSection.HeaderSection
import com.justcalls.ui.screens.home.components.recentlyCallsSection.RecentlyCallsSection
import com.justcalls.ui.theme.AppColors

@Composable
fun MainScreen() {
    MaterialTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = AppColors.Surface
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                HeaderSection()
                Spacer(modifier = Modifier.width(35.dp))
                Column(Modifier.fillMaxSize()) {
                    ButtonSection()
                    RecentlyCallsSection()
                }
            }
        }
    }
}