package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme =
  darkColorScheme(
    primary = MoonlitGlow,
    secondary = MoonlightBlue,
    tertiary = MoonlightSilver,
    background = DeepNightForest,
    surface = NatureSurface,
    surfaceVariant = NatureSurfaceElevated,
    onPrimary = Color.Black,
    onSecondary = Color.White,
    onBackground = MoonlightSilver,
    onSurface = MoonlightSilver,
    onSurfaceVariant = MoonlightSoft
  )

private val LightColorScheme =
  lightColorScheme(
    primary = ForestGreen,
    secondary = LeafGreen,
    tertiary = EarthBrown,
    background = LightBeige,
    surface = Color.White,
    surfaceVariant = LightSurfaceElevated,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    onSurfaceVariant = TextSecondary
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  content: @Composable () -> Unit,
) {
  val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
