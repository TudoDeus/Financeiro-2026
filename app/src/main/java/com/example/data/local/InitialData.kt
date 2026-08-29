package com.example.data.local

import com.example.data.model.AccountEntity
import com.example.data.model.CategoryEntity
import com.example.data.model.CreditCardEntity
import com.example.data.model.GoalEntity
import com.example.data.model.TransactionEntity

object InitialData {

    val MONTH_NAMES = listOf(
        "Janeiro", "Fevereiro", "Março", "Abril", "Maio", "Junho",
        "Julho", "Agosto", "Setembro", "Outubro", "Novembro", "Dezembro"
    )

    val INITIAL_CATEGORIES = listOf(
        // Despesas (Exatas solicitadas: Casa, Assinaturas, Carro, Alimentação/Mercado, Academia, Terreno, Lazer)
        CategoryEntity(id = "cat-casa", name = "Casa", type = "expense", icon = "Home", color = "#3B82F6", monthlyBudget = 2200.0),
        CategoryEntity(id = "cat-assinaturas", name = "Assinaturas", type = "expense", icon = "FileText", color = "#8B5CF6", monthlyBudget = 350.0),
        CategoryEntity(id = "cat-carro", name = "Carro", type = "expense", icon = "DirectionsCar", color = "#F59E0B", monthlyBudget = 700.0),
        CategoryEntity(id = "cat-alimentacao-mercado", name = "Alimentação/Mercado", type = "expense", icon = "ShoppingCart", color = "#10B981", monthlyBudget = 1600.0),
        CategoryEntity(id = "cat-academia", name = "Academia", type = "expense", icon = "FitnessCenter", color = "#EC4899", monthlyBudget = 180.0),
        CategoryEntity(id = "cat-terreno", name = "Terreno", type = "expense", icon = "Landscape", color = "#14B8A6", monthlyBudget = 1200.0),
        CategoryEntity(id = "cat-lazer", name = "Lazer", type = "expense", icon = "Sparkles", color = "#F43F5E", monthlyBudget = 600.0),

        // Receitas
        CategoryEntity(id = "cat-salario", name = "Salário", type = "income", icon = "AccountBalanceWallet", color = "#10B981", monthlyBudget = 7500.0),
        CategoryEntity(id = "cat-rec-terreno", name = "Terreno", type = "income", icon = "Landscape", color = "#14B8A6", monthlyBudget = 2500.0),
        CategoryEntity(id = "cat-rendimentos", name = "Rendimentos", type = "income", icon = "TrendingUp", color = "#8B5CF6", monthlyBudget = 500.0),
        CategoryEntity(id = "cat-outras-rec", name = "Outras Receitas", type = "income", icon = "AddCircle", color = "#64748B", monthlyBudget = 300.0)
    )

    val INITIAL_ACCOUNTS = listOf(
        AccountEntity(id = "acc-nubank", name = "Nubank Conta", type = "checking", institution = "Nubank", balance = 4850.50, color = "#820AD1"),
        AccountEntity(id = "acc-itau", name = "Itaú Personalité", type = "checking", institution = "Itaú", balance = 12400.00, color = "#EC7000"),
        AccountEntity(id = "acc-inter", name = "Inter Investimentos", type = "investment", institution = "Inter", balance = 28950.00, color = "#FF7A00"),
        AccountEntity(id = "acc-carteira", name = "Dinheiro em Espécie", type = "cash", institution = "Carteira Física", balance = 350.00, color = "#10B981")
    )

    val INITIAL_CREDIT_CARDS = listOf(
        CreditCardEntity(id = "card-nubank-uv", name = "Nubank Ultravioleta", institution = "Nubank", limit = 15000.0, currentInvoice = 3240.80, closingDay = 18, dueDay = 25, color = "#820AD1"),
        CreditCardEntity(id = "card-itau-black", name = "Itaú Mastercard Black", institution = "Itaú", limit = 25000.0, currentInvoice = 1890.00, closingDay = 5, dueDay = 12, color = "#1E293B")
    )

    val INITIAL_GOALS = listOf(
        GoalEntity(id = "goal-1", title = "Reserva de Emergência (6 Meses)", targetAmount = 30000.0, currentAmount = 22500.0, deadline = "2026-12-31", category = "Segurança", color = "#10B981", notes = "Manter em CDB liquidez diária"),
        GoalEntity(id = "goal-2", title = "Viagem de Férias 2026", targetAmount = 12000.0, currentAmount = 6800.0, deadline = "2026-11-15", category = "Sonhos", color = "#3B82F6", notes = "Passagens e hospedagem"),
        GoalEntity(id = "goal-3", title = "Aporte Terreno & Obras", targetAmount = 40000.0, currentAmount = 18400.0, deadline = "2026-12-31", category = "Investimentos", color = "#14B8A6", notes = "Construção e infraestrutura do lote")
    )

    val INITIAL_TRANSACTIONS = listOf(
        // Receitas Agosto 2026
        TransactionEntity(id = "t-1", date = "2026-08-05", description = "Salário Mensal CLT", amount = 7500.00, type = "income", category = "Salário", account = "Itaú Personalité", status = "completed"),
        TransactionEntity(id = "t-2", date = "2026-08-10", description = "Venda/Locação Lote Terreno", amount = 2200.00, type = "income", category = "Terreno", account = "Nubank Conta", status = "completed"),
        TransactionEntity(id = "t-3", date = "2026-08-15", description = "Dividendos FIIs & Ações", amount = 385.40, type = "income", category = "Rendimentos", account = "Inter Investimentos", status = "completed"),

        // Despesas Agosto 2026
        TransactionEntity(id = "t-4", date = "2026-08-06", description = "Aluguel & Condomínio", amount = 2150.00, type = "expense", category = "Casa", account = "Itaú Personalité", status = "completed", isRecurring = true),
        TransactionEntity(id = "t-5", date = "2026-08-07", description = "Supermercado Pão de Açúcar", amount = 684.20, type = "expense", category = "Alimentação/Mercado", account = "Nubank Ultravioleta", status = "completed"),
        TransactionEntity(id = "t-6", date = "2026-08-09", description = "Combustível Posto Shell", amount = 230.00, type = "expense", category = "Carro", account = "Nubank Ultravioleta", status = "completed"),
        TransactionEntity(id = "t-7", date = "2026-08-11", description = "Mensalidade Smart Fit", amount = 149.90, type = "expense", category = "Academia", account = "Nubank Ultravioleta", status = "completed", isRecurring = true),
        TransactionEntity(id = "t-8", date = "2026-08-13", description = "Parcela Loteamento Terreno (08/36)", amount = 980.00, type = "expense", category = "Terreno", account = "Itaú Personalité", status = "completed", isInstallment = true, installmentCurrent = 8, installmentTotal = 36, installmentGroupId = "inst-terreno"),
        TransactionEntity(id = "t-9", date = "2026-08-14", description = "Jantar Restaurante Italiano", amount = 215.00, type = "expense", category = "Lazer", account = "Nubank Ultravioleta", status = "completed"),
        TransactionEntity(id = "t-10", date = "2026-08-16", description = "Netflix + Spotify + Prime", amount = 95.80, type = "expense", category = "Assinaturas", account = "Nubank Ultravioleta", status = "completed", isRecurring = true),
        TransactionEntity(id = "t-11", date = "2026-08-18", description = "Seguro Auto & Manutenção", amount = 320.00, type = "expense", category = "Carro", account = "Nubank Ultravioleta", status = "completed"),
        TransactionEntity(id = "t-12", date = "2026-08-20", description = "Feira Orgânica e Hortifruti", amount = 185.00, type = "expense", category = "Alimentação/Mercado", account = "Nubank Ultravioleta", status = "completed"),
        TransactionEntity(id = "t-13", date = "2026-08-22", description = "Manutenção & Limpeza Terreno", amount = 250.00, type = "expense", category = "Terreno", account = "Nubank Conta", status = "completed"),
        TransactionEntity(id = "t-14", date = "2026-08-27", description = "Conta de Energia Enel", amount = 210.00, type = "expense", category = "Casa", account = "Itaú Personalité", status = "pending", isRecurring = true),

        // Julho 2026
        TransactionEntity(id = "t-15", date = "2026-07-05", description = "Salário Mensal CLT", amount = 7500.00, type = "income", category = "Salário", account = "Itaú Personalité", status = "completed"),
        TransactionEntity(id = "t-16", date = "2026-07-06", description = "Aluguel & Condomínio", amount = 2150.00, type = "expense", category = "Casa", account = "Itaú Personalité", status = "completed"),
        TransactionEntity(id = "t-17", date = "2026-07-10", description = "Supermercado Mensal", amount = 1350.00, type = "expense", category = "Alimentação/Mercado", account = "Nubank Ultravioleta", status = "completed"),
        TransactionEntity(id = "t-18", date = "2026-07-12", description = "Parcela Loteamento Terreno (07/36)", amount = 980.00, type = "expense", category = "Terreno", account = "Itaú Personalité", status = "completed", isInstallment = true, installmentCurrent = 7, installmentTotal = 36, installmentGroupId = "inst-terreno"),
        TransactionEntity(id = "t-19", date = "2026-07-15", description = "Rendimento Aluguel Terreno", amount = 1500.00, type = "income", category = "Terreno", account = "Nubank Conta", status = "completed"),
        TransactionEntity(id = "t-20", date = "2026-07-20", description = "Passeio e Viagem Final de Semana", amount = 890.00, type = "expense", category = "Lazer", account = "Nubank Ultravioleta", status = "completed")
    )
}
