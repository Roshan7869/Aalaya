package com.aalay.app.ui.viewmodels

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.aalay.app.data.repository.StudentAuthRepository
import com.aalay.app.data.models.StudentUser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.Assert.*
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.whenever
import org.mockito.kotlin.verify
import org.mockito.kotlin.any

@ExperimentalCoroutinesApi
class AuthViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = UnconfinedTestDispatcher()

    @Mock
    private lateinit var authRepository: StudentAuthRepository

    private lateinit var viewModel: AuthViewModel

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        Dispatchers.setMain(testDispatcher)
        viewModel = AuthViewModel(authRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `signInWithEmail should update UI state on success`() = runTest {
        // Arrange
        val email = "test@example.com"
        val password = "password123"
        val expectedUser = StudentUser(
            id = "test-user-id",
            email = email,
            fullName = "Test User"
        )
        whenever(authRepository.signInWithEmail(email, password))
            .thenReturn(Result.success(expectedUser))

        // Act
        viewModel.signInWithEmail(email, password)

        // Assert
        val uiState = viewModel.uiState.first()
        assertTrue(uiState.isAuthenticated)
        assertNull(uiState.error)
        verify(authRepository).signInWithEmail(email, password)
    }

    @Test
    fun `signInWithEmail should update UI state on failure`() = runTest {
        // Arrange
        val email = "test@example.com"
        val password = "wrongpassword"
        val errorMessage = "Invalid credentials"
        whenever(authRepository.signInWithEmail(email, password))
            .thenReturn(Result.failure(Exception(errorMessage)))

        // Act
        viewModel.signInWithEmail(email, password)

        // Assert
        val uiState = viewModel.uiState.first()
        assertFalse(uiState.isAuthenticated)
        assertEquals(errorMessage, uiState.error)
        verify(authRepository).signInWithEmail(email, password)
    }

    @Test
    fun `signUpWithEmail should call repository with correct parameters`() = runTest {
        // Arrange
        val email = "newuser@example.com"
        val password = "password123"
        val firstName = "John"
        val lastName = "Doe"
        val expectedUser = StudentUser(
            id = "new-user-id",
            email = email,
            fullName = "$firstName $lastName"
        )
        whenever(authRepository.signUpWithEmail(email, password, firstName, lastName))
            .thenReturn(Result.success(expectedUser))

        // Act
        viewModel.signUpWithEmail(email, password, firstName, lastName)

        // Assert
        val uiState = viewModel.uiState.first()
        assertTrue(uiState.isAuthenticated)
        assertNull(uiState.error)
        verify(authRepository).signUpWithEmail(email, password, firstName, lastName)
    }

    @Test
    fun `signInWithGoogle should handle repository call`() = runTest {
        // Arrange
        val expectedUser = StudentUser(
            id = "google-user-id",
            email = "google@example.com",
            fullName = "Google User"
        )
        whenever(authRepository.signInWithGoogle())
            .thenReturn(Result.success(expectedUser))

        // Act
        viewModel.signInWithGoogle()

        // Assert
        val uiState = viewModel.uiState.first()
        assertTrue(uiState.isAuthenticated)
        assertNull(uiState.error)
        verify(authRepository).signInWithGoogle()
    }

    @Test
    fun `loading state should be managed correctly during sign in`() = runTest {
        // Arrange
        val email = "test@example.com"
        val password = "password123"
        whenever(authRepository.signInWithEmail(any(), any()))
            .thenReturn(Result.success(StudentUser(id = "test-id", email = email)))

        // Act & Assert
        assertFalse(viewModel.isLoading.first())
        
        viewModel.signInWithEmail(email, password)
        
        // Loading should be false after completion
        assertFalse(viewModel.isLoading.first())
    }

    @Test
    fun `clearError should reset error state`() = runTest {
        // Arrange
        val email = "test@example.com"
        val password = "wrongpassword"
        whenever(authRepository.signInWithEmail(email, password))
            .thenReturn(Result.failure(Exception("Error")))

        // Act
        viewModel.signInWithEmail(email, password)
        viewModel.clearError()

        // Assert
        val uiState = viewModel.uiState.first()
        assertNull(uiState.error)
    }

    @Test
    fun `initial state should be correct`() = runTest {
        // Assert
        val uiState = viewModel.uiState.first()
        assertFalse(uiState.isAuthenticated)
        assertNull(uiState.error)
        assertFalse(viewModel.isLoading.first())
    }
}