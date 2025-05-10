package com.aestroon.components.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp
import com.aestroon.components.theme.AppDimensions.normal

val Shapes = Shapes(
    small = RoundedCornerShape(4.dp),
    medium = RoundedCornerShape(4.dp),
    large = RoundedCornerShape(normal.dp)
)

val FullRoundedCornerShape = RoundedCornerShape(50)
val LightRoundedCornerShape = RoundedCornerShape(10.dp)
