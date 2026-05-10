package com.riversongai.utils

import android.content.Context

object UIStyleManager {
    private const val PREF_MV_MODE = "mv_mode_enabled"

    fun isMVModeEnabled(context: Context): Boolean =
        context.getSharedPreferences("rs_prefs", Context.MODE_PRIVATE)
            .getBoolean(PREF_MV_MODE, false)

    fun setMVMode(context: Context, enabled: Boolean) {
        context.getSharedPreferences("rs_prefs", Context.MODE_PRIVATE)
            .edit().putBoolean(PREF_MV_MODE, enabled).apply()
    }

    // Returns the appropriate card color attribute based on MV mode
    // depth: 0 = base, 1 = raised, 2 = top (closest to viewer)
    fun resolveCardColor(context: Context, depth: Int): Int {
        val attr = if (isMVModeEnabled(context)) {
            when (depth) {
                0 -> com.google.android.material.R.attr.colorSurface
                1 -> com.google.android.material.R.attr.colorSurfaceContainer
                else -> com.google.android.material.R.attr.colorSurfaceContainerHigh
            }
        } else {
            com.google.android.material.R.attr.colorSurfaceContainer
        }
        val typedValue = android.util.TypedValue()
        context.theme.resolveAttribute(attr, typedValue, true)
        return typedValue.data
    }
}
