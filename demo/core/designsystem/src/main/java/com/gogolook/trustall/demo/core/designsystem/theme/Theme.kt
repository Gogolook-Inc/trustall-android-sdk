package com.gogolook.trustall.demo.core.designsystem.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = BluePrimary,
    onPrimary = White,
    primaryContainer = BluePrimaryContainer,
    onPrimaryContainer = Black,
    secondary = BlueDark,
    onSecondary = White,
    secondaryContainer = BlueDarkContainer,
    onSecondaryContainer = Black,
    tertiary = GreenAccent,
    onTertiary = Black,
    tertiaryContainer = GreenAccentContainer,
    onTertiaryContainer = Color(0xFF00210E), // Very dark green
    error = RedError,
    errorContainer = RedErrorContainer,
    onError = White,
    onErrorContainer = Color(0xFF410002), // Very dark red
    background = White,
    onBackground = Black,
    surface = White,
    onSurface = Black,
    surfaceVariant = GreySurfaceVariant,
    onSurfaceVariant = Color(0xFF44474F), // Dark grey
    outline = GreyOutline
)

@Composable
fun AppTheme(
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
        // Force light scheme for now to match user request explicitly unless they defined a dark palette
        // darkTheme -> DarkColorScheme 
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = MaterialTheme.typography,
        content = content
    )
}
