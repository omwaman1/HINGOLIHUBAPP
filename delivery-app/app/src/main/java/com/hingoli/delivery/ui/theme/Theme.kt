package com.hingoli.delivery.ui.theme

import android.app.Activity
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// ===========================================
// DARK CHARCOAL PREMIUM THEME
// ===========================================

// Accent Colors
val DeliveryOrange = Color(0xFFFF5722)         // Vibrant orange for primary actions
val DeliveryOrangeDark = Color(0xFFE64A19)     // Darker orange
val DeliveryOrangeLight = Color(0xFFFF8A65)    // Lighter orange for highlights
val DeliveryGreen = Color(0xFF66BB6A)          // Success green
val DeliveryBlue = Color(0xFF42A5F5)           // Info blue
val DeliveryRed = Color(0xFFEF5350)            // Error red

// Dark Charcoal Palette
val CharcoalDarkest = Color(0xFF121212)        // Deepest black - status bar
val CharcoalDark = Color(0xFF1A1A1A)           // Dark background
val CharcoalMedium = Color(0xFF242424)         // Card backgrounds
val CharcoalLight = Color(0xFF2D2D2D)          // Elevated surfaces
val CharcoalBorder = Color(0xFF3D3D3D)         // Borders
val CharcoalDivider = Color(0xFF333333)        // Dividers

// Text Colors for Dark Theme
val TextWhite = Color(0xFFFFFFFF)              // Primary text
val TextLight = Color(0xFFE0E0E0)              // Secondary text
val TextMuted = Color(0xFF9E9E9E)              // Tertiary/muted text
val TextDisabled = Color(0xFF616161)           // Disabled text

// For exports to other files
val PremiumBorder = CharcoalBorder
val PremiumTextSecondary = TextMuted
val PremiumIconTint = TextMuted

private val DarkCharcoalScheme = darkColorScheme(
    primary = DeliveryOrange,
    onPrimary = Color.White,
    primaryContainer = DeliveryOrangeDark,
    onPrimaryContainer = DeliveryOrangeLight,
    secondary = DeliveryBlue,
    onSecondary = Color.White,
    tertiary = DeliveryGreen,
    onTertiary = Color.White,
    background = CharcoalDark,
    surface = CharcoalMedium,
    surfaceVariant = CharcoalLight,
    onBackground = TextWhite,
    onSurface = TextWhite,
    onSurfaceVariant = TextLight,
    outline = CharcoalBorder,
    outlineVariant = CharcoalDivider
)

@Composable
fun DeliveryAppTheme(
    content: @Composable () -> Unit
) {
    val colorScheme = DarkCharcoalScheme  // Force dark charcoal theme
    val view = LocalView.current
    
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            // Dark status bar matching the theme
            window.statusBarColor = CharcoalDarkest.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography(),
        content = content
    )
}
