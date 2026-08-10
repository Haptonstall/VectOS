package com.lz.ui.theme

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

private val DarkColorScheme = darkColorScheme(
    primary = VectosPrimary,
    onPrimary = VectosOnPrimary,
    primaryContainer = VectosPrimaryContainer,
    onPrimaryContainer = VectosOnPrimaryContainer,
    secondary = VectosSecondary,
    onSecondary = VectosOnSecondary,
    secondaryContainer = VectosSecondaryContainer,
    onSecondaryContainer = VectosOnSecondaryContainer,
    tertiary = VectosTertiary,
    onTertiary = VectosOnTertiary,
    background = Color(0xFF09090B),
    onBackground = Color.White,
    surface = Color(0xFF09090B),
    onSurface = Color.White,
    surfaceVariant = Color(0xFF27272A),
    onSurfaceVariant = Color(0xFFD4D4D8),
    outline = Color(0xFF52525B),
    error = VectosError,
    onError = VectosOnError,
    errorContainer = VectosErrorContainer,
    onErrorContainer = VectosOnErrorContainer
)

private val LightColorScheme = lightColorScheme(
    primary = VectosPrimary,
    onPrimary = VectosOnPrimary,
    primaryContainer = VectosPrimaryContainer,
    onPrimaryContainer = VectosOnPrimaryContainer,
    secondary = VectosSecondary,
    onSecondary = VectosOnSecondary,
    secondaryContainer = VectosSecondaryContainer,
    onSecondaryContainer = VectosOnSecondaryContainer,
    tertiary = VectosTertiary,
    onTertiary = VectosOnTertiary,
    background = VectosBackground,
    onBackground = VectosOnBackground,
    surface = VectosSurface,
    onSurface = VectosOnSurface,
    surfaceVariant = VectosSurfaceVariant,
    onSurfaceVariant = VectosOnSurfaceVariant,
    outline = VectosOutline,
    error = VectosError,
    onError = VectosOnError,
    errorContainer = VectosErrorContainer,
    onErrorContainer = VectosOnErrorContainer
)

@Composable
fun VectOSTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
