package com.riversongai.data.model

data class HomeStatus(
    val configured: Boolean,
    val reachable: Boolean,
    val url: String?
)
