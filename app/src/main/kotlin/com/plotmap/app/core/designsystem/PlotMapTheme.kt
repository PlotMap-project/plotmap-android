package com.plotmap.app.core.designsystem

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp

val LocalIsDarkTheme = staticCompositionLocalOf { true }

private val ManuscriptColorScheme =
    darkColorScheme(
        primary = Gold,
        onPrimary = OnGold,
        primaryContainer = SurfaceVariant,
        onPrimaryContainer = GoldBright,
        secondary = Lavender,
        onSecondary = OnGold,
        secondaryContainer = Surface,
        onSecondaryContainer = LavenderLight,
        tertiary = LavenderLight,
        onTertiary = OnGold,
        background = BackgroundSolid,
        onBackground = OnBackground,
        surface = Surface,
        onSurface = OnBackground,
        surfaceVariant = SurfaceVariant,
        onSurfaceVariant = TextBody,
        error = WineRed,
        onError = OnGold,
        errorContainer = WineSurface,
        onErrorContainer = WineText,
        outline = BorderCard,
        outlineVariant = BorderThin,
    )

private fun TextStyle.with(
    family: FontFamily,
    weight: FontWeight,
    scale: Float,
    letterSpacingEm: Float = Float.NaN,
): TextStyle =
    copy(
        fontFamily = family,
        fontWeight = weight,
        fontSize = fontSize * scale,
        lineHeight = if (lineHeight.isSp) lineHeight * scale else lineHeight,
        letterSpacing = if (letterSpacingEm.isNaN()) letterSpacing else letterSpacingEm.em,
    )

fun getTypography(fontSizeStr: String): Typography {
    val scale =
        when (fontSizeStr) {
            "small" -> 0.9f
            "large" -> 1.2f
            else -> 1.05f
        }
    val d = Typography()
    return Typography(
        displayLarge = d.displayLarge.with(PlayfairDisplayFamily, FontWeight.ExtraBold, scale),
        displayMedium = d.displayMedium.with(PlayfairDisplayFamily, FontWeight.ExtraBold, scale),
        displaySmall = d.displaySmall.with(PlayfairDisplayFamily, FontWeight.ExtraBold, scale),
        headlineLarge = d.headlineLarge.with(PlayfairDisplayFamily, FontWeight.Bold, scale),
        headlineMedium = d.headlineMedium.with(PlayfairDisplayFamily, FontWeight.Bold, scale),
        headlineSmall = d.headlineSmall.with(PlayfairDisplayFamily, FontWeight.Bold, scale),
        titleLarge = d.titleLarge.with(PlayfairDisplayFamily, FontWeight.Bold, scale),
        titleMedium = d.titleMedium.with(PlayfairDisplayFamily, FontWeight.SemiBold, scale),
        titleSmall = d.titleSmall.with(PlayfairDisplayFamily, FontWeight.SemiBold, scale),
        bodyLarge = d.bodyLarge.with(PtSerifFamily, FontWeight.Normal, scale).copy(lineHeight = d.bodyLarge.fontSize * scale * 1.5f),
        bodyMedium = d.bodyMedium.with(PtSerifFamily, FontWeight.Normal, scale).copy(lineHeight = d.bodyMedium.fontSize * scale * 1.5f),
        bodySmall = d.bodySmall.with(PtSerifFamily, FontWeight.Normal, scale).copy(lineHeight = d.bodySmall.fontSize * scale * 1.45f),
        labelLarge = d.labelLarge.with(ForumFamily, FontWeight.Normal, scale, letterSpacingEm = 0.06f),
        labelMedium = d.labelMedium.with(ForumFamily, FontWeight.Normal, scale, letterSpacingEm = 0.08f),
        labelSmall = d.labelSmall.with(ForumFamily, FontWeight.Normal, scale, letterSpacingEm = 0.1f),
    )
}

val LogoTextStyle =
    TextStyle(
        fontFamily = CinzelDecorativeFamily,
        fontWeight = FontWeight.Black,
        fontSize = 40.sp,
        letterSpacing = 0.05.em,
    )

@Composable
fun PlotMapTheme(
    isDarkTheme: Boolean = true,
    fontSizeStr: String = "medium",
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(LocalIsDarkTheme provides true) {
        MaterialTheme(
            colorScheme = ManuscriptColorScheme,
            typography = getTypography(fontSizeStr),
            content = content,
        )
    }
}
