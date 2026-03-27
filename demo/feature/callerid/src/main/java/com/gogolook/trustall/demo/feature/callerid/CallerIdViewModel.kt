package com.gogolook.trustall.demo.feature.callerid

import androidx.activity.ComponentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gogolook.trustall.callerid.callerId
import com.gogolook.trustall.contact.contact
import com.gogolook.trustall.core.Trustall
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

import com.gogolook.trustall.permission.model.PermissionResult
import kotlinx.coroutines.Dispatchers

data class CallerIdUiState(
    val hasPhonePermissions: Boolean = false,
    val hasCallLogPermissions: Boolean = false,
    val hasContactPermission: Boolean = false,
    val isCallScreeningRoleHeld: Boolean = false,
    val isCallRedirectionRoleHeld: Boolean = false,
    val hasOverlayPermission: Boolean = false,
    val logs: List<String> = emptyList(),
    val permissionDialogRequest: PermissionDialogRequest? = null
)

data class PermissionDialogRequest(
    val type: PermissionDialogType,
    val openSettings: Boolean
)

enum class PermissionDialogType {
    PHONE, CALL_LOG, CONTACT, SCREENING, REDIRECTION
}

class CallerIdViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(CallerIdUiState())
    val uiState: StateFlow<CallerIdUiState> = _uiState.asStateFlow()

    private val trustallCallerId = Trustall.callerId

    init {
        checkPermissions()
        viewModelScope.launch {
            CallerIdLogManager.logs.collect { logs ->
                _uiState.value = _uiState.value.copy(logs = logs)
            }
        }
    }


    fun checkPermissions() {
        _uiState.value = _uiState.value.copy(
            hasPhonePermissions = trustallCallerId.hasPhonePermissions(),
            hasCallLogPermissions = trustallCallerId.hasCallLogPermissions(),
            hasContactPermission = Trustall.contact.hasContactPermission(),
            isCallScreeningRoleHeld = trustallCallerId.isCallScreeningRoleHeld(),
            isCallRedirectionRoleHeld = trustallCallerId.isCallRedirectionRoleHeld(),
            hasOverlayPermission = trustallCallerId.canDrawOverlays(),
        )
    }

    private fun handlePermissionResult(result: PermissionResult, type: PermissionDialogType) {
        checkPermissions()
        when (result) {
            PermissionResult.Granted -> { /* No dialog needed */ }
            PermissionResult.ShowRationale -> {
                _uiState.value = _uiState.value.copy(
                    permissionDialogRequest = PermissionDialogRequest(type, openSettings = false)
                )
            }
            else -> { // NeverAskAgain, NotSupported, or generic denial requiring settings
                _uiState.value = _uiState.value.copy(
                    permissionDialogRequest = PermissionDialogRequest(type, openSettings = true)
                )
            }
        }
    }
    
    fun dismissDialog() {
        _uiState.value = _uiState.value.copy(permissionDialogRequest = null)
    }

    fun requestPhonePermissions(activity: ComponentActivity) {
        viewModelScope.launch {
            val result = trustallCallerId.requestPhonePermissions(activity)
            addLog("Request Phone Perm: $result")
            handlePermissionResult(result, PermissionDialogType.PHONE)
        }
    }

    fun requestCallLogPermissions(activity: ComponentActivity) {
        viewModelScope.launch {
            val result = trustallCallerId.requestCallLogPermissions(activity)
            addLog("Request CallLog Perm: $result")
            handlePermissionResult(result, PermissionDialogType.CALL_LOG)
        }
    }

    fun requestContactPermission(activity: ComponentActivity) {
        viewModelScope.launch {
            val result = Trustall.contact.requestContactPermission(activity)
            addLog("Request Contact Perm: $result")
            handlePermissionResult(result, PermissionDialogType.CONTACT)
        }
    }

    fun requestCallScreeningRole(activity: ComponentActivity) {
        viewModelScope.launch {
            val result = trustallCallerId.requestCallScreeningRole(activity)
            addLog("Request Screening Role: $result")
             handlePermissionResult(result, PermissionDialogType.SCREENING)
        }
    }

    fun requestCallRedirectionRole(activity: ComponentActivity) {
        viewModelScope.launch {
            val result = trustallCallerId.requestCallRedirectionRole(activity)
            addLog("Request Redirection Role: $result")
             handlePermissionResult(result, PermissionDialogType.REDIRECTION)
        }
    }
    
    fun requestOverlayPermission(activity: ComponentActivity) {
        viewModelScope.launch {
            trustallCallerId.launchDrawOverlaysSettings(activity)
        }
    }
    
    fun launchAppDetailsSettings(activity: ComponentActivity) {
        viewModelScope.launch {
            trustallCallerId.launchAppDetailsSettings(activity)
        }
    }

    fun launchManageDefaultAppsSettings(activity: ComponentActivity) {
        viewModelScope.launch {
            trustallCallerId.launchManageDefaultAppsSettings(activity)
        }
    }

    private fun addLog(message: String) {
        CallerIdLogManager.addLog(message)
    }
}
