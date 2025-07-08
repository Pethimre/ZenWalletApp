package com.aestroon.common.navigation

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.aestroon.common.theme.AppWhite
import com.aestroon.common.theme.BlackChipColor

data class ButtonData(val label: String, val icon: ImageVector)

@Composable
fun AnimatedNavigationBar(
    selectedIndex: Int,
    onSelectedIndexChange: (Int) -> Unit,
    onCircleClick: () -> Unit,
    buttons: List<ButtonData>,
    barColor: Color,
    circleColor: Color,
    selectedColor: Color,
    unselectedColor: Color,
) {
    val circleRadius = 28.dp
    var barSize by remember { mutableStateOf(IntSize.Zero) }

    val offsetStep = remember(barSize) {
        if (buttons.isNotEmpty()) barSize.width.toFloat() / (buttons.size * 2) else 0f
    }
    val offset = remember(selectedIndex, offsetStep) {
        offsetStep + selectedIndex * 2 * offsetStep
    }

    val circleRadiusPx = with(LocalDensity.current) { circleRadius.toPx() }

    val offsetTransition = updateTransition(offset, label = "offsetTransition")
    val animation = spring<Float>(dampingRatio = 0.5f, stiffness = Spring.StiffnessVeryLow)

    val cutoutOffset by offsetTransition.animateFloat(
        transitionSpec = { if (initialState == 0f) spring() else animation },
        label = "cutoutOffset"
    ) { it }

    val circleOffset by offsetTransition.animateFloat(
        transitionSpec = { if (initialState == 0f) spring() else animation },
        label = "circleOffset"
    ) { it }

    val barShape = remember(cutoutOffset) { BarShape(offset = cutoutOffset, circleRadius = circleRadiusPx) }

    Box(modifier = Modifier.padding(WindowInsets.navigationBars.asPaddingValues())) {
        Circle(
            modifier = Modifier
                .offset { IntOffset(x = circleOffset.toInt() - circleRadiusPx.toInt(), y = -circleRadiusPx.toInt() / 2) }
                .zIndex(1f),
            color = circleColor,
            radius = circleRadius,
            button = buttons[selectedIndex],
            iconColor = BlackChipColor,
            onClick = onCircleClick
        )
        Row(
            modifier = Modifier
                .onPlaced { barSize = it.size }
                .clip(barShape)
                .fillMaxWidth()
                .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                .background(barColor),
            horizontalArrangement = Arrangement.SpaceAround,
        ) {
            buttons.forEachIndexed { index, button ->
                val isSelected = index == selectedIndex
                NavigationBarItem(
                    selected = isSelected,
                    onClick = { onSelectedIndexChange(index) },
                    icon = {
                        val iconAlpha by animateFloatAsState(targetValue = if (isSelected) 0f else 1f, label = "iconAlpha")
                        Icon(
                            imageVector = button.icon,
                            contentDescription = button.label,
                            modifier = Modifier.alpha(iconAlpha)
                        )
                    },
                    label = { Text(button.label) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = selectedColor,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        unselectedIconColor = unselectedColor,
                        unselectedTextColor = unselectedColor,
                        indicatorColor = Color.Transparent,
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
    onClick: () -> Unit
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(radius * 2)
            .border(1.dp, AppWhite, CircleShape)
            .clip(CircleShape)
            .background(color)
            .clickable(onClick = onClick)
    ) {
        AnimatedContent(targetState = button.icon, label = "BottomBarCircleIcon") { targetIcon ->
            Icon(targetIcon, button.label, tint = iconColor)
        }
    }
}

class BarShape(private val offset: Float, private val circleRadius: Float) : Shape {
    override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline {
        val path = Path().apply {
            moveTo(0f, 0f)

            lineTo(offset - circleRadius * 1.2f, 0f)

            cubicTo(
                x1 = offset - circleRadius, y1 = 0f,
                x2 = offset - circleRadius, y2 = circleRadius,
                x3 = offset, y3 = circleRadius
            )
            cubicTo(
                x1 = offset + circleRadius, y1 = circleRadius,
                x2 = offset + circleRadius, y2 = 0f,
                x3 = offset + circleRadius * 1.2f, y3 = 0f
            )

            lineTo(size.width, 0f)
            lineTo(size.width, size.height)
            lineTo(0f, size.height)
            close()
        }
        return Outline.Generic(path)
    }
}
