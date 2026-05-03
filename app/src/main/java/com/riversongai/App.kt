package com.riversongai

import android.app.Application
import com.riversongai.di.appModule
import com.riversongai.utils.ErrorHandler
import com.riversongai.utils.NotificationHelper
import com.riversongai.utils.ThemeManager
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class App : Application() {

    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@App)
            modules(appModule)
        }

        ThemeManager.initialize(this)
        NotificationHelper.createNotificationChannel(this)

        Thread.setDefaultUncaughtExceptionHandler { _, throwable ->
            ErrorHandler.logException(throwable)
        }
    }
}
