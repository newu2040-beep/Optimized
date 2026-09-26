package com.example.ui.theme

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import com.example.data.preferences.ThemeStyle

private val DarkScheme = darkColorScheme(
    primary = AccentCyan,
    onPrimary = Color(0xFF00363D),
    primaryContainer = Color(0xFF004F58),
    onPrimaryContainer = Color(0xFF9EEFFF),
    secondary = AccentCobalt,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF003E99),
    onSecondaryContainer = Color(0xFFD4E3FF),
    tertiary = AccentEmerald,
    background = DarkBackground,
    onBackground = DarkTextPrimary,
    surface = DarkSurface,
    onSurface = DarkTextPrimary,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkTextSecondary,
    outline = DarkBorder
)

private val GraphiteScheme = darkColorScheme(
    primary = GraphiteAccent,
    onPrimary = Color(0xFF003544),
    primaryContainer = Color(0xFF004D63),
    onPrimaryContainer = Color(0xFFC0E8FF),
    secondary = Color(0xFFA1A1AA),
    onSecondary = Color(0xFF18181B),
    secondaryContainer = Color(0xFF3F3F46),
    onSecondaryContainer = Color(0xFFE4E4E7),
    background = GraphiteBackground,
    onBackground = GraphiteTextPrimary,
    surface = GraphiteSurface,
    onSurface = GraphiteTextPrimary,
    surfaceVariant = GraphiteSurfaceVariant,
    onSurfaceVariant = GraphiteTextSecondary,
    outline = GraphiteBorder
)

private val MidnightScheme = darkColorScheme(
    primary = MidnightAccent,
    onPrimary = Color(0xFF002B73),
    primaryContainer = Color(0xFF0040A8),
    onPrimaryContainer = Color(0xFFD9E2FF),
    secondary = Color(0xFF38BDF8),
    onSecondary = Color(0xFF003545),
    secondaryContainer = Color(0xFF004D64),
    onSecondaryContainer = Color(0xFFBEE9FF),
    background = MidnightBackground,
    onBackground = MidnightTextPrimary,
    surface = MidnightSurface,
    onSurface = MidnightTextPrimary,
    surfaceVariant = MidnightSurfaceVariant,
    onSurfaceVariant = MidnightTextSecondary,
    outline = MidnightBorder
)

private val AuroraScheme = darkColorScheme(
    primary = AuroraAccent,
    onPrimary = Color(0xFF003730),
    primaryContainer = Color(0xFF005147),
    onPrimaryContainer = Color(0xFF70F7E2),
    secondary = Color(0xFF34D399),
    onSecondary = Color(0xFF003824),
    background = AuroraBackground,
    onBackground = AuroraTextPrimary,
    surface = AuroraSurface,
    onSurface = AuroraTextPrimary,
    surfaceVariant = AuroraSurfaceVariant,
    onSurfaceVariant = AuroraTextSecondary,
    outline = AuroraBorder
)

private val CreamScheme = lightColorScheme(
    primary = CreamAccent,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFDDB3),
    onPrimaryContainer = Color(0xFF2A1700),
    secondary = Color(0xFF78716C),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE7E5E4),
    onSecondaryContainer = Color(0xFF1C1917),
    background = CreamBackground,
    onBackground = CreamTextPrimary,
    surface = CreamSurface,
    onSurface = CreamTextPrimary,
    surfaceVariant = CreamSurfaceVariant,
    onSurfaceVariant = CreamTextSecondary,
    outline = CreamBorder
)

private val LightScheme = lightColorScheme(
    primary = LightAccent,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFBAE6FD),
    onPrimaryContainer = Color(0xFF00354E),
    secondary = Color(0xFF475569),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE2E8F0),
    onSecondaryContainer = Color(0xFF0F172A),
    background = LightBackground,
    onBackground = LightTextPrimary,
    surface = LightSurface,
    onSurface = LightTextPrimary,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightTextSecondary,
    outline = LightBorder
)

private val CyberpunkScheme = darkColorScheme(
    primary = CyberpunkAccent,
    onPrimary = Color(0xFF1F0C3B),
    primaryContainer = Color(0xFF381466),
    onPrimaryContainer = Color(0xFFFFF7A8),
    secondary = CyberpunkSecondary,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF4A1E86),
    onSecondaryContainer = Color(0xFFF3C4FB),
    background = CyberpunkBackground,
    onBackground = CyberpunkTextPrimary,
    surface = CyberpunkSurface,
    onSurface = CyberpunkTextPrimary,
    surfaceVariant = CyberpunkSurfaceVariant,
    onSurfaceVariant = CyberpunkTextSecondary,
    outline = CyberpunkBorder
)

private val SunsetScheme = darkColorScheme(
    primary = SunsetAccent,
    onPrimary = Color(0xFF2C0F00),
    primaryContainer = Color(0xFF5D2504),
    onPrimaryContainer = Color(0xFFFFDBCF),
    secondary = SunsetSecondary,
    onSecondary = Color(0xFF3A1C00),
    secondaryContainer = Color(0xFF452E6A),
    onSecondaryContainer = Color(0xFFFFE0B2),
    background = SunsetBackground,
    onBackground = SunsetTextPrimary,
    surface = SunsetSurface,
    onSurface = SunsetTextPrimary,
    surfaceVariant = SunsetSurfaceVariant,
    onSurfaceVariant = SunsetTextSecondary,
    outline = SunsetBorder
)

private val EmeraldScheme = darkColorScheme(
    primary = EmeraldAccent,
    onPrimary = Color(0xFF00391A),
    primaryContainer = Color(0xFF005327),
    onPrimaryContainer = Color(0xFF7DFFAB),
    secondary = EmeraldSecondary,
    onSecondary = Color(0xFF00381B),
    secondaryContainer = Color(0xFF102E1B),
    onSecondaryContainer = Color(0xFFA5D6A7),
    background = EmeraldBackground,
    onBackground = EmeraldTextPrimary,
    surface = EmeraldSurface,
    onSurface = EmeraldTextPrimary,
    surfaceVariant = EmeraldSurfaceVariant,
    onSurfaceVariant = EmeraldTextSecondary,
    outline = EmeraldBorder
)

private val OceanicScheme = darkColorScheme(
    primary = OceanicAccent,
    onPrimary = Color(0xFF00363D),
    primaryContainer = Color(0xFF004E59),
    onPrimaryContainer = Color(0xFF9CF4FF),
    secondary = OceanicSecondary,
    onSecondary = Color(0xFF00363F),
    secondaryContainer = Color(0xFF0F2D4A),
    onSecondaryContainer = Color(0xFF80DEEA),
    background = OceanicBackground,
    onBackground = OceanicTextPrimary,
    surface = OceanicSurface,
    onSurface = OceanicTextPrimary,
    surfaceVariant = OceanicSurfaceVariant,
    onSurfaceVariant = OceanicTextSecondary,
    outline = OceanicBorder
)

private val AmoledScheme = darkColorScheme(
    primary = AmoledAccent,
    onPrimary = Color.Black,
    primaryContainer = Color(0xFF0C2738),
    onPrimaryContainer = Color(0xFFBAE6FD),
    secondary = AmoledSecondary,
    onSecondary = Color.Black,
    secondaryContainer = Color(0xFF1E1E1E),
    onSecondaryContainer = Color(0xFFE0E0E0),
    background = AmoledBackground,
    onBackground = AmoledTextPrimary,
    surface = AmoledSurface,
    onSurface = AmoledTextPrimary,
    surfaceVariant = AmoledSurfaceVariant,
    onSurfaceVariant = AmoledTextSecondary,
    outline = AmoledBorder
)

@Composable
fun OptimizedTheme(
    themeStyle: ThemeStyle = ThemeStyle.GRAPHITE,
    systemDark: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val targetScheme = when (themeStyle) {
        ThemeStyle.SYSTEM_DEFAULT -> if (systemDark) DarkScheme else LightScheme
        ThemeStyle.DARK -> DarkScheme
        ThemeStyle.LIGHT -> LightScheme
        ThemeStyle.GRAPHITE -> GraphiteScheme
        ThemeStyle.CREAM -> CreamScheme
        ThemeStyle.MIDNIGHT -> MidnightScheme
        ThemeStyle.AURORA -> AuroraScheme
        ThemeStyle.CYBERPUNK -> CyberpunkScheme
        ThemeStyle.SUNSET -> SunsetScheme
        ThemeStyle.EMERALD -> EmeraldScheme
        ThemeStyle.OCEANIC -> OceanicScheme
        ThemeStyle.AMOLED -> AmoledScheme
    }

    MaterialTheme(
        colorScheme = targetScheme,
        typography = Typography,
        content = content
    )
}
