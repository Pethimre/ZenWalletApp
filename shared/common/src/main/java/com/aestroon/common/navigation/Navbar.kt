package com.aestroon.common.navigation

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntOffset
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.StackedLineChart
import androidx.compose.material.icons.filled.Wallet
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.aestroon.common.components.BarShape
import com.aestroon.common.theme.AppWhite
import com.aestroon.common.theme.PrimaryColor
import com.aestroon.common.theme.ZenWalletTheme

data class ButtonData(val text: String, val icon: ImageVector)

@Composable
fun AnimatedNavigationBar(
    buttons: List<ButtonData>,
    barColor: Color,
    circleColor: Color,
    selectedColor: Color,
    unselectedColor: Color,
    onItemClick: (Int) -> Unit,
) {
    val circleRadius = 26.dp

    var selectedItem by rememberSaveable { mutableIntStateOf(2) }
    var barSize by remember { mutableStateOf(IntSize(0, 0)) }
    // first item's center offset for Arrangement.SpaceAround
    val offsetStep = remember(barSize) {
        barSize.width.toFloat() / (buttons.size * 2)
    }
    val offset = remember(selectedItem, offsetStep) {
        offsetStep + selectedItem * 2 * offsetStep
    }
    val circleRadiusPx = LocalDensity.current.run { circleRadius.toPx().toInt() }
    val offsetTransition = updateTransition(offset, "offset transition")
    val animation = spring<Float>(dampingRatio = 0.5f, stiffness = Spring.StiffnessVeryLow)
    val cutoutOffset by offsetTransition.animateFloat(
        transitionSpec = {
            if (this.initialState == 0f) {
                snap()
            } else {
                animation
            }
        },
        label = "cutout offset"
    ) { it }
    val circleOffset by offsetTransition.animateIntOffset(
        transitionSpec = {
            if (this.initialState == 0f) {
                snap()
            } else {
                spring(animation.dampingRatio, animation.stiffness)
            }
        },
        label = "circle offset"
    ) {
        IntOffset(it.toInt() - circleRadiusPx, -circleRadiusPx)
    }
    val barShape = remember(cutoutOffset) {
        BarShape(
            offset = cutoutOffset,
            circleRadius = circleRadius,
            cornerRadius = 25.dp,
        )
    }

    Box(Modifier.padding(WindowInsets.navigationBars.asPaddingValues())) {
        Circle(
            modifier = Modifier
                .offset { circleOffset }
                // the circle should be above the bar for accessibility reasons
                .zIndex(1f),
            color = circleColor,
            radius = circleRadius,
            button = buttons[selectedItem],
            iconColor = selectedColor,
        )
        Row(
            modifier = Modifier
                .onPlaced { barSize = it.size }
                .graphicsLayer {
                    shape = barShape
                    clip = true
                }
                .fillMaxWidth()
                .background(barColor),
            horizontalArrangement = Arrangement.SpaceAround,
        ) {
            buttons.forEachIndexed { index, button ->
                val isSelected = index == selectedItem
                NavigationBarItem(
                    selected = isSelected,
                    onClick = {
                        selectedItem = index
                        onItemClick(index)
                    },
                    icon = {
                        val iconAlpha by animateFloatAsState(
                            targetValue = if (isSelected) 0f else 1f,
                            label = "Navbar item icon"
                        )
                        Icon(
                            imageVector = button.icon,
                            contentDescription = button.text,
                            modifier = Modifier.alpha(iconAlpha)
                        )
                    },
                    label = { Text(button.text) },
                    colors = NavigationBarItemDefaults.colors().copy(
                        selectedIconColor = selectedColor,
                        selectedTextColor = selectedColor,
                        unselectedIconColor = unselectedColor,
                        unselectedTextColor = unselectedColor,
                        selectedIndicatorColor = Color.Transparent,
                    )
                )
            }
        }
    }
}


@Composable
private fun Circle(
    modifier: Modifier = Modifier,
    color: Color = Color.White,
    radius: Dp,
    button: ButtonData,
    iconColor: Color,
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(radius * 2)
            .border(1.dp, AppWhite, CircleShape)
            .clip(CircleShape)
            .background(color),
    ) {
        AnimatedContent(
            targetState = button.icon, label = "Bottom bar circle icon",
        ) { targetIcon ->
            Icon(targetIcon, button.text, tint = iconColor)
        }
    }
}

@Composable
@Preview
fun AnimatedNavigationBarPreview(){
    val buttons = listOf(
        ButtonData("Wallets", Icons.Default.Wallet),
        ButtonData("Portfolio", Icons.Default.StackedLineChart),
        ButtonData("Home", Icons.Default.Home),
        ButtonData("Calendar", Icons.Default.DateRange),
        ButtonData("Settings", Icons.Default.Settings),
    )

    ZenWalletTheme {
        AnimatedNavigationBar(
            buttons = buttons,
            barColor = MaterialTheme.colorScheme.onPrimary,
            circleColor = MaterialTheme.colorScheme.onPrimary,
            selectedColor = PrimaryColor,
            unselectedColor = Color.Gray,
            onItemClick = {}
        )
    }
}