package com.example.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = Emerald500,
    onPrimary = Slate950,
    primaryContainer = Emerald900,
    onPrimaryContainer = Emerald100,
    secondary = Indigo500,
    onSecondary = Color.White,
    secondaryContainer = Slate800,
    onSecondaryContainer = Slate200,
    tertiary = Amber500,
    background = Slate950,
    onBackground = Slate100,
    surface = Slate900,
    onSurface = Slate100,
    surfaceVariant = Slate800,
    onSurfaceVariant = Slate300,
    outline = Slate700,
    error = Rose500,
    onError = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = Emerald600,
    onPrimary = Color.White,
    primaryContainer = Emerald50,
    onPrimaryContainer = Emerald900,
    secondary = Indigo600,
    onSecondary = Color.White,
    secondaryContainer = Slate100,
    onSecondaryContainer = Slate800,
    tertiary = Amber600,
    background = Slate50,
    onBackground = Slate900,
    surface = Color.White,
    onSurface = Slate900,
    surfaceVariant = Slate100,
    onSurfaceVariant = Slate700,
    outline = Slate200,
    error = Rose600,
    onError = Color.White
)

enum class AppThemeMode(val title: String) {
    CLASSIC("Clássico"),
    SEPIA("Sépia"),
    DARK("Dark")
}

private val SepiaColorScheme = lightColorScheme(
    primary = SepiaPrimary,
    onPrimary = Color.White,
    primaryContainer = SepiaPrimaryContainer,
    onPrimaryContainer = SepiaOnPrimaryContainer,
    secondary = SepiaSecondary,
    onSecondary = Color.White,
    secondaryContainer = SepiaSecondaryContainer,
    onSecondaryContainer = SepiaOnBackground,
    tertiary = SepiaTertiary,
    background = SepiaBackground,
    onBackground = SepiaOnBackground,
    surface = SepiaSurface,
    onSurface = SepiaOnSurface,
    surfaceVariant = SepiaSurfaceVariant,
    onSurfaceVariant = SepiaOnSurfaceVariant,
    outline = SepiaOutline,
    error = Rose600,
    onError = Color.White
)

@Composable
fun ControleFinanceiroTheme(
    themeMode: AppThemeMode = AppThemeMode.CLASSIC,
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val isDark = themeMode == AppThemeMode.DARK || (themeMode == AppThemeMode.CLASSIC && darkTheme)
    val colorScheme = when (themeMode) {
        AppThemeMode.DARK -> DarkColorScheme
        AppThemeMode.SEPIA -> SepiaColorScheme
        AppThemeMode.CLASSIC -> if (darkTheme) DarkColorScheme else LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = Color.Transparent.toArgb()
            window.navigationBarColor = Color.Transparent.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !isDark
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = !isDark
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography(),
        content = content
    )
}

