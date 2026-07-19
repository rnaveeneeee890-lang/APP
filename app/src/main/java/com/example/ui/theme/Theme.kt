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
    primary = ElectricYellow,
    onPrimary = DeepObsidian,
    secondary = ElectricBlue,
    onSecondary = DeepObsidian,
    tertiary = SuccessGreen,
    background = DeepObsidian,
    surface = CarbonCard,
    onBackground = Color.White,
    onSurface = Color.White,
    surfaceVariant = CarbonCardLight,
    onSurfaceVariant = Color.White,
    error = AlertRed
  )

private val LightColorScheme =
  lightColorScheme(
    primary = Color(0xFFE5A100), // Darker yellow for visibility in light mode
    onPrimary = Color.White,
    secondary = Color(0xFF00B2CC),
    onSecondary = Color.White,
    tertiary = SuccessGreen,
    background = Color(0xFFF6F8FA),
    surface = Color.White,
    onBackground = DeepObsidian,
    onSurface = DeepObsidian,
    surfaceVariant = Color(0xFFEDF1F5),
    onSurfaceVariant = DeepObsidian,
    error = AlertRed
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  // Dynamic color is available on Android 12+
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
