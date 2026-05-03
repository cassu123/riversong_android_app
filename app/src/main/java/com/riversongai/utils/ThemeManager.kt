package com.riversongai.utils

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import com.riversongai.R

object ThemeManager {
    private const val PREFS_NAME = "river_song_prefs"
    private const val KEY_THEME = "app_theme"

    const val THEME_DEFAULT = "default"
    const val THEME_DARK = "dark"
    const val THEME_OCEAN = "ocean"
    const val THEME_SUNSET = "sunset"

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

    fun applyTheme(context: Context) {
        val themeKey = getSelectedTheme(context)
        when (themeKey) {
            THEME_DARK -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            THEME_DEFAULT -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            else -> {
                // Ocean and Sunset are custom themes applied in Activity.setTheme()
                // But we should also decide on night mode for them.
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
            }
        }
    }

    fun applyThemeToActivity(context: Context) {
        val themeKey = getSelectedTheme(context)
        when (themeKey) {
            THEME_OCEAN -> context.setTheme(R.style.Theme_RiverSong_Ocean)
            THEME_SUNSET -> context.setTheme(R.style.Theme_RiverSong_Sunset)
            else -> context.setTheme(R.style.Theme_RiverSong)
        }
    }
}
