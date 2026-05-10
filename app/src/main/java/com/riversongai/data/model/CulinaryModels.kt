package com.riversongai.data.model

import com.google.gson.annotations.SerializedName

// --- Recipe ---
data class IngestResponse(
    val count: Int,
    val recipes: List<Recipe>
)

data class Recipe(
    val id: String,
    val title: String,
    @SerializedName("meal_type") val mealType: String,
    @SerializedName("primary_protein") val primaryProtein: String?,
    val servings: Int,
    @SerializedName("image_url") val imageUrl: String?,
    @SerializedName("source_url") val sourceUrl: String?,
    @SerializedName("source_type") val sourceType: String,
    @SerializedName("ingredients_json") val ingredientsJson: String?,
    @SerializedName("steps_json") val stepsJson: String?,
    @SerializedName("equipment_needed_json") val equipmentNeededJson: String?,
    @SerializedName("blacklisted_json") val blacklistedJson: String?,
    val rating: Int
)

data class RecipeCreate(
    val title: String,
    @SerializedName("meal_type") val mealType: String,
    val servings: Int,
    val ingredients: List<Ingredient>?,
    val steps: List<String>?,
    @SerializedName("equipment_needed") val equipmentNeeded: List<String>?
)

data class Ingredient(
    val name: String,
    val qty: String,
    val unit: String
)

data class ScaleRequest(
    @SerializedName("target_servings") val targetServings: Int,
    @SerializedName("prefer_system") val preferSystem: String? // "imperial" or "metric"
)

data class ScaleResponse(
    @SerializedName("recipe_id") val recipeId: String,
    @SerializedName("original_servings") val originalServings: Int,
    @SerializedName("target_servings") val targetServings: Int,
    @SerializedName("scale_factor") val scaleFactor: Float,
    @SerializedName("prefer_system") val preferSystem: String?,
    @SerializedName("scaled_ingredients") val scaledIngredients: List<Ingredient>
)

data class EquipmentTranslateRequest(
    val equipment: String
)

data class EquipmentTranslateResponse(
    @SerializedName("recipe_id") val recipeId: String,
    val equipment: String,
    @SerializedName("rewritten_steps") val rewrittenSteps: List<String>
)

// --- Banned Items ---
data class BannedItem(
    val id: String,
    val name: String,
    val substitute: String?
)

data class BannedItemCreate(
    val name: String,
    val substitute: String? = null
)

// --- Kitchen Equipment ---
data class KitchenEquipment(
    val id: String,
    val make: String?,
    val model: String?,
    val label: String,
    val capabilities: List<String> = emptyList()
)

data class EquipmentIdentifyRequest(
    val make: String,
    val model: String
)

data class EquipmentIdentifyResponse(
    val label: String,
    val types: List<String>
)

data class EquipmentCreate(
    val make: String,
    val model: String
)

data class EquipmentUpdate(
    val make: String?,
    val model: String?
)

// --- Stockroom ---
data class StockroomItem(
    val id: String,
    val name: String,
    val brand: String?,
    val barcode: String?,
    val state: String // "Good", "Medium", "Low"
)

data class StockroomItemCreate(
    val name: String,
    val brand: String? = null,
    val barcode: String? = null,
    val state: String = "Good"
)

data class StockroomItemUpdate(
    val name: String? = null,
    val brand: String? = null,
    val state: String? = null
)

data class ScanRequest(
    val barcode: String
)

// --- Prep Deck ---
data class PrepSession(
    val id: String,
    val label: String,
    @SerializedName("is_active") val isActive: Boolean,
    @SerializedName("completed_at") val completedAt: String?,
    @SerializedName("target_containers") val targetContainers: Int?,
    @SerializedName("container_oz") val containerOz: Float?,
    val recipes: List<PrepSessionRecipe> = emptyList()
)

data class PrepSessionRecipe(
    val id: String,
    @SerializedName("session_id") val sessionId: String,
    @SerializedName("recipe_id") val recipeId: String,
    val recipe: Recipe?,
    @SerializedName("recipe_title") val recipeTitle: String?,
    @SerializedName("servings_target") val servingsTarget: Int?,
    @SerializedName("scaled_ingredients_json") val scaledIngredientsJson: String?
)

data class PrepSessionCreate(
    val label: String,
    @SerializedName("target_containers") val targetContainers: Int? = null,
    @SerializedName("container_oz") val containerOz: Float? = null
)

data class AddRecipeToPrep(
    @SerializedName("recipe_id") val recipeId: String,
    @SerializedName("servings_target") val servingsTarget: Int? = null
)

data class PrepRecipeScaleUpdate(
    @SerializedName("target_servings") val targetServings: Int,
    @SerializedName("scaled_ingredients") val scaledIngredients: List<Ingredient>
)

data class ShoppingListResponse(
    @SerializedName("session_id") val sessionId: String,
    @SerializedName("shopping_list") val shoppingList: List<Ingredient>
)

data class StagingAreaResponse(
    @SerializedName("session_id") val sessionId: String,
    val piles: List<StagingPile>
)

data class StagingPile(
    @SerializedName("recipe_id") val recipeId: String,
    @SerializedName("recipe_title") val recipeTitle: String,
    val ingredients: List<Ingredient>
)

// --- Dinner Proposals ---
data class DinnerProposal(
    val id: String,
    @SerializedName("recipe_id") val recipeId: String,
    val recipe: Recipe?,
    val status: String, // "pending", "approved"
    @SerializedName("votes_yes") val votesYes: List<String> = emptyList(),
    @SerializedName("votes_no") val votesNo: List<String> = emptyList()
)

data class DinnerVoteRequest(
    val vote: String // "yes" or "no"
)

data class CookNowResponse(
    @SerializedName("recipe_id") val recipeId: String,
    val title: String,
    val servings: Int,
    @SerializedName("shopping_list") val shoppingList: List<Ingredient>,
    val steps: List<String>
)

// --- Walmart Export ---
data class WalmartMapping(
    val id: String,
    @SerializedName("ingredient_name") val ingredientName: String,
    @SerializedName("walmart_item_id") val walmartItemId: String
)

data class WalmartMappingCreate(
    @SerializedName("ingredient_name") val ingredientName: String,
    @SerializedName("walmart_item_id") val walmartItemId: String
)

data class WalmartExportResponse(
    @SerializedName("cart_url") val cartUrl: String?,
    @SerializedName("mapped_count") val mappedCount: Int,
    val unmapped: List<String>
)

// --- Household ---
data class CulinaryHousehold(
    val id: String,
    @SerializedName("owner_id") val ownerId: String
)
