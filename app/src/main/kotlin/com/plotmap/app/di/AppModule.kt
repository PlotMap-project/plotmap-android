package com.plotmap.app.di
import android.content.Context
import com.plotmap.app.core.data.PreferencesManager
import com.plotmap.app.core.data.ProjectRepository
import com.plotmap.app.core.data.TokenManager
import com.plotmap.app.feature.auth.AuthViewModel
import com.plotmap.app.feature.splash.SplashViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule =
    module {
        single { TokenManager(get<Context>()) }
        single { PreferencesManager(get<Context>()) }
        single { ProjectRepository(get()) }
        viewModel { SplashViewModel(get()) }
        viewModel { AuthViewModel(get(), get()) }
    }
