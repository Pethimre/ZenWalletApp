package com.aestroon.common.domain

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.aestroon.common.utilities.TextFormatter
import com.aestroon.common.theme.AppBlack
import com.aestroon.common.theme.ZenWalletTheme

data class BankCard(
    val type: String,
    val accountDescription: String,
    val balance: Double,
    val currency: String,
    val icon: ImageVector,
    val iconBackgroundColor: Color,
)

@Composable
fun MinimalistBankCard(
    card: BankCard,
    modifier: Modifier = Modifier
) {
    val gradient = Brush.linearGradient(
        colors = listOf(
            card.iconBackgroundColor,
            card.iconBackgroundColor.copy(alpha = 0.5f)
        ),
        start = Offset.Zero,
        end = Offset.Infinite
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1.7f)
            .clip(RoundedCornerShape(24.dp))
            .background(brush = gradient)
            .padding(16.dp)
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val spacing = 40f
            for (x in 0..(size.width / spacing).toInt()) {
                for (y in 0..(size.height / spacing).toInt()) {
                    drawCircle(
                        color = Color.White.copy(alpha = 0.05f),
                        radius = 6f,
                        center = Offset(x * spacing, y * spacing)
                    )
                }
            }
        }

        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .zIndex(2f)
                        .background(AppBlack, shape = CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = card.icon,
                        contentDescription = null,
                        tint = Color.White,
                    )
                }

                Box(
                    modifier = Modifier
                        .offset(x = -(8).dp)
                        .background(
                            color = Color.Gray.copy(alpha = 0.35f),
                            shape = RoundedCornerShape(topEnd = 50.dp, bottomEnd = 50.dp)
                        )
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = card.type,
                        style = MaterialTheme.typography.labelLarge.copy(color = Color.White)
                    )
                }
            }

            Column {
                Text(
                    text = card.accountDescription,
                    maxLines = 4,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodyMedium.copy(color = Color.White.copy(alpha = 0.8f))
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${TextFormatter.toBasicFormat(card.balance)} ${card.currency}",
                    style = MaterialTheme.typography.headlineSmall.copy(color = Color.White)
                )
            }
        }
    }
}

@Preview
@Composable
fun MinimalistBankCardPreview(){
    ZenWalletTheme {
        MinimalistBankCard(
            card = BankCard(
                type = "Savings",
                accountDescription = "Main account",
                balance = 1542300.45,
                currency = "$",
                icon = Icons.Default.AccountBalance,
                iconBackgroundColor = Color(0xFF1976D2)
            ),
            modifier = Modifier.padding(16.dp)
        )
    }
}