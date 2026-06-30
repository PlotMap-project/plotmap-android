package com.plotmap.app.core.designsystem

import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import com.plotmap.app.R

@OptIn(ExperimentalTextApi::class)
private fun playfair(
    weight: FontWeight,
    style: FontStyle = FontStyle.Normal,
) = Font(
    resId = if (style == FontStyle.Italic) R.font.playfair_display_italic_variable else R.font.playfair_display_variable,
    weight = weight,
    style = style,
    variationSettings = FontVariation.Settings(FontVariation.weight(weight.weight)),
)

val CinzelDecorativeFamily =
    FontFamily(
        Font(R.font.cinzel_decorative_bold, FontWeight.Bold),
        Font(R.font.cinzel_decorative_black, FontWeight.Black),
    )

val PlayfairDisplayFamily =
    FontFamily(
        playfair(FontWeight.Normal),
        playfair(FontWeight.Medium),
        playfair(FontWeight.SemiBold),
        playfair(FontWeight.Bold),
        playfair(FontWeight.ExtraBold),
        playfair(FontWeight.Black),
        playfair(FontWeight.Normal, FontStyle.Italic),
        playfair(FontWeight.Bold, FontStyle.Italic),
    )

val PtSerifFamily =
    FontFamily(
        Font(R.font.pt_serif_regular, FontWeight.Normal),
        Font(R.font.pt_serif_bold, FontWeight.Bold),
        Font(R.font.pt_serif_italic, FontWeight.Normal, FontStyle.Italic),
        Font(R.font.pt_serif_bold_italic, FontWeight.Bold, FontStyle.Italic),
    )

val ForumFamily = FontFamily(Font(R.font.forum_regular, FontWeight.Normal))

val MarckScriptFamily = FontFamily(Font(R.font.marck_script_regular, FontWeight.Normal))
