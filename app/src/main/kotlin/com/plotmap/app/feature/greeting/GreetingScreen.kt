package com.plotmap.app.feature.greeting
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.plotmap.app.R
import com.plotmap.app.core.designsystem.GoldBright
import com.plotmap.app.core.designsystem.components.ManuscriptBackground

@Composable
fun GreetingScreen(
    userName: String = "",
    isRegistration: Boolean = false,
    onFinished: () -> Unit,
) {
    val alpha = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        alpha.animateTo(1f, animationSpec = tween(durationMillis = 500))
        alpha.animateTo(0f, animationSpec = tween(durationMillis = 1000))
        onFinished()
    }
    val normalizedUserName = userName.trim()
    val greeting =
        if (isRegistration) {
            "Добро пожаловать, $normalizedUserName!"
        } else if (normalizedUserName.isBlank()) {
            stringResource(R.string.welcome)
        } else {
            stringResource(R.string.welcome_name, normalizedUserName)
        }
    ManuscriptBackground {
        Box(
            modifier = Modifier.fillMaxSize().padding(32.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = greeting,
                style = MaterialTheme.typography.headlineMedium,
                color = GoldBright,
                textAlign = TextAlign.Center,
                modifier = Modifier.graphicsLayer(alpha = alpha.value),
            )
        }
    }
}
