package com.riversongai.utils

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

class SessionManager(context: Context) {

    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val prefs = EncryptedSharedPreferences.create(
        context,
        "river_song_secure_prefs",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun saveAuthToken(token: String) {
        prefs.edit().putString(Constants.PREF_AUTH_TOKEN, token).apply()
    }

    fun getAuthToken(): String? = prefs.getString(Constants.PREF_AUTH_TOKEN, null)

    fun getBearerToken(): String? = getAuthToken()?.let { "Bearer $it" }

    fun saveUserId(id: String) {
        prefs.edit().putString(Constants.PREF_USER_ID, id).apply()
    }

    fun getUserId(): String? = prefs.getString(Constants.PREF_USER_ID, null)

    fun saveUsername(username: String) {
        prefs.edit().putString(Constants.PREF_USERNAME, username).apply()
    }

    fun getUsername(): String? = prefs.getString(Constants.PREF_USERNAME, null)

    fun saveUserRole(role: String) {
        prefs.edit().putString(Constants.PREF_USER_ROLE, role).apply()
    }

    fun getUserRole(): String? = prefs.getString(Constants.PREF_USER_ROLE, null)

    fun isLoggedIn(): Boolean = !getAuthToken().isNullOrBlank()

    fun clearSession() {
        prefs.edit().clear().apply()
    }
}
