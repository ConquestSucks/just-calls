package com.justcalls.ui.screens.home.components.recentlyCallsSection

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.justcalls.ui.theme.AppColors
import justcalls.composeapp.generated.resources.Res

@Composable
fun RecentlyCallsItem() {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(AppColors.Primary)
            .padding(15.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(AppColors.Circle)
                    .padding(10.dp)
            ) {
                AsyncImage(
                    model = Res.getUri("drawable/ic_video.svg"),
                    contentDescription = "Видео",
                    modifier = Modifier.size(30.dp),
                )
            }
        }
        Spacer(Modifier.width(16.dp))
        Column {
            Text(
                text = "Команда разработки",
                fontSize = 13.sp,
                color = AppColors.TextSecondary
            )
            Row {
                Text(
                    text = "Сегодня",
                    fontSize = 8.sp,
                    color = AppColors.TextSecondaryAccent
                )
                Spacer(Modifier.width(3.dp))
                Text(
                    text = "45 мин",
                    fontSize = 8.sp,
                    color = AppColors.TextSecondaryAccent
                )
            }
        }
    }
}