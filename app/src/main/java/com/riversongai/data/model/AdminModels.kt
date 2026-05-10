package com.riversongai.data.model

data class AppUser(
    val id: String,
    val email: String,
    val displayName: String,
    val role: String, // "admin"|"user"|"parent"
    val isPending: Boolean
)

data class RoleUpdateBody(
    val role: String
)

data class KillSwitchStatus(
    val active: Boolean,
    val activatedAt: String?
)

data class KillSwitchResetBody(
    val password: String
)
