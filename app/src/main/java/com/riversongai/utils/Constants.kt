package com.riversongai.utils

object Constants {

    // Update this to your backend IP/domain before running
    const val BASE_URL = "http://YOUR_BACKEND_IP:5000/"

    const val PREF_AUTH_TOKEN = "auth_token"
    const val PREF_USER_ID = "user_id"
    const val PREF_USERNAME = "username"
    const val PREF_USER_ROLE = "user_role"

    const val ERROR_NETWORK_UNAVAILABLE = "Network unavailable. Please check your connection."
    const val ERROR_GENERIC = "An unexpected error occurred. Please try again."
    const val ERROR_UNAUTHORIZED = "Authentication failed. Please log in again."
}
