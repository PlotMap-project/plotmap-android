package com.plotmap.app.feature.settings
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.plotmap.app.R
import com.plotmap.app.core.designsystem.GoldBright
import com.plotmap.app.core.designsystem.GoldDeep
import com.plotmap.app.core.designsystem.GoldHighlight
import com.plotmap.app.core.designsystem.LogoTextStyle
import com.plotmap.app.core.designsystem.TextBody
import com.plotmap.app.core.designsystem.TextMuted
import com.plotmap.app.core.designsystem.components.ManuscriptBackground
import com.plotmap.app.core.designsystem.components.OverlineLabel
import com.plotmap.app.core.designsystem.components.PlotMapBackButton
import com.plotmap.app.core.designsystem.components.PlotMapSecondaryButton

@Composable
fun AboutScreen(onBack: () -> Unit) {
    val uriHandler = LocalUriHandler.current

    ManuscriptBackground {
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                PlotMapBackButton(onClick = onBack, contentDescription = stringResource(R.string.btn_back))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.about_app),
                    style = MaterialTheme.typography.headlineMedium,
                    color = GoldBright,
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "PlotMap",
                style =
                    LogoTextStyle.copy(
                        fontSize = 36.sp,
                        brush = Brush.linearGradient(listOf(GoldBright, GoldHighlight, GoldDeep)),
                    ),
                modifier = Modifier.padding(bottom = 8.dp),
            )

            Text(
                text = stringResource(R.string.app_subtitle),
                style = MaterialTheme.typography.bodyLarge,
                color = TextBody,
                modifier = Modifier.padding(bottom = 24.dp),
            )

            Text(
                text = stringResource(R.string.app_version),
                style = MaterialTheme.typography.bodyMedium,
                color = TextMuted,
                modifier = Modifier.padding(bottom = 32.dp),
            )

            Spacer(modifier = Modifier.height(24.dp))

            OverlineLabel(
                text = stringResource(R.string.links),
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            )

            PlotMapSecondaryButton(
                text = stringResource(R.string.tg_channel),
                onClick = { uriHandler.openUri("https://t.me/plotmap_project") },
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(modifier = Modifier.height(12.dp))

            PlotMapSecondaryButton(
                text = stringResource(R.string.privacy_policy),
                onClick = { uriHandler.openUri("https://example.com/privacy-policy") },
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(modifier = Modifier.weight(1f))
        }
    }
}
