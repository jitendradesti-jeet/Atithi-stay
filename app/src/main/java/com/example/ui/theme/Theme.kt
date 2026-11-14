package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

private val DarkColorScheme =
  darkColorScheme(
    primary = TerracottaDark,
    onPrimary = Color(0xFF5D1800),
    primaryContainer = TerracottaContainerDark,
    onPrimaryContainer = TerracottaContainerLight,
    secondary = SaffronDark,
    onSecondary = Color(0xFF472A00),
    secondaryContainer = SaffronContainerDark,
    onSecondaryContainer = SaffronContainerLight,
    tertiary = HeritageTealDark,
    onTertiary = Color(0xFF00363B),
    tertiaryContainer = HeritageTealContainerDark,
    onTertiaryContainer = HeritageTealContainerLight,
    background = ObsidianBackground,
    onBackground = CreamOnDark,
    surface = ObsidianSurface,
    onSurface = CreamOnDark,
    surfaceVariant = ObsidianCardDark,
    onSurfaceVariant = MutedSandOnDark,
    outline = Color(0xFF4D423B),
    outlineVariant = Color(0xFF382F2A),
  )

private val LightColorScheme =
  lightColorScheme(
    primary = TerracottaPrimary,
    onPrimary = Color.White,
    primaryContainer = TerracottaContainerLight,
    onPrimaryContainer = Color(0xFF3D0C00),
    secondary = SaffronSecondary,
    onSecondary = Color.White,
    secondaryContainer = SaffronContainerLight,
    onSecondaryContainer = Color(0xFF2E1900),
    tertiary = HeritageTealTertiary,
    onTertiary = Color.White,
    tertiaryContainer = HeritageTealContainerLight,
    onTertiaryContainer = Color(0xFF062023),
    background = WarmIvoryBackground,
    onBackground = DeepEspressoOnLight,
    surface = WarmIvoryBackground,
    onSurface = DeepEspressoOnLight,
    surfaceVariant = WarmSandSurface,
    onSurfaceVariant = MutedTaupeOnLight,
    surfaceContainerLow = WarmCardLight,
    surfaceContainer = WarmSandSurface,
    outline = Color(0xFFD6CBC0),
    outlineVariant = Color(0xFFE7DDD3),
  )

val AppShapes =
  Shapes(
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(28.dp),
  )

@Composable
fun AtithiStayTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
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

  MaterialTheme(
    colorScheme = colorScheme,
    typography = Typography,
    shapes = AppShapes,
    content = content,
  )
}
