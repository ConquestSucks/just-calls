package com.justcalls.ui.components.forms

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.justcalls.ui.theme.AppColors
import justcalls.composeapp.generated.resources.Res

@Composable
fun ConfirmPasswordField(
    value: String,
    onValueChange: (String) -> Unit,
    password: String,
    errorMessage: String? = null,
    modifier: Modifier = Modifier
) {
    var passwordVisible by remember { mutableStateOf(false) }
    
    ColumnWithLabel(
        label = "Подтвердите пароль *",
        errorMessage = errorMessage,
        modifier = modifier
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
                    color = if (errorMessage != null) AppColors.Error else AppColors.InputBorder,
                    shape = RoundedCornerShape(12.dp)
                )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AsyncImage(
                    model = Res.getUri("drawable/ic_lock.svg"),
                    contentDescription = "Подтвердите пароль",
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 20.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    BasicTextField(
                        value = value,
                        onValueChange = onValueChange,
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
                            if (value.isEmpty()) {
                                Text(
                                    text = "Повторите пароль",
                                    color = AppColors.TextSecondaryAccent,
                                    fontSize = 14.sp,
                                    lineHeight = 18.sp
                                )
                            }
                            innerTextField()
                        },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        singleLine = true
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                val interactionSource = remember { MutableInteractionSource() }
                val isPressed by interactionSource.collectIsPressedAsState()
                val backgroundColor by animateColorAsState(
                    targetValue = if (isPressed) AppColors.InputBorder else Color.Transparent
                )
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(backgroundColor)
                        .clickable(
                            interactionSource = interactionSource,
                            indication = null
                        ) { passwordVisible = !passwordVisible },
                    contentAlignment = Alignment.Center
                ) {
                    AsyncImage(
                        model = Res.getUri(if (passwordVisible) "drawable/ic_eye.svg" else "drawable/ic_eye_slash.svg"),
                        contentDescription = if (passwordVisible) "Скрыть пароль" else "Показать пароль",
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}


