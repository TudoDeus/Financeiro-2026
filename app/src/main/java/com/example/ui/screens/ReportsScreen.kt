package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
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
import com.example.data.model.CategoryEntity
import com.example.data.model.TransactionEntity
import com.example.ui.theme.*
import java.util.Locale

@Composable
fun ReportsScreen(
    selectedYear: Int,
    allTransactions: List<TransactionEntity>,
    categories: List<CategoryEntity>
) {
    val yearPrefix = "$selectedYear-"
    val yearTransactions = allTransactions.filter { it.date.startsWith(yearPrefix) }

    val annualIncome = yearTransactions.filter { it.type == "income" }.sumOf { it.amount }
    val annualExpense = yearTransactions.filter { it.type == "expense" }.sumOf { it.amount }
    val annualBalance = annualIncome - annualExpense
    val avgSavingsRate = if (annualIncome > 0) Math.max(0, Math.min(100, Math.round(((annualIncome - annualExpense) / annualIncome) * 100).toInt())) else 0

    // Monthly data for all 12 months
    val monthlyData = (1..12).map { m ->
        val mPrefix = String.format(Locale.US, "%04d-%02d", selectedYear, m)
        val mInc = allTransactions.filter { it.date.startsWith(mPrefix) && it.type == "income" }.sumOf { it.amount }
        val mExp = allTransactions.filter { it.date.startsWith(mPrefix) && it.type == "expense" }.sumOf { it.amount }
        Triple(InitialData.MONTH_NAMES[m - 1], mInc, mExp)
    }

    // Category distribution
    val expenseCategories = categories.filter { it.type == "expense" }
    val categoryRanking = expenseCategories.map { cat ->
        val spent = yearTransactions.filter { it.category == cat.name && it.type == "expense" }.sumOf { it.amount }
        cat to spent
    }.filter { it.second > 0 }.sortedByDescending { it.second }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("reports_screen_root"),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 10.dp, bottom = 100.dp)
    ) {
        // Annual Overview Banner
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.linearGradient(
                                listOf(
                                    Color(0xFF0F172A),
                                    Color(0xFF1E293B),
                                    Color(0xFF064E3B)
                                )
                            )
                        )
                        .padding(18.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("RELATÓRIO ANUAL", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Emerald300)
                                Text("Consolidado de $selectedYear", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = Color.White)
                            }
                            Surface(
                                color = Emerald500.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text(
                                    text = "$avgSavingsRate% Poupança",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Emerald300,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Divider(color = Color.White.copy(alpha = 0.15f))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Total Receitas", fontSize = 10.sp, color = Emerald100)
                                Text(
                                    "R$ ${String.format(Locale.US, "%,.2f", annualIncome).replace(",", "X").replace(".", ",").replace("X", ".")}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = Emerald300
                                )
                            }
                            Column {
                                Text("Total Despesas", fontSize = 10.sp, color = Color(0xFFFECDD3))
                                Text(
                                    "R$ ${String.format(Locale.US, "%,.2f", annualExpense).replace(",", "X").replace(".", ",").replace("X", ".")}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = Color(0xFFFECDD3)
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Economia Anual", fontSize = 10.sp, color = Color(0xFFBFDBFE))
                                Text(
                                    "R$ ${String.format(Locale.US, "%,.2f", annualBalance).replace(",", "X").replace(".", ",").replace("X", ".")}",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 13.sp,
                                    color = if (annualBalance >= 0) Emerald300 else Color(0xFFFECDD3)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Monthly Breakdown Chart List
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(Icons.Default.BarChart, contentDescription = null, tint = Emerald600, modifier = Modifier.size(20.dp))
                        Text("Evolução Mensal ($selectedYear)", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }

                    monthlyData.forEach { (mName, inc, exp) ->
                        val hasData = inc > 0 || exp > 0
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 3.dp),
                            verticalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = mName,
                                    fontSize = 11.sp,
                                    fontWeight = if (hasData) FontWeight.Bold else FontWeight.Normal,
                                    color = if (hasData) MaterialTheme.colorScheme.onSurface else Slate400
                                )
                                if (hasData) {
                                    Text(
                                        text = "+ R$ ${String.format(Locale.US, "%.0f", inc)}  /  - R$ ${String.format(Locale.US, "%.0f", exp)}",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (inc >= exp) Emerald600 else Rose600
                                    )
                                } else {
                                    Text("Sem lançamentos", fontSize = 10.sp, color = Slate400)
                                }
                            }

                            if (hasData) {
                                val maxVal = Math.max(1.0, Math.max(inc, exp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    LinearProgressIndicator(
                                        progress = { (inc / maxVal).toFloat() },
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(5.dp)
                                            .clip(RoundedCornerShape(2.dp)),
                                        color = Emerald500,
                                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                                    )
                                    LinearProgressIndicator(
                                        progress = { (exp / maxVal).toFloat() },
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(5.dp)
                                            .clip(RoundedCornerShape(2.dp)),
                                        color = Rose500,
                                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Top Annual Categories Ranking
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(Icons.Default.Category, contentDescription = null, tint = Indigo600, modifier = Modifier.size(20.dp))
                        Text("Maiores Despesas Acumuladas no Ano", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }

                    if (categoryRanking.isEmpty()) {
                        Text("Nenhuma despesa registrada no ano.", fontSize = 12.sp, color = Slate400)
                    } else {
                        val totalExp = Math.max(1.0, annualExpense)
                        categoryRanking.forEachIndexed { idx, (cat, spent) ->
                            val pct = Math.min(100, Math.round((spent / totalExp) * 100).toInt())
                            val color = try { Color(android.graphics.Color.parseColor(cat.color)) } catch (e: Exception) { Emerald500 }

                            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("${idx + 1}. ${cat.name}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                    Text("R$ ${String.format(Locale.US, "%.2f", spent)} ($pct%)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                                LinearProgressIndicator(
                                    progress = { pct / 100f },
                                    modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                                    color = color,
                                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
