package com.gogolook.trustall.demo.feature.offlinedb

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gogolook.trustall.core.Trustall
import com.gogolook.trustall.core.offlinedb.model.DownloadState
import com.gogolook.trustall.core.offlinedb.model.OfflineDbProfile
import com.gogolook.trustall.core.offlinedb.model.OfflineNumberInfo
import com.gogolook.trustall.core.offlinedb.offlineDb
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class OfflineDbViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(OfflineDbUiState())
    val uiState: StateFlow<OfflineDbUiState> = _uiState.asStateFlow()

    init {
        refreshProfile()
    }

    fun refreshProfile() {
        viewModelScope.launch {
            try {
                val profile = Trustall.offlineDb.getDbProfile()
                _uiState.value = _uiState.value.copy(dbProfile = profile)
            } catch (e: Exception) {
                // Handle error if needed, or just ignore for profile check
            }
        }
    }

    fun downloadDb() {
        viewModelScope.launch {
            // Reset previous state
            _uiState.value = _uiState.value.copy(downloadState = null)
            
            Trustall.offlineDb.downloadIfNeeded().collect { state ->
                 _uiState.value = _uiState.value.copy(downloadState = state)
                 if (state is DownloadState.Finished) {
                     refreshProfile()
                 }
            }
        }
    }

    fun clearDb() {
        viewModelScope.launch {
            Trustall.offlineDb.clear()
            _uiState.value = _uiState.value.copy(
                dbProfile = null,
                searchResult = null,
                downloadState = null // Reset download state on clear
            )
        }
    }

    fun search(number: String) {
        if (number.isBlank()) return
        
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                searchResult = null,
                searchError = null,
                isSearching = true
            )
            try {
                val info = Trustall.offlineDb.getNumberInfo(number)
                if (info != null) {
                    _uiState.value = _uiState.value.copy(searchResult = info)
                } else {
                    _uiState.value = _uiState.value.copy(searchError = "No offline data found for this number.")
                }
            } catch (e: Exception) {
                Log.e("OfflineDbViewModel", "Error searching offline database: ${e.message}", e)
                _uiState.value = _uiState.value.copy(searchError = "Search failed: ${e.message}")
            } finally {
                _uiState.value = _uiState.value.copy(isSearching = false)
            }
        }
    }
}

data class OfflineDbUiState(
    val dbProfile: OfflineDbProfile? = null,
    val downloadState: DownloadState? = null,
    val searchResult: OfflineNumberInfo? = null,
    val searchError: String? = null,
    val isSearching: Boolean = false
)
