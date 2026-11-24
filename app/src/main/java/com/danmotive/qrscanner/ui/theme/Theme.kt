
package com.danmotive.qrscanner.ui.theme

import android.os.Build
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.danmotive.qrscanner.data.ThemeMode

private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80
)

private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40
)

private val ThemeColorAnimation = tween<Color>(durationMillis = 240)

@Composable
private fun animateThemeColor(target: Color): Color =
    animateColorAsState(
        targetValue = target,
        animationSpec = ThemeColorAnimation
    ).value

@Composable
private fun animatedColorScheme(target: ColorScheme): ColorScheme = target.copy(
    // Анимируем только реально используемые роли: меньше работы на кадр.
    primary = animateThemeColor(target.primary),
    onPrimary = animateThemeColor(target.onPrimary),
    primaryContainer = animateThemeColor(target.primaryContainer),
    onPrimaryContainer = animateThemeColor(target.onPrimaryContainer),
    secondary = animateThemeColor(target.secondary),
    onSecondary = animateThemeColor(target.onSecondary),
    secondaryContainer = animateThemeColor(target.secondaryContainer),
    onSecondaryContainer = animateThemeColor(target.onSecondaryContainer),
    background = animateThemeColor(target.background),
    onBackground = animateThemeColor(target.onBackground),
    surface = animateThemeColor(target.surface),
    onSurface = animateThemeColor(target.onSurface),
    surfaceVariant = animateThemeColor(target.surfaceVariant),
    onSurfaceVariant = animateThemeColor(target.onSurfaceVariant),
    surfaceTint = animateThemeColor(target.surfaceTint),
    inverseSurface = animateThemeColor(target.inverseSurface),
    inverseOnSurface = animateThemeColor(target.inverseOnSurface),
    outline = animateThemeColor(target.outline),
    outlineVariant = animateThemeColor(target.outlineVariant),
    scrim = animateThemeColor(target.scrim),
    surfaceBright = animateThemeColor(target.surfaceBright),
    surfaceDim = animateThemeColor(target.surfaceDim),
    surfaceContainer = animateThemeColor(target.surfaceContainer),
    surfaceContainerHigh = animateThemeColor(target.surfaceContainerHigh),
    surfaceContainerHighest = animateThemeColor(target.surfaceContainerHighest),
    surfaceContainerLow = animateThemeColor(target.surfaceContainerLow),
    surfaceContainerLowest = animateThemeColor(target.surfaceContainerLowest)
)

@Composable
fun QRScannerTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val darkTheme = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }

    val context = LocalContext.current
    val dynamicDark = remember(context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            dynamicDarkColorScheme(context)
        } else {
            DarkColorScheme
        }
    }
    val dynamicLight = remember(context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            dynamicLightColorScheme(context)
        } else {
            LightColorScheme
        }
    }

    val targetColorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S ->
            if (darkTheme) dynamicDark else dynamicLight

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }
    val colorScheme = animatedColorScheme(targetColorScheme)

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
