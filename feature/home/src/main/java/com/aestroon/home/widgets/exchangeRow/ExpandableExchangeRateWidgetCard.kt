package com.aestroon.home.widgets.exchangeRow

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.UnfoldLess
import androidx.compose.material.icons.filled.UnfoldMore
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.aestroon.common.data.model.CurrencyExchangeInfo

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun ExpandableExchangeRateWidgetCard(
    modifier: Modifier = Modifier,
    allExchangeRates: List<CurrencyExchangeInfo>,
    ratesForCollapsedView: List<CurrencyExchangeInfo>,
    baseCurrencySymbol: String,
    initiallyExpanded: Boolean = false,
    cardTitle: String = "Exchange Rates"
) {
    var isExpanded by remember { mutableStateOf(initiallyExpanded) }

    if (allExchangeRates.isEmpty()) return

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background),
    ) {
        AnimatedContent(
            targetState = isExpanded,
            transitionSpec = {
                fadeIn(animationSpec = tween(300, easing = FastOutSlowInEasing)) +
                        expandVertically(animationSpec = tween(300, easing = FastOutSlowInEasing), expandFrom = Alignment.Top) togetherWith
                        fadeOut(animationSpec = tween(300, easing = FastOutSlowInEasing)) +
                        shrinkVertically(animationSpec = tween(300, easing = FastOutSlowInEasing), shrinkTowards = Alignment.Top)
            },
            label = "expandableExchangeRateContent"
        ) { targetExpanded ->
            if (targetExpanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { isExpanded = false }
                        .padding(vertical = 12.dp, horizontal = 16.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(text = cardTitle, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        Icon(imageVector = Icons.Filled.UnfoldLess, contentDescription = "Collapse", tint = MaterialTheme.colorScheme.onBackground, modifier = Modifier.size(28.dp))
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    allExchangeRates.forEachIndexed { index, rateInfo ->
                        DetailedExchangeRateRow(info = rateInfo, baseCurrencySymbol = baseCurrencySymbol)
                        if (index < allExchangeRates.size - 1) {
                            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                        }
                    }
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth().clickable { isExpanded = true },
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                ) {
                    CollapsedRatesDisplay(rates = ratesForCollapsedView)
                }
            }
        }
    }
}