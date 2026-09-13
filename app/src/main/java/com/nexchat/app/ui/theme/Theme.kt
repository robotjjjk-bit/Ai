package com.nexchat.app.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val LightScheme = lightColorScheme(
    primary = ColorTokens.PrimaryLight,
    surface = ColorTokens.CanvasLight,
    background = ColorTokens.CanvasLight,
)
private val DarkScheme = darkColorScheme(
    primary = ColorTokens.PrimaryDark,
    surface = ColorTokens.CanvasDark,
    background = ColorTokens.CanvasDark,
)

@Composable
fun NexChatTheme(
    darkTheme: Boolean? = null,
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val systemDark = isSystemInDarkTheme()
    val useDark = darkTheme ?: systemDark
    val ctx = LocalContext.current
    val scheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S ->
            if (useDark) dynamicDarkColorScheme(ctx) else dynamicLightColorScheme(ctx)
        useDark -> DarkScheme
        else -> LightScheme
    }
    MaterialTheme(colorScheme = scheme, typography = NexType, shapes = NexShapes, content = content)
}
