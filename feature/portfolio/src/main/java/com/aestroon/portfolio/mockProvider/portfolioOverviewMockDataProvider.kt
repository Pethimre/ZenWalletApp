package com.aestroon.portfolio.mockProvider

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.CurrencyBitcoin
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.graphics.vector.ImageVector
import kotlin.random.Random

enum class PortfolioAssetType(val displayName: String) {
    ALL("All"),
    STOCKS("Stocks"),
    BONDS("Bonds"),
    CRYPTO("Crypto")
}

data class Instrument(
    val id: String,
    val name: String,
    val symbol: String,
    var currentPrice: Double,
    var priceChangePercentage24h: Double,
    val historicalData: List<Double> = List(30) { Random.nextDouble(50.0, 200.0) },
    val type: PortfolioAssetType,
    val icon: ImageVector? = null
)

data class HeldInstrument(
    val instrument: Instrument,
    var quantity: Double,
    val averageBuyPrice: Double
) {
    val currentValue: Double get() = quantity * instrument.currentPrice
    val costBasis: Double get() = quantity * averageBuyPrice
    val profitLoss: Double get() = currentValue - costBasis
    val profitLossPercentage: Double get() = if (costBasis == 0.0) 0.0 else (profitLoss / costBasis) * 100
}

data class PortfolioAccount(
    val id: String,
    var accountName: String,
    val accountType: PortfolioAssetType,
    val instruments: MutableList<HeldInstrument> = mutableStateListOf()
) {
    val totalValue: Double get() = instruments.sumOf { it.currentValue }
    val totalCostBasis: Double get() = instruments.sumOf { it.costBasis }
    val overallProfitLoss: Double get() = totalValue - totalCostBasis
    val overallProfitLossPercentage: Double get() = if (totalCostBasis == 0.0) 0.0 else (overallProfitLoss / totalCostBasis) * 100
}

data class PortfolioSummary(
    val totalPortfolioValue: Double,
    val totalProfitLoss: Double,
    val totalProfitLossPercentage: Double,
    val performanceByAssetType: Map<PortfolioAssetType, Double>,
    val historicalData: List<Double>
)

fun mockInstruments(type: PortfolioAssetType, count: Int): List<Instrument> {
    return List(count) { i ->
        val symbol = when (type) {
            PortfolioAssetType.STOCKS -> listOf("AAPL", "MSFT", "GOOGL", "AMZN", "TSLA")[i % 5]
            PortfolioAssetType.BONDS -> listOf("US10Y", "DE10Y", "GB10Y", "JGB10Y", "CORPB")[i % 5]
            PortfolioAssetType.CRYPTO -> listOf("BTC", "ETH", "ADA", "SOL", "DOT")[i % 5]
            else -> "UKNWN${i}"
        }
        val name = when (type) {
            PortfolioAssetType.STOCKS -> listOf(
                "Apple Inc.",
                "Microsoft Corp.",
                "Alphabet Inc.",
                "Amazon.com Inc.",
                "Tesla Inc."
            )[i % 5]

            PortfolioAssetType.BONDS -> listOf(
                "US Treasury 10Y",
                "German Bund 10Y",
                "UK Gilt 10Y",
                "Japan Gov. Bond 10Y",
                "Corp Bond AAA"
            )[i % 5]

            PortfolioAssetType.CRYPTO -> listOf(
                "Bitcoin",
                "Ethereum",
                "Cardano",
                "Solana",
                "Polkadot"
            )[i % 5]

            else -> "Unknown Asset ${i}"
        }
        val basePrice = when (type) {
            PortfolioAssetType.CRYPTO -> Random.nextDouble(500.0, 50000.0)
            PortfolioAssetType.STOCKS -> Random.nextDouble(50.0, 500.0)
            PortfolioAssetType.BONDS -> Random.nextDouble(90.0, 110.0)
            else -> Random.nextDouble(10.0, 100.0)
        }
        Instrument(
            id = "inst_${type.name}_${symbol}_$i",
            name = name,
            symbol = symbol,
            currentPrice = basePrice * Random.nextDouble(0.95, 1.05),
            priceChangePercentage24h = Random.nextDouble(-5.0, 5.0),
            historicalData = List(30) { basePrice * Random.nextDouble(0.85, 1.15) },
            type = type,
            icon = when (type) {
                PortfolioAssetType.STOCKS -> Icons.Filled.ShowChart; PortfolioAssetType.BONDS -> Icons.Filled.AccountBalance; PortfolioAssetType.CRYPTO -> Icons.Filled.CurrencyBitcoin; else -> Icons.Filled.HelpOutline
            }
        )
    }
}

fun mockPortfolioAccounts(): MutableList<PortfolioAccount> { /* ... same as before ... */
    val stockInstruments = mockInstruments(PortfolioAssetType.STOCKS, 5)
    val bondInstruments = mockInstruments(PortfolioAssetType.BONDS, 3)
    val cryptoInstruments = mockInstruments(PortfolioAssetType.CRYPTO, 4)
    return mutableListOf(
        PortfolioAccount(
            id = "acc_stocks_broker",
            accountName = "Brokerage Stocks",
            accountType = PortfolioAssetType.STOCKS,
            instruments = mutableStateListOf(
                HeldInstrument(
                    stockInstruments[0],
                    10.0,
                    stockInstruments[0].currentPrice * 0.95
                ), HeldInstrument(stockInstruments[1], 5.0, stockInstruments[1].currentPrice * 1.02)
            )
        ),
        PortfolioAccount(
            id = "acc_bonds_main",
            accountName = "Fixed Income Portfolio",
            accountType = PortfolioAssetType.BONDS,
            instruments = mutableStateListOf(
                HeldInstrument(
                    bondInstruments[0],
                    100.0,
                    bondInstruments[0].currentPrice * 0.99
                ), HeldInstrument(bondInstruments[1], 50.0, bondInstruments[1].currentPrice * 1.01)
            )
        ),
        PortfolioAccount(
            id = "acc_crypto_exchange",
            accountName = "Crypto Exchange Wallet",
            accountType = PortfolioAssetType.CRYPTO,
            instruments = mutableStateListOf(
                HeldInstrument(
                    cryptoInstruments[0],
                    0.5,
                    cryptoInstruments[0].currentPrice * 0.8
                ),
                HeldInstrument(cryptoInstruments[1], 10.0, cryptoInstruments[1].currentPrice * 1.1),
                HeldInstrument(cryptoInstruments[2], 100.0, cryptoInstruments[2].currentPrice * 0.9)
            )
        ),
        PortfolioAccount(
            id = "acc_stocks_retirement",
            accountName = "Retirement Fund (Stocks)",
            accountType = PortfolioAssetType.STOCKS,
            instruments = mutableStateListOf(
                HeldInstrument(
                    stockInstruments[2],
                    20.0,
                    stockInstruments[2].currentPrice * 0.90
                ),
                HeldInstrument(stockInstruments[3], 15.0, stockInstruments[3].currentPrice * 0.92)
            )
        )
    )
}

fun mockPortfolioSummary(accounts: List<PortfolioAccount>): PortfolioSummary { /* ... same as before ... */
    val totalValue = accounts.sumOf { it.totalValue }
    val totalCost = accounts.sumOf { it.totalCostBasis }
    val totalPnl = totalValue - totalCost
    val totalPnlPercent = if (totalCost == 0.0) 0.0 else (totalPnl / totalCost) * 100
    val valueByType = PortfolioAssetType.entries.associateWith { type ->
        if (type == PortfolioAssetType.ALL) 0.0 else accounts.filter { it.accountType == type }
            .sumOf { it.totalValue }
    }
    val combinedHistorical = List(30) { index ->
        val dailyTotalValue = accounts.sumOf { acc ->
            acc.instruments.sumOf { heldInst ->
                (heldInst.instrument.historicalData.getOrElse(index) { heldInst.instrument.currentPrice }) * heldInst.quantity
            }
        }; if (accounts.isNotEmpty()) dailyTotalValue else Random.nextDouble(50000.0, 100000.0)
    }.ifEmpty { List(30) { Random.nextDouble(50000.0, 100000.0) } }
    return PortfolioSummary(
        totalPortfolioValue = totalValue,
        totalProfitLoss = totalPnl,
        totalProfitLossPercentage = totalPnlPercent,
        performanceByAssetType = valueByType,
        historicalData = combinedHistorical
    )
}