package com.plotmap.app.feature.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.plotmap.app.core.data.TokenManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

class SplashViewModel(
    private val tokenManager: TokenManager,
) : ViewModel() {
    private val _navigationEvent = MutableSharedFlow<SplashEvent>()
    val navigationEvent = _navigationEvent.asSharedFlow()

    init {
        checkAuth()
    }

    private fun checkAuth() {
        viewModelScope.launch {
            delay(2500)
            val token = tokenManager.getToken()
            if (!token.isNullOrBlank()) {
                _navigationEvent.emit(SplashEvent.NavigateToHome(tokenManager.getUserName().orEmpty()))
            } else {
                _navigationEvent.emit(SplashEvent.NavigateToAuth)
            }
        }
    }
}

sealed class SplashEvent {
    data class NavigateToHome(val userName: String) : SplashEvent()

    object NavigateToAuth : SplashEvent()
}
