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
import com.example.data.local.InitialData
import com.example.data.model.AccountEntity
import com.example.data.model.CreditCardEntity
import com.example.data.model.TransactionEntity
import com.example.ui.components.ConfirmDeleteDialog
import com.example.ui.theme.*
import java.util.Locale

@Composable
fun AccountsCardsScreen(
    selectedMonth: Int,
    selectedYear: Int,
    accounts: List<AccountEntity>,
    creditCards: List<CreditCardEntity>,
    transactions: List<TransactionEntity>,
    allTransactions: List<TransactionEntity>,
    onAddAccount: (AccountEntity) -> Unit,
    onUpdateAccount: (AccountEntity) -> Unit,
    onDeleteAccount: (AccountEntity) -> Unit,
    onUpdateAccountBalance: (String, Double) -> Unit,
    onAddCreditCard: (CreditCardEntity) -> Unit,
    onUpdateCreditCard: (CreditCardEntity) -> Unit,
    onDeleteCreditCard: (CreditCardEntity) -> Unit,
    onPayCardInvoice: (CreditCardEntity, String, Double) -> Unit
) {
    var showNewAccountDialog by remember { mutableStateOf(false) }
    var accountToEdit by remember { mutableStateOf<AccountEntity?>(null) }
    var accountToDelete by remember { mutableStateOf<AccountEntity?>(null) }
    var accountForBalanceAdjust by remember { mutableStateOf<AccountEntity?>(null) }

    var showNewCardDialog by remember { mutableStateOf(false) }
    var cardToEdit by remember { mutableStateOf<CreditCardEntity?>(null) }
    var cardToDelete by remember { mutableStateOf<CreditCardEntity?>(null) }
    var cardForInvoiceDetails by remember { mutableStateOf<CreditCardEntity?>(null) }
    var cardForPayment by remember { mutableStateOf<CreditCardEntity?>(null) }

    val totalBalance = accounts.sumOf { it.balance }
    val totalInvoice = creditCards.sumOf { it.currentInvoice }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("accounts_cards_screen_root"),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 10.dp, bottom = 100.dp)
    ) {
        // Summary Header Card
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shadowElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Saldo Total em Contas", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
                            text = "R$ ${String.format(Locale.US, "%,.2f", totalBalance).replace(",", "X").replace(".", ",").replace("X", ".")}",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Emerald600
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text("Faturas em Aberto", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
                            text = "R$ ${String.format(Locale.US, "%,.2f", totalInvoice).replace(",", "X").replace(".", ",").replace("X", ".")}",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Rose600
                        )
                    }
                }
            }
        }

        // Section 1: Contas Bancárias
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(Icons.Default.AccountBalance, contentDescription = null, tint = Emerald600, modifier = Modifier.size(20.dp))
                    Text("Contas Bancárias (${accounts.size})", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
                Button(
                    onClick = { showNewAccountDialog = true },
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Emerald600),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(3.dp))
                    Text("Nova Conta", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        items(accounts, key = { it.id }) { acc ->
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 1.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(Indigo500.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.AccountBalance, contentDescription = null, tint = Indigo600, modifier = Modifier.size(20.dp))
                        }
                        Column {
                            Text(acc.name, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text(acc.institution, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "R$ ${String.format(Locale.US, "%.2f", acc.balance)}",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 14.sp,
                                color = if (acc.balance >= 0) MaterialTheme.colorScheme.onSurface else Rose600
                            )
                            Text(
                                text = "Ajustar Saldo",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Emerald600,
                                modifier = Modifier.clickable { accountForBalanceAdjust = acc }
                            )
                        }

                        IconButton(onClick = { accountToDelete = acc }, modifier = Modifier.size(28.dp)) {
                            Icon(Icons.Default.DeleteOutline, contentDescription = "Excluir", tint = Slate400, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }

        // Section 2: Cartões de Crédito
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 10.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(Icons.Default.CreditCard, contentDescription = null, tint = Indigo600, modifier = Modifier.size(20.dp))
                    Text("Cartões de Crédito (${creditCards.size})", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
                Button(
                    onClick = { showNewCardDialog = true },
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Indigo600),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(3.dp))
                    Text("Novo Cartão", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        items(creditCards, key = { it.id }) { card ->
            val available = Math.max(0.0, card.limit - card.currentInvoice)
            val usedPct = if (card.limit > 0) Math.min(100, Math.round((card.currentInvoice / card.limit) * 100).toInt()) else 0

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
                                    Color(0xFF1E293B),
                                    Color(0xFF334155),
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
                                Text(card.name, fontWeight = FontWeight.ExtraBold, fontSize = 15.sp, color = Color.White)
                                Text(card.institution, fontSize = 11.sp, color = Slate300)
                            }
                            Surface(
                                color = Color.White.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = "Fecha dia ${card.closingDay} • Vence dia ${card.dueDay}",
                                    fontSize = 10.sp,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        // Invoice & Limit Info
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Fatura Atual (${InitialData.MONTH_NAMES[selectedMonth - 1]})", fontSize = 10.sp, color = Slate300)
                                Text(
                                    text = "R$ ${String.format(Locale.US, "%.2f", card.currentInvoice)}",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFFFDA4AF)
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Limite Disponível", fontSize = 10.sp, color = Slate300)
                                Text(
                                    text = "R$ ${String.format(Locale.US, "%.2f", available)} de R$ ${String.format(Locale.US, "%.2f", card.limit)}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Emerald400
                                )
                            }
                        }

                        // Progress Bar
                        LinearProgressIndicator(
                            progress = { usedPct / 100f },
                            modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                            color = if (usedPct > 80) Rose500 else Indigo500,
                            trackColor = Color.Black.copy(alpha = 0.4f)
                        )

                        // Action Buttons: Ver Fatura / Pagar Fatura
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = { cardForInvoiceDetails = card },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                                modifier = Modifier.weight(1f),
                                contentPadding = PaddingValues(vertical = 6.dp)
                            ) {
                                Icon(Icons.Default.ReceiptLong, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Ver Fatura", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }

                            Button(
                                onClick = { cardForPayment = card },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Emerald600),
                                modifier = Modifier.weight(1f),
                                contentPadding = PaddingValues(vertical = 6.dp)
                            ) {
                                Icon(Icons.Default.Payment, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Pagar Fatura", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }

    // New / Edit Account Dialog
    if (showNewAccountDialog) {
        var accName by remember { mutableStateOf("") }
        var accInst by remember { mutableStateOf("") }
        var accBalanceText by remember { mutableStateOf("0.00") }
        Dialog(onDismissRequest = { showNewAccountDialog = false }) {
            Surface(shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.surface, modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Nova Conta Bancária", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    OutlinedTextField(value = accName, onValueChange = { accName = it }, label = { Text("Nome da Conta (ex: Nubank)") }, singleLine = true, shape = RoundedCornerShape(10.dp), modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = accInst, onValueChange = { accInst = it }, label = { Text("Instituição Financeira") }, singleLine = true, shape = RoundedCornerShape(10.dp), modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = accBalanceText, onValueChange = { accBalanceText = it }, label = { Text("Saldo Inicial (R$)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), singleLine = true, shape = RoundedCornerShape(10.dp), modifier = Modifier.fillMaxWidth())
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = { showNewAccountDialog = false }) { Text("Cancelar") }
                        Button(onClick = {
                            if (accName.isNotBlank()) {
                                onAddAccount(
                                    AccountEntity(
                                        id = "acc-${System.currentTimeMillis()}",
                                        name = accName.trim(),
                                        type = "checking",
                                        institution = accInst.trim().ifEmpty { accName.trim() },
                                        balance = accBalanceText.replace(",", ".").toDoubleOrNull() ?: 0.0
                                    )
                                )
                                showNewAccountDialog = false
                            }
                        }, colors = ButtonDefaults.buttonColors(containerColor = Emerald600)) {
                            Text("Salvar")
                        }
                    }
                }
            }
        }
    }

    // New Card Dialog
    if (showNewCardDialog) {
        var cardName by remember { mutableStateOf("") }
        var cardInst by remember { mutableStateOf("") }
        var cardLimitText by remember { mutableStateOf("5000.00") }
        var closingDay by remember { mutableStateOf(15) }
        var dueDay by remember { mutableStateOf(22) }
        Dialog(onDismissRequest = { showNewCardDialog = false }) {
            Surface(shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.surface, modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Novo Cartão de Crédito", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    OutlinedTextField(value = cardName, onValueChange = { cardName = it }, label = { Text("Nome do Cartão (ex: Nubank Ultravioleta)") }, singleLine = true, shape = RoundedCornerShape(10.dp), modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = cardInst, onValueChange = { cardInst = it }, label = { Text("Banco / Emissor") }, singleLine = true, shape = RoundedCornerShape(10.dp), modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = cardLimitText, onValueChange = { cardLimitText = it }, label = { Text("Limite Total (R$)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), singleLine = true, shape = RoundedCornerShape(10.dp), modifier = Modifier.fillMaxWidth())
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(value = closingDay.toString(), onValueChange = { closingDay = it.toIntOrNull() ?: 1 }, label = { Text("Dia Fechamento") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true, shape = RoundedCornerShape(10.dp), modifier = Modifier.weight(1f))
                        OutlinedTextField(value = dueDay.toString(), onValueChange = { dueDay = it.toIntOrNull() ?: 1 }, label = { Text("Dia Vencimento") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true, shape = RoundedCornerShape(10.dp), modifier = Modifier.weight(1f))
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = { showNewCardDialog = false }) { Text("Cancelar") }
                        Button(onClick = {
                            if (cardName.isNotBlank()) {
                                onAddCreditCard(
                                    CreditCardEntity(
                                        id = "card-${System.currentTimeMillis()}",
                                        name = cardName.trim(),
                                        institution = cardInst.trim().ifEmpty { cardName.trim() },
                                        limit = cardLimitText.replace(",", ".").toDoubleOrNull() ?: 5000.0,
                                        closingDay = closingDay,
                                        dueDay = dueDay
                                    )
                                )
                                showNewCardDialog = false
                            }
                        }, colors = ButtonDefaults.buttonColors(containerColor = Indigo600)) {
                            Text("Salvar")
                        }
                    }
                }
            }
        }
    }

    // Adjust Account Balance Dialog
    if (accountForBalanceAdjust != null) {
        val acc = accountForBalanceAdjust!!
        var newBalText by remember { mutableStateOf(String.format(Locale.US, "%.2f", acc.balance)) }
        Dialog(onDismissRequest = { accountForBalanceAdjust = null }) {
            Surface(shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.surface, modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Ajustar Saldo: ${acc.name}", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    OutlinedTextField(
                        value = newBalText,
                        onValueChange = { newBalText = it },
                        label = { Text("Saldo Atualizado (R$)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = { accountForBalanceAdjust = null }) { Text("Cancelar") }
                        Button(onClick = {
                            val bal = newBalText.replace(",", ".").toDoubleOrNull() ?: acc.balance
                            onUpdateAccountBalance(acc.id, bal)
                            accountForBalanceAdjust = null
                        }, colors = ButtonDefaults.buttonColors(containerColor = Emerald600)) {
                            Text("Confirmar")
                        }
                    }
                }
            }
        }
    }

    // Pay Card Invoice Dialog
    if (cardForPayment != null) {
        val card = cardForPayment!!
        var selectedPayAccount by remember { mutableStateOf(accounts.firstOrNull()?.name ?: "Nubank Conta") }
        var payAmountText by remember { mutableStateOf(String.format(Locale.US, "%.2f", card.currentInvoice)) }
        var showAccMenu by remember { mutableStateOf(false) }

        Dialog(onDismissRequest = { cardForPayment = null }) {
            Surface(shape = RoundedCornerShape(18.dp), color = MaterialTheme.colorScheme.surface, modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Pagar Fatura: ${card.name}", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    OutlinedTextField(
                        value = payAmountText,
                        onValueChange = { payAmountText = it },
                        label = { Text("Valor do Pagamento (R$)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Box {
                        OutlinedTextField(
                            value = selectedPayAccount,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Debitar da Conta") },
                            trailingIcon = { IconButton(onClick = { showAccMenu = true }) { Icon(Icons.Default.ArrowDropDown, contentDescription = null) } },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth().clickable { showAccMenu = true }
                        )
                        DropdownMenu(expanded = showAccMenu, onDismissRequest = { showAccMenu = false }) {
                            accounts.forEach { acc ->
                                DropdownMenuItem(
                                    text = { Text("${acc.name} (Saldo: R$ ${String.format(Locale.US, "%.2f", acc.balance)})") },
                                    onClick = {
                                        selectedPayAccount = acc.name
                                        showAccMenu = false
                                    }
                                )
                            }
                        }
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = { cardForPayment = null }) { Text("Cancelar") }
                        Button(
                            onClick = {
                                val amt = payAmountText.replace(",", ".").toDoubleOrNull() ?: card.currentInvoice
                                onPayCardInvoice(card, selectedPayAccount, amt)
                                cardForPayment = null
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Emerald600)
                        ) {
                            Text("Liquidar Fatura")
                        }
                    }
                }
            }
        }
    }

    // Card Invoice Details & 6-Month Projection Dialog
    if (cardForInvoiceDetails != null) {
        val card = cardForInvoiceDetails!!
        val cardMonthTxs = transactions.filter { it.account == card.name && it.type == "expense" }

        Dialog(onDismissRequest = { cardForInvoiceDetails = null }) {
            Surface(shape = RoundedCornerShape(18.dp), color = MaterialTheme.colorScheme.surface, modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Lançamentos da Fatura: ${card.name}", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Text("Fatura de ${InitialData.MONTH_NAMES[selectedMonth - 1]}: R$ ${String.format(Locale.US, "%.2f", card.currentInvoice)}", fontSize = 12.sp, color = Emerald600, fontWeight = FontWeight.Bold)

                    if (cardMonthTxs.isEmpty()) {
                        Text("Nenhum lançamento no cartão neste mês.", fontSize = 11.sp, color = Slate400)
                    } else {
                        Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            cardMonthTxs.forEach { tx ->
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(tx.description, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                                    Text("R$ ${String.format(Locale.US, "%.2f", tx.amount)}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Rose600)
                                }
                            }
                        }
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        Button(onClick = { cardForInvoiceDetails = null }, colors = ButtonDefaults.buttonColors(containerColor = Indigo600)) {
                            Text("Fechar")
                        }
                    }
                }
            }
        }
    }

    // Delete Account Confirmation
    if (accountToDelete != null) {
        val acc = accountToDelete!!
        ConfirmDeleteDialog(
            isOpen = true,
            title = "Excluir Conta Bancária",
            message = "Deseja excluir a conta '${acc.name}'?",
            onDismiss = { accountToDelete = null },
            onConfirm = {
                onDeleteAccount(acc)
                accountToDelete = null
            }
        )
    }
}
