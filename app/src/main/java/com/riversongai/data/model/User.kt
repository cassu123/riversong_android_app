package com.riversongai.data.model

enum class UserRole {
    ADMIN,
    PARENT,
    CHILD,
    GUEST,
    UNKNOWN
}

data class User(
    val id: String,
    val username: String,
    val email: String,
    val firstName: String? = null,
    val lastName: String? = null,
    val role: UserRole,
    val isActive: Boolean = true,
    val profilePictureUrl: String? = null,
    val lastLogin: Long? = null
)
