package com.aalay.app.data.models

import com.google.gson.annotations.SerializedName

/**
 * Route model representing a single route from directions API
 * Contains detailed route information including traffic-aware duration
 */
data class Route(
    @SerializedName("route_id")
    val routeId: String? = null,
    
    // Route geometry and coordinates
    @SerializedName("geometry")
    val geometry: String? = null, // Polyline encoded string
    
    @SerializedName("coordinates")
    val coordinates: List<LatLong> = emptyList(),
    
    // Distance information
    @SerializedName("distance")
    val distance: Distance,
    
    // Duration information
    @SerializedName("duration")
    val duration: Duration,
    
    @SerializedName("duration_in_traffic")
    val durationInTraffic: Duration? = null,
    
    // Route characteristics
    @SerializedName("route_name")
    val routeName: String? = null,
    
    @SerializedName("route_type")
    val routeType: RouteType = RouteType.DRIVING,
    
    @SerializedName("traffic_condition")
    val trafficCondition: TrafficCondition = TrafficCondition.UNKNOWN,
    
    @SerializedName("congestion_level")
    val congestionLevel: CongestionLevel = CongestionLevel.UNKNOWN,
    
    // Route details
    @SerializedName("steps")
    val steps: List<RouteStep> = emptyList(),
    
    @SerializedName("waypoints")
    val waypoints: List<Waypoint> = emptyList(),
    
    @SerializedName("via_waypoints")
    val viaWaypoints: List<LatLong> = emptyList(),
    
    // Route quality and preferences
    @SerializedName("is_optimal")
    val isOptimal: Boolean = false,
    
    @SerializedName("route_quality")
    val routeQuality: RouteQuality = RouteQuality.UNKNOWN,
    
    @SerializedName("road_types")
    val roadTypes: List<RoadType> = emptyList(),
    
    @SerializedName("toll_info")
    val tollInfo: TollInfo? = null,
    
    // Student-specific route features
    @SerializedName("passes_near_colleges")
    val passesNearColleges: List<String> = emptyList(),
    
    @SerializedName("public_transport_accessible")
    val publicTransportAccessible: Boolean = false,
    
    @SerializedName("safety_rating")
    val safetyRating: Float? = null,
    
    @SerializedName("student_friendly_landmarks")
    val studentFriendlyLandmarks: List<Landmark> = emptyList(),
    
    // Time-based information
    @SerializedName("best_time_to_travel")
    val bestTimeToTravel: List<TimeWindow> = emptyList(),
    
    @SerializedName("rush_hour_impact")
    val rushHourImpact: RushHourImpact? = null,
    
    // Accessibility and convenience
    @SerializedName("wheelchair_accessible")
    val wheelchairAccessible: Boolean? = null,
    
    @SerializedName("bicycle_friendly")
    val bicycleFriendly: Boolean = false,
    
    @SerializedName("walking_safety_score")
    val walkingSafetyScore: Float? = null,
    
    // Additional metadata
    @SerializedName("created_at")
    val createdAt: Long = System.currentTimeMillis(),
    
    @SerializedName("expires_at")
    val expiresAt: Long? = null,
    
    @SerializedName("data_source")
    val dataSource: String? = null,
    
    @SerializedName("last_updated")
    val lastUpdated: Long = System.currentTimeMillis()
) {
    /**
     * Get duration considering traffic if available
     */
    fun getEffectiveDuration(): Duration {
        return durationInTraffic ?: duration
    }
    
    /**
     * Get duration in minutes
     */
    fun getDurationInMinutes(): Int {
        return getEffectiveDuration().seconds / 60
    }
    
    /**
     * Get distance in kilometers
     */
    fun getDistanceInKilometers(): Double {
        return distance.meters / 1000.0
    }
    
    /**
     * Check if route data is still valid
     */
    fun isValid(): Boolean {
        val now = System.currentTimeMillis()
        return expiresAt?.let { it > now } ?: true
    }
    
    /**
     * Get formatted duration string
     */
    fun getFormattedDuration(): String {
        val totalMinutes = getDurationInMinutes()
        val hours = totalMinutes / 60
        val minutes = totalMinutes % 60
        
        return when {
            hours > 0 && minutes > 0 -> "${hours}h ${minutes}m"
            hours > 0 -> "${hours}h"
            else -> "${minutes}m"
        }
    }
    
    /**
     * Get traffic-adjusted duration increase percentage
     */
    fun getTrafficDelayPercentage(): Float? {
        return durationInTraffic?.let { trafficDuration ->
            val increase = (trafficDuration.seconds - duration.seconds).toFloat()
            (increase / duration.seconds) * 100
        }
    }
    
    /**
     * Check if route is suitable for students (safety, accessibility, etc.)
     */
    fun isStudentFriendly(): Boolean {
        return (safetyRating ?: 0f) >= 3.0f && 
               publicTransportAccessible &&
               studentFriendlyLandmarks.isNotEmpty()
    }
}

/**
 * Distance information
 */
data class Distance(
    @SerializedName("meters")
    val meters: Double,
    
    @SerializedName("kilometers")
    val kilometers: Double = meters / 1000.0,
    
    @SerializedName("formatted")
    val formatted: String? = null
)

/**
 * Duration information with traffic awareness
 */
data class Duration(
    @SerializedName("seconds")
    val seconds: Int,
    
    @SerializedName("minutes")
    val minutes: Int = seconds / 60,
    
    @SerializedName("text")
    val text: String? = null,
    
    @SerializedName("in_traffic_text")
    val inTrafficText: String? = null
)

/**
 * Individual step in a route
 */
data class RouteStep(
    @SerializedName("instruction")
    val instruction: String,
    
    @SerializedName("distance")
    val distance: Distance,
    
    @SerializedName("duration")
    val duration: Duration,
    
    @SerializedName("start_location")
    val startLocation: LatLong,
    
    @SerializedName("end_location")
    val endLocation: LatLong,
    
    @SerializedName("travel_mode")
    val travelMode: TravelMode = TravelMode.DRIVING,
    
    @SerializedName("maneuver")
    val maneuver: String? = null,
    
    @SerializedName("road_name")
    val roadName: String? = null
)

/**
 * Waypoint information
 */
data class Waypoint(
    @SerializedName("location")
    val location: LatLong,
    
    @SerializedName("name")
    val name: String? = null,
    
    @SerializedName("type")
    val type: WaypointType = WaypointType.INTERMEDIATE,
    
    @SerializedName("arrival_time")
    val arrivalTime: Long? = null,
    
    @SerializedName("departure_time")
    val departureTime: Long? = null
)

/**
 * Toll information
 */
data class TollInfo(
    @SerializedName("has_tolls")
    val hasTolls: Boolean = false,
    
    @SerializedName("toll_amount")
    val tollAmount: Double? = null,
    
    @SerializedName("currency")
    val currency: String = "INR",
    
    @SerializedName("toll_gates")
    val tollGates: List<LatLong> = emptyList()
)

/**
 * Landmark information
 */
data class Landmark(
    @SerializedName("name")
    val name: String,
    
    @SerializedName("location")
    val location: LatLong,
    
    @SerializedName("type")
    val type: LandmarkType,
    
    @SerializedName("distance_from_route_meters")
    val distanceFromRouteMeters: Double,
    
    @SerializedName("student_relevant")
    val studentRelevant: Boolean = false
)

/**
 * Time window for optimal travel
 */
data class TimeWindow(
    @SerializedName("start_time")
    val startTime: String, // Format: "HH:mm"
    
    @SerializedName("end_time")
    val endTime: String,
    
    @SerializedName("day_of_week")
    val dayOfWeek: String? = null,
    
    @SerializedName("duration_seconds")
    val durationSeconds: Int,
    
    @SerializedName("traffic_multiplier")
    val trafficMultiplier: Float = 1.0f
)

/**
 * Rush hour impact information
 */
data class RushHourImpact(
    @SerializedName("morning_rush_start")
    val morningRushStart: String, // "HH:mm"
    
    @SerializedName("morning_rush_end")
    val morningRushEnd: String,
    
    @SerializedName("evening_rush_start")
    val eveningRushStart: String,
    
    @SerializedName("evening_rush_end")
    val eveningRushEnd: String,
    
    @SerializedName("delay_percentage")
    val delayPercentage: Float,
    
    @SerializedName("alternative_routes_available")
    val alternativeRoutesAvailable: Boolean = false
)

// Supporting enums
enum class RouteType {
    @SerializedName("driving")
    DRIVING,
    
    @SerializedName("walking")
    WALKING,
    
    @SerializedName("cycling")
    CYCLING,
    
    @SerializedName("public_transport")
    PUBLIC_TRANSPORT,
    
    @SerializedName("mixed")
    MIXED
}

enum class TrafficCondition {
    @SerializedName("free_flow")
    FREE_FLOW,
    
    @SerializedName("light")
    LIGHT,
    
    @SerializedName("moderate")
    MODERATE,
    
    @SerializedName("heavy")
    HEAVY,
    
    @SerializedName("severe")
    SEVERE,
    
    @SerializedName("unknown")
    UNKNOWN
}

enum class CongestionLevel {
    @SerializedName("low")
    LOW,
    
    @SerializedName("medium")
    MEDIUM,
    
    @SerializedName("high")
    HIGH,
    
    @SerializedName("very_high")
    VERY_HIGH,
    
    @SerializedName("unknown")
    UNKNOWN
}

enum class RouteQuality {
    @SerializedName("excellent")
    EXCELLENT,
    
    @SerializedName("good")
    GOOD,
    
    @SerializedName("fair")
    FAIR,
    
    @SerializedName("poor")
    POOR,
    
    @SerializedName("unknown")
    UNKNOWN
}

enum class RoadType {
    @SerializedName("highway")
    HIGHWAY,
    
    @SerializedName("arterial")
    ARTERIAL,
    
    @SerializedName("local")
    LOCAL,
    
    @SerializedName("residential")
    RESIDENTIAL,
    
    @SerializedName("service")
    SERVICE
}

enum class TravelMode {
    @SerializedName("driving")
    DRIVING,
    
    @SerializedName("walking")
    WALKING,
    
    @SerializedName("cycling")
    CYCLING,
    
    @SerializedName("transit")
    TRANSIT
}

enum class WaypointType {
    @SerializedName("start")
    START,
    
    @SerializedName("end")
    END,
    
    @SerializedName("intermediate")
    INTERMEDIATE,
    
    @SerializedName("via")
    VIA
}

enum class LandmarkType {
    @SerializedName("college")
    COLLEGE,
    
    @SerializedName("university")
    UNIVERSITY,
    
    @SerializedName("library")
    LIBRARY,
    
    @SerializedName("hospital")
    HOSPITAL,
    
    @SerializedName("metro_station")
    METRO_STATION,
    
    @SerializedName("bus_stop")
    BUS_STOP,
    
    @SerializedName("restaurant")
    RESTAURANT,
    
    @SerializedName("shopping_center")
    SHOPPING_CENTER,
    
    @SerializedName("atm")
    ATM,
    
    @SerializedName("pharmacy")
    PHARMACY
}