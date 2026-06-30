package com.plotmap.app.core.designsystem.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.plotmap.app.core.designsystem.Gold
import com.plotmap.app.core.designsystem.GoldBright
import com.plotmap.app.core.designsystem.GoldBronze
import com.plotmap.app.core.designsystem.GoldButtonBorder
import com.plotmap.app.core.designsystem.OnGold
import com.plotmap.app.core.designsystem.PlayfairDisplayFamily
import com.plotmap.app.core.designsystem.SecondarySurface
import com.plotmap.app.core.designsystem.WineBorder
import com.plotmap.app.core.designsystem.WineRed
import com.plotmap.app.core.designsystem.WineSurface
import com.plotmap.app.core.designsystem.WineText

private val ButtonShape = RoundedCornerShape(11.dp)
private val ButtonContentPadding = PaddingValues(horizontal = 24.dp, vertical = 14.dp)

@Composable
fun plotMapButtonColors(): ButtonColors =
    ButtonDefaults.buttonColors(
        containerColor = Gold,
        contentColor = OnGold,
    )

@Composable
private fun ButtonLabel(
    text: String,
    color: Color,
    weight: FontWeight,
) {
    Text(
        text = text,
        color = color,
        fontFamily = PlayfairDisplayFamily,
        fontWeight = weight,
        fontSize = 16.sp,
    )
}

@Composable
fun PlotMapPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val gradient =
        if (enabled) {
            Brush.linearGradient(listOf(GoldBright, GoldBronze))
        } else {
            Brush.linearGradient(listOf(GoldBronze.copy(alpha = 0.35f), GoldBronze.copy(alpha = 0.25f)))
        }
    Box(
        modifier =
            modifier
                .shadow(if (enabled) 8.dp else 0.dp, ButtonShape, spotColor = Color.Black, ambientColor = Color.Black)
                .clip(ButtonShape)
                .background(gradient)
                .clickable(enabled = enabled, onClick = onClick)
                .padding(ButtonContentPadding),
        contentAlignment = Alignment.Center,
    ) {
        ButtonLabel(text, OnGold.copy(alpha = if (enabled) 1f else 0.55f), FontWeight.Bold)
    }
}

@Composable
fun PlotMapSecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    Box(
        modifier =
            modifier
                .clip(ButtonShape)
                .background(SecondarySurface)
                .border(BorderStroke(1.dp, GoldButtonBorder), ButtonShape)
                .clickable(enabled = enabled, onClick = onClick)
                .padding(ButtonContentPadding),
        contentAlignment = Alignment.Center,
    ) {
        ButtonLabel(text, GoldBright.copy(alpha = if (enabled) 1f else 0.5f), FontWeight.SemiBold)
    }
}

@Composable
fun PlotMapDestructiveButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    Box(
        modifier =
            modifier
                .clip(ButtonShape)
                .background(WineSurface)
                .border(BorderStroke(1.dp, WineBorder.copy(alpha = 0.55f)), ButtonShape)
                .clickable(enabled = enabled, onClick = onClick)
                .padding(ButtonContentPadding),
        contentAlignment = Alignment.Center,
    ) {
        ButtonLabel(text, if (enabled) WineText else WineRed.copy(alpha = 0.5f), FontWeight.SemiBold)
    }
}
