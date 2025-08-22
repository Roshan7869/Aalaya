package com.aalay.app.data.models

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

/**
 * Student user model for authentication and profile management
 */
@Entity(tableName = "student_users")
data class StudentUser(
    @PrimaryKey
    @SerializedName("id")
    val id: String,
    
    // Basic user information
    @SerializedName("email")
    val email: String,
    
    @SerializedName("phone")
    val phone: String? = null,
    
    @SerializedName("first_name")
    val firstName: String,
    
    @SerializedName("last_name")
    val lastName: String,
    
    @SerializedName("profile_image_url")
    val profileImageUrl: String? = null,
    
    @SerializedName("date_of_birth")
    val dateOfBirth: String? = null,
    
    @SerializedName("gender")
    val gender: Gender? = null,
    
    // Student-specific information
    @SerializedName("student_id")
    val studentId: String? = null,
    
    @SerializedName("college_name")
    val collegeName: String? = null,
    
    @SerializedName("college_email")
    val collegeEmail: String? = null,
    
    @SerializedName("course")
    val course: String? = null,
    
    @SerializedName("year_of_study")
    val yearOfStudy: Int? = null,
    
    @SerializedName("graduation_year")
    val graduationYear: Int? = null,
    
    @SerializedName("field_of_study")
    val fieldOfStudy: String? = null,
    
    // Location preferences
    @Embedded(prefix = "current_location_")
    @SerializedName("current_location")
    val currentLocation: LatLong? = null,
    
    @Embedded(prefix = "college_location_")
    @SerializedName("college_location")
    val collegeLocation: LatLong? = null,
    
    @SerializedName("preferred_cities")
    val preferredCities: List<String> = emptyList(),
    
    // Accommodation preferences
    @SerializedName("budget_min")
    val budgetMin: Double? = null,
    
    @SerializedName("budget_max")
    val budgetMax: Double? = null,
    
    @SerializedName("preferred_accommodation_types")
    val preferredAccommodationTypes: List<AccommodationType> = emptyList(),
    
    @SerializedName("preferred_room_type")
    val preferredRoomType: RoomType? = null,
    
    @SerializedName("gender_preference")
    val genderPreference: GenderPreference? = null,
    
    @SerializedName("max_distance_to_college_km")
    val maxDistanceToCollegeKm: Double? = null,
    
    @SerializedName("required_amenities")
    val requiredAmenities: List<String> = emptyList(),
    
    @SerializedName("preferred_amenities")
    val preferredAmenities: List<String> = emptyList(),
    
    // Personal preferences for roommate matching
    @SerializedName("interests")
    val interests: List<String> = emptyList(),
    
    @SerializedName("lifestyle_preferences")
    val lifestylePreferences: LifestylePreferences? = null,
    
    @SerializedName("study_habits")
    val studyHabits: StudyHabits? = null,
    
    // Verification status
    @SerializedName("is_student_verified")
    val isStudentVerified: Boolean = false,
    
    @SerializedName("is_email_verified")
    val isEmailVerified: Boolean = false,
    
    @SerializedName("is_phone_verified")
    val isPhoneVerified: Boolean = false,
    
    @SerializedName("verification_document_url")
    val verificationDocumentUrl: String? = null,
    
    // Account status and preferences
    @SerializedName("is_premium")
    val isPremium: Boolean = false,
    
    @SerializedName("notification_preferences")
    val notificationPreferences: NotificationPreferences = NotificationPreferences(),
    
    @SerializedName("privacy_settings")
    val privacySettings: PrivacySettings = PrivacySettings(),
    
    // Authentication
    @SerializedName("auth_provider")
    val authProvider: AuthProvider = AuthProvider.EMAIL,
    
    @SerializedName("provider_id")
    val providerId: String? = null,
    
    // Meta information
    @SerializedName("created_at")
    val createdAt: Long = System.currentTimeMillis(),
    
    @SerializedName("updated_at")
    val updatedAt: Long = System.currentTimeMillis(),
    
    @SerializedName("last_login")
    val lastLogin: Long? = null,
    
    @SerializedName("is_active")
    val isActive: Boolean = true
) {
    /**
     * Get full name of the user
     */
    fun getFullName(): String = "$firstName $lastName"
    
    /**
     * Check if user has completed student verification
     */
    fun isFullyVerified(): Boolean = isStudentVerified && isEmailVerified
    
    /**
     * Get display email (prefer college email if available)
     */
    fun getDisplayEmail(): String = collegeEmail ?: email
}

/**
 * Gender enumeration
 */
enum class Gender {
    @SerializedName("male")
    MALE,
    
    @SerializedName("female")
    FEMALE,
    
    @SerializedName("other")
    OTHER,
    
    @SerializedName("prefer_not_to_say")
    PREFER_NOT_TO_SAY
}

/**
 * Authentication provider options
 */
enum class AuthProvider {
    @SerializedName("email")
    EMAIL,
    
    @SerializedName("google")
    GOOGLE,
    
    @SerializedName("facebook")
    FACEBOOK,
    
    @SerializedName("phone")
    PHONE
}

/**
 * Lifestyle preferences for roommate matching
 */
data class LifestylePreferences(
    @SerializedName("smoking")
    val smoking: Boolean = false,
    
    @SerializedName("drinking")
    val drinking: Boolean = false,
    
    @SerializedName("vegetarian")
    val vegetarian: Boolean? = null,
    
    @SerializedName("early_riser")
    val earlyRiser: Boolean? = null,
    
    @SerializedName("night_owl")
    val nightOwl: Boolean? = null,
    
    @SerializedName("social_level")
    val socialLevel: SocialLevel? = null,
    
    @SerializedName("cleanliness_level")
    val cleanlinessLevel: CleanlinessLevel? = null,
    
    @SerializedName("music_preferences")
    val musicPreferences: List<String> = emptyList()
)

/**
 * Study habits for academic compatibility
 */
data class StudyHabits(
    @SerializedName("study_hours_per_day")
    val studyHoursPerDay: Int? = null,
    
    @SerializedName("preferred_study_time")
    val preferredStudyTime: StudyTime? = null,
    
    @SerializedName("study_environment")
    val studyEnvironment: StudyEnvironment? = null,
    
    @SerializedName("group_study_preference")
    val groupStudyPreference: Boolean? = null,
    
    @SerializedName("exam_stress_level")
    val examStressLevel: StressLevel? = null
)

/**
 * Notification preferences
 */
data class NotificationPreferences(
    @SerializedName("push_notifications")
    val pushNotifications: Boolean = true,
    
    @SerializedName("email_notifications")
    val emailNotifications: Boolean = true,
    
    @SerializedName("sms_notifications")
    val smsNotifications: Boolean = false,
    
    @SerializedName("new_listings_near_college")
    val newListingsNearCollege: Boolean = true,
    
    @SerializedName("price_drop_alerts")
    val priceDropAlerts: Boolean = true,
    
    @SerializedName("booking_confirmations")
    val bookingConfirmations: Boolean = true,
    
    @SerializedName("roommate_match_alerts")
    val roommateMatchAlerts: Boolean = true,
    
    @SerializedName("promotional_offers")
    val promotionalOffers: Boolean = true
)

/**
 * Privacy settings
 */
data class PrivacySettings(
    @SerializedName("profile_visibility")
    val profileVisibility: ProfileVisibility = ProfileVisibility.STUDENTS_ONLY,
    
    @SerializedName("show_phone_number")
    val showPhoneNumber: Boolean = false,
    
    @SerializedName("show_college_details")
    val showCollegeDetails: Boolean = true,
    
    @SerializedName("allow_roommate_matching")
    val allowRoommateMatching: Boolean = true,
    
    @SerializedName("show_in_search")
    val showInSearch: Boolean = true
)

// Supporting enums
enum class SocialLevel {
    @SerializedName("very_social")
    VERY_SOCIAL,
    
    @SerializedName("moderately_social")
    MODERATELY_SOCIAL,
    
    @SerializedName("prefer_quiet")
    PREFER_QUIET
}

enum class CleanlinessLevel {
    @SerializedName("very_clean")
    VERY_CLEAN,
    
    @SerializedName("moderately_clean")
    MODERATELY_CLEAN,
    
    @SerializedName("flexible")
    FLEXIBLE
}

enum class StudyTime {
    @SerializedName("early_morning")
    EARLY_MORNING,
    
    @SerializedName("morning")
    MORNING,
    
    @SerializedName("afternoon")
    AFTERNOON,
    
    @SerializedName("evening")
    EVENING,
    
    @SerializedName("late_night")
    LATE_NIGHT
}

enum class StudyEnvironment {
    @SerializedName("complete_silence")
    COMPLETE_SILENCE,
    
    @SerializedName("light_background_noise")
    LIGHT_BACKGROUND_NOISE,
    
    @SerializedName("music_while_studying")
    MUSIC_WHILE_STUDYING
}

enum class StressLevel {
    @SerializedName("low")
    LOW,
    
    @SerializedName("moderate")
    MODERATE,
    
    @SerializedName("high")
    HIGH
}

enum class ProfileVisibility {
    @SerializedName("public")
    PUBLIC,
    
    @SerializedName("students_only")
    STUDENTS_ONLY,
    
    @SerializedName("verified_students_only")
    VERIFIED_STUDENTS_ONLY,
    
    @SerializedName("private")
    PRIVATE
}