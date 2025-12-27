package com.hingoli.hub.ui.theme

import androidx.compose.ui.graphics.Color

// ============ JUSTDIAL BLUE THEME ============

// Primary - JustDial Blue
val Primary = Color(0xFF116DB6)             // JustDial Blue
val PrimaryDark = Color(0xFF0D5A96)         // Darker Blue
val PrimaryLight = Color(0xFF1E88C9)        // Light Blue

// Secondary - Complementary Blue
val Secondary = Color(0xFF0D8ABC)           // Sky Blue
val SecondaryLight = Color(0xFFB3E0F2)      // Light Sky

// Backgrounds - Clean whites
val BackgroundLight = Color(0xFFFFFFFF)     // Pure white
val SurfaceLight = Color(0xFFFFFFFF)        // Pure white
val SurfaceVariantLight = Color(0xFFF0F7FC) // Very light blue

// Text colors - Clean hierarchy
val TextPrimary = Color(0xFF1A1A1A)         // Near black
val TextSecondary = Color(0xFF6B7280)       // Medium gray
val TextTertiary = Color(0xFF9CA3AF)        // Light gray

// Borders and dividers
val BorderLight = Color(0xFFE5E7EB)         // Subtle border
val DividerColor = Color(0xFFF3F4F6)        // Very light divider

// Dark mode colors
val BackgroundDark = Color(0xFF0A1929)      // Deep navy
val SurfaceDark = Color(0xFF132F4C)         // Dark blue
val SurfaceVariantDark = Color(0xFF1E4976)  // Slightly lighter

// Status colors
val SuccessGreen = Color(0xFF22C55E)        // Green
val WarningOrange = Color(0xFFF59E0B)       // Amber
val ErrorRed = Color(0xFFEF4444)            // Red
val InfoBlue = Color(0xFF116DB6)            // Same as primary

// Rating
val StarFilled = Color(0xFFFBBF24)          // Gold
val StarEmpty = Color(0xFFE5E7EB)           // Light gray

// Verified/Featured badges
val VerifiedGreen = Color(0xFF22C55E)       // Green
val FeaturedBadge = Color(0xFFF59E0B)       // Amber

// Category Colors - All use primary for consistency
val ColorServices = Primary
val ColorShop = Primary
val ColorJobs = Primary
val ColorRealEstate = Primary
val ColorBusiness = Primary

// Legacy aliases (for backward compatibility)
val PrimaryBlue = Primary
val AccentOrange = WarningOrange
val AccentGreen = SuccessGreen
val AccentRed = ErrorRed
val AccentYellow = StarFilled
val Surface = SurfaceLight
val SurfaceVariant = SurfaceVariantLight
val Background = BackgroundLight
val OnSurface = TextPrimary
val OnSurfaceVariant = TextSecondary
val CardBackground = SurfaceLight
val CardBorder = BorderLight

// ============ COMMONLY USED INLINE COLORS ============
// Add these to avoid magic numbers like Color(0xFF...) in code

// Background shades
val LightGrayBackground = Color(0xFFF5F5F5)   // Image placeholders
val VeryLightBlue = Color(0xFFF8FAFC)         // Dialog gradient end
val SlateGray = Color(0xFF64748B)             // Labels, hints
val DarkSlate = Color(0xFF1E293B)             // Dark text

// Input field colors  
val InputBorder = Color(0xFFE2E8F0)           // TextField borders
val DisabledButton = Color(0xFFCBD5E1)        // Disabled state

// Chat colors
val ChatBubbleMine = Color(0xFFDCF8C6)        // My message bubble
val ChatBubbleOther = Color.White             // Other's message bubble

// Overlay
val OverlayDark = Color(0x99000000)           // 60% black overlay
val OverlayLight = Color(0x33000000)          // 20% black overlay

