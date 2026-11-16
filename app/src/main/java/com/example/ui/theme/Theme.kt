package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val Luck780ColorScheme = darkColorScheme(
    primary = FestiveCrimson,
    onPrimary = Color.White,
    primaryContainer = FestiveCrimsonDark,
    onPrimaryContainer = Color.White,
    secondary = ArcadeGold,
    onSecondary = TextOnGold,
    secondaryContainer = ArcadeGoldDeep,
    onSecondaryContainer = TextOnGold,
    tertiary = WinterFrostCyan,
    onTertiary = Color(0xFF001F2E),
    background = RoyalPlumDark,
    onBackground = TextOnDarkPrimary,
    surface = RoyalPlumSurface,
    onSurface = TextOnDarkPrimary,
    surfaceVariant = RoyalPlumCard,
    onSurfaceVariant = TextOnDarkSecondary,
    outline = RoyalPlumBorder,
    error = Color(0xFFFF6B81),
    onError = Color(0xFF2B0007)
)

private val Luck780Shapes = Shapes(
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(22.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun Luck780Theme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = Luck780ColorScheme,
        typography = Luck780Typography,
        shapes = Luck780Shapes,
        content = content
    )
}
