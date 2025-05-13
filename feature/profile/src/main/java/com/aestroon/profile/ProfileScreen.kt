package com.aestroon.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.aestroon.common.theme.DarkGreyChipColor
import com.aestroon.common.theme.FaintBlueChipColor
import com.aestroon.common.theme.PrimaryColor

@Composable
fun ProfileScreen() {
    val username = "Mock User"
    val userId = "891748923cFR"
    val profileImageUrl =
        "https://preview.redd.it/transpennine-express-in-york-v0-ep5dvu9e9l0f1.jpeg?width=1080&crop=smart&auto=webp&s=6c2991b37171e39b3de6ece21fe7cf55d86528d0"

    val lightBackground = MaterialTheme.colorScheme.background
    val headerBackground = PrimaryColor

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(lightBackground)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .background(
                        color = headerBackground,
                        shape = RoundedCornerShape(bottomStart = 50.dp, bottomEnd = 50.dp)
                    )
            ) {
                Text(
                    text = "Profile",
                    style = MaterialTheme.typography.titleLarge.copy(color = Color.White),
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 24.dp)
                )

                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .align(Alignment.Center)
                        .offset(y = 16.dp)
                        .clip(CircleShape)
                        .border(4.dp, Color.White, CircleShape)
                        .background(DarkGreyChipColor),
                    contentAlignment = Alignment.Center
                ) {
                    AsyncImage(
                        model = profileImageUrl,
                        contentDescription = "Profile Picture",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = username,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                ),
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            Text(
                text = "ID: $userId",
                style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onBackground),
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            Spacer(modifier = Modifier.height(24.dp))

            ProfileOption(icon = Icons.Default.Edit, title = "Edit Profile")
            ProfileOption(icon = Icons.Default.Security, title = "Security")
            ProfileOption(icon = Icons.Default.Settings, title = "Setting")
            ProfileOption(icon = Icons.Default.Help, title = "Help")
            ProfileOption(icon = Icons.Default.Logout, title = "Logout")
        }
    }
}

@Composable
fun ProfileOption(icon: ImageVector, title: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp, vertical = 12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(
                    color = FaintBlueChipColor.copy(alpha = .1f),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color(0xFF007BFF)
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onBackground
            )
        )
    }
}
