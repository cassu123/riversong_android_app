package com.riversongai.data.model

data class KillSwitchStatus(
    val active: Boolean,
    val activatedAt: String?
)

data class KillSwitchResetBody(
    val password: String
)
