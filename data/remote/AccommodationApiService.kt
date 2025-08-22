package com.aalay.app.data.remote

import com.aalay.app.data.models.Accommodation
import com.aalay.app.data.models.BookingRequest
import com.aalay.app.data.models.BookingResponse
import com.aalay.app.data.models.SearchRequest
import com.aalay.app.data.models.SearchResponse
import retrofit2.Response
import retrofit2.http.*

/**
 * Retrofit API service for accommodation-related backend operations
 * Handles search, booking, and student-specific features
 */
interface AccommodationApiService {
    
    // ============ SEARCH & DISCOVERY ============
    
    @POST("api/v1/accommodations/search")
    suspend fun searchAccommodations(
        @Body searchRequest: SearchRequest
    ): Response<SearchResponse>
    
    @GET("api/v1/accommodations/nearby")
    suspend fun getNearbyAccommodations(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("radius") radiusKm: Double,
        @Query("limit") limit: Int = 20,
        @Query("offset") offset: Int = 0,
        @Query("student_friendly") studentFriendly: Boolean = true
    ): Response<SearchResponse>
    
    @GET("api/v1/accommodations/college/{collegeId}")
    suspend fun getAccommodationsNearCollege(
        @Path("collegeId") collegeId: String,
        @Query("max_distance") maxDistanceKm: Double,
        @Query("budget_min") minBudget: Double? = null,
        @Query("budget_max") maxBudget: Double? = null,
        @Query("accommodation_types") types: List<String>? = null
    ): Response<SearchResponse>
    
    @GET("api/v1/accommodations/recommendations/{userId}")
    suspend fun getPersonalizedRecommendations(
        @Path("userId") userId: String,
        @Query("limit") limit: Int = 10
    ): Response<SearchResponse>
    
    // ============ ACCOMMODATION DETAILS ============
    
    @GET("api/v1/accommodations/{accommodationId}")
    suspend fun getAccommodationDetails(
        @Path("accommodationId") accommodationId: String,
        @Query("user_id") userId: String? = null // For personalized details
    ): Response<Accommodation>
    
    @GET("api/v1/accommodations/{accommodationId}/reviews")
    suspend fun getAccommodationReviews(
        @Path("accommodationId") accommodationId: String,
        @Query("student_only") studentOnly: Boolean = false,
        @Query("limit") limit: Int = 20,
        @Query("offset") offset: Int = 0
    ): Response<ReviewsResponse>
    
    @GET("api/v1/accommodations/{accommodationId}/availability")
    suspend fun checkAvailability(
        @Path("accommodationId") accommodationId: String,
        @Query("check_in") checkInDate: String, // ISO format
        @Query("duration_months") durationMonths: Int
    ): Response<AvailabilityResponse>
    
    // ============ BOOKING MANAGEMENT ============
    
    @POST("api/v1/bookings")
    suspend fun createBooking(
        @Body bookingRequest: BookingRequest,
        @Header("Authorization") authToken: String
    ): Response<BookingResponse>
    
    @GET("api/v1/bookings/user/{userId}")
    suspend fun getUserBookings(
        @Path("userId") userId: String,
        @Header("Authorization") authToken: String,
        @Query("status") status: String? = null, // "active", "completed", "cancelled"
        @Query("limit") limit: Int = 20
    ): Response<BookingsResponse>
    
    @PUT("api/v1/bookings/{bookingId}/cancel")
    suspend fun cancelBooking(
        @Path("bookingId") bookingId: String,
        @Body cancellationRequest: CancellationRequest,
        @Header("Authorization") authToken: String
    ): Response<BookingResponse>
    
    @PUT("api/v1/bookings/{bookingId}/extend")
    suspend fun extendBooking(
        @Path("bookingId") bookingId: String,
        @Body extensionRequest: ExtensionRequest,
        @Header("Authorization") authToken: String
    ): Response<BookingResponse>
    
    // ============ STUDENT-SPECIFIC FEATURES ============
    
    @POST("api/v1/users/{userId}/student-verification")
    suspend fun submitStudentVerification(
        @Path("userId") userId: String,
        @Body verificationData: StudentVerificationRequest,
        @Header("Authorization") authToken: String
    ): Response<VerificationResponse>
    
    @GET("api/v1/accommodations/student-deals")
    suspend fun getStudentDeals(
        @Query("user_id") userId: String,
        @Query("location_lat") latitude: Double? = null,
        @Query("location_lng") longitude: Double? = null,
        @Query("college_id") collegeId: String? = null
    ): Response<SearchResponse>
    
    @POST("api/v1/roommate-matching")
    suspend fun findRoommates(
        @Body matchingRequest: RoommateMatchingRequest,
        @Header("Authorization") authToken: String
    ): Response<RoommateMatchingResponse>
    
    @GET("api/v1/colleges/search")
    suspend fun searchColleges(
        @Query("query") query: String,
        @Query("latitude") latitude: Double? = null,
        @Query("longitude") longitude: Double? = null,
        @Query("radius") radiusKm: Double? = null
    ): Response<CollegeSearchResponse>
    
    // ============ USER PREFERENCES & WISHLIST ============
    
    @POST("api/v1/users/{userId}/wishlist")
    suspend fun addToWishlist(
        @Path("userId") userId: String,
        @Body wishlistRequest: WishlistRequest,
        @Header("Authorization") authToken: String
    ): Response<WishlistResponse>
    
    @DELETE("api/v1/users/{userId}/wishlist/{accommodationId}")
    suspend fun removeFromWishlist(
        @Path("userId") userId: String,
        @Path("accommodationId") accommodationId: String,
        @Header("Authorization") authToken: String
    ): Response<WishlistResponse>
    
    @GET("api/v1/users/{userId}/wishlist")
    suspend fun getWishlist(
        @Path("userId") userId: String,
        @Header("Authorization") authToken: String
    ): Response<SearchResponse>
    
    // ============ REVIEWS & RATINGS ============
    
    @POST("api/v1/accommodations/{accommodationId}/reviews")
    suspend fun submitReview(
        @Path("accommodationId") accommodationId: String,
        @Body reviewRequest: ReviewRequest,
        @Header("Authorization") authToken: String
    ): Response<ReviewResponse>
    
    @PUT("api/v1/reviews/{reviewId}")
    suspend fun updateReview(
        @Path("reviewId") reviewId: String,
        @Body reviewRequest: ReviewRequest,
        @Header("Authorization") authToken: String
    ): Response<ReviewResponse>
    
    // ============ NOTIFICATIONS & ALERTS ============
    
    @POST("api/v1/users/{userId}/price-alerts")
    suspend fun createPriceAlert(
        @Path("userId") userId: String,
        @Body alertRequest: PriceAlertRequest,
        @Header("Authorization") authToken: String
    ): Response<AlertResponse>
    
    @GET("api/v1/users/{userId}/notifications")
    suspend fun getNotifications(
        @Path("userId") userId: String,
        @Header("Authorization") authToken: String,
        @Query("unread_only") unreadOnly: Boolean = false,
        @Query("limit") limit: Int = 50
    ): Response<NotificationsResponse>
    
    @PUT("api/v1/notifications/{notificationId}/read")
    suspend fun markNotificationAsRead(
        @Path("notificationId") notificationId: String,
        @Header("Authorization") authToken: String
    ): Response<NotificationResponse>
    
    // ============ ANALYTICS & INSIGHTS ============
    
    @POST("api/v1/analytics/search")
    suspend fun trackSearchEvent(
        @Body searchAnalytics: SearchAnalyticsRequest
    ): Response<AnalyticsResponse>
    
    @POST("api/v1/analytics/view")
    suspend fun trackAccommodationView(
        @Body viewAnalytics: ViewAnalyticsRequest
    ): Response<AnalyticsResponse>
    
    @GET("api/v1/analytics/trends")
    suspend fun getTrendingAccommodations(
        @Query("location_lat") latitude: Double,
        @Query("location_lng") longitude: Double,
        @Query("radius") radiusKm: Double,
        @Query("time_period") timePeriod: String = "week" // "day", "week", "month"
    ): Response<SearchResponse>
}

// ============ DATA CLASSES FOR API RESPONSES ============

data class ReviewsResponse(
    val success: Boolean,
    val reviews: List<Review>,
    val totalCount: Int,
    val averageRating: Float
)

data class AvailabilityResponse(
    val success: Boolean,
    val available: Boolean,
    val availableFrom: String?,
    val availableRooms: Int,
    val pricing: PricingInfo?,
    val restrictions: List<String>
)

data class BookingsResponse(
    val success: Boolean,
    val bookings: List<BookingResponse>,
    val totalCount: Int
)

data class VerificationResponse(
    val success: Boolean,
    val verificationStatus: String, // "pending", "approved", "rejected"
    val message: String,
    val benefits: List<String>? // Available student benefits
)

data class RoommateMatchingResponse(
    val success: Boolean,
    val matches: List<RoommateMatch>,
    val totalMatches: Int
)

data class CollegeSearchResponse(
    val success: Boolean,
    val colleges: List<College>,
    val totalCount: Int
)

data class WishlistResponse(
    val success: Boolean,
    val message: String,
    val totalWishlistItems: Int
)

data class AlertResponse(
    val success: Boolean,
    val alertId: String,
    val message: String
)

data class NotificationsResponse(
    val success: Boolean,
    val notifications: List<NotificationItem>,
    val unreadCount: Int,
    val totalCount: Int
)

data class NotificationResponse(
    val success: Boolean,
    val message: String
)

data class AnalyticsResponse(
    val success: Boolean,
    val message: String
)