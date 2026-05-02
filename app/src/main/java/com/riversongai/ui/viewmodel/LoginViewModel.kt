package com.riversongai.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.riversongai.data.model.AuthResponse
import com.riversongai.data.repository.UserRepository
import com.riversongai.utils.SessionManager
import kotlinx.coroutines.launch

class LoginViewModel(
    private val userRepository: UserRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _loginResult = MutableLiveData<Result<AuthResponse>>()
    val loginResult: LiveData<Result<AuthResponse>> = _loginResult

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    fun login(email: String, password: String) {
        _isLoading.value = true
        viewModelScope.launch {
            val result = userRepository.loginUser(email, password)
            result.onSuccess { auth ->
                sessionManager.saveAuthToken(auth.token)
                auth.user?.let { user ->
                    sessionManager.saveUserId(user.id)
                    sessionManager.saveDisplayName(user.displayName)
                    sessionManager.saveUserRole(user.role)
                }
            }
            _loginResult.value = result
            _isLoading.value = false
        }
    }
}
