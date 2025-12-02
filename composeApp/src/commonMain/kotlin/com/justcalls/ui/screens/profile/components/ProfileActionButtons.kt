package com.justcalls.ui.screens.profile.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.justcalls.ui.theme.AppColors
import com.justcalls.ui.theme.diagonalGradientBackground

@Composable
fun ProfileActionButtons(
    isEditMode: Boolean,
    onEditClick: () -> Unit,
    onSaveClick: () -> Unit,
    onCancelClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (isEditMode) {
        Row(
            modifier = modifier.fillMaxWidth(),
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
                        onClick = onCancelClick
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Отменить",
                    color = AppColors.TextHeader,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
            
            val saveInteractionSource = remember { MutableInteractionSource() }
            val isSavePressed by saveInteractionSource.collectIsPressedAsState()
            val saveScale by animateFloatAsState(
                targetValue = if (isSavePressed) 0.97f else 1f,
                animationSpec = tween(durationMillis = 100),
                label = "save_scale"
            )
            
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp)
                    .scale(saveScale)
                    .clip(RoundedCornerShape(16.dp))
                    .diagonalGradientBackground(AppColors.GradientColors)
                    .clickable(
                        interactionSource = saveInteractionSource,
                        indication = null,
                        onClick = onSaveClick
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Сохранить",
                    color = AppColors.Primary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    } else {
        val editInteractionSource = remember { MutableInteractionSource() }
        val isEditPressed by editInteractionSource.collectIsPressedAsState()
        val editScale by animateFloatAsState(
            targetValue = if (isEditPressed) 0.97f else 1f,
            animationSpec = tween(durationMillis = 100),
            label = "edit_scale"
        )
        
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(56.dp)
                .scale(editScale)
                .clip(RoundedCornerShape(16.dp))
                .diagonalGradientBackground(AppColors.GradientColors)
                .clickable(
                    interactionSource = editInteractionSource,
                    indication = null,
                    onClick = onEditClick
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Редактировать",
                color = AppColors.Primary,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

