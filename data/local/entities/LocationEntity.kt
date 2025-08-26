package com.aalay.app.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo
import androidx.room.TypeConverters
import com.aalay.app.data.local.RoomtypeConverter

/**
 * Room database entity for Bhilai location data
 * Stores room and mess information with geographic coordinates
 */
@Entity(tableName = "bhilai_locations")
@TypeConverters(RoomtypeConverter::class)
data class LocationEntity(
    @PrimaryKey 
    val id: String,
    
    @ColumnInfo(name = "type")
    val type: String, // "room" or "mess"
    
    @ColumnInfo(name = "name")
    val name: String,
    
    @ColumnInfo(name = "latitude")
    val latitude: Double,
    
    @ColumnInfo(name = "longitude") 
    val longitude: Double,
    
    @ColumnInfo(name = "address")
    val address: String,
    
    @ColumnInfo(name = "description")
    val description: String? = null,
    
    @ColumnInfo(name = "price_per_month")
    val pricePerMonth: Int? = null,
    
    @ColumnInfo(name = "rating")
    val rating: Float = 0.0f,
    
    @ColumnInfo(name = "total_ratings")
    val totalRatings: Int = 0,
    
    @ColumnInfo(name = "amenities")
    val amenities: List<String> = emptyList(),
    
    @ColumnInfo(name = "contact_phone")
    val contactPhone: String? = null,
    
    @ColumnInfo(name = "contact_email")
    val contactEmail: String? = null,
    
    @ColumnInfo(name = "is_verified")
    val isVerified: Boolean = false,
    
    @ColumnInfo(name = "is_featured")
    val isFeatured: Boolean = false,
    
    @ColumnInfo(name = "availability")
    val availability: String? = null, // "available", "full", "limited"
    
    @ColumnInfo(name = "images")
    val images: List<String> = emptyList(),
    
    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),
    
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = System.currentTimeMillis(),
    
    @ColumnInfo(name = "distance_from_center")
    val distanceFromCenter: Double? = null, // Distance in KM from Bhilai center
    
    @ColumnInfo(name = "nearby_colleges")
    val nearbyColleges: List<String> = emptyList()
) {
    /**
     * Check if coordinates are within Bhilai bounds
     */
    fun isWithinBhilaiBounds(): Boolean {
        return latitude in 21.1..21.3 && longitude in 81.2..81.4
    }
    
    /**
     * Generate Google Maps URL for navigation
     */
    fun getGoogleMapsUrl(): String {
        return "https://www.google.com/maps/search/?api=1&query=$latitude,$longitude"
    }
    
    /**
     * Get formatted price string
     */
    fun getFormattedPrice(): String? {
        return pricePerMonth?.let { "₹$it/month" }
    }
    
    /**
     * Get rating string with total ratings
     */
    fun getRatingDisplay(): String {
        return if (totalRatings > 0) {
            "★ ${String.format("%.1f", rating)} ($totalRatings reviews)"
        } else {
            "No ratings yet"
        }
    }
    
    /**
     * Check if location is available for booking
     */
    fun isAvailable(): Boolean {
        return availability != "full"
    }
}

/**
 * Data class for location coordinates
 */
data class LocationCoordinates(
    val latitude: Double,
    val longitude: Double
) {
    fun isInBhilai(): Boolean {
        return latitude in 21.1..21.3 && longitude in 81.2..81.4
    }
    
    fun toGoogleMapsUrl(): String {
        return "https://www.google.com/maps/search/?api=1&query=$latitude,$longitude"
    }
}

/**
 * Enum class for location types
 */
enum class LocationType(val value: String, val displayName: String) {
    ROOM("room", "Student Room/PG"),
    MESS("mess", "Mess/Food Service"),
    BOTH("both", "Room + Mess");
    
    companion object {
        fun fromString(value: String): LocationType {
            return values().find { it.value.equals(value, ignoreCase = true) } ?: ROOM
        }
    }
}

/**
 * Data class for location filters
 */
data class LocationFilter(
    val type: LocationType? = null,
    val maxPrice: Int? = null,
    val minRating: Float? = null,
    val verifiedOnly: Boolean = false,
    val availableOnly: Boolean = true,
    val maxDistance: Double? = null, // in KM from Bhilai center
    val amenities: List<String> = emptyList()
)