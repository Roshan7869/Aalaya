package com.aalay.app.data.remote

import com.aalay.app.data.models.DirectionsResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Retrofit API service for Mapbox Directions API
 * Provides traffic-aware routing for student accommodations
 * Profile: driving-traffic, walking, cycling
 */
interface MapboxDirectionsService {
    
    /**
     * Get directions between two points with traffic consideration
     * @param profile Routing profile: "driving-traffic", "walking", "cycling"
     * @param coordinates Semicolon-separated longitude,latitude pairs (start;end)
     * @param accessToken Mapbox API access token
     * @param alternatives Whether to return alternative routes
     * @param geometries Format for route geometry: "geojson" or "polyline"
     * @param overview Level of detail: "full", "simplified", "false"
     * @param steps Whether to return turn-by-turn instructions
     * @param congestionAnnotations Include traffic congestion data
     * @param durationAnnotations Include duration annotations
     */
    @GET("directions/v5/mapbox/{profile}/{coordinates}")
    suspend fun getDirections(
        @Path("profile") profile: String,
        @Path("coordinates") coordinates: String, // "lng,lat;lng,lat"
        @Query("access_token") accessToken: String,
        @Query("alternatives") alternatives: Boolean = true,
        @Query("geometries") geometries: String = "geojson",
        @Query("overview") overview: String = "full",
        @Query("steps") steps: Boolean = true,
        @Query("annotations") annotations: String = "congestion,duration",
        @Query("exclude") exclude: String? = null, // "toll" for students
        @Query("language") language: String = "en"
    ): Response<DirectionsResponse>
    
    /**
     * Get traffic-optimized driving directions specifically for students
     * Excludes tolls by default and optimizes for cost-effectiveness
     */
    @GET("directions/v5/mapbox/driving-traffic/{coordinates}")
    suspend fun getStudentDrivingDirections(
        @Path("coordinates") coordinates: String,
        @Query("access_token") accessToken: String,
        @Query("alternatives") alternatives: Boolean = true,
        @Query("exclude") exclude: String = "toll", // Exclude tolls for budget-conscious students
        @Query("geometries") geometries: String = "geojson",
        @Query("overview") overview: String = "simplified",
        @Query("steps") steps: Boolean = false, // Students typically use Google Maps for navigation
        @Query("annotations") annotations: String = "congestion,duration",
        @Query("approaches") approaches: String? = null // "curb" for pickup/drop locations
    ): Response<DirectionsResponse>
    
    /**
     * Get walking directions optimized for students
     * Includes sidewalk preference and campus-friendly routes
     */
    @GET("directions/v5/mapbox/walking/{coordinates}")
    suspend fun getWalkingDirections(
        @Path("coordinates") coordinates: String,
        @Query("access_token") accessToken: String,
        @Query("alternatives") alternatives: Boolean = true,
        @Query("geometries") geometries: String = "geojson",
        @Query("overview") overview: String = "full",
        @Query("steps") steps: Boolean = true,
        @Query("walkway_bias") walkwayBias: Double = 0.2, // Prefer pedestrian-friendly routes
        @Query("walking_speed") walkingSpeed: Double = 1.4, // Average student walking speed (m/s)
        @Query("exclude") exclude: String? = null
    ): Response<DirectionsResponse>
    
    /**
     * Get cycling directions for students
     * Optimized for safety and bike-friendly routes
     */
    @GET("directions/v5/mapbox/cycling/{coordinates}")
    suspend fun getCyclingDirections(
        @Path("coordinates") coordinates: String,
        @Query("access_token") accessToken: String,
        @Query("alternatives") alternatives: Boolean = true,
        @Query("geometries") geometries: String = "geojson",
        @Query("overview") overview: String = "full",
        @Query("steps") steps: Boolean = true,
        @Query("annotations") annotations: String = "duration",
        @Query("exclude") exclude: String? = null
    ): Response<DirectionsResponse>
    
    /**
     * Get multiple route profiles in a single request for comparison
     * Useful for showing students different commute options
     */
    @GET("directions/v5/mapbox/driving-traffic/{coordinates}")
    suspend fun getMultiProfileDirections(
        @Path("coordinates") coordinates: String,
        @Query("access_token") accessToken: String,
        @Query("alternatives") alternatives: Boolean = true,
        @Query("geometries") geometries: String = "geojson",
        @Query("overview") overview: String = "simplified",
        @Query("steps") steps: Boolean = false,
        @Query("annotations") annotations: String = "congestion,duration,distance",
        @Query("exclude") exclude: String = "toll",
        @Query("approaches") approaches: String? = null
    ): Response<DirectionsResponse>
    
    /**
     * Get matrix of distances/durations between multiple points
     * Useful for comparing multiple accommodations from user's location
     * @param coordinates Up to 25 coordinate pairs
     * @param sources Indices of source coordinates (0-based)
     * @param destinations Indices of destination coordinates (0-based)
     */
    @GET("directions-matrix/v1/mapbox/{profile}/{coordinates}")
    suspend fun getDistanceMatrix(
        @Path("profile") profile: String,
        @Path("coordinates") coordinates: String,
        @Query("access_token") accessToken: String,
        @Query("sources") sources: String? = null, // "0" for single source
        @Query("destinations") destinations: String? = null, // "all" or specific indices
        @Query("annotations") annotations: String = "duration,distance",
        @Query("exclude") exclude: String? = null
    ): Response<MatrixResponse>
    
    /**
     * Get isochrone (travel time area) for student location analysis
     * Shows areas reachable within specific time limits
     */
    @GET("isochrone/v1/mapbox/{profile}/{coordinates}")
    suspend fun getIsochrone(
        @Path("profile") profile: String,
        @Path("coordinates") coordinates: String, // Single point "lng,lat"
        @Query("access_token") accessToken: String,
        @Query("contours_minutes") contoursMinutes: String = "15,30,45", // 15, 30, 45 minute zones
        @Query("contours_colors") contoursColors: String = "6706ce,04e813,4286f4",
        @Query("polygons") polygons: Boolean = true,
        @Query("exclude") exclude: String? = "toll"
    ): Response<IsochroneResponse>
    
    /**
     * Get route optimization for multiple stops
     * Useful for students planning visits to multiple accommodations
     */
    @GET("optimized-trips/v1/mapbox/{profile}/{coordinates}")
    suspend fun getOptimizedRoute(
        @Path("profile") profile: String,
        @Path("coordinates") coordinates: String, // Multiple points
        @Query("access_token") accessToken: String,
        @Query("roundtrip") roundtrip: Boolean = true,
        @Query("source") source: String = "first", // Start from first coordinate
        @Query("destination") destination: String = "last", // End at last coordinate
        @Query("geometries") geometries: String = "geojson",
        @Query("overview") overview: String = "simplified",
        @Query("steps") steps: Boolean = false,
        @Query("annotations") annotations: String = "duration,distance"
    ): Response<OptimizedTripResponse>
}

// ============ RESPONSE DATA CLASSES ============

/**
 * Matrix API response for bulk distance/duration calculations
 */
data class MatrixResponse(
    val code: String,
    val durations: List<List<Double?>>?,
    val distances: List<List<Double?>>?,
    val sources: List<MatrixWaypoint>,
    val destinations: List<MatrixWaypoint>
)

data class MatrixWaypoint(
    val distance: Double,
    val name: String,
    val location: List<Double>
)

/**
 * Isochrone API response for travel time polygons
 */
data class IsochroneResponse(
    val type: String,
    val features: List<IsochroneFeature>
)

data class IsochroneFeature(
    val type: String,
    val properties: IsochroneProperties,
    val geometry: IsochroneGeometry
)

data class IsochroneProperties(
    val fillColor: String,
    val fill: String,
    val fillOpacity: Double,
    val color: String,
    val contour: Int
)

data class IsochroneGeometry(
    val type: String,
    val coordinates: List<List<List<Double>>>
)

/**
 * Optimization API response for multi-stop routes
 */
data class OptimizedTripResponse(
    val code: String,
    val waypoints: List<OptimizedWaypoint>,
    val trips: List<OptimizedTrip>
)

data class OptimizedWaypoint(
    val distance: Double,
    val name: String,
    val location: List<Double>,
    val waypointIndex: Int,
    val tripsIndex: Int
)

data class OptimizedTrip(
    val geometry: Any, // GeoJSON or encoded polyline
    val legs: List<RouteLeg>,
    val weightName: String,
    val weight: Double,
    val duration: Double,
    val distance: Double
)

data class RouteLeg(
    val distance: Double,
    val duration: Double,
    val summary: String,
    val steps: List<RouteStep>?
)

data class RouteStep(
    val distance: Double,
    val duration: Double,
    val geometry: Any,
    val name: String,
    val mode: String,
    val maneuver: StepManeuver,
    val voiceInstructions: List<VoiceInstruction>?,
    val bannerInstructions: List<BannerInstruction>?
)

data class StepManeuver(
    val type: String,
    val instruction: String,
    val bearingAfter: Double?,
    val bearingBefore: Double?,
    val location: List<Double>,
    val modifier: String?
)

data class VoiceInstruction(
    val distanceAlongGeometry: Double,
    val announcement: String,
    val ssmlAnnouncement: String?
)

data class BannerInstruction(
    val distanceAlongGeometry: Double,
    val primary: InstructionComponent,
    val secondary: InstructionComponent?
)

data class InstructionComponent(
    val text: String,
    val components: List<InstructionTextComponent>?,
    val type: String,
    val modifier: String?
)

data class InstructionTextComponent(
    val text: String,
    val type: String
)