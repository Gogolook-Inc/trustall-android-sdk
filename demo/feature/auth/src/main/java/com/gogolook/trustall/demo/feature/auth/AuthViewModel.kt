package com.gogolook.trustall.demo.feature.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gogolook.trustall.core.auth.auth
import com.gogolook.trustall.core.Trustall
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID
import com.gogolook.trustall.core.auth.model.AuthResult

class AuthViewModel : ViewModel() {

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Loading)
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun checkStatus() {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            try {
                // Accessing Trustall.auth via extension
                val auth = Trustall.auth
                val userId = auth.getUserId()
                
                // 1. Check userId for registration status
                if (userId.isNotEmpty()) {
                    val memberId = auth.getMemberId()
                    val region = auth.region
                    val deviceId = auth.deviceId
                    
                    _uiState.value = AuthUiState.Registered(
                        memberId = memberId,
                        userId = userId,
                        region = region,
                        deviceId = deviceId
                    )
                } else {
                    _uiState.value = AuthUiState.NotRegistered
                }
            } catch (e: Exception) {
               _uiState.value = AuthUiState.Error(e.message ?: "Unknown error occurred during status check")
            }
        }
    }

    fun register() {
         viewModelScope.launch {
             _uiState.value = AuthUiState.Loading
            try {
                val newMemberId = UUID.randomUUID().toString()
                when (val result = Trustall.auth.register(newMemberId)) {
                    is AuthResult.Success -> {
                        checkStatus()
                    }
                    is AuthResult.Error -> {
                        _uiState.value = AuthUiState.Error("Registration failed with error: $result")
                    }
                }
            } catch (e: Exception) {
                _uiState.value = AuthUiState.Error("Registration failed: ${e.message}")
            }
        }
    }

    fun updateMemberId(newMemberId: String) {
        viewModelScope.launch {
            Trustall.auth.setMemberId(newMemberId)
            checkStatus()
        }
    }

    // Helper to reset error state to NotRegistered (retry flow)
    fun dismissError() {
        checkStatus()
    }
}

sealed interface AuthUiState {
    data object Loading : AuthUiState
    
    data class Registered(
        val memberId: String,
        val userId: String,
        val region: String,
        val deviceId: String
    ) : AuthUiState
    
    data object NotRegistered : AuthUiState
    
    data class Error(val message: String) : AuthUiState
}
