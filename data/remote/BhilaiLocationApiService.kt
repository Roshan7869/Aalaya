package com.aalay.app.data.remote

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import com.aalay.app.data.local.entities.LocationEntity

/**
 * Retrofit API service for Bhilai location operations
 * Handles fetching room and mess data from the backend
 */
interface BhilaiLocationApiService {
    
    /**
     * Get all Bhilai locations
     */
    @GET("locations")
    suspend fun getAllLocations(): Response<List<LocationEntity>>
    
    /**
     * Get locations by type (room/mess)
     */
    @GET("locations")
    suspend fun getLocationsByType(
        @Query("type") type: String
    ): Response<List<LocationEntity>>
    
    /**
     * Get locations with filters
     */
    @GET("locations")
    suspend fun getFilteredLocations(
        @Query("type") type: String? = null,
        @Query("verified") verified: Boolean? = null,
        @Query("min_rating") minRating: Float? = null,
        @Query("max_price") maxPrice: Int? = null,
        @Query("amenities") amenities: String? = null
    ): Response<List<LocationEntity>>
    
    /**
     * Get location by ID
     */
    @GET("locations/{id}")
    suspend fun getLocationById(
        @Path("id") locationId: String
    ): Response<LocationEntity>
    
    /**
     * Search locations
     */
    @GET("locations/search")
    suspend fun searchLocations(
        @Query("q") searchQuery: String,
        @Query("type") type: String? = null
    ): Response<List<LocationEntity>>
    
    /**
     * Get nearby locations (within radius)
     */
    @GET("locations/nearby")
    suspend fun getNearbyLocations(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("radius") radiusKm: Double = 5.0,
    ): Response<List<LocationEntity>>
    
    /**
     * Get featured/promoted locations
     */
    @GET("locations/featured")
    suspend fun getFeaturedLocations(
        @Query("type") type: String? = null,
        @Query("limit") limit: Int = 10
    ): Response<List<LocationEntity>>
    
    /**
     * Get top-rated locations
     */
    @GET("locations/top-rated")
    suspend fun getTopRatedLocations(
        @Query("type") type: String? = null,
        @Query("min_rating") minRating: Float = 4.0f,
        @Query("limit") limit: Int = 10
    ): Response<List<LocationEntity>>
    
    /**
     * Get recently added locations
     */
    @GET("locations/recent")
    suspend fun getRecentLocations(
        @Query("type") type: String? = null,
        @Query("days") days: Int = 7,
        @Query("limit") limit: Int = 10
    ): Response<List<LocationEntity>>
}

/**
 * API response wrapper for better error handling
 */
data class ApiResponse<T>(
    val success: Boolean,
    val data: T?,
    val message: String?,
    val error: String?
)

/**
 * Location filter parameters for API requests
 */
data class LocationFilters(
    val type: String? = null, // "room" or "mess"
    val verified: Boolean? = null,
    val minRating: Float? = null,
    val maxPrice: Int? = null,
    val amenities: List<String>? = null,
    val nearLatitude: Double? = null,
    val nearLongitude: Double? = null,
    val radiusKm: Double = 5.0
)