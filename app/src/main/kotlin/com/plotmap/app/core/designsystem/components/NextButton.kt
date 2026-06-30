package com.plotmap.app.core.designsystem.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.plotmap.app.R

object NextButton {
    @Composable
    operator fun invoke(
        onClick: () -> Unit,
        modifier: Modifier = Modifier,
        enabled: Boolean = true,
    ) {
        PlotMapPrimaryButton(
            text = stringResource(R.string.btn_next),
            onClick = onClick,
            modifier = modifier,
            enabled = enabled,
        )
    }
}
