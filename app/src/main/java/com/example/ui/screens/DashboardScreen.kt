package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.InitialData
import com.example.data.model.*
import com.example.ui.theme.*
import java.util.Locale

@Composable
fun DashboardScreen(
    selectedMonth: Int,
    selectedYear: Int,
    monthSummary: MonthSummary,
    transactions: List<TransactionEntity>,
    categories: List<CategoryEntity>,
    accounts: List<AccountEntity>,
    creditCards: List<CreditCardEntity>,
    goals: List<GoalEntity>,
    onOpenNewTransaction: () -> Unit,
    onNavigateToTransactions: () -> Unit,
    onNavigateToGoals: () -> Unit,
    onNavigateToBudget: () -> Unit,
    onNavigateToAccounts: () -> Unit,
    onToggleTransactionStatus: (TransactionEntity) -> Unit,
    onEditTransaction: (TransactionEntity) -> Unit
) {
    val totalAccountBalances = accounts.sumOf { it.balance }
    val totalCardInvoices = creditCards.sumOf { it.currentInvoice }

    val expenseCategories = categories.filter { it.type == "expense" }
    val categoryExpenses = expenseCategories.map { cat ->
        val spent = transactions.filter { it.category == cat.name && it.type == "expense" }.sumOf { it.amount }
        cat to spent
    }.filter { it.second > 0 }.sortedByDescending { it.second }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("dashboard_screen_scroll"),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 100.dp)
    ) {
        // Hero Balance Card
        item {
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("hero_balance_card")
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.linearGradient(
                                listOf(
                                    Color(0xFF064E3B),
                                    Color(0xFF0F766E),
                                    Color(0xFF0F172A)
                                )
                            )
                        )
                        .padding(20.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "SALDO TOTAL EM CONTAS",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Emerald100
                                )
                                Text(
                                    text = "R$ ${String.format(Locale.US, "%,.2f", totalAccountBalances).replace(",", "X").replace(".", ",").replace("X", ".")}",
                                    style = MaterialTheme.typography.headlineMedium.copy(
                                        fontWeight = FontWeight.ExtraBold,
                                        letterSpacing = (-0.5).sp
                                    ),
                                    color = Color.White
                                )
                            }
                            Surface(
                                color = Emerald500.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(Icons.Default.TrendingUp, contentDescription = null, tint = Emerald400, modifier = Modifier.size(16.dp))
                                    Text(
                                        text = "${monthSummary.savingsRate}% Poupança",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Emerald100
                                    )
                                }
                            }
                        }

                        Divider(color = Color.White.copy(alpha = 0.15f))

                        // 3 Column Mini Stats: Receitas, Despesas, Balanço do Mês
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            // Receitas
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Icon(Icons.Default.ArrowCircleUp, contentDescription = null, tint = Emerald400, modifier = Modifier.size(14.dp))
                                    Text("Receitas", fontSize = 11.sp, color = Emerald100)
                                }
                                Text(
                                    text = "R$ ${String.format(Locale.US, "%.2f", monthSummary.totalIncome)}",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Emerald100
                                )
                            }

                            // Despesas
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Icon(Icons.Default.ArrowCircleDown, contentDescription = null, tint = Color(0xFFFDA4AF), modifier = Modifier.size(14.dp))
                                    Text("Despesas", fontSize = 11.sp, color = Color(0xFFFECDD3))
                                }
                                Text(
                                    text = "R$ ${String.format(Locale.US, "%.2f", monthSummary.totalExpense)}",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFFECDD3)
                                )
                            }

                            // Balanço Líquido
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Icon(Icons.Default.AccountBalance, contentDescription = null, tint = Color(0xFF93C5FD), modifier = Modifier.size(14.dp))
                                    Text("Balanço", fontSize = 11.sp, color = Color(0xFFBFDBFE))
                                }
                                Text(
                                    text = "R$ ${String.format(Locale.US, "%.2f", monthSummary.balance)}",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (monthSummary.balance >= 0) Emerald100 else Color(0xFFFECDD3)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Quick Shortcuts Row
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                listOf(
                    Triple("Novo Lançamento", Icons.Default.AddCircle, Emerald600 to onOpenNewTransaction),
                    Triple("Ver Lançamentos", Icons.Default.ReceiptLong, Indigo600 to onNavigateToTransactions),
                    Triple("Metas 2026", Icons.Default.Savings, Amber600 to onNavigateToGoals),
                    Triple("Contas/Cartões", Icons.Default.CreditCard, Slate700 to onNavigateToAccounts)
                ).forEach { (label, icon, pair) ->
                    val (color, action) = pair
                    Surface(
                        onClick = action,
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.surface,
                        shadowElevation = 1.dp,
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(
                            modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(color.copy(alpha = 0.12f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(imageVector = icon, contentDescription = label, tint = color, modifier = Modifier.size(18.dp))
                            }
                            Text(
                                text = label,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }

        // Categories Spending Breakdown Card
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shadowElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Default.PieChart, contentDescription = null, tint = Emerald600, modifier = Modifier.size(20.dp))
                            Text(
                                text = "Despesas por Categoria",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                        TextButton(onClick = onNavigateToBudget) {
                            Text("Ver Orçamento", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Emerald600)
                        }
                    }

                    if (categoryExpenses.isEmpty()) {
                        Text(
                            text = "Nenhuma despesa registrada para ${InitialData.MONTH_NAMES[selectedMonth - 1]}.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    } else {
                        val totalExp = Math.max(1.0, monthSummary.totalExpense)
                        categoryExpenses.take(5).forEach { (cat, spent) ->
                            val percent = Math.min(100, Math.round((spent / totalExp) * 100).toInt())
                            val catColor = try { Color(android.graphics.Color.parseColor(cat.color)) } catch (e: Exception) { Emerald500 }

                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = cat.name,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "R$ ${String.format(Locale.US, "%.2f", spent)} ($percent%)",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                LinearProgressIndicator(
                                    progress = { percent / 100f },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(7.dp)
                                        .clip(RoundedCornerShape(4.dp)),
                                    color = catColor,
                                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }

        // Recent Transactions Header & List
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(Icons.Default.ReceiptLong, contentDescription = null, tint = Emerald600, modifier = Modifier.size(18.dp))
                    Text(
                        text = "Últimos Lançamentos (${InitialData.MONTH_NAMES[selectedMonth - 1]})",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )
                }
                TextButton(onClick = onNavigateToTransactions) {
                    Text("Ver Todos (${transactions.size})", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Emerald600)
                }
            }
        }

        if (transactions.isEmpty()) {
            item {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.Receipt, contentDescription = null, tint = Slate400, modifier = Modifier.size(36.dp))
                        Text("Nenhum lançamento neste mês", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        Button(
                            onClick = onOpenNewTransaction,
                            colors = ButtonDefaults.buttonColors(containerColor = Emerald600),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("+ Novo Lançamento", fontSize = 12.sp)
                        }
                    }
                }
            }
        } else {
            items(transactions.take(6), key = { it.id }) { tx ->
                TransactionCardItem(
                    transaction = tx,
                    onToggleStatus = { onToggleTransactionStatus(tx) },
                    onEdit = { onEditTransaction(tx) }
                )
            }
        }

        // Goals Snapshot
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shadowElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Default.Savings, contentDescription = null, tint = Amber600, modifier = Modifier.size(20.dp))
                            Text(
                                text = "Metas & Reservas 2026",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                        TextButton(onClick = onNavigateToGoals) {
                            Text("Acompanhar", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Amber600)
                        }
                    }

                    goals.take(3).forEach { g ->
                        val pct = Math.min(100, Math.round((g.currentAmount / g.targetAmount) * 100).toInt())
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(g.title, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                Text("$pct%", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Emerald600)
                            }
                            LinearProgressIndicator(
                                progress = { pct / 100f },
                                modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                                color = Emerald500,
                                trackColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TransactionCardItem(
    transaction: TransactionEntity,
    onToggleStatus: () -> Unit,
    onEdit: () -> Unit
) {
    val isIncome = transaction.type == "income"
    val isTransfer = transaction.type == "transfer"
    val isCompleted = transaction.status == "completed"

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 1.dp,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onEdit() }
            .testTag("transaction_item_${transaction.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                // Type Icon
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(
                            if (isIncome) Emerald50
                            else if (isTransfer) Indigo500.copy(alpha = 0.1f)
                            else Rose50
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isIncome) Icons.Default.ArrowUpward
                        else if (isTransfer) Icons.Default.SwapHoriz
                        else Icons.Default.ArrowDownward,
                        contentDescription = null,
                        tint = if (isIncome) Emerald600 else if (isTransfer) Indigo600 else Rose600,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = transaction.description,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1
                        )
                        if (transaction.isInstallment) {
                            Surface(
                                color = Indigo500.copy(alpha = 0.12f),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = "Parc.",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Indigo700,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                )
                            }
                        } else if (transaction.isRecurring) {
                            Surface(
                                color = Emerald500.copy(alpha = 0.12f),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = "Fixo",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Emerald700,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                )
                            }
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = transaction.date.split("-").reversed().joinToString("/"),
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text("•", fontSize = 10.sp, color = Slate400)
                        Text(
                            text = transaction.category,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text("•", fontSize = 10.sp, color = Slate400)
                        Text(
                            text = transaction.account,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1
                        )
                    }
                }
            }

            // Amount and Status Toggle
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "${if (isIncome) "+ " else if (isTransfer) "" else "- "}R$ ${String.format(Locale.US, "%.2f", transaction.amount)}",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (isIncome) Emerald600 else if (isTransfer) Indigo600 else Rose600
                )

                Surface(
                    onClick = onToggleStatus,
                    shape = RoundedCornerShape(6.dp),
                    color = if (isCompleted) Emerald50 else Amber500.copy(alpha = 0.12f),
                    modifier = Modifier.testTag("status_toggle_${transaction.id}")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Icon(
                            imageVector = if (isCompleted) Icons.Default.CheckCircle else Icons.Default.Schedule,
                            contentDescription = null,
                            tint = if (isCompleted) Emerald700 else Amber600,
                            modifier = Modifier.size(11.dp)
                        )
                        Text(
                            text = if (isCompleted) "Pago" else "Pendente",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isCompleted) Emerald700 else Amber600
                        )
                    }
                }
            }
        }
    }
}
