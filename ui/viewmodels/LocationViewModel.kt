package com.aalay.app.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.aalay.app.data.local.entities.LocationEntity
import com.aalay.app.data.local.entities.LocationType
import com.aalay.app.domain.usecase.*

/**
 * ViewModel for Bhilai location screen
 * Manages UI state for location display and navigation
 */
@HiltViewModel
class LocationViewModel @Inject constructor(
    private val getBhilaiLocationsUseCase: GetBhilaiLocationsUseCase,
    private val searchBhilaiLocationsUseCase: SearchBhilaiLocationsUseCase,
    private val generateMapsUrlUseCase: GenerateMapsUrlUseCase,
    private val calculateDistanceUseCase: CalculateDistanceUseCase,
    private val manageBhilaiLocationUseCase: ManageBhilaiLocationUseCase
) : ViewModel() {
    
    // UI State
    private val _uiState = MutableStateFlow(LocationUiState())
    val uiState: StateFlow<LocationUiState> = _uiState.asStateFlow()
    
    // Search query
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()
    
    // Selected filter type
    private val _selectedType = MutableStateFlow<LocationType?>(null)
    val selectedType: StateFlow<LocationType?> = _selectedType.asStateFlow()
    
    // Locations data
    val locations: StateFlow<List<LocationEntity>> = combine(
        searchQuery,
        selectedType,
        uiState
    ) { query, type, state ->
        Triple(query, type, state.refreshTrigger)
    }.flatMapLatest { (query, type, _) ->
        when {
            query.isNotBlank() -> searchBhilaiLocationsUseCase(query)
            type != null -> getBhilaiLocationsUseCase.byType(type.value)
            else -> getBhilaiLocationsUseCase()
        }
    }.catch { error ->
        _uiState.update { it.copy(error = error.message) }
        emit(emptyList())
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )
    
    // Featured locations
    val featuredLocations: StateFlow<List<LocationEntity>> = 
        getBhilaiLocationsUseCase.featured(limit = 5)
            .catch { emit(emptyList()) }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )
    
    // Statistics
    private val _stats = MutableStateFlow<LocationStats?>(null)
    val stats: StateFlow<LocationStats?> = _stats.asStateFlow()
    
    init {
        loadLocationStats()
        refreshLocations()
    }
    
    /**
     * Update search query
     */
    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }
    
    /**
     * Clear search query
     */
    fun clearSearch() {
        _searchQuery.value = ""
    }
    
    /**
     * Set location type filter
     */
    fun setLocationTypeFilter(type: LocationType?) {
        _selectedType.value = type
    }
    
    /**
     * Get Google Maps URL for a location
     */
    fun getNavigationUrl(location: LocationEntity): String {
        return try {
            generateMapsUrlUseCase(location)
        } catch (e: Exception) {
            // Fallback to basic URL if validation fails
            "https://www.google.com/maps/search/?api=1&query=${location.latitude},${location.longitude}"
        }
    }
    
    /**
     * Get directions URL for a location
     */
    fun getDirectionsUrl(location: LocationEntity): String {
        return try {
            generateMapsUrlUseCase.directions(location)
        } catch (e: Exception) {
            "https://www.google.com/maps/dir/?api=1&destination=${location.latitude},${location.longitude}"
        }
    }
    
    /**
     * Calculate distance from center for a location
     */
    fun getDistanceFromCenter(location: LocationEntity): String {
        val distance = calculateDistanceUseCase(location)
        return calculateDistanceUseCase.formatDistance(distance)
    }
    
    /**
     * Check if location is within walking distance
     */
    fun isWalkingDistance(location: LocationEntity): Boolean {
        return calculateDistanceUseCase.isWalkingDistance(location)
    }
    
    /**
     * Refresh locations from network
     */
    fun refreshLocations() {
        _uiState.update { it.copy(isLoading = true, error = null) }
        
        viewModelScope.launch {
            try {
                val success = manageBhilaiLocationUseCase.refreshData()
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        refreshTrigger = it.refreshTrigger + 1,
                        error = if (!success) "Failed to refresh data" else null
                    )
                }
                loadLocationStats()
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Unknown error occurred"
                    )
                }
            }
        }
    }
    
    /**
     * Get location by ID
     */
    fun getLocationById(locationId: String, callback: (LocationEntity?) -> Unit) {
        viewModelScope.launch {
            val location = manageBhilaiLocationUseCase.getById(locationId)
            callback(location)
        }
    }
    
    /**
     * Update location availability
     */
    fun updateLocationAvailability(locationId: String, availability: String) {
        viewModelScope.launch {
            try {
                manageBhilaiLocationUseCase.updateAvailability(locationId, availability)
                refreshLocations()
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Failed to update availability") }
            }
        }
    }
    
    /**
     * Load location statistics
     */
    private fun loadLocationStats() {
        viewModelScope.launch {
            try {
                val locationStats = manageBhilaiLocationUseCase.getStats()
                _stats.value = LocationStats(
                    totalCount = locationStats.totalLocations,
                    roomCount = locationStats.roomCount,
                    messCount = locationStats.messCount
                )
            } catch (e: Exception) {
                // Ignore stats loading errors
            }
        }
    }
    
    /**
     * Clear error message
     */
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
    
    /**
     * Get filtered locations based on current filters
     */
    fun getFilteredLocations(
        verifiedOnly: Boolean = false,
        availableOnly: Boolean = true,
        maxPrice: Int? = null,
        minRating: Float? = null
    ): Flow<List<LocationEntity>> {
        val currentType = selectedType.value?.value ?: "room"
        return getBhilaiLocationsUseCase.filtered(
            type = currentType,
            verifiedOnly = verifiedOnly,
            availableOnly = availableOnly,
            maxPrice = maxPrice,
            minRating = minRating
        )
    }
    
    /**
     * Get nearby locations
     */
    fun getNearbyLocations(
        latitude: Double? = null,
        longitude: Double? = null,
        radiusKm: Double = 5.0
    ): Flow<List<LocationEntity>> {
        return if (latitude != null && longitude != null) {
            getBhilaiLocationsUseCase.nearby(latitude, longitude, radiusKm)
        } else {
            getBhilaiLocationsUseCase.nearby(radiusKm = radiusKm)
        }
    }
}

/**
 * UI State for Location screen
 */
data class LocationUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val refreshTrigger: Int = 0
)

/**
 * Location statistics for UI display
 */
data class LocationStats(
    val totalCount: Int = 0,
    val roomCount: Int = 0,
    val messCount: Int = 0
) {
    val hasData: Boolean get() = totalCount > 0
}