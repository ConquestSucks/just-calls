package com.justcalls.ui.screens.home.components.buttonSection

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.justcalls.ui.theme.AppColors
import com.justcalls.ui.theme.diagonalGradientBackground
import justcalls.composeapp.generated.resources.Res

@Composable
fun ConnectRoomButton() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(
                width = 2.dp,
                color = AppColors.Circle,
                shape = RoundedCornerShape(16.dp)
            )
            .padding(22.dp, 24.dp, 0.dp, 32.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(AppColors.Circle)
                    .padding(10.dp)
            ) {
                AsyncImage(
                    model = Res.getUri("drawable/log-in.svg"),
                    contentDescription = "Создать",
                    modifier = Modifier.size(30.dp),
                )
            }
            Spacer(Modifier.height(24.dp))
            Text(
                text = "Войти",
                fontSize = 12.sp,
                color = AppColors.TextSecondary,
                modifier = Modifier.padding(start = 4.dp)
            )
            Spacer(Modifier.width(11.dp))
            Text(
                text = "По коду",
                fontSize = 11.sp,
                color = AppColors.TextSecondaryAccent,
                modifier = Modifier.padding(start = 4.dp)
            )
        }
    }
}