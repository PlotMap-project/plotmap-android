package com.plotmap.app.feature.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.plotmap.app.core.designsystem.components.AuthFields
import com.plotmap.app.core.designsystem.components.NextButton
import org.koin.androidx.compose.koinViewModel

@Composable
fun AuthScreen(
    initialLoginMode: Boolean = false,
    viewModel: AuthViewModel = koinViewModel(),
    onLoginSuccess: (String, Boolean) -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()
    var isLoginMode by remember { mutableStateOf(initialLoginMode) }
    var email by remember { mutableStateOf("") }
    var userName by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordRepeat by remember { mutableStateOf("") }

    val hasCyrillic = password.any { it in 'а'..'я' || it in 'А'..'Я' || it == 'ё' || it == 'Ё' }
    val hasDigit = password.any { it.isDigit() }
    val hasLatinLetter = password.any { it in 'a'..'z' || it in 'A'..'Z' }
    val isLengthValid = password.length >= 8

    val passwordErrorMessage =
        when {
            password.isEmpty() -> null
            hasCyrillic -> "Пароль должен состоять только из латинских букв и цифр"
            !isLengthValid -> "Длина пароля должна быть не менее 8 символов"
            !hasDigit -> "В пароле должна быть хотя бы одна цифра"
            !hasLatinLetter -> "В пароле должна быть хотя бы одна латинская буква"
            else -> null
        }

    val isPasswordValid = password.isNotEmpty() && passwordErrorMessage == null
    val isPasswordRepeatValid = passwordRepeat.isNotEmpty() && password == passwordRepeat

    LaunchedEffect(Unit) {
        viewModel.navigationEvent.collect { event ->
            when (event) {
                is AuthEvent.NavigateToGreeting -> onLoginSuccess(event.userName.ifBlank { "User" }, event.isRegistration)
            }
        }
    }

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        if (isLoginMode) {
            Text("Вход", color = MaterialTheme.colorScheme.onBackground, style = MaterialTheme.typography.headlineMedium)
            AuthFields.LoginFields(
                userName = userName,
                password = password,
                onUserNameChange = { userName = it },
                onPasswordChange = { password = it },
            )
            NextButton(
                onClick = { viewModel.login(userName, password) },
                modifier = Modifier.padding(top = 24.dp),
                enabled = userName.isNotBlank() && password.isNotBlank() && !uiState.isLoading,
            )
            uiState.errorMessage?.let { errorText ->
                Text(
                    text = errorText,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }
            Spacer(modifier = Modifier.height(32.dp))
            Text(
                "Регистрация через VK или Google",
                color = MaterialTheme.colorScheme.onBackground,
            )
            Text(
                "Нет аккаунта? Зарегистрируйтесь",
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                style = MaterialTheme.typography.bodySmall,
                modifier =
                    Modifier
                        .padding(top = 16.dp)
                        .clickable { isLoginMode = false },
            )
        } else {
            Text("Регистрация", color = MaterialTheme.colorScheme.onBackground, style = MaterialTheme.typography.headlineMedium)
            AuthFields.RegistrationFields(
                email = email,
                userName = userName,
                password = password,
                passwordRepeat = passwordRepeat,
                isUsernameTaken = false,
                passwordErrorMessage = passwordErrorMessage,
                isPasswordValid = isPasswordValid,
                isPasswordRepeatValid = isPasswordRepeatValid,
                onEmailChange = { email = it },
                onUserNameChange = { userName = it },
                onPasswordChange = { password = it },
                onPasswordRepeatChange = { passwordRepeat = it },
            )
            NextButton(
                onClick = { viewModel.register(email, userName, password) },
                modifier = Modifier.padding(top = 24.dp),
                enabled = isPasswordValid && isPasswordRepeatValid && email.isNotBlank() && userName.isNotBlank() && !uiState.isLoading,
            )
            uiState.errorMessage?.let { errorText ->
                Text(
                    text = errorText,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }
            Spacer(modifier = Modifier.height(32.dp))
            Text(
                "Регистрация через VK или Google",
                color = MaterialTheme.colorScheme.onBackground,
            )
            Text(
                "Уже есть аккаунт? Войдите",
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                style = MaterialTheme.typography.bodySmall,
                modifier =
                    Modifier
                        .padding(top = 16.dp)
                        .clickable { isLoginMode = true },
            )
        }
        Spacer(modifier = Modifier.height(48.dp))
        Button(onClick = { onLoginSuccess("TestUser", false) }) {
            Text("DEBUG: Войти как TestUser")
        }
    }
}
