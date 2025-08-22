package com.aalay.app.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.aalay.app.data.models.LatLong

/**
 * Room entity for offline accommodation storage
 * Optimized for student-focused features and quick access
 */
@Entity(tableName = "accommodations")
data class AccommodationEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val description: String,
    val accommodationType: String, // "PG", "Hostel", "Apartment", "Flat"
    val roomType: String, // "Single", "Shared", "Double"
    val genderPreference: String?, // "Male", "Female", "Any"
    
    // Location details with lat/long for distance calculations
    val latitude: Double,
    val longitude: Double,
    val address: String,
    val city: String,
    val state: String,
    val pincode: String,
    
    // Pricing - student-focused
    val monthlyRent: Double,
    val weeklyRent: Double?,
    val securityDeposit: Double,
    val maintenanceCharges: Double?,
    val electricityIncluded: Boolean,
    val waterIncluded: Boolean,
    
    // Student-specific amenities
    val hasWifi: Boolean,
    val hasStudyDesk: Boolean,
    val hasLaundry: Boolean,
    val hasMessFood: Boolean,
    val hasGym: Boolean,
    val hasParking: Boolean,
    val hasAC: Boolean,
    val hasAttachedBathroom: Boolean,
    
    // Proximity to educational institutions
    val nearestCollegeName: String?,
    val distanceToCollege: Double?, // in kilometers
    val nearPublicTransport: Boolean,
    
    // Host/Owner details
    val hostId: String,
    val hostName: String,
    val hostPhone: String?,
    val hostVerified: Boolean,
    val isStudentFriendly: Boolean, // Special badge for student accommodations
    
    // Reviews and ratings
    val averageRating: Float,
    val totalReviews: Int,
    val studentReviews: Int, // Reviews specifically from students
    
    // Availability and booking
    val isAvailable: Boolean,
    val availableFrom: Long, // Timestamp
    val minimumStayDuration: Int, // in months
    val maximumOccupancy: Int,
    val currentOccupancy: Int,
    
    // Media
    val primaryImageUrl: String?,
    val imageUrls: List<String>, // List of all image URLs
    val virtualTourUrl: String?,
    
    // Caching metadata
    val lastUpdated: Long,
    val cachedAt: Long,
    val isBookmarked: Boolean = false,
    
    // Additional student features
    val allowsVisitors: Boolean,
    val curfewTime: String?, // "22:00" format
    val studentIdRequired: Boolean,
    val semesterBookingAvailable: Boolean,
    val roomateMatchingAvailable: Boolean
) {
    fun toLatLong(): LatLong = LatLong(latitude, longitude)
    
    companion object {
        // Helper function to determine if accommodation matches student criteria
        fun isStudentOptimized(entity: AccommodationEntity): Boolean {
            return entity.isStudentFriendly && 
                   entity.hasWifi && 
                   entity.hasStudyDesk &&
                   entity.semesterBookingAvailable
        }
    }
}