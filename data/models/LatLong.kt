package com.aalay.app.data.models

import androidx.room.Entity
import com.google.gson.annotations.SerializedName

/**
 * Data class representing geographical coordinates
 * Used throughout the app for location-based features
 */
@Entity
data class LatLong(
    @SerializedName("latitude")
    val latitude: Double,
    
    @SerializedName("longitude")
    val longitude: Double
) {
    companion object {
        /**
         * Calculate distance between two points using Haversine formula
         * @param other The other LatLong point to calculate distance to
         * @return Distance in kilometers
         */
        fun LatLong.distanceTo(other: LatLong): Double {
            val earthRadius = 6371.0 // Earth's radius in kilometers
            
            val lat1Rad = Math.toRadians(this.latitude)
            val lat2Rad = Math.toRadians(other.latitude)
            val deltaLatRad = Math.toRadians(other.latitude - this.latitude)
            val deltaLngRad = Math.toRadians(other.longitude - this.longitude)
            
            val a = Math.sin(deltaLatRad / 2) * Math.sin(deltaLatRad / 2) +
                    Math.cos(lat1Rad) * Math.cos(lat2Rad) *
                    Math.sin(deltaLngRad / 2) * Math.sin(deltaLngRad / 2)
            
            val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
            
            return earthRadius * c
        }
    }
    
    /**
     * Check if coordinates are valid
     */
    fun isValid(): Boolean {
        return latitude in -90.0..90.0 && longitude in -180.0..180.0
    }
    
    /**
     * Convert to Google Maps navigation URI format
     */
    fun toGoogleMapsUri(): String {
        return "google.navigation:q=$latitude,$longitude"
    }
}