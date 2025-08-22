package com.aalay.app.utils

import kotlin.math.*

/**
 * Utility class for calculating distances between geographical coordinates
 * Uses the Haversine formula for accurate distance calculations
 */
object DistanceCalculator {
    
    private const val EARTH_RADIUS_KM = 6371.0
    private const val EARTH_RADIUS_MILES = 3959.0
    
    /**
     * Calculate distance between two points using Haversine formula
     * @param lat1 Latitude of first point
     * @param lon1 Longitude of first point
     * @param lat2 Latitude of second point
     * @param lon2 Longitude of second point
     * @param unit Distance unit (KM or MILES)
     * @return Distance in specified unit
     */
    fun calculateDistance(
        lat1: Double,
        lon1: Double,
        lat2: Double,
        lon2: Double,
        unit: DistanceUnit = DistanceUnit.KM
    ): Double {
        // Convert latitude and longitude from degrees to radians
        val lat1Rad = Math.toRadians(lat1)
        val lon1Rad = Math.toRadians(lon1)
        val lat2Rad = Math.toRadians(lat2)
        val lon2Rad = Math.toRadians(lon2)
        
        // Calculate differences
        val deltaLat = lat2Rad - lat1Rad
        val deltaLon = lon2Rad - lon1Rad
        
        // Haversine formula
        val a = sin(deltaLat / 2).pow(2) + 
                cos(lat1Rad) * cos(lat2Rad) * sin(deltaLon / 2).pow(2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        
        val radius = when (unit) {
            DistanceUnit.KM -> EARTH_RADIUS_KM
            DistanceUnit.MILES -> EARTH_RADIUS_MILES
        }
        
        return radius * c
    }
    
    /**
     * Calculate distance and return formatted string
     * @param lat1 Latitude of first point
     * @param lon1 Longitude of first point
     * @param lat2 Latitude of second point
     * @param lon2 Longitude of second point
     * @param unit Distance unit
     * @return Formatted distance string (e.g., "2.5 km", "1.2 miles")
     */
    fun getFormattedDistance(
        lat1: Double,
        lon1: Double,
        lat2: Double,
        lon2: Double,
        unit: DistanceUnit = DistanceUnit.KM
    ): String {
        val distance = calculateDistance(lat1, lon1, lat2, lon2, unit)
        val unitSuffix = when (unit) {
            DistanceUnit.KM -> "km"
            DistanceUnit.MILES -> "miles"
        }
        
        return when {
            distance < 1.0 -> {
                val meters = (distance * 1000).roundToInt()
                "$meters m"
            }
            distance < 10.0 -> {
                "%.1f $unitSuffix".format(distance)
            }
            else -> {
                "${distance.roundToInt()} $unitSuffix"
            }
        }
    }
    
    /**
     * Get proximity category based on distance
     * @param distanceKm Distance in kilometers
     * @return Proximity category for UI display
     */
    fun getProximityCategory(distanceKm: Double): ProximityCategory {
        return when {
            distanceKm <= 0.5 -> ProximityCategory.VERY_CLOSE
            distanceKm <= 1.0 -> ProximityCategory.WALKING_DISTANCE
            distanceKm <= 3.0 -> ProximityCategory.SHORT_COMMUTE
            distanceKm <= 10.0 -> ProximityCategory.MODERATE_COMMUTE
            else -> ProximityCategory.LONG_COMMUTE
        }
    }
    
    /**
     * Get walking time estimate based on distance
     * Adjusted for Chhattisgarh terrain and climate (slower in summer)
     * @param distanceKm Distance in kilometers
     * @return Estimated walking time in minutes
     */
    fun getWalkingTimeMinutes(distanceKm: Double): Int {
        // Slower walking speed due to hot climate in CG (April-June)
        val walkingSpeedKmh = if (isHotSeason()) 4.0 else 5.0
        val timeHours = distanceKm / walkingSpeedKmh
        return (timeHours * 60).roundToInt()
    }
    
    /**
     * Get cycling time estimate based on distance
     * Adjusted for Chhattisgarh roads and weather conditions
     * @param distanceKm Distance in kilometers
     * @return Estimated cycling time in minutes
     */
    fun getCyclingTimeMinutes(distanceKm: Double): Int {
        // Adjusted cycling speed for CG road conditions
        val cyclingSpeedKmh = if (isHotSeason()) 12.0 else 15.0
        val timeHours = distanceKm / cyclingSpeedKmh
        return (timeHours * 60).roundToInt()
    }
    
    /**
     * Get auto-rickshaw time estimate (common in CG cities)
     * @param distanceKm Distance in kilometers
     * @return Estimated auto-rickshaw time in minutes
     */
    fun getAutoRickshawTimeMinutes(distanceKm: Double): Int {
        val autoSpeedKmh = 25.0 // Average speed in CG cities
        val timeHours = distanceKm / autoSpeedKmh
        return (timeHours * 60).roundToInt()
    }
    
    /**
     * Check if it's hot season in Chhattisgarh (April-June)
     * @return True if it's hot season
     */
    private fun isHotSeason(): Boolean {
        val calendar = Calendar.getInstance()
        val month = calendar.get(Calendar.MONTH)
        return month in Calendar.APRIL..Calendar.JUNE
    }
    
    /**
     * Sort accommodations by distance from a reference point
     * @param accommodations List of accommodations with lat/lng
     * @param referenceLat Reference latitude (e.g., user's location or college)
     * @param referenceLng Reference longitude
     * @return Sorted list by proximity
     */
    fun <T> sortByDistance(
        items: List<T>,
        referenceLat: Double,
        referenceLng: Double,
        getLatitude: (T) -> Double,
        getLongitude: (T) -> Double
    ): List<Pair<T, Double>> {
        return items.map { item ->
            val distance = calculateDistance(
                referenceLat, referenceLng,
                getLatitude(item), getLongitude(item)
            )
            Pair(item, distance)
        }.sortedBy { it.second }
    }
    
    /**
     * Check if a location is within a specified radius
     * @param centerLat Center point latitude
     * @param centerLng Center point longitude
     * @param pointLat Point to check latitude
     * @param pointLng Point to check longitude
     * @param radiusKm Radius in kilometers
     * @return True if point is within radius
     */
    fun isWithinRadius(
        centerLat: Double,
        centerLng: Double,
        pointLat: Double,
        pointLng: Double,
        radiusKm: Double
    ): Boolean {
        val distance = calculateDistance(centerLat, centerLng, pointLat, pointLng)
        return distance <= radiusKm
    }
}

enum class DistanceUnit {
    KM, MILES
}

enum class ProximityCategory(val displayText: String, val color: String) {
    VERY_CLOSE("Very Close", "#4CAF50"),
    WALKING_DISTANCE("Walking Distance", "#8BC34A"),
    SHORT_COMMUTE("Short Commute", "#FFC107"),
    MODERATE_COMMUTE("Moderate Commute", "#FF9800"),
    LONG_COMMUTE("Long Commute", "#F44336")
}