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
        // Despesas
        CategoryEntity(id = "cat-moradia", name = "Moradia & Aluguel", type = "expense", icon = "Home", color = "#3B82F6", monthlyBudget = 2200.0),
        CategoryEntity(id = "cat-alimentacao", name = "Alimentação & Supermercado", type = "expense", icon = "Utensils", color = "#10B981", monthlyBudget = 1500.0),
        CategoryEntity(id = "cat-transporte", name = "Transporte & Combustível", type = "expense", icon = "Car", color = "#F59E0B", monthlyBudget = 650.0),
        CategoryEntity(id = "cat-saude", name = "Saúde & Farmácia", type = "expense", icon = "HeartPulse", color = "#EF4444", monthlyBudget = 450.0),
        CategoryEntity(id = "cat-educacao", name = "Educação & Cursos", type = "expense", icon = "GraduationCap", color = "#8B5CF6", monthlyBudget = 400.0),
        CategoryEntity(id = "cat-lazer", name = "Lazer & Restaurantes", type = "expense", icon = "Sparkles", color = "#EC4899", monthlyBudget = 600.0),
        CategoryEntity(id = "cat-compras", name = "Compras & Vestuário", type = "expense", icon = "ShoppingBag", color = "#06B6D4", monthlyBudget = 350.0),
        CategoryEntity(id = "cat-servicos", name = "Contas & Assinaturas", type = "expense", icon = "FileText", color = "#64748B", monthlyBudget = 320.0),

        // Receitas
        CategoryEntity(id = "cat-salario", name = "Salário Principal", type = "income", icon = "Briefcase", color = "#10B981", monthlyBudget = 7500.0),
        CategoryEntity(id = "cat-freelance", name = "Projetos & Freelance", type = "income", icon = "Laptop", color = "#3B82F6", monthlyBudget = 2000.0),
        CategoryEntity(id = "cat-rendimentos", name = "Rendimentos & Dividendos", type = "income", icon = "TrendingUp", color = "#8B5CF6", monthlyBudget = 450.0),
        CategoryEntity(id = "cat-outras-rec", name = "Outras Receitas", type = "income", icon = "PlusCircle", color = "#64748B", monthlyBudget = 200.0)
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
        GoalEntity(id = "goal-3", title = "Aporte Carteira de Ações/FIIs", targetAmount = 20000.0, currentAmount = 11400.0, deadline = "2026-12-31", category = "Investimentos", color = "#8B5CF6", notes = "Dividendos com foco em renda passiva")
    )

    val INITIAL_TRANSACTIONS = listOf(
        // Receitas Agosto 2026
        TransactionEntity(id = "t-1", date = "2026-08-05", description = "Salário Mensal", amount = 7500.00, type = "income", category = "Salário Principal", account = "Itaú Personalité", status = "completed"),
        TransactionEntity(id = "t-2", date = "2026-08-10", description = "Consultoria Desenvolvimento Web", amount = 2200.00, type = "income", category = "Projetos & Freelance", account = "Nubank Conta", status = "completed"),
        TransactionEntity(id = "t-3", date = "2026-08-15", description = "Dividendos FIIs & Ações", amount = 385.40, type = "income", category = "Rendimentos & Dividendos", account = "Inter Investimentos", status = "completed"),

        // Despesas Agosto 2026
        TransactionEntity(id = "t-4", date = "2026-08-06", description = "Aluguel & Condomínio", amount = 2150.00, type = "expense", category = "Moradia & Aluguel", account = "Itaú Personalité", status = "completed", isRecurring = true),
        TransactionEntity(id = "t-5", date = "2026-08-07", description = "Supermercado Pão de Açúcar", amount = 684.20, type = "expense", category = "Alimentação & Supermercado", account = "Nubank Ultravioleta", status = "completed"),
        TransactionEntity(id = "t-6", date = "2026-08-09", description = "Combustível Posto Shell", amount = 230.00, type = "expense", category = "Transporte & Combustível", account = "Nubank Ultravioleta", status = "completed"),
        TransactionEntity(id = "t-7", date = "2026-08-12", description = "Farmácia Drogasil (Medicamentos)", amount = 142.50, type = "expense", category = "Saúde & Farmácia", account = "Nubank Conta", status = "completed"),
        TransactionEntity(id = "t-8", date = "2026-08-14", description = "Jantar Restaurante Italiano", amount = 215.00, type = "expense", category = "Lazer & Restaurantes", account = "Nubank Ultravioleta", status = "completed"),
        TransactionEntity(id = "t-9", date = "2026-08-16", description = "Netflix Premium + Spotify Duo", amount = 95.80, type = "expense", category = "Contas & Assinaturas", account = "Nubank Ultravioleta", status = "completed", isRecurring = true, notes = "Assinatura fixa mensal no cartão"),
        TransactionEntity(id = "t-10", date = "2026-08-18", description = "Smartphone Galaxy S24 (03/10)", amount = 389.90, type = "expense", category = "Compras & Vestuário", account = "Nubank Ultravioleta", status = "completed", isInstallment = true, installmentCurrent = 3, installmentTotal = 10, installmentGroupId = "inst-galaxy-s24"),
        TransactionEntity(id = "t-11", date = "2026-08-20", description = "Feira Orgânica e Hortifruti", amount = 185.00, type = "expense", category = "Alimentação & Supermercado", account = "Nubank Ultravioleta", status = "completed"),
        TransactionEntity(id = "t-12", date = "2026-08-22", description = "Smart TV 4K Sala (02/06)", amount = 450.00, type = "expense", category = "Compras & Vestuário", account = "Itaú Mastercard Black", status = "completed", isInstallment = true, installmentCurrent = 2, installmentTotal = 6, installmentGroupId = "inst-smart-tv"),
        TransactionEntity(id = "t-12b", date = "2026-08-24", description = "Financiamento Imobiliário / Boleto (18/60)", amount = 890.00, type = "expense", category = "Moradia & Aluguel", account = "Itaú Personalité", status = "completed", isInstallment = true, installmentCurrent = 18, installmentTotal = 60, installmentGroupId = "inst-financiamento-imob", notes = "Parcela em débito bancário / boleto"),
        TransactionEntity(id = "t-13", date = "2026-08-27", description = "Fatura de Energia Enel (Agendada)", amount = 210.00, type = "expense", category = "Moradia & Aluguel", account = "Itaú Personalité", status = "pending", isRecurring = true),
        TransactionEntity(id = "t-14", date = "2026-08-29", description = "Plano de Saúde Familiar", amount = 550.00, type = "expense", category = "Saúde & Farmácia", account = "Itaú Mastercard Black", status = "pending", isRecurring = true),

        // Julho 2026
        TransactionEntity(id = "t-15", date = "2026-07-05", description = "Salário Mensal", amount = 7500.00, type = "income", category = "Salário Principal", account = "Itaú Personalité", status = "completed"),
        TransactionEntity(id = "t-16", date = "2026-07-06", description = "Aluguel & Condomínio", amount = 2150.00, type = "expense", category = "Moradia & Aluguel", account = "Itaú Personalité", status = "completed"),
        TransactionEntity(id = "t-17", date = "2026-07-10", description = "Supermercado e Compras", amount = 1350.00, type = "expense", category = "Alimentação & Supermercado", account = "Nubank Ultravioleta", status = "completed"),
        TransactionEntity(id = "t-18", date = "2026-07-15", description = "Freelance Design UI/UX", amount = 1800.00, type = "income", category = "Projetos & Freelance", account = "Nubank Conta", status = "completed"),
        TransactionEntity(id = "t-19", date = "2026-07-20", description = "Lazer e Viagem Final de Semana", amount = 890.00, type = "expense", category = "Lazer & Restaurantes", account = "Nubank Ultravioleta", status = "completed")
    )
}
