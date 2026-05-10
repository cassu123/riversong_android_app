package com.riversongai.data.model

import com.google.gson.annotations.SerializedName

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
