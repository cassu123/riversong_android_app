package com.riversongai.utils

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import com.riversongai.R
import com.riversongai.data.model.UserProfileUpdate
import com.riversongai.data.remote.RiverSongApiService

object ThemeManager {
    private const val PREFS_NAME = "river_song_prefs"
    private const val KEY_THEME = "app_theme"

    const val THEME_DEFAULT = "default"
    const val THEME_HALO = "halo"
    const val THEME_CRIMSON = "crimson"
    const val THEME_COMBAT = "combat"
    const val THEME_VIOLET = "violet"
    const val THEME_PEACH = "peach"
    const val THEME_ARCTIC = "arctic"
    const val THEME_CYBERPUNK = "cyberpunk"
    const val THEME_DUNE = "dune"

    fun initialize(context: Context) {
        applyTheme(context)
    }

    fun setTheme(context: Context, themeKey: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_THEME, themeKey).apply()
        applyTheme(context)
    }

    fun getSelectedTheme(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_THEME, THEME_DEFAULT) ?: THEME_DEFAULT
    }

    // Called after login to pull server theme into local prefs
    suspend fun syncThemeFromServer(context: Context, apiService: RiverSongApiService) {
        try {
            val response = apiService.getUserProfile()
            if (response.isSuccessful) {
                val serverTheme = response.body()?.theme
                if (!serverTheme.isNullOrBlank()) {
                    context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                        .edit().putString(KEY_THEME, serverTheme).apply()
                    applyTheme(context)
                }
            }
        } catch (e: Exception) {
            // Silently fall back to local theme — server sync is best-effort
        }
    }

    // Called when user selects a theme in UserDashboardScreen
    suspend fun saveThemeToServer(context: Context, apiService: RiverSongApiService, themeKey: String) {
        try {
            apiService.updateUserProfile(UserProfileUpdate(theme = themeKey))
        } catch (e: Exception) {
            // Silently fail — local theme is already applied
        }
    }

    fun applyTheme(context: Context) {
        val themeKey = getSelectedTheme(context)
        when (themeKey) {
            THEME_PEACH, THEME_ARCTIC -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
            else -> {
                // Most of our custom themes are dark by design
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            }
        }
    }

    fun applyThemeToActivity(context: Context) {
        val themeKey = getSelectedTheme(context)
        val themeResId = when (themeKey) {
            THEME_HALO -> R.style.Theme_RiverSong_Halo
            THEME_CRIMSON -> R.style.Theme_RiverSong_Crimson
            THEME_COMBAT -> R.style.Theme_RiverSong_Combat
            THEME_VIOLET -> R.style.Theme_RiverSong_Violet
            THEME_PEACH -> R.style.Theme_RiverSong_Peach
            THEME_ARCTIC -> R.style.Theme_RiverSong_Arctic
            THEME_CYBERPUNK -> R.style.Theme_RiverSong_Cyberpunk
            THEME_DUNE -> R.style.Theme_RiverSong_Dune
            else -> R.style.Theme_RiverSong
        }
        context.setTheme(themeResId)
    }
}
