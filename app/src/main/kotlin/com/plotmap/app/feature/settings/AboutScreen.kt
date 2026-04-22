package com.plotmap.app.feature.settings
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.plotmap.app.R
import com.plotmap.app.core.designsystem.components.PlotMapBackButton
import com.plotmap.app.core.designsystem.components.plotMapButtonColors

@Composable
fun AboutScreen(onBack: () -> Unit) {
    val uriHandler = LocalUriHandler.current

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            PlotMapBackButton(onClick = onBack, contentDescription = stringResource(R.string.btn_back))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = stringResource(R.string.about_app),
                style = MaterialTheme.typography.headlineMedium,
            )
        }

        Text(
            text = "PlotMap",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(bottom = 8.dp),
        )

        Text(
            text = stringResource(R.string.app_subtitle),
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(bottom = 24.dp),
        )

        Text(
            text = stringResource(R.string.app_version),
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(bottom = 32.dp),
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = stringResource(R.string.links),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 16.dp),
        )

        Button(
            onClick = {
                uriHandler.openUri("https://t.me/plotmap_project")
            },
            modifier = Modifier.fillMaxWidth(),
            colors = plotMapButtonColors(),
        ) {
            Text(
                text = stringResource(R.string.tg_channel),
                textDecoration = TextDecoration.Underline,
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = {
                uriHandler.openUri("https://example.com/privacy-policy")
            },
            modifier = Modifier.fillMaxWidth(),
            colors = plotMapButtonColors(),
        ) {
            Text(
                text = stringResource(R.string.privacy_policy),
                textDecoration = TextDecoration.Underline,
            )
        }

        Spacer(modifier = Modifier.weight(1f))
    }
}
