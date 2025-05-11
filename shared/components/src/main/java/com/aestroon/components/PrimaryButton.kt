package com.aestroon.components

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
import com.aestroon.components.theme.AppDimensions.normal
import com.aestroon.components.theme.ComponentDimensions.buttonHeight
import com.aestroon.components.theme.PrimaryColor

@Composable
fun PrimaryButton(
    onClick: () -> Unit,
    text: String,
    shape: RoundedCornerShape = RoundedCornerShape(normal.dp),
    ){
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(buttonHeight.dp),
        shape = shape,
        colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor)
    ) {
        Text(text, color = Color.White)
    }
}