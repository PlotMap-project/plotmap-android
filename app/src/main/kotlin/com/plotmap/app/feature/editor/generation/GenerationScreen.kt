package com.plotmap.app.feature.editor.generation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.plotmap.app.R
import com.plotmap.app.core.designsystem.components.ManuscriptBackground
import com.plotmap.app.core.designsystem.components.PlotMapBackButton
import kotlinx.coroutines.delay
import org.koin.androidx.compose.koinViewModel

@Composable
fun GenerationScreen(
    name: String,
    description: String,
    text: String,
    onGenerationComplete: (projectId: String) -> Unit,
    onBack: () -> Unit,
    viewModel: GenerationViewModel = koinViewModel(),
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.generate(name, description, text)
    }

    ManuscriptBackground {
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            when (val current = state) {
                is GenerationUiState.Loading -> GenerationProgress()

                is GenerationUiState.Success ->
                    LaunchedEffect(current) {
                        onGenerationComplete(current.projectId)
                    }

                is GenerationUiState.Error -> {
                    Text(
                        text = stringResource(R.string.generation_error),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center,
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = { viewModel.generate(name, description, text) },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(stringResource(R.string.generation_retry))
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    PlotMapBackButton(
                        onClick = onBack,
                        contentDescription = stringResource(R.string.btn_back),
                    )
                }
            }
        }
    }
}

@Composable
private fun GenerationProgress() {
    val messages =
        listOf(
            stringResource(R.string.generation_msg_1),
            stringResource(R.string.generation_msg_2),
            stringResource(R.string.generation_msg_3),
        )
    var messageIndex by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(3500)
            messageIndex = (messageIndex + 1) % messages.size
        }
    }

    CircularProgressIndicator()
    Spacer(modifier = Modifier.height(24.dp))
    Text(
        text = messages[messageIndex],
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onBackground,
        textAlign = TextAlign.Center,
    )
}
