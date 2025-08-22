package com.aalay.app.data.repository

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.aalay.app.data.local.dao.TrafficCacheDao
import com.aalay.app.data.local.entity.TrafficCacheEntity
import com.aalay.app.data.remote.MapboxDirectionsService
import com.aalay.app.data.models.*
import com.aalay.app.utils.DistanceCalculator
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DirectionsRepository @Inject constructor(
    private val mapboxService: MapboxDirectionsService,
    private val trafficCacheDao: TrafficCacheDao,
    private val distanceCalculator: DistanceCalculator,
    @ApplicationContext private val context: Context
) {
    
    companion object {
        private const val CACHE_VALIDITY_HOURS = 2 // Traffic data valid for 2 hours
        private const val MAPBOX_ACCESS_TOKEN = "your_mapbox_access_token" // TODO: Move to BuildConfig
    }
    
    /**
     * Get traffic-aware directions to accommodation with student-optimized routing
     */
    suspend fun getDirectionsToAccommodation(
        userLocation: LatLong,
        accommodationLocation: LatLong,
        transportProfile: TransportProfile = TransportProfile.WALKING,
        avoidTolls: Boolean = true, // Student budget-friendly default
        departureTime: Long? = null
    ): Result<DirectionsResponse> {
        return try {
            // Check cache first
            val cacheKey = generateCacheKey(userLocation, accommodationLocation, transportProfile)
            val cachedResult = getCachedDirections(cacheKey)
            
            if (cachedResult != null && isCacheValid(cachedResult.cachedAt)) {
                // Increment hit count for analytics
                trafficCacheDao.incrementHitCount(cacheKey)
                return Result.success(cachedResult.toDirectionsResponse())
            }
            
            // Fetch from Mapbox API
            val response = mapboxService.getDirections(
                coordinates = "${userLocation.longitude},${userLocation.latitude};${accommodationLocation.longitude},${accommodationLocation.latitude}",
                profile = transportProfile.mapboxProfile,
                geometries = "geojson",
                steps = true,
                voiceInstructions = true,
                bannerInstructions = true,
                roundtrip = false,
                source = "first",
                destination = "last",
                alternatives = true,
                exclude = if (avoidTolls) "toll" else null,
                departAt = departureTime?.let { formatMapboxTime(it) },
                accessToken = MAPBOX_ACCESS_TOKEN
            )
            
            if (response.isSuccessful) {
                val directionsResponse = response.body()!!
                
                // Enhance with student-specific information
                val enhancedResponse = enhanceDirectionsForStudents(
                    directionsResponse,
                    userLocation,
                    accommodationLocation
                )
                
                // Cache the result
                cacheDirections(cacheKey, enhancedResponse)
                
                Result.success(enhancedResponse)
            } else {
                // Fallback to basic distance calculation
                val fallbackResponse = createFallbackDirections(userLocation, accommodationLocation)
                Result.success(fallbackResponse)
            }
        } catch (e: Exception) {
            // Return basic distance as fallback
            val fallbackResponse = createFallbackDirections(userLocation, accommodationLocation)
            Result.success(fallbackResponse)
        }
    }
    
    /**
     * Get multiple routes for accommodation comparison
     */
    suspend fun getBulkDirections(
        userLocation: LatLong,
        accommodationLocations: List<Pair<String, LatLong>>,
        transportProfile: TransportProfile = TransportProfile.WALKING
    ): Flow<Result<List<AccommodationDirections>>> = flow {
        try {
            val results = mutableListOf<AccommodationDirections>()
            
            // Process in batches to avoid API rate limits
            accommodationLocations.chunked(5).forEach { batch ->
                val batchResults = batch.map { (accommodationId, location) ->
                    val directionsResult = getDirectionsToAccommodation(
                        userLocation = userLocation,
                        accommodationLocation = location,
                        transportProfile = transportProfile
                    )
                    
                    if (directionsResult.isSuccess) {
                        val directions = directionsResult.getOrNull()!!
                        AccommodationDirections(
                            accommodationId = accommodationId,
                            directions = directions,
                            estimatedCommuteTimes = calculateCommuteVariations(directions)
                        )
                    } else {
                        // Fallback with basic distance
                        val distance = distanceCalculator.calculateDistance(userLocation, location)
                        AccommodationDirections(
                            accommodationId = accommodationId,
                            directions = createFallbackDirections(userLocation, location),
                            estimatedCommuteTimes = EstimatedCommuteTimes(
                                peakHours = (distance * 12).toInt(), // Rough walking estimate
                                offPeakHours = (distance * 10).toInt(),
                                averageTime = (distance * 11).toInt()
                            )
                        )
                    }
                }
                results.addAll(batchResults)
            }
            
            emit(Result.success(results))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }
    
    /**
     * Launch Google Maps for turn-by-turn navigation
     */
    fun launchGoogleMapsNavigation(
        destinationLocation: LatLong,
        destinationName: String? = null,
        travelMode: GoogleMapsTravelMode = GoogleMapsTravelMode.WALKING
    ): Result<Boolean> {
        return try {
            val uri = buildString {
                append("google.navigation:q=")
                append("${destinationLocation.latitude},${destinationLocation.longitude}")
                if (destinationName != null) {
                    append("&label=${Uri.encode(destinationName)}")
                }
                append("&mode=${travelMode.mode}")
            }
            
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri)).apply {
                setPackage("com.google.android.apps.maps")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            
            // Check if Google Maps is installed
            val packageManager = context.packageManager
            if (intent.resolveActivity(packageManager) != null) {
                context.startActivity(intent)
                Result.success(true)
            } else {
                // Fallback to browser-based Google Maps
                launchWebGoogleMaps(destinationLocation, destinationName)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get isochrone data for finding accommodations within travel time
     */
    suspend fun getAccessibilityIsochrone(
        centerLocation: LatLong,
        maxTravelTimeMinutes: Int = 30,
        transportProfile: TransportProfile = TransportProfile.WALKING
    ): Result<IsochroneResponse> {
        return try {
            val response = mapboxService.getIsochrone(
                coordinates = "${centerLocation.longitude},${centerLocation.latitude}",
                contours = maxTravelTimeMinutes,
                profile = transportProfile.mapboxProfile,
                accessToken = MAPBOX_ACCESS_TOKEN
            )
            
            if (response.isSuccessful) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to get isochrone data"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Calculate commute cost for different transport modes
     */
    suspend fun calculateCommuteAnalysis(
        userLocation: LatLong,
        accommodationLocation: LatLong,
        collegeLocation: LatLong?
    ): Result<CommuteAnalysis> {
        return try {
            val profiles = listOf(
                TransportProfile.WALKING,
                TransportProfile.CYCLING,
                TransportProfile.DRIVING
            )
            
            val routes = mutableMapOf<TransportProfile, DirectionsResponse>()
            
            // Get directions for each transport mode
            profiles.forEach { profile ->
                val result = getDirectionsToAccommodation(
                    userLocation = userLocation,
                    accommodationLocation = accommodationLocation,
                    transportProfile = profile
                )
                if (result.isSuccess) {
                    routes[profile] = result.getOrNull()!!
                }
            }
            
            // Calculate costs and recommendations
            val analysis = CommuteAnalysis(
                walkingTime = routes[TransportProfile.WALKING]?.routes?.firstOrNull()?.duration,
                cyclingTime = routes[TransportProfile.CYCLING]?.routes?.firstOrNull()?.duration,
                drivingTime = routes[TransportProfile.DRIVING]?.routes?.firstOrNull()?.duration,
                publicTransitTime = null, // TODO: Integrate public transit API
                estimatedMonthlyCosts = calculateMonthlyCosts(routes),
                carbonFootprint = calculateCarbonFootprint(routes),
                studentRecommendation = generateStudentRecommendation(routes, collegeLocation),
                peakHourVariation = calculatePeakHourVariation(routes)
            )
            
            Result.success(analysis)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get optimized route for visiting multiple accommodations
     */
    suspend fun getOptimizedViewingRoute(
        startLocation: LatLong,
        accommodationLocations: List<Pair<String, LatLong>>,
        returnToStart: Boolean = true
    ): Result<OptimizedRoute> {
        return try {
            // Build coordinates string for Mapbox Optimization API
            val coordinates = buildString {
                append("${startLocation.longitude},${startLocation.latitude}")
                accommodationLocations.forEach { (_, location) ->
                    append(";${location.longitude},${location.latitude}")
                }
                if (returnToStart) {
                    append(";${startLocation.longitude},${startLocation.latitude}")
                }
            }
            
            val response = mapboxService.getOptimizedRoute(
                coordinates = coordinates,
                profile = "walking", // Best for accommodation viewing
                source = "first",
                destination = if (returnToStart) "last" else "any",
                roundtrip = returnToStart,
                steps = true,
                accessToken = MAPBOX_ACCESS_TOKEN
            )
            
            if (response.isSuccessful) {
                val optimizedResponse = response.body()!!
                Result.success(
                    OptimizedRoute(
                        totalDuration = optimizedResponse.routes.firstOrNull()?.duration ?: 0,
                        totalDistance = optimizedResponse.routes.firstOrNull()?.distance ?: 0.0,
                        visitOrder = optimizedResponse.waypoints.map { it.waypointIndex },
                        route = optimizedResponse.routes.firstOrNull(),
                        estimatedViewingTime = calculateViewingTime(accommodationLocations.size)
                    )
                )
            } else {
                Result.failure(Exception("Failed to get optimized route"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Private helper methods
    
    private fun generateCacheKey(
        userLocation: LatLong,
        accommodationLocation: LatLong,
        transportProfile: TransportProfile
    ): String {
        return "${userLocation.latitude}_${userLocation.longitude}_${accommodationLocation.latitude}_${accommodationLocation.longitude}_${transportProfile.name}"
    }
    
    private suspend fun getCachedDirections(cacheKey: String): TrafficCacheEntity? {
        return trafficCacheDao.getCachedRoute(cacheKey)
    }
    
    private fun isCacheValid(cachedAt: Long): Boolean {
        val cacheAgeHours = (System.currentTimeMillis() - cachedAt) / (1000 * 60 * 60)
        return cacheAgeHours < CACHE_VALIDITY_HOURS
    }
    
    private suspend fun cacheDirections(cacheKey: String, directions: DirectionsResponse) {
        val cacheEntity = TrafficCacheEntity(
            routeKey = cacheKey,
            directionsJson = directions.toJson(), // You'd implement this serialization
            duration = directions.routes.firstOrNull()?.duration ?: 0,
            distance = directions.routes.firstOrNull()?.distance ?: 0.0,
            trafficCondition = "moderate", // Extract from Mapbox response
            cachedAt = System.currentTimeMillis()
        )
        trafficCacheDao.insertRoute(cacheEntity)
    }
    
    private fun enhanceDirectionsForStudents(
        directionsResponse: DirectionsResponse,
        userLocation: LatLong,
        accommodationLocation: LatLong
    ): DirectionsResponse {
        // Add student-specific enhancements like safety scores, well-lit paths, etc.
        return directionsResponse.copy(
            routes = directionsResponse.routes.map { route ->
                route.copy(
                    // Add student-specific metadata
                    metadata = route.metadata + mapOf(
                        "safety_score" to calculateSafetyScore(route),
                        "student_friendly" to isStudentFriendlyRoute(route),
                        "late_night_safe" to isLateNightSafe(route)
                    )
                )
            }
        )
    }
    
    private fun createFallbackDirections(
        userLocation: LatLong,
        accommodationLocation: LatLong
    ): DirectionsResponse {
        val distance = distanceCalculator.calculateDistance(userLocation, accommodationLocation)
        val walkingDuration = (distance * 12 * 60).toInt() // 12 minutes per km
        
        return DirectionsResponse(
            routes = listOf(
                Route(
                    distance = distance * 1000, // Convert to meters
                    duration = walkingDuration,
                    geometry = null, // No detailed geometry for fallback
                    legs = emptyList(),
                    metadata = mapOf(
                        "fallback" to true,
                        "estimated" to true
                    )
                )
            ),
            waypoints = emptyList(),
            code = "Ok"
        )
    }
    
    private fun launchWebGoogleMaps(
        destinationLocation: LatLong,
        destinationName: String?
    ): Result<Boolean> {
        return try {
            val uri = buildString {
                append("https://www.google.com/maps/dir/?api=1")
                append("&destination=${destinationLocation.latitude},${destinationLocation.longitude}")
                if (destinationName != null) {
                    append("&destination_place_id=${Uri.encode(destinationName)}")
                }
                append("&travelmode=walking")
            }
            
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            
            context.startActivity(intent)
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    private fun calculateCommuteVariations(directions: DirectionsResponse): EstimatedCommuteTimes {
        val baseDuration = directions.routes.firstOrNull()?.duration ?: 0
        return EstimatedCommuteTimes(
            peakHours = (baseDuration * 1.3).toInt(), // 30% longer during peak
            offPeakHours = (baseDuration * 0.9).toInt(), // 10% faster off-peak
            averageTime = baseDuration
        )
    }
    
    private fun calculateMonthlyCosts(routes: Map<TransportProfile, DirectionsResponse>): Map<TransportProfile, Double> {
        return routes.mapValues { (profile, directions) ->
            val distance = directions.routes.firstOrNull()?.distance ?: 0.0
            when (profile) {
                TransportProfile.WALKING -> 0.0 // Free
                TransportProfile.CYCLING -> 50.0 // Bike maintenance
                TransportProfile.DRIVING -> distance * 0.5 * 30 // Rough fuel cost per month
            }
        }
    }
    
    private fun calculateCarbonFootprint(routes: Map<TransportProfile, DirectionsResponse>): Map<TransportProfile, Double> {
        return routes.mapValues { (profile, directions) ->
            val distance = directions.routes.firstOrNull()?.distance ?: 0.0
            when (profile) {
                TransportProfile.WALKING, TransportProfile.CYCLING -> 0.0
                TransportProfile.DRIVING -> distance * 0.21 // kg CO2 per km
            }
        }
    }
    
    private fun generateStudentRecommendation(
        routes: Map<TransportProfile, DirectionsResponse>,
        collegeLocation: LatLong?
    ): String {
        val walkingRoute = routes[TransportProfile.WALKING]
        val cyclingRoute = routes[TransportProfile.CYCLING]
        
        return when {
            walkingRoute?.routes?.firstOrNull()?.duration ?: Int.MAX_VALUE < 20 * 60 -> 
                "🚶‍♂️ Perfect walking distance! Great for daily exercise and saving money."
            cyclingRoute?.routes?.firstOrNull()?.duration ?: Int.MAX_VALUE < 15 * 60 -> 
                "🚴‍♂️ Ideal for cycling! Consider getting a bike for quick, eco-friendly commutes."
            else -> 
                "🚌 Consider public transport or carpooling options for cost-effective commuting."
        }
    }
    
    private fun calculatePeakHourVariation(routes: Map<TransportProfile, DirectionsResponse>): Map<String, Int> {
        val baseDuration = routes[TransportProfile.WALKING]?.routes?.firstOrNull()?.duration ?: 0
        return mapOf(
            "morning_peak" to (baseDuration * 1.2).toInt(),
            "evening_peak" to (baseDuration * 1.3).toInt(),
            "off_peak" to (baseDuration * 0.9).toInt()
        )
    }
    
    private fun calculateSafetyScore(route: Route): Double {
        // Implement safety scoring based on route characteristics
        return 8.5 // Placeholder
    }
    
    private fun isStudentFriendlyRoute(route: Route): Boolean {
        // Check for well-lit areas, proximity to campus, etc.
        return true // Placeholder
    }
    
    private fun isLateNightSafe(route: Route): Boolean {
        // Check for safety during late hours
        return true // Placeholder
    }
    
    private fun formatMapboxTime(timestamp: Long): String {
        // Format timestamp for Mapbox API
        return timestamp.toString()
    }
    
    private fun calculateViewingTime(numberOfAccommodations: Int): Int {
        // Estimate viewing time: 15 minutes per accommodation + travel time buffer
        return numberOfAccommodations * 15 + 10
    }
    
    /**
     * Clean up old cached routes to manage storage
     */
    suspend fun cleanupOldCache(maxAgeMs: Long = 24 * 60 * 60 * 1000L) { // 24 hours
        trafficCacheDao.cleanupOldRoutes(System.currentTimeMillis() - maxAgeMs)
    }
}

// Extension function for JSON serialization (implement with your preferred JSON library)
private fun DirectionsResponse.toJson(): String {
    // Implement JSON serialization
    return "" // Placeholder
}

private fun TrafficCacheEntity.toDirectionsResponse(): DirectionsResponse {
    // Implement JSON deserialization
    return DirectionsResponse(emptyList(), emptyList(), "Ok") // Placeholder
}