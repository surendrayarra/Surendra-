package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
  primary = JoystickOrange,
  onPrimary = Color.Black,
  primaryContainer = ButtonOrangeDark,
  onPrimaryContainer = Color.White,
  secondary = BluetoothBlueLight,
  onSecondary = Color.Black,
  background = DarkNavyBg,
  onBackground = Color(0xFFF1F5F9),
  surface = ControllerNavy,
  onSurface = Color(0xFFF1F5F9),
  surfaceVariant = ControllerNavyDark,
  onSurfaceVariant = Color(0xFF94A3B8),
  outline = ControllerNavyBorder
)

@Composable
fun MyApplicationTheme(
  content: @Composable () -> Unit,
) {
  MaterialTheme(
    colorScheme = DarkColorScheme,
    typography = Typography,
    content = content
  )
}
