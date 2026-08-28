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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.GoalEntity
import com.example.ui.components.ConfirmDeleteDialog
import com.example.ui.theme.*
import java.util.Locale

@Composable
fun GoalsScreen(
    goals: List<GoalEntity>,
    onAddGoal: (GoalEntity) -> Unit,
    onDeleteGoal: (GoalEntity) -> Unit,
    onAddAporte: (String, Double) -> Unit
) {
    var showNewGoalDialog by remember { mutableStateOf(false) }
    var goalToDelete by remember { mutableStateOf<GoalEntity?>(null) }
    var activeAporteGoalId by remember { mutableStateOf<String?>(null) }
    var aporteInputText by remember { mutableStateOf("") }

    val totalSaved = goals.sumOf { it.currentAmount }
    val totalTarget = goals.sumOf { it.targetAmount }
    val overallProgress = if (totalTarget > 0) Math.min(100, Math.round((totalSaved / totalTarget) * 100).toInt()) else 0

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("goals_screen_root"),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(top = 10.dp, bottom = 100.dp)
    ) {
        // Top Banner
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
                                    Color(0xFF064E3B),
                                    Color(0xFF0F766E),
                                    Color(0xFF0F172A)
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
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Emerald400, modifier = Modifier.size(18.dp))
                                    Text("Metas & Planejamento para 2026", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color.White)
                                }
                                Text("Acompanhe suas reservas e sonhos de médio/longo prazo", fontSize = 11.sp, color = Emerald100)
                            }

                            Button(
                                onClick = { showNewGoalDialog = true },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Emerald500),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(3.dp))
                                Text("Nova Meta", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        // Progress Bar & Total Info
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Progresso Geral ($overallProgress%)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Emerald300)
                                Text(
                                    "R$ ${String.format(Locale.US, "%,.0f", totalSaved)} de R$ ${String.format(Locale.US, "%,.0f", totalTarget)}",
                                    fontSize = 11.sp,
                                    color = Color.White
                                )
                            }
                            LinearProgressIndicator(
                                progress = { overallProgress / 100f },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp)),
                                color = Emerald400,
                                trackColor = Color.Black.copy(alpha = 0.4f)
                            )
                        }
                    }
                }
            }
        }

        // Goals List
        items(goals, key = { it.id }) { g ->
            val percent = if (g.targetAmount > 0) Math.min(100, Math.round((g.currentAmount / g.targetAmount) * 100).toInt()) else 0
            val remaining = Math.max(0.0, g.targetAmount - g.currentAmount)
            val isFinished = g.currentAmount >= g.targetAmount
            val isAporting = activeAporteGoalId == g.id

            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 1.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = Emerald50,
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = g.category.uppercase(),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = Emerald700,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }

                        IconButton(onClick = { goalToDelete = g }, modifier = Modifier.size(24.dp)) {
                            Icon(Icons.Default.DeleteOutline, contentDescription = "Excluir", tint = Slate400, modifier = Modifier.size(16.dp))
                        }
                    }

                    Column {
                        Text(g.title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        if (!g.notes.isNullOrBlank()) {
                            Text(g.notes, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    // Progress
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Progresso", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                "$percent%",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isFinished) Emerald600 else MaterialTheme.colorScheme.onSurface
                            )
                        }
                        LinearProgressIndicator(
                            progress = { percent / 100f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = if (isFinished) Emerald500 else Emerald600,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    }

                    // Amounts
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Acumulado", fontSize = 10.sp, color = Slate400)
                            Text(
                                "R$ ${String.format(Locale.US, "%.2f", g.currentAmount)}",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 13.sp,
                                color = Emerald600
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Objetivo", fontSize = 10.sp, color = Slate400)
                            Text(
                                "R$ ${String.format(Locale.US, "%.2f", g.targetAmount)}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }

                    Divider(color = MaterialTheme.colorScheme.surfaceVariant)

                    // Footer / Aporte Action
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Prazo: ${g.deadline.split("-").reversed().joinToString("/")}",
                            fontSize = 10.sp,
                            color = Slate400
                        )
                        Text(
                            text = "Falta: R$ ${String.format(Locale.US, "%.2f", remaining)}",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    if (isAporting) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = aporteInputText,
                                onValueChange = { aporteInputText = it },
                                placeholder = { Text("Valor R$", fontSize = 11.sp) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                singleLine = true,
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f)
                            )
                            Button(
                                onClick = {
                                    val amt = aporteInputText.replace(",", ".").toDoubleOrNull() ?: 0.0
                                    if (amt > 0) {
                                        onAddAporte(g.id, amt)
                                    }
                                    activeAporteGoalId = null
                                    aporteInputText = ""
                                },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Emerald600)
                            ) {
                                Icon(Icons.Default.Check, contentDescription = "Salvar", modifier = Modifier.size(16.dp))
                            }
                        }
                    } else {
                        OutlinedButton(
                            onClick = {
                                activeAporteGoalId = g.id
                                aporteInputText = ""
                            },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth(),
                            contentPadding = PaddingValues(vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp), tint = Emerald600)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Adicionar Aporte / Depósito", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Emerald600)
                        }
                    }
                }
            }
        }
    }

    // New Goal Dialog
    if (showNewGoalDialog) {
        var title by remember { mutableStateOf("") }
        var targetText by remember { mutableStateOf("") }
        var currentText by remember { mutableStateOf("0.00") }
        var deadline by remember { mutableStateOf("2026-12-31") }
        var category by remember { mutableStateOf("Segurança") }
        var notes by remember { mutableStateOf("") }

        Dialog(onDismissRequest = { showNewGoalDialog = false }) {
            Surface(shape = RoundedCornerShape(18.dp), color = MaterialTheme.colorScheme.surface, modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Nova Meta Financeira 2026", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Título da Meta (ex: Troca de Carro)") }, singleLine = true, shape = RoundedCornerShape(10.dp), modifier = Modifier.fillMaxWidth())

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(value = targetText, onValueChange = { targetText = it }, label = { Text("Valor Alvo (R$)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), singleLine = true, shape = RoundedCornerShape(10.dp), modifier = Modifier.weight(1f))
                        OutlinedTextField(value = currentText, onValueChange = { currentText = it }, label = { Text("Já Poupançado (R$)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), singleLine = true, shape = RoundedCornerShape(10.dp), modifier = Modifier.weight(1f))
                    }

                    OutlinedTextField(value = deadline, onValueChange = { deadline = it }, label = { Text("Data Prazo (AAAA-MM-DD)") }, singleLine = true, shape = RoundedCornerShape(10.dp), modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = notes, onValueChange = { notes = it }, label = { Text("Detalhes / Alocação (Opcional)") }, singleLine = true, shape = RoundedCornerShape(10.dp), modifier = Modifier.fillMaxWidth())

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = { showNewGoalDialog = false }) { Text("Cancelar") }
                        Button(
                            onClick = {
                                if (title.isNotBlank()) {
                                    val tgt = targetText.replace(",", ".").toDoubleOrNull() ?: 1000.0
                                    val curr = currentText.replace(",", ".").toDoubleOrNull() ?: 0.0
                                    onAddGoal(
                                        GoalEntity(
                                            id = "goal-${System.currentTimeMillis()}",
                                            title = title.trim(),
                                            targetAmount = tgt,
                                            currentAmount = curr,
                                            deadline = deadline,
                                            category = category,
                                            notes = notes.trim().ifEmpty { null }
                                        )
                                    )
                                    showNewGoalDialog = false
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Emerald600)
                        ) {
                            Text("Criar Meta")
                        }
                    }
                }
            }
        }
    }

    // Delete Goal Confirmation
    if (goalToDelete != null) {
        val g = goalToDelete!!
        ConfirmDeleteDialog(
            isOpen = true,
            title = "Excluir Meta",
            message = "Deseja excluir a meta '${g.title}'?",
            onDismiss = { goalToDelete = null },
            onConfirm = {
                onDeleteGoal(g)
                goalToDelete = null
            }
        )
    }
}
