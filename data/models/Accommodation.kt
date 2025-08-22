package com.aalay.app.data.models

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

/**
 * Core accommodation listing model
 * Represents PG, hostels, apartments, flats, and shared rooms
 */
@Entity(tableName = "accommodations")
data class Accommodation(
    @PrimaryKey
    @SerializedName("id")
    val id: String,
    
    @SerializedName("title")
    val title: String,
    
    @SerializedName("description")
    val description: String,
    
    @SerializedName("type")
    val type: AccommodationType,
    
    @SerializedName("room_type")
    val roomType: RoomType,
    
    @SerializedName("gender_preference")
    val genderPreference: GenderPreference,
    
    // Location details
    @Embedded(prefix = "location_")
    @SerializedName("location")
    val location: LatLong,
    
    @SerializedName("address")
    val address: String,
    
    @SerializedName("city")
    val city: String,
    
    @SerializedName("state")
    val state: String,
    
    @SerializedName("postal_code")
    val postalCode: String,
    
    @SerializedName("country")
    val country: String = "India",
    
    // Pricing details
    @SerializedName("monthly_rent")
    val monthlyRent: Double,
    
    @SerializedName("security_deposit")
    val securityDeposit: Double,
    
    @SerializedName("weekly_rent")
    val weeklyRent: Double? = null,
    
    @SerializedName("electricity_charges")
    val electricityCharges: Double? = null,
    
    @SerializedName("maintenance_charges")
    val maintenanceCharges: Double? = null,
    
    // Property details
    @SerializedName("total_rooms")
    val totalRooms: Int,
    
    @SerializedName("available_rooms")
    val availableRooms: Int,
    
    @SerializedName("bathrooms")
    val bathrooms: Int,
    
    @SerializedName("floor_number")
    val floorNumber: Int? = null,
    
    @SerializedName("total_floors")
    val totalFloors: Int? = null,
    
    // Amenities
    @SerializedName("amenities")
    val amenities: List<String>,
    
    @SerializedName("has_wifi")
    val hasWifi: Boolean = false,
    
    @SerializedName("has_ac")
    val hasAc: Boolean = false,
    
    @SerializedName("has_parking")
    val hasParking: Boolean = false,
    
    @SerializedName("has_laundry")
    val hasLaundry: Boolean = false,
    
    @SerializedName("has_mess")
    val hasMess: Boolean = false,
    
    @SerializedName("has_gym")
    val hasGym: Boolean = false,
    
    @SerializedName("has_study_room")
    val hasStudyRoom: Boolean = false,
    
    @SerializedName("has_power_backup")
    val hasPowerBackup: Boolean = false,
    
    // Images and media
    @SerializedName("images")
    val images: List<String>,
    
    @SerializedName("virtual_tour_url")
    val virtualTourUrl: String? = null,
    
    // Owner/Host details
    @SerializedName("owner_id")
    val ownerId: String,
    
    @SerializedName("owner_name")
    val ownerName: String,
    
    @SerializedName("owner_phone")
    val ownerPhone: String,
    
    @SerializedName("owner_email")
    val ownerEmail: String,
    
    @SerializedName("owner_verified")
    val ownerVerified: Boolean = false,
    
    // Ratings and reviews
    @SerializedName("rating")
    val rating: Float = 0.0f,
    
    @SerializedName("review_count")
    val reviewCount: Int = 0,
    
    // Availability and booking
    @SerializedName("is_available")
    val isAvailable: Boolean = true,
    
    @SerializedName("available_from")
    val availableFrom: String? = null,
    
    @SerializedName("minimum_stay_months")
    val minimumStayMonths: Int = 1,
    
    @SerializedName("maximum_stay_months")
    val maximumStayMonths: Int? = null,
    
    // Student-specific features
    @SerializedName("is_student_friendly")
    val isStudentFriendly: Boolean = true,
    
    @SerializedName("nearby_colleges")
    val nearbyColleges: List<String> = emptyList(),
    
    @SerializedName("distance_to_nearest_college_km")
    val distanceToNearestCollegeKm: Double? = null,
    
    @SerializedName("has_study_desk")
    val hasStudyDesk: Boolean = false,
    
    @SerializedName("allows_visitors")
    val allowsVisitors: Boolean = true,
    
    @SerializedName("curfew_time")
    val curfewTime: String? = null,
    
    // Meta information
    @SerializedName("created_at")
    val createdAt: Long = System.currentTimeMillis(),
    
    @SerializedName("updated_at")
    val updatedAt: Long = System.currentTimeMillis(),
    
    @SerializedName("is_featured")
    val isFeatured: Boolean = false,
    
    @SerializedName("is_premium")
    val isPremium: Boolean = false
)

/**
 * Types of accommodations available
 */
enum class AccommodationType {
    @SerializedName("pg")
    PG,
    
    @SerializedName("hostel")
    HOSTEL,
    
    @SerializedName("apartment")
    APARTMENT,
    
    @SerializedName("flat")
    FLAT,
    
    @SerializedName("shared_room")
    SHARED_ROOM
}

/**
 * Room sharing types
 */
enum class RoomType {
    @SerializedName("single")
    SINGLE,
    
    @SerializedName("double")
    DOUBLE,
    
    @SerializedName("triple")
    TRIPLE,
    
    @SerializedName("shared")
    SHARED,
    
    @SerializedName("dormitory")
    DORMITORY
}

/**
 * Gender preferences for accommodation
 */
enum class GenderPreference {
    @SerializedName("male")
    MALE,
    
    @SerializedName("female")
    FEMALE,
    
    @SerializedName("mixed")
    MIXED,
    
    @SerializedName("no_preference")
    NO_PREFERENCE
}