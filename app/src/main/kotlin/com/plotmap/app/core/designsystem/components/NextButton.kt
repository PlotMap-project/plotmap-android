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
        val textColor =
            when {
                enabled && isDarkTheme -> PaperBeige
                enabled -> MaterialTheme.colorScheme.background
                else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
            }

        Button(
            onClick = onClick,
            modifier = modifier.shadow(8.dp, RoundedCornerShape(4.dp)),
            enabled = enabled,
            colors = plotMapButtonColors(),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp, pressedElevation = 12.dp),
        ) {
            Text(text = "Далее", color = textColor)
        }
    }
}
