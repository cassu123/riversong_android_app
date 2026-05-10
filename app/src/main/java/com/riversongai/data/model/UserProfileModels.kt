package com.riversongai.data.model

data class UserProfile(
    val displayName: String?,
    val callsign: String?,
    val theme: String?
)

data class UserProfileUpdate(
    val displayName: String? = null,
    val callsign: String? = null,
    val theme: String? = null
)
