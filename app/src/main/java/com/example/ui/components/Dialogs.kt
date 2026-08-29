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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.*
import com.example.ui.theme.*
import com.example.ui.util.toCurrency
import com.example.ui.util.toFormattedDate
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
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

    val isEditing = initialTransaction != null

    var type by remember(initialTransaction) { mutableStateOf(initialTransaction?.type ?: "expense") }
    var launchMode by remember(initialTransaction) {
        mutableStateOf(
            if (isEditing) "single"
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
            initialTransaction?.category ?: filteredCategories.firstOrNull()?.name ?: "Casa"
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

    // Installment options with String inputs to allow smooth editing & backspace
    var installmentMode by remember { mutableStateOf("per_installment") } // "per_installment" or "total"
    var totalInstallmentsText by remember(initialTransaction) {
        mutableStateOf(initialTransaction?.installmentTotal?.toString() ?: "10")
    }
    var startInstallmentText by remember(initialTransaction) {
        mutableStateOf(initialTransaction?.installmentCurrent?.toString() ?: "1")
    }

    // Recurring options
    var recurrenceScope by remember { mutableStateOf("end_of_year") }
    var customMonths by remember { mutableStateOf(12) }

    var errorMsg by remember { mutableStateOf<String?>(null) }
    var showDatePicker by remember { mutableStateOf(false) }

    val totalInstallments = totalInstallmentsText.toIntOrNull() ?: 2
    val startInstallment = startInstallmentText.toIntOrNull() ?: 1

    val parsedAmount = amountText.replace(",", ".").toDoubleOrNull() ?: 0.0
    val perInstallment = if (installmentMode == "per_installment") parsedAmount else (if (totalInstallments > 0) parsedAmount / totalInstallments else 0.0)
    val totalAmountCalc = if (installmentMode == "per_installment") parsedAmount * totalInstallments else parsedAmount
    val remainingInstallments = Math.max(1, totalInstallments - startInstallment + 1)

    if (showDatePicker) {
        val initialEpochMillis = remember(date) {
            try {
                LocalDate.parse(date).atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
            } catch (e: Exception) {
                System.currentTimeMillis()
            }
        }
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = initialEpochMillis)

        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val selectedLocalDate = Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate()
                            date = selectedLocalDate.format(DateTimeFormatter.ISO_DATE)
                        }
                        showDatePicker = false
                    }
                ) {
                    Text("Confirmar", fontWeight = FontWeight.Bold, color = Emerald600)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancelar")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

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
                            text = if (isEditing) "Editar Lançamento" else "Novo Lançamento",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (isEditing) "Altere os dados deste lançamento"
                            else if (launchMode == "installment") "Parcelamento automático mês a mês"
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
                                    val newCats = categories.filter { it.type == (if (t == "income") "income" else "expense") }
                                    if (category !in newCats.map { it.name }) {
                                        category = newCats.firstOrNull()?.name ?: "Casa"
                                    }
                                }
                        ) {
                            Box(
                                modifier = Modifier.padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    fontWeight = if (selected) FontWeight.ExtraBold else FontWeight.Normal,
                                    fontSize = 12.sp,
                                    color = if (selected) color else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                // Mode Tabs (Only for new transactions)
                if (!isEditing && type != "transfer") {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(
                            Triple("single", "À Vista", Icons.Default.Payments),
                            Triple("installment", "Parcelado", Icons.Default.ViewWeek),
                            Triple("recurring", "Fixo Mensal", Icons.Default.EventRepeat)
                        ).forEach { (mode, label, icon) ->
                            val selected = launchMode == mode
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { launchMode = mode }
                            ) {
                                Row(
                                    modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = null,
                                        tint = if (selected) Emerald600 else Slate500,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = label,
                                        fontSize = 11.sp,
                                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (selected) Emerald700 else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }

                // Description Field
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Descrição", fontSize = 11.sp) },
                    placeholder = { Text("Ex: Supermercado, Salário, Carro...", fontSize = 12.sp) },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().testTag("transaction_description_input")
                )

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
                                if (!isEditing && launchMode == "installment") {
                                    if (installmentMode == "per_installment") "Valor Parcela (R$)" else "Valor Total (R$)"
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
                        value = date.toFormattedDate(),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Data", fontSize = 11.sp) },
                        trailingIcon = {
                            IconButton(onClick = { showDatePicker = true }) {
                                Icon(
                                    imageVector = Icons.Default.CalendarMonth,
                                    contentDescription = "Selecionar data no calendário",
                                    tint = Emerald600
                                )
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { showDatePicker = true }
                            .testTag("transaction_date_input")
                    )
                }

                // INSTALLMENT CONFIGURATION CARD (Simple Clickable Toggles & Smooth Text Inputs)
                if (!isEditing && launchMode == "installment" && type != "transfer") {
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
                            verticalArrangement = Arrangement.spacedBy(10.dp)
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

                                // Clickable toggle between "Por Parcela" and "Total"
                                Row(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color.White)
                                        .padding(2.dp),
                                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                                ) {
                                    listOf("per_installment" to "Por Parcela", "total" to "Valor Total").forEach { (m, lbl) ->
                                        val sel = installmentMode == m
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(if (sel) Indigo600 else Color.Transparent)
                                                .clickable { installmentMode = m }
                                                .padding(horizontal = 8.dp, vertical = 4.dp)
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
                                    value = totalInstallmentsText,
                                    onValueChange = { totalInstallmentsText = it },
                                    label = { Text("Qtd Parcelas", fontSize = 10.sp) },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true,
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.weight(1f)
                                )
                                OutlinedTextField(
                                    value = startInstallmentText,
                                    onValueChange = { startInstallmentText = it },
                                    label = { Text("Parcela Atual", fontSize = 10.sp) },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true,
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            // Quick installment count chips
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                listOf(2, 3, 6, 10, 12, 24, 36, 60).forEach { n ->
                                    val sel = totalInstallmentsText == n.toString()
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(if (sel) Indigo600 else Color.White)
                                            .clickable { totalInstallmentsText = n.toString() }
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
                                text = "Resumo: $remainingInstallments parcelas de ${perInstallment.toCurrency()} (Total: ${totalAmountCalc.toCurrency()})",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Indigo700
                            )
                        }
                    }
                }

                // RECURRING CONFIGURATION CARD
                if (!isEditing && launchMode == "recurring" && type != "transfer") {
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
                                text = "Recorrência Mensal Automática",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = Emerald700
                            )

                            listOf(
                                "end_of_year" to "Até o final de 2026 (Recomendado)",
                                "full_year" to "Próximos 12 meses",
                                "custom" to "Personalizado (Meses)"
                            ).forEach { (s, lbl) ->
                                val sel = recurrenceScope == s
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { recurrenceScope = s }
                                        .padding(vertical = 2.dp)
                                ) {
                                    RadioButton(
                                        selected = sel,
                                        onClick = { recurrenceScope = s },
                                        colors = RadioButtonDefaults.colors(selectedColor = Emerald600)
                                    )
                                    Text(lbl, fontSize = 11.sp, fontWeight = if (sel) FontWeight.Bold else FontWeight.Normal)
                                }
                            }

                            if (recurrenceScope == "custom") {
                                OutlinedTextField(
                                    value = customMonths.toString(),
                                    onValueChange = { customMonths = it.toIntOrNull() ?: 12 },
                                    label = { Text("Quantidade de Meses", fontSize = 10.sp) },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true,
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }

                // Category Selector (Not needed for Transfer)
                if (type != "transfer") {
                    var showCatDropdown by remember { mutableStateOf(false) }
                    Box(modifier = Modifier.fillMaxWidth()) {
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
                                    text = {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            val c = try { Color(android.graphics.Color.parseColor(cat.color)) } catch (e: Exception) { Emerald500 }
                                            Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(c))
                                            Text(cat.name, fontSize = 13.sp)
                                        }
                                    },
                                    onClick = {
                                        category = cat.name
                                        showCatDropdown = false
                                    }
                                )
                            }
                        }
                    }
                }

                // Source Account & Target Account (For Transfer)
                var showAccDropdown by remember { mutableStateOf(false) }
                Box(modifier = Modifier.fillMaxWidth()) {
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
                                    text = { Text("💳 ${card.name} (Fatura: ${card.currentInvoice.toCurrency()})") },
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
                                text = { Text("🏛️ ${acc.name} (Saldo: ${acc.balance.toCurrency()})") },
                                onClick = {
                                    account = acc.name
                                    showAccDropdown = false
                                }
                            )
                        }
                    }
                }

                // Target Account (Only for Transfer)
                if (type == "transfer") {
                    var showTargetAccDropdown by remember { mutableStateOf(false) }
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = targetAccount,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Conta de Destino", fontSize = 11.sp) },
                            trailingIcon = {
                                IconButton(onClick = { showTargetAccDropdown = true }) {
                                    Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                                }
                            },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showTargetAccDropdown = true }
                        )
                        DropdownMenu(
                            expanded = showTargetAccDropdown,
                            onDismissRequest = { showTargetAccDropdown = false }
                        ) {
                            accounts.filter { it.name != account }.forEach { acc ->
                                DropdownMenuItem(
                                    text = { Text("🏛️ ${acc.name} (Saldo: ${acc.balance.toCurrency()})") },
                                    onClick = {
                                        targetAccount = acc.name
                                        showTargetAccDropdown = false
                                    }
                                )
                            }
                        }
                    }
                }

                // Status (Simple, well-proportioned clickable toggle button!) and Notes
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Clickable Status Toggle
                    val isDone = status == "completed"
                    Surface(
                        onClick = { status = if (isDone) "pending" else "completed" },
                        shape = RoundedCornerShape(12.dp),
                        color = if (isDone) Emerald50 else Amber500.copy(alpha = 0.15f),
                        border = CardDefaults.outlinedCardBorder().copy(
                            brush = Brush.linearGradient(
                                listOf(
                                    if (isDone) Emerald500.copy(alpha = 0.5f) else Amber500.copy(alpha = 0.5f),
                                    if (isDone) Emerald500.copy(alpha = 0.5f) else Amber500.copy(alpha = 0.5f)
                                )
                            )
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp)
                            .testTag("status_clickable_toggle")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = if (isDone) Icons.Default.CheckCircle else Icons.Default.Schedule,
                                contentDescription = null,
                                tint = if (isDone) Emerald700 else Amber700,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Column {
                                Text(
                                    text = "Status (Clique)",
                                    fontSize = 9.sp,
                                    color = if (isDone) Emerald700.copy(alpha = 0.8f) else Amber700.copy(alpha = 0.8f)
                                )
                                Text(
                                    text = if (isDone) "Concluído" else "Pendente",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isDone) Emerald700 else Amber700
                                )
                            }
                        }
                    }

                    // Notes
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Observações", fontSize = 11.sp) },
                        placeholder = { Text("Opcional", fontSize = 11.sp) },
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

                            // If editing an existing transaction: ALWAYS update the single transaction directly!
                            if (isEditing) {
                                val tx = TransactionEntity(
                                    id = initialTransaction!!.id,
                                    date = date,
                                    description = description.trim(),
                                    amount = parsedAmount,
                                    type = type,
                                    category = if (type == "transfer") "Transferência" else category,
                                    account = account,
                                    targetAccount = if (type == "transfer") targetAccount else null,
                                    status = status,
                                    notes = notes.trim().ifEmpty { null },
                                    isInstallment = initialTransaction.isInstallment,
                                    installmentGroupId = initialTransaction.installmentGroupId,
                                    installmentCurrent = initialTransaction.installmentCurrent,
                                    installmentTotal = initialTransaction.installmentTotal,
                                    isRecurring = initialTransaction.isRecurring,
                                    recurrenceGroupId = initialTransaction.recurrenceGroupId
                                )
                                onSaveSingle(tx)
                            } else if (launchMode == "installment" && type != "transfer") {
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
                            } else if (launchMode == "recurring" && type != "transfer") {
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
                                    id = UUID.randomUUID().toString(),
                                    date = date,
                                    description = description.trim(),
                                    amount = parsedAmount,
                                    type = type,
                                    category = if (type == "transfer") "Transferência" else category,
                                    account = account,
                                    targetAccount = if (type == "transfer") targetAccount else null,
                                    status = status,
                                    notes = notes.trim().ifEmpty { null },
                                    isInstallment = false,
                                    installmentGroupId = null,
                                    installmentCurrent = null,
                                    installmentTotal = null,
                                    isRecurring = false,
                                    recurrenceGroupId = null
                                )
                                onSaveSingle(tx)
                            }
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Emerald600),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("save_transaction_button")
                    ) {
                        Text(if (isEditing) "Salvar Alterações" else "Criar Lançamento", fontWeight = FontWeight.Bold)
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
