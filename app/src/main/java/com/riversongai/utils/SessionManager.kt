@file:Suppress("DEPRECATION")
package com.riversongai.utils

import android.content.Context
import androidx.core.content.edit
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

class SessionManager(context: Context) {

    private val masterKey = MasterKey.Builder(context, MasterKey.DEFAULT_MASTER_KEY_ALIAS)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val prefs = try {
        createPrefs(context)
    } catch (e: Exception) {
        context.deleteSharedPreferences("river_song_secure_prefs")
        createPrefs(context)
    }

    private fun createPrefs(context: Context) = EncryptedSharedPreferences.create(
        context,
        "river_song_secure_prefs",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun saveAuthToken(token: String) = prefs.edit { putString(Constants.PREF_AUTH_TOKEN, token) }
    fun getAuthToken(): String? = prefs.getString(Constants.PREF_AUTH_TOKEN, null)
    fun getBearerToken(): String? = getAuthToken()?.let { "Bearer $it" }

    fun saveUserId(id: String) = prefs.edit { putString(Constants.PREF_USER_ID, id) }
    fun getUserId(): String? = prefs.getString(Constants.PREF_USER_ID, null)

    fun saveDisplayName(name: String) = prefs.edit { putString(Constants.PREF_DISPLAY_NAME, name) }
    fun getDisplayName(): String? = prefs.getString(Constants.PREF_DISPLAY_NAME, null)

    fun saveUserRole(role: String) = prefs.edit { putString(Constants.PREF_USER_ROLE, role) }
    fun getUserRole(): String? = prefs.getString(Constants.PREF_USER_ROLE, null)

    fun isAdmin(): Boolean = getUserRole() == "admin"
    fun getRole(): String = getUserRole() ?: "user"

    fun isLoggedIn(): Boolean = !getAuthToken().isNullOrBlank()

    fun clearSession() = prefs.edit { clear() }
}
