package com.gogolook.trustall.demo.feature.msgfilter

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gogolook.trustall.core.Trustall
import com.gogolook.trustall.msgfilter.messageFilter
import com.gogolook.trustall.msgfilter.model.FilterResult
import com.gogolook.trustall.msgfilter.model.Message
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MsgFilterUiState(
        val isLoading: Boolean = false,
        val result: FilterResult.Success? = null,
        val error: String? = null
)

class MsgFilterViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(MsgFilterUiState())
    val uiState: StateFlow<MsgFilterUiState> = _uiState.asStateFlow()

    fun filter(text: String) {
        if (text.isBlank()) return

        _uiState.update { it.copy(isLoading = true, result = null, error = null) }

        viewModelScope.launch {
            try {
                // Generate key from SHA-256 hash of the text
                val key = sha256(text)
                val message = Message(key = key, text = text)
                val result = Trustall.messageFilter.filter(message)

                when (result) {
                    is FilterResult.Success -> {
                        _uiState.update { it.copy(isLoading = false, result = result) }
                    }
                    is FilterResult.Failure -> {
                        _uiState.update {
                            it.copy(
                                    isLoading = false,
                                    error = result.error.message ?: "Unknown error"
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message ?: "Unknown error") }
            }
        }
    }

    private fun sha256(input: String): String {
        val bytes = input.toByteArray()
        val md = java.security.MessageDigest.getInstance("SHA-256")
        val digest = md.digest(bytes)
        return digest.fold("") { str, it -> str + "%02x".format(it) }
    }
}
