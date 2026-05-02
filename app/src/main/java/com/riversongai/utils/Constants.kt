package com.riversongai.utils

import com.riversongai.BuildConfig

object Constants {

    val BASE_URL: String = BuildConfig.BASE_URL

    const val PREF_AUTH_TOKEN = "auth_token"
    const val PREF_USER_ID = "user_id"
    const val PREF_DISPLAY_NAME = "display_name"
    const val PREF_USER_ROLE = "user_role"

    const val ERROR_NETWORK_UNAVAILABLE = "Network unavailable. Please check your connection."
    const val ERROR_GENERIC = "An unexpected error occurred. Please try again."
    const val ERROR_UNAUTHORIZED = "Your session has expired. Please sign in again."
}
