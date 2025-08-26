package com.aalay.app.utils

import kotlin.math.*

/**
 * Utility class for map-related operations specific to Bhilai
 * Handles coordinate validation and Google Maps URL generation
 */
object MapUtils {
    
    // Bhilai geographical boundaries
    private const val BHILAI_MIN_LAT = 21.1
    private const val BHILAI_MAX_LAT = 21.3
    private const val BHILAI_MIN_LNG = 81.2
    private const val BHILAI_MAX_LNG = 81.4
    
    // Bhilai center coordinates
    const val BHILAI_CENTER_LAT = 21.2181
    const val BHILAI_CENTER_LNG = 81.3248
    
    // Google Maps URL base
    private const val GOOGLE_MAPS_BASE_URL = "https://www.google.com/maps/search/?api=1&query="
    
    /**
     * Validate if coordinates are within Bhilai bounds
     * @param latitude The latitude coordinate
     * @param longitude The longitude coordinate
     * @return true if coordinates are within Bhilai, false otherwise
     */
    fun isWithinBhilaiBounds(latitude: Double, longitude: Double): Boolean {
        return latitude in BHILAI_MIN_LAT..BHILAI_MAX_LAT && 
               longitude in BHILAI_MIN_LNG..BHILAI_MAX_LNG
    }
    
    /**
     * Generate Google Maps URL for navigation
     * @param latitude The latitude coordinate
     * @param longitude The longitude coordinate
     * @param validateBounds Whether to validate coordinates are within Bhilai (default: true)
     * @return Google Maps URL string
     * @throws IllegalArgumentException if coordinates are outside Bhilai bounds and validation is enabled
     */
    fun generateMapsUrl(
        latitude: Double, 
        longitude: Double, 
        validateBounds: Boolean = true
    ): String {
        if (validateBounds && !isWithinBhilaiBounds(latitude, longitude)) {
            throw IllegalArgumentException(
                "Coordinates ($latitude, $longitude) are outside Bhilai bounds. " +
                "Expected: lat: $BHILAI_MIN_LAT-$BHILAI_MAX_LAT, lng: $BHILAI_MIN_LNG-$BHILAI_MAX_LNG"
            )
        }
        
        return "$GOOGLE_MAPS_BASE_URL$latitude,$longitude"
    }
    
    /**
     * Generate Google Maps URL with location name
     * @param latitude The latitude coordinate
     * @param longitude The longitude coordinate
     * @param locationName The name of the location
     * @param validateBounds Whether to validate coordinates are within Bhilai
     * @return Google Maps URL string with location name
     */
    fun generateMapsUrlWithName(
        latitude: Double,
        longitude: Double,
        locationName: String,
        validateBounds: Boolean = true
    ): String {
        if (validateBounds && !isWithinBhilaiBounds(latitude, longitude)) {
            throw IllegalArgumentException(
                "Coordinates ($latitude, $longitude) are outside Bhilai bounds."
            )
        }
        
        val encodedName = locationName.replace(" ", "+")
        return "${GOOGLE_MAPS_BASE_URL}$latitude,$longitude&query_place_id=$encodedName"
    }
    
    /**
     * Generate directions URL from current location to destination
     * @param destLatitude Destination latitude
     * @param destLongitude Destination longitude
     * @param validateBounds Whether to validate coordinates are within Bhilai
     * @return Google Maps directions URL
     */
    fun generateDirectionsUrl(
        destLatitude: Double,
        destLongitude: Double,
        validateBounds: Boolean = true
    ): String {
        if (validateBounds && !isWithinBhilaiBounds(destLatitude, destLongitude)) {
            throw IllegalArgumentException(
                "Destination coordinates are outside Bhilai bounds."
            )
        }
        
        return "https://www.google.com/maps/dir/?api=1&destination=$destLatitude,$destLongitude"
    }
    
    /**
     * Calculate distance between two coordinates using Haversine formula
     * @param lat1 First latitude
     * @param lng1 First longitude
     * @param lat2 Second latitude
     * @param lng2 Second longitude
     * @return Distance in kilometers
     */
    fun calculateDistance(lat1: Double, lng1: Double, lat2: Double, lng2: Double): Double {
        val earthRadius = 6371.0 // Earth's radius in kilometers
        
        val dLat = Math.toRadians(lat2 - lat1)
        val dLng = Math.toRadians(lng2 - lng1)
        
        val a = sin(dLat / 2).pow(2) + 
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) * 
                sin(dLng / 2).pow(2)
        
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        
        return earthRadius * c
    }
    
    /**
     * Calculate distance from Bhilai center
     * @param latitude The latitude coordinate
     * @param longitude The longitude coordinate
     * @return Distance in kilometers from Bhilai center
     */
    fun distanceFromBhilaiCenter(latitude: Double, longitude: Double): Double {
        return calculateDistance(BHILAI_CENTER_LAT, BHILAI_CENTER_LNG, latitude, longitude)
    }
    
    /**
     * Get formatted distance string
     * @param distanceKm Distance in kilometers
     * @return Formatted distance string
     */
    fun formatDistance(distanceKm: Double): String {
        return when {
            distanceKm < 1.0 -> "${(distanceKm * 1000).roundToInt()} m"
            distanceKm < 10.0 -> "${String.format("%.1f", distanceKm)} km"
            else -> "${distanceKm.roundToInt()} km"
        }
    }
    
    /**
     * Check if a location is within walking distance of Bhilai center
     * @param latitude The latitude coordinate
     * @param longitude The longitude coordinate
     * @param walkingDistanceKm Maximum walking distance in km (default: 2.0)
     * @return true if within walking distance
     */
    fun isWalkingDistanceFromCenter(
        latitude: Double, 
        longitude: Double, 
        walkingDistanceKm: Double = 2.0
    ): Boolean {
        return distanceFromBhilaiCenter(latitude, longitude) <= walkingDistanceKm
    }
    
    /**
     * Get the appropriate zoom level for Google Maps based on distance
     * @param distanceKm Distance in kilometers
     * @return Zoom level (1-20)
     */
    fun getZoomLevel(distanceKm: Double): Int {
        return when {
            distanceKm <= 0.5 -> 17  // Very close
            distanceKm <= 1.0 -> 16  // Close
            distanceKm <= 2.0 -> 15  // Nearby
            distanceKm <= 5.0 -> 14  // Moderate distance
            else -> 13               // Far
        }
    }
    
    /**
     * Validate and sanitize coordinates
     * @param latitude Raw latitude value
     * @param longitude Raw longitude value
     * @return Pair of validated coordinates or null if invalid
     */
    fun validateAndSanitizeCoordinates(latitude: Double?, longitude: Double?): Pair<Double, Double>? {
        if (latitude == null || longitude == null) return null
        if (latitude.isNaN() || longitude.isNaN()) return null
        if (latitude.isInfinite() || longitude.isInfinite()) return null
        
        // Round to 6 decimal places for precision
        val roundedLat = (latitude * 1000000).roundToInt() / 1000000.0
        val roundedLng = (longitude * 1000000).roundToInt() / 1000000.0
        
        return if (isWithinBhilaiBounds(roundedLat, roundedLng)) {
            Pair(roundedLat, roundedLng)
        } else {
            null
        }
    }
    
    /**
     * Generate a bounding box around Bhilai for map display
     * @param paddingKm Additional padding in kilometers
     * @return Bounding box coordinates [minLat, minLng, maxLat, maxLng]
     */
    fun getBhilaiBoundingBox(paddingKm: Double = 1.0): DoubleArray {
        // Approximate: 1 degree latitude ≈ 111 km, 1 degree longitude ≈ 111 * cos(latitude) km
        val latPadding = paddingKm / 111.0
        val lngPadding = paddingKm / (111.0 * cos(Math.toRadians(BHILAI_CENTER_LAT)))
        
        return doubleArrayOf(
            BHILAI_MIN_LAT - latPadding,  // minLat
            BHILAI_MIN_LNG - lngPadding,  // minLng
            BHILAI_MAX_LAT + latPadding,  // maxLat
            BHILAI_MAX_LNG + lngPadding   // maxLng
        )
    }
}