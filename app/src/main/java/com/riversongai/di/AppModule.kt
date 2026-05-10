package com.riversongai.di

import com.google.gson.FieldNamingPolicy
import com.google.gson.GsonBuilder
import com.riversongai.data.remote.RiverSongApiService
import com.riversongai.data.repository.CommerceRepository
import com.riversongai.data.repository.ConversationRepository
import com.riversongai.data.repository.FeedsRepository
import com.riversongai.data.repository.InventoryRepository
import com.riversongai.data.repository.MaintenanceRepository
import com.riversongai.data.repository.MemoryRepository
import com.riversongai.data.repository.RoutinesRepository
import com.riversongai.data.repository.SettingsRepository
import com.riversongai.data.repository.SmartHomeRepository
import com.riversongai.data.repository.UserRepository
import com.riversongai.ui.viewmodel.ChatViewModel
import com.riversongai.ui.viewmodel.CommerceViewModel
import com.riversongai.ui.viewmodel.FeedsViewModel
import com.riversongai.ui.viewmodel.HomeViewModel
import com.riversongai.ui.viewmodel.InventoryViewModel
import com.riversongai.ui.viewmodel.LoginViewModel
import com.riversongai.ui.viewmodel.MaintenanceViewModel
import com.riversongai.ui.viewmodel.MemoryViewModel
import com.riversongai.ui.viewmodel.RegisterViewModel
import com.riversongai.ui.viewmodel.RoutinesViewModel
import com.riversongai.ui.viewmodel.SettingsViewModel
import com.riversongai.ui.viewmodel.SmartHomeControlViewModel
import com.riversongai.ui.viewmodel.UserDashboardViewModel
import com.riversongai.utils.Constants
import com.riversongai.utils.SessionManager
import okhttp3.OkHttpClient
import org.koin.android.ext.koin.androidApplication
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

val appModule = module {

    single { SessionManager(androidContext()) }
    
    single {
        val sessionManager: SessionManager = get()
        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor { chain ->
                val original = chain.request()
                val requestBuilder = original.newBuilder()
                sessionManager.getAuthToken()?.let {
                    requestBuilder.header("Authorization", "Bearer $it")
                }
                chain.proceed(requestBuilder.build())
            }
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .writeTimeout(10, TimeUnit.SECONDS)
            .build()

        val gson = GsonBuilder()
            .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
            .create()

        Retrofit.Builder()
            .baseUrl(Constants.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
            .create(RiverSongApiService::class.java)
    }

    single { UserRepository(get()) }
    single { SmartHomeRepository(get()) }
    single { ConversationRepository(get(), get()) }
    single { MemoryRepository(get()) }
    single { FeedsRepository(get()) }
    single { RoutinesRepository(get()) }
    single { SettingsRepository(get()) }
    single { com.riversongai.data.repository.SportsRepository(get()) }
    single { InventoryRepository(get()) }
    single { MaintenanceRepository(get()) }
    single { CommerceRepository(get()) }

    viewModel { LoginViewModel(get(), get()) }
    viewModel { RegisterViewModel(get()) }
    viewModel { HomeViewModel(get(), get(), get(), get(), get(), get()) }
    viewModel { UserDashboardViewModel(get(), get(), get(), get(), get()) }
    viewModel { SmartHomeControlViewModel(get(), get()) }
    viewModel { ChatViewModel(androidApplication(), get()) }
    viewModel { MemoryViewModel(get()) }
    viewModel { FeedsViewModel(get()) }
    viewModel { com.riversongai.ui.viewmodel.SportsViewModel(get()) }
    viewModel { RoutinesViewModel(androidApplication(), get()) }
    viewModel { SettingsViewModel(get(), get()) }
    viewModel { InventoryViewModel(get()) }
    viewModel { MaintenanceViewModel(get()) }
    viewModel { CommerceViewModel(get()) }
    viewModel { com.riversongai.ui.viewmodel.AnalyticsViewModel(get()) }
    viewModel { com.riversongai.ui.viewmodel.CulinaryViewModel(get()) }
    viewModel { com.riversongai.ui.viewmodel.ReadingViewModel(get()) }
    viewModel { com.riversongai.ui.viewmodel.GoogleViewModel(get()) }
    viewModel { com.riversongai.ui.viewmodel.UsersViewModel(get()) }
}
