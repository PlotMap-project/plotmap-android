package com.plotmap.app.core.designsystem.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.plotmap.app.core.designsystem.DarkBrownInk
import com.plotmap.app.core.designsystem.LocalIsDarkTheme
import com.plotmap.app.core.designsystem.MilkChocolateCard
import com.plotmap.app.core.designsystem.PaperBeige

@Composable
fun SettingsActionButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val isDarkTheme = LocalIsDarkTheme.current
    ActionButton(
        text = text,
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        containerColor = if (isDarkTheme) MilkChocolateCard else DarkBrownInk,
        contentColor = PaperBeige,
    )
}
