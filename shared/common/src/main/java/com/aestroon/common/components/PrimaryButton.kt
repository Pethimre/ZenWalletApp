package com.aestroon.common.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.aestroon.common.theme.AppDimensions
import com.aestroon.common.theme.ComponentDimensions
import com.aestroon.common.theme.PrimaryColor

@Composable
fun PrimaryButton(
    onClick: () -> Unit,
    text: String,
    shape: RoundedCornerShape = RoundedCornerShape(AppDimensions.normal.dp),
    ){
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(ComponentDimensions.buttonHeight.dp),
        shape = shape,
        colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor)
    ) {
        Text(text, color = Color.White)
    }
}