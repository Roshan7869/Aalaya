package com.aalay.app.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity for caching traffic-aware route data from Mapbox
 * Reduces API calls and provides offline access to recent route queries
 */
@Entity(tableName = "traffic_cache")
data class TrafficCacheEntity(
    @PrimaryKey
    val routeId: String, // Composite: "${fromLat}_${fromLng}_${toLat}_${toLng}_${profile}"
    
    // Route coordinates
    val fromLatitude: Double,
    val fromLongitude: Double,
    val toLatitude: Double,
    val toLongitude: Double,
    val profile: String, // "driving-traffic", "walking", "cycling"
    
    // Route details from Mapbox Directions API
    val duration: Double, // Duration in seconds
    val distance: Double, // Distance in meters
    val geometry: String, // Encoded polyline geometry
    
    // Traffic-aware data
    val durationInTraffic: Double, // Duration considering current traffic
    val trafficCongestionLevel: String, // "low", "moderate", "heavy", "severe"
    val alternativeRoutesCount: Int,
    
    // Route instructions (simplified)
    val routeInstructions: List<String>, // Step-by-step directions
    val waypoints: List<String>, // Key waypoints along the route
    
    // Contextual information for students
    val passesNearCollege: Boolean,
    val passesNearPublicTransport: Boolean,
    val tollsRequired: Boolean,
    val estimatedFuelCost: Double?, // For driving routes
    
    // Time-based variations (helpful for students planning commutes)
    val peakHourDuration: Double?, // Duration during peak hours
    val offPeakDuration: Double?, // Duration during off-peak hours
    val weekendDuration: Double?, // Duration during weekends
    
    // Cache metadata
    val cachedAt: Long, // When this route was cached
    val expiresAt: Long, // When this cache entry expires
    val hitCount: Int, // Number of times this route was accessed
    val lastAccessedAt: Long, // Last time this route was used
    
    // Query context
    val userId: String?, // User who made the query (for personalization)
    val queryType: String, // "accommodation_search", "commute_planning", "one_time"
    val accommodationId: String?, // If route is to a specific accommodation
    
    // Additional metadata for analytics
    val deviceLocation: String?, // General area where query was made
    val timeOfQuery: String, // Hour of day when query was made
    val dayOfWeek: Int, // 1-7, useful for traffic patterns
    val isWeekend: Boolean
) {
    companion object {
        // Cache expiry times
        const val TRAFFIC_CACHE_DURATION_MINUTES = 30 // Traffic data expires in 30 minutes
        const val STATIC_ROUTE_CACHE_HOURS = 24 // Non-traffic route data expires in 24 hours
        const val MAX_HIT_COUNT_FOR_PROMOTION = 5 // Promote frequently used routes
        
        /**
         * Generate route ID from coordinates and profile
         */
        fun generateRouteId(
            fromLat: Double,
            fromLng: Double,
            toLat: Double,
            toLng: Double,
            profile: String
        ): String {
            return "${String.format("%.4f", fromLat)}_${String.format("%.4f", fromLng)}_" +
                   "${String.format("%.4f", toLat)}_${String.format("%.4f", toLng)}_$profile"
        }
        
        /**
         * Check if cache entry is still valid
         */
        fun isValidCache(entity: TrafficCacheEntity, currentTime: Long): Boolean {
            return entity.expiresAt > currentTime
        }
        
        /**
         * Determine if route should be prioritized for students
         */
        fun isStudentFriendlyRoute(entity: TrafficCacheEntity): Boolean {
            return entity.passesNearCollege || 
                   entity.passesNearPublicTransport ||
                   entity.profile == "walking" ||
                   !entity.tollsRequired
        }
    }
}