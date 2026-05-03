package com.riversongai.data.repository

import android.util.Log
import com.riversongai.data.model.AuthResponse
import com.riversongai.data.model.User
import com.riversongai.data.remote.LoginRequest
import com.riversongai.data.remote.RiverSongApiService
import com.riversongai.data.remote.SignupRequest

class UserRepository(private val apiService: RiverSongApiService) {

    private val tag = "UserRepository"

    suspend fun loginUser(email: String, password: String): Result<AuthResponse> {
        return try {
            val response = apiService.loginUser(LoginRequest(email, password))
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                val error = parseError(response.errorBody()?.string(), response.code())
                Log.e(tag, "Login failed: ${response.code()} - $error")
                Result.failure(Exception(error))
            }
        } catch (e: Exception) {
            Log.e(tag, "Login exception", e)
            Result.failure(e)
        }
    }

    suspend fun signupUser(displayName: String, email: String, password: String): Result<Unit> {
        return try {
            val response = apiService.signupUser(SignupRequest(email, password, displayName))
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                val error = parseError(response.errorBody()?.string(), response.code())
                Log.e(tag, "Signup failed: ${response.code()} - $error")
                Result.failure(Exception(error))
            }
        } catch (e: Exception) {
            Log.e(tag, "Signup exception", e)
            Result.failure(e)
        }
    }

    suspend fun getCurrentUser(): Result<User> {
        return try {
            val response = apiService.getCurrentUser()
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                val error = parseError(response.errorBody()?.string(), response.code())
                Log.e(tag, "Fetch user failed: ${response.code()} - $error")
                Result.failure(Exception(error))
            }
        } catch (e: Exception) {
            Log.e(tag, "Fetch user exception", e)
            Result.failure(e)
        }
    }

    suspend fun updateProfile(firstName: String, lastName: String, callsign: String?): Result<User> {
        return try {
            val response = apiService.updateProfile(com.riversongai.data.model.UpdateProfileRequest(firstName, lastName, callsign))
            if (response.isSuccessful && response.body() != null) Result.success(response.body()!!)
            else Result.failure(Exception(parseError(response.errorBody()?.string(), response.code())))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun changePassword(current: String, newPass: String): Result<Unit> {
        return try {
            val response = apiService.changePassword(com.riversongai.data.model.ChangePasswordRequest(current, newPass))
            if (response.isSuccessful) Result.success(Unit)
            else Result.failure(Exception(parseError(response.errorBody()?.string(), response.code())))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun parseError(body: String?, code: Int): String {
        if (body.isNullOrBlank()) return "Error $code"
        return try {
            val json = org.json.JSONObject(body)
            json.optString("detail", "Error $code")
        } catch (_: Exception) {
            body
        }
    }
}
