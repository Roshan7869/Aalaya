package com.aalay.app.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.catch
import javax.inject.Inject
import javax.inject.Singleton
import com.aalay.app.data.local.dao.LocationDao
import com.aalay.app.data.local.entities.LocationEntity
import com.aalay.app.data.remote.BhilaiLocationApiService
import com.aalay.app.utils.MapUtils
import timber.log.Timber

/**
 * Repository for Bhilai location data
 * Coordinates between local database and remote API
 */
@Singleton
class BhilaiLocationRepository @Inject constructor(
    private val locationDao: LocationDao,
    private val apiService: BhilaiLocationApiService
) {
    
    /**
     * Get all locations with network refresh
     */
    fun getAllLocations(forceRefresh: Boolean = false): Flow<List<LocationEntity>> = flow {
        // Emit cached data first
        val cachedData = locationDao.getAllLocations()
        emit(cachedData.map { it })
        
        // Fetch fresh data if needed
        if (forceRefresh || shouldRefreshData()) {
            try {
                refreshLocationsFromNetwork()
            } catch (e: Exception) {
                Timber.e(e, "Failed to refresh locations from network")
            }
        }
    }.catch { e ->
        Timber.e(e, "Error in getAllLocations")
        // Fallback to cached data
        emit(locationDao.getAllLocations().map { it })
    }
    
    /**
     * Get locations by type (room/mess)
     */
    fun getLocationsByType(type: String, forceRefresh: Boolean = false): Flow<List<LocationEntity>> = flow {
        // Emit cached data
        emit(locationDao.getLocationsByType(type).map { it })
        
        if (forceRefresh) {
            try {
                refreshLocationsByTypeFromNetwork(type)
            } catch (e: Exception) {
                Timber.e(e, "Failed to refresh $type locations")
            }
        }
    }
    
    /**
     * Get filtered locations
     */
    fun getFilteredLocations(
        type: String,
        verifiedOnly: Boolean = false,
        availableOnly: Boolean = true,
        maxPrice: Int? = null,
        minRating: Float? = null
    ): Flow<List<LocationEntity>> {
        return locationDao.getFilteredLocations(type, verifiedOnly, availableOnly, maxPrice, minRating)
    }
    
    /**
     * Search locations
     */
    fun searchLocations(query: String): Flow<List<LocationEntity>> {
        return locationDao.searchLocations(query)
    }
    
    /**
     * Get nearby locations
     */
    fun getNearbyLocations(
        latitude: Double = MapUtils.BHILAI_CENTER_LAT,
        longitude: Double = MapUtils.BHILAI_CENTER_LNG,
        radiusKm: Double = 5.0
    ): Flow<List<LocationEntity>> = flow {
        // Convert radius to approximate lat/lng range
        val latRange = radiusKm / 111.0  // 1 degree ≈ 111 km
        val lngRange = radiusKm / (111.0 * kotlin.math.cos(Math.toRadians(latitude)))
        
        emit(locationDao.getNearbyLocations(latitude, longitude, latRange, lngRange).map { 
            it.filter { location -> 
                MapUtils.calculateDistance(latitude, longitude, location.latitude, location.longitude) <= radiusKm
            }
        })
        
        // Try to refresh from network
        try {
            refreshNearbyLocationsFromNetwork(latitude, longitude, radiusKm)
        } catch (e: Exception) {
            Timber.e(e, "Failed to refresh nearby locations")
        }
    }
    
    /**
     * Get featured locations
     */
    fun getFeaturedLocations(type: String? = null, limit: Int = 10): Flow<List<LocationEntity>> {
        return if (type.isNullOrBlank()) {
            locationDao.getFeaturedLocations(limit)
        } else {
            locationDao.getFeaturedLocations(limit).map { locations ->
                locations.filter { it.type == type }
            }
        }
    }
    
    /**
     * Get top-rated locations
     */
    fun getTopRatedLocations(
        type: String? = null,
        minRating: Float = 4.0f,
        limit: Int = 10
    ): Flow<List<LocationEntity>> {
        return locationDao.getLocationsByMinRating(minRating).map { locations ->
            val filtered = if (type.isNullOrBlank()) locations else locations.filter { it.type == type }
            filtered.take(limit)
        }
    }
    
    /**
     * Get recent locations
     */
    fun getRecentLocations(type: String? = null, days: Int = 7, limit: Int = 10): Flow<List<LocationEntity>> {
        val since = System.currentTimeMillis() - (days * 24 * 60 * 60 * 1000L)
        return locationDao.getRecentLocations(since, limit).map { locations ->
            if (type.isNullOrBlank()) locations else locations.filter { it.type == type }
        }
    }
    
    /**
     * Get location by ID
     */
    suspend fun getLocationById(locationId: String): LocationEntity? {
        return locationDao.getLocationById(locationId) ?: run {
            // Try to fetch from network if not in cache
            try {
                val response = apiService.getLocationById(locationId)
                if (response.isSuccessful) {
                    response.body()?.let { location ->
                        locationDao.insertLocation(location)
                        location
                    }
                } else {
                    null
                }
            } catch (e: Exception) {
                Timber.e(e, "Failed to fetch location $locationId from network")
                null
            }
        }
    }
    
    /**
     * Refresh all locations from network
     */
    suspend fun refreshLocationsFromNetwork() {
        try {
            val response = apiService.getAllLocations()
            if (response.isSuccessful) {
                response.body()?.let { locations ->
                    // Validate coordinates before saving
                    val validLocations = locations.filter { 
                        MapUtils.isWithinBhilaiBounds(it.latitude, it.longitude)
                    }
                    
                    if (validLocations.isNotEmpty()) {
                        locationDao.insertLocations(validLocations)
                        Timber.i("Refreshed ${validLocations.size} locations from network")
                    }
                }
            } else {
                Timber.w("Failed to refresh locations: ${response.code()}")
            }
        } catch (e: Exception) {
            Timber.e(e, "Network error while refreshing locations")
            throw e
        }
    }
    
    /**
     * Refresh locations by type from network
     */
    private suspend fun refreshLocationsByTypeFromNetwork(type: String) {
        try {
            val response = apiService.getLocationsByType(type)
            if (response.isSuccessful) {
                response.body()?.let { locations ->
                    val validLocations = locations.filter { 
                        MapUtils.isWithinBhilaiBounds(it.latitude, it.longitude)
                    }
                    locationDao.insertLocations(validLocations)
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to refresh $type locations from network")
        }
    }
    
    /**
     * Refresh nearby locations from network
     */
    private suspend fun refreshNearbyLocationsFromNetwork(
        latitude: Double,
        longitude: Double,
        radiusKm: Double
    ) {
        try {
            val response = apiService.getNearbyLocations(latitude, longitude, radiusKm)
            if (response.isSuccessful) {
                response.body()?.let { locations ->
                    val validLocations = locations.filter { 
                        MapUtils.isWithinBhilaiBounds(it.latitude, it.longitude)
                    }
                    locationDao.insertLocations(validLocations)
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to refresh nearby locations from network")
        }
    }
    
    /**
     * Add or update location
     */
    suspend fun saveLocation(location: LocationEntity): Boolean {
        return try {
            if (!MapUtils.isWithinBhilaiBounds(location.latitude, location.longitude)) {
                Timber.w("Attempting to save location outside Bhilai bounds: ${location.name}")
                return false
            }
            
            locationDao.insertLocation(location)
            true
        } catch (e: Exception) {
            Timber.e(e, "Failed to save location: ${location.name}")
            false
        }
    }
    
    /**
     * Update location availability
     */
    suspend fun updateLocationAvailability(locationId: String, availability: String): Boolean {
        return try {
            locationDao.updateLocationAvailability(locationId, availability)
            true
        } catch (e: Exception) {
            Timber.e(e, "Failed to update availability for location $locationId")
            false
        }
    }
    
    /**
     * Update location rating
     */
    suspend fun updateLocationRating(locationId: String, rating: Float, totalRatings: Int): Boolean {
        return try {
            locationDao.updateLocationRating(locationId, rating, totalRatings)
            true
        } catch (e: Exception) {
            Timber.e(e, "Failed to update rating for location $locationId")
            false
        }
    }
    
    /**
     * Get location statistics
     */
    suspend fun getLocationStats(): LocationStats {
        return try {
            val totalCount = locationDao.getLocationCount()
            val roomCount = locationDao.getLocationCountByType("room")
            val messCount = locationDao.getLocationCountByType("mess")
            
            LocationStats(
                totalLocations = totalCount,
                roomCount = roomCount,
                messCount = messCount
            )
        } catch (e: Exception) {
            Timber.e(e, "Failed to get location stats")
            LocationStats()
        }
    }
    
    /**
     * Check if data should be refreshed (simple time-based logic)
     */
    private fun shouldRefreshData(): Boolean {
        // For simplicity, always allow refresh in this implementation
        // In production, you might want to implement more sophisticated logic
        return true
    }
    
    /**
     * Clear all cached locations
     */
    suspend fun clearCache(): Boolean {
        return try {
            locationDao.clearAllLocations()
            true
        } catch (e: Exception) {
            Timber.e(e, "Failed to clear location cache")
            false
        }
    }
}

/**
 * Data class for location statistics
 */
data class LocationStats(
    val totalLocations: Int = 0,
    val roomCount: Int = 0,
    val messCount: Int = 0
) {
    val hasData: Boolean get() = totalLocations > 0
}