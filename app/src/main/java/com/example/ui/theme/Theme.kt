package com.example.ui.theme

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

private val DarkColorScheme =
  darkColorScheme(
    primary = CosmicGoldAmber,
    onPrimary = CosmicBlack,
    primaryContainer = CosmicGoldAmberContainer,
    onPrimaryContainer = CosmicGoldAmber,
    secondary = CosmicElectricCyan,
    onSecondary = CosmicBlack,
    secondaryContainer = CosmicElectricCyanContainer,
    onSecondaryContainer = CosmicElectricCyan,
    tertiary = CosmicNebulaMagenta,
    onTertiary = CosmicBlack,
    tertiaryContainer = Color(0x33D500F9),
    onTertiaryContainer = CosmicNebulaViolet,
    background = CosmicBlack,
    onBackground = CosmicTextWhite,
    surface = CosmicSurfaceGlass,
    onSurface = CosmicTextWhite,
    surfaceVariant = CosmicSurfaceVariant,
    onSurfaceVariant = CosmicTextSubtle,
    outline = Color(0x55FFB300),
    outlineVariant = Color(0x4000E5FF),
  )

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF00668B),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFC7E7FF),
    onPrimaryContainer = Color(0xFF001E2E),
    secondary = Color(0xFF4F616E),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFD2E5F5),
    onSecondaryContainer = Color(0xFF0B1D29),
    tertiary = Color(0xFF63597C),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFE9DDFF),
    onTertiaryContainer = Color(0xFF1F1635),
    background = Color(0xFFFBFDFE),
    onBackground = Color(0xFF191C1E),
    surface = Color(0xFFFBFDFE),
    onSurface = Color(0xFF191C1E),
    surfaceVariant = Color(0xFFDDE3EA),
    onSurfaceVariant = Color(0xFF41484D),
    outline = Color(0xFF71787E),
    outlineVariant = Color(0xFFC1C7CE)
)


@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  // We prefer the custom crafted Professional Polish palette by default for consistency
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  val colorScheme =
    when {
      dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
        val context = LocalContext.current
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
      }

      darkTheme -> DarkColorScheme
      else -> LightColorScheme
    }

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}

