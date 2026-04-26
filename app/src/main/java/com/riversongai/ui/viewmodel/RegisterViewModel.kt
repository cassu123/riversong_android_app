package com.riversongai.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.riversongai.data.model.AuthResponse
import com.riversongai.data.remote.RegisterRequest
import com.riversongai.data.repository.UserRepository
import com.riversongai.utils.SessionManager
import kotlinx.coroutines.launch

class RegisterViewModel(
    private val userRepository: UserRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _registerResult = MutableLiveData<Result<AuthResponse>>()
    val registerResult: LiveData<Result<AuthResponse>> = _registerResult

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    fun register(
        username: String,
        email: String,
        password: String,
        firstName: String? = null,
        lastName: String? = null
    ) {
        _isLoading.value = true
        viewModelScope.launch {
            val result = userRepository.registerUser(
                RegisterRequest(
                    username = username,
                    email = email,
                    password = password,
                    firstName = firstName?.ifBlank { null },
                    lastName = lastName?.ifBlank { null }
                )
            )
            result.onSuccess { auth ->
                sessionManager.saveAuthToken(auth.accessToken)
                auth.user?.let {
                    sessionManager.saveUserId(it.id)
                    sessionManager.saveUsername(it.username)
                    sessionManager.saveUserRole(it.role.name)
                }
            }
            _registerResult.value = result
            _isLoading.value = false
        }
    }
}
