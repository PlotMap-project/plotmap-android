package com.plotmap.app.feature.auth
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.plotmap.app.core.coroutines.DispatcherProvider
import com.plotmap.app.core.data.TokenManager
import com.plotmap.app.core.network.ApiErrorResponse
import com.plotmap.app.core.network.AuthResponse
import com.plotmap.app.core.network.LoginEmailRequest
import com.plotmap.app.core.network.LoginNameRequest
import com.plotmap.app.core.network.PlotMapApi
import com.plotmap.app.core.network.RegisterRequest
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import retrofit2.HttpException

data class AuthUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
)

class AuthViewModel(
    private val tokenManager: TokenManager,
    private val plotMapApi: PlotMapApi,
    private val dispatchers: DispatcherProvider,
) : ViewModel() {
    private val _navigationEvent = MutableSharedFlow<AuthEvent>()
    val navigationEvent = _navigationEvent.asSharedFlow()
    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState = _uiState.asStateFlow()
    private val json = Json { ignoreUnknownKeys = true }

    fun register(
        email: String,
        userName: String,
        password: String,
    ) {
        performAuth(isRegistration = true) {
            plotMapApi.register(
                RegisterRequest(
                    email = email.trim(),
                    password = password,
                    name = userName.trim(),
                ),
            )
        }
    }

    fun login(
        loginOrEmail: String,
        password: String,
    ) {
        val trimmedLogin = loginOrEmail.trim()
        performAuth(isRegistration = false) {
            if (trimmedLogin.contains("@")) {
                plotMapApi.login(
                    LoginEmailRequest(
                        email = trimmedLogin,
                        password = password,
                    ),
                )
            } else {
                plotMapApi.loginByName(
                    LoginNameRequest(
                        name = trimmedLogin,
                        password = password,
                    ),
                )
            }
        }
    }

    private fun performAuth(
        isRegistration: Boolean,
        request: suspend () -> AuthResponse,
    ) {
        viewModelScope.launch {
            _uiState.value = AuthUiState(isLoading = true)
            runCatching { withContext(dispatchers.io) { request() } }
                .onSuccess { response ->
                    tokenManager.saveToken(response.token.trim())
                    tokenManager.saveUserName(response.name)
                    _uiState.value = AuthUiState()
                    _navigationEvent.emit(AuthEvent.NavigateToGreeting(response.name, isRegistration))
                }
                .onFailure { throwable ->
                    _uiState.value =
                        AuthUiState(
                            errorMessage = throwable.toUserMessage(),
                        )
                }
        }
    }

    private fun Throwable.toUserMessage(): String =
        when (this) {
            is HttpException -> {
                val body = response()?.errorBody()?.string().orEmpty()
                val apiError = runCatching { json.decodeFromString<ApiErrorResponse>(body) }.getOrNull()
                when {
                    apiError?.error == "INVALID_CREDENTIALS" -> "Неверный логин или пароль"
                    !apiError?.message.isNullOrBlank() -> apiError?.message.orEmpty()
                    else -> message.orEmpty().ifBlank { "Ошибка авторизации" }
                }
            }
            else -> message.orEmpty().ifBlank { "Ошибка сети" }
        }
}

sealed class AuthEvent {
    data class NavigateToGreeting(val userName: String, val isRegistration: Boolean) : AuthEvent()
}
