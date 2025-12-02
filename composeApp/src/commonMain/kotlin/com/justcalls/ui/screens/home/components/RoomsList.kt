package com.justcalls.ui.screens.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.justcalls.data.models.responses.RoomDto
import com.justcalls.ui.theme.AppColors

@Composable
fun RoomListItem(
    room: RoomDto,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(
                color = AppColors.InputBackground,
                shape = RoundedCornerShape(14.dp)
            )
            .border(
                width = 1.dp,
                color = AppColors.InputBorder,
                shape = RoundedCornerShape(14.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 16.dp)
    ) {
        Text(
            text = room.title ?: room.name,
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
            color = AppColors.TextHeader
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Ключ: ${room.name}",
                fontSize = 12.sp,
                color = AppColors.TextSecondary,
                fontWeight = FontWeight.Normal
            )
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(AppColors.ServerStatusActive)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "${room.participants}/${room.maxParticipants}",
                    fontSize = 12.sp,
                    color = AppColors.GradientColors[0],
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}



