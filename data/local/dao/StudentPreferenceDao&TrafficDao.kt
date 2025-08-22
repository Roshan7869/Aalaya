package com.aalay.app.data.local.dao

import androidx.room.*
import com.aalay.app.data.local.entities.StudentPreferencesEntity
import com.aalay.app.data.local.entities.TrafficCacheEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO for student preferences management
 */
@Dao
interface StudentPreferencesDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdatePreferences(preferences: StudentPreferencesEntity)
    
    @Query("SELECT * FROM student_preferences WHERE userId = :userId")
    suspend fun getPreferences(userId: String): StudentPreferencesEntity?
    
    @Query("SELECT * FROM student_preferences WHERE userId = :userId")
    fun getPreferencesFlow(userId: String): Flow<StudentPreferencesEntity?>
    
    @Update
    suspend fun updatePreferences(preferences: StudentPreferencesEntity)
    
    @Query("DELETE FROM student_preferences WHERE userId = :userId")
    suspend fun deletePreferences(userId: String)
    
    // Update specific preference fields
    @Query("UPDATE student_preferences SET maxBudget = :maxBudget, minBudget = :minBudget WHERE userId = :userId")
    suspend fun updateBudgetRange(userId: String, minBudget: Double, maxBudget: Double)
    
    @Query("UPDATE student_preferences SET collegeName = :collegeName, collegeLatitude = :lat, collegeLongitude = :lng WHERE userId = :userId")
    suspend fun updateCollegeInfo(userId: String, collegeName: String, lat: Double, lng: Double)
    
    @Query("UPDATE student_preferences SET lastSearchLatitude = :lat, lastSearchLongitude = :lng, lastActiveAt = :timestamp WHERE userId = :userId")
    suspend fun updateLastSearchLocation(userId: String, lat: Double, lng: Double, timestamp: Long)
    
    @Query("UPDATE student_preferences SET searchHistory = :searchHistory WHERE userId = :userId")
    suspend fun updateSearchHistory(userId: String, searchHistory: List<String>)
    
    // Analytics queries
    @Query("SELECT COUNT(*) FROM student_preferences WHERE collegeName IS NOT NULL")
    suspend fun getVerifiedStudentCount(): Int
    
    @Query("SELECT AVG(maxBudget) FROM student_preferences")
    suspend fun getAverageBudget(): Double?
}

/**
 * DAO for traffic cache management
 */
@Dao
interface TrafficCacheDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRouteCache(route: TrafficCacheEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRouteCaches(routes: List<TrafficCacheEntity>)
    
    @Query("SELECT * FROM traffic_cache WHERE routeId = :routeId")
    suspend fun getRouteCache(routeId: String): TrafficCacheEntity?
    
    @Query("""
        SELECT * FROM traffic_cache 
        WHERE fromLatitude BETWEEN :fromLat - 0.001 AND :fromLat + 0.001
        AND fromLongitude BETWEEN :fromLng - 0.001 AND :fromLng + 0.001
        AND toLatitude BETWEEN :toLat - 0.001 AND :toLat + 0.001
        AND toLongitude BETWEEN :toLng - 0.001 AND :toLng + 0.001
        AND profile = :profile
        AND expiresAt > :currentTime
        ORDER BY cachedAt DESC
        LIMIT 1
    """)
    suspend fun getNearbyRouteCache(
        fromLat: Double, 
        fromLng: Double, 
        toLat: Double, 
        toLng: Double, 
        profile: String, 
        currentTime: Long
    ): TrafficCacheEntity?
    
    @Update
    suspend fun updateRouteCache(route: TrafficCacheEntity)
    
    @Query("UPDATE traffic_cache SET hitCount = hitCount + 1, lastAccessedAt = :timestamp WHERE routeId = :routeId")
    suspend fun incrementHitCount(routeId: String, timestamp: Long)
    
    @Delete
    suspend fun deleteRouteCache(route: TrafficCacheEntity)
    
    // Cache cleanup
    @Query("DELETE FROM traffic_cache WHERE expiresAt < :currentTime")
    suspend fun deleteExpiredRoutes(currentTime: Long)
    
    @Query("DELETE FROM traffic_cache WHERE hitCount < :minHitCount AND cachedAt < :oldestKeepTime")
    suspend fun deleteUnpopularRoutes(minHitCount: Int, oldestKeepTime: Long)
    
    @Query("""
        DELETE FROM traffic_cache WHERE routeId NOT IN (
            SELECT routeId FROM traffic_cache 
            ORDER BY hitCount DESC, lastAccessedAt DESC 
            LIMIT :maxCacheSize
        )
    """)
    suspend fun limitCacheSize(maxCacheSize: Int)
    
    // Frequently used routes
    @Query("""
        SELECT * FROM traffic_cache 
        WHERE hitCount >= :minHitCount 
        AND userId = :userId
        ORDER BY hitCount DESC, lastAccessedAt DESC
        LIMIT :limit
    """)
    suspend fun getFrequentRoutes(userId: String, minHitCount: Int = 3, limit: Int = 10): List<TrafficCacheEntity>
    
    // Student-specific queries
    @Query("""
        SELECT * FROM traffic_cache 
        WHERE (passesNearCollege = 1 OR passesNearPublicTransport = 1)
        AND userId = :userId
        AND expiresAt > :currentTime
        ORDER BY hitCount DESC
    """)
    suspend fun getStudentFriendlyRoutes(userId: String, currentTime: Long): List<TrafficCacheEntity>
    
    @Query("""
        SELECT * FROM traffic_cache 
        WHERE accommodationId = :accommodationId
        AND expiresAt > :currentTime
        ORDER BY cachedAt DESC
        LIMIT 1
    """)
    suspend fun getRouteToAccommodation(accommodationId: String, currentTime: Long): TrafficCacheEntity?
    
    // Analytics
    @Query("SELECT COUNT(*) FROM traffic_cache")
    suspend fun getCacheSize(): Int
    
    @Query("SELECT AVG(duration) FROM traffic_cache WHERE profile = :profile")
    suspend fun getAverageDurationByProfile(profile: String): Double?
    
    @Query("""
        SELECT * FROM traffic_cache 
        WHERE trafficCongestionLevel IN (:congestionLevels)
        AND expiresAt > :currentTime
        ORDER BY duration ASC
    """)
    suspend fun getRoutesByCongestionLevel(congestionLevels: List<String>, currentTime: Long): List<TrafficCacheEntity>
    
    // Peak hour analytics for students
    @Query("""
        SELECT AVG(peakHourDuration) as avgPeakDuration, 
               AVG(offPeakDuration) as avgOffPeakDuration,
               COUNT(*) as routeCount
        FROM traffic_cache 
        WHERE peakHourDuration IS NOT NULL 
        AND offPeakDuration IS NOT NULL
        AND passesNearCollege = 1
    """)
    suspend fun getCollegeCommuteStats(): TrafficAnalytics?
    
    @Query("DELETE FROM traffic_cache")
    suspend fun clearAllCache()
}

/**
 * Data class for traffic analytics results
 */
data class TrafficAnalytics(
    val avgPeakDuration: Double,
    val avgOffPeakDuration: Double,
    val routeCount: Int
)