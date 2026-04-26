package com.riversongai.utils

import android.util.Log

object ErrorHandler {

    private const val TAG = "RiverSongError"

    fun logException(throwable: Throwable, message: String? = null) {
        Log.e(TAG, message ?: "Unexpected error", throwable)
    }

    fun getFriendlyMessage(throwable: Throwable): String {
        return when (throwable) {
            is java.net.UnknownHostException,
            is java.io.IOException -> Constants.ERROR_NETWORK_UNAVAILABLE
            is java.net.SocketTimeoutException -> "Request timed out. Please try again."
            is retrofit2.HttpException -> when (throwable.code()) {
                401 -> Constants.ERROR_UNAUTHORIZED
                403 -> "You don't have permission to do that."
                404 -> "Resource not found."
                500 -> "Server error. Please try again later."
                else -> Constants.ERROR_GENERIC
            }
            else -> Constants.ERROR_GENERIC
        }
    }
}
