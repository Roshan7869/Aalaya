package com.aalay.app.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aalay.app.data.models.StudentUser
import com.aalay.app.data.repository.StudentAuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authRepository: StudentAuthRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    init {
        loadUserProfile()
    }
    
    private fun loadUserProfile() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val user = authRepository.getCurrentUser()
                _uiState.value = _uiState.value.copy(
                    user = user,
                    error = null
                )
            } catch (e: Exception) {
                Timber.e(e, "Failed to load user profile")
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Failed to load profile"
                )
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun updateProfile(updatedUser: StudentUser) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val result = authRepository.updateUserProfile(updatedUser)
                
                if (result.isSuccess) {
                    _uiState.value = _uiState.value.copy(
                        user = updatedUser,
                        error = null
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        error = result.exceptionOrNull()?.message ?: "Update failed"
                    )
                }
            } catch (e: Exception) {
                Timber.e(e, "Profile update failed")
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Update failed"
                )
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun signOut() {
        viewModelScope.launch {
            try {
                authRepository.signOut()
                _uiState.value = ProfileUiState()
            } catch (e: Exception) {
                Timber.e(e, "Sign out failed")
            }
        }
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}

data class ProfileUiState(
    val user: StudentUser? = null,
    val error: String? = null
)