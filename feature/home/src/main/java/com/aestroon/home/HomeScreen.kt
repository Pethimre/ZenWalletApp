import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aestroon.common.TextFormatter
import com.aestroon.components.expandableListSection
import com.aestroon.components.theme.AppDimensions.normal
import com.aestroon.components.theme.AppDimensions.small
import com.aestroon.components.theme.PrimaryFontColor
import com.aestroon.components.theme.SecondaryFontColor
import com.aestroon.home.MinimalistBankCard
import com.aestroon.home.mockProvider.BANK_CARDS
import com.aestroon.home.mockProvider.BankCard
import com.aestroon.home.mockProvider.MOCK_TRANSACTIONS
import com.aestroon.home.mockProvider.Transaction

@Composable
fun HomeScreen() {
    val bankCards = remember { BANK_CARDS }
    val allTransactions = remember { MOCK_TRANSACTIONS }
    val pagerState = rememberPagerState(initialPage = 0, pageCount = { bankCards.size })
    val selectedCardIndex = pagerState.currentPage

    val incomingTransactions = allTransactions[selectedCardIndex].filter { it.amount > 0 }
    val outgoingTransactions = allTransactions[selectedCardIndex].filter { it.amount < 0 }

    val totalBalance = bankCards.sumOf { it.balance }
    val baseCurrency = "EUR"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(normal.dp)
    ) {
        Spacer(modifier = Modifier.height(normal.dp))

        Text("Current Balance", color = SecondaryFontColor, fontWeight = FontWeight.SemiBold)
        Text(
            text = "${TextFormatter.toBasicFormat(totalBalance)} $baseCurrency",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = PrimaryFontColor,
        )

        Spacer(modifier = Modifier.height(normal.dp))

        CardCarousel(pagerState, bankCards)

        Spacer(modifier = Modifier.height(normal.dp))

        TransactionFlow(incomingTransactions, outgoingTransactions)
    }
}

@Composable
fun CardCarousel(pagerState: PagerState, bankCards: List<BankCard>) {
    HorizontalPager(state = pagerState) { page ->
        MinimalistBankCard(bankCards[page])
    }
}

@Composable
fun TransactionFlow(
    incomingTransactions: List<Transaction>,
    outgoingTransactions: List<Transaction>
) {
    var showAllIncoming by remember { mutableStateOf(false) }
    var showAllOutgoing by remember { mutableStateOf(false) }

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        expandableListSection(
            sectionId = "incoming_transactions",
            title = "Incoming Transactions",
            allItems = incomingTransactions,
            isExpanded = showAllIncoming,
            onToggleExpand = { showAllIncoming = !showAllIncoming },
            itemKeyProvider = { transaction -> transaction.id },
            itemContent = { transaction ->
                TransactionItem(transaction = transaction)
            },
            titleContent = {
                TransactionHeader(
                    sumOfTransactions = incomingTransactions.sumOf { it.amount },
                    currency = incomingTransactions.first().currency,
                    headerText = "Incoming Transactions",
                )
            },
        )

        item(key = "spacer_between_transactions") {
            Spacer(modifier = Modifier.height(16.dp))
        }

        expandableListSection(
            sectionId = "outgoing_transactions",
            title = "Outgoing Transactions",
            allItems = outgoingTransactions,
            isExpanded = showAllOutgoing,
            onToggleExpand = { showAllOutgoing = !showAllOutgoing },
            itemKeyProvider = { transaction -> transaction.id },
            itemContent = { transaction ->
                TransactionItem(transaction = transaction)
            },
            titleContent = {
                TransactionHeader(
                    sumOfTransactions = outgoingTransactions.sumOf { it.amount },
                    currency = outgoingTransactions.first().currency,
                    headerText = "Outgoing Transactions",
                )
            },
        )
    }
}

@Composable
fun TransactionHeader(sumOfTransactions: Double, currency: String, headerText: String){
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = headerText,
            fontWeight = FontWeight.SemiBold,
            color = SecondaryFontColor,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
        Row {
            Icon(
                tint = if (sumOfTransactions > 0) Color.Green else Color.Red,
                imageVector = if (sumOfTransactions > 0) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                contentDescription = null,
            )
            Text(
                text = "${TextFormatter.toBasicFormat(sumOfTransactions)} $currency",
                fontWeight = FontWeight.SemiBold,
                color = SecondaryFontColor,
                modifier = Modifier.padding(end = 12.dp),
                fontSize = 14.sp,
            )
        }
    }
}

@Composable
fun TransactionItem(transaction: Transaction) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier
                .background(Color.White)
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = transaction.icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier
                        .size(40.dp)
                        .background(transaction.iconBackgroundColor, CircleShape)
                        .padding(8.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(transaction.title, fontWeight = FontWeight.SemiBold)
                    Text(transaction.time, fontSize = 12.sp, color = Color.Gray)
                }
            }
            Text(
                text = "${TextFormatter.toBasicFormat(transaction.amount)} ${transaction.currency}",
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.End,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(end = normal.dp),
            )
        }
    }
}
