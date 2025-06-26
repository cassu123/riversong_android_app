// app/src/main/java/com/riversongai/data/model/User.kt
//
// This file defines the data model for a user within the River Song Android application.
// It represents a user profile, including their unique identifier, personal details,
// and assigned role, as managed by the backend user management system.

package com.riversongai.data.model

// Section: User Role Enumeration
// Defines the different roles a user can have within the River Song system.
// This matches the roles described in the backend user management (Admin, Parent, Child, Guest).
enum class UserRole {
    ADMIN,
    PARENT,
    CHILD,
    GUEST,
    // Add other roles as necessary if defined in your backend
    UNKNOWN // For handling roles not explicitly defined or new roles from backend
}

// Section: User Data Class Definition
// A data class to encapsulate all relevant information for a user.
// It includes properties for identification, personal details, and their role.
data class User(
    // Section: User Identifier
    // Unique identifier for the user, typically generated and managed by the backend.
    val id: String,

    // Section: Authentication and Basic Information
    // User's chosen username.
    val username: String,
    // User's email address, often used for login or notifications.
    val email: String,
    // User's first name.
    val firstName: String? = null,
    // User's last name.
    val lastName: String? = null,

    // Section: Role and Status
    // The role of the user within the system, using the UserRole enum defined above.
    val role: UserRole,
    // Indicates if the user account is active.
    val isActive: Boolean = true,

    // Section: Optional Profile Details
    // URL to the user's profile picture, if available.
    val profilePictureUrl: String? = null,
    // Timestamp of the user's last login, useful for tracking activity.
    val lastLogin: Long? = null // Using Long for Unix timestamp in milliseconds
)