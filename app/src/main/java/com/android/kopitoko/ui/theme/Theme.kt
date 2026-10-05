package com.android.kopitoko.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val LightColorScheme = lightColorScheme(
    primary = CoffeeLightPrimary,
    onPrimary = CoffeeLightOnPrimary,
    primaryContainer = CoffeeLightPrimaryContainer,
    onPrimaryContainer = CoffeeLightOnPrimaryContainer,
    secondary = CoffeeLightSecondary,
    onSecondary = CoffeeLightOnSecondary,
    secondaryContainer = CoffeeLightSecondaryContainer,
    onSecondaryContainer = CoffeeLightOnSecondaryContainer,
    tertiary = CoffeeLightTertiary,
    onTertiary = CoffeeLightOnTertiary,
    tertiaryContainer = CoffeeLightTertiaryContainer,
    onTertiaryContainer = CoffeeLightOnTertiaryContainer,
    background = CoffeeLightBackground,
    onBackground = CoffeeLightOnBackground,
    surface = CoffeeLightSurface,
    onSurface = CoffeeLightOnSurface
)

private val DarkColorScheme = darkColorScheme(
    primary = CoffeeDarkPrimary,
    onPrimary = CoffeeDarkOnPrimary,
    primaryContainer = CoffeeDarkPrimaryContainer,
    onPrimaryContainer = CoffeeDarkOnPrimaryContainer,
    secondary = CoffeeDarkSecondary,
    onSecondary = CoffeeDarkOnSecondary,
    secondaryContainer = CoffeeDarkSecondaryContainer,
    onSecondaryContainer = CoffeeDarkOnSecondaryContainer,
    tertiary = CoffeeDarkTertiary,
    onTertiary = CoffeeDarkOnTertiary,
    tertiaryContainer = CoffeeDarkTertiaryContainer,
    onTertiaryContainer = CoffeeDarkOnTertiaryContainer,
    background = CoffeeDarkBackground,
    onBackground = CoffeeDarkOnBackground,
    surface = CoffeeDarkSurface,
    onSurface = CoffeeDarkOnSurface
)

@Composable
fun KopitokoTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
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
