package com.aalay.app.data.local.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import com.aalay.app.data.local.entities.LocationEntity

/**
 * Data Access Object for Bhilai location operations
 * Provides CRUD operations for LocationEntity
 */
@Dao
interface LocationDao {
    
    /**
     * Get all locations
     */
    @Query("SELECT * FROM bhilai_locations ORDER BY created_at DESC")
    fun getAllLocations(): Flow<List<LocationEntity>>
    
    /**
     * Get locations by type (room/mess)
     */
    @Query("SELECT * FROM bhilai_locations WHERE type = :type ORDER BY rating DESC, created_at DESC")
    fun getLocationsByType(type: String): Flow<List<LocationEntity>>
    
    /**
     * Get location by ID
     */
    @Query("SELECT * FROM bhilai_locations WHERE id = :locationId")
    suspend fun getLocationById(locationId: String): LocationEntity?
    
    /**
     * Get verified locations only
     */
    @Query("SELECT * FROM bhilai_locations WHERE is_verified = 1 ORDER BY rating DESC")
    fun getVerifiedLocations(): Flow<List<LocationEntity>>
    
    /**
     * Get featured locations
     */
    @Query("SELECT * FROM bhilai_locations WHERE is_featured = 1 ORDER BY rating DESC LIMIT :limit")
    fun getFeaturedLocations(limit: Int = 10): Flow<List<LocationEntity>>
    
    /**
     * Get available locations
     */
    @Query("SELECT * FROM bhilai_locations WHERE availability != 'full' ORDER BY rating DESC")
    fun getAvailableLocations(): Flow<List<LocationEntity>>
    
    /**
     * Search locations by name or address
     */
    @Query("""
        SELECT * FROM bhilai_locations 
        WHERE name LIKE '%' || :searchQuery || '%' 
        OR address LIKE '%' || :searchQuery || '%'
        OR description LIKE '%' || :searchQuery || '%'
        ORDER BY rating DESC
    """)
    fun searchLocations(searchQuery: String): Flow<List<LocationEntity>>
    
    /**
     * Get locations with minimum rating
     */
    @Query("""
        SELECT * FROM bhilai_locations 
        WHERE rating >= :minRating AND total_ratings > 0
        ORDER BY rating DESC
    """)
    fun getLocationsByMinRating(minRating: Float): Flow<List<LocationEntity>>
    
    /**
     * Get locations within price range
     */
    @Query("""
        SELECT * FROM bhilai_locations 
        WHERE price_per_month IS NOT NULL 
        AND price_per_month <= :maxPrice
        ORDER BY price_per_month ASC
    """)
    fun getLocationsByMaxPrice(maxPrice: Int): Flow<List<LocationEntity>>
    
    /**
     * Get locations by type with filters
     */
    @Query("""
        SELECT * FROM bhilai_locations 
        WHERE type = :type 
        AND (:verifiedOnly = 0 OR is_verified = 1)
        AND (:availableOnly = 0 OR availability != 'full')
        AND (:maxPrice IS NULL OR price_per_month IS NULL OR price_per_month <= :maxPrice)
        AND (:minRating IS NULL OR rating >= :minRating)
        ORDER BY 
            CASE WHEN is_featured = 1 THEN 0 ELSE 1 END,
            rating DESC, 
            created_at DESC
    """)
    fun getFilteredLocations(
        type: String,
        verifiedOnly: Boolean = false,
        availableOnly: Boolean = true,
        maxPrice: Int? = null,
        minRating: Float? = null
    ): Flow<List<LocationEntity>>
    
    /**
     * Get recent locations (within specified days)
     */
    @Query("""
        SELECT * FROM bhilai_locations 
        WHERE created_at >= :since
        ORDER BY created_at DESC
        LIMIT :limit
    """)
    fun getRecentLocations(since: Long, limit: Int = 20): Flow<List<LocationEntity>>
    
    /**
     * Get locations near a point (simplified distance calculation)
     */
    @Query("""
        SELECT * FROM bhilai_locations 
        WHERE ABS(latitude - :centerLat) <= :latRange 
        AND ABS(longitude - :centerLng) <= :lngRange
        ORDER BY 
            ((latitude - :centerLat) * (latitude - :centerLat) + 
             (longitude - :centerLng) * (longitude - :centerLng)) ASC
    """)
    fun getNearbyLocations(
        centerLat: Double,
        centerLng: Double,
        latRange: Double = 0.05, // ~5km range
        lngRange: Double = 0.05
    ): Flow<List<LocationEntity>>
    
    /**
     * Insert single location
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLocation(location: LocationEntity)
    
    /**
     * Insert multiple locations
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLocations(locations: List<LocationEntity>)
    
    /**
     * Update location
     */
    @Update
    suspend fun updateLocation(location: LocationEntity)
    
    /**
     * Delete location
     */
    @Delete
    suspend fun deleteLocation(location: LocationEntity)
    
    /**
     * Delete location by ID
     */
    @Query("DELETE FROM bhilai_locations WHERE id = :locationId")
    suspend fun deleteLocationById(locationId: String)
    
    /**
     * Clear all locations
     */
    @Query("DELETE FROM bhilai_locations")
    suspend fun clearAllLocations()
    
    /**
     * Get location count
     */
    @Query("SELECT COUNT(*) FROM bhilai_locations")
    suspend fun getLocationCount(): Int
    
    /**
     * Get location count by type
     */
    @Query("SELECT COUNT(*) FROM bhilai_locations WHERE type = :type")
    suspend fun getLocationCountByType(type: String): Int
    
    /**
     * Update location availability
     */
    @Query("UPDATE bhilai_locations SET availability = :availability, updated_at = :updatedAt WHERE id = :locationId")
    suspend fun updateLocationAvailability(locationId: String, availability: String, updatedAt: Long = System.currentTimeMillis())
    
    /**
     * Update location rating
     */
    @Query("""
        UPDATE bhilai_locations 
        SET rating = :rating, total_ratings = :totalRatings, updated_at = :updatedAt 
        WHERE id = :locationId
    """)
    suspend fun updateLocationRating(
        locationId: String, 
        rating: Float, 
        totalRatings: Int, 
        updatedAt: Long = System.currentTimeMillis()
    )
}