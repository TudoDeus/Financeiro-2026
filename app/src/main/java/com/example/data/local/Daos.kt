package com.example.data.local

import androidx.room.*
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions ORDER BY date DESC")
    fun getAllTransactions(): Flow<List<TransactionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransactions(transactions: List<TransactionEntity>)

    @Update
    suspend fun updateTransaction(transaction: TransactionEntity)

    @Delete
    suspend fun deleteTransaction(transaction: TransactionEntity)

    @Query("DELETE FROM transactions WHERE id = :id")
    suspend fun deleteTransactionById(id: String)

    @Query("DELETE FROM transactions WHERE installmentGroupId = :groupId")
    suspend fun deleteTransactionsByInstallmentGroup(groupId: String)

    @Query("DELETE FROM transactions WHERE recurrenceGroupId = :groupId")
    suspend fun deleteTransactionsByRecurrenceGroup(groupId: String)

    @Query("DELETE FROM transactions WHERE date LIKE :monthPrefix || '%'")
    suspend fun deleteTransactionsByMonth(monthPrefix: String)
}

@Dao
interface CategoryDao {
    @Query("SELECT * FROM categories")
    fun getAllCategories(): Flow<List<CategoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: CategoryEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategories(categories: List<CategoryEntity>)

    @Update
    suspend fun updateCategory(category: CategoryEntity)

    @Delete
    suspend fun deleteCategory(category: CategoryEntity)

    @Query("UPDATE categories SET monthlyBudget = :budget WHERE id = :categoryId")
    suspend fun updateCategoryBudget(categoryId: String, budget: Double)
}

@Dao
interface AccountDao {
    @Query("SELECT * FROM accounts")
    fun getAllAccounts(): Flow<List<AccountEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAccount(account: AccountEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAccounts(accounts: List<AccountEntity>)

    @Update
    suspend fun updateAccount(account: AccountEntity)

    @Delete
    suspend fun deleteAccount(account: AccountEntity)

    @Query("UPDATE accounts SET balance = :balance WHERE id = :accountId")
    suspend fun updateAccountBalance(accountId: String, balance: Double)
}

@Dao
interface CreditCardDao {
    @Query("SELECT * FROM credit_cards")
    fun getAllCreditCards(): Flow<List<CreditCardEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCreditCard(card: CreditCardEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCreditCards(cards: List<CreditCardEntity>)

    @Update
    suspend fun updateCreditCard(card: CreditCardEntity)

    @Delete
    suspend fun deleteCreditCard(card: CreditCardEntity)

    @Query("UPDATE credit_cards SET currentInvoice = :invoice WHERE id = :cardId")
    suspend fun updateCardInvoice(cardId: String, invoice: Double)
}

@Dao
interface GoalDao {
    @Query("SELECT * FROM goals")
    fun getAllGoals(): Flow<List<GoalEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGoal(goal: GoalEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGoals(goals: List<GoalEntity>)

    @Update
    suspend fun updateGoal(goal: GoalEntity)

    @Delete
    suspend fun deleteGoal(goal: GoalEntity)

    @Query("UPDATE goals SET currentAmount = currentAmount + :addedAmount WHERE id = :goalId")
    suspend fun addAporte(goalId: String, addedAmount: Double)
}
