package com.riversongai.data.remote

import com.google.gson.FieldNamingPolicy
import com.google.gson.GsonBuilder
import com.riversongai.BuildConfig
import com.riversongai.data.model.AuthResponse
import com.riversongai.data.model.Device
import com.riversongai.data.model.DashboardStats
import com.riversongai.data.model.Fact
import com.riversongai.data.model.FactCreate
import com.riversongai.data.model.FeedPreferences
import com.riversongai.data.model.LlmSettings
import com.riversongai.data.model.ModelCatalog
import com.riversongai.data.model.NewsArticle
import com.riversongai.data.model.Routine
import com.riversongai.data.model.RoutineCreate
import com.riversongai.data.model.RoutineRunResponse
import com.riversongai.data.model.StockQuote
import com.riversongai.data.model.User
import com.riversongai.data.model.WeatherData
import com.riversongai.utils.SessionManager
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import java.util.concurrent.TimeUnit

interface RiverSongApiService {

    @POST("api/auth/login")
    suspend fun loginUser(@Body request: LoginRequest): Response<AuthResponse>

    @POST("api/auth/signup")
    suspend fun signupUser(@Body request: SignupRequest): Response<Void>

    @GET("api/auth/me")
    suspend fun getCurrentUser(): Response<User>

    @PATCH("api/user/profile")
    suspend fun updateProfile(@Body request: com.riversongai.data.model.UpdateProfileRequest): Response<User>

    @POST("api/user/change-password")
    suspend fun changePassword(@Body request: com.riversongai.data.model.ChangePasswordRequest): Response<Void>

    @GET("api/home/status")
    suspend fun getHomeStatus(): Response<com.riversongai.data.model.HomeStatus>

    @GET("api/home/devices")
    suspend fun getDevices(): Response<List<Device>>

    @POST("api/home/action")
    suspend fun callAction(@Body request: HomeActionRequest): Response<HomeActionResponse>

    @POST("api/conversation/chat")
    suspend fun chatHttp(@Body request: ChatRequest): Response<okhttp3.ResponseBody>

    @GET("api/chat/models")
    suspend fun getChatModels(): Response<List<com.riversongai.data.model.ChatModel>>

    @GET("api/chat/history")
    suspend fun getChatHistory(): Response<List<com.riversongai.data.model.ChatSession>>

    @GET("api/chat/history/{sessionId}")
    suspend fun getChatSessionDetail(@Path("sessionId") sessionId: String): Response<com.riversongai.data.model.ChatSessionDetail>

    // Memory
    @GET("api/memory/facts")
    suspend fun getFacts(): Response<List<Fact>>

    @POST("api/memory/facts")
    suspend fun createFact(@Body request: FactCreate): Response<Fact>

    @DELETE("api/memory/facts/{id}")
    suspend fun deleteFact(@Path("id") factId: String): Response<Void>

    @GET("api/memory/preferences")
    suspend fun getMemoryPreferences(): Response<List<com.riversongai.data.model.MemoryPreference>>

    @GET("api/memory/summaries")
    suspend fun getMemorySummaries(): Response<List<com.riversongai.data.model.MemorySummary>>

    // Routines
    @GET("api/routines")
    suspend fun getRoutines(): Response<List<Routine>>

    @POST("api/routines")
    suspend fun createRoutine(@Body request: RoutineCreate): Response<Routine>

    @PATCH("api/routines/{id}")
    suspend fun updateRoutine(@Path("id") routineId: String, @Body fields: Map<String, @JvmSuppressWildcards Any?>): Response<Routine>

    @DELETE("api/routines/{id}")
    suspend fun deleteRoutine(@Path("id") routineId: String): Response<Void>

    @POST("api/routines/{id}/run")
    suspend fun runRoutine(@Path("id") routineId: String): Response<RoutineRunResponse>

    // Feeds
    @GET("api/feeds/news")
    suspend fun getNews(@retrofit2.http.Query("category") category: String? = null): Response<List<NewsArticle>>

    @GET("api/feeds/weather")
    suspend fun getWeather(): Response<WeatherData>

    @GET("api/feeds/stocks")
    suspend fun getStocks(): Response<List<StockQuote>>

    @GET("api/feeds/preferences")
    suspend fun getFeedPreferences(): Response<FeedPreferences>

    @PUT("api/feeds/preferences")
    suspend fun saveFeedPreferences(@Body prefs: FeedPreferences): Response<Void>

    @POST("api/settings")
    suspend fun saveSettings(@Body body: Map<String, String>): Response<Void>

    // Sports
    @GET("api/sports/following")
    suspend fun getSportsFollowing(): Response<List<com.riversongai.data.model.SportsTeam>>

    @GET("api/sports/results")
    suspend fun getSportsResults(@retrofit2.http.Query("teamId") teamId: String?): Response<List<com.riversongai.data.model.SportsMatch>>

    @GET("api/sports/fixtures")
    suspend fun getSportsFixtures(@retrofit2.http.Query("teamId") teamId: String?): Response<List<com.riversongai.data.model.SportsMatch>>

    @POST("api/sports/follow")
    suspend fun followSportsTeam(@Body body: Map<String, String>): Response<Void>

    @DELETE("api/sports/follow/{teamId}")
    suspend fun unfollowSportsTeam(@Path("teamId") teamId: String): Response<Void>

    @GET("api/sports/search")
    suspend fun searchSportsTeams(@retrofit2.http.Query("q") query: String): Response<List<com.riversongai.data.model.SportsTeam>>

    // Settings
    @GET("api/models")
    suspend fun getModels(): Response<ModelCatalog>

    @GET("api/settings/llm")
    suspend fun getLlmSettings(): Response<LlmSettings>

    @POST("api/settings/llm")
    suspend fun saveLlmSettings(@Body body: Map<String, @JvmSuppressWildcards Any?>): Response<Void>

    @GET("api/settings/voices")
    suspend fun getVoices(): Response<List<com.riversongai.data.model.VoiceOption>>

    @GET("api/settings/memory-ttl")
    suspend fun getMemoryTtl(): Response<com.riversongai.data.model.MemoryTtlSettings>

    @PATCH("api/settings/memory-ttl")
    suspend fun updateMemoryTtl(@Body settings: com.riversongai.data.model.MemoryTtlSettings): Response<com.riversongai.data.model.MemoryTtlSettings>

    @POST("api/tts/preview")
    suspend fun testVoice(@Body body: Map<String, String>): Response<okhttp3.ResponseBody>

    @DELETE("api/memory/preferences/{id}")
    suspend fun deletePreference(@Path("id") id: String): Response<Unit>

    @DELETE("api/memory/summaries/{id}")
    suspend fun deleteSummary(@Path("id") id: String): Response<Unit>

    // ── Inventory ─────────────────────────────────────────────────────────────
    @GET("api/inventory/homes")
    suspend fun getInventoryHomes(): Response<List<com.riversongai.data.model.InventoryHome>>

    @POST("api/inventory/homes")
    suspend fun createInventoryHome(@Body body: com.riversongai.data.model.CreateInventoryHome): Response<com.riversongai.data.model.InventoryHome>

    @DELETE("api/inventory/homes/{homeId}")
    suspend fun deleteInventoryHome(@Path("homeId") homeId: String): Response<Void>

    @GET("api/inventory/homes/{homeId}/items")
    suspend fun getInventoryItems(@Path("homeId") homeId: String): Response<List<com.riversongai.data.model.InventoryItem>>

    @POST("api/inventory/homes/{homeId}/items")
    suspend fun createInventoryItem(@Path("homeId") homeId: String, @Body body: com.riversongai.data.model.CreateInventoryItem): Response<com.riversongai.data.model.InventoryItem>

    @PATCH("api/inventory/items/{itemId}")
    suspend fun updateInventoryItem(@Path("itemId") itemId: String, @Body body: com.riversongai.data.model.CreateInventoryItem): Response<com.riversongai.data.model.InventoryItem>

    @DELETE("api/inventory/items/{itemId}")
    suspend fun deleteInventoryItem(@Path("itemId") itemId: String): Response<Void>

    // ── Vehicles / Maintenance ─────────────────────────────────────────────
    @GET("api/vehicles/")
    suspend fun getVehicles(): Response<List<com.riversongai.data.model.Vehicle>>

    @POST("api/vehicles/")
    suspend fun createVehicle(@Body body: com.riversongai.data.model.CreateVehicle): Response<com.riversongai.data.model.Vehicle>

    @PATCH("api/vehicles/{vehicleId}")
    suspend fun updateVehicle(@Path("vehicleId") vehicleId: String, @Body body: com.riversongai.data.model.CreateVehicle): Response<com.riversongai.data.model.Vehicle>

    @DELETE("api/vehicles/{vehicleId}")
    suspend fun deleteVehicle(@Path("vehicleId") vehicleId: String): Response<Void>

    @GET("api/vehicles/{vehicleId}/specs/checkpoints")
    suspend fun getServiceCheckpoints(@Path("vehicleId") vehicleId: String): Response<List<com.riversongai.data.model.ServiceCheckpoint>>

    @GET("api/vehicles/{vehicleId}/logs")
    suspend fun getServiceLogs(@Path("vehicleId") vehicleId: String): Response<List<com.riversongai.data.model.ServiceLog>>

    @POST("api/vehicles/{vehicleId}/logs")
    suspend fun createServiceLog(@Path("vehicleId") vehicleId: String, @Body body: com.riversongai.data.model.CreateServiceLog): Response<com.riversongai.data.model.ServiceLog>

    // ── Commerce / Store ───────────────────────────────────────────────────
    @GET("api/commerce/workspaces")
    suspend fun getWorkspaces(): Response<List<com.riversongai.data.model.CommerceWorkspace>>

    @POST("api/commerce/workspaces")
    suspend fun createWorkspace(@Body body: com.riversongai.data.model.CreateWorkspace): Response<com.riversongai.data.model.CommerceWorkspace>

    @GET("api/commerce/workspaces/{workspaceId}/products")
    suspend fun getProducts(@Path("workspaceId") workspaceId: String): Response<List<com.riversongai.data.model.Product>>

    @POST("api/commerce/workspaces/{workspaceId}/products")
    suspend fun createProduct(@Path("workspaceId") workspaceId: String, @Body body: com.riversongai.data.model.CreateProduct): Response<com.riversongai.data.model.Product>

    @PATCH("api/commerce/products/{productId}")
    suspend fun updateProduct(@Path("productId") productId: String, @Body body: com.riversongai.data.model.CreateProduct): Response<com.riversongai.data.model.Product>

    @DELETE("api/commerce/products/{productId}")
    suspend fun deleteProduct(@Path("productId") productId: String): Response<Void>

    @POST("api/commerce/products/{productId}/stock")
    suspend fun adjustStock(@Path("productId") productId: String, @Body body: com.riversongai.data.model.StockAdjust): Response<com.riversongai.data.model.Product>

    // Dashboard
    @GET("api/dashboard")
    suspend fun getDashboard(): Response<DashboardStats>

    // Analytics
    @GET("api/analytics/snapshots")
    suspend fun getAnalyticsSnapshots(@retrofit2.http.Query("days") days: Int): Response<List<com.riversongai.data.model.AnalyticsSnapshot>>

    @GET("api/analytics/platforms")
    suspend fun getAnalyticsPlatforms(): Response<List<String>>

    @POST("api/analytics/snapshots")
    suspend fun addSnapshot(@Body body: com.riversongai.data.model.SnapshotCreate): Response<com.riversongai.data.model.AnalyticsSnapshot>

    @DELETE("api/analytics/snapshots/{id}")
    suspend fun deleteSnapshot(@Path("id") id: String): Response<Unit>

    @GET("api/analytics/{platform}/summary")
    suspend fun getPlatformSummary(@Path("platform") platform: String): Response<com.riversongai.data.model.PlatformSummary>

    // Culinary
    @GET("api/culinary/recipes")
    suspend fun getRecipes(): Response<List<com.riversongai.data.model.Recipe>>

    @POST("api/culinary/recipes")
    suspend fun createRecipe(@Body recipe: com.riversongai.data.model.RecipeCreate): Response<com.riversongai.data.model.Recipe>

    @GET("api/culinary/household")
    suspend fun getCulinaryHousehold(): Response<com.riversongai.data.model.CulinaryHousehold>

    @POST("api/culinary/household/equipment")
    suspend fun updateEquipment(@Body body: com.riversongai.data.model.EquipmentUpdate): Response<com.riversongai.data.model.CulinaryHousehold>

    @GET("api/culinary/household/banned")
    suspend fun getBannedItems(): Response<List<com.riversongai.data.model.BannedItem>>

    @POST("api/culinary/household/banned")
    suspend fun addBannedItem(@Body body: com.riversongai.data.model.BannedItemCreate): Response<com.riversongai.data.model.BannedItem>

    @DELETE("api/culinary/household/banned/{id}")
    suspend fun deleteBannedItem(@Path("id") id: String): Response<Unit>

    @GET("api/culinary/household")
    suspend fun getHouseholdProfile(): Response<com.riversongai.data.model.HouseholdProfile>

    // Reading
    @GET("api/reading/shelf")
    suspend fun getReadingShelf(@retrofit2.http.Query("status") status: String? = null): Response<List<com.riversongai.data.model.Book>>

    @POST("api/reading/shelf")
    suspend fun addBook(@Body book: com.riversongai.data.model.BookCreate): Response<com.riversongai.data.model.Book>

    @PATCH("api/reading/shelf/{bookId}")
    suspend fun updateBook(@Path("bookId") bookId: String, @Body update: com.riversongai.data.model.BookUpdate): Response<com.riversongai.data.model.Book>

    @DELETE("api/reading/shelf/{bookId}")
    suspend fun deleteBook(@Path("bookId") bookId: String): Response<Unit>

    @GET("api/reading/stats")
    suspend fun getReadingStats(): Response<com.riversongai.data.model.ReadingStats>

    @GET("api/reading/connections")
    suspend fun getReadingConnections(): Response<com.riversongai.data.model.ReadingConnections>

    @GET("api/reading/libby/loans")
    suspend fun getLibbyLoans(): Response<List<com.riversongai.data.model.LibbyLoan>>

    @GET("api/reading/libby/holds")
    suspend fun getLibbyHolds(): Response<List<com.riversongai.data.model.LibbyHold>>

    // Google Integration
    @GET("api/google/status")
    suspend fun getGoogleStatus(): Response<com.riversongai.data.model.GoogleStatus>

    @GET("api/google/auth/url")
    suspend fun getGoogleAuthUrl(@retrofit2.http.Query("redirect_uri") redirectUri: String): Response<com.riversongai.data.model.GoogleAuthUrl>

    @GET("api/google/calendar/upcoming")
    suspend fun getCalendarEvents(
        @retrofit2.http.Query("days") days: Int = 7,
        @retrofit2.http.Query("max_results") maxResults: Int = 10
    ): Response<com.riversongai.data.model.CalendarResponse>

    @GET("api/google/gmail/unread")
    suspend fun getGmailUnread(
        @retrofit2.http.Query("max_results") maxResults: Int = 5
    ): Response<com.riversongai.data.model.GmailResponse>

    // Admin
    @GET("api/admin/users")
    suspend fun getUsers(): Response<List<com.riversongai.data.model.AppUser>>

    @PATCH("api/admin/users/{userId}/role")
    suspend fun updateUserRole(
        @Path("userId") userId: String,
        @Body body: com.riversongai.data.model.RoleUpdateBody
    ): Response<com.riversongai.data.model.AppUser>

    @POST("api/admin/users/{userId}/approve")
    suspend fun approveUser(@Path("userId") userId: String): Response<com.riversongai.data.model.AppUser>

    @GET("api/killswitch/status")
    suspend fun getKillSwitchStatus(): Response<com.riversongai.data.model.KillSwitchStatus>

    @POST("api/killswitch/activate")
    suspend fun activateKillSwitch(): Response<com.riversongai.data.model.KillSwitchStatus>

    @POST("api/killswitch/reset")
    suspend fun resetKillSwitch(@Body body: com.riversongai.data.model.KillSwitchResetBody): Response<com.riversongai.data.model.KillSwitchStatus>

    @GET("api/admin/feature-visibility")
    suspend fun getFeatureVisibility(): Response<Map<String, Boolean>>

    @POST("api/admin/feature-visibility")
    suspend fun setFeatureVisibility(@Body body: Map<String, Boolean>): Response<Map<String, Boolean>>

    @GET("api/user/profile")
    suspend fun getUserProfile(): Response<com.riversongai.data.model.UserProfile>

    @PATCH("api/user/profile")
    suspend fun updateUserProfile(@Body body: com.riversongai.data.model.UserProfileUpdate): Response<com.riversongai.data.model.UserProfile>

    // Transcribe (voice)
    @POST("api/conversation/transcribe")
    suspend fun transcribeAudio(@Body request: TranscribeRequest): Response<TranscribeResponse>

    companion object {
        fun create(baseUrl: String, sessionManager: SessionManager): RiverSongApiService {
            val client = OkHttpClient.Builder()
                .addInterceptor(AuthInterceptor(sessionManager))
                .apply {
                    if (BuildConfig.DEBUG) {
                        addInterceptor(HttpLoggingInterceptor().apply {
                            level = HttpLoggingInterceptor.Level.BASIC
                        })
                    }
                }
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build()

            val gson = GsonBuilder()
                .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                .create()

            return Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build()
                .create(RiverSongApiService::class.java)
        }
    }
}

data class LoginRequest(
    val email: String,
    val password: String
)

data class SignupRequest(
    val email: String,
    val password: String,
    @com.google.gson.annotations.SerializedName("display_name") val displayName: String
)

data class HomeActionRequest(
    @com.google.gson.annotations.SerializedName("entity_id") val entityId: String,
    val action: String,
    @com.google.gson.annotations.SerializedName("brightness_pct") val brightnessPct: Int? = null,
    val temperature: Float? = null
)

data class HomeActionResponse(
    val ok: Boolean,
    val detail: String? = null
)

data class ChatRequest(
    val message: String,
    val history: List<Map<String, String>> = emptyList()
)

data class TranscribeRequest(val audio: String)

data class TranscribeResponse(val text: String)
