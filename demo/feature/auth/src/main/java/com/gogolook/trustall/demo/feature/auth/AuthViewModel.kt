package com.gogolook.trustall.demo.feature.auth

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.gogolook.trustall.core.auth.auth
import com.gogolook.trustall.demo.core.util.SdkBootstrap
import com.gogolook.trustall.core.Trustall
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID
import com.gogolook.trustall.core.auth.model.AuthResult

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Loading)
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    /** What the last in-place re-initialize did, shown until dismissed. */
    private val _notice = MutableStateFlow<String?>(null)
    val notice: StateFlow<String?> = _notice.asStateFlow()

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

                    _uiState.value = AuthUiState.Registered(
                        memberId = memberId,
                        userId = userId,
                        region = region,
                        deviceId = Trustall.deviceId,
                        authDeviceId = auth.deviceId,
                    )
                } else {
                    // Still carries the identifiers: this is where the screen lands right after
                    // a device-id change, and the new one is the thing worth confirming.
                    _uiState.value = AuthUiState.NotRegistered(
                        deviceId = Trustall.deviceId,
                        authDeviceId = auth.deviceId,
                    )
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

    /**
     * Adopts [newDeviceId] by running `Trustall.initialize()` again, without a restart.
     *
     * What this reaches, and what it does not, is the whole point of the button. A second
     * initialize replaces what `Trustall` itself holds — the identifier, the stored copy, and
     * the registration made under the old identifier, which it clears from the auth store. It
     * rebuilds nothing that was already built: every provider, Omnidroid's managers, and
     * `TrustallAuth` itself were constructed on first use and keep running.
     *
     * `TrustallAuth` is the exception, and only because the SDK makes it one: discarding the
     * registration also drops the built instance, so the next caller rebuilds it around the new
     * identifier. The "auth layer sends" row below is the canary for that — it appears only if
     * the two ever disagree again.
     *
     * Deliberately no automatic re-register here. Registering would write a user ID straight
     * back and hide the very thing worth looking at: that the clear happened at all.
     */
    fun applyDeviceId(newDeviceId: String) {
        viewModelScope.launch {
            val previous = Trustall.deviceId
            _uiState.value = AuthUiState.Loading
            try {
                SdkBootstrap.initialize(getApplication(), newDeviceId.trim())
                checkStatus()
                val authDeviceId = Trustall.auth.deviceId
                _notice.value = buildString {
                    append("Re-initialized in place: $previous -> ${Trustall.deviceId}. ")
                    append("The stored registration was discarded, so this screen is back to ")
                    append("Not registered. ")
                    if (authDeviceId != Trustall.deviceId) {
                        append("The auth layer still holds $authDeviceId — AuthManager was ")
                        append("seeded when it was first used and initialize() does not ")
                        append("re-seed it, so registering now would go out under the old ")
                        append("identifier. Restart to make them agree.")
                    }
                }
            } catch (e: Exception) {
                _uiState.value = AuthUiState.Error(
                    "Re-initialize failed: ${e.message ?: e::class.java.simpleName}"
                )
            }
        }
    }

    /** A valid identifier in the same shape the SDK generates, to save typing 32 hex characters. */
    fun randomDeviceId(): String = SdkBootstrap.randomDeviceId()

    fun dismissNotice() {
        _notice.value = null
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
        /** What `Trustall` holds, which is what a fresh launch would register under. */
        val deviceId: String,
        /** What the auth layer will actually send. Differs after an in-place re-initialize. */
        val authDeviceId: String,
    ) : AuthUiState
    
    data class NotRegistered(
        /** What `Trustall` holds, which is what registering now would use. */
        val deviceId: String,
        /** What the auth layer will actually send. Should never differ; see [applyDeviceId]. */
        val authDeviceId: String,
    ) : AuthUiState
    
    data class Error(val message: String) : AuthUiState
}
