package com.plotmap.app.core.designsystem.components

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.dp
import com.plotmap.app.core.designsystem.DarkCappuccinoColor
import com.plotmap.app.core.designsystem.LightCappuccinoColor
import com.plotmap.app.core.designsystem.LocalIsDarkTheme
import com.plotmap.app.core.designsystem.PaperBeige

object NextButton {
    @Composable
    operator fun invoke(
        onClick: () -> Unit,
        modifier: Modifier = Modifier,
        enabled: Boolean = true,
    ) {
        val isDarkTheme = LocalIsDarkTheme.current
        val shape = RoundedCornerShape(12.dp)
        val shadowColor = if (isDarkTheme) LightCappuccinoColor else DarkCappuccinoColor
        val textColor =
            when {
                enabled && isDarkTheme -> PaperBeige
                enabled -> MaterialTheme.colorScheme.background
                else -> PaperBeige
            }

        val colors =
            if (enabled) {
                plotMapButtonColors()
            } else {
                ButtonDefaults.buttonColors(
                    containerColor = LightCappuccinoColor,
                    contentColor = PaperBeige,
                    disabledContainerColor = LightCappuccinoColor,
                    disabledContentColor = PaperBeige,
                )
            }

        Button(
            onClick = onClick,
            modifier =
                modifier.shadow(
                    elevation = 3.dp,
                    shape = shape,
                    ambientColor = shadowColor,
                    spotColor = shadowColor,
                    clip = false,
                ),
            enabled = enabled,
            colors = colors,
            shape = shape,
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp, pressedElevation = 0.dp),
        ) {
            Text(text = "Далее", color = textColor)
        }
    }
}
