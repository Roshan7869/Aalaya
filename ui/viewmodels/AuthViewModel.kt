package com.aalay.app.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aalay.app.data.repository.StudentAuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: StudentAuthRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    fun signInWithEmail(email: String, password: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val result = authRepository.signInWithEmail(email, password)
                
                if (result.isSuccess) {
                    _uiState.value = _uiState.value.copy(
                        isAuthenticated = true,
                        error = null
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        error = result.exceptionOrNull()?.message ?: "Authentication failed"
                    )
                }
            } catch (e: Exception) {
                Timber.e(e, "Sign in failed")
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Authentication failed"
                )
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun signUpWithEmail(email: String, password: String, firstName: String, lastName: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val result = authRepository.signUpWithEmail(email, password, firstName, lastName)
                
                if (result.isSuccess) {
                    _uiState.value = _uiState.value.copy(
                        isAuthenticated = true,
                        error = null
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        error = result.exceptionOrNull()?.message ?: "Registration failed"
                    )
                }
            } catch (e: Exception) {
                Timber.e(e, "Sign up failed")
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Registration failed"
                )
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun signInWithGoogle() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val result = authRepository.signInWithGoogle()
                
                if (result.isSuccess) {
                    _uiState.value = _uiState.value.copy(
                        isAuthenticated = true,
                        error = null
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        error = result.exceptionOrNull()?.message ?: "Google sign-in failed"
                    )
                }
            } catch (e: Exception) {
                Timber.e(e, "Google sign in failed")
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Google sign-in failed"
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

data class AuthUiState(
    val isAuthenticated: Boolean = false,
    val error: String? = null
)