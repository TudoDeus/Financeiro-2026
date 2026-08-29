package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        TransactionEntity::class,
        CategoryEntity::class,
        AccountEntity::class,
        CreditCardEntity::class,
        GoalEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao
    abstract fun categoryDao(): CategoryDao
    abstract fun accountDao(): AccountDao
    abstract fun creditCardDao(): CreditCardDao
    abstract fun goalDao(): GoalDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "controle_financeiro_db"
                )
                    .addCallback(DatabaseCallback(scope))
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateDatabase(database)
                    }
                }
            }

            override fun onOpen(db: SupportSQLiteDatabase) {
                super.onOpen(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        // Ensure requested categories exist
                        db.execSQL("INSERT OR IGNORE INTO categories (id, name, type, icon, color, monthlyBudget) VALUES ('cat-casa', 'Casa', 'expense', 'Home', '#3B82F6', 2200.0)")
                        db.execSQL("INSERT OR IGNORE INTO categories (id, name, type, icon, color, monthlyBudget) VALUES ('cat-assinaturas', 'Assinaturas', 'expense', 'FileText', '#8B5CF6', 350.0)")
                        db.execSQL("INSERT OR IGNORE INTO categories (id, name, type, icon, color, monthlyBudget) VALUES ('cat-carro', 'Carro', 'expense', 'DirectionsCar', '#F59E0B', 700.0)")
                        db.execSQL("INSERT OR IGNORE INTO categories (id, name, type, icon, color, monthlyBudget) VALUES ('cat-alimentacao-mercado', 'Alimentação/Mercado', 'expense', 'ShoppingCart', '#10B981', 1600.0)")
                        db.execSQL("INSERT OR IGNORE INTO categories (id, name, type, icon, color, monthlyBudget) VALUES ('cat-academia', 'Academia', 'expense', 'FitnessCenter', '#EC4899', 180.0)")
                        db.execSQL("INSERT OR IGNORE INTO categories (id, name, type, icon, color, monthlyBudget) VALUES ('cat-terreno', 'Terreno', 'expense', 'Landscape', '#14B8A6', 1200.0)")
                        db.execSQL("INSERT OR IGNORE INTO categories (id, name, type, icon, color, monthlyBudget) VALUES ('cat-lazer', 'Lazer', 'expense', 'Sparkles', '#F43F5E', 600.0)")
                        db.execSQL("INSERT OR IGNORE INTO categories (id, name, type, icon, color, monthlyBudget) VALUES ('cat-salario', 'Salário', 'income', 'AccountBalanceWallet', '#10B981', 7500.0)")
                        db.execSQL("INSERT OR IGNORE INTO categories (id, name, type, icon, color, monthlyBudget) VALUES ('cat-rec-terreno', 'Terreno', 'income', 'Landscape', '#14B8A6', 2500.0)")
                        db.execSQL("INSERT OR IGNORE INTO categories (id, name, type, icon, color, monthlyBudget) VALUES ('cat-rendimentos', 'Rendimentos', 'income', 'TrendingUp', '#8B5CF6', 500.0)")
                        db.execSQL("INSERT OR IGNORE INTO categories (id, name, type, icon, color, monthlyBudget) VALUES ('cat-outras-rec', 'Outras Receitas', 'income', 'AddCircle', '#64748B', 300.0)")
                    }
                }
            }

            suspend fun populateDatabase(db: AppDatabase) {
                db.categoryDao().insertCategories(InitialData.INITIAL_CATEGORIES)
                db.accountDao().insertAccounts(InitialData.INITIAL_ACCOUNTS)
                db.creditCardDao().insertCreditCards(InitialData.INITIAL_CREDIT_CARDS)
                db.goalDao().insertGoals(InitialData.INITIAL_GOALS)
                db.transactionDao().insertTransactions(InitialData.INITIAL_TRANSACTIONS)
            }
        }
    }
}
