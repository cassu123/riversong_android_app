// app/src/main/java/com/riversongai/data/repository/UserRepository.kt
//
// This file defines the repository for managing user data and authentication within
// the River Song Android application. It serves as the primary interface for
// the application's UI/ViewModel layer to interact with user-related
// data, abstracting whether the data comes from the network API or a local source.

package com.riversongai.data.repository

import com.riversongai.data.model.User
import com.riversongai.data.remote.LoginRequest
import com.riversongai.data.remote.RegisterRequest
import com.riversongai.data.remote.RiverSongApiService
import android.util.Log

// Section: UserRepository Class Definition
// This class is responsible for handling all user-related data operations,
// including authentication and fetching user profiles.
// It uses RiverSongApiService to communicate with the backend.
class UserRepository(private val apiService: RiverSongApiService) {

    // Section: Constants
    // Tag for logging within this repository.
    private val TAG = "UserRepository"

    // Section: User Authentication
    // Authenticates a user by sending login credentials to the backend API.
    // Returns a Result indicating success with a User object or failure with an Exception.
    suspend fun loginUser(loginRequest: LoginRequest): Result<User> {
        return try {
            val response = apiService.loginUser(loginRequest) // Assuming API takes LoginRequest
            if (response.isSuccessful && response.body() != null) {
                Log.d(TAG, "User logged in successfully: ${response.body()?.username}")
                Result.success(response.body()!!)
            } else {
                val errorBody = response.errorBody()?.string() ?: "Unknown error"
                Log.e(TAG, "Login failed: ${response.code()} - $errorBody")
                Result.failure(Exception("Login failed: ${response.code()} - $errorBody"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception during login: ${e.message}", e)
            Result.failure(e)
        }
    }

    // Section: User Registration
    // Registers a new user account by sending registration details to the backend API.
    // Returns a Result indicating success with the newly created User or failure.
    suspend fun registerUser(registerRequest: RegisterRequest): Result<User> {
        return try {
            val response = apiService.registerUser(registerRequest) // Assuming API takes RegisterRequest
            if (response.isSuccessful && response.body() != null) {
                Log.d(TAG, "User registered successfully: ${response.body()?.username}")
                Result.success(response.body()!!)
            } else {
                val errorBody = response.errorBody()?.string() ?: "Unknown error"
                Log.e(TAG, "Registration failed: ${response.code()} - $errorBody")
                Result.failure(Exception("Registration failed: ${response.code()} - $errorBody"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception during registration: ${e.message}", e)
            Result.failure(e)
        }
    }

    // Section: Get Current User Profile
    // Retrieves the profile of the currently authenticated user from the backend API.
    // Requires an authentication token (e.g., Bearer token) for authorization.
    suspend fun getCurrentUser(authToken: String): Result<User> {
        return try {
            val response = apiService.getCurrentUser(authToken)
            if (response.isSuccessful && response.body() != null) {
                Log.d(TAG, "Successfully fetched current user profile.")
                Result.success(response.body()!!)
            } else {
                val errorBody = response.errorBody()?.string() ?: "Unknown error"
                Log.e(TAG, "Failed to fetch current user: ${response.code()} - $errorBody")
                Result.failure(Exception("Failed to fetch user profile: ${response.code()} - $errorBody"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception fetching current user: ${e.message}", e)
            Result.failure(e)
        }
    }

    // Section: Local User Session Management (Placeholder)
    // You might add methods here to save/retrieve user tokens or session data locally
    // using SharedPreferences or a secure storage solution.
    // fun saveAuthToken(token: String) {
    //     // Logic to save token locally
    //     Log.d(TAG, "Auth token saved locally.")
    // }

    // fun getAuthToken(): String? {
    //     // Logic to retrieve token locally
    //     return null // Placeholder
    // }

    // fun clearUserSession() {
    //     // Logic to clear local user data/tokens upon logout
    //     Log.d(TAG, "User session cleared locally.")
    // }
}