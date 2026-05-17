package com.shaikh.storentry.presentation.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = Primary,
    secondary = Secondary,
    background = BackgroundLight,
    surface = SurfaceLight,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = OnBackgroundLight,
    onSurface = OnBackgroundLight,
    onSurfaceVariant = OnSurfaceVariant,
    error = ErrorColor,
    errorContainer = ErrorContainer,
    outline = OutlineColor
)

private val DarkColorScheme = darkColorScheme(
    primary = Primary,
    secondary = Secondary,
    background = BackgroundDark,
    surface = SurfaceDark,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = OnBackgroundDark,
    onSurface = OnBackgroundDark
)

/**
 * App theme.
 * Skill: edge-to-edge — DO NOT set statusBarColor here.
 * enableEdgeToEdge() from ComponentActivity handles status/nav icon tint automatically.
 * We only set isAppearanceLightNavigationBars to match theme for WindowCompat mode.
 */
@Composable
fun AppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
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

    // Skill: edge-to-edge — set system bar icon appearance to match theme.
    // We use WindowCompat (not enableEdgeToEdge) here only for icon tint.
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window ?: return@SideEffect
            val controller = WindowCompat.getInsetsController(window, view)
            // Light status bar icons for light theme, dark icons for dark theme
            controller.isAppearanceLightStatusBars = !darkTheme
            controller.isAppearanceLightNavigationBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
