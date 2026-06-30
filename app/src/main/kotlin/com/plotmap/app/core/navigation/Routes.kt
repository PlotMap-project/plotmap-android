package com.plotmap.app.core.navigation
import android.net.Uri
import com.plotmap.app.core.models.EditorMode

sealed class Screen(val route: String) {
    object Splash : Screen("splash")

    object Auth : Screen("auth?isLoginMode={isLoginMode}") {
        fun createRoute(isLoginMode: Boolean) = "auth?isLoginMode=$isLoginMode"
    }

    object Greeting : Screen("greeting?userName={userName}&isRegistration={isRegistration}") {
        fun createRoute(
            userName: String,
            isRegistration: Boolean,
        ) = "greeting?userName=${Uri.encode(userName)}&isRegistration=$isRegistration"
    }

    object Home : Screen("home")

    object Settings : Screen("settings")

    object ProjectCreation : Screen("project_creation")

    object ProjectGeneration : Screen("project_generation")

    object Editor : Screen("editor/{projectId}?mode={mode}") {
        fun createRoute(
            projectId: String,
            mode: EditorMode = EditorMode.MANUAL,
        ) = "editor/$projectId?mode=${mode.name}"
    }

    object About : Screen("about")

    object Profile : Screen("profile")
}
