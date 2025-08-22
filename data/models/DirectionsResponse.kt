package com.aalay.app.data.models

import com.google.gson.annotations.SerializedName

/**
 * Response model for Mapbox Directions API
 * Contains routes with traffic-aware duration and congestion information
 */
data class DirectionsResponse(
    @SerializedName("code")
    val code: String, // "Ok" for successful requests
    
    @SerializedName("message")
    val message: String? = null,
    
    @SerializedName("routes")
    val routes: List<Route> = emptyList(),
    
    @SerializedName("waypoints")
    val waypoints: List<Waypoint> = emptyList(),
    
    // Request metadata
    @SerializedName("uuid")
    val uuid: String? = null,
    
    @SerializedName("request_timestamp")
    val requestTimestamp: Long = System.currentTimeMillis(),
    
    @SerializedName("response_timestamp")
    val responseTimestamp: Long = System.currentTimeMillis(),
    
    // API specific information
    @SerializedName("api_version")
    val apiVersion: String? = null,
    
    @SerializedName("data_source")
    val dataSource: String = "mapbox",
    
    @SerializedName("rate_limit_remaining")
    val rateLimitRemaining: Int? = null,
    
    @SerializedName("rate_limit_reset")
    val rateLimitReset: Long? = null,
    
    // Request parameters (for debugging/caching)
    @SerializedName("original_request")
    val originalRequest: DirectionsRequest? = null,
    
    // Caching and performance
    @SerializedName("cache_key")
    val cacheKey: String? = null,
    
    @SerializedName("cached_response")
    val cachedResponse: Boolean = false,
    
    @SerializedName("expires_at")
    val expiresAt: Long? = null,
    
    // Student-specific metadata
    @SerializedName("student_optimized")
    val studentOptimized: Boolean = false,
    
    @SerializedName("college_proximity_considered")
    val collegeProximityConsidered: Boolean = false,
    
    @SerializedName("public_transport_integrated")
    val publicTransportIntegrated: Boolean = false
) {
    /**
     * Check if the response is successful
     */
    fun isSuccessful(): Boolean {
        return code.equals("Ok", ignoreCase = true) && routes.isNotEmpty()
    }
    
    /**
     * Get the fastest route (considering traffic)
     */
    fun getFastestRoute(): Route? {
        return routes.minByOrNull { 
            it.durationInTraffic?.seconds ?: it.duration.seconds 
        }
    }
    
    /**
     * Get the shortest distance route
     */
    fun getShortestRoute(): Route? {
        return routes.minByOrNull { it.distance.meters }
    }
    
    /**
     * Get the most optimal route (balanced between time and distance)
     */
    fun getOptimalRoute(): Route? {
        return routes.find { it.isOptimal } ?: getFastestRoute()
    }
    
    /**
     * Get routes suitable for students (considering safety, public transport, etc.)
     */
    fun getStudentFriendlyRoutes(): List<Route> {
        return routes.filter { it.isStudentFriendly() }
    }
    
    /**
     * Get routes with least traffic congestion
     */
    fun getLeastCongestedRoutes(): List<Route> {
        return routes.filter { 
            it.congestionLevel in listOf(
                CongestionLevel.LOW, 
                CongestionLevel.MEDIUM
            ) 
        }.sortedBy { it.congestionLevel.ordinal }
    }
    
    /**
     * Check if response data is still valid
     */
    fun isValid(): Boolean {
        val now = System.currentTimeMillis()
        return expiresAt?.let { it > now } ?: true
    }
    
    /**
     * Get average duration across all routes
     */
    fun getAverageDuration(): Duration? {
        if (routes.isEmpty()) return null
        
        val avgSeconds = routes.map { 
            it.durationInTraffic?.seconds ?: it.duration.seconds 
        }.average().toInt()
        
        return Duration(
            seconds = avgSeconds,
            minutes = avgSeconds / 60
        )
    }
    
    /**
     * Get route recommendations based on student preferences
     */
    fun getStudentRecommendations(): StudentRouteRecommendations {
        val fastest = getFastestRoute()
        val shortest = getShortestRoute()
        val safest = routes.maxByOrNull { it.safetyRating ?: 0f }
        val mostPublicTransport = routes.maxByOrNull { 
            if (it.publicTransportAccessible) 1 else 0 
        }
        val leastTraffic = routes.minByOrNull { 
            it.getTrafficDelayPercentage() ?: 0f 
        }
        
        return StudentRouteRecommendations(
            fastest = fastest,
            shortest = shortest,
            safest = safest,
            mostPublicTransportAccessible = mostPublicTransport,
            leastTraffic = leastTraffic,
            recommended = getOptimalRoute()
        )
    }
}

/**
 * Original request parameters for reference and caching
 */
data class DirectionsRequest(
    @SerializedName("origin")
    val origin: LatLong,
    
    @SerializedName("destination")
    val destination: LatLong,
    
    @SerializedName("waypoints")
    val waypoints: List<LatLong> = emptyList(),
    
    @SerializedName("travel_mode")
    val travelMode: TravelMode = TravelMode.DRIVING,
    
    @SerializedName("route_type")
    val routeType: RouteType = RouteType.DRIVING,
    
    @SerializedName("avoid_tolls")
    val avoidTolls: Boolean = false,
    
    @SerializedName("avoid_highways")
    val avoidHighways: Boolean = false,
    
    @SerializedName("avoid_ferries")
    val avoidFerries: Boolean = false,
    
    @SerializedName("departure_time")
    val departureTime: Long? = null,
    
    @SerializedName("arrival_time")
    val arrivalTime: Long? = null,
    
    @SerializedName("traffic_model")
    val trafficModel: TrafficModel = TrafficModel.BEST_GUESS,
    
    @SerializedName("alternatives")
    val alternatives: Boolean = true,
    
    @SerializedName("max_alternatives")
    val maxAlternatives: Int = 3,
    
    @SerializedName("language")
    val language: String = "en",
    
    @SerializedName("units")
    val units: String = "metric",
    
    // Student-specific parameters
    @SerializedName("prefer_student_areas")
    val preferStudentAreas: Boolean = true,
    
    @SerializedName("include_public_transport")
    val includePublicTransport: Boolean = true,
    
    @SerializedName("safety_priority")
    val safetyPriority: Boolean = true,
    
    @SerializedName("budget_conscious")
    val budgetConscious: Boolean = true,
    
    @SerializedName("college_locations")
    val collegeLocations: List<LatLong> = emptyList(),
    
    @SerializedName("time_of_day_preference")
    val timeOfDayPreference: TimeOfDayPreference = TimeOfDayPreference.ANY_TIME
)

/**
 * Student-specific route recommendations
 */
data class StudentRouteRecommendations(
    @SerializedName("fastest")
    val fastest: Route? = null,
    
    @SerializedName("shortest")
    val shortest: Route? = null,
    
    @SerializedName("safest")
    val safest: Route? = null,
    
    @SerializedName("most_public_transport_accessible")
    val mostPublicTransportAccessible: Route? = null,
    
    @SerializedName("least_traffic")
    val leastTraffic: Route? = null,
    
    @SerializedName("most_budget_friendly")
    val mostBudgetFriendly: Route? = null,
    
    @SerializedName("recommended")
    val recommended: Route? = null,
    
    @SerializedName("recommendation_reason")
    val recommendationReason: String? = null
) {
    /**
     * Get all unique routes from recommendations
     */
    fun getAllRoutes(): List<Route> {
        return listOfNotNull(
            fastest, shortest, safest, mostPublicTransportAccessible, 
            leastTraffic, mostBudgetFriendly, recommended
        ).distinctBy { it.routeId }
    }
    
    /**
     * Get route by recommendation type
     */
    fun getRouteByType(type: RecommendationType): Route? {
        return when (type) {
            RecommendationType.FASTEST -> fastest
            RecommendationType.SHORTEST -> shortest
            RecommendationType.SAFEST -> safest
            RecommendationType.PUBLIC_TRANSPORT -> mostPublicTransportAccessible
            RecommendationType.LEAST_TRAFFIC -> leastTraffic
            RecommendationType.BUDGET_FRIENDLY -> mostBudgetFriendly
            RecommendationType.RECOMMENDED -> recommended
        }
    }
}

/**
 * Traffic model for time estimates
 */
enum class TrafficModel {
    @SerializedName("best_guess")
    BEST_GUESS,
    
    @SerializedName("pessimistic")
    PESSIMISTIC,
    
    @SerializedName("optimistic")
    OPTIMISTIC
}

/**
 * Time of day preference for routing
 */
enum class TimeOfDayPreference {
    @SerializedName("early_morning")
    EARLY_MORNING, // 6 AM - 9 AM
    
    @SerializedName("morning")
    MORNING, // 9 AM - 12 PM
    
    @SerializedName("afternoon")
    AFTERNOON, // 12 PM - 6 PM
    
    @SerializedName("evening")
    EVENING, // 6 PM - 9 PM
    
    @SerializedName("night")
    NIGHT, // 9 PM - 6 AM
    
    @SerializedName("rush_hour")
    RUSH_HOUR, // Peak traffic times
    
    @SerializedName("off_peak")
    OFF_PEAK, // Low traffic times
    
    @SerializedName("any_time")
    ANY_TIME
}

/**
 * Route recommendation types
 */
enum class RecommendationType {
    FASTEST,
    SHORTEST,
    SAFEST,
    PUBLIC_TRANSPORT,
    LEAST_TRAFFIC,
    BUDGET_FRIENDLY,
    RECOMMENDED
}