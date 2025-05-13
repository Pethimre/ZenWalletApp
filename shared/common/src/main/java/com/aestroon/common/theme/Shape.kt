package com.aestroon.common.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

val Shapes = Shapes(
    small = RoundedCornerShape(4.dp),
    medium = RoundedCornerShape(4.dp),
    large = RoundedCornerShape(AppDimensions.normal.dp)
)

val FullRoundedCornerShape = RoundedCornerShape(50)
val LightRoundedCornerShape = RoundedCornerShape(10.dp)
