// app/src/main/java/com/riversongai/di/AppModule.kt
//
// This file defines the Koin module for dependency injection within the
// River Song Android application. It specifies how various components,
// such as API services and repositories, should be created and provided
// throughout the app, promoting a loosely coupled and testable architecture.

package com.riversongai.di

import com.riversongai.data.remote.RiverSongApiService
import com.riversongai.data.repository.SmartHomeRepository
import com.riversongai.data.repository.UserRepository
import org.koin.dsl.module
import android.util.Log // Added Log import

// Section: Koin Application Module
// This is a Koin module that defines how your application's dependencies
// are provided. It uses the 'module' function from Koin DSL.
val appModule = module {

    // Section: API Service Provider
    // Provides a singleton instance of RiverSongApiService.
    // 'single' means that only one instance of RiverSongApiService will be created
    // and reused throughout the application's lifecycle.
    single {
        // 'get()' here retrieves the baseUrl that was passed to the Koin start.
        // In App.kt, this would be passed when starting Koin modules.
        // For simplicity, we'll use a direct reference for now.
        // It should ideally be injected or come from a config.
        val baseUrl = "YOUR_BACKEND_BASE_URL" // This should match the one in App.kt and passed to Koin
        RiverSongApiService.create(baseUrl)
    }

    // Section: User Repository Provider
    // Provides a singleton instance of UserRepository.
    // 'get()' here automatically injects the RiverSongApiService
    // (which Koin knows how to provide from the 'single' above).
    single {
        UserRepository(get())
    }

    // Section: Smart Home Repository Provider
    // Provides a singleton instance of SmartHomeRepository.
    // 'get()' here automatically injects the RiverSongApiService
    // into the SmartHomeRepository's constructor.
    single {
        SmartHomeRepository(get())
    }

    // Section: Other Component Providers (Placeholder)
    // You would add more 'single' or 'factory' definitions here
    // for other dependencies like ViewModels, UseCases, or local database DAOs.
    // For example:
    // factory { MyViewModel(get(), get()) } // 'factory' provides a new instance every time it's requested.

    // Section: Logger for Module Setup
    // Logging to confirm that the module is being loaded correctly.
    Log.d("AppModule", "Koin module 'appModule' loaded successfully.")
}