package com.aalay.app.data.repository

import com.aalay.app.data.local.dao.AccommodationDao
import com.aalay.app.data.local.dao.StudentPreferencesDao
import com.aalay.app.data.local.entity.AccommodationEntity
import com.aalay.app.data.local.entity.StudentPreferencesEntity
import com.aalay.app.data.remote.AccommodationApiService
import com.aalay.app.data.models.*
import com.aalay.app.utils.DistanceCalculator
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AccommodationRepository @Inject constructor(
    private val apiService: AccommodationApiService,
    private val accommodationDao: AccommodationDao,
    private val studentPreferencesDao: StudentPreferencesDao,
    private val distanceCalculator: DistanceCalculator
) {
    
    /**
     * Search accommodations with student-focused filters
     * Combines online search with offline caching for optimal performance
     */
    suspend fun searchAccommodations(
        location: LatLong,
        radius: Double = 10.0, // km
        filters: StudentSearchFilters
    ): Flow<Result<List<Accommodation>>> = flow {
        try {
            // Emit cached results first for immediate response
            val cachedResults = accommodationDao.searchNearbyAccommodations(
                latitude = location.latitude,
                longitude = location.longitude,
                radiusKm = radius,
                minPrice = filters.minBudget ?: 0.0,
                maxPrice = filters.maxBudget ?: Double.MAX_VALUE,
                accommodationType = filters.accommodationType,
                amenities = filters.requiredAmenities
            ).map { entities ->
                entities.map { it.toAccommodation() }
            }
            
            emit(Result.success(cachedResults.first()))
            
            // Fetch fresh data from API
            val apiResponse = apiService.searchAccommodations(
                latitude = location.latitude,
                longitude = location.longitude,
                radius = radius,
                dateFrom = filters.checkInDate,
                dateTo = filters.checkOutDate,
                accommodationType = filters.accommodationType,
                minBudget = filters.minBudget,
                maxBudget = filters.maxBudget,
                amenities = filters.requiredAmenities,
                roomType = filters.roomType,
                genderPreference = filters.genderPreference,
                collegeId = filters.collegeId,
                studentVerifiedOnly = filters.studentVerifiedOnly
            )
            
            if (apiResponse.isSuccessful) {
                val accommodations = apiResponse.body()?.accommodations ?: emptyList()
                
                // Calculate distances and sort by proximity
                val accommodationsWithDistance = accommodations.map { accommodation ->
                    val distance = distanceCalculator.calculateDistance(
                        location, 
                        LatLong(accommodation.latitude, accommodation.longitude)
                    )
                    accommodation.copy(distanceFromUser = distance)
                }.sortedBy { it.distanceFromUser }
                
                // Cache fresh results
                cacheAccommodations(accommodationsWithDistance)
                
                emit(Result.success(accommodationsWithDistance))
            } else {
                emit(Result.failure(Exception("Failed to fetch accommodations: ${apiResponse.message()}")))
            }
            
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }
    
    /**
     * Get accommodation details with enhanced student information
     */
    suspend fun getAccommodationDetails(accommodationId: String): Result<AccommodationDetails> {
        return try {
            val response = apiService.getAccommodationDetails(accommodationId)
            if (response.isSuccessful) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to load accommodation details"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get accommodations near specific college with student deals
     */
    suspend fun getAccommodationsNearCollege(
        collegeId: String,
        studentId: String?,
        limit: Int = 20
    ): Flow<Result<List<Accommodation>>> = flow {
        try {
            val response = apiService.getAccommodationsNearCollege(
                collegeId = collegeId,
                studentId = studentId,
                limit = limit,
                includeStudentDeals = true
            )
            
            if (response.isSuccessful) {
                val accommodations = response.body()?.accommodations ?: emptyList()
                emit(Result.success(accommodations))
                
                // Cache college-specific results
                cacheAccommodations(accommodations)
            } else {
                emit(Result.failure(Exception("Failed to fetch college accommodations")))
            }
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }
    
    /**
     * Get personalized recommendations based on student profile and history
     */
    suspend fun getPersonalizedRecommendations(
        studentId: String,
        location: LatLong?,
        limit: Int = 10
    ): Result<List<Accommodation>> {
        return try {
            val preferences = studentPreferencesDao.getPreferences(studentId)
            
            val response = apiService.getPersonalizedRecommendations(
                studentId = studentId,
                userLatitude = location?.latitude,
                userLongitude = location?.longitude,
                preferredBudget = preferences?.preferredBudget,
                preferredAmenities = preferences?.preferredAmenities,
                preferredColleges = preferences?.preferredColleges,
                limit = limit
            )
            
            if (response.isSuccessful) {
                Result.success(response.body()?.accommodations ?: emptyList())
            } else {
                Result.failure(Exception("Failed to load recommendations"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Book accommodation with student-friendly policies
     */
    suspend fun bookAccommodation(bookingRequest: BookingRequest): Result<BookingResponse> {
        return try {
            val response = apiService.bookAccommodation(bookingRequest)
            if (response.isSuccessful) {
                val bookingResponse = response.body()!!
                
                // Update user preferences based on booking
                updateStudentPreferences(bookingRequest)
                
                Result.success(bookingResponse)
            } else {
                Result.failure(Exception("Booking failed: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Submit booking (alias for bookAccommodation to support BookingViewModel)
     */
    suspend fun submitBooking(bookingRequest: BookingRequest): Result<BookingResponse> {
        return bookAccommodation(bookingRequest)
    }
    
    /**
     * Get student bookings with academic calendar integration
     */
    suspend fun getStudentBookings(
        studentId: String,
        status: BookingStatus? = null
    ): Result<List<Booking>> {
        return try {
            val response = apiService.getStudentBookings(studentId, status?.name)
            if (response.isSuccessful) {
                Result.success(response.body()?.bookings ?: emptyList())
            } else {
                Result.failure(Exception("Failed to load bookings"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Manage wishlist for students
     */
    suspend fun toggleWishlist(studentId: String, accommodationId: String): Result<Boolean> {
        return try {
            val response = apiService.toggleWishlist(studentId, accommodationId)
            if (response.isSuccessful) {
                Result.success(response.body()?.isWishlisted ?: false)
            } else {
                Result.failure(Exception("Failed to update wishlist"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getWishlist(studentId: String): Result<List<Accommodation>> {
        return try {
            val response = apiService.getWishlist(studentId)
            if (response.isSuccessful) {
                Result.success(response.body()?.accommodations ?: emptyList())
            } else {
                Result.failure(Exception("Failed to load wishlist"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Submit and get reviews with student verification badges
     */
    suspend fun submitReview(review: ReviewRequest): Result<Review> {
        return try {
            val response = apiService.submitReview(review)
            if (response.isSuccessful) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to submit review"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getAccommodationReviews(
        accommodationId: String,
        studentVerifiedOnly: Boolean = false
    ): Result<List<Review>> {
        return try {
            val response = apiService.getAccommodationReviews(accommodationId, studentVerifiedOnly)
            if (response.isSuccessful) {
                Result.success(response.body()?.reviews ?: emptyList())
            } else {
                Result.failure(Exception("Failed to load reviews"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Find potential roommates based on preferences and college
     */
    suspend fun findPotentialRoommates(
        studentId: String,
        accommodationId: String? = null,
        collegeId: String? = null
    ): Result<List<StudentProfile>> {
        return try {
            val response = apiService.findRoommates(studentId, accommodationId, collegeId)
            if (response.isSuccessful) {
                Result.success(response.body()?.potentialRoommates ?: emptyList())
            } else {
                Result.failure(Exception("Failed to find roommates"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get student deals and flash offers
     */
    suspend fun getStudentDeals(
        studentId: String,
        location: LatLong?,
        collegeId: String?
    ): Result<List<StudentDeal>> {
        return try {
            val response = apiService.getStudentDeals(
                studentId = studentId,
                latitude = location?.latitude,
                longitude = location?.longitude,
                collegeId = collegeId
            )
            if (response.isSuccessful) {
                Result.success(response.body()?.deals ?: emptyList())
            } else {
                Result.failure(Exception("Failed to load student deals"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Private helper methods
    
    private suspend fun cacheAccommodations(accommodations: List<Accommodation>) {
        try {
            val entities = accommodations.map { it.toAccommodationEntity() }
            accommodationDao.insertAccommodations(entities)
        } catch (e: Exception) {
            // Log error but don't fail the main operation
        }
    }
    
    private suspend fun updateStudentPreferences(bookingRequest: BookingRequest) {
        try {
            val currentPrefs = studentPreferencesDao.getPreferences(bookingRequest.studentId)
            val updatedPrefs = currentPrefs?.copy(
                lastBookedBudget = bookingRequest.totalAmount,
                preferredRoomType = bookingRequest.roomType,
                bookingHistory = currentPrefs.bookingHistory + bookingRequest.accommodationId,
                lastUpdated = System.currentTimeMillis()
            ) ?: StudentPreferencesEntity(
                studentId = bookingRequest.studentId,
                preferredBudget = bookingRequest.totalAmount.toDouble(),
                preferredRoomType = bookingRequest.roomType,
                bookingHistory = listOf(bookingRequest.accommodationId),
                lastUpdated = System.currentTimeMillis()
            )
            
            studentPreferencesDao.insertOrUpdatePreferences(updatedPrefs)
        } catch (e: Exception) {
            // Log error but don't fail the booking
        }
    }
    
    /**
     * Get offline cached accommodations for immediate display
     */
    fun getCachedAccommodationsNearby(
        location: LatLong,
        radiusKm: Double = 10.0
    ): Flow<List<Accommodation>> {
        return accommodationDao.searchNearbyAccommodations(
            latitude = location.latitude,
            longitude = location.longitude,
            radiusKm = radiusKm
        ).map { entities ->
            entities.map { entity ->
                entity.toAccommodation().copy(
                    distanceFromUser = distanceCalculator.calculateDistance(
                        location,
                        LatLong(entity.latitude, entity.longitude)
                    )
                )
            }.sortedBy { it.distanceFromUser }
        }
    }
    
    /**
     * Clear old cached data to manage storage
     */
    suspend fun clearOldCache(maxAgeMs: Long = 7 * 24 * 60 * 60 * 1000L) { // 7 days
        accommodationDao.clearOldCache(System.currentTimeMillis() - maxAgeMs)
    }
}

// Extension functions for entity conversion
private fun AccommodationEntity.toAccommodation(): Accommodation {
    return Accommodation(
        id = this.id,
        title = this.title,
        description = this.description,
        accommodationType = this.accommodationType,
        latitude = this.latitude,
        longitude = this.longitude,
        pricePerMonth = this.pricePerMonth,
        securityDeposit = this.securityDeposit,
        amenities = this.amenities,
        photos = this.photos,
        rating = this.rating,
        reviewCount = this.reviewCount,
        hostId = this.hostId,
        hostName = this.hostName,
        isStudentFriendly = this.isStudentFriendly,
        distanceToCollege = this.distanceToCollege,
        collegeId = this.collegeId,
        availableFrom = this.availableFrom,
        genderPreference = this.genderPreference,
        roomType = this.roomType,
        isStudentVerified = this.isStudentVerified,
        hasStudentDeals = this.hasStudentDeals
    )
}

private fun Accommodation.toAccommodationEntity(): AccommodationEntity {
    return AccommodationEntity(
        id = this.id,
        title = this.title,
        description = this.description,
        accommodationType = this.accommodationType,
        latitude = this.latitude,
        longitude = this.longitude,
        pricePerMonth = this.pricePerMonth,
        securityDeposit = this.securityDeposit,
        amenities = this.amenities,
        photos = this.photos,
        rating = this.rating,
        reviewCount = this.reviewCount,
        hostId = this.hostId,
        hostName = this.hostName,
        isStudentFriendly = this.isStudentFriendly,
        distanceToCollege = this.distanceToCollege,
        collegeId = this.collegeId,
        availableFrom = this.availableFrom,
        genderPreference = this.genderPreference,
        roomType = this.roomType,
        isStudentVerified = this.isStudentVerified,
        hasStudentDeals = this.hasStudentDeals,
        cachedAt = System.currentTimeMillis()
    )
}