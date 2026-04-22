package com.plotmap.app.feature.editor.workspace

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.plotmap.app.R
import com.plotmap.app.core.designsystem.components.PlotMapBackButton

@Composable
fun EditorScreen(
    projectName: String,
    onBack: () -> Unit,
    onBackToHome: () -> Unit,
) {
    Scaffold(
        topBar = {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                PlotMapBackButton(onClick = onBackToHome, contentDescription = stringResource(R.string.back_to_home))
                Text(
                    text = stringResource(R.string.editor_title, projectName),
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onBackground,
                )
            }
        },
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding))
    }
}
