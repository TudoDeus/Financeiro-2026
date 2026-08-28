package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.local.InitialData
import com.example.data.model.CategoryEntity
import com.example.data.model.TransactionEntity
import com.example.ui.components.CategoryDialog
import com.example.ui.components.ConfirmDeleteDialog
import com.example.ui.theme.*
import java.util.Locale

@Composable
fun BudgetScreen(
    selectedMonth: Int,
    selectedYear: Int,
    categories: List<CategoryEntity>,
    transactions: List<TransactionEntity>,
    onAddCategory: (CategoryEntity) -> Unit,
    onUpdateCategory: (CategoryEntity) -> Unit,
    onDeleteCategory: (CategoryEntity) -> Unit,
    onUpdateCategoryBudget: (String, Double) -> Unit
) {
    var activeTab by remember { mutableStateOf("expense") } // "expense" | "income"
    var showNewCategoryDialog by remember { mutableStateOf(false) }
    var categoryToEdit by remember { mutableStateOf<CategoryEntity?>(null) }
    var categoryToDelete by remember { mutableStateOf<CategoryEntity?>(null) }
    var categoryForInlineBudget by remember { mutableStateOf<CategoryEntity?>(null) }

    val filteredCategories = categories.filter { it.type == activeTab }

    // Math for Expenses vs Budget
    val totalBudget = filteredCategories.sumOf { it.monthlyBudget }
    val totalActual = filteredCategories.sumOf { cat ->
        transactions.filter { it.category == cat.name && it.type == activeTab }.sumOf { it.amount }
    }
    val overallProgress = if (totalBudget > 0) Math.min(100, Math.round((totalActual / totalBudget) * 100).toInt()) else 0
    val remainingBudget = totalBudget - totalActual

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("budget_screen_root"),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(top = 10.dp, bottom = 100.dp)
    ) {
        // Budget Overview Card
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (activeTab == "expense") {
                        if (remainingBudget >= 0) Emerald900 else Color(0xFF881337)
                    } else Emerald900
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = if (activeTab == "expense") "ORÇAMENTO DE ${InitialData.MONTH_NAMES[selectedMonth - 1].uppercase()}" else "META DE RECEITAS",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Emerald100
                            )
                            Text(
                                text = "R$ ${String.format(Locale.US, "%,.2f", totalActual).replace(",", "X").replace(".", ",").replace("X", ".")} de R$ ${String.format(Locale.US, "%,.2f", totalBudget).replace(",", "X").replace(".", ",").replace("X", ".")}",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                                color = Color.White
                            )
                        }
                        Surface(
                            color = Color.White.copy(alpha = 0.18f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(
                                text = "$overallProgress%",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    // Progress Bar
                    LinearProgressIndicator(
                        progress = { Math.min(1f, overallProgress / 100f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = if (activeTab == "expense" && overallProgress > 100) Rose500 else Emerald400,
                        trackColor = Color.Black.copy(alpha = 0.3f)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = if (activeTab == "expense") {
                                if (remainingBudget >= 0) "Economia disponível: R$ ${String.format(Locale.US, "%.2f", remainingBudget)}"
                                else "Limite estourado em: R$ ${String.format(Locale.US, "%.2f", Math.abs(remainingBudget))}"
                            } else "Falta para atingir meta: R$ ${String.format(Locale.US, "%.2f", Math.max(0.0, remainingBudget))}",
                            fontSize = 11.sp,
                            color = Emerald100
                        )
                    }
                }
            }
        }

        // Tabs & New Category Button
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Tab Switcher
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(3.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    listOf("expense" to "Despesas", "income" to "Receitas").forEach { (tabKey, label) ->
                        val sel = activeTab == tabKey
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (sel) MaterialTheme.colorScheme.surface else Color.Transparent,
                            modifier = Modifier
                                .clickable { activeTab = tabKey }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = label,
                                fontSize = 12.sp,
                                fontWeight = if (sel) FontWeight.Bold else FontWeight.Medium,
                                color = if (sel) (if (tabKey == "expense") Rose600 else Emerald600) else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Button(
                    onClick = { showNewCategoryDialog = true },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Emerald600),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(3.dp))
                    Text("Nova Categoria", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Category Cards List
        items(filteredCategories, key = { it.id }) { cat ->
            val actual = transactions.filter { it.category == cat.name && it.type == activeTab }.sumOf { it.amount }
            val percent = if (cat.monthlyBudget > 0) Math.round((actual / cat.monthlyBudget) * 100).toInt() else 0
            val catColor = try { Color(android.graphics.Color.parseColor(cat.color)) } catch (e: Exception) { Emerald500 }
            val isOverBudget = activeTab == "expense" && actual > cat.monthlyBudget && cat.monthlyBudget > 0

            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 1.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(catColor.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Category, contentDescription = null, tint = catColor, modifier = Modifier.size(18.dp))
                            }

                            Column {
                                Text(
                                    text = cat.name,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "${if (activeTab == "expense") "Teto" else "Meta"}: R$ ${String.format(Locale.US, "%.2f", cat.monthlyBudget)}",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            // Quick Edit Budget Button
                            IconButton(
                                onClick = { categoryForInlineBudget = cat },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(Icons.Default.Edit, contentDescription = "Editar Teto", tint = Slate400, modifier = Modifier.size(15.dp))
                            }
                            IconButton(
                                onClick = { categoryToDelete = cat },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(Icons.Default.DeleteOutline, contentDescription = "Excluir", tint = Slate400, modifier = Modifier.size(15.dp))
                            }
                        }
                    }

                    // Progress Bar
                    LinearProgressIndicator(
                        progress = { Math.min(1f, percent / 100f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = if (isOverBudget) Rose600 else catColor,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )

                    // Footer with actual spent vs budget
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Realizado: R$ ${String.format(Locale.US, "%.2f", actual)}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isOverBudget) Rose600 else MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "$percent% ${if (isOverBudget) "(Excedido)" else ""}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isOverBudget) Rose600 else Emerald600
                        )
                    }
                }
            }
        }
    }

    // New / Edit Category Dialog
    if (showNewCategoryDialog || categoryToEdit != null) {
        CategoryDialog(
            isOpen = true,
            initialCategory = categoryToEdit,
            onDismiss = {
                showNewCategoryDialog = false
                categoryToEdit = null
            },
            onSave = { cat ->
                if (categoryToEdit != null) onUpdateCategory(cat) else onAddCategory(cat)
                showNewCategoryDialog = false
                categoryToEdit = null
            }
        )
    }

    // Inline Budget Quick Edit Dialog
    if (categoryForInlineBudget != null) {
        val cat = categoryForInlineBudget!!
        var newBudgetText by remember { mutableStateOf(String.format(Locale.US, "%.2f", cat.monthlyBudget)) }
        Dialog(onDismissRequest = { categoryForInlineBudget = null }) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth().padding(16.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Ajustar Teto de ${cat.name}", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    OutlinedTextField(
                        value = newBudgetText,
                        onValueChange = { newBudgetText = it },
                        label = { Text("Novo Valor (R$)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = { categoryForInlineBudget = null }) { Text("Cancelar") }
                        Button(
                            onClick = {
                                val valNum = newBudgetText.replace(",", ".").toDoubleOrNull() ?: cat.monthlyBudget
                                onUpdateCategoryBudget(cat.id, valNum)
                                categoryForInlineBudget = null
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Emerald600)
                        ) {
                            Text("Salvar")
                        }
                    }
                }
            }
        }
    }

    // Delete Category Confirmation
    if (categoryToDelete != null) {
        val cat = categoryToDelete!!
        ConfirmDeleteDialog(
            isOpen = true,
            title = "Excluir Categoria",
            message = "Deseja excluir a categoria '${cat.name}'?",
            onDismiss = { categoryToDelete = null },
            onConfirm = {
                onDeleteCategory(cat)
                categoryToDelete = null
            }
        )
    }
}
