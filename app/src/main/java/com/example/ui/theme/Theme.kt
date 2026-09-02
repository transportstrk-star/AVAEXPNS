package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = EmeraldPrimaryDark,
    onPrimary = EmeraldContainerDark,
    primaryContainer = EmeraldContainerDark,
    onPrimaryContainer = EmeraldPrimaryDark,
    secondary = SlateSecondaryDark,
    onSecondary = SlateContainerDark,
    secondaryContainer = SlateContainerDark,
    onSecondaryContainer = SlateSecondaryDark,
    tertiary = GoldTertiaryDark,
    onTertiary = GoldContainerDark,
    tertiaryContainer = GoldContainerDark,
    onTertiaryContainer = GoldTertiaryDark,
    background = BackgroundDark,
    onBackground = OnBackgroundDark,
    surface = SurfaceDark,
    onSurface = OnSurfaceDark,
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = SlateSecondaryDark,
    outline = OutlineDark
)

private val LightColorScheme = lightColorScheme(
    primary = EmeraldPrimary,
    onPrimary = OnEmeraldPrimary,
    primaryContainer = EmeraldContainer,
    onPrimaryContainer = EmeraldPrimary,
    secondary = SlateSecondary,
    onSecondary = OnEmeraldPrimary,
    secondaryContainer = SlateContainer,
    onSecondaryContainer = SlateSecondary,
    tertiary = GoldTertiary,
    onTertiary = OnEmeraldPrimary,
    tertiaryContainer = GoldContainer,
    onTertiaryContainer = GoldTertiary,
    background = BackgroundLight,
    onBackground = OnBackgroundLight,
    surface = SurfaceLight,
    onSurface = OnSurfaceLight,
    surfaceVariant = SurfaceVariantLight,
    onSurfaceVariant = SlateSecondary,
    outline = OutlineLight
)

@Composable
fun MyApplicationTheme(
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

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
