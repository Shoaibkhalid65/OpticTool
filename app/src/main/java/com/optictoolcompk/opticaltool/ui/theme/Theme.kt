package com.optictoolcompk.opticaltool.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    // PRIMARY - Main brand color
    primary = Primary,
    onPrimary = TextOnPrimary,
    primaryContainer = PrimaryLight,
    onPrimaryContainer = PrimaryPressed,

    // SECONDARY - Supporting color
    secondary = Secondary,
    onSecondary = Color.White,
    secondaryContainer = Secondary.copy(alpha = 0.15f),
    onSecondaryContainer = PrimaryPressed,

    // TERTIARY - Accent color for highlights
    tertiary = Accent,
    onTertiary = Color.White,
    tertiaryContainer = Accent.copy(alpha = 0.15f),
    onTertiaryContainer = Primary,

    // BACKGROUND
    background = Background,
    onBackground = TextPrimary,

    // SURFACE
    surface = Surface,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceVariant,
    onSurfaceVariant = TextSecondary,
    surfaceTint = Primary,

    // INVERSE SURFACE (for snackbars, etc.)
    inverseSurface = PrimaryPressed,
    inverseOnSurface = Color.White,
    inversePrimary = PrimaryLight,

    // ERROR
    error = Error,
    onError = Color.White,
    errorContainer = Error.copy(alpha = 0.1f),
    onErrorContainer = Error,

    // OUTLINES & BORDERS
    outline = InputBorderIdle,
    outlineVariant = OutlineLight,

    // SCRIM (for dialogs/modals)
    scrim = Color.Black.copy(alpha = 0.32f)
)

private val DarkColorScheme = darkColorScheme(
    // PRIMARY
    primary = PrimaryLight,
    onPrimary = PrimaryPressed,
    primaryContainer = Primary,
    onPrimaryContainer = PrimaryLight,

    // SECONDARY
    secondary = Secondary.copy(alpha = 0.8f),
    onSecondary = Color.White,
    secondaryContainer = Secondary.copy(alpha = 0.3f),
    onSecondaryContainer = PrimaryLight,

    // TERTIARY
    tertiary = Accent.copy(alpha = 0.8f),
    onTertiary = Color.White,
    tertiaryContainer = Accent.copy(alpha = 0.3f),
    onTertiaryContainer = PrimaryLight,

    // BACKGROUND
    background = DarkBackground,
    onBackground = Color(0xFFE3F2FD),

    // SURFACE
    surface = DarkSurface,
    onSurface = Color(0xFFE3F2FD),
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = Color(0xFFB0BEC5),
    surfaceTint = PrimaryLight,

    // INVERSE SURFACE
    inverseSurface = Color(0xFFE3F2FD),
    inverseOnSurface = DarkSurface,
    inversePrimary = Primary,

    // ERROR
    error = Color(0xFFEF5350),
    onError = Color.White,
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),

    // OUTLINES & BORDERS
    outline = Color(0xFF4A6572),
    outlineVariant = Color(0xFF2C3E50),

    // SCRIM
    scrim = Color.Black.copy(alpha = 0.5f)
)

@Composable
fun OpticalToolTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Set to true if you want Material You dynamic colors
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S -> {
            val context = androidx.compose.ui.platform.LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}