package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.TransactionEntity
import com.example.ui.components.GoogleSyncDialog
import com.example.ui.components.TopHeader
import com.example.ui.components.TransactionDialog
import com.example.ui.screens.*
import com.example.ui.theme.ControleFinanceiroTheme
import com.example.ui.theme.Emerald600
import com.example.ui.viewmodel.FinanceViewModel

enum class NavigationTab(val title: String, val icon: ImageVector) {
    DASHBOARD("Início", Icons.Default.Dashboard),
    TRANSACTIONS("Lançamentos", Icons.Default.ReceiptLong),
    BUDGET("Orçamento", Icons.Default.PieChart),
    ACCOUNTS("Contas", Icons.Default.CreditCard),
    GOALS("Metas", Icons.Default.Savings),
    REPORTS("Relatórios", Icons.Default.Analytics)
}

class MainActivity : ComponentActivity() {
    private val viewModel: FinanceViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            ControleFinanceiroTheme {
                MainApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun MainApp(viewModel: FinanceViewModel) {
    val context = LocalContext.current
    var currentTab by remember { mutableStateOf(NavigationTab.DASHBOARD) }

    val selectedMonth by viewModel.selectedMonth.collectAsStateWithLifecycle()
    val selectedYear by viewModel.selectedYear.collectAsStateWithLifecycle()
    val isGoogleConnected by viewModel.isGoogleConnected.collectAsStateWithLifecycle()
    val googleSheetId by viewModel.googleSheetId.collectAsStateWithLifecycle()

    val monthTransactions by viewModel.monthTransactions.collectAsStateWithLifecycle()
    val filteredTransactions by viewModel.filteredTransactions.collectAsStateWithLifecycle()
    val allTransactions by viewModel.allTransactions.collectAsStateWithLifecycle()
    val categories by viewModel.allCategories.collectAsStateWithLifecycle()
    val accounts by viewModel.allAccounts.collectAsStateWithLifecycle()
    val creditCards by viewModel.allCreditCards.collectAsStateWithLifecycle()
    val goals by viewModel.allGoals.collectAsStateWithLifecycle()
    val monthSummary by viewModel.monthSummary.collectAsStateWithLifecycle()

    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val filterType by viewModel.filterType.collectAsStateWithLifecycle()
    val filterCategory by viewModel.filterCategory.collectAsStateWithLifecycle()
    val filterAccount by viewModel.filterAccount.collectAsStateWithLifecycle()

    var showTransactionDialog by remember { mutableStateOf(false) }
    var transactionToEdit by remember { mutableStateOf<TransactionEntity?>(null) }
    var showGoogleSyncDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopHeader(
                selectedMonth = selectedMonth,
                selectedYear = selectedYear,
                isGoogleConnected = isGoogleConnected,
                onPrevMonth = { viewModel.prevMonth() },
                onNextMonth = { viewModel.nextMonth() },
                onMonthYearSelected = { m, y -> viewModel.setMonthAndYear(m, y) },
                onOpenGoogleSync = { showGoogleSyncDialog = true },
                onOpenNewTransaction = {
                    transactionToEdit = null
                    showTransactionDialog = true
                }
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp,
                modifier = Modifier.testTag("bottom_navigation_bar")
            ) {
                NavigationTab.entries.forEach { tab ->
                    val selected = currentTab == tab
                    NavigationBarItem(
                        selected = selected,
                        onClick = { currentTab = tab },
                        icon = {
                            Icon(
                                imageVector = tab.icon,
                                contentDescription = tab.title,
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        label = {
                            Text(
                                text = tab.title,
                                fontSize = 10.sp,
                                maxLines = 1
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Emerald600,
                            selectedTextColor = Emerald600,
                            indicatorColor = Emerald600.copy(alpha = 0.12f)
                        ),
                        modifier = Modifier.testTag("nav_tab_${tab.name.lowercase()}")
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentTab) {
                NavigationTab.DASHBOARD -> {
                    DashboardScreen(
                        selectedMonth = selectedMonth,
                        selectedYear = selectedYear,
                        monthSummary = monthSummary,
                        transactions = monthTransactions,
                        categories = categories,
                        accounts = accounts,
                        creditCards = creditCards,
                        goals = goals,
                        onOpenNewTransaction = {
                            transactionToEdit = null
                            showTransactionDialog = true
                        },
                        onNavigateToTransactions = { currentTab = NavigationTab.TRANSACTIONS },
                        onNavigateToGoals = { currentTab = NavigationTab.GOALS },
                        onNavigateToBudget = { currentTab = NavigationTab.BUDGET },
                        onNavigateToAccounts = { currentTab = NavigationTab.ACCOUNTS },
                        onToggleTransactionStatus = { viewModel.toggleTransactionStatus(it) },
                        onEditTransaction = {
                            transactionToEdit = it
                            showTransactionDialog = true
                        }
                    )
                }

                NavigationTab.TRANSACTIONS -> {
                    TransactionsScreen(
                        selectedMonth = selectedMonth,
                        selectedYear = selectedYear,
                        transactions = filteredTransactions,
                        categories = categories,
                        searchQuery = searchQuery,
                        filterType = filterType,
                        filterCategory = filterCategory,
                        filterAccount = filterAccount,
                        onSearchChange = { viewModel.setSearchQuery(it) },
                        onFilterTypeChange = { viewModel.setFilterType(it) },
                        onFilterCategoryChange = { viewModel.setFilterCategory(it) },
                        onFilterAccountChange = { viewModel.setFilterAccount(it) },
                        onOpenNewTransaction = {
                            transactionToEdit = null
                            showTransactionDialog = true
                        },
                        onToggleStatus = { viewModel.toggleTransactionStatus(it) },
                        onEditTransaction = {
                            transactionToEdit = it
                            showTransactionDialog = true
                        },
                        onDeleteTransaction = { tx, all -> viewModel.deleteTransaction(tx, all) },
                        onResetMonth = { prefix -> viewModel.resetMonthTransactions(prefix) }
                    )
                }

                NavigationTab.BUDGET -> {
                    BudgetScreen(
                        selectedMonth = selectedMonth,
                        selectedYear = selectedYear,
                        categories = categories,
                        transactions = monthTransactions,
                        onAddCategory = { viewModel.addCategory(it) },
                        onUpdateCategory = { viewModel.updateCategory(it) },
                        onDeleteCategory = { viewModel.deleteCategory(it) },
                        onUpdateCategoryBudget = { id, b -> viewModel.updateCategoryBudget(id, b) }
                    )
                }

                NavigationTab.ACCOUNTS -> {
                    AccountsCardsScreen(
                        selectedMonth = selectedMonth,
                        selectedYear = selectedYear,
                        accounts = accounts,
                        creditCards = creditCards,
                        transactions = monthTransactions,
                        allTransactions = allTransactions,
                        onAddAccount = { viewModel.addAccount(it) },
                        onUpdateAccount = { viewModel.updateAccount(it) },
                        onDeleteAccount = { viewModel.deleteAccount(it) },
                        onUpdateAccountBalance = { id, bal -> viewModel.updateAccountBalance(id, bal) },
                        onAddCreditCard = { viewModel.addCreditCard(it) },
                        onUpdateCreditCard = { viewModel.updateCreditCard(it) },
                        onDeleteCreditCard = { viewModel.deleteCreditCard(it) },
                        onPayCardInvoice = { card, acc, amt ->
                            viewModel.payCreditCardInvoice(card, acc, amt)
                            Toast.makeText(context, "Fatura de ${card.name} liquidada com sucesso!", Toast.LENGTH_SHORT).show()
                        }
                    )
                }

                NavigationTab.GOALS -> {
                    GoalsScreen(
                        goals = goals,
                        onAddGoal = { viewModel.addGoal(it) },
                        onDeleteGoal = { viewModel.deleteGoal(it) },
                        onAddAporte = { id, amt ->
                            viewModel.addGoalAporte(id, amt)
                            Toast.makeText(context, "Aporte registrado com sucesso!", Toast.LENGTH_SHORT).show()
                        }
                    )
                }

                NavigationTab.REPORTS -> {
                    ReportsScreen(
                        selectedYear = selectedYear,
                        allTransactions = allTransactions,
                        categories = categories
                    )
                }
            }
        }
    }

    // Transaction Creation/Editing Dialog
    if (showTransactionDialog) {
        TransactionDialog(
            isOpen = true,
            initialTransaction = transactionToEdit,
            categories = categories,
            accounts = accounts,
            creditCards = creditCards,
            onDismiss = {
                showTransactionDialog = false
                transactionToEdit = null
            },
            onSaveSingle = { tx ->
                viewModel.saveSingleTransaction(tx)
                Toast.makeText(context, "Lançamento salvo com sucesso!", Toast.LENGTH_SHORT).show()
                showTransactionDialog = false
                transactionToEdit = null
            },
            onSaveInstallment = { desc, perInst, total, start, type, cat, acc, date, status, notes ->
                viewModel.saveInstallmentTransaction(
                    desc, perInst, total, start, type, cat, acc, date, status, notes
                )
                Toast.makeText(context, "Parcelamento em $total vezes criado com sucesso!", Toast.LENGTH_SHORT).show()
                showTransactionDialog = false
                transactionToEdit = null
            },
            onSaveRecurring = { desc, amt, type, cat, acc, target, date, status, notes, scope, custom ->
                viewModel.saveRecurringTransaction(
                    desc, amt, type, cat, acc, target, date, status, notes, scope, custom
                )
                Toast.makeText(context, "Lançamento fixo mensal replicado com sucesso!", Toast.LENGTH_SHORT).show()
                showTransactionDialog = false
                transactionToEdit = null
            }
        )
    }

    // Google Sheets Sync Settings Dialog
    if (showGoogleSyncDialog) {
        GoogleSyncDialog(
            isOpen = true,
            sheetId = googleSheetId,
            isConnected = isGoogleConnected,
            onDismiss = { showGoogleSyncDialog = false },
            onSave = { id, connected ->
                viewModel.setGoogleSheetId(id)
                viewModel.setGoogleConnected(connected)
                showGoogleSyncDialog = false
                Toast.makeText(context, if (connected) "Google Sheets conectado!" else "Sincronização desativada.", Toast.LENGTH_SHORT).show()
            }
        )
    }
}
