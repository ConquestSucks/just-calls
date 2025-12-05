package com.justcalls.ui.screens.home.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.justcalls.ui.components.forms.ColumnWithLabel
import com.justcalls.ui.theme.AppColors
import com.justcalls.ui.theme.diagonalGradientBackground

@Composable
fun CreateRoomDialog(
    onDismiss: () -> Unit,
    onCreateRoom: (String) -> Unit
) {
    var roomName by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    
    Dialog(onDismissRequest = onDismiss) {
        MaterialTheme {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = AppColors.Surface,
                shape = RoundedCornerShape(24.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp)
                ) {
                    Text(
                        text = "Создать комнату 🚀",
                        color = AppColors.TextHeader,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        lineHeight = 32.sp
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "Введите название для новой комнаты",
                        color = AppColors.TextSecondary,
                        fontSize = 14.sp,
                        lineHeight = 20.sp
                    )
                    
                    Spacer(modifier = Modifier.height(32.dp))
                    
                    ColumnWithLabel(
                        label = "Название комнаты *",
                        errorMessage = error
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    color = AppColors.InputBackground,
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .border(
                                    width = 1.dp,
                                    color = if (error != null) AppColors.Error else AppColors.InputBorder,
                                    shape = RoundedCornerShape(12.dp)
                                )
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 48.dp)
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                BasicTextField(
                                    value = roomName,
                                    onValueChange = { 
                                        roomName = it
                                        error = null
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .heightIn(min = 20.dp),
                                    textStyle = TextStyle(
                                        color = AppColors.TextHeader,
                                        fontSize = 14.sp,
                                        lineHeight = 18.sp
                                    ),
                                    cursorBrush = SolidColor(AppColors.GradientColors[0]),
                                    decorationBox = { innerTextField ->
                                        if (roomName.isEmpty()) {
                                            Text(
                                                text = "Введите название",
                                                color = AppColors.TextSecondaryAccent,
                                                fontSize = 14.sp,
                                                lineHeight = 18.sp
                                            )
                                        }
                                        innerTextField()
                                    },
                                    singleLine = true
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        val cancelInteractionSource = remember { MutableInteractionSource() }
                        val isCancelPressed by cancelInteractionSource.collectIsPressedAsState()
                        val cancelScale by animateFloatAsState(
                            targetValue = if (isCancelPressed) 0.97f else 1f,
                            animationSpec = tween(durationMillis = 100),
                            label = "cancel_scale"
                        )
                        
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp)
                                .scale(cancelScale)
                                .clip(RoundedCornerShape(16.dp))
                                .background(AppColors.InputBackground)
                                .clickable(
                                    interactionSource = cancelInteractionSource,
                                    indication = null,
                                    onClick = onDismiss
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Отмена",
                                color = AppColors.TextHeader,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        
                        val createInteractionSource = remember { MutableInteractionSource() }
                        val isCreatePressed by createInteractionSource.collectIsPressedAsState()
                        val createScale by animateFloatAsState(
                            targetValue = if (isCreatePressed) 0.97f else 1f,
                            animationSpec = tween(durationMillis = 100),
                            label = "create_scale"
                        )
                        
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp)
                                .scale(createScale)
                                .clip(RoundedCornerShape(16.dp))
                                .diagonalGradientBackground(AppColors.GradientColors)
                                .clickable(
                                    interactionSource = createInteractionSource,
                                    indication = null,
                                    onClick = {
                                        if (roomName.isBlank()) {
                                            error = "Название комнаты обязательно"
                                        } else {
                                            onCreateRoom(roomName)
                                            onDismiss()
                                        }
                                    }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Создать",
                                color = AppColors.Primary,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }
    }
}

