import androidx.compose.ui.graphics.vector.ImageVector
import java.util.Date

// Represents a single financial instrument (e.g., Apple stock)
data class Instrument(
    val id: String,
    val name: String,
    val symbol: String,
    val currentPrice: Double,
    val currency: String,
    val priceChangePercentage24h: Double = 0.0,
    val type: PortfolioAssetType,
    val icon: ImageVector?,
    val maturityDate: Date? = null,
    val couponRate: Double? = null
)

// Represents an instrument held by the user in a portfolio
data class HeldInstrument(
    val instrument: Instrument,
    val quantity: Double,
    val averageBuyPrice: Double
) {
    val currentValue: Double get() = instrument.currentPrice * quantity
    val totalCost: Double get() = averageBuyPrice * quantity
    val profitLoss: Double get() = currentValue - totalCost
    val profitLossPercentage: Double get() = if (totalCost == 0.0) 0.0 else (profitLoss / totalCost) * 100
}

// Represents a user's portfolio account (e.g., "Crypto Exchange Wallet")
data class PortfolioAccount(
    val id: String,
    val accountName: String,
    val accountType: PortfolioAssetType,
    val instruments: List<HeldInstrument>
) {
    val totalValue: Double get() = instruments.sumOf { it.currentValue }
    val totalCostBasis: Double get() = instruments.sumOf { it.totalCost }
    val overallProfitLoss: Double get() = totalValue - totalCostBasis
    val overallProfitLossPercentage: Double get() = if (totalCostBasis == 0.0) 0.0 else (overallProfitLoss / totalCostBasis) * 100
}

// Represents the summary of all portfolios combined
data class PortfolioSummary(
    val totalPortfolioValue: Double,
    val totalProfitLoss: Double,
    val totalProfitLossPercentage: Double,
    val performanceByAssetType: Map<PortfolioAssetType, Double>,
    val historicalData: List<Double>
)

enum class PortfolioAssetType(val displayName: String) {
    ALL("All"),
    STOCKS("Stocks"),
    BONDS("Bonds"),
    CRYPTO("Crypto")
}