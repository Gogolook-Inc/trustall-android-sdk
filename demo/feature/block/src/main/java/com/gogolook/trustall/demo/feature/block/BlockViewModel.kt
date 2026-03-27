package com.gogolook.trustall.demo.feature.block

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gogolook.trustall.callerid.callerId
import com.gogolook.trustall.core.Trustall
import com.gogolook.trustall.numberblock.model.BlockInfo
import com.gogolook.trustall.numberblock.numberBlock
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class BlockUiState(
        val blockedNumbers: List<BlockInfo> = emptyList(),
        val checkResult: Pair<String, Boolean>? = null, // Number to isBlocked
        val isLoading: Boolean = false,
        val error: String? = null,
        val isCallScreeningRoleHeld: Boolean = false,
        val isCallRedirectionRoleHeld: Boolean = false
)

class BlockViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(BlockUiState())
    val uiState: StateFlow<BlockUiState> = _uiState.asStateFlow()

    init {
        checkPermissions()
        loadBlockedNumbers()
    }

    fun checkPermissions() {
        val callerId = Trustall.callerId
        _uiState.update {
            it.copy(
                    isCallScreeningRoleHeld = callerId.isCallScreeningRoleHeld(),
                    isCallRedirectionRoleHeld = callerId.isCallRedirectionRoleHeld()
            )
        }
    }

    private fun loadBlockedNumbers() {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true) }
                val list = Trustall.numberBlock.getAll()
                _uiState.update { it.copy(blockedNumbers = list, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message, isLoading = false) }
            }
        }
    }

    fun addBlock(number: String) {
        if (number.isBlank()) return
        viewModelScope.launch {
            try {
                Trustall.numberBlock.add(number)
                loadBlockedNumbers()
                checkBlockStatus(number)
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    fun removeBlock(number: String) {
        viewModelScope.launch {
            try {
                Trustall.numberBlock.remove(number)
                loadBlockedNumbers()
                checkBlockStatus(number)
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    fun checkBlockStatus(number: String) {
        if (number.isBlank()) return
        viewModelScope.launch {
            try {
                val isBlocked = Trustall.numberBlock.isBlocked(number)
                _uiState.update { it.copy(checkResult = number to isBlocked) }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    fun clearAll() {
        viewModelScope.launch {
            try {
                Trustall.numberBlock.clearAll()
                loadBlockedNumbers()
                _uiState.update { it.copy(checkResult = null) }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }
}
