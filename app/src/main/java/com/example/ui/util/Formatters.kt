package com.example.ui.util

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.ui.theme.*
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

private val ptBrSymbols = DecimalFormatSymbols(Locale("pt", "BR")).apply {
    groupingSeparator = '.'
    decimalSeparator = ','
}

private val currencyFormatter = DecimalFormat("R$ #,##0.00", ptBrSymbols)
private val numberFormatter = DecimalFormat("#,##0.00", ptBrSymbols)

/**
 * Formats a Double amount to Brazilian Real format: "R$ 1.250,50"
 */
fun Double.toCurrency(includeSymbol: Boolean = true): String {
    return if (includeSymbol) {
        currencyFormatter.format(this)
    } else {
        numberFormatter.format(this)
    }
}

/**
 * Formats an ISO date string "YYYY-MM-DD" to "DD/MM/YYYY"
 */
fun String.toFormattedDate(): String {
    return try {
        val parsed = LocalDate.parse(this)
        parsed.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
    } catch (e: Exception) {
        val parts = this.split("-")
        if (parts.size == 3) "${parts[2]}/${parts[1]}/${parts[0]}" else this
    }
}

/**
 * Provides contextual icon and brand color based on the category name and transaction type.
 */
data class CategoryVisual(
    val icon: ImageVector,
    val color: Color,
    val containerColor: Color
)

fun getCategoryVisual(categoryName: String, type: String = "expense"): CategoryVisual {
    val norm = categoryName.lowercase(Locale.ROOT).trim()

    return when {
        type == "transfer" || norm.contains("transferência") || norm.contains("transferencia") -> {
            CategoryVisual(
                icon = Icons.Default.SwapHoriz,
                color = Color(0xFF4F46E5),
                containerColor = Color(0xFFEEF2FF)
            )
        }
        type == "income" || norm.contains("salário") || norm.contains("salario") || norm.contains("renda") -> {
            CategoryVisual(
                icon = Icons.Default.Payments,
                color = Emerald600,
                containerColor = Emerald50
            )
        }
        norm.contains("invest") || norm.contains("aplic") || norm.contains("rendimento") -> {
            CategoryVisual(
                icon = Icons.Default.TrendingUp,
                color = Color(0xFF0284C7),
                containerColor = Color(0xFFE0F2FE)
            )
        }
        norm.contains("casa") || norm.contains("moradia") || norm.contains("aluguel") || norm.contains("condom") -> {
            CategoryVisual(
                icon = Icons.Default.Home,
                color = Color(0xFF2563EB),
                containerColor = Color(0xFFEFF6FF)
            )
        }
        norm.contains("terreno") || norm.contains("lote") || norm.contains("obra") || norm.contains("constru") -> {
            CategoryVisual(
                icon = Icons.Default.Landscape,
                color = Color(0xFF059669),
                containerColor = Color(0xFFECFDF5)
            )
        }
        norm.contains("alimenta") || norm.contains("mercado") || norm.contains("supermercado") || norm.contains("comida") || norm.contains("restaurante") -> {
            CategoryVisual(
                icon = Icons.Default.ShoppingCart,
                color = Color(0xFFEA580C),
                containerColor = Color(0xFFFFF7ED)
            )
        }
        norm.contains("carro") || norm.contains("veículo") || norm.contains("veiculo") || norm.contains("transporte") || norm.contains("combustivel") || norm.contains("gasolina") || norm.contains("uber") -> {
            CategoryVisual(
                icon = Icons.Default.DirectionsCar,
                color = Amber600,
                containerColor = Color(0xFFFFFBEB)
            )
        }
        norm.contains("assinatura") || norm.contains("streaming") || norm.contains("netflix") || norm.contains("spotify") || norm.contains("internet") -> {
            CategoryVisual(
                icon = Icons.Default.Subscriptions,
                color = Color(0xFF7C3AED),
                containerColor = Color(0xFFF5F3FF)
            )
        }
        norm.contains("academia") || norm.contains("saúde") || norm.contains("saude") || norm.contains("farmacia") || norm.contains("fitness") || norm.contains("médic") || norm.contains("medic") -> {
            CategoryVisual(
                icon = Icons.Default.FitnessCenter,
                color = Rose600,
                containerColor = Rose50
            )
        }
        norm.contains("lazer") || norm.contains("viagem") || norm.contains("festa") || norm.contains("cinema") || norm.contains("jogo") || norm.contains("hobby") -> {
            CategoryVisual(
                icon = Icons.Default.SportsEsports,
                color = Color(0xFF0891B2),
                containerColor = Color(0xFFECFEFF)
            )
        }
        norm.contains("educa") || norm.contains("curso") || norm.contains("faculdade") || norm.contains("livro") -> {
            CategoryVisual(
                icon = Icons.Default.School,
                color = Color(0xFF9333EA),
                containerColor = Color(0xFFFAF5FF)
            )
        }
        norm.contains("roupa") || norm.contains("vestu") || norm.contains("compra") || norm.contains("shopping") -> {
            CategoryVisual(
                icon = Icons.Default.ShoppingBag,
                color = Color(0xFFDB2777),
                containerColor = Color(0xFFFDF2F8)
            )
        }
        else -> {
            if (type == "income") {
                CategoryVisual(
                    icon = Icons.Default.ArrowUpward,
                    color = Emerald600,
                    containerColor = Emerald50
                )
            } else {
                CategoryVisual(
                    icon = Icons.Default.ReceiptLong,
                    color = Color(0xFF64748B),
                    containerColor = Slate100
                )
            }
        }
    }
}
