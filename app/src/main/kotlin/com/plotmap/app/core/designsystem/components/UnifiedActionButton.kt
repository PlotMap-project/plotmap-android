package com.plotmap.app.core.designsystem.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.plotmap.app.core.designsystem.MilkChocolateCard
import com.plotmap.app.core.designsystem.PaperBeige

@Composable
fun UnifiedActionButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    ActionButton(
        text = text,
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        containerColor = MilkChocolateCard,
        contentColor = PaperBeige,
    )
}
