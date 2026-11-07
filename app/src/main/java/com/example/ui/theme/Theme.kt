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

private val DarkColorScheme = darkColorScheme(
  primary = EmeraldDarkPrimary,
  onPrimary = Color(0xFF00382B),
  primaryContainer = Color(0xFF00513F),
  onPrimaryContainer = Color(0xFF73F8D4),
  secondary = EmeraldSecondary,
  onSecondary = Color.White,
  background = SlateDarkBackground,
  onBackground = Color(0xFFE9EDEF),
  surface = SlateDarkSurface,
  onSurface = Color(0xFFE9EDEF),
  surfaceVariant = SlateDarkSurfaceVariant,
  onSurfaceVariant = Color(0xFF8696A0),
  outline = SlateDarkBorder
)

private val LightColorScheme = lightColorScheme(
  primary = EmeraldSecondary,
  onPrimary = Color.White,
  primaryContainer = Color(0xFFD9FDD3),
  onPrimaryContainer = Color(0xFF002118),
  secondary = EmeraldPrimaryVariant,
  onSecondary = Color.White,
  background = LightBackground,
  onBackground = Color(0xFF111B21),
  surface = LightSurface,
  onSurface = Color(0xFF111B21),
  surfaceVariant = LightSurfaceVariant,
  onSurfaceVariant = Color(0xFF54656F),
  outline = LightBorder
)

@Composable
fun ChatsMessagePkTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
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
