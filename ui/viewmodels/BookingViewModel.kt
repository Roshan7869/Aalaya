package com.aalay.app.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aalay.app.data.models.BookingRequest
import com.aalay.app.data.repository.AccommodationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class BookingViewModel @Inject constructor(
    private val accommodationRepository: AccommodationRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(BookingUiState())
    val uiState: StateFlow<BookingUiState> = _uiState.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    fun submitBooking(bookingRequest: BookingRequest) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val result = accommodationRepository.submitBooking(bookingRequest)
                
                if (result.isSuccess) {
                    _uiState.value = _uiState.value.copy(
                        isBookingConfirmed = true,
                        error = null
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        error = result.exceptionOrNull()?.message ?: "Booking failed"
                    )
                }
            } catch (e: Exception) {
                Timber.e(e, "Booking submission failed")
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Booking failed"
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

data class BookingUiState(
    val isBookingConfirmed: Boolean = false,
    val error: String? = null
)