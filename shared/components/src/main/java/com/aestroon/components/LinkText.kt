package com.aestroon.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.aestroon.components.theme.AppDimensions.tiny
import com.aestroon.components.theme.PrimaryColor

@Composable
fun LinkText(
    labelText: String,
    interactiveText: String,
    onClick: () -> Unit = {},
){
    Row {
        Text(labelText)
        Spacer(modifier = Modifier.width(tiny.dp))
        Text(
            text = interactiveText,
            color = PrimaryColor,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.clickable {
                onClick()
            }
        )
    }
}