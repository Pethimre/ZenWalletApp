package com.aestroon.common.presentation.screen.components

import HeldInstrument
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.aestroon.common.utilities.TextFormatter.formatPercentage

@Composable
fun AssetPieChart(
    instruments: List<HeldInstrument>,
    modifier: Modifier = Modifier
) {
    val totalValue = instruments.sumOf { it.currentValue }
    if (totalValue == 0.0) return

    val colors = remember {
        listOf(
            Color(0xFF6200EE), Color(0xFF03DAC5), Color(0xFFBB86FC),
            Color(0xFF3700B3), Color(0xFF018786), Color(0xFFCF6679),
            Color(0xFFB00020), Color(0xFFFF0266)
        )
    }

    Row(
        modifier = modifier.fillMaxWidth().height(150.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Canvas(modifier = Modifier.size(150.dp)) {
            var startAngle = 0f
            instruments.forEachIndexed { index, instrument ->
                val sweepAngle = (instrument.currentValue / totalValue * 360).toFloat()
                drawArc(
                    color = colors[index % colors.size],
                    startAngle = startAngle,
                    sweepAngle = sweepAngle,
                    useCenter = false,
                    style = Stroke(width = 35f)
                )
                startAngle += sweepAngle
            }
        }
        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            instruments.forEachIndexed { index, instrument ->
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 4.dp)) {
                    Box(modifier = Modifier.size(12.dp).background(colors[index % colors.size], CircleShape))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${instrument.instrument.name} (${formatPercentage(instrument.currentValue / totalValue * 100)})",
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}
