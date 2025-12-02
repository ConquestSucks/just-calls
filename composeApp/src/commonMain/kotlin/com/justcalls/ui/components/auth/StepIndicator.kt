package com.justcalls.ui.components.auth

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.justcalls.ui.theme.AppColors
import com.justcalls.ui.theme.diagonalGradientBackground

@Composable
fun StepIndicator(
    currentStep: Int,
    totalSteps: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(totalSteps) { step ->
            val stepNumber = step + 1
            val isCompleted = stepNumber < currentStep
            val isCurrent = stepNumber == currentStep
            
            Box(
                modifier = Modifier
                    .size(if (isCurrent) 40.dp else 32.dp)
                    .clip(CircleShape)
                    .background(
                        color = when {
                            isCompleted || isCurrent -> Color.Transparent
                            else -> AppColors.InputBorder
                        }
                    )
                    .then(
                        if (isCompleted || isCurrent) {
                            Modifier.diagonalGradientBackground(AppColors.GradientColors)
                        } else {
                            Modifier
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (isCompleted) "✓" else stepNumber.toString(),
                    color = if (isCompleted || isCurrent) AppColors.Primary else AppColors.TextSecondary,
                    fontSize = if (isCurrent) 16.sp else 14.sp,
                    fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal
                )
            }
            
            if (step < totalSteps - 1) {
                val progress by animateFloatAsState(
                    targetValue = if (stepNumber < currentStep) 1f else 0f,
                    animationSpec = tween(durationMillis = 300),
                    label = "step_progress"
                )
                
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(2.dp)
                        .clip(RoundedCornerShape(1.dp))
                        .background(AppColors.InputBorder)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(progress)
                            .height(2.dp)
                            .diagonalGradientBackground(AppColors.GradientColors)
                    )
                }
            }
        }
    }
}


