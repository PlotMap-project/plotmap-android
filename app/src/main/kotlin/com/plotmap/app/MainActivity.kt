package com.plotmap.app

import android.os.Bundle
import android.util.Log
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.core.os.LocaleListCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.plotmap.app.core.data.PreferencesManager
import com.plotmap.app.core.data.ProjectRepository
import com.plotmap.app.core.data.TokenManager
import com.plotmap.app.core.designsystem.PlotMapTheme
import com.plotmap.app.core.navigation.Screen
import com.plotmap.app.feature.auth.AuthScreen
import com.plotmap.app.feature.editor.creation.CreationChoiceScreen
import com.plotmap.app.feature.editor.generation.GenerationScreen
import com.plotmap.app.feature.editor.workspace.EditorScreen
import com.plotmap.app.feature.editor.workspace.EditorViewModel
import com.plotmap.app.feature.greeting.GreetingScreen
import com.plotmap.app.feature.home.HomeProjectItem
import com.plotmap.app.feature.home.HomeScreen
import com.plotmap.app.feature.profile.ProfileScreen
import com.plotmap.app.feature.settings.AboutScreen
import com.plotmap.app.feature.settings.SettingsScreen
import com.plotmap.app.feature.splash.SplashEvent
import com.plotmap.app.feature.splash.SplashScreen
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.compose.koinViewModel

class MainActivity : AppCompatActivity() {
    private val tokenManager: TokenManager by inject()
    private val preferencesManager: PreferencesManager by inject()
    private val projectRepository: ProjectRepository by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val language = preferencesManager.getLanguage()
        if (language.isNotEmpty()) {
            AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(language))
        }

        setContent {
            var isDarkTheme by remember {
                mutableStateOf(preferencesManager.getTheme() == "dark")
            }
            var fontSizeStr by remember {
                mutableStateOf(preferencesManager.getFontSize())
            }
            var listHeightStr by remember {
                mutableStateOf(preferencesManager.getListHeight())
            }

            PlotMapTheme(
                isDarkTheme = isDarkTheme,
                fontSizeStr = fontSizeStr,
            ) {
                PlotMapApp(
                    tokenManager = tokenManager,
                    preferencesManager = preferencesManager,
                    projectRepository = projectRepository,
                    isDarkTheme = isDarkTheme,
                    listHeightStr = listHeightStr,
                    onThemeChange = { isDarkTheme = it },
                    onFontSizeChange = { fontSizeStr = it },
                    onListHeightChange = { listHeightStr = it },
                )
            }
        }
    }
}

@Composable
fun PlotMapApp(
    tokenManager: TokenManager,
    preferencesManager: PreferencesManager,
    projectRepository: ProjectRepository,
    isDarkTheme: Boolean,
    listHeightStr: String,
    onThemeChange: (Boolean) -> Unit,
    onFontSizeChange: (String) -> Unit = {},
    onListHeightChange: (String) -> Unit = {},
) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    var sortBy by remember { mutableStateOf(preferencesManager.getSortBy()) }
    val projects = remember { mutableStateListOf<HomeProjectItem>() }
    val coroutineScope = rememberCoroutineScope()

    fun refreshProjects() {
        coroutineScope.launch {
            runCatching { projectRepository.loadProjects() }
                .onSuccess { loadedProjects ->
                    projects.clear()
                    projects.addAll(loadedProjects)
                }
                .onFailure {
                    projects.clear()
                }
        }
    }

    LaunchedEffect(currentRoute, tokenManager.getToken()) {
        if (currentRoute == Screen.Home.route && !tokenManager.getToken().isNullOrBlank()) {
            refreshProjects()
        }
    }

    val sortedProjects = sortProjects(projects, sortBy)
    NavHost(navController = navController, startDestination = Screen.Splash.route) {
        composable(Screen.Splash.route) {
            SplashScreen { event ->
                when (event) {
                    SplashEvent.NavigateToAuth ->
                        navController.navigate(Screen.Auth.createRoute(isLoginMode = false)) {
                            popUpTo(Screen.Splash.route) { inclusive = true }
                        }
                    is SplashEvent.NavigateToHome ->
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Splash.route) { inclusive = true }
                        }
                }
            }
        }
        composable(Screen.Auth.route) { backStackEntry ->
            val initialLoginModeStr = backStackEntry.arguments?.getString("isLoginMode") ?: "false"
            val initialLoginMode = initialLoginModeStr.toBoolean()
            AuthScreen(
                initialLoginMode = initialLoginMode,
                onLoginSuccess = { userName, isRegistration ->
                    val normalizedUserName = userName.trim()
                    if (isRegistration) {
                        projects.clear()
                    }
                    navController.navigate(Screen.Greeting.createRoute(normalizedUserName, isRegistration)) {
                        popUpTo(Screen.Auth.route) { inclusive = true }
                    }
                },
            )
        }
        composable(Screen.Greeting.route) { backStackEntry ->
            val userName = backStackEntry.arguments?.getString("userName") ?: ""
            val isRegistration = backStackEntry.arguments?.getString("isRegistration")?.toBoolean() ?: false
            GreetingScreen(userName = userName, isRegistration = isRegistration, onFinished = {
                navController.navigate(Screen.Home.route) {
                    popUpTo(Screen.Greeting.route) { inclusive = true }
                }
            })
        }
        composable(Screen.Home.route) {
            HomeScreen(
                listHeightStr = listHeightStr,
                projects = sortedProjects,
                onNavigateToCreate = { navController.navigate(Screen.ProjectCreation.route) },
                onNavigateToEditor = { projectId -> navController.navigate(Screen.Editor.createRoute(projectId)) },
                onRenameProject = { projectId, newTitle ->
                    if (newTitle.any { it.isLetter() }) {
                        coroutineScope.launch {
                            runCatching { projectRepository.renameProject(projectId, newTitle) }
                                .onSuccess {
                                    refreshProjects()
                                }
                        }
                    }
                },
                onDeleteProject = { projectId ->
                    coroutineScope.launch {
                        runCatching { projectRepository.deleteProject(projectId) }
                            .onSuccess {
                                refreshProjects()
                            }
                    }
                },
                onNavigateToProfile = { navController.navigate(Screen.Profile.route) },
            )
        }
        composable(Screen.Profile.route) {
            ProfileScreen(
                onBack = { navController.popBackStack() },
                onNavigateToSettings = { navController.navigate(Screen.Settings.route) },
                onLogout = {
                    tokenManager.clear()
                    navController.navigate(Screen.Auth.createRoute(isLoginMode = true)) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                },
                onDeleteAccount = {
                    tokenManager.clear()
                    navController.navigate(Screen.Auth.createRoute(isLoginMode = false)) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                },
            )
        }
        composable(Screen.Settings.route) {
            SettingsScreen(
                onBack = { navController.popBackStack() },
                onNavigateToAbout = { navController.navigate(Screen.About.route) },
                onLogout = {
                    tokenManager.clear()
                    navController.navigate(Screen.Auth.createRoute(isLoginMode = true)) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                },
                isDarkTheme = isDarkTheme,
                onThemeChange = { isDark ->
                    onThemeChange(isDark)
                    preferencesManager.setTheme(if (isDark) "dark" else "light")
                },
                onFontSizeChange = onFontSizeChange,
                onListHeightChange = onListHeightChange,
                onSortByChange = { sortBy = it },
            )
        }
        composable(Screen.About.route) {
            AboutScreen(
                onBack = { navController.popBackStack() },
            )
        }
        composable(Screen.ProjectCreation.route) {
            CreationChoiceScreen(
                onCreateProject = { name, description, sourceText, isAiGenerated ->
                    coroutineScope.launch {
                        kotlinx.coroutines.delay(100)
                        val fakeId = java.util.UUID.randomUUID().toString()

                        val createdProject =
                            HomeProjectItem(
                                id = fakeId,
                                title = name,
                                description = description,
                                isAiGenerated = false,
                                createdAt = System.currentTimeMillis(),
                                modifiedAt = System.currentTimeMillis(),
                            )

                        Log.d("PlotMapNav", "Заглушка: Создан локальный проект с ID: $fakeId")

                        projects.add(0, createdProject)
                        navController.navigate(Screen.Editor.createRoute(createdProject.id)) {
                            popUpTo(Screen.ProjectCreation.route) { inclusive = true }
                        }
                    }
                },
                onBack = { navController.popBackStack() },
            )
        }
        composable(Screen.ProjectGeneration.route) {
            GenerationScreen(
                onGenerationComplete = { newProjectId ->
                    navController.navigate(Screen.Editor.createRoute(newProjectId)) {
                        popUpTo(Screen.Home.route)
                    }
                },
            )
        }
        composable(Screen.Editor.route) { backStackEntry ->
            val projectId = backStackEntry.arguments?.getString("projectId") ?: ""
            val viewModel: EditorViewModel = koinViewModel()

            EditorScreen(
                projectId = projectId,
                viewModel = viewModel,
                onBackToHome = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Home.route) { inclusive = false }
                        launchSingleTop = true
                    }
                },
            )
        }
    }
}

private fun sortProjects(
    projects: List<HomeProjectItem>,
    sortBy: String,
): List<HomeProjectItem> =
    when (sortBy) {
        "date_created" -> sortByDateCreated(projects)
        "name" -> sortByName(projects)
        else -> sortByDateModified(projects)
    }

private fun sortByDateModified(projects: List<HomeProjectItem>): List<HomeProjectItem> =
    projects.sortedWith(compareByDescending<HomeProjectItem> { it.modifiedAt }.thenBy { it.title.lowercase() })

private fun sortByDateCreated(projects: List<HomeProjectItem>): List<HomeProjectItem> =
    projects.sortedWith(compareByDescending<HomeProjectItem> { it.createdAt }.thenBy { it.title.lowercase() })

private fun sortByName(projects: List<HomeProjectItem>): List<HomeProjectItem> =
    projects.sortedWith(compareBy<HomeProjectItem> { it.title.lowercase() }.thenByDescending { it.modifiedAt })
