package com.aalay.app.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aalay.app.data.models.Accommodation
import com.aalay.app.data.models.LatLong
import com.aalay.app.data.models.StudentSearchFilters
import com.aalay.app.data.repository.AccommodationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val accommodationRepository: AccommodationRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()
    
    private val _currentLocation = MutableStateFlow<LatLong?>(null)
    val currentLocation: StateFlow<LatLong?> = _currentLocation.asStateFlow()
    
    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }
    
    fun updateLocation(location: LatLong) {
        _currentLocation.value = location
    }
    
    fun searchAccommodations(
        location: LatLong,
        filters: StudentSearchFilters = StudentSearchFilters()
    ) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                accommodationRepository.searchAccommodations(location, filters = filters)
                    .collect { result ->
                        if (result.isSuccess) {
                            _uiState.value = _uiState.value.copy(
                                accommodations = result.getOrNull() ?: emptyList(),
                                error = null
                            )
                        } else {
                            _uiState.value = _uiState.value.copy(
                                error = result.exceptionOrNull()?.message ?: "Search failed"
                            )
                        }
                    }
            } catch (e: Exception) {
                Timber.e(e, "Search failed")
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Search failed"
                )
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun getRecommendations(studentId: String, location: LatLong?) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val result = accommodationRepository.getPersonalizedRecommendations(studentId, location)
                
                if (result.isSuccess) {
                    _uiState.value = _uiState.value.copy(
                        accommodations = result.getOrNull() ?: emptyList(),
                        error = null
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        error = result.exceptionOrNull()?.message ?: "Failed to load recommendations"
                    )
                }
            } catch (e: Exception) {
                Timber.e(e, "Failed to load recommendations")
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Failed to load recommendations"
                )
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}

data class SearchUiState(
    val accommodations: List<Accommodation> = emptyList(),
    val error: String? = null
)