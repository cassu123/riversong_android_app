package com.riversongai.data.model

data class CulinaryHousehold(
    val equipment: Map<String, Boolean>
)

data class EquipmentUpdate(
    val equipment: Map<String, Boolean>
)

data class BannedItem(
    val id: String,
    val name: String,
    val reason: String?, // allergy/preference/other
    val substitute: String?
)

data class BannedItemCreate(
    val name: String,
    val reason: String?,
    val substitute: String?
)
