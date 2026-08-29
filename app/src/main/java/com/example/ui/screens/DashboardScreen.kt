package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.local.InitialData
import com.example.data.model.*
import com.example.ui.theme.*
import com.example.ui.util.getCategoryVisual
import com.example.ui.util.toCurrency
import com.example.ui.util.toFormattedDate
import com.example.ui.viewmodel.CumulativeBalanceSummary
import java.util.Locale

@Composable
fun DashboardScreen(
    selectedMonth: Int,
    selectedYear: Int,
    monthSummary: MonthSummary,
    cumulativeSummary: CumulativeBalanceSummary,
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

    val expenseCategories = categories.filter { it.type == "expense" }
    val categoryExpenses = expenseCategories.map { cat ->
        val spent = transactions.filter { it.category == cat.name && it.type == "expense" }.sumOf { it.amount }
        cat to spent
    }.filter { it.second > 0 }.sortedByDescending { it.second }

    // Collapsible states - Collapsed by default as requested
    var showCumulativeBalance by remember { mutableStateOf(false) }
    var showCategoriesExpanded by remember { mutableStateOf(false) }
    var showTransactionsExpanded by remember { mutableStateOf(false) }
    var showGoalsExpanded by remember { mutableStateOf(false) }
    var showAccountsExpanded by remember { mutableStateOf(false) }

    // Selected category for detail inspection (e.g. Terreno + respective incomes)
    var inspectingCategory by remember { mutableStateOf<CategoryEntity?>(null) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("dashboard_screen_scroll"),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 100.dp)
    ) {
        // 1. Hero Balance Card (Enlarged & Prominent)
        item {
            Card(
                shape = RoundedCornerShape(26.dp),
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
                                    Color(0xFF044E3B),
                                    Color(0xFF0D6B63),
                                    Color(0xFF0F172A)
                                )
                            )
                        )
                        .padding(24.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "SALDO GERAL EM CONTAS",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.5.sp,
                                    color = Emerald100
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = totalAccountBalances.toCurrency(),
                                    style = MaterialTheme.typography.headlineLarge.copy(
                                        fontWeight = FontWeight.ExtraBold,
                                        letterSpacing = (-0.5).sp,
                                        fontSize = 32.sp
                                    ),
                                    color = Color.White
                                )
                            }
                            Surface(
                                color = Emerald500.copy(alpha = 0.25f),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(Icons.Default.TrendingUp, contentDescription = null, tint = Emerald300, modifier = Modifier.size(18.dp))
                                    Text(
                                        text = "${monthSummary.savingsRate}% Poupança",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }
                        }

                        HorizontalDivider(color = Color.White.copy(alpha = 0.18f), thickness = 1.dp)

                        // 3 Stats Columns
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            // Receitas
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Icon(Icons.Default.ArrowCircleUp, contentDescription = null, tint = Emerald300, modifier = Modifier.size(16.dp))
                                    Text("Receitas", fontSize = 12.sp, color = Emerald100)
                                }
                                Text(
                                    text = monthSummary.totalIncome.toCurrency(),
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Emerald100
                                )
                            }

                            // Despesas
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Icon(Icons.Default.ArrowCircleDown, contentDescription = null, tint = Color(0xFFFDA4AF), modifier = Modifier.size(16.dp))
                                    Text("Despesas", fontSize = 12.sp, color = Color(0xFFFECDD3))
                                }
                                Text(
                                    text = monthSummary.totalExpense.toCurrency(),
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFFFECDD3)
                                )
                            }

                            // Balanço Mês
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Icon(Icons.Default.AccountBalance, contentDescription = null, tint = Color(0xFF93C5FD), modifier = Modifier.size(16.dp))
                                    Text("Balanço Mês", fontSize = 12.sp, color = Color(0xFFBFDBFE))
                                }
                                Text(
                                    text = monthSummary.balance.toCurrency(),
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (monthSummary.balance >= 0) Emerald100 else Color(0xFFFECDD3)
                                )
                            }
                        }

                        // Cumulative Balance Button
                        Surface(
                            onClick = { showCumulativeBalance = !showCumulativeBalance },
                            shape = RoundedCornerShape(12.dp),
                            color = Color.White.copy(alpha = 0.15f),
                            modifier = Modifier.fillMaxWidth().testTag("cumulative_balance_toggle")
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
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CompareArrows,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(
                                        text = "Balanço Acumulado: ${cumulativeSummary.currentMonthName} + ${cumulativeSummary.prevMonthName}",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                                val iconRotation by animateFloatAsState(if (showCumulativeBalance) 180f else 0f)
                                Icon(
                                    imageVector = Icons.Default.KeyboardArrowDown,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.rotate(iconRotation)
                                )
                            }
                        }

                        // Expanded Cumulative Balance Breakdown
                        AnimatedVisibility(visible = showCumulativeBalance) {
                            Surface(
                                color = Color.Black.copy(alpha = 0.3f),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(14.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = "SOMA DOS 2 MESES (Balanço Conjunto):",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Emerald200
                                    )
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "• ${cumulativeSummary.currentMonthName}:",
                                            fontSize = 12.sp,
                                            color = Color.White
                                        )
                                        Text(
                                            text = cumulativeSummary.currentMonthBalance.toCurrency(),
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (cumulativeSummary.currentMonthBalance >= 0) Emerald300 else Color(0xFFFDA4AF)
                                        )
                                    }
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "• ${cumulativeSummary.prevMonthName} (Anterior):",
                                            fontSize = 12.sp,
                                            color = Color.White
                                        )
                                        Text(
                                            text = cumulativeSummary.prevMonthBalance.toCurrency(),
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (cumulativeSummary.prevMonthBalance >= 0) Emerald300 else Color(0xFFFDA4AF)
                                        )
                                    }
                                    HorizontalDivider(color = Color.White.copy(alpha = 0.2f), thickness = 1.dp)
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "Total Acumulado:",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = Color.White
                                        )
                                        Text(
                                            text = cumulativeSummary.totalCumulativeBalance.toCurrency(),
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = if (cumulativeSummary.totalCumulativeBalance >= 0) Color(0xFF86EFAC) else Color(0xFFFCA5A5)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // 2. Quick Shortcuts Row
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                listOf(
                    Triple("Novo Lançamento", Icons.Default.AddCircle, Emerald600 to onOpenNewTransaction),
                    Triple("Lançamentos", Icons.Default.ReceiptLong, Indigo600 to onNavigateToTransactions),
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

        // 3. Categories Spending Breakdown Card (Collapsible, Click to inspect related incomes)
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showCategoriesExpanded = !showCategoriesExpanded }
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Default.PieChart, contentDescription = null, tint = Emerald600, modifier = Modifier.size(20.dp))
                            Column {
                                Text(
                                    text = "Despesas por Categoria",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = if (categoryExpenses.isEmpty()) "Nenhuma despesa" else "${categoryExpenses.size} categorias ativas • Clique para ver receitas",
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        val rotation by animateFloatAsState(if (showCategoriesExpanded) 180f else 0f)
                        IconButton(onClick = { showCategoriesExpanded = !showCategoriesExpanded }) {
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowDown,
                                contentDescription = if (showCategoriesExpanded) "Recolher" else "Expandir",
                                modifier = Modifier.rotate(rotation)
                            )
                        }
                    }

                    AnimatedVisibility(visible = showCategoriesExpanded) {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            if (categoryExpenses.isEmpty()) {
                                Text(
                                    text = "Nenhuma despesa registrada para ${InitialData.MONTH_NAMES[selectedMonth - 1]}.",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(vertical = 8.dp)
                                )
                            } else {
                                val totalExp = Math.max(1.0, monthSummary.totalExpense)
                                categoryExpenses.forEach { (cat, spent) ->
                                    val percent = Math.min(100, Math.round((spent / totalExp) * 100).toInt())
                                    val catColor = try { Color(android.graphics.Color.parseColor(cat.color)) } catch (e: Exception) { Emerald500 }

                                    Surface(
                                        onClick = { inspectingCategory = cat },
                                        shape = RoundedCornerShape(10.dp),
                                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                        modifier = Modifier.fillMaxWidth().testTag("cat_item_${cat.name.lowercase()}")
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(10.dp),
                                            verticalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                ) {
                                                    Box(
                                                        modifier = Modifier
                                                            .size(10.dp)
                                                            .clip(CircleShape)
                                                            .background(catColor)
                                                    )
                                                    Text(
                                                        text = cat.name,
                                                        fontSize = 13.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = MaterialTheme.colorScheme.onSurface
                                                    )
                                                }
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                                ) {
                                                    Text(
                                                        text = "${spent.toCurrency()} ($percent%)",
                                                        fontSize = 12.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = MaterialTheme.colorScheme.onSurface
                                                    )
                                                    Icon(
                                                        imageVector = Icons.Default.ChevronRight,
                                                        contentDescription = "Ver Receitas",
                                                        tint = Slate400,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                }
                                            }
                                            LinearProgressIndicator(
                                                progress = { percent / 100f },
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(7.dp)
                                                    .clip(RoundedCornerShape(4.dp)),
                                                color = catColor,
                                                trackColor = MaterialTheme.colorScheme.surface
                                            )
                                        }
                                    }
                                }
                            }

                            TextButton(
                                onClick = onNavigateToBudget,
                                modifier = Modifier.align(Alignment.End)
                            ) {
                                Text("Abrir Planejamento de Orçamento", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Emerald600)
                            }
                        }
                    }
                }
            }
        }

        // 4. Recent Transactions Card (Collapsible by default)
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showTransactionsExpanded = !showTransactionsExpanded }
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Default.ReceiptLong, contentDescription = null, tint = Emerald600, modifier = Modifier.size(20.dp))
                            Column {
                                Text(
                                    text = "Últimos Lançamentos (${InitialData.MONTH_NAMES[selectedMonth - 1]})",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = "${transactions.size} lançamentos • Clique para expandir",
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        val rotation by animateFloatAsState(if (showTransactionsExpanded) 180f else 0f)
                        IconButton(onClick = { showTransactionsExpanded = !showTransactionsExpanded }) {
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowDown,
                                contentDescription = if (showTransactionsExpanded) "Recolher" else "Expandir",
                                modifier = Modifier.rotate(rotation)
                            )
                        }
                    }

                    AnimatedVisibility(visible = showTransactionsExpanded) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            if (transactions.isEmpty()) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(Icons.Default.Receipt, contentDescription = null, tint = Slate400, modifier = Modifier.size(32.dp))
                                    Text("Nenhum lançamento no mês", fontSize = 12.sp, color = Slate600)
                                    Button(
                                        onClick = onOpenNewTransaction,
                                        colors = ButtonDefaults.buttonColors(containerColor = Emerald600),
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                    ) {
                                        Text("+ Novo", fontSize = 11.sp)
                                    }
                                }
                            } else {
                                transactions.take(6).forEach { tx ->
                                    TransactionCardItem(
                                        transaction = tx,
                                        onToggleStatus = { onToggleTransactionStatus(tx) },
                                        onEdit = { onEditTransaction(tx) }
                                    )
                                }

                                TextButton(
                                    onClick = onNavigateToTransactions,
                                    modifier = Modifier.align(Alignment.End)
                                ) {
                                    Text("Ver Todos os ${transactions.size} Lançamentos", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Emerald600)
                                }
                            }
                        }
                    }
                }
            }
        }

        // 5. Goals & Reserves Card (Collapsible by default)
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showGoalsExpanded = !showGoalsExpanded }
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Default.Savings, contentDescription = null, tint = Amber600, modifier = Modifier.size(20.dp))
                            Column {
                                Text(
                                    text = "Metas & Reservas 2026",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = "${goals.size} metas cadastradas • Clique para expandir",
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        val rotation by animateFloatAsState(if (showGoalsExpanded) 180f else 0f)
                        IconButton(onClick = { showGoalsExpanded = !showGoalsExpanded }) {
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowDown,
                                contentDescription = if (showGoalsExpanded) "Recolher" else "Expandir",
                                modifier = Modifier.rotate(rotation)
                            )
                        }
                    }

                    AnimatedVisibility(visible = showGoalsExpanded) {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            goals.forEach { g ->
                                val pct = Math.min(100, Math.round((g.currentAmount / g.targetAmount) * 100).toInt())
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(g.title, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                        Text("${g.currentAmount.toCurrency()} / ${g.targetAmount.toCurrency()} ($pct%)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Emerald600)
                                    }
                                    LinearProgressIndicator(
                                        progress = { pct / 100f },
                                        modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                                        color = Emerald500,
                                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                                    )
                                }
                            }

                            TextButton(
                                onClick = onNavigateToGoals,
                                modifier = Modifier.align(Alignment.End)
                            ) {
                                Text("Gerenciar Todas as Metas", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Amber600)
                            }
                        }
                    }
                }
            }
        }

        // 6. Accounts & Cards Quick Overview (Collapsible by default)
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showAccountsExpanded = !showAccountsExpanded }
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Default.CreditCard, contentDescription = null, tint = Indigo600, modifier = Modifier.size(20.dp))
                            Column {
                                Text(
                                    text = "Saldos das Contas & Faturas",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = "${accounts.size} contas e ${creditCards.size} cartões",
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        val rotation by animateFloatAsState(if (showAccountsExpanded) 180f else 0f)
                        IconButton(onClick = { showAccountsExpanded = !showAccountsExpanded }) {
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowDown,
                                contentDescription = if (showAccountsExpanded) "Recolher" else "Expandir",
                                modifier = Modifier.rotate(rotation)
                            )
                        }
                    }

                    AnimatedVisibility(visible = showAccountsExpanded) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            accounts.forEach { acc ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(text = "🏛️ ${acc.name}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                    Text(
                                        text = "R$ ${String.format(Locale.US, "%.2f", acc.balance)}",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (acc.balance >= 0) Emerald600 else Rose600
                                    )
                                }
                            }

                            if (creditCards.isNotEmpty()) {
                                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 0.5.dp)
                                creditCards.forEach { card ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(text = "💳 ${card.name}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                        Text(
                                            text = "Fatura: R$ ${String.format(Locale.US, "%.2f", card.currentInvoice)}",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Rose600
                                        )
                                    }
                                }
                            }

                            TextButton(
                                onClick = onNavigateToAccounts,
                                modifier = Modifier.align(Alignment.End)
                            ) {
                                Text("Acessar Contas e Cartões", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Indigo600)
                            }
                        }
                    }
                }
            }
        }
    }

    // Modal: Category Expenses & Respective Incomes Inspection (e.g. Terreno)
    if (inspectingCategory != null) {
        val cat = inspectingCategory!!
        val catExpenses = transactions.filter { it.category == cat.name && it.type == "expense" }
        val allMonthIncomes = transactions.filter { it.type == "income" }
        val catSpent = catExpenses.sumOf { it.amount }
        val totalIncomes = allMonthIncomes.sumOf { it.amount }

        Dialog(onDismissRequest = { inspectingCategory = null }) {
            Surface(
                shape = RoundedCornerShape(22.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
                    .testTag("category_income_inspection_dialog")
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Emerald500.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.AccountBalance, contentDescription = null, tint = Emerald700, modifier = Modifier.size(20.dp))
                            }
                            Column {
                                Text(
                                    text = "Categoria: ${cat.name}",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold)
                                )
                                Text(
                                    text = "Despesas e Receitas do Mês",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        IconButton(onClick = { inspectingCategory = null }, modifier = Modifier.size(28.dp)) {
                            Icon(Icons.Default.Close, contentDescription = "Fechar")
                        }
                    }

                    // Total expense card
                    Surface(
                        color = Rose50,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Total Despesas (${cat.name}):",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Rose700
                            )
                            Text(
                                text = "R$ ${String.format(Locale.US, "%.2f", catSpent)}",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Rose700
                            )
                        }
                    }

                    // Section 1: Despesas da Categoria
                    Text(
                        text = "Lançamentos de Despesa (${catExpenses.size}):",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Slate700
                    )

                    if (catExpenses.isEmpty()) {
                        Text(
                            text = "Nenhuma despesa para esta categoria neste mês.",
                            fontSize = 11.sp,
                            color = Slate500
                        )
                    } else {
                        catExpenses.forEach { tx ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(tx.description, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                    Text("${tx.date.split("-").reversed().joinToString("/")} • ${tx.account}", fontSize = 10.sp, color = Slate500)
                                }
                                Text(
                                    text = "- R$ ${String.format(Locale.US, "%.2f", tx.amount)}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Rose600
                                )
                            }
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 0.5.dp)

                    // Section 2: Receitas do Mês (Para cobertura de custos)
                    Text(
                        text = "Receitas Respectivas do Período (${allMonthIncomes.size} - Total: R$ ${String.format(Locale.US, "%.2f", totalIncomes)}):",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Emerald700
                    )

                    if (allMonthIncomes.isEmpty()) {
                        Text(
                            text = "Nenhuma receita registrada neste mês.",
                            fontSize = 11.sp,
                            color = Slate500
                        )
                    } else {
                        allMonthIncomes.forEach { inc ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Emerald50)
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(inc.description, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Slate800)
                                    Text("${inc.date.split("-").reversed().joinToString("/")} • ${inc.category} (${inc.account})", fontSize = 10.sp, color = Slate600)
                                }
                                Text(
                                    text = "+ R$ ${String.format(Locale.US, "%.2f", inc.amount)}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Emerald700
                                )
                            }
                        }
                    }

                    Button(
                        onClick = { inspectingCategory = null },
                        colors = ButtonDefaults.buttonColors(containerColor = Emerald600),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Fechar")
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
    val visual = getCategoryVisual(transaction.category, transaction.type)

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onEdit() }
            .testTag("transaction_item_${transaction.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(visual.containerColor),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = visual.icon,
                        contentDescription = transaction.category,
                        tint = visual.color,
                        modifier = Modifier.size(17.dp)
                    )
                }

                Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                    Text(
                        text = transaction.description,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1
                    )
                    Text(
                        text = "${transaction.date.toFormattedDate()} • ${transaction.category}",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Amount and Status
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = "${if (isIncome) "+ " else if (isTransfer) "" else "- "}${transaction.amount.toCurrency()}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (isIncome) Emerald600 else if (isTransfer) Indigo600 else Rose600
                )

                Surface(
                    onClick = onToggleStatus,
                    shape = RoundedCornerShape(6.dp),
                    color = if (isCompleted) Emerald50 else Amber500.copy(alpha = 0.15f),
                    modifier = Modifier.testTag("status_toggle_${transaction.id}")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Icon(
                            imageVector = if (isCompleted) Icons.Default.CheckCircle else Icons.Default.Schedule,
                            contentDescription = null,
                            tint = if (isCompleted) Emerald700 else Amber600,
                            modifier = Modifier.size(10.dp)
                        )
                        Text(
                            text = if (isCompleted) "Pago" else "Pendente",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isCompleted) Emerald700 else Amber600
                        )
                    }
                }
            }
        }
    }
}
