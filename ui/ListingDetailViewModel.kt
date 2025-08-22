package com.aalay.app.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aalay.app.data.models.Accommodation
import com.aalay.app.data.repository.AccommodationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class ListingDetailViewModel @Inject constructor(
    private val accommodationRepository: AccommodationRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(ListingDetailUiState())
    val uiState: StateFlow<ListingDetailUiState> = _uiState.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    fun loadAccommodationDetails(accommodationId: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val result = accommodationRepository.getAccommodationDetails(accommodationId)
                
                if (result.isSuccess) {
                    _uiState.value = _uiState.value.copy(
                        accommodation = result.getOrNull()?.accommodation,
                        error = null
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        error = result.exceptionOrNull()?.message ?: "Failed to load accommodation"
                    )
                }
            } catch (e: Exception) {
                Timber.e(e, "Failed to load accommodation details")
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Failed to load accommodation"
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

data class ListingDetailUiState(
    val accommodation: Accommodation? = null,
    val error: String? = null
)