package com.riversongai.data.model

import com.google.gson.annotations.SerializedName

data class Recipe(
    val id: String,
    val title: String,
    val ingredients: List<Ingredient>,
    val instructions: String,
    @SerializedName("prep_time") val prepTime: Int,
    @SerializedName("cook_time") val cookTime: Int,
    val servings: Int,
    val rating: Float,
    @SerializedName("meal_type") val mealType: String,
    @SerializedName("cooking_methods") val cookingMethods: List<String>,
    @SerializedName("image_url") val imageUrl: String?
)

data class Ingredient(
    val name: String,
    val quantity: String,
    val unit: String
)

data class RecipeCreate(
    val title: String,
    @SerializedName("meal_type") val mealType: String,
    @SerializedName("prep_time") val prepTime: Int,
    @SerializedName("cook_time") val cookTime: Int,
    val servings: Int,
    val instructions: String
)

data class HouseholdProfile(
    @SerializedName("banned_ingredients") val bannedIngredients: List<String>,
    val equipment: List<String>
)
