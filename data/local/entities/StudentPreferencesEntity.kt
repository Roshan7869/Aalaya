package com.aalay.app.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity for storing student preferences and search history
 * Enables personalized recommendations and quick access to preferred criteria
 */
@Entity(tableName = "student_preferences")
data class StudentPreferencesEntity(
    @PrimaryKey
    val userId: String,
    
    // College/University details
    val collegeName: String?,
    val collegeLatitude: Double?,
    val collegeLongitude: Double?,
    val collegeAddress: String?,
    val studentEmailDomain: String?, // e.g., "@university.edu"
    val studentId: String?,
    val graduationYear: Int?,
    val course: String?,
    
    // Budget preferences
    val minBudget: Double,
    val maxBudget: Double,
    val preferredPaymentMethod: String?, // "UPI", "Card", "Wallet"
    val budgetFlexibility: String, // "Strict", "Flexible"
    
    // Accommodation preferences
    val preferredAccommodationType: List<String>, // ["PG", "Hostel", "Apartment"]
    val preferredRoomType: String, // "Single", "Shared", "Any"
    val genderPreference: String?, // "Male", "Female", "Any"
    val maxDistanceFromCollege: Double, // in kilometers
    
    // Essential amenities (student-focused)
    val requiresWifi: Boolean,
    val requiresStudyDesk: Boolean,
    val requiresLaundry: Boolean,
    val requiresMessFood: Boolean,
    val requiresGym: Boolean,
    val requiresParking: Boolean,
    val requiresAC: Boolean,
    
    // Lifestyle preferences
    val preferredCurfewTime: String?, // "No Curfew", "22:00", "23:00"
    val allowsVisitors: Boolean,
    val prefersSameGenderRoomate: Boolean,
    val roommatePreferences: List<String>, // ["Non-Smoker", "Quiet", "Social"]
    
    // Search behavior data
    val frequentSearchLocations: List<String>,
    val lastSearchLatitude: Double?,
    val lastSearchLongitude: Double?,
    val searchHistory: List<String>, // Recent search terms
    val bookingHistory: List<String>, // Previous accommodation IDs
    
    // Notification preferences
    val enablePriceAlerts: Boolean,
    val enableNewListingAlerts: Boolean,
    val enableBookingReminders: Boolean,
    val alertRadius: Double, // in kilometers from college
    
    // App usage patterns
    val preferredSearchTime: String?, // "Morning", "Evening", "Night"
    val averageSessionDuration: Long, // in minutes
    val lastActiveAt: Long, // timestamp
    val totalBookings: Int,
    val averageStayDuration: Int, // in months
    
    // Social and community preferences
    val interestedInStudyGroups: Boolean,
    val interestedInEvents: Boolean,
    val shareContactWithRoommates: Boolean,
    val participateInCommunityForums: Boolean,
    
    // Academic preferences
    val examPeriodBookingFlexibility: Boolean,
    val vacationPeriodPreferences: String?, // "Stay", "Leave", "Flexible"
    val internshipLocationPreferences: List<String>,
    
    // Metadata
    val createdAt: Long,
    val updatedAt: Long,
    val syncedAt: Long? // Last server sync timestamp
)