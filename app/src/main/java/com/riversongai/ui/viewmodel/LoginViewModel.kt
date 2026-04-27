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

    fun login(username: String, password: String) {
        _isLoading.value = true
        viewModelScope.launch {
            val result = userRepository.loginUser(username, password)
            result.onSuccess { auth ->
                sessionManager.saveAuthToken(auth.accessToken)
                auth.user?.let {
                    sessionManager.saveUserId(it.id)
                    sessionManager.saveUsername(it.username)
                    sessionManager.saveUserRole(it.role.name)
                }
            }
            _loginResult.value = result
            _isLoading.value = false
        }
    }
}
