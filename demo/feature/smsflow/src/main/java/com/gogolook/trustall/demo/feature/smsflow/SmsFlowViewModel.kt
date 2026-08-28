package com.gogolook.trustall.demo.feature.smsflow

import androidx.activity.ComponentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gogolook.trustall.core.Trustall
import com.gogolook.trustall.demo.core.ui.ScannedSms
import com.gogolook.trustall.permission.model.PermissionResult
import com.gogolook.trustall.smsflow.smsFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface SmsFlowUiState {
    data object Idle : SmsFlowUiState
    data object NeedPermission : SmsFlowUiState
    data class Listening(val messages: List<ScannedSms>) : SmsFlowUiState
}

class SmsFlowViewModel : ViewModel() {

    private val permissionGranted = MutableStateFlow(false)

    // Messages come from SmsFlowManager (started in DemoApplication), so the list stays
    // in sync with what the alert overlay shows and nothing is scanned twice.
    val uiState: StateFlow<SmsFlowUiState> =
        combine(permissionGranted, SmsFlowManager.messages) { granted, messages ->
            if (granted) SmsFlowUiState.Listening(messages) else SmsFlowUiState.NeedPermission
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SmsFlowUiState.Idle)

    fun checkPermission() {
        permissionGranted.value = Trustall.smsFlow.hasSmsReceivePermission()
    }

    fun requestPermission(activity: ComponentActivity) {
        viewModelScope.launch {
            val result = Trustall.smsFlow.requestSmsReceivePermission(activity)
            permissionGranted.value = result is PermissionResult.Granted
        }
    }
}
