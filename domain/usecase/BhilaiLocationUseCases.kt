package com.aalay.app.domain.usecase

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import com.aalay.app.data.local.entities.LocationEntity
import com.aalay.app.data.repository.BhilaiLocationRepository
import com.aalay.app.utils.MapUtils

/**
 * Use case for getting Bhilai locations with various filters
 */
class GetBhilaiLocationsUseCase @Inject constructor(
    private val repository: BhilaiLocationRepository
) {
    
    /**
     * Get all locations
     */
    operator fun invoke(forceRefresh: Boolean = false): Flow<List<LocationEntity>> {
        return repository.getAllLocations(forceRefresh)
    }
    
    /**
     * Get locations by type
     */
    fun byType(type: String, forceRefresh: Boolean = false): Flow<List<LocationEntity>> {
        return repository.getLocationsByType(type, forceRefresh)
    }
    
    /**
     * Get filtered locations
     */
    fun filtered(
        type: String,
        verifiedOnly: Boolean = false,
        availableOnly: Boolean = true,
        maxPrice: Int? = null,
        minRating: Float? = null
    ): Flow<List<LocationEntity>> {
        return repository.getFilteredLocations(type, verifiedOnly, availableOnly, maxPrice, minRating)
    }
    
    /**
     * Get nearby locations
     */
    fun nearby(
        latitude: Double = MapUtils.BHILAI_CENTER_LAT,
        longitude: Double = MapUtils.BHILAI_CENTER_LNG,
        radiusKm: Double = 5.0
    ): Flow<List<LocationEntity>> {
        return repository.getNearbyLocations(latitude, longitude, radiusKm)
    }
    
    /**
     * Get featured locations
     */
    fun featured(type: String? = null, limit: Int = 10): Flow<List<LocationEntity>> {
        return repository.getFeaturedLocations(type, limit)
    }
    
    /**
     * Get top-rated locations
     */
    fun topRated(
        type: String? = null,
        minRating: Float = 4.0f,
        limit: Int = 10
    ): Flow<List<LocationEntity>> {
        return repository.getTopRatedLocations(type, minRating, limit)
    }
}

/**
 * Use case for searching Bhilai locations
 */
class SearchBhilaiLocationsUseCase @Inject constructor(
    private val repository: BhilaiLocationRepository
) {
    
    operator fun invoke(query: String): Flow<List<LocationEntity>> {
        return if (query.isBlank()) {
            repository.getAllLocations()
        } else {
            repository.searchLocations(query.trim())
        }
    }
}

/**
 * Use case for generating Google Maps URLs for Bhilai locations
 */
class GenerateMapsUrlUseCase @Inject constructor() {
    
    /**
     * Generate navigation URL for a location
     */
    operator fun invoke(location: LocationEntity): String {
        return MapUtils.generateMapsUrl(location.latitude, location.longitude)
    }
    
    /**
     * Generate navigation URL with coordinates
     */
    fun forCoordinates(latitude: Double, longitude: Double): String {
        return MapUtils.generateMapsUrl(latitude, longitude)
    }
    
    /**
     * Generate directions URL
     */
    fun directions(location: LocationEntity): String {
        return MapUtils.generateDirectionsUrl(location.latitude, location.longitude)
    }
    
    /**
     * Generate URL with location name
     */
    fun withName(location: LocationEntity): String {
        return MapUtils.generateMapsUrlWithName(
            location.latitude, 
            location.longitude, 
            location.name
        )
    }
}

/**
 * Use case for calculating distances in Bhilai
 */
class CalculateDistanceUseCase @Inject constructor() {
    
    /**
     * Calculate distance from Bhilai center
     */
    operator fun invoke(location: LocationEntity): Double {
        return MapUtils.distanceFromBhilaiCenter(location.latitude, location.longitude)
    }
    
    /**
     * Calculate distance between two coordinates
     */
    fun between(
        lat1: Double, lng1: Double,
        lat2: Double, lng2: Double
    ): Double {
        return MapUtils.calculateDistance(lat1, lng1, lat2, lng2)
    }
    
    /**
     * Check if location is within walking distance from center
     */
    fun isWalkingDistance(location: LocationEntity, maxDistanceKm: Double = 2.0): Boolean {
        return MapUtils.isWalkingDistanceFromCenter(
            location.latitude, 
            location.longitude, 
            maxDistanceKm
        )
    }
    
    /**
     * Get formatted distance string
     */
    fun formatDistance(distanceKm: Double): String {
        return MapUtils.formatDistance(distanceKm)
    }
}

/**
 * Use case for validating Bhilai coordinates
 */
class ValidateBhilaiCoordinatesUseCase @Inject constructor() {
    
    /**
     * Validate if coordinates are within Bhilai bounds
     */
    operator fun invoke(latitude: Double, longitude: Double): Boolean {
        return MapUtils.isWithinBhilaiBounds(latitude, longitude)
    }
    
    /**
     * Validate location entity coordinates
     */
    fun validate(location: LocationEntity): Boolean {
        return location.isWithinBhilaiBounds()
    }
    
    /**
     * Sanitize and validate coordinates
     */
    fun sanitize(latitude: Double?, longitude: Double?): Pair<Double, Double>? {
        return MapUtils.validateAndSanitizeCoordinates(latitude, longitude)
    }
}

/**
 * Use case for managing location data
 */
class ManageBhilaiLocationUseCase @Inject constructor(
    private val repository: BhilaiLocationRepository
) {
    
    /**
     * Get location by ID
     */
    suspend fun getById(locationId: String): LocationEntity? {
        return repository.getLocationById(locationId)
    }
    
    /**
     * Save location
     */
    suspend fun save(location: LocationEntity): Boolean {
        return repository.saveLocation(location)
    }
    
    /**
     * Update location availability
     */
    suspend fun updateAvailability(locationId: String, availability: String): Boolean {
        return repository.updateLocationAvailability(locationId, availability)
    }
    
    /**
     * Update location rating
     */
    suspend fun updateRating(locationId: String, rating: Float, totalRatings: Int): Boolean {
        return repository.updateLocationRating(locationId, rating, totalRatings)
    }
    
    /**
     * Get location statistics
     */
    suspend fun getStats() = repository.getLocationStats()
    
    /**
     * Refresh data from network
     */
    suspend fun refreshData(): Boolean {
        return try {
            repository.refreshLocationsFromNetwork()
            true
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Clear cache
     */
    suspend fun clearCache(): Boolean {
        return repository.clearCache()
    }
}