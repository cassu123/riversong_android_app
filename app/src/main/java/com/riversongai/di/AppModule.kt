package com.riversongai.di

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
import org.koin.android.ext.koin.androidApplication
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {

    single { SessionManager(androidContext()) }
    single { RiverSongApiService.create(Constants.BASE_URL, get()) }

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
    viewModel { HomeViewModel(get(), get(), get(), get()) }
    viewModel { UserDashboardViewModel(get(), get(), get(), get(), get()) }
    viewModel { SmartHomeControlViewModel(get(), get()) }
    viewModel { ChatViewModel(androidApplication(), get()) }
    viewModel { MemoryViewModel(get()) }
    viewModel { FeedsViewModel(get()) }
    viewModel { com.riversongai.ui.viewmodel.SportsViewModel(get()) }
    viewModel { RoutinesViewModel(androidApplication(), get()) }
    viewModel { SettingsViewModel(get()) }
    viewModel { InventoryViewModel(get()) }
    viewModel { MaintenanceViewModel(get()) }
    viewModel { CommerceViewModel(get()) }
}
