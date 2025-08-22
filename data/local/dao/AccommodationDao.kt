package com.aalay.app.data.local.dao

import androidx.room.*
import androidx.room.OnConflictStrategy
import com.aalay.app.data.local.entities.AccommodationEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO for accommodation-related database operations
 * Optimized for student-focused queries and offline access
 */
@Dao
interface AccommodationDao {
    
    // Basic CRUD operations
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAccommodation(accommodation: AccommodationEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAccommodations(accommodations: List<AccommodationEntity>)
    
    @Update
    suspend fun updateAccommodation(accommodation: AccommodationEntity)
    
    @Delete
    suspend fun deleteAccommodation(accommodation: AccommodationEntity)
    
    @Query("DELETE FROM accommodations WHERE id = :accommodationId")
    suspend fun deleteAccommodationById(accommodationId: String)
    
    // Basic retrieval
    @Query("SELECT * FROM accommodations WHERE id = :id")
    suspend fun getAccommodationById(id: String): AccommodationEntity?
    
    @Query("SELECT * FROM accommodations ORDER BY lastUpdated DESC")
    fun getAllAccommodations(): Flow<List<AccommodationEntity>>
    
    // Student-specific queries
    @Query("""
        SELECT * FROM accommodations 
        WHERE isStudentFriendly = 1 
        AND isAvailable = 1 
        ORDER BY averageRating DESC, studentReviews DESC
    """)
    fun getStudentFriendlyAccommodations(): Flow<List<AccommodationEntity>>
    
    // Location-based queries using lat/long
    @Query("""
        SELECT *, 
        (6371 * acos(cos(radians(:userLat)) * cos(radians(latitude)) * 
        cos(radians(longitude) - radians(:userLng)) + sin(radians(:userLat)) * 
        sin(radians(latitude)))) AS distance 
        FROM accommodations 
        WHERE isAvailable = 1
        HAVING distance <= :radiusKm 
        ORDER BY distance ASC
    """)
    suspend fun getAccommodationsNearLocation(
        userLat: Double, 
        userLng: Double, 
        radiusKm: Double
    ): List<AccommodationEntity>
    
    // Budget-based filtering
    @Query("""
        SELECT * FROM accommodations 
        WHERE monthlyRent BETWEEN :minBudget AND :maxBudget 
        AND isAvailable = 1 
        ORDER BY monthlyRent ASC
    """)
    fun getAccommodationsByBudget(minBudget: Double, maxBudget: Double): Flow<List<AccommodationEntity>>
    
    // Accommodation type filtering
    @Query("""
        SELECT * FROM accommodations 
        WHERE accommodationType IN (:types) 
        AND isAvailable = 1 
        ORDER BY averageRating DESC
    """)
    fun getAccommodationsByType(types: List<String>): Flow<List<AccommodationEntity>>
    
    // Combined student-optimized search
    @Query("""
        SELECT *, 
        (6371 * acos(cos(radians(:userLat)) * cos(radians(latitude)) * 
        cos(radians(longitude) - radians(:userLng)) + sin(radians(:userLat)) * 
        sin(radians(latitude)))) AS distance 
        FROM accommodations 
        WHERE monthlyRent BETWEEN :minBudget AND :maxBudget 
        AND accommodationType IN (:types)
        AND isAvailable = 1
        AND isStudentFriendly = 1
        AND (:genderPreference IS NULL OR genderPreference = :genderPreference OR genderPreference = 'Any')
        AND (:requiresWifi = 0 OR hasWifi = 1)
        AND (:requiresStudyDesk = 0 OR hasStudyDesk = 1)
        AND (:requiresMessFood = 0 OR hasMessFood = 1)
        HAVING distance <= :maxDistance
        ORDER BY 
            CASE WHEN :sortBy = 'distance' THEN distance END ASC,
            CASE WHEN :sortBy = 'price' THEN monthlyRent END ASC,
            CASE WHEN :sortBy = 'rating' THEN averageRating END DESC,
            distance ASC
    """)
    suspend fun searchStudentAccommodations(
        userLat: Double,
        userLng: Double,
        minBudget: Double,
        maxBudget: Double,
        types: List<String>,
        genderPreference: String?,
        requiresWifi: Boolean,
        requiresStudyDesk: Boolean,
        requiresMessFood: Boolean,
        maxDistance: Double,
        sortBy: String // 'distance', 'price', 'rating'
    ): List<AccommodationEntity>
    
    // Bookmarked accommodations
    @Query("SELECT * FROM accommodations WHERE isBookmarked = 1 ORDER BY lastUpdated DESC")
    fun getBookmarkedAccommodations(): Flow<List<AccommodationEntity>>
    
    @Query("UPDATE accommodations SET isBookmarked = :isBookmarked WHERE id = :accommodationId")
    suspend fun updateBookmarkStatus(accommodationId: String, isBookmarked: Boolean)
    
    // Recently viewed (based on cache time)
    @Query("""
        SELECT * FROM accommodations 
        WHERE cachedAt > :sinceTimestamp 
        ORDER BY cachedAt DESC 
        LIMIT :limit
    """)
    suspend fun getRecentlyViewed(sinceTimestamp: Long, limit: Int = 10): List<AccommodationEntity>
    
    // College proximity queries
    @Query("""
        SELECT * FROM accommodations 
        WHERE nearestCollegeName LIKE '%' || :collegeName || '%' 
        AND distanceToCollege <= :maxDistance 
        AND isAvailable = 1
        ORDER BY distanceToCollege ASC
    """)
    fun getAccommodationsNearCollege(
        collegeName: String, 
        maxDistance: Double
    ): Flow<List<AccommodationEntity>>
    
    // Amenity-based filtering
    @Query("""
        SELECT * FROM accommodations 
        WHERE isAvailable = 1
        AND (:hasWifi = 0 OR hasWifi = 1)
        AND (:hasStudyDesk = 0 OR hasStudyDesk = 1)
        AND (:hasLaundry = 0 OR hasLaundry = 1)
        AND (:hasMessFood = 0 OR hasMessFood = 1)
        AND (:hasGym = 0 OR hasGym = 1)
        AND (:hasParking = 0 OR hasParking = 1)
        ORDER BY averageRating DESC
    """)
    fun getAccommodationsByAmenities(
        hasWifi: Boolean,
        hasStudyDesk: Boolean,
        hasLaundry: Boolean,
        hasMessFood: Boolean,
        hasGym: Boolean,
        hasParking: Boolean
    ): Flow<List<AccommodationEntity>>
    
    // Cache management
    @Query("DELETE FROM accommodations WHERE cachedAt < :expireTime AND isBookmarked = 0")
    suspend fun clearExpiredCache(expireTime: Long)
    
    @Query("SELECT COUNT(*) FROM accommodations")
    suspend fun getCacheSize(): Int
    
    @Query("DELETE FROM accommodations WHERE isBookmarked = 0")
    suspend fun clearAllNonBookmarkedCache()
    
    // Statistics for analytics
    @Query("SELECT COUNT(*) FROM accommodations WHERE isStudentFriendly = 1")
    suspend fun getStudentFriendlyCount(): Int
    
    @Query("SELECT AVG(monthlyRent) FROM accommodations WHERE isAvailable = 1 AND accommodationType = :type")
    suspend fun getAverageRentByType(type: String): Double?
}