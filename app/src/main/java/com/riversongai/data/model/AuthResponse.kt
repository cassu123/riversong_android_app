package com.riversongai.data.model

import com.google.gson.annotations.SerializedName

data class AuthResponse(
    val token: String,
    val user: User? = null
)

data class SignupResponse(
    val detail: String? = null
)
