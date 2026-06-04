package com.example.todolist.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = Blurple,
    onPrimary = FullWhite,
    secondary = Greyple,
    background = NotQuiteBlack,
    surface = DarkNotBlack,
    onBackground = FullWhite,
    onSurface = FullWhite,
    error = DestructiveRed,
    onError = FullWhite
)

// Define the new LightColorScheme
private val LightColorScheme = lightColorScheme(
    primary = Blurple,
    onPrimary = FullWhite,
    secondary = Greyple,
    background = LightBackground,
    surface = LightSurface,
    onBackground = LightOnText,
    onSurface = LightOnText,
    error = DestructiveRed,
    onError = FullWhite
)

@Composable
fun TodoListTheme(
    // Allow the theme to be controlled externally
    darkTheme: Boolean,
    content: @Composable () -> Unit
) {
    // Select the color scheme based on the parameter
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb() // Changed to primary for better look
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}