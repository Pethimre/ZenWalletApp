package com.aestroon.common.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.aestroon.common.theme.AppDimensions
import com.aestroon.common.theme.PrimaryColor

@Composable
fun LinkText(
    labelText: String,
    interactiveText: String,
    onClick: () -> Unit = {},
){
    Row {
        Text(labelText, color = MaterialTheme.colorScheme.onBackground)
        Spacer(modifier = Modifier.width(AppDimensions.tiny.dp))
        Text(
            text = interactiveText,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.clickable {
                onClick()
            }
        )
    }
}