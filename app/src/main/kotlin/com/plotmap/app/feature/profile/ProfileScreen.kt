package com.plotmap.app.feature.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.plotmap.app.core.data.TokenManager
import com.plotmap.app.core.designsystem.GoldBright
import com.plotmap.app.core.designsystem.OnBackground
import com.plotmap.app.core.designsystem.components.ManuscriptBackground
import com.plotmap.app.core.designsystem.components.NextButton
import com.plotmap.app.core.designsystem.components.PlotMapBackButton
import com.plotmap.app.core.designsystem.components.PlotMapDestructiveButton
import com.plotmap.app.core.designsystem.components.SettingsActionButton
import com.plotmap.app.core.designsystem.components.UnifiedActionButton
import org.koin.java.KoinJavaComponent.inject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onBack: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onLogout: () -> Unit,
    onDeleteAccount: () -> Unit,
) {
    val tokenManager: TokenManager by inject(TokenManager::class.java)
    val userName = tokenManager.getUserName() ?: "User"

    var showLogoutDialog by remember { mutableStateOf(false) }
    var showDeleteStep1Dialog by remember { mutableStateOf(false) }
    var showDeleteStep2Dialog by remember { mutableStateOf(false) }
    var showDeleteStep3Dialog by remember { mutableStateOf(false) }
    var password by remember { mutableStateOf("") }

    val primaryTextColor = OnBackground

    ManuscriptBackground {
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                PlotMapBackButton(onClick = onBack, contentDescription = "Назад")
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Профиль",
                    style = MaterialTheme.typography.headlineMedium,
                    color = GoldBright,
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Filled.AccountCircle,
                        contentDescription = "Аватар",
                        modifier = Modifier.size(100.dp),
                        tint = MaterialTheme.colorScheme.primary,
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = userName,
                        style = MaterialTheme.typography.titleLarge,
                        color = primaryTextColor,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }

            Spacer(modifier = Modifier.height(48.dp))

            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
            ) {
                Text(
                    text = "Синхронизация с Google",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = primaryTextColor,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "отключена",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            SettingsActionButton(
                text = "Настройки",
                onClick = onNavigateToSettings,
            )

            Spacer(modifier = Modifier.height(16.dp))

            UnifiedActionButton(
                text = "Выйти из аккаунта",
                onClick = { showLogoutDialog = true },
            )

            Spacer(modifier = Modifier.height(16.dp))

            PlotMapDestructiveButton(
                text = "Удалить аккаунт",
                onClick = { showDeleteStep1Dialog = true },
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Вы уверены?", style = MaterialTheme.typography.titleMedium) },
            confirmButton = {
                TextButton(onClick = {
                    showLogoutDialog = false
                    onLogout()
                }) {
                    Text("Да")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Отмена")
                }
            },
        )
    }

    if (showDeleteStep1Dialog) {
        AlertDialog(
            onDismissRequest = { showDeleteStep1Dialog = false },
            title = { Text("Удаление аккаунта", style = MaterialTheme.typography.titleMedium) },
            text = { Text("Вы уверены, что хотите удалить аккаунт?", style = MaterialTheme.typography.bodyMedium) },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteStep1Dialog = false
                    showDeleteStep2Dialog = true
                }) {
                    Text("Да")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteStep1Dialog = false }) {
                    Text("Отмена")
                }
            },
        )
    }

    if (showDeleteStep2Dialog) {
        AlertDialog(
            onDismissRequest = { showDeleteStep2Dialog = false },
            title = { Text("Введите пароль", style = MaterialTheme.typography.titleMedium) },
            text = {
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                    label = { Text("Пароль") },
                    shape = RoundedCornerShape(18.dp),
                )
            },
            confirmButton = {
                NextButton(
                    onClick = {
                        showDeleteStep2Dialog = false
                        showDeleteStep3Dialog = true
                    },
                )
            },
            dismissButton = {
                TextButton(onClick = { showDeleteStep2Dialog = false }) {
                    Text("Отмена")
                }
            },
        )
    }

    if (showDeleteStep3Dialog) {
        AlertDialog(
            onDismissRequest = { showDeleteStep3Dialog = false },
            title = { Text("Вы точно-точно уверены, что хотите удалить аккаунт?", style = MaterialTheme.typography.titleMedium) },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteStep3Dialog = false
                    onDeleteAccount()
                }) {
                    Text("Да")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteStep3Dialog = false }) {
                    Text("Отмена")
                }
            },
        )
    }
}
