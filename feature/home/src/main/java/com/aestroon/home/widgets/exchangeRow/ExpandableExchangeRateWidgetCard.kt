package com.aestroon.home.widgets.exchangeRow

import androidx.compose.animation.*
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.aestroon.home.mockProvider.CurrencyExchangeInfo
import com.aestroon.home.mockProvider.RateTrend

const val NUMBER_OF_CURRENCIES_ON_COMPACT = 3

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun ExpandableExchangeRateWidgetCard(
    modifier: Modifier = Modifier,
    allExchangeRates: List<CurrencyExchangeInfo>,
    ratesForCollapsedView: List<CurrencyExchangeInfo> = allExchangeRates.take(NUMBER_OF_CURRENCIES_ON_COMPACT),
    baseCurrencySymbol: String = "Ft",
    initiallyExpanded: Boolean = false,
    cardTitle: String = "Exchange Rates"
) {
    var isExpanded by remember { mutableStateOf(initiallyExpanded) }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.background
        ),
    ) {
        AnimatedContent(
            targetState = isExpanded,
            transitionSpec = {
                fadeIn(animationSpec = tween(300, easing = FastOutSlowInEasing)) +
                        expandVertically(
                            animationSpec = tween(300, easing = FastOutSlowInEasing),
                            expandFrom = Alignment.Top,
                        ) togetherWith
                        fadeOut(animationSpec = tween(300, easing = FastOutSlowInEasing)) +
                        shrinkVertically(
                            animationSpec = tween(300, easing = FastOutSlowInEasing),
                            shrinkTowards = Alignment.Top,
                        )
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
                    ){
                        Text(
                            text = cardTitle,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Icon(
                            imageVector = Icons.Filled.UnfoldLess,
                            contentDescription = "Collapse",
                            tint = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.size(28.dp),
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))

                    allExchangeRates.forEachIndexed { index, rateInfo ->
                        DetailedExchangeRateRow(
                            info = rateInfo,
                            baseCurrencySymbol = baseCurrencySymbol,
                        )
                        if (index < allExchangeRates.size - 1) {
                            HorizontalDivider(
                                modifier = Modifier.padding(vertical = 8.dp),
                                thickness = 0.5.dp,
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                            )
                        }
                    }
                }
            } else {
                Box(modifier = Modifier.clickable { isExpanded = true }) {
                    CollapsedRatesDisplay(
                        rates = ratesForCollapsedView
                    )
                }
            }
        }
    }
}

private val sampleExchangeRatesForWidget = listOf(
    CurrencyExchangeInfo("EUR", "Euro", Icons.Filled.EuroSymbol, 403.85, RateTrend.UP),
    CurrencyExchangeInfo("USD", "US Dollar", Icons.Filled.AttachMoney, 360.94, RateTrend.DOWN),
    CurrencyExchangeInfo("GBP", "British Pound", Icons.Filled.CurrencyPound, 480.39, RateTrend.STABLE),
    CurrencyExchangeInfo("CHF", "Swiss Franc", Icons.Filled.CurrencyFranc, 392.50, RateTrend.UP)
)
private val sampleCollapsedRates = sampleExchangeRatesForWidget.take(NUMBER_OF_CURRENCIES_ON_COMPACT)


@Preview(showBackground = true, name = "Collapsed Exchange Rate Widget")
@Composable
fun ExpandableExchangeRateWidgetCardCollapsedPreview() {
    MaterialTheme {
        ExpandableExchangeRateWidgetCard(
            allExchangeRates = sampleExchangeRatesForWidget,
            ratesForCollapsedView = sampleCollapsedRates,
            initiallyExpanded = false,
        )
    }
}

@Preview(showBackground = true, name = "Expanded Exchange Rate Widget")
@Composable
fun ExpandableExchangeRateWidgetCardExpandedPreview() {
    MaterialTheme {
        ExpandableExchangeRateWidgetCard(
            allExchangeRates = sampleExchangeRatesForWidget,
            ratesForCollapsedView = sampleCollapsedRates,
            initiallyExpanded = true,
        )
    }
}

@Preview(showBackground = true, name = "Collapsed Dark")
@Composable
fun ExpandableExchangeRateWidgetCardCollapsedDarkPreview() {
    MaterialTheme(colorScheme = darkColorScheme()) {
        ExpandableExchangeRateWidgetCard(
            allExchangeRates = sampleExchangeRatesForWidget,
            ratesForCollapsedView = sampleCollapsedRates,
            initiallyExpanded = false,
        )
    }
}

@Preview(showBackground = true, name = "Expanded Dark")
@Composable
fun ExpandableExchangeRateWidgetCardExpandedDarkPreview() {
    MaterialTheme(colorScheme = darkColorScheme()) {
        ExpandableExchangeRateWidgetCard(
            allExchangeRates = sampleExchangeRatesForWidget,
            ratesForCollapsedView = sampleCollapsedRates,
            initiallyExpanded = true,
        )
    }
}