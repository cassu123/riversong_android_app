package com.riversongai.data.model

import com.google.gson.annotations.SerializedName

data class UpdateProfileRequest(val firstName: String, val lastName: String, val callsign: String?)
data class ChangePasswordRequest(val currentPassword: String, val newPassword: String)

data class User(
    val id: String,
    val email: String,
    @SerializedName("display_name") val displayName: String,
    val role: String,
    @SerializedName("is_approved") val isApproved: Boolean = false,
    @SerializedName("is_active") val isActive: Boolean = true
) {
    val isAdmin: Boolean get() = role == "admin"
    val firstName: String get() = displayName.split(" ").first()
}
