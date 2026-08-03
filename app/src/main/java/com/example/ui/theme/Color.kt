package com.example.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// RemX Instagram-inspired accent colors (Violet -> Magenta/Pink -> Orange)
val RemXViolet = Color(0xFF8A2BE2)
val RemXPink = Color(0xFFE02874)
val RemXOrange = Color(0xFFFF6B35)
val RemXPurpleDark = Color(0xFF6C1D9B)

// Dark Theme Palette (Quasi-Black)
val DarkBackground = Color(0xFF09080C)
val DarkSurface = Color(0xFF14121A)
val DarkSurfaceVariant = Color(0xFF1F1C28)
val DarkSurfaceContainer = Color(0xFF282434)
val DarkOnBackground = Color(0xFFF4F2F8)
val DarkOnSurface = Color(0xFFF4F2F8)
val DarkOnSurfaceVariant = Color(0xFFB9B4C4)
val DarkOutline = Color(0xFF3B354A)

// Light Theme Palette
val LightBackground = Color(0xFFF9F7FC)
val LightSurface = Color(0xFFFFFFFF)
val LightSurfaceVariant = Color(0xFFF0ECF5)
val LightSurfaceContainer = Color(0xFFE6E0F0)
val LightOnBackground = Color(0xFF1A1822)
val LightOnSurface = Color(0xFF1A1822)
val LightOnSurfaceVariant = Color(0xFF635D70)
val LightOutline = Color(0xFFD3CBE0)

// Gradients
val RemXGradientBrush = Brush.horizontalGradient(
    colors = listOf(RemXViolet, RemXPink, RemXOrange)
)

val RemXGradientBrushVertical = Brush.verticalGradient(
    colors = listOf(RemXViolet, RemXPink, RemXOrange)
)

val RemXDarkCardGradient = Brush.verticalGradient(
    colors = listOf(Color(0xFF1F1C2A), Color(0xFF14121A))
)
