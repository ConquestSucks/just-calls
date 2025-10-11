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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.justcalls.ui.components.IconPressableButton
import com.justcalls.ui.theme.AppColors

@Composable
fun HeaderSection() {

    Row(
        modifier = Modifier.fillMaxWidth()
            .padding(top = 40.dp, start = 25.dp, end = 25.dp, bottom = 25.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically, // Добавляем центрирование по вертикали
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            UserAvatar("BA")
            Spacer(modifier = Modifier.width(18.dp))
            Column {
                Text(
                    text = "Baobab",
                    color = AppColors.TextHeader,
                    fontSize = 25.sp,
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

            onClick = {}, contentDescription = "Настройки"
        )
    }
}