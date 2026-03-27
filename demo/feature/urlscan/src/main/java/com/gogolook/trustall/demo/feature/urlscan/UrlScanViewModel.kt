package com.gogolook.trustall.demo.feature.urlscan

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gogolook.trustall.core.Trustall
import com.gogolook.trustall.core.urlscan.model.CachePolicy
import com.gogolook.trustall.core.urlscan.model.UrlScanResult
import com.gogolook.trustall.core.urlscan.urlScan
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class UrlScanViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(UrlScanUiState())
    val uiState: StateFlow<UrlScanUiState> = _uiState.asStateFlow()

    fun scan(url: String, allowCache: Boolean, cacheDurationMinutes: Int?) {
        if (url.isBlank()) return
        
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true, 
                result = null, 
                error = null
            )
            
            try {
                // Trustall.urlScan
                val cachePolicy = if (allowCache) CachePolicy.minute(cacheDurationMinutes ?: 1) else CachePolicy.NO_CACHE
                
                // Note: The CachePolicy constructor in `TrustallUrlScan.scan` might need adjustment based on how it's exposed. 
                // Based on previous file reading: 
                // suspend fun scan(url: String, cachePolicy: CachePolicy = CachePolicy.NO_CACHE): UrlScanResult
                
                val result = Trustall.urlScan.scan(url, cachePolicy)
                if (result is UrlScanResult.Success) {
                    _uiState.value = _uiState.value.copy(result = result)
                } else if (result is UrlScanResult.Error) {
                    _uiState.value = _uiState.value.copy(error = "Scan failed: ${result.error.message}")
                }
            } catch (e: Exception) {
                 _uiState.value = _uiState.value.copy(error = "Error: ${e.message}")
            } finally {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }
}

data class UrlScanUiState(
    val isLoading: Boolean = false,
    val result: UrlScanResult.Success? = null,
    val error: String? = null,
)
