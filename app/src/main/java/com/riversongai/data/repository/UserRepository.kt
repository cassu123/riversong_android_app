package com.riversongai.data.repository

import android.util.Log
import com.riversongai.data.model.AuthResponse
import com.riversongai.data.model.User
import com.riversongai.data.remote.LoginRequest
import com.riversongai.data.remote.RegisterRequest
import com.riversongai.data.remote.RiverSongApiService

class UserRepository(private val apiService: RiverSongApiService) {

    private val tag = "UserRepository"

    suspend fun loginUser(request: LoginRequest): Result<AuthResponse> {
        return try {
            val response = apiService.loginUser(request)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                val error = response.errorBody()?.string() ?: "Unknown error"
                Log.e(tag, "Login failed: ${response.code()} - $error")
                Result.failure(Exception("Login failed: ${response.code()} - $error"))
            }
        } catch (e: Exception) {
            Log.e(tag, "Login exception", e)
            Result.failure(e)
        }
    }

    suspend fun registerUser(request: RegisterRequest): Result<AuthResponse> {
        return try {
            val response = apiService.registerUser(request)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                val error = response.errorBody()?.string() ?: "Unknown error"
                Log.e(tag, "Registration failed: ${response.code()} - $error")
                Result.failure(Exception("Registration failed: ${response.code()} - $error"))
            }
        } catch (e: Exception) {
            Log.e(tag, "Registration exception", e)
            Result.failure(e)
        }
    }

    suspend fun getCurrentUser(authToken: String): Result<User> {
        return try {
            val response = apiService.getCurrentUser(authToken)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                val error = response.errorBody()?.string() ?: "Unknown error"
                Log.e(tag, "Fetch user failed: ${response.code()} - $error")
                Result.failure(Exception("Failed to fetch user: ${response.code()} - $error"))
            }
        } catch (e: Exception) {
            Log.e(tag, "Fetch user exception", e)
            Result.failure(e)
        }
    }
}
