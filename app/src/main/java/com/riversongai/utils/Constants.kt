// app/src/main/java/com/riversongai/utils/Constants.kt
//
// This file defines application-wide constant values used throughout the River Song
// Android application. Centralizing these values helps maintain consistency,
// reduces the risk of typos, and makes it easier to update common configurations.

package com.riversongai.utils

// Section: Constants Object Definition
// An object declaration in Kotlin creates a singleton instance, perfect for
// holding constant values that are globally accessible.
object Constants {

    // Section: Network and API Configuration
    // Base URL for the River Song AI backend API.
    // This should be updated to your deployed backend's URL.
    const val BASE_URL = "http://YOUR_DEPLOYED_BACKEND_IP_OR_DOMAIN:5000/"

    // API endpoint paths (examples)
    const val API_VERSION_V1 = "/api/v1"
    const val ENDPOINT_AUTH_LOGIN = "$API_VERSION_V1/auth/login"
    const val ENDPOINT_DEVICES = "$API_VERSION_V1/devices"
    const val ENDPOINT_USERS_ME = "$API_VERSION_V1/users/me"
    // Add more API endpoints as you define them in your backend and RiverSongApiService.

    // Section: SharedPreferences Keys
    // Keys used for storing data securely or persistently in SharedPreferences.
    // Example: Authentication token storage.
    const val PREF_AUTH_TOKEN = "auth_token"
    const val PREF_USER_ID = "user_id"
    const val PREF_USERNAME = "username"
    const val PREF_USER_ROLE = "user_role"

    // Section: Permission Request Codes
    // Unique request codes for runtime permissions.
    const val PERMISSION_REQUEST_CODE_CAMERA = 1001
    const val PERMISSION_REQUEST_CODE_RECORD_AUDIO = 1002
    const val PERMISSION_REQUEST_CODE_LOCATION = 1003
    const val PERMISSION_REQUEST_CODE_BLUETOOTH = 1004
    const val PERMISSION_REQUEST_CODE_STORAGE = 1005
    const val PERMISSION_REQUEST_CODE_NOTIFICATIONS = 1006
    const val PERMISSION_REQUEST_CODE_ALL = 1000 // General request code for multiple permissions

    // Section: Logging Tags
    // Common tags for logging, providing context in Logcat.
    const val LOG_TAG_AUTH = "RiverSongAuth"
    const val LOG_TAG_DEVICE_CONTROL = "RiverSongDeviceControl"
    const val LOG_TAG_USER_DASHBOARD = "RiverSongUserDashboard"
    const val LOG_TAG_API = "RiverSongAPI"

    // Section: UI Related Constants (Optional)
    // Example: Time duration for Toast messages or animation durations.
    const val TOAST_LONG_DURATION = 5000 // milliseconds
    const val ANIM_DURATION_SHORT = 300L // milliseconds

    // Section: Default Values
    // Default values or limits used within the application.
    const val DEFAULT_TEMPERATURE_CELSIUS = 22.0f
    const val MAX_BRIGHTNESS_PERCENT = 100
    const val MIN_BRIGHTNESS_PERCENT = 0

    // Section: Error Messages (Examples)
    // Common error messages to display to the user.
    const val ERROR_NETWORK_UNAVAILABLE = "Network unavailable. Please check your connection."
    const val ERROR_GENERIC = "An unexpected error occurred. Please try again."
    const val ERROR_UNAUTHORIZED = "Authentication failed. Please log in again."
}