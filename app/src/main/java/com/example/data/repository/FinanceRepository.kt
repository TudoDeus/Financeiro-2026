package com.example.data.repository

import com.example.data.local.*
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow

class FinanceRepository(private val db: AppDatabase) {

    // Transactions
    val allTransactions: Flow<List<TransactionEntity>> = db.transactionDao().getAllTransactions()

    suspend fun insertTransaction(transaction: TransactionEntity) =
        db.transactionDao().insertTransaction(transaction)

    suspend fun insertTransactions(transactions: List<TransactionEntity>) =
        db.transactionDao().insertTransactions(transactions)

    suspend fun updateTransaction(transaction: TransactionEntity) =
        db.transactionDao().updateTransaction(transaction)

    suspend fun deleteTransaction(transaction: TransactionEntity) =
        db.transactionDao().deleteTransaction(transaction)

    suspend fun deleteTransactionById(id: String) =
        db.transactionDao().deleteTransactionById(id)

    suspend fun deleteTransactionsByInstallmentGroup(groupId: String) =
        db.transactionDao().deleteTransactionsByInstallmentGroup(groupId)

    suspend fun deleteTransactionsByRecurrenceGroup(groupId: String) =
        db.transactionDao().deleteTransactionsByRecurrenceGroup(groupId)

    suspend fun deleteTransactionsByMonth(monthPrefix: String) =
        db.transactionDao().deleteTransactionsByMonth(monthPrefix)

    // Categories
    val allCategories: Flow<List<CategoryEntity>> = db.categoryDao().getAllCategories()

    suspend fun insertCategory(category: CategoryEntity) =
        db.categoryDao().insertCategory(category)

    suspend fun updateCategory(category: CategoryEntity) =
        db.categoryDao().updateCategory(category)

    suspend fun deleteCategory(category: CategoryEntity) =
        db.categoryDao().deleteCategory(category)

    suspend fun updateCategoryBudget(categoryId: String, budget: Double) =
        db.categoryDao().updateCategoryBudget(categoryId, budget)

    // Accounts
    val allAccounts: Flow<List<AccountEntity>> = db.accountDao().getAllAccounts()

    suspend fun insertAccount(account: AccountEntity) =
        db.accountDao().insertAccount(account)

    suspend fun updateAccount(account: AccountEntity) =
        db.accountDao().updateAccount(account)

    suspend fun deleteAccount(account: AccountEntity) =
        db.accountDao().deleteAccount(account)

    suspend fun updateAccountBalance(accountId: String, balance: Double) =
        db.accountDao().updateAccountBalance(accountId, balance)

    // Credit Cards
    val allCreditCards: Flow<List<CreditCardEntity>> = db.creditCardDao().getAllCreditCards()

    suspend fun insertCreditCard(card: CreditCardEntity) =
        db.creditCardDao().insertCreditCard(card)

    suspend fun updateCreditCard(card: CreditCardEntity) =
        db.creditCardDao().updateCreditCard(card)

    suspend fun deleteCreditCard(card: CreditCardEntity) =
        db.creditCardDao().deleteCreditCard(card)

    suspend fun updateCardInvoice(cardId: String, invoice: Double) =
        db.creditCardDao().updateCardInvoice(cardId, invoice)

    // Goals
    val allGoals: Flow<List<GoalEntity>> = db.goalDao().getAllGoals()

    suspend fun insertGoal(goal: GoalEntity) =
        db.goalDao().insertGoal(goal)

    suspend fun updateGoal(goal: GoalEntity) =
        db.goalDao().updateGoal(goal)

    suspend fun deleteGoal(goal: GoalEntity) =
        db.goalDao().deleteGoal(goal)

    suspend fun addAporte(goalId: String, addedAmount: Double) =
        db.goalDao().addAporte(goalId, addedAmount)
}
