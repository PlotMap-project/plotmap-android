package com.plotmap.app.core.designsystem

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import com.plotmap.app.R

val LocalIsDarkTheme = staticCompositionLocalOf { false }

val PaperBeige = Color(0xFFFCF5EB)
val DarkBrownInk = Color(0xFF3D2917)
val LightOrange = Color(0xFFE9D7CB)
val SealingWaxRed = Color(0xFFA83F39)
val DarkCappuccinoColor = Color(0xFF947158)
val LightCappuccinoColor = Color(0xFFC4A289)
val TotalWhite = Color(0xFFFFFFFF)
val DarkCoffeeBackground = Color(0xFF1E140F)
val MilkChocolateCard = Color(0xFF38261D)
val LatteCreamText = Color(0xFFE6D6C6)
val DarkSealingWaxRed = Color(0xFFC0554E)
private val PlotMapFontFamily = FontFamily(Font(R.font.somic_sans_ms))

private val LightColors =
    lightColorScheme(
        primary = DarkBrownInk,
        onPrimary = PaperBeige,
        primaryContainer = DarkBrownInk,
        onPrimaryContainer = DarkBrownInk,
        secondary = DarkBrownInk,
        onSecondary = DarkBrownInk,
        secondaryContainer = PaperBeige,
        onSecondaryContainer = DarkBrownInk,
        tertiary = SealingWaxRed,
        onTertiary = Color.White,
        background = PaperBeige,
        onBackground = DarkBrownInk,
        surface = PaperBeige,
        onSurface = DarkBrownInk,
        surfaceVariant = LightOrange,
        onSurfaceVariant = DarkBrownInk,
    )

private val DarkColors =
    darkColorScheme(
        primary = LatteCreamText,
        onPrimary = DarkCoffeeBackground,
        primaryContainer = MilkChocolateCard,
        onPrimaryContainer = LatteCreamText,
        secondary = MilkChocolateCard,
        onSecondary = LatteCreamText,
        secondaryContainer = DarkCoffeeBackground,
        onSecondaryContainer = LatteCreamText,
        tertiary = DarkSealingWaxRed,
        onTertiary = DarkCoffeeBackground,
        background = DarkCoffeeBackground,
        onBackground = LatteCreamText,
        surface = DarkCoffeeBackground,
        onSurface = LatteCreamText,
        surfaceVariant = MilkChocolateCard,
        onSurfaceVariant = LatteCreamText,
    )

fun getTypography(fontSizeStr: String): Typography {
    val scale =
        when (fontSizeStr) {
            "small" -> 0.9f
            "large" -> 1.2f
            else -> 1.05f
        }
    val defaultTypography = Typography()
    return Typography(
        displayLarge =
            defaultTypography.displayLarge.copy(
                fontSize = defaultTypography.displayLarge.fontSize * scale,
                fontFamily = PlotMapFontFamily,
            ),
        displayMedium =
            defaultTypography.displayMedium.copy(
                fontSize = defaultTypography.displayMedium.fontSize * scale,
                fontFamily = PlotMapFontFamily,
            ),
        displaySmall =
            defaultTypography.displaySmall.copy(
                fontSize = defaultTypography.displaySmall.fontSize * scale,
                fontFamily = PlotMapFontFamily,
            ),
        headlineLarge =
            defaultTypography.headlineLarge.copy(
                fontSize = defaultTypography.headlineLarge.fontSize * scale,
                fontFamily = PlotMapFontFamily,
            ),
        headlineMedium =
            defaultTypography.headlineMedium.copy(
                fontSize = defaultTypography.headlineMedium.fontSize * scale,
                fontFamily = PlotMapFontFamily,
            ),
        headlineSmall =
            defaultTypography.headlineSmall.copy(
                fontSize = defaultTypography.headlineSmall.fontSize * scale,
                fontFamily = PlotMapFontFamily,
            ),
        titleLarge =
            defaultTypography.titleLarge.copy(
                fontSize = defaultTypography.titleLarge.fontSize * scale,
                fontFamily = PlotMapFontFamily,
            ),
        titleMedium =
            defaultTypography.titleMedium.copy(
                fontSize = defaultTypography.titleMedium.fontSize * scale,
                fontFamily = PlotMapFontFamily,
            ),
        titleSmall =
            defaultTypography.titleSmall.copy(
                fontSize = defaultTypography.titleSmall.fontSize * scale,
                fontFamily = PlotMapFontFamily,
            ),
        bodyLarge =
            defaultTypography.bodyLarge.copy(
                fontSize = defaultTypography.bodyLarge.fontSize * scale,
                fontFamily = PlotMapFontFamily,
            ),
        bodyMedium =
            defaultTypography.bodyMedium.copy(
                fontSize = defaultTypography.bodyMedium.fontSize * scale,
                fontFamily = PlotMapFontFamily,
            ),
        bodySmall =
            defaultTypography.bodySmall.copy(
                fontSize = defaultTypography.bodySmall.fontSize * scale,
                fontFamily = PlotMapFontFamily,
            ),
        labelLarge =
            defaultTypography.labelLarge.copy(
                fontSize = defaultTypography.labelLarge.fontSize * scale,
                fontFamily = PlotMapFontFamily,
            ),
        labelMedium =
            defaultTypography.labelMedium.copy(
                fontSize = defaultTypography.labelMedium.fontSize * scale,
                fontFamily = PlotMapFontFamily,
            ),
        labelSmall =
            defaultTypography.labelSmall.copy(
                fontSize = defaultTypography.labelSmall.fontSize * scale,
                fontFamily = PlotMapFontFamily,
            ),
    )
}

@Composable
fun PlotMapTheme(
    isDarkTheme: Boolean = isSystemInDarkTheme(),
    fontSizeStr: String = "medium",
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(LocalIsDarkTheme provides isDarkTheme) {
        MaterialTheme(
            colorScheme = if (isDarkTheme) DarkColors else LightColors,
            typography = getTypography(fontSizeStr),
            content = content,
        )
    }
}
