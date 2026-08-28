package com.gogolook.trustall.demo.feature.smslog

import androidx.activity.ComponentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gogolook.trustall.callerid.callerId
import com.gogolook.trustall.callerid.model.NumberInfo
import com.gogolook.trustall.callerid.model.NumberInfoState
import com.gogolook.trustall.contact.contact
import com.gogolook.trustall.core.Trustall
import com.gogolook.trustall.core.urlscan.model.CachePolicy
import com.gogolook.trustall.core.urlscan.model.UrlScanResult
import com.gogolook.trustall.core.urlscan.urlScan
import com.gogolook.trustall.permission.model.PermissionResult
import com.gogolook.trustall.msgfilter.messageFilter
import com.gogolook.trustall.msgfilter.model.FilterResult
import com.gogolook.trustall.msgfilter.model.FilterType
import com.gogolook.trustall.msgfilter.model.Message
import com.gogolook.trustall.smslog.model.SmsLog
import com.gogolook.trustall.smslog.smsLog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SmsLogUiModel(
    val smsLog: SmsLog,
    val numberInfo: NumberInfo? = null,
    val filterType: FilterType? = null,
    val urlResults: List<UrlScanResult>? = null
)

sealed interface SmsLogUiState {
    data object Idle : SmsLogUiState
    data object Loading : SmsLogUiState
    data object NeedPermission : SmsLogUiState
    data class Success(val logs: List<SmsLogUiModel>) : SmsLogUiState
    data class Error(val message: String) : SmsLogUiState
}

class SmsLogViewModel : ViewModel() {
    private val _uiState = MutableStateFlow<SmsLogUiState>(SmsLogUiState.Idle)
    val uiState: StateFlow<SmsLogUiState> = _uiState.asStateFlow()

    fun checkAndLoadLogs() {
        if (Trustall.smsLog.hasSmsLogPermission()) {
            loadLogs()
        } else {
            _uiState.value = SmsLogUiState.NeedPermission
        }
    }

    fun requestPermissions(activity: ComponentActivity) {
        viewModelScope.launch {
            val result = Trustall.smsLog.requestSmsLogPermission(activity)
            if (result is PermissionResult.Granted) {
                Trustall.contact.requestContactPermission(activity)
                loadLogs()
            }
        }
    }

    private fun loadLogs() {
        _uiState.value = SmsLogUiState.Loading
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val logs = Trustall.smsLog.getSmsLogs()
                _uiState.value = SmsLogUiState.Success(logs.map { SmsLogUiModel(it) })
                fetchNumberInfoForDistinctNumbers(logs)
                fetchFilterInfoForLogs(logs)
                fetchUrlScanForLogs(logs)
            } catch (e: Exception) {
                _uiState.value = SmsLogUiState.Error(e.message ?: "Failed to load SMS logs")
            }
        }
    }

    private fun fetchNumberInfoForDistinctNumbers(logs: List<SmsLog>) {
        val distinctNumbers = logs.map { it.displayAddress }.distinct()
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
                                _uiState.update { state ->
                                    if (state !is SmsLogUiState.Success) return@update state
                                    SmsLogUiState.Success(
                                        state.logs.map {
                                            if (it.smsLog.displayAddress == number) it.copy(numberInfo = info) else it
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

    private fun fetchUrlScanForLogs(logs: List<SmsLog>) {
        viewModelScope.launch(Dispatchers.IO) {
            logs.forEach { log ->
                launch {
                    try {
                        // scanText extracts URLs locally first, so bodies without URLs
                        // trigger no network call. Daily cache avoids rescanning the
                        // same URLs on every reload.
                        val results = Trustall.urlScan.scanText(log.displayBody, CachePolicy.day(1))
                        // Atomic update: concurrent scans (and the other fetches) would
                        // otherwise overwrite each other's read-modify-write.
                        _uiState.update { state ->
                            if (state !is SmsLogUiState.Success) return@update state
                            SmsLogUiState.Success(
                                state.logs.map {
                                    if (it.smsLog.id == log.id) it.copy(urlResults = results) else it
                                }
                            )
                        }
                    } catch (_: Exception) {}
                }
            }
        }
    }

    private fun fetchFilterInfoForLogs(logs: List<SmsLog>) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val messages = logs.map {
                    Message(key = it.id, text = it.displayBody)
                }

                if (messages.isEmpty()) return@launch

                when (val result = Trustall.messageFilter.filter(messages)) {
                    is FilterResult.Success -> {
                        _uiState.update { state ->
                            if (state !is SmsLogUiState.Success) return@update state
                            SmsLogUiState.Success(
                                state.logs.map { uiModel ->
                                    val type = result.results[uiModel.smsLog.id]
                                    if (type != null) uiModel.copy(filterType = type) else uiModel
                                }
                            )
                        }
                    }
                    is FilterResult.Failure -> {}
                }
            } catch (_: Exception) {}
        }
    }
}
