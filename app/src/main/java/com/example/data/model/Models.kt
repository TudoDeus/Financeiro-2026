package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

enum class TransactionType {
    INCOME, EXPENSE, TRANSFER
}

enum class TransactionStatus {
    COMPLETED, PENDING
}

enum class AccountType {
    CHECKING, SAVINGS, CREDIT, INVESTMENT, CASH
}

@Serializable
@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey
    val id: String,
    val date: String, // YYYY-MM-DD
    val description: String,
    val amount: Double,
    val type: String, // "income", "expense", "transfer"
    val category: String,
    val subcategory: String? = null,
    val account: String,
    val targetAccount: String? = null,
    val status: String = "completed", // "completed", "pending"
    val notes: String? = null,
    val tags: String? = null,
    val syncedWithSheet: Boolean = false,
    val isRecurring: Boolean = false,
    val recurrenceGroupId: String? = null,
    val isInstallment: Boolean = false,
    val installmentGroupId: String? = null,
    val installmentCurrent: Int? = null,
    val installmentTotal: Int? = null,
    val originalAmount: Double? = null
)

@Serializable
@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val type: String, // "income", "expense"
    val icon: String,
    val color: String,
    val monthlyBudget: Double = 0.0
)

@Serializable
@Entity(tableName = "accounts")
data class AccountEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val type: String, // "checking", "savings", "credit", "investment", "cash"
    val institution: String,
    val balance: Double = 0.0,
    val color: String = "#8B5CF6",
    val accountNumber: String? = null
)

@Serializable
@Entity(tableName = "credit_cards")
data class CreditCardEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val institution: String,
    val limit: Double = 5000.0,
    val currentInvoice: Double = 0.0,
    val closingDay: Int = 15,
    val dueDay: Int = 22,
    val color: String = "#8B5CF6"
)

@Serializable
@Entity(tableName = "goals")
data class GoalEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val targetAmount: Double,
    val currentAmount: Double = 0.0,
    val deadline: String, // YYYY-MM-DD
    val category: String,
    val color: String = "#10B981",
    val notes: String? = null
)

data class MonthSummary(
    val month: String,
    val totalIncome: Double,
    val totalExpense: Double,
    val balance: Double,
    val savingsRate: Int,
    val pendingExpenses: Double,
    val pendingIncomes: Double
)
