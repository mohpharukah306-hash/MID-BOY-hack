package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val CyberColorScheme = darkColorScheme(
  primary = NeonEmerald,
  onPrimary = CyberBlack,
  primaryContainer = DarkEmerald,
  onPrimaryContainer = NeonMint,
  secondary = NeonMint,
  onSecondary = CyberBlack,
  secondaryContainer = CyberSurfaceContainer,
  onSecondaryContainer = TextPrimary,
  tertiary = CyberCyan,
  onTertiary = CyberBlack,
  background = CyberBackground,
  onBackground = TextPrimary,
  surface = CyberSurface,
  onSurface = TextPrimary,
  surfaceVariant = CyberSurfaceVariant,
  onSurfaceVariant = TextSecondary,
  outline = DeepEmeraldBorder,
  outlineVariant = CyberSurfaceContainer,
  error = CyberRed,
  onError = Color.White
)

@Composable
fun MyApplicationTheme(
  content: @Composable () -> Unit,
) {
  MaterialTheme(
    colorScheme = CyberColorScheme,
    typography = Typography,
    content = content
  )
}
