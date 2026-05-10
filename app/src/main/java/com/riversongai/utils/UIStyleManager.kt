package com.riversongai.utils

import android.content.Context
import android.util.TypedValue
import androidx.annotation.AttrRes

object UIStyleManager {

    fun resolveCardColor(context: Context, depth: Int): Int {
        val attr = when (depth) {
            0 -> com.google.android.material.R.attr.colorSurface
            1 -> com.google.android.material.R.attr.colorSurfaceContainer
            else -> com.google.android.material.R.attr.colorSurfaceContainerHigh
        }
        return resolveAttrColor(context, attr)
    }

    fun resolveAttrColor(context: Context, @AttrRes attr: Int): Int {
        val typedValue = TypedValue()
        context.theme.resolveAttribute(attr, typedValue, true)
        return typedValue.data
    }
}
