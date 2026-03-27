package com.gogolook.trustall.demo.feature.calllog

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.activity.ComponentActivity
import com.gogolook.trustall.callerid.callerId
import com.gogolook.trustall.callerid.model.NumberInfo
import com.gogolook.trustall.callerid.model.NumberInfoState
import com.gogolook.trustall.calllog.callLog
import com.gogolook.trustall.calllog.model.CallLog
import com.gogolook.trustall.calllog.model.UploadResult
import com.gogolook.trustall.contact.contact
import com.gogolook.trustall.core.Trustall
import com.gogolook.trustall.permission.model.PermissionResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface UploadState {
    data object Idle : UploadState
    data object Loading : UploadState
    data object Success : UploadState
    data class Error(val message: String) : UploadState
}

data class CallLogUiModel(
    val callLog: CallLog,
    val numberInfo: NumberInfo? = null,
    val uploadState: UploadState = UploadState.Idle,
)

sealed interface CallLogUiState {
    data object Idle : CallLogUiState
    data object Loading : CallLogUiState
    data object NeedPermission : CallLogUiState
    data class Success(val logs: List<CallLogUiModel>) : CallLogUiState
    data class Error(val message: String) : CallLogUiState
}

class CallLogViewModel : ViewModel() {
    private val _uiState = MutableStateFlow<CallLogUiState>(CallLogUiState.Idle)
    val uiState: StateFlow<CallLogUiState> = _uiState.asStateFlow()

    private val _autoUploadState = MutableStateFlow<UploadState>(UploadState.Idle)
    val autoUploadState: StateFlow<UploadState> = _autoUploadState.asStateFlow()

    fun checkAndLoadLogs() {
        if (Trustall.callLog.hasCallLogPermission()) {
            loadLogs()
        } else {
            _uiState.value = CallLogUiState.NeedPermission
        }
    }

    fun requestPermissions(activity: ComponentActivity) {
        viewModelScope.launch {
            val result = Trustall.callLog.requestCallLogPermission(activity)
            if (result is PermissionResult.Granted) {
                Trustall.contact.requestContactPermission(activity)
                loadLogs()
            }
        }
    }

    private fun loadLogs() {
        _uiState.value = CallLogUiState.Loading
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val logs = Trustall.callLog.getCallLogs()
                _uiState.value = CallLogUiState.Success(logs.map { CallLogUiModel(it) })
                fetchNumberInfoForDistinctNumbers(logs)
            } catch (e: Exception) {
                _uiState.value = CallLogUiState.Error(e.message ?: "Failed to load call logs")
            }
        }
    }

    fun uploadCallLog(callLog: CallLog) {
        viewModelScope.launch {
            updateItemUploadState(callLog.id, UploadState.Loading)
            val result = Trustall.callLog.uploadCallLogs(listOf(callLog))
            updateItemUploadState(
                id = callLog.id,
                state = when (result) {
                    is UploadResult.Success -> UploadState.Success
                    is UploadResult.Error -> UploadState.Error("${result.code}: ${result.message}")
                    is UploadResult.NetworkError -> UploadState.Error(result.exception.message ?: "Network error")
                }
            )
        }
    }

    fun autoUploadCallLogs() {
        if (_autoUploadState.value is UploadState.Loading) return
        viewModelScope.launch {
            _autoUploadState.value = UploadState.Loading
            val result = Trustall.callLog.autoUploadCallLogs()
            _autoUploadState.value = when (result) {
                is UploadResult.Success -> UploadState.Success
                is UploadResult.Error -> UploadState.Error("${result.code}: ${result.message}")
                is UploadResult.NetworkError -> UploadState.Error(result.exception.message ?: "Network error")
            }
        }
    }

    private fun updateItemUploadState(id: Long, state: UploadState) {
        val current = _uiState.value as? CallLogUiState.Success ?: return
        _uiState.value = current.copy(
            logs = current.logs.map { if (it.callLog.id == id) it.copy(uploadState = state) else it }
        )
    }

    private fun fetchNumberInfoForDistinctNumbers(logs: List<CallLog>) {
        val distinctNumbers = logs.map { it.number }.distinct()
        viewModelScope.launch(Dispatchers.IO) {
            distinctNumbers.forEach { number ->
                launch {
                    try {
                        Trustall.callerId.getNumberInfo(number).collect { state ->
                            val info = when (state) {
                                is NumberInfoState.Partial -> state.numberInfo
                                is NumberInfoState.Finish -> state.numberInfo
                                is NumberInfoState.Loading -> null
                            }
                            if (info != null) {
                                val currentState = _uiState.value
                                if (currentState is CallLogUiState.Success) {
                                    _uiState.value = currentState.copy(
                                        logs = currentState.logs.map {
                                            if (it.callLog.number == number) it.copy(numberInfo = info) else it
                                        }
                                    )
                                }
                            }
                        }
                    } catch (_: Exception) {}
                }
            }
        }
    }
}
