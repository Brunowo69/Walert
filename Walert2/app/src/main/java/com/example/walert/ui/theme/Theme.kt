package com.example.walert.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

// Paleta por defecto (Claro)
private val LightColorScheme = lightColorScheme(
    primary = AppPrimary,
    secondary = AppSecondary,
    tertiary = AppTertiary,
    background = AppBackground,
    surface = AppSurface,
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onTertiary = Color.Black,
    onBackground = TextOnBackground,
    onSurface = TextOnBackground,
    primaryContainer = Color(0xFF8F70FA),
    secondaryContainer = SecondaryContainer,
    surfaceVariant = SurfaceVariant,
    onSurfaceVariant = OnSurfaceVariant
)

// Paleta por defecto (Oscuro)
private val DarkColorScheme = darkColorScheme(
    primary = AppPrimaryDark,
    secondary = AppSecondaryDark,
    tertiary = AppTertiaryDark,
    background = AppBackgroundDark,
    surface = AppSurfaceDark,
    onPrimary = Color.Black,
    onSecondary = Color.Black,
    onTertiary = Color.Black,
    onBackground = TextOnBackgroundDark,
    onSurface = TextOnBackgroundDark,
    secondaryContainer = SecondaryContainerDark,
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = OnSurfaceVariantDark
)

// --- Nuevas Paletas de Colores ---

// Paleta Tema Bosque (Claro)
private val ForestLightColorScheme = lightColorScheme(
    primary = Color(0xFF386A20),
    secondary = Color(0xFF52634F),
    tertiary = Color(0xFF386666),
    background = Color(0xFFFBFDF8),
    surface = Color(0xFFFBFDF8)
)

// Paleta Tema Bosque (Oscuro)
private val ForestDarkColorScheme = darkColorScheme(
    primary = Color(0xFFB5CC9F),
    secondary = Color(0xFFB9CBAA),
    tertiary = Color(0xFFA0D0D0),
    background = Color(0xFF1A1C19),
    surface = Color(0xFF1A1C19)
)

// Paleta Tema Océano (Claro)
private val OceanLightColorScheme = lightColorScheme(
    primary = Color(0xFF00639B),
    secondary = Color(0xFF50606E),
    tertiary = Color(0xFF645A7D),
    background = Color(0xFFF7F9FF),
    surface = Color(0xFFF7F9FF)
)

// Paleta Tema Océano (Oscuro)
private val OceanDarkColorScheme = darkColorScheme(
    primary = Color(0xFF9FCAFF),
    secondary = Color(0xFFB8C8DA),
    tertiary = Color(0xFFCEC2E8),
    background = Color(0xFF101418),
    surface = Color(0xFF101418)
)


@Composable
fun WalertTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    themeName: String = "default", // Nuevo parámetro para el nombre del tema
    dynamicColor: Boolean = false, // Lo mantenemos deshabilitado
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        else -> when (themeName) {
            "theme_forest" -> if (darkTheme) ForestDarkColorScheme else ForestLightColorScheme
            "theme_ocean" -> if (darkTheme) OceanDarkColorScheme else OceanLightColorScheme
            else -> if (darkTheme) DarkColorScheme else LightColorScheme // Tema por defecto
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
