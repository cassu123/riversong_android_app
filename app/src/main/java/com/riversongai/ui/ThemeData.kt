package com.riversongai.ui

import com.riversongai.utils.ThemeManager

data class ThemeOption(
    val key: String,
    val label: String,
    val primaryHex: String,
    val bgHex: String,
)

val APP_THEMES = listOf(
    ThemeOption(ThemeManager.THEME_DEFAULT,   "River Song",      "#96CBFF", "#0F1316"),
    ThemeOption(ThemeManager.THEME_HALO,      "Halo Blue",       "#35a7ff", "#080c13"),
    ThemeOption(ThemeManager.THEME_CRIMSON,   "Crimson Dark",    "#c53a1f", "#140c0b"),
    ThemeOption(ThemeManager.THEME_COMBAT,    "Combat",          "#3dcc79", "#0a100a"),
    ThemeOption(ThemeManager.THEME_VIOLET,    "Midnight Violet", "#9b6b9e", "#1a1025"),
    ThemeOption(ThemeManager.THEME_PEACH,     "Peach Dream",     "#D66C59", "#FEE7D9"),
    ThemeOption(ThemeManager.THEME_ARCTIC,    "Arctic",          "#4A7AA8", "#dce6f0"),
    ThemeOption(ThemeManager.THEME_CYBERPUNK, "Cyberpunk",       "#e8ff00", "#050505"),
    ThemeOption(ThemeManager.THEME_DUNE,      "Dune",            "#deb651", "#0a0804"),
)
