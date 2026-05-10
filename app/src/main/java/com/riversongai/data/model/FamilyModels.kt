package com.riversongai.data.model

import com.google.gson.annotations.SerializedName

data class FamilyMember(
    val id: String,
    val email: String,
    @SerializedName("display_name") val displayName: String,
    val role: String
)

data class FamilyLink(
    @SerializedName("parent_id") val parentId: String,
    @SerializedName("child_id") val childId: String
)

data class FamilyGroup(
    val id: String = "",
    val name: String = "",
    @SerializedName("shared_modules") val sharedModules: List<String> = emptyList(),
    val members: List<FamilyGroupMember> = emptyList()
)

data class FamilyGroupMember(
    @SerializedName("profile_id") val profileId: String,
    @SerializedName("display_name") val displayName: String = "",
    val relationship: String = "member"
)

data class FamilyGroupCreate(
    val name: String,
    @SerializedName("shared_modules") val sharedModules: List<String> = listOf("culinary", "inventory", "store", "maintenance")
)
