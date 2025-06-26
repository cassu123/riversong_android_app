// app/src/main/java/com/riversongai/App.kt
//
// This file serves as the main entry point for the River Song Android application,
// extending the Android Application class to handle global application-level
// initialization and setup that needs to occur once when the app starts.

package com.riversongai

import android.app.Application
import android.util.Log

// Section: Application-level Initialization
// This class extends Application, allowing it to manage global application state
// and perform setup tasks that are required before any activities or services run.
class App : Application() {

    // Section: Companion Object for Global Access
    // A companion object provides a way to define static-like members
    // that can be accessed globally throughout the application.
    companion object {
        private const val TAG = "RiverSongApp"
        // Placeholder for an API service instance, allowing global access to the backend.
        // This will be initialized during the application's onCreate.
        lateinit var riverSongApiService: com.riversongai.data.remote.RiverSongApiService
    }

    // Section: Application Creation Lifecycle
    // Called when the application is first created, before any activities,
    // services, or receiver objects (except content providers) have been created.
    // Use this for one-time initialization processes.
    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "River Song Application started.")

        // Initialize the API Service to communicate with the Python backend.
        // This assumes RiverSongApiService is set up to handle HTTP requests.
        // Replace "YOUR_BACKEND_BASE_URL" with the actual URL of your deployed Python API.
        // For example: "http://192.168.1.100:5000" or "https://your-riversong-api.com"
        // Ensure your backend is reachable from the device/emulator.
        initializeApiService("YOUR_BACKEND_BASE_URL")

        // Section: Global Error Handling (Optional)
        // Set up a default uncaught exception handler to log crashes.
        // In a production app, you might integrate with a crash reporting tool here.
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            Log.e(TAG, "Uncaught exception on thread: ${thread.name}", throwable)
            // You might want to pass this to a more robust error reporting mechanism
            // like Firebase Crashlytics or directly to your ErrorHandler backend.
            // For now, it just logs.
            com.riversongai.utils.ErrorHandler.logException(throwable)
        }

        // Section: Dependency Injection Setup (Placeholder)
        // If you choose to use a Dependency Injection framework like Hilt or Koin,
        // this is where you would typically start the DI container or module loading.
        // Example with Koin: startKoin { modules(appModule) }
        // For Hilt, it's often handled by annotations, but initialization specific to app scope might go here.
        Log.d(TAG, "Dependency injection setup (if any) initialized.")

        // Section: Other Global Initializations
        // Add any other global initializations needed for your application here,
        // such as:
        // - Initializing third-party SDKs (e.g., analytics, push notifications)
        // - Database initialization (e.g., Room database)
        // - Preferences manager setup
        // - Theme or UI configuration that applies globally
        Log.d(TAG, "Other global initializations complete.")
    }

    // Section: API Service Initialization
    // Private function to initialize the RiverSongApiService.
    // It takes the base URL of your backend API.
    private fun initializeApiService(baseUrl: String) {
        // Here you would instantiate your Retrofit/OkHttp client
        // and build your RiverSongApiService instance.
        // This is a placeholder for the actual network client setup.
        riverSongApiService = com.riversongai.data.remote.RiverSongApiService.create(baseUrl)
        Log.d(TAG, "RiverSongApiService initialized with base URL: $baseUrl")
    }

    // Section: Application Termination Lifecycle (Optional)
    // Called when the application is about to terminate.
    // This method is not guaranteed to be called in all scenarios,
    // so critical cleanup should not solely rely on it.
    override fun onTerminate() {
        super.onTerminate()
        Log.d(TAG, "River Song Application terminating.")
        // Perform any necessary cleanup here, though it's not always reliable.
    }
}