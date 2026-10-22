package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
  primary = NeonCyan,
  onPrimary = CyberBackground,
  primaryContainer = CyberSurfaceVariant,
  onPrimaryContainer = NeonCyan,
  secondary = NeonMagenta,
  onSecondary = CyberBackground,
  secondaryContainer = CyberSurfaceVariant,
  onSecondaryContainer = NeonMagenta,
  tertiary = NeonYellow,
  onTertiary = CyberBackground,
  tertiaryContainer = CyberSurfaceVariant,
  onTertiaryContainer = NeonYellow,
  background = CyberBackground,
  onBackground = TextPrimary,
  surface = CyberSurface,
  onSurface = TextPrimary,
  surfaceVariant = CyberSurfaceVariant,
  onSurfaceVariant = TextSecondary,
  outline = CyberSurfaceHigh
)

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  dynamicColor: Boolean = false, // Preserve bespoke neon synthwave aesthetic
  content: @Composable () -> Unit,
) {
  MaterialTheme(
    colorScheme = DarkColorScheme,
    typography = Typography,
    content = content
  )
}
