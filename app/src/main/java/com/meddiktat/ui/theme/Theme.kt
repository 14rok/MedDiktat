package com.meddiktat.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColors = darkColorScheme(
    primary = Teal80,
    secondary = TealGrey80,
    tertiary = Amber80,
)

private val LightColors = lightColorScheme(
    primary = Teal40,
    secondary = TealGrey40,
    tertiary = Amber40,
)

/**
 * App-Theme mit Dark-Mode-Unterstützung. Auf Android 12+ werden optional die
 * dynamischen Systemfarben genutzt, sonst die definierte Teal-Palette.
 */
@Composable
fun MedDiktatTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColors
        else -> LightColors
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content,
    )
}
