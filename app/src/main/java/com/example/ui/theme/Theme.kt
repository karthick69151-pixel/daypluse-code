package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val DarkColorScheme = darkColorScheme(
    primary = AmberGlowDark,
    onPrimary = Color(0xFF281002),
    primaryContainer = AmberContainerDark,
    onPrimaryContainer = Color(0xFFFFD9C2),
    secondary = MintSageDark,
    onSecondary = Color(0xFF04261D),
    secondaryContainer = MintContainerDark,
    onSecondaryContainer = Color(0xFFBDF5E6),
    tertiary = Color(0xFF9EB4E0),
    onTertiary = Color(0xFF111C33),
    background = ObsidianBg,
    onBackground = IvoryText,
    surface = MidnightSurface,
    onSurface = IvoryText,
    surfaceVariant = MidnightSurfaceHigh,
    onSurfaceVariant = CoolMutedText,
    surfaceContainerLowest = ObsidianBg,
    surfaceContainerLow = MidnightSurface,
    surfaceContainer = MidnightSurfaceHigh,
    surfaceContainerHigh = MidnightSurfaceHighest,
    outline = HairlineDark,
    outlineVariant = Color(0xFF232C3D)
)

private val LightColorScheme = lightColorScheme(
    primary = TerracottaPrimary,
    onPrimary = Color.White,
    primaryContainer = TerracottaContainer,
    onPrimaryContainer = OnTerracottaContainer,
    secondary = ForestSageSecondary,
    onSecondary = Color.White,
    secondaryContainer = ForestSageContainer,
    onSecondaryContainer = OnForestSageContainer,
    tertiary = MidnightSlateTertiary,
    onTertiary = Color.White,
    tertiaryContainer = MidnightContainer,
    onTertiaryContainer = Color(0xFF152238),
    background = AlabasterBg,
    onBackground = InkCharcoal,
    surface = WarmPaperSurface,
    onSurface = InkCharcoal,
    surfaceVariant = WarmSandContainer,
    onSurfaceVariant = MutedSlate,
    surfaceContainerLowest = WarmPaperSurface,
    surfaceContainerLow = AlabasterBg,
    surfaceContainer = WarmSandContainer,
    surfaceContainerHigh = WarmSandHigh,
    outline = HairlineWarm,
    outlineVariant = Color(0xFFEBE4D8)
)

val DayPulseShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(22.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun DayPulseTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = DayPulseShapes,
        content = content
    )
}
