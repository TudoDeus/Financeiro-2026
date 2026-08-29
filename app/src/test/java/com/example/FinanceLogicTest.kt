package com.example

import com.example.data.model.MonthSummary
import com.example.data.model.TransactionEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class FinanceLogicTest {

    @Test
    fun testMonthSummaryCalculation() {
        val transactions = listOf(
            TransactionEntity(
                id = "1",
                date = "2026-03-05",
                description = "Salário",
                amount = 10000.0,
                type = "income",
                category = "Salário",
                account = "Nubank Conta",
                status = "completed"
            ),
            TransactionEntity(
                id = "2",
                date = "2026-03-10",
                description = "Aluguel",
                amount = 3000.0,
                type = "expense",
                category = "Moradia",
                account = "Nubank Conta",
                status = "completed"
            ),
            TransactionEntity(
                id = "3",
                date = "2026-03-15",
                description = "Supermercado",
                amount = 1000.0,
                type = "expense",
                category = "Alimentação",
                account = "Nubank Ultravioleta",
                status = "completed"
            ),
            TransactionEntity(
                id = "4",
                date = "2026-03-20",
                description = "Conta de Luz",
                amount = 500.0,
                type = "expense",
                category = "Moradia",
                account = "Nubank Conta",
                status = "pending"
            )
        )

        val totalIncome = transactions.filter { it.type == "income" }.sumOf { it.amount }
        val totalExpense = transactions.filter { it.type == "expense" }.sumOf { it.amount }
        val pendingExpense = transactions.filter { it.type == "expense" && it.status == "pending" }.sumOf { it.amount }
        val balance = totalIncome - totalExpense
        val savingsRate = if (totalIncome > 0) Math.max(0, Math.min(100, Math.round(((totalIncome - totalExpense) / totalIncome) * 100).toInt())) else 0

        val summary = MonthSummary(
            month = "Março",
            totalIncome = totalIncome,
            totalExpense = totalExpense,
            balance = balance,
            savingsRate = savingsRate,
            pendingExpenses = pendingExpense,
            pendingIncomes = 0.0
        )

        assertEquals(10000.0, summary.totalIncome, 0.001)
        assertEquals(4500.0, summary.totalExpense, 0.001)
        assertEquals(5500.0, summary.balance, 0.001)
        assertEquals(55, summary.savingsRate)
        assertEquals(500.0, summary.pendingExpenses, 0.001)
    }

    @Test
    fun testInstallmentDateProgression() {
        val startDateStr = "2026-03-15"
        val totalInstallments = 5
        val startInstallment = 1
        val parsedDate = LocalDate.parse(startDateStr, DateTimeFormatter.ISO_DATE)

        val dates = (startInstallment..totalInstallments).mapIndexed { index, instNum ->
            val instDate = parsedDate.plusMonths(index.toLong())
            instNum to instDate.format(DateTimeFormatter.ISO_DATE)
        }

        assertEquals(5, dates.size)
        assertEquals(1 to "2026-03-15", dates[0])
        assertEquals(2 to "2026-04-15", dates[1])
        assertEquals(3 to "2026-05-15", dates[2])
        assertEquals(4 to "2026-06-15", dates[3])
        assertEquals(5 to "2026-07-15", dates[4])
    }

    @Test
    fun testRecurringScopeCalculation() {
        val startDateStr = "2026-09-10"
        val parsedDate = LocalDate.parse(startDateStr, DateTimeFormatter.ISO_DATE)
        val startMonth = parsedDate.monthValue // 9
        val remainingInYear = 12 - startMonth + 1 // 4 months (Sep, Oct, Nov, Dec)

        assertEquals(4, remainingInYear)
    }
}
