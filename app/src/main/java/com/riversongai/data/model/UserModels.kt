package com.riversongai.data.model

import com.google.gson.annotations.SerializedName

data class User(
    val id: String,
    val email: String,
    @SerializedName("display_name") val displayName: String,
    val role: String,
    val username: String? = null,
    val birthday: String? = null,
    val theme: String = "halo",
    @SerializedName("is_approved") val isApproved: Boolean = false,
    @SerializedName("is_active") val isActive: Boolean = true
) {
    val isAdmin: Boolean get() = role == "admin"
    val firstName: String get() = displayName.split(" ").first()
}

data class AuthResponse(
    @SerializedName("access_token") val accessToken: String? = null,
    @SerializedName("token_type") val tokenType: String? = null,
    val token: String = "",
    val user: User? = null
)

data class SignupResponse(
    val detail: String? = null
)

data class UserProfile(
    val id: String,
    val email: String,
    @SerializedName("display_name") val displayName: String,
    val username: String? = null,
    val birthday: String? = null,
    val role: String,
    val theme: String = "halo"
)

data class UserProfileUpdate(
    @SerializedName("display_name") val displayName: String? = null,
    val username: String? = null,
    val birthday: String? = null,
    val theme: String? = null
)

data class UpdateProfileRequest(
    val firstName: String,
    val lastName: String,
    val callsign: String?
)

data class ChangePasswordRequest(
    @SerializedName("current_password") val currentPassword: String,
    @SerializedName("new_password") val newPassword: String
)

data class Integrations(
    @SerializedName("amazon_sp_api") val amazonSpApi: AmazonSpApiKeys = AmazonSpApiKeys(),
    @SerializedName("walmart_api") val walmartApi: WalmartApiKeys = WalmartApiKeys()
)

data class AmazonSpApiKeys(
    @SerializedName("lwa_app_id") val lwaAppId: String = "",
    @SerializedName("lwa_client_secret") val lwaClientSecret: String = "",
    @SerializedName("lwa_refresh_token") val lwaRefreshToken: String = "",
    @SerializedName("aws_access_key") val awsAccessKey: String = "",
    @SerializedName("aws_secret_key") val awsSecretKey: String = "",
    @SerializedName("seller_id") val sellerId: String = ""
)

data class WalmartApiKeys(
    @SerializedName("client_id") val clientId: String = "",
    @SerializedName("client_secret") val clientSecret: String = ""
)

data class AppUser(
    val id: String,
    val email: String,
    @SerializedName("display_name") val displayName: String,
    val role: String,
    @SerializedName("is_approved") val isApproved: Boolean = false,
    @SerializedName("is_pending") val isPending: Boolean = false
)

data class RoleUpdateBody(
    val role: String
)
