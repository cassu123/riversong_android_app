package com.riversongai.utils

import com.riversongai.BuildConfig

object Constants {

    // Set per build type in app/build.gradle:
    //   debug   -> http://10.0.2.2:5000/  (local server, change to your LAN IP for a real device)
    //   release -> https://riversongai.com/ (through Cloudflare)
    val BASE_URL: String = BuildConfig.BASE_URL

    const val PREF_AUTH_TOKEN = "auth_token"
    const val PREF_USER_ID = "user_id"
    const val PREF_USERNAME = "username"
    const val PREF_USER_ROLE = "user_role"

    const val ERROR_NETWORK_UNAVAILABLE = "Network unavailable. Please check your connection."
    const val ERROR_GENERIC = "An unexpected error occurred. Please try again."
    const val ERROR_UNAUTHORIZED = "Authentication failed. Please log in again."
}
