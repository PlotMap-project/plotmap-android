package com.plotmap.app.feature.splash

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.plotmap.app.R
import com.plotmap.app.core.designsystem.GoldBright
import com.plotmap.app.core.designsystem.GoldDeep
import com.plotmap.app.core.designsystem.GoldHighlight
import com.plotmap.app.core.designsystem.LogoTextStyle
import com.plotmap.app.core.designsystem.components.ManuscriptBackground
import com.plotmap.app.core.designsystem.components.OrnamentDivider
import com.plotmap.app.core.designsystem.components.OverlineLabel
import org.koin.androidx.compose.koinViewModel

@Composable
fun SplashScreen(
    viewModel: SplashViewModel = koinViewModel(),
    onNavigate: (SplashEvent) -> Unit,
) {
    LaunchedEffect(Unit) {
        viewModel.navigationEvent.collect { event ->
            onNavigate(event)
        }
    }

    ManuscriptBackground {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center,
        ) {
            Box(
                modifier =
                    Modifier
                        .size(96.dp)
                        .border(1.dp, GoldBright, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Text(text = "❦", color = GoldBright, fontSize = 40.sp)
            }
            Spacer(modifier = Modifier.height(28.dp))
            Text(
                text = stringResource(R.string.app_name),
                style =
                    LogoTextStyle.copy(
                        brush = Brush.linearGradient(listOf(GoldBright, GoldHighlight, GoldDeep)),
                    ),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(modifier = Modifier.height(12.dp))
            OrnamentDivider(modifier = Modifier.fillMaxWidth(0.5f))
            Spacer(modifier = Modifier.height(8.dp))
            OverlineLabel(text = stringResource(R.string.app_subtitle))
        }
    }
}
