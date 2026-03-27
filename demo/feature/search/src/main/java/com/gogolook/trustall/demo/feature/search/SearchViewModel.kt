package com.gogolook.trustall.demo.feature.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gogolook.trustall.core.Trustall
import com.gogolook.trustall.core.numbersearch.numberSearch
import com.gogolook.trustall.core.numbersearch.model.OnlineNumberInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SearchViewModel : ViewModel() {

    private val _uiState = MutableStateFlow<SearchUiState>(
        SearchUiState.Idle(
            SampleInfo(
                allSamples = emptyList(),
                availableCountries = emptyList(),
                selectedCountry = null,
                filteredSamples = emptyList()
            )
        )
    )
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()
    
    // Initialize loaded samples
    init {
        val allSamples = SampleData.samples
        val countries = SampleData.countries
        // Default to first country if available
        val initialCountry = countries.firstOrNull()
        
        val initialSampleInfo = SampleInfo(
            allSamples = allSamples,
            availableCountries = countries,
            selectedCountry = initialCountry,
            filteredSamples = allSamples.filter { it.country == initialCountry }
        )
        
        _uiState.value = SearchUiState.Idle(initialSampleInfo)
    }

    fun search(phoneNumber: String) {
        if (phoneNumber.isBlank()) return

        val currentSampleInfo = _uiState.value.sampleInfo
        
        viewModelScope.launch {
            _uiState.value = SearchUiState.Loading(currentSampleInfo)
            try {
                // Accessing Trustall.numberSearch via extension
                val result = Trustall.numberSearch.getNumberInfo(phoneNumber)
                if (result != null) {
                    _uiState.value = SearchUiState.Success(currentSampleInfo, result)
                } else {
                    _uiState.value = SearchUiState.Error(currentSampleInfo, "No result found")
                }
            } catch (e: Exception) {
                _uiState.value = SearchUiState.Error(currentSampleInfo, "Search failed: ${e.message}")
            }
        }
    }
    
    fun selectCountry(country: String) {
        val currentSampleInfo = _uiState.value.sampleInfo
        val newSampleInfo = currentSampleInfo.copy(
            selectedCountry = country,
            filteredSamples = currentSampleInfo.allSamples.filter { it.country == country }
        )
        
        _uiState.value = _uiState.value.copySampleInfo(newSampleInfo)
    }
    
    private fun SearchUiState.copySampleInfo(newSampleInfo: SampleInfo): SearchUiState {
        return when(this) {
            is SearchUiState.Idle -> this.copy(sampleInfo = newSampleInfo)
            is SearchUiState.Loading -> this.copy(sampleInfo = newSampleInfo)
            is SearchUiState.Success -> this.copy(sampleInfo = newSampleInfo)
            is SearchUiState.Error -> this.copy(sampleInfo = newSampleInfo)
        }
    }
}

data class SampleInfo(
    val allSamples: List<SampleNumber> = emptyList(),
    val availableCountries: List<String> = emptyList(),
    val selectedCountry: String? = null,
    val filteredSamples: List<SampleNumber> = emptyList()
)

sealed interface SearchUiState {
    val sampleInfo: SampleInfo

    data class Idle(override val sampleInfo: SampleInfo) : SearchUiState
    data class Loading(override val sampleInfo: SampleInfo) : SearchUiState
    data class Success(override val sampleInfo: SampleInfo, val result: OnlineNumberInfo) : SearchUiState
    data class Error(override val sampleInfo: SampleInfo, val message: String) : SearchUiState
}
