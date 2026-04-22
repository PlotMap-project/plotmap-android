package com.plotmap.app
import android.app.Application
import com.plotmap.app.di.appModule
import com.plotmap.app.di.networkModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class PlotMapApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@PlotMapApplication)
            modules(appModule, networkModule)
        }
    }
}
