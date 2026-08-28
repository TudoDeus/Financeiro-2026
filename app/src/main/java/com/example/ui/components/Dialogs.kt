package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
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
import com.example.data.model.*
import com.example.ui.theme.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

@Composable
fun TransactionDialog(
    isOpen: Boolean,
    initialTransaction: TransactionEntity? = null,
    categories: List<CategoryEntity>,
    accounts: List<AccountEntity>,
    creditCards: List<CreditCardEntity>,
    onDismiss: () -> Unit,
    onSaveSingle: (TransactionEntity) -> Unit,
    onSaveInstallment: (
        desc: String,
        amountPerInstallment: Double,
        totalInstallments: Int,
        startInstallment: Int,
        type: String,
        category: String,
        account: String,
        startDate: String,
        status: String,
        notes: String?
    ) -> Unit,
    onSaveRecurring: (
        desc: String,
        amount: Double,
        type: String,
        category: String,
        account: String,
        targetAccount: String?,
        startDate: String,
        status: String,
        notes: String?,
        scope: String,
        customMonths: Int
    ) -> Unit
) {
    if (!isOpen) return

    var type by remember(initialTransaction) { mutableStateOf(initialTransaction?.type ?: "expense") }
    var launchMode by remember(initialTransaction) {
        mutableStateOf(
            if (initialTransaction?.isInstallment == true) "installment"
            else if (initialTransaction?.isRecurring == true) "recurring"
            else "single"
        )
    }

    var description by remember(initialTransaction) { mutableStateOf(initialTransaction?.description ?: "") }
    var amountText by remember(initialTransaction) {
        mutableStateOf(initialTransaction?.amount?.let { String.format(Locale.US, "%.2f", it) } ?: "")
    }
    var date by remember(initialTransaction) {
        mutableStateOf(initialTransaction?.date ?: LocalDate.now().format(DateTimeFormatter.ISO_DATE))
    }

    val filteredCategories = categories.filter { it.type == (if (type == "income") "income" else "expense") }
    var category by remember(initialTransaction, type) {
        mutableStateOf(
            initialTransaction?.category ?: filteredCategories.firstOrNull()?.name ?: "Geral"
        )
    }

    var account by remember(initialTransaction) {
        mutableStateOf(
            initialTransaction?.account ?: creditCards.firstOrNull()?.name ?: accounts.firstOrNull()?.name ?: "Nubank Conta"
        )
    }
    var targetAccount by remember(initialTransaction) {
        mutableStateOf(initialTransaction?.targetAccount ?: accounts.getOrNull(1)?.name ?: accounts.firstOrNull()?.name ?: "")
    }
    var status by remember(initialTransaction) { mutableStateOf(initialTransaction?.status ?: "completed") }
    var notes by remember(initialTransaction) { mutableStateOf(initialTransaction?.notes ?: "") }

    // Installment options
    var installmentMode by remember { mutableStateOf("per_installment") } // "per_installment" or "total"
    var totalInstallments by remember(initialTransaction) { mutableStateOf(initialTransaction?.installmentTotal ?: 10) }
    var startInstallment by remember(initialTransaction) { mutableStateOf(initialTransaction?.installmentCurrent ?: 1) }

    // Recurring options
    var recurrenceScope by remember { mutableStateOf("end_of_year") } // "end_of_year", "full_year", "custom"
    var customMonths by remember { mutableStateOf(12) }

    var errorMsg by remember { mutableStateOf<String?>(null) }

    val parsedAmount = amountText.replace(",", ".").toDoubleOrNull() ?: 0.0
    val perInstallment = if (installmentMode == "per_installment") parsedAmount else (if (totalInstallments > 0) parsedAmount / totalInstallments else 0.0)
    val totalAmountCalc = if (installmentMode == "per_installment") parsedAmount * totalInstallments else parsedAmount
    val remainingInstallments = Math.max(1, totalInstallments - startInstallment + 1)

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
                .testTag("transaction_dialog_surface")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Title and Close
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (initialTransaction != null) "Editar Lançamento" else "Novo Lançamento",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (launchMode == "installment") "Parcelamento automático mês a mês"
                            else if (launchMode == "recurring") "Lançamento fixo e recorrente"
                            else "Lançamento à vista pontual",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Fechar", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                if (errorMsg != null) {
                    Surface(
                        color = Rose50,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = errorMsg!!,
                            color = Rose700,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }

                // Type selector: Despesa, Receita, Transferência
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    listOf(
                        Triple("expense", "Despesa", Rose600),
                        Triple("income", "Receita", Emerald600),
                        Triple("transfer", "Transf.", Indigo600)
                    ).forEach { (t, label, color) ->
                        val selected = type == t
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (selected) MaterialTheme.colorScheme.surface else Color.Transparent,
                            shadowElevation = if (selected) 2.dp else 0.dp,
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    type = t
                                    val cat = categories.firstOrNull { it.type == (if (t == "income") "income" else "expense") }
                                    if (cat != null) category = cat.name
                                }
                                .testTag("type_${t}_button")
                        ) {
                            Box(
                                modifier = Modifier.padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    fontSize = 12.sp,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (selected) color else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                // Launch Format Selector (À Vista, Parcelado, Fixo)
                if (initialTransaction == null && type != "transfer") {
                    Text(
                        text = "FORMATO DO LANÇAMENTO",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(
                            Triple("single", "À Vista", Icons.Default.AttachMoney),
                            Triple("installment", "Parcelado", Icons.Default.Layers),
                            Triple("recurring", "Fixo Mensal", Icons.Default.Repeat)
                        ).forEach { (m, label, icon) ->
                            val selected = launchMode == m
                            OutlinedCard(
                                onClick = { launchMode = m },
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.outlinedCardColors(
                                    containerColor = if (selected) {
                                        if (m == "installment") Indigo500.copy(alpha = 0.12f)
                                        else if (m == "recurring") Emerald500.copy(alpha = 0.12f)
                                        else MaterialTheme.colorScheme.primaryContainer
                                    } else Color.Transparent
                                ),
                                border = CardDefaults.outlinedCardBorder().copy(
                                    brush = Brush.linearGradient(
                                        listOf(
                                            if (selected) {
                                                if (m == "installment") Indigo600
                                                else if (m == "recurring") Emerald600
                                                else MaterialTheme.colorScheme.primary
                                            } else MaterialTheme.colorScheme.outline,
                                            if (selected) {
                                                if (m == "installment") Indigo600
                                                else if (m == "recurring") Emerald600
                                                else MaterialTheme.colorScheme.primary
                                            } else MaterialTheme.colorScheme.outline
                                        )
                                    )
                                ),
                                modifier = Modifier.weight(1f).testTag("mode_${m}_button")
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 10.dp, horizontal = 4.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = null,
                                        tint = if (selected) {
                                            if (m == "installment") Indigo600
                                            else if (m == "recurring") Emerald600
                                            else MaterialTheme.colorScheme.primary
                                        } else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(
                                        text = label,
                                        fontSize = 11.sp,
                                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }

                // Amount & Date
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = amountText,
                        onValueChange = { amountText = it },
                        label = {
                            Text(
                                if (launchMode == "installment") {
                                    if (installmentMode == "per_installment") "Valor da Parcela (R$)" else "Valor Total (R$)"
                                } else "Valor (R$)",
                                fontSize = 11.sp
                            )
                        },
                        prefix = { Text("R$ ", fontWeight = FontWeight.Bold, fontSize = 13.sp) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1.1f).testTag("transaction_amount_input")
                    )

                    OutlinedTextField(
                        value = date,
                        onValueChange = { date = it },
                        label = { Text("Data (AAAA-MM-DD)", fontSize = 11.sp) },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f).testTag("transaction_date_input")
                    )
                }

                // INSTALLMENT CONFIGURATION CARD
                if (launchMode == "installment" && type != "transfer") {
                    Surface(
                        color = Indigo500.copy(alpha = 0.08f),
                        shape = RoundedCornerShape(14.dp),
                        border = CardDefaults.outlinedCardBorder().copy(
                            brush = Brush.linearGradient(listOf(Indigo500.copy(alpha = 0.4f), Indigo500.copy(alpha = 0.4f)))
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Configuração do Parcelamento",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = Indigo700
                                )
                                Row(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(Color.White)
                                        .padding(2.dp)
                                ) {
                                    listOf("per_installment" to "Por Parcela", "total" to "Total").forEach { (m, lbl) ->
                                        val sel = installmentMode == m
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(if (sel) Indigo600 else Color.Transparent)
                                                .clickable { installmentMode = m }
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = lbl,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (sel) Color.White else Slate600
                                            )
                                        }
                                    }
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedTextField(
                                    value = totalInstallments.toString(),
                                    onValueChange = { totalInstallments = it.toIntOrNull() ?: 2 },
                                    label = { Text("Qtd Parcelas", fontSize = 10.sp) },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true,
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.weight(1f)
                                )
                                OutlinedTextField(
                                    value = startInstallment.toString(),
                                    onValueChange = { startInstallment = it.toIntOrNull() ?: 1 },
                                    label = { Text("Parcela Atual", fontSize = 10.sp) },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true,
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            // Quick chips
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                listOf(2, 3, 6, 10, 12, 24, 36, 60).forEach { n ->
                                    val sel = totalInstallments == n
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(if (sel) Indigo600 else Color.White)
                                            .clickable { totalInstallments = n }
                                            .padding(horizontal = 6.dp, vertical = 3.dp)
                                    ) {
                                        Text(
                                            text = "${n}x",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (sel) Color.White else Indigo600
                                        )
                                    }
                                }
                            }

                            Text(
                                text = "Resumo: $remainingInstallments parcelas de R$ ${String.format(Locale.US, "%.2f", perInstallment)} (Total: R$ ${String.format(Locale.US, "%.2f", totalAmountCalc)})",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Indigo700
                            )
                        }
                    }
                }

                // RECURRING CONFIGURATION CARD
                if (launchMode == "recurring" && type != "transfer") {
                    Surface(
                        color = Emerald500.copy(alpha = 0.08f),
                        shape = RoundedCornerShape(14.dp),
                        border = CardDefaults.outlinedCardBorder().copy(
                            brush = Brush.linearGradient(listOf(Emerald500.copy(alpha = 0.4f), Emerald500.copy(alpha = 0.4f)))
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Frequência de Replicação Fixo",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = Emerald700
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                listOf(
                                    "end_of_year" to "Até Fim do Ano",
                                    "full_year" to "Ano Todo (12m)",
                                    "custom" to "Personalizado"
                                ).forEach { (sc, lbl) ->
                                    val sel = recurrenceScope == sc
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (sel) Emerald600 else Color.White)
                                            .clickable { recurrenceScope = sc }
                                            .padding(vertical = 6.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = lbl,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (sel) Color.White else Slate700
                                        )
                                    }
                                }
                            }
                            Text(
                                text = "Será criado um lançamento fixo de R$ ${String.format(Locale.US, "%.2f", parsedAmount)} nos meses selecionados.",
                                fontSize = 11.sp,
                                color = Emerald700
                            )
                        }
                    }
                }

                // Description
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Descrição", fontSize = 11.sp) },
                    placeholder = { Text("Ex: Supermercado, Salário, TV Samsung...", fontSize = 12.sp) },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().testTag("transaction_description_input")
                )

                // Category (if not transfer)
                if (type != "transfer") {
                    var showCatDropdown by remember { mutableStateOf(false) }
                    Box {
                        OutlinedTextField(
                            value = category,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Categoria", fontSize = 11.sp) },
                            trailingIcon = {
                                IconButton(onClick = { showCatDropdown = true }) {
                                    Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                                }
                            },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showCatDropdown = true }
                                .testTag("transaction_category_field")
                        )
                        DropdownMenu(
                            expanded = showCatDropdown,
                            onDismissRequest = { showCatDropdown = false }
                        ) {
                            filteredCategories.forEach { cat ->
                                DropdownMenuItem(
                                    text = { Text(cat.name) },
                                    onClick = {
                                        category = cat.name
                                        showCatDropdown = false
                                    }
                                )
                            }
                        }
                    }
                }

                // Account / Credit Card Selection
                var showAccDropdown by remember { mutableStateOf(false) }
                Box {
                    OutlinedTextField(
                        value = account,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(if (type == "transfer") "Conta de Origem" else "Conta / Cartão de Crédito", fontSize = 11.sp) },
                        trailingIcon = {
                            IconButton(onClick = { showAccDropdown = true }) {
                                Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showAccDropdown = true }
                            .testTag("transaction_account_field")
                    )
                    DropdownMenu(
                        expanded = showAccDropdown,
                        onDismissRequest = { showAccDropdown = false }
                    ) {
                        if (type != "transfer" && creditCards.isNotEmpty()) {
                            DropdownMenuItem(
                                text = { Text("CARTÕES DE CRÉDITO", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Slate400) },
                                onClick = {}
                            )
                            creditCards.forEach { card ->
                                DropdownMenuItem(
                                    text = { Text("💳 ${card.name} (Fatura: R$ ${String.format(Locale.US, "%.2f", card.currentInvoice)})") },
                                    onClick = {
                                        account = card.name
                                        showAccDropdown = false
                                    }
                                )
                            }
                        }
                        DropdownMenuItem(
                            text = { Text("CONTAS BANCÁRIAS", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Slate400) },
                            onClick = {}
                        )
                        accounts.forEach { acc ->
                            DropdownMenuItem(
                                text = { Text("🏛️ ${acc.name} (Saldo: R$ ${String.format(Locale.US, "%.2f", acc.balance)})") },
                                onClick = {
                                    account = acc.name
                                    showAccDropdown = false
                                }
                            )
                        }
                    }
                }

                // Status & Notes
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    var showStatusDropdown by remember { mutableStateOf(false) }
                    Box(modifier = Modifier.weight(1f)) {
                        OutlinedTextField(
                            value = if (status == "completed") "Concluído / Pago" else "Pendente / Agendado",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Status", fontSize = 11.sp) },
                            trailingIcon = {
                                IconButton(onClick = { showStatusDropdown = true }) {
                                    Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                                }
                            },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                        DropdownMenu(
                            expanded = showStatusDropdown,
                            onDismissRequest = { showStatusDropdown = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Concluído / Pago") },
                                onClick = {
                                    status = "completed"
                                    showStatusDropdown = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Pendente / Agendado") },
                                onClick = {
                                    status = "pending"
                                    showStatusDropdown = false
                                }
                            )
                        }
                    }

                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Observações", fontSize = 11.sp) },
                        placeholder = { Text("Opcional", fontSize = 12.sp) },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1.2f)
                    )
                }

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Cancelar", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (description.isBlank()) {
                                errorMsg = "Por favor informe a descrição."
                                return@Button
                            }
                            if (parsedAmount <= 0) {
                                errorMsg = "Informe um valor válido maior que zero."
                                return@Button
                            }

                            if (launchMode == "installment" && type != "transfer") {
                                onSaveInstallment(
                                    description,
                                    perInstallment,
                                    totalInstallments,
                                    startInstallment,
                                    type,
                                    category,
                                    account,
                                    date,
                                    status,
                                    notes
                                )
                            } else if (launchMode == "recurring") {
                                onSaveRecurring(
                                    description,
                                    parsedAmount,
                                    type,
                                    category,
                                    account,
                                    if (type == "transfer") targetAccount else null,
                                    date,
                                    status,
                                    notes,
                                    recurrenceScope,
                                    customMonths
                                )
                            } else {
                                val tx = TransactionEntity(
                                    id = initialTransaction?.id ?: UUID.randomUUID().toString(),
                                    date = date,
                                    description = description.trim(),
                                    amount = parsedAmount,
                                    type = type,
                                    category = if (type == "transfer") "Transferência" else category,
                                    account = account,
                                    targetAccount = if (type == "transfer") targetAccount else null,
                                    status = status,
                                    notes = notes.trim().ifEmpty { null },
                                    isInstallment = initialTransaction?.isInstallment ?: false,
                                    installmentGroupId = initialTransaction?.installmentGroupId,
                                    installmentCurrent = initialTransaction?.installmentCurrent,
                                    installmentTotal = initialTransaction?.installmentTotal,
                                    isRecurring = initialTransaction?.isRecurring ?: false,
                                    recurrenceGroupId = initialTransaction?.recurrenceGroupId
                                )
                                onSaveSingle(tx)
                            }
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Emerald600),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("save_transaction_button")
                    ) {
                        Text(if (initialTransaction != null) "Salvar Alterações" else "Criar Lançamento", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun CategoryDialog(
    isOpen: Boolean,
    initialCategory: CategoryEntity? = null,
    onDismiss: () -> Unit,
    onSave: (CategoryEntity) -> Unit
) {
    if (!isOpen) return

    var name by remember(initialCategory) { mutableStateOf(initialCategory?.name ?: "") }
    var type by remember(initialCategory) { mutableStateOf(initialCategory?.type ?: "expense") }
    var budgetText by remember(initialCategory) {
        mutableStateOf(initialCategory?.monthlyBudget?.let { String.format(Locale.US, "%.2f", it) } ?: "500.00")
    }
    var selectedColor by remember(initialCategory) { mutableStateOf(initialCategory?.color ?: "#10B981") }

    val colorsList = listOf("#10B981", "#3B82F6", "#F59E0B", "#EF4444", "#8B5CF6", "#EC4899", "#06B6D4", "#64748B")

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth().padding(16.dp).testTag("category_dialog")
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = if (initialCategory != null) "Editar Categoria" else "Nova Categoria",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )

                // Type selector
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("expense" to "Despesa", "income" to "Receita").forEach { (t, lbl) ->
                        val sel = type == t
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (sel) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.weight(1f).clickable { type = t }
                        ) {
                            Box(modifier = Modifier.padding(vertical = 8.dp), contentAlignment = Alignment.Center) {
                                Text(lbl, fontWeight = if (sel) FontWeight.Bold else FontWeight.Normal, fontSize = 12.sp)
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nome da Categoria") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = budgetText,
                    onValueChange = { budgetText = it },
                    label = { Text(if (type == "income") "Meta Mensal (R$)" else "Teto Orçamentário (R$)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                // Color picker
                Text("Cor de Destaque", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    colorsList.forEach { hex ->
                        val color = try { Color(android.graphics.Color.parseColor(hex)) } catch (e: Exception) { Emerald500 }
                        val sel = selectedColor == hex
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(color)
                                .clickable { selectedColor = hex }
                                .border(
                                    width = if (sel) 3.dp else 0.dp,
                                    color = if (sel) MaterialTheme.colorScheme.onSurface else Color.Transparent,
                                    shape = CircleShape
                                )
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) { Text("Cancelar") }
                    Button(
                        onClick = {
                            if (name.isBlank()) return@Button
                            val budget = budgetText.replace(",", ".").toDoubleOrNull() ?: 0.0
                            val cat = CategoryEntity(
                                id = initialCategory?.id ?: "cat-${System.currentTimeMillis()}",
                                name = name.trim(),
                                type = type,
                                icon = "Tag",
                                color = selectedColor,
                                monthlyBudget = budget
                            )
                            onSave(cat)
                            onDismiss()
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

@Composable
fun GoogleSyncDialog(
    isOpen: Boolean,
    sheetId: String,
    isConnected: Boolean,
    onDismiss: () -> Unit,
    onSave: (String, Boolean) -> Unit
) {
    if (!isOpen) return

    var currentId by remember(sheetId) { mutableStateOf(sheetId) }
    var connected by remember(isConnected) { mutableStateOf(isConnected) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth().padding(16.dp).testTag("google_sync_dialog")
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.CloudDone, contentDescription = null, tint = Emerald600)
                    Text("Sincronização Google Sheets", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }

                Text(
                    text = "Mantenha seus lançamentos e faturas sincronizados em tempo real com a sua planilha do Google Sheets de 2026.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedTextField(
                    value = currentId,
                    onValueChange = { currentId = it },
                    label = { Text("ID da Planilha Google (Spreadsheet ID)") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Sincronização Automática", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    Switch(
                        checked = connected,
                        onCheckedChange = { connected = it }
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) { Text("Fechar") }
                    Button(
                        onClick = {
                            onSave(currentId.trim(), connected)
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Emerald600)
                    ) {
                        Text("Salvar Conexão")
                    }
                }
            }
        }
    }
}

@Composable
fun ConfirmDeleteDialog(
    isOpen: Boolean,
    title: String,
    message: String,
    showDeleteGroupOption: Boolean = false,
    groupOptionLabel: String = "Excluir todas as parcelas / recorrências?",
    onDismiss: () -> Unit,
    onConfirm: (deleteAllInGroup: Boolean) -> Unit
) {
    if (!isOpen) return

    var deleteGroup by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(message, fontSize = 13.sp)
                if (showDeleteGroupOption) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.clickable { deleteGroup = !deleteGroup }.padding(top = 8.dp)
                    ) {
                        Checkbox(checked = deleteGroup, onCheckedChange = { deleteGroup = it })
                        Text(groupOptionLabel, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(deleteGroup)
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = Rose600)
            ) {
                Text("Excluir", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        },
        shape = RoundedCornerShape(16.dp)
    )
}
