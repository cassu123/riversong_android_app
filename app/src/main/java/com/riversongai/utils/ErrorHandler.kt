// app/src/main/java/com/riversongai/utils/ErrorHandler.kt
//
// This file defines a utility object for centralizing error and exception handling
// within the River Song Android application. It provides methods for consistent
// logging of errors and exceptions, and can be extended to provide user-friendly
// error messages or integrate with crash reporting services.

package com.riversongai.utils

import android.util.Log

// Section: ErrorHandler Object Definition
// An object declaration creates a singleton, making ErrorHandler globally accessible
// without needing explicit instantiation or dependency injection for simple utility functions.
object ErrorHandler {

    // Section: Constants
    // Tag for logging within this error handler.
    private const val TAG = "RiverSongErrorHandler"

    // Section: Log Exception
    // Logs a Throwable (exception) with a stack trace.
    // This is useful for catching and reporting unexpected errors in a structured way.
    fun logException(throwable: Throwable, message: String? = null) {
        val errorMessage = message ?: "An unexpected error occurred."
        Log.e(TAG, errorMessage, throwable)
        // In a production app, you would also send this to a crash reporting service
        // like Firebase Crashlytics, Sentry, etc.
        // Example: Crashlytics.recordException(throwable)
    }

    // Section: Log Error Message
    // Logs a simple error message.
    // Useful for non-exception-based error states or for additional context.
    fun logError(tag: String, message: String) {
        Log.e(tag, message)
    }

    // Section: Get User-Friendly Message from Exception
    // Converts a technical exception into a more readable message for the end-user.
    // This improves the user experience by avoiding raw technical details.
    fun getFriendlyMessage(throwable: Throwable): String {
        return when (throwable) {
            is java.net.UnknownHostException -> Constants.ERROR_NETWORK_UNAVAILABLE
            is java.net.SocketTimeoutException -> "Request timed out. Please try again."
            is java.io.IOException -> Constants.ERROR_NETWORK_UNAVAILABLE
            is retrofit2.HttpException -> {
                // Handle specific HTTP error codes
                when (throwable.code()) {
                    401 -> Constants.ERROR_UNAUTHORIZED // Unauthorized
                    403 -> "You don't have permission to perform this action." // Forbidden
                    404 -> "Resource not found."
                    500 -> "Server error. Please try again later."
                    else -> Constants.ERROR_GENERIC
                }
            }
            else -> Constants.ERROR_GENERIC
        }
    }

    // Section: Show Error Toast (UI-related, optional direct handling)
    // You might also have a method to directly show a Toast message on the UI layer.
    // This requires a Context, so it's often better handled by the Fragment/Activity.
    // fun showToastError(context: Context, throwable: Throwable) {
    //     Toast.makeText(context, getFriendlyMessage(throwable), Toast.LENGTH_LONG).show()
    // }
}