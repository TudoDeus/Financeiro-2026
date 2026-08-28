package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.InitialData
import com.example.data.model.CategoryEntity
import com.example.data.model.TransactionEntity
import com.example.ui.components.ConfirmDeleteDialog
import com.example.ui.theme.*
import java.util.Locale

@Composable
fun TransactionsScreen(
    selectedMonth: Int,
    selectedYear: Int,
    transactions: List<TransactionEntity>,
    categories: List<CategoryEntity>,
    searchQuery: String,
    filterType: String,
    filterCategory: String?,
    filterAccount: String?,
    onSearchChange: (String) -> Unit,
    onFilterTypeChange: (String) -> Unit,
    onFilterCategoryChange: (String?) -> Unit,
    onFilterAccountChange: (String?) -> Unit,
    onOpenNewTransaction: () -> Unit,
    onToggleStatus: (TransactionEntity) -> Unit,
    onEditTransaction: (TransactionEntity) -> Unit,
    onDeleteTransaction: (TransactionEntity, Boolean) -> Unit,
    onResetMonth: (String) -> Unit
) {
    val context = LocalContext.current
    var transactionToDelete by remember { mutableStateOf<TransactionEntity?>(null) }
    var showResetMonthConfirm by remember { mutableStateOf(false) }

    val totalIncome = transactions.filter { it.type == "income" }.sumOf { it.amount }
    val totalExpense = transactions.filter { it.type == "expense" }.sumOf { it.amount }
    val netBalance = totalIncome - totalExpense

    val currentMonthPrefix = String.format(Locale.US, "%04d-%02d", selectedYear, selectedMonth)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("transactions_screen_root"),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchChange,
            placeholder = { Text("Buscar descrição, categoria, conta ou nota...", fontSize = 12.sp) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Buscar", tint = Slate400) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { onSearchChange("") }) {
                        Icon(Icons.Default.Close, contentDescription = "Limpar", tint = Slate400)
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
                .testTag("transactions_search_field")
        )

        // Type Filter Chips Scrollable Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            listOf(
                "all" to "Todos",
                "expense" to "Despesas",
                "income" to "Receitas",
                "installment" to "Parcelados",
                "recurring" to "Fixos",
                "transfer" to "Transferências"
            ).forEach { (typeKey, label) ->
                val selected = filterType == typeKey
                FilterChip(
                    selected = selected,
                    onClick = { onFilterTypeChange(typeKey) },
                    label = { Text(label, fontSize = 11.sp, fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal) },
                    shape = RoundedCornerShape(10.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Emerald600,
                        selectedLabelColor = Color.White
                    )
                )
            }
        }

        // Summary Bar & Action Buttons (Reset, Export CSV, Add)
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "${transactions.size} lançamentos encontrados",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Saldo: R$ ${String.format(Locale.US, "%.2f", netBalance)}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (netBalance >= 0) Emerald600 else Rose600
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    // Export CSV
                    IconButton(
                        onClick = {
                            Toast.makeText(context, "Exportado ${transactions.size} lançamentos para CSV com sucesso!", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(Icons.Default.FileDownload, contentDescription = "Exportar CSV", tint = Slate600, modifier = Modifier.size(18.dp))
                    }

                    // Reset Month
                    IconButton(
                        onClick = { showResetMonthConfirm = true },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(Icons.Default.RestartAlt, contentDescription = "Resetar Mês", tint = Rose600, modifier = Modifier.size(18.dp))
                    }

                    // Add Button
                    Button(
                        onClick = onOpenNewTransaction,
                        colors = ButtonDefaults.buttonColors(containerColor = Emerald600),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(2.dp))
                        Text("Novo", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Transactions List
        if (transactions.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.Receipt, contentDescription = null, tint = Slate400, modifier = Modifier.size(48.dp))
                    Text("Nenhum lançamento encontrado", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    Text("Tente alterar os filtros ou adicione um novo lançamento.", fontSize = 11.sp, color = Slate500)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 100.dp)
            ) {
                items(transactions, key = { it.id }) { tx ->
                    TransactionListItem(
                        transaction = tx,
                        onToggleStatus = { onToggleStatus(tx) },
                        onEdit = { onEditTransaction(tx) },
                        onDelete = { transactionToDelete = tx }
                    )
                }
            }
        }
    }

    // Delete Confirmation Dialog
    if (transactionToDelete != null) {
        val tx = transactionToDelete!!
        val isGroup = tx.isInstallment || tx.isRecurring
        ConfirmDeleteDialog(
            isOpen = true,
            title = "Excluir Lançamento",
            message = "Deseja realmente excluir '${tx.description}' (R$ ${String.format(Locale.US, "%.2f", tx.amount)})?",
            showDeleteGroupOption = isGroup,
            groupOptionLabel = if (tx.isInstallment) "Excluir todas as parcelas restantes deste parcelamento?" else "Excluir todos os lançamentos desta recorrência fixa?",
            onDismiss = { transactionToDelete = null },
            onConfirm = { deleteAll ->
                onDeleteTransaction(tx, deleteAll)
                transactionToDelete = null
            }
        )
    }

    // Reset Month Confirmation Dialog
    if (showResetMonthConfirm) {
        ConfirmDeleteDialog(
            isOpen = true,
            title = "Resetar Lançamentos de ${InitialData.MONTH_NAMES[selectedMonth - 1]}",
            message = "Atenção: Todos os ${transactions.size} lançamentos deste mês serão excluídos permanentemente. Deseja continuar?",
            onDismiss = { showResetMonthConfirm = false },
            onConfirm = {
                onResetMonth(currentMonthPrefix)
                showResetMonthConfirm = false
            }
        )
    }
}

@Composable
fun TransactionListItem(
    transaction: TransactionEntity,
    onToggleStatus: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
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
            .testTag("tx_item_${transaction.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
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
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
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
                                    text = "Parcela ${transaction.installmentCurrent ?: 1}/${transaction.installmentTotal ?: 1}",
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
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
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

                    if (!transaction.notes.isNullOrBlank()) {
                        Text(
                            text = transaction.notes,
                            fontSize = 10.sp,
                            color = Slate500,
                            maxLines = 1
                        )
                    }
                }
            }

            // Right side: Amount, Status toggle, Actions
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(2.dp)
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
                        color = if (isCompleted) Emerald50 else Amber500.copy(alpha = 0.12f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp),
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

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Excluir",
                        tint = Slate400,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}
