package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.InitialData
import com.example.data.model.*
import com.example.data.repository.FinanceRepository
import com.example.ui.theme.AppThemeMode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

enum class TxSortOption(val label: String) {
    DATE_DESC("Mais recentes"),
    DATE_ASC("Mais antigos"),
    AMOUNT_DESC("Maior valor"),
    AMOUNT_ASC("Menor valor"),
    NAME_ASC("Alfabético (A-Z)")
}

data class CumulativeBalanceSummary(
    val currentMonthName: String,
    val currentMonthBalance: Double,
    val prevMonthName: String,
    val prevMonthBalance: Double,
    val totalCumulativeBalance: Double,
    val currentIncome: Double,
    val currentExpense: Double,
    val prevIncome: Double,
    val prevExpense: Double
)

class FinanceViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = application.getSharedPreferences("finance_app_prefs", Context.MODE_PRIVATE)
    private val repository: FinanceRepository

    val allTransactions: StateFlow<List<TransactionEntity>>
    val allCategories: StateFlow<List<CategoryEntity>>
    val allAccounts: StateFlow<List<AccountEntity>>
    val allCreditCards: StateFlow<List<CreditCardEntity>>
    val allGoals: StateFlow<List<GoalEntity>>

    // Persistent Month & Year
    private val _selectedMonth = MutableStateFlow(prefs.getInt("saved_month", 8))
    val selectedMonth: StateFlow<Int> = _selectedMonth.asStateFlow()

    private val _selectedYear = MutableStateFlow(prefs.getInt("saved_year", 2026))
    val selectedYear: StateFlow<Int> = _selectedYear.asStateFlow()

    // Persistent Theme Mode
    private val savedThemeName = prefs.getString("saved_theme_mode", AppThemeMode.CLASSIC.name) ?: AppThemeMode.CLASSIC.name
    private val initialTheme = try {
        AppThemeMode.valueOf(savedThemeName)
    } catch (e: Exception) {
        AppThemeMode.CLASSIC
    }
    private val _themeMode = MutableStateFlow(initialTheme)
    val themeMode: StateFlow<AppThemeMode> = _themeMode.asStateFlow()

    // Cumulative Balance Toggle
    private val _showCumulativeBalance = MutableStateFlow(false)
    val showCumulativeBalance: StateFlow<Boolean> = _showCumulativeBalance.asStateFlow()

    // Sort Option
    private val _sortOption = MutableStateFlow(TxSortOption.DATE_DESC)
    val sortOption: StateFlow<TxSortOption> = _sortOption.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _filterType = MutableStateFlow("all") // all, expense, income, transfer, installment, recurring
    val filterType: StateFlow<String> = _filterType.asStateFlow()

    private val _filterCategory = MutableStateFlow<String?>(null)
    val filterCategory: StateFlow<String?> = _filterCategory.asStateFlow()

    private val _filterAccount = MutableStateFlow<String?>(null)
    val filterAccount: StateFlow<String?> = _filterAccount.asStateFlow()

    private val _filterStatus = MutableStateFlow<String?>(null) // all, completed, pending
    val filterStatus: StateFlow<String?> = _filterStatus.asStateFlow()

    // Active Category Inspection Modal (e.g. clicking Terreno shows expenses & respective incomes)
    private val _inspectedCategory = MutableStateFlow<String?>(null)
    val inspectedCategory: StateFlow<String?> = _inspectedCategory.asStateFlow()

    private val _googleSheetId = MutableStateFlow("1BxiMVs0XRA5nFMdKvBdBZjgmUUqptlbs74OgvE2upms")
    val googleSheetId: StateFlow<String> = _googleSheetId.asStateFlow()

    private val _isGoogleConnected = MutableStateFlow(true)
    val isGoogleConnected: StateFlow<Boolean> = _isGoogleConnected.asStateFlow()

    private val _syncMessage = MutableStateFlow<String?>(null)
    val syncMessage: StateFlow<String?> = _syncMessage.asStateFlow()

    init {
        val database = AppDatabase.getDatabase(application, viewModelScope)
        repository = FinanceRepository(database)

        allTransactions = repository.allTransactions.stateIn(
            viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
        )
        allCategories = repository.allCategories.stateIn(
            viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
        )
        allAccounts = repository.allAccounts.stateIn(
            viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
        )
        allCreditCards = repository.allCreditCards.stateIn(
            viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
        )
        allGoals = repository.allGoals.stateIn(
            viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
        )
    }

    val currentMonthPrefix: StateFlow<String> = combine(_selectedYear, _selectedMonth) { year, month ->
        String.format(Locale.US, "%04d-%02d", year, month)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "2026-08")

    val prevMonthPrefix: StateFlow<String> = combine(_selectedYear, _selectedMonth) { year, month ->
        val prevYear = if (month == 1) year - 1 else year
        val prevMonth = if (month == 1) 12 else month - 1
        String.format(Locale.US, "%04d-%02d", prevYear, prevMonth)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "2026-07")

    val monthTransactions: StateFlow<List<TransactionEntity>> = combine(
        allTransactions, currentMonthPrefix
    ) { transactions, prefix ->
        transactions.filter { it.date.startsWith(prefix) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val prevMonthTransactions: StateFlow<List<TransactionEntity>> = combine(
        allTransactions, prevMonthPrefix
    ) { transactions, prefix ->
        transactions.filter { it.date.startsWith(prefix) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Cumulative Balance Data (Current Month + Previous Month)
    val cumulativeBalanceSummary: StateFlow<CumulativeBalanceSummary> = combine(
        monthTransactions, prevMonthTransactions, _selectedMonth, _selectedYear
    ) { currTxs, prevTxs, month, year ->
        val currIncome = currTxs.filter { it.type == "income" }.sumOf { it.amount }
        val currExpense = currTxs.filter { it.type == "expense" }.sumOf { it.amount }
        val currBal = currIncome - currExpense

        val prevIncome = prevTxs.filter { it.type == "income" }.sumOf { it.amount }
        val prevExpense = prevTxs.filter { it.type == "expense" }.sumOf { it.amount }
        val prevBal = prevIncome - prevExpense

        val prevMonthNum = if (month == 1) 12 else month - 1
        val currName = InitialData.MONTH_NAMES.getOrElse(month - 1) { "Mês $month" }
        val prevName = InitialData.MONTH_NAMES.getOrElse(prevMonthNum - 1) { "Mês $prevMonthNum" }

        CumulativeBalanceSummary(
            currentMonthName = currName,
            currentMonthBalance = currBal,
            prevMonthName = prevName,
            prevMonthBalance = prevBal,
            totalCumulativeBalance = currBal + prevBal,
            currentIncome = currIncome,
            currentExpense = currExpense,
            prevIncome = prevIncome,
            prevExpense = prevExpense
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        CumulativeBalanceSummary("Agosto", 0.0, "Julho", 0.0, 0.0, 0.0, 0.0, 0.0, 0.0)
    )

    private data class TxFilters(
        val query: String,
        val type: String,
        val category: String?,
        val account: String?,
        val status: String?,
        val sort: TxSortOption
    )

    private data class FilterCriteria(val query: String, val type: String, val cat: String?, val acc: String?)

    private val filterCriteriaFlow = combine(
        _searchQuery, _filterType, _filterCategory, _filterAccount
    ) { query, type, cat, acc ->
        FilterCriteria(query, type, cat, acc)
    }

    private val filtersFlow = combine(
        filterCriteriaFlow, _filterStatus, _sortOption
    ) { crit, status, sort ->
        TxFilters(crit.query, crit.type, crit.cat, crit.acc, status, sort)
    }

    val filteredTransactions: StateFlow<List<TransactionEntity>> = combine(
        monthTransactions, filtersFlow
    ) { list, filters ->
        val filtered = list.filter { tx ->
            val matchQuery = filters.query.isEmpty() ||
                    tx.description.contains(filters.query, ignoreCase = true) ||
                    tx.category.contains(filters.query, ignoreCase = true) ||
                    tx.account.contains(filters.query, ignoreCase = true) ||
                    (tx.notes?.contains(filters.query, ignoreCase = true) == true)

            val matchType = when (filters.type) {
                "all" -> true
                "expense" -> tx.type == "expense"
                "income" -> tx.type == "income"
                "transfer" -> tx.type == "transfer"
                "installment" -> tx.isInstallment
                "recurring" -> tx.isRecurring
                else -> true
            }

            val matchCat = filters.category == null || tx.category == filters.category
            val matchAcc = filters.account == null || tx.account == filters.account
            val matchStatus = filters.status == null || tx.status == filters.status

            matchQuery && matchType && matchCat && matchAcc && matchStatus
        }

        when (filters.sort) {
            TxSortOption.DATE_DESC -> filtered.sortedByDescending { it.date }
            TxSortOption.DATE_ASC -> filtered.sortedBy { it.date }
            TxSortOption.AMOUNT_DESC -> filtered.sortedByDescending { it.amount }
            TxSortOption.AMOUNT_ASC -> filtered.sortedBy { it.amount }
            TxSortOption.NAME_ASC -> filtered.sortedBy { it.description.lowercase() }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val monthSummary: StateFlow<MonthSummary> = combine(monthTransactions, _selectedMonth) { list, month ->
        val income = list.filter { it.type == "income" }.sumOf { it.amount }
        val expense = list.filter { it.type == "expense" }.sumOf { it.amount }
        val bal = income - expense
        val rate = if (income > 0) Math.max(0, Math.min(100, Math.round(((income - expense) / income) * 100).toInt())) else 0
        val pendingExp = list.filter { it.type == "expense" && it.status == "pending" }.sumOf { it.amount }
        val pendingInc = list.filter { it.type == "income" && it.status == "pending" }.sumOf { it.amount }

        val monthName = InitialData.MONTH_NAMES.getOrElse(month - 1) { "Mês $month" }

        MonthSummary(
            month = monthName,
            totalIncome = income,
            totalExpense = expense,
            balance = bal,
            savingsRate = rate,
            pendingExpenses = pendingExp,
            pendingIncomes = pendingInc
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        MonthSummary("Agosto", 0.0, 0.0, 0.0, 0, 0.0, 0.0)
    )

    fun setThemeMode(mode: AppThemeMode) {
        _themeMode.value = mode
        prefs.edit().putString("saved_theme_mode", mode.name).apply()
    }

    fun toggleCumulativeBalance() {
        _showCumulativeBalance.value = !_showCumulativeBalance.value
    }

    fun setSortOption(option: TxSortOption) {
        _sortOption.value = option
    }

    fun setInspectedCategory(categoryName: String?) {
        _inspectedCategory.value = categoryName
    }

    private fun persistSelectedDate(month: Int, year: Int) {
        prefs.edit()
            .putInt("saved_month", month)
            .putInt("saved_year", year)
            .apply()
    }

    fun nextMonth() {
        if (_selectedMonth.value == 12) {
            _selectedMonth.value = 1
            _selectedYear.value += 1
        } else {
            _selectedMonth.value += 1
        }
        persistSelectedDate(_selectedMonth.value, _selectedYear.value)
    }

    fun prevMonth() {
        if (_selectedMonth.value == 1) {
            _selectedMonth.value = 12
            _selectedYear.value -= 1
        } else {
            _selectedMonth.value -= 1
        }
        persistSelectedDate(_selectedMonth.value, _selectedYear.value)
    }

    fun setMonthAndYear(month: Int, year: Int) {
        _selectedMonth.value = month
        _selectedYear.value = year
        persistSelectedDate(month, year)
    }

    fun setSearchQuery(q: String) {
        _searchQuery.value = q
    }

    fun setFilterType(t: String) {
        _filterType.value = t
    }

    fun setFilterCategory(c: String?) {
        _filterCategory.value = c
    }

    fun setFilterAccount(a: String?) {
        _filterAccount.value = a
    }

    fun setFilterStatus(s: String?) {
        _filterStatus.value = s
    }

    fun setGoogleSheetId(id: String) {
        _googleSheetId.value = id
    }

    fun setGoogleConnected(connected: Boolean) {
        _isGoogleConnected.value = connected
    }

    fun showSyncMessage(msg: String) {
        _syncMessage.value = msg
        viewModelScope.launch {
            kotlinx.coroutines.delay(4000)
            if (_syncMessage.value == msg) {
                _syncMessage.value = null
            }
        }
    }

    // Transactions CRUD - Safe save & update without duplication
    fun saveSingleTransaction(transaction: TransactionEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.insertTransaction(transaction)
            recalculateCardInvoices()
        }
    }

    fun updateTransaction(transaction: TransactionEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateTransaction(transaction)
            recalculateCardInvoices()
        }
    }

    fun saveInstallmentTransaction(
        baseDesc: String,
        amountPerInstallment: Double,
        totalInstallments: Int,
        startInstallment: Int,
        type: String,
        category: String,
        account: String,
        startDate: String,
        status: String,
        notes: String?
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val groupId = "inst-${System.currentTimeMillis()}"
            val remaining = totalInstallments - startInstallment + 1
            val list = mutableListOf<TransactionEntity>()

            val parts = startDate.split("-")
            val baseYear = parts[0].toInt()
            val baseMonth = parts[1].toInt()
            val baseDay = parts[2].toInt()

            for (i in 0 until remaining) {
                val currentNum = startInstallment + i
                val targetMonthIdx = (baseMonth - 1) + i
                val targetYear = baseYear + (targetMonthIdx / 12)
                val targetMonth = (targetMonthIdx % 12) + 1
                val maxDaysInMonth = LocalDate.of(targetYear, targetMonth, 1).lengthOfMonth()
                val actualDay = Math.min(baseDay, maxDaysInMonth)
                val instDate = String.format(Locale.US, "%04d-%02d-%02d", targetYear, targetMonth, actualDay)

                val padLen = Math.max(2, totalInstallments.toString().length)
                val padTotal = totalInstallments.toString().padStart(padLen, '0')
                val padCurr = currentNum.toString().padStart(padLen, '0')
                val cleanDesc = baseDesc.replace(Regex("\\s*\\(\\d+/\\d+\\)\\s*$"), "").trim()

                list.add(
                    TransactionEntity(
                        id = UUID.randomUUID().toString(),
                        date = instDate,
                        description = "$cleanDesc ($padCurr/$padTotal)",
                        amount = amountPerInstallment,
                        type = type,
                        category = category,
                        account = account,
                        status = if (i == 0) status else "pending",
                        notes = if (!notes.isNullOrEmpty()) "$notes | Parcela $currentNum de $totalInstallments" else "Parcela $currentNum de $totalInstallments",
                        syncedWithSheet = _isGoogleConnected.value,
                        isInstallment = true,
                        installmentGroupId = groupId,
                        installmentCurrent = currentNum,
                        installmentTotal = totalInstallments,
                        originalAmount = amountPerInstallment * totalInstallments
                    )
                )
            }
            repository.insertTransactions(list)
            recalculateCardInvoices()
        }
    }

    fun saveRecurringTransaction(
        desc: String,
        amount: Double,
        type: String,
        category: String,
        account: String,
        targetAccount: String?,
        startDate: String,
        status: String,
        notes: String?,
        scope: String, // "end_of_year", "full_year", "custom"
        customMonths: Int = 12
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val groupId = "rec-${System.currentTimeMillis()}"
            val list = mutableListOf<TransactionEntity>()
            val parts = startDate.split("-")
            val baseYear = parts[0].toInt()
            val baseMonth = parts[1].toInt()
            val baseDay = parts[2].toInt()

            if (scope == "full_year") {
                for (m in 1..12) {
                    val maxDays = LocalDate.of(baseYear, m, 1).lengthOfMonth()
                    val actualDay = Math.min(baseDay, maxDays)
                    val dateStr = String.format(Locale.US, "%04d-%02d-%02d", baseYear, m, actualDay)
                    val isPastOrCurrent = m <= baseMonth

                    list.add(
                        TransactionEntity(
                            id = UUID.randomUUID().toString(),
                            date = dateStr,
                            description = desc.trim(),
                            amount = amount,
                            type = type,
                            category = category,
                            account = account,
                            targetAccount = targetAccount,
                            status = if (isPastOrCurrent) status else "pending",
                            notes = if (!notes.isNullOrEmpty()) "$notes (Fixo Mensal)" else "Fixo Mensal",
                            syncedWithSheet = _isGoogleConnected.value,
                            isRecurring = true,
                            recurrenceGroupId = groupId
                        )
                    )
                }
            } else {
                val count = if (scope == "end_of_year") Math.max(1, 12 - baseMonth + 1) else customMonths
                for (i in 0 until count) {
                    val targetMonthIdx = (baseMonth - 1) + i
                    val targetYear = baseYear + (targetMonthIdx / 12)
                    val targetMonth = (targetMonthIdx % 12) + 1
                    val maxDaysInMonth = LocalDate.of(targetYear, targetMonth, 1).lengthOfMonth()
                    val actualDay = Math.min(baseDay, maxDaysInMonth)
                    val dateStr = String.format(Locale.US, "%04d-%02d-%02d", targetYear, targetMonth, actualDay)

                    list.add(
                        TransactionEntity(
                            id = UUID.randomUUID().toString(),
                            date = dateStr,
                            description = desc.trim(),
                            amount = amount,
                            type = type,
                            category = category,
                            account = account,
                            targetAccount = targetAccount,
                            status = if (i == 0) status else "pending",
                            notes = if (!notes.isNullOrEmpty()) "$notes (Fixo Mensal)" else "Fixo Mensal",
                            syncedWithSheet = _isGoogleConnected.value,
                            isRecurring = true,
                            recurrenceGroupId = groupId
                        )
                    )
                }
            }
            repository.insertTransactions(list)
            recalculateCardInvoices()
        }
    }

    fun deleteTransaction(transaction: TransactionEntity, deleteAllInGroup: Boolean = false) {
        viewModelScope.launch(Dispatchers.IO) {
            if (deleteAllInGroup && transaction.isInstallment && !transaction.installmentGroupId.isNullOrEmpty()) {
                repository.deleteTransactionsByInstallmentGroup(transaction.installmentGroupId)
            } else if (deleteAllInGroup && transaction.isRecurring && !transaction.recurrenceGroupId.isNullOrEmpty()) {
                repository.deleteTransactionsByRecurrenceGroup(transaction.recurrenceGroupId)
            } else {
                repository.deleteTransaction(transaction)
            }
            recalculateCardInvoices()
        }
    }

    fun toggleTransactionStatus(transaction: TransactionEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            val newStatus = if (transaction.status == "completed") "pending" else "completed"
            repository.updateTransaction(transaction.copy(status = newStatus))
            recalculateCardInvoices()
        }
    }

    fun resetMonthTransactions(monthPrefix: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteTransactionsByMonth(monthPrefix)
            recalculateCardInvoices()
        }
    }

    // Categories
    fun addCategory(category: CategoryEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.insertCategory(category)
        }
    }

    fun updateCategory(category: CategoryEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateCategory(category)
        }
    }

    fun deleteCategory(category: CategoryEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteCategory(category)
        }
    }

    fun updateCategoryBudget(categoryId: String, budget: Double) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateCategoryBudget(categoryId, budget)
        }
    }

    // Accounts
    fun addAccount(account: AccountEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.insertAccount(account)
        }
    }

    fun updateAccount(account: AccountEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateAccount(account)
        }
    }

    fun deleteAccount(account: AccountEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteAccount(account)
        }
    }

    fun updateAccountBalance(accountId: String, newBalance: Double) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateAccountBalance(accountId, newBalance)
        }
    }

    // Credit Cards
    fun addCreditCard(card: CreditCardEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.insertCreditCard(card)
        }
    }

    fun updateCreditCard(card: CreditCardEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateCreditCard(card)
        }
    }

    fun deleteCreditCard(card: CreditCardEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteCreditCard(card)
        }
    }

    fun payCreditCardInvoice(card: CreditCardEntity, fromAccountName: String, amount: Double) {
        viewModelScope.launch(Dispatchers.IO) {
            val currentAccounts = allAccounts.value
            val targetAcc = currentAccounts.find { it.name == fromAccountName }
            if (targetAcc != null) {
                repository.updateAccountBalance(targetAcc.id, targetAcc.balance - amount)
            }
            val today = LocalDate.now().format(DateTimeFormatter.ISO_DATE)
            val paymentTx = TransactionEntity(
                id = UUID.randomUUID().toString(),
                date = today,
                description = "Pagamento Fatura ${card.name}",
                amount = amount,
                type = "expense",
                category = "Assinaturas",
                account = fromAccountName,
                status = "completed",
                notes = "Liquidação de fatura do cartão",
                syncedWithSheet = _isGoogleConnected.value
            )
            repository.insertTransaction(paymentTx)
            recalculateCardInvoices()
        }
    }

    // Goals
    fun addGoal(goal: GoalEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.insertGoal(goal)
        }
    }

    fun updateGoal(goal: GoalEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateGoal(goal)
        }
    }

    fun deleteGoal(goal: GoalEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteGoal(goal)
        }
    }

    fun addGoalAporte(goalId: String, amount: Double) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.addAporte(goalId, amount)
        }
    }

    private suspend fun recalculateCardInvoices() {
        val currentPrefix = currentMonthPrefix.value
        val txs = allTransactions.first().filter { it.date.startsWith(currentPrefix) && it.type == "expense" }
        val cards = allCreditCards.first()
        cards.forEach { card ->
            val sum = txs.filter { it.account == card.name }.sumOf { it.amount }
            repository.updateCardInvoice(card.id, sum)
        }
    }
}

