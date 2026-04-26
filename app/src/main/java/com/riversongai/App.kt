package com.riversongai

import android.app.Application
import com.riversongai.di.appModule
import com.riversongai.utils.ErrorHandler
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class App : Application() {

    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@App)
            modules(appModule)
        }

        Thread.setDefaultUncaughtExceptionHandler { _, throwable ->
            ErrorHandler.logException(throwable)
        }
    }
}
