package com.riversongai.data.remote

import com.google.gson.FieldNamingPolicy
import com.google.gson.GsonBuilder
import com.riversongai.BuildConfig
import com.riversongai.data.model.*
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import java.util.concurrent.TimeUnit

interface RiverSongApiService {

    // --- Auth & User ---
    @POST("api/auth/login")
    suspend fun login(@Body body: Map<String, String>): Response<AuthResponse>

    @POST("api/auth/register")
    suspend fun register(@Body body: Map<String, String>): Response<AuthResponse>

    @GET("api/auth/me")
    suspend fun getCurrentUser(): Response<UserProfile>

    @PATCH("api/auth/password")
    suspend fun changePassword(@Body body: Map<String, String>): Response<Void>

    @GET("api/auth/integrations")
    suspend fun getIntegrations(): Response<Integrations>

    @PUT("api/auth/integrations")
    suspend fun saveIntegrations(@Body body: Integrations): Response<Void>

    @GET("api/auth/profile")
    suspend fun getUserProfile(): Response<UserProfile>

    @PATCH("api/auth/profile")
    suspend fun updateUserProfile(@Body body: UserProfileUpdate): Response<UserProfile>

    // --- Dashboard ---
    @GET("api/dashboard/summary")
    suspend fun getDashboard(): Response<DashboardStats>

    @POST("api/home/action")
    suspend fun callAction(@Body body: HomeActionRequest): Response<Void>

    @GET("api/home/status")
    suspend fun getHomeStatus(): Response<Map<String, Boolean>>

    @GET("api/home/devices")
    suspend fun getDevices(): Response<List<Device>>

    // --- Conversational AI ---
    @POST("api/chat/message")
    suspend fun sendMessage(@Body body: ChatRequest): Response<ChatResponse>

    @POST("api/conversation/chat")
    @Streaming
    suspend fun chatHttp(@Body body: ChatRequest): Response<okhttp3.ResponseBody>

    @POST("api/conversation/extract-facts")
    suspend fun extractFacts(@Body body: Map<String, Any?>): Response<Void>

    @POST("api/chat/transcribe")
    suspend fun transcribe(@Body body: TranscribeRequest): Response<TranscribeResponse>

    @GET("api/chat/history")
    suspend fun getChatHistory(): Response<List<ChatMessage>>

    // --- Memory ---
    @GET("api/memory/facts")
    suspend fun getFacts(): Response<List<Fact>>

    @POST("api/memory/facts")
    suspend fun createFact(@Body body: FactCreate): Response<Fact>

    @DELETE("api/memory/facts/{id}")
    suspend fun deleteFact(@Path("id") id: String): Response<Void>

    @GET("api/memory/preferences")
    suspend fun getPreferences(): Response<List<MemoryPreference>>

    @DELETE("api/memory/preferences/{id}")
    suspend fun deletePreference(@Path("id") id: String): Response<Void>

    @GET("api/memory/summaries")
    suspend fun getSummaries(): Response<List<MemorySummary>>

    @DELETE("api/memory/summaries/{id}")
    suspend fun deleteSummary(@Path("id") id: String): Response<Void>

    @GET("api/settings/memory")
    suspend fun getMemoryTtl(): Response<MemoryTtlSettings>

    @POST("api/settings/memory")
    suspend fun updateMemoryTtl(@Body body: MemoryTtlSettings): Response<MemoryTtlSettings>

    // --- Feeds & Preferences ---
    @GET("api/feeds/preferences")
    suspend fun getFeedPreferences(): Response<FeedPreferences>

    @PUT("api/feeds/preferences")
    suspend fun saveFeedPreferences(@Body body: FeedPreferences): Response<Void>

    @GET("api/feeds/news")
    suspend fun getNews(@Query("category") category: String?): Response<List<NewsArticle>>

    @GET("api/feeds/weather")
    suspend fun getWeather(): Response<WeatherData>

    @GET("api/feeds/stocks")
    suspend fun getStocks(): Response<List<StockQuote>>

    @GET("api/feeds/stocks/chart")
    suspend fun getStockChart(@Query("ticker") ticker: String): Response<List<StockChartEntry>>

    // --- Sports ---
    @GET("api/feeds/sports/standings")
    suspend fun getSportsStandings(@Query("league_id") leagueId: String): Response<List<StandingEntry>>

    @GET("api/feeds/sports/event-stats")
    suspend fun getSportsEventStats(@Query("event_id") eventId: String): Response<List<SportsEventStat>>

    @POST("api/feeds/sports/follow")
    suspend fun followSportsTeam(@Body body: Map<String, String>): Response<Void>

    @DELETE("api/feeds/sports/follow/{teamId}")
    suspend fun unfollowSportsTeam(@Path("teamId") teamId: String): Response<Void>

    @GET("api/feeds/sports/leagues")
    suspend fun getSportsLeagues(): Response<List<SportsLeague>>

    @GET("api/feeds/sports/teams/{leagueId}")
    suspend fun getSportsTeams(@Path("leagueId") leagueId: String): Response<List<SportsTeam>>

    @GET("api/feeds/sports/search")
    suspend fun searchSportsTeams(@Query("q") query: String): Response<List<SportsTeam>>

    // Legacy repository method names — correct distinct paths
    @GET("api/feeds/sports")
    suspend fun getSportsFollowing(): Response<List<SportsTeam>>

    @GET("api/feeds/sports/results")
    suspend fun getSportsResults(@Query("teamId") teamId: String?): Response<List<SportsMatch>>

    @GET("api/feeds/sports/fixtures")
    suspend fun getSportsFixtures(@Query("teamId") teamId: String?): Response<List<SportsMatch>>

    // --- Models & Voices ---
    @GET("api/models")
    suspend fun getModels(): Response<ModelCatalog>

    @GET("api/settings/llm")
    suspend fun getLlmSettings(): Response<LlmSettings>

    @POST("api/settings/llm")
    suspend fun saveLlmSettings(@Body body: LlmSettings): Response<Void>

    @GET("api/settings/voice")
    suspend fun getVoices(): Response<List<VoiceOption>>

    @POST("api/settings/voice")
    suspend fun setVoice(@Body body: Map<String, String>): Response<Void>

    @GET("api/tts/preview/{voice_id}")
    suspend fun previewVoice(@Path("voice_id") voiceId: String): Response<Map<String, String>>

    @POST("api/tts/preview")
    suspend fun testVoice(@Body body: Map<String, String>): Response<okhttp3.ResponseBody>

    // --- Orchestration ---
    @GET("api/settings/orchestration")
    suspend fun getOrchestrationSettings(): Response<N8nSettings>

    @POST("api/settings/orchestration")
    suspend fun saveOrchestrationSettings(@Body body: N8nSettings): Response<Void>

    // --- Admin ---
    @GET("api/admin/users")
    suspend fun getAdminUsers(): Response<List<FamilyMember>>

    @PATCH("api/admin/users/{user_id}")
    suspend fun updateAdminUser(@Path("user_id") userId: String, @Body body: Map<String, Any?>): Response<FamilyMember>

    @GET("api/admin/model-visibility")
    suspend fun getModelVisibility(): Response<ModelVisibilityResponse>

    @PUT("api/admin/model-visibility")
    suspend fun setModelVisibility(@Body body: ModelVisibilityUpdate): Response<ModelVisibilityResponse>

    @GET("api/admin/feature-visibility")
    suspend fun getFeatureVisibility(): Response<Map<String, Any?>>

    @PUT("api/admin/feature-visibility")
    suspend fun setFeatureVisibility(@Body body: Map<String, Any?>): Response<Void>

    @GET("api/admin/family")
    suspend fun getFamilyLinks(): Response<Map<String, Any?>>

    @POST("api/admin/family")
    suspend fun addFamilyLink(@Body body: FamilyLink): Response<Void>

    @DELETE("api/admin/family/{parentId}/{childId}")
    suspend fun deleteFamilyLink(@Path("parentId") parentId: String, @Path("childId") childId: String): Response<Void>

    @GET("api/admin/family-groups")
    suspend fun getFamilyGroups(): Response<Map<String, Any?>>

    @POST("api/admin/family-groups")
    suspend fun createFamilyGroup(@Body body: FamilyGroupCreate): Response<FamilyGroup>

    // --- Analytics ---
    @GET("api/analytics/business-report")
    suspend fun getBusinessReport(@Query("days") days: Int): Response<SystemAnalytics>

    @GET("api/analytics/platforms")
    suspend fun getAnalyticsPlatforms(): Response<List<Map<String, Any?>>>

    @PUT("api/analytics/platforms/{platform}")
    suspend fun updateAnalyticsPlatform(@Path("platform") platform: String, @Body body: PlatformConfig): Response<Void>

    @GET("api/analytics/snapshots")
    suspend fun getAnalyticsSnapshots(@Query("platform") platform: String?, @Query("days") days: Int): Response<List<AnalyticsSnapshot>>

    @POST("api/analytics/snapshots")
    suspend fun addAnalyticsSnapshot(@Body body: AnalyticsSnapshot): Response<Void>

    @GET("api/analytics/{platform}/summary")
    suspend fun getPlatformSummary(@Path("platform") platform: String): Response<PlatformInsight>

    // --- Commerce ---
    @GET("api/commerce/workspaces")
    suspend fun getWorkspaces(): Response<List<CommerceWorkspace>>

    @POST("api/commerce/workspaces")
    suspend fun createWorkspace(@Body body: CreateWorkspace): Response<CommerceWorkspace>

    @GET("api/commerce/workspaces/{id}/products")
    suspend fun getProducts(@Path("id") workspaceId: String): Response<List<Product>>

    @POST("api/commerce/workspaces/{id}/products")
    suspend fun createProduct(@Path("id") workspaceId: String, @Body body: CreateProduct): Response<Product>

    @GET("api/commerce/products/{id}")
    suspend fun getProductDetail(@Path("id") productId: String): Response<Product>

    @PATCH("api/commerce/products/{id}")
    suspend fun updateProduct(@Path("id") productId: String, @Body body: CreateProduct): Response<Product>

    @DELETE("api/commerce/products/{id}")
    suspend fun deleteProduct(@Path("id") productId: String): Response<Void>

    @POST("api/commerce/products/{id}/stock")
    suspend fun adjustStock(@Path("id") productId: String, @Body body: StockAdjust): Response<Product>

    @POST("api/commerce/products/{id}/image")
    @Multipart
    suspend fun uploadProductImage(@Path("id") productId: String, @Part file: MultipartBody.Part): Response<Product>

    @DELETE("api/commerce/products/{id}/image")
    suspend fun deleteProductImage(@Path("id") productId: String): Response<Void>

    @GET("api/commerce/workspaces/{id}/suppliers")
    suspend fun getSuppliers(@Path("id") workspaceId: String): Response<List<Supplier>>

    @GET("api/commerce/workspaces/{id}/customers")
    suspend fun getCustomers(@Path("id") workspaceId: String): Response<List<Customer>>

    @GET("api/commerce/workspaces/{id}/sales")
    suspend fun getSales(@Path("id") workspaceId: String): Response<List<Sale>>

    @GET("api/commerce/workspaces/{id}/members")
    suspend fun getMembers(@Path("id") workspaceId: String): Response<List<WorkspaceMember>>

    // --- Routines ---
    @GET("api/routines")
    suspend fun getRoutines(): Response<List<Routine>>

    @POST("api/routines")
    suspend fun createRoutine(@Body body: RoutineCreate): Response<Routine>

    @PATCH("api/routines/{id}")
    suspend fun updateRoutine(@Path("id") id: String, @Body body: Map<String, Any?>): Response<Routine>

    @DELETE("api/routines/{id}")
    suspend fun deleteRoutine(@Path("id") id: String): Response<Void>

    @POST("api/routines/{id}/run")
    suspend fun runRoutine(@Path("id") id: String): Response<RoutineRunResponse>

    // --- Culinary ---
    @GET("api/culinary/recipes")
    suspend fun getRecipes(): Response<List<Recipe>>

    @POST("api/culinary/recipes")
    suspend fun createRecipe(@Body recipe: RecipeCreate): Response<Recipe>

    @POST("api/culinary/recipes/ingest")
    suspend fun ingestRecipe(@Body body: Map<String, String>): Response<Recipe>

    @GET("api/culinary/dinner")
    suspend fun getDinnerProposals(): Response<List<DinnerProposal>>

    @POST("api/culinary/dinner/{id}/vote")
    suspend fun voteDinner(@Path("id") id: String, @Body body: DinnerVoteRequest): Response<Void>

    @GET("api/culinary/stockroom")
    suspend fun getStockroom(): Response<List<StockroomItem>>

    @POST("api/culinary/stockroom/scan")
    suspend fun scanStockroom(@Body body: ScanRequest): Response<StockroomItem>

    @GET("api/culinary/prep")
    suspend fun getActivePrep(): Response<PrepSession>

    @POST("api/culinary/prep/scale")
    suspend fun scalePrep(@Body body: Map<String, Any?>): Response<PrepSession>

    @POST("api/culinary/prep/cook-now")
    suspend fun cookNow(@Body body: Map<String, String>): Response<Void>

    @GET("api/culinary/household/banned")
    suspend fun getBannedItems(): Response<List<BannedItem>>

    @POST("api/culinary/household/banned")
    suspend fun addBannedItem(@Body body: BannedItemCreate): Response<BannedItem>

    @DELETE("api/culinary/household/banned/{id}")
    suspend fun deleteBannedItem(@Path("id") id: String): Response<Void>

    @GET("api/culinary/household/equipment")
    suspend fun getEquipment(): Response<List<KitchenEquipment>>

    @POST("api/culinary/walmart/mapping")
    suspend fun createWalmartMapping(@Body body: Map<String, String>): Response<Void>

    @GET("api/culinary/walmart/mappings")
    suspend fun getWalmartMappings(): Response<List<WalmartMapping>>

    @GET("api/culinary/walmart/export")
    suspend fun exportWalmartCart(@Query("prep_id") prepId: String?): Response<Map<String, String>>

    // --- Inventory ---
    @GET("api/inventory/homes")
    suspend fun getInventoryHomes(): Response<List<InventoryHome>>

    @POST("api/inventory/homes")
    suspend fun createInventoryHome(@Body body: CreateInventoryHome): Response<InventoryHome>

    @GET("api/inventory/homes/{id}/items")
    suspend fun getInventoryItems(@Path("id") homeId: String): Response<List<InventoryItem>>

    @POST("api/inventory/homes/{id}/items")
    suspend fun createInventoryItem(@Path("id") homeId: String, @Body body: CreateInventoryItem): Response<InventoryItem>

    @PATCH("api/inventory/items/{id}")
    suspend fun updateInventoryItem(@Path("id") itemId: String, @Body body: Map<String, Any?>): Response<InventoryItem>

    @DELETE("api/inventory/items/{id}")
    suspend fun deleteInventoryItem(@Path("id") itemId: String): Response<Void>

    @GET("api/inventory/items/{id}/attachments")
    suspend fun getItemAttachments(@Path("id") itemId: String): Response<List<ItemAttachment>>

    @Multipart
    @POST("api/inventory/items/{id}/attachments")
    suspend fun uploadItemAttachment(@Path("id") itemId: String, @Part file: MultipartBody.Part): Response<ItemAttachment>

    @POST("api/inventory/items/{id}/issue")
    suspend fun issueInventoryItem(@Path("id") itemId: String, @Body body: Map<String, String>): Response<InventoryItem>

    @POST("api/inventory/items/{id}/return")
    suspend fun returnInventoryItem(@Path("id") itemId: String): Response<InventoryItem>

    @GET("api/inventory/homes/{id}/audit/active")
    suspend fun getActiveAudit(@Path("id") homeId: String): Response<InventoryAudit>

    @POST("api/inventory/homes/{id}/audit/start")
    suspend fun startInventoryAudit(@Path("id") homeId: String): Response<InventoryAudit>

    @POST("api/vision/inventory-item")
    @Multipart
    suspend fun analyzeInventoryPhoto(@Part file: MultipartBody.Part): Response<Map<String, String>>

    // --- Vehicles / Maintenance ---
    @GET("api/vehicles/")
    suspend fun getVehicles(): Response<List<Vehicle>>

    @POST("api/vehicles/")
    suspend fun createVehicle(@Body body: CreateVehicle): Response<Vehicle>

    @PATCH("api/vehicles/{id}")
    suspend fun updateVehicle(@Path("id") id: String, @Body body: CreateVehicle): Response<Vehicle>

    @DELETE("api/vehicles/{id}")
    suspend fun deleteVehicle(@Path("id") id: String): Response<Void>

    @GET("api/vehicles/{id}/logs")
    suspend fun getServiceLogs(@Path("id") id: String): Response<List<ServiceLog>>

    @POST("api/vehicles/{id}/logs")
    suspend fun createServiceLog(@Path("id") id: String, @Body body: CreateServiceLog): Response<ServiceLog>

    @GET("api/vehicles/{id}/assignments")
    suspend fun getVehicleAssignments(@Path("id") id: String): Response<List<VehicleAssignment>>

    @GET("api/vehicles/{id}/specs/checkpoints")
    suspend fun getServiceCheckpoints(@Path("id") id: String): Response<List<ServiceCheckpoint>>

    // --- Reading ---
    @GET("api/reading/shelf")
    suspend fun getReadingShelf(@Query("service") service: String?, @Query("status") status: String?): Response<List<Book>>

    @POST("api/reading/shelf")
    suspend fun addBook(@Body book: BookCreate): Response<Book>

    @PATCH("api/reading/shelf/{bookId}")
    suspend fun updateBook(@Path("bookId") bookId: String, @Body update: BookUpdate): Response<Book>

    @DELETE("api/reading/shelf/{bookId}")
    suspend fun deleteBook(@Path("bookId") bookId: String): Response<Void>

    @GET("api/reading/stats")
    suspend fun getReadingStats(): Response<ReadingStats>

    @GET("api/reading/connections")
    suspend fun getReadingConnections(): Response<ReadingConnections>

    @GET("api/reading/libby/loans")
    suspend fun getLibbyLoans(): Response<List<LibbyLoan>>

    @GET("api/reading/libby/holds")
    suspend fun getLibbyHolds(): Response<List<LibbyHold>>

    @POST("api/reading/sync/kindle")
    suspend fun syncKindle(): Response<Void>

    @POST("api/reading/sync/google_play")
    suspend fun syncGooglePlay(): Response<Void>

    // --- Google ---
    @GET("api/google/status")
    suspend fun getGoogleStatus(): Response<GoogleStatus>

    @GET("api/google/calendar/upcoming")
    suspend fun getCalendarEvents(@Query("days") days: Int, @Query("max_results") maxResults: Int): Response<CalendarResponse>

    @GET("api/google/gmail/unread")
    suspend fun getGmailUnread(@Query("max_results") maxResults: Int): Response<GmailResponse>
}
