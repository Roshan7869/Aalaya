package com.aalay.app.domain

import com.aalay.app.data.models.Accommodation
import com.aalay.app.data.models.LatLong
import com.aalay.app.data.repository.AccommodationRepository
import com.aalay.app.utils.DistanceCalculator
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import kotlin.math.*

data class SearchFilters(
    val budget: IntRange? = null,
    val accommodationType: List<String> = emptyList(), // Hostels, Apartments, Flats, PG
    val amenities: List<String> = emptyList(), // WiFi, study desk, laundry, mess/food, gym
    val roomType: String? = null, // single, shared, gender-specific
    val minRating: Float? = null,
    val maxDistanceKm: Double? = null,
    val genderPreference: String? = null
)

data class SearchResult(
    val accommodation: Accommodation,
    val distanceKm: Double,
    val distanceText: String
)

class SearchNearCollegeUseCase @Inject constructor(
    private val accommodationRepository: AccommodationRepository,
    private val distanceCalculator: DistanceCalculator
) {

    suspend operator fun invoke(
        collegeLocation: LatLong,
        filters: SearchFilters = SearchFilters(),
        maxRadius: Double = 10.0 // Default 10km radius
    ): Flow<Result<List<SearchResult>>> {
        return try {
            accommodationRepository.searchAccommodations(
                latitude = collegeLocation.latitude,
                longitude = collegeLocation.longitude,
                radius = maxRadius,
                filters = mapFiltersToApiParams(filters)
            ).map { accommodations ->
                val filteredResults = accommodations
                    .map { accommodation ->
                        val distance = distanceCalculator.calculateDistance(
                            startLat = collegeLocation.latitude,
                            startLng = collegeLocation.longitude,
                            endLat = accommodation.location.latitude,
                            endLng = accommodation.location.longitude
                        )
                        
                        SearchResult(
                            accommodation = accommodation,
                            distanceKm = distance,
                            distanceText = formatDistance(distance)
                        )
                    }
                    .filter { result -> 
                        applyLocalFilters(result, filters, maxRadius)
                    }
                    .sortedBy { it.distanceKm } // Sort by distance (closest first)

                Result.success(filteredResults)
            }
        } catch (e: Exception) {
            kotlinx.coroutines.flow.flowOf(Result.failure(e))
        }
    }

    /**
     * Search accommodations near multiple colleges (for students attending multiple campuses)
     */
    suspend fun searchNearMultipleColleges(
        collegeLocations: List<LatLong>,
        filters: SearchFilters = SearchFilters(),
        maxRadius: Double = 10.0
    ): Flow<Result<List<SearchResult>>> {
        return try {
            if (collegeLocations.isEmpty()) {
                return kotlinx.coroutines.flow.flowOf(Result.success(emptyList()))
            }

            // Find center point of all colleges
            val centerLat = collegeLocations.map { it.latitude }.average()
            val centerLng = collegeLocations.map { it.longitude }.average()
            val centerLocation = LatLong(centerLat, centerLng)

            accommodationRepository.searchAccommodations(
                latitude = centerLat,
                longitude = centerLng,
                radius = maxRadius * 1.5, // Expand radius for multiple colleges
                filters = mapFiltersToApiParams(filters)
            ).map { accommodations ->
                val filteredResults = accommodations
                    .map { accommodation ->
                        // Find distance to nearest college
                        val distancesToColleges = collegeLocations.map { college ->
                            distanceCalculator.calculateDistance(
                                startLat = college.latitude,
                                startLng = college.longitude,
                                endLat = accommodation.location.latitude,
                                endLng = accommodation.location.longitude
                            )
                        }
                        
                        val minDistance = distancesToColleges.minOrNull() ?: Double.MAX_VALUE
                        
                        SearchResult(
                            accommodation = accommodation,
                            distanceKm = minDistance,
                            distanceText = "${formatDistance(minDistance)} from nearest campus"
                        )
                    }
                    .filter { result -> 
                        applyLocalFilters(result, filters, maxRadius)
                    }
                    .sortedBy { it.distanceKm }

                Result.success(filteredResults)
            }
        } catch (e: Exception) {
            kotlinx.coroutines.flow.flowOf(Result.failure(e))
        }
    }

    /**
     * Get accommodations sorted by proximity to user's preferred colleges
     */
    suspend fun getRecommendedNearCollege(
        userLocation: LatLong,
        collegeLocation: LatLong,
        userPreferences: SearchFilters,
        limit: Int = 20
    ): Flow<Result<List<SearchResult>>> {
        return try {
            // Create a weighted search considering both user location and college location
            val midpointLat = (userLocation.latitude + collegeLocation.latitude) / 2
            val midpointLng = (userLocation.longitude + collegeLocation.longitude) / 2
            
            accommodationRepository.searchAccommodations(
                latitude = midpointLat,
                longitude = midpointLng,
                radius = 15.0, // Wider radius for recommendations
                filters = mapFiltersToApiParams(userPreferences)
            ).map { accommodations ->
                val scoredResults = accommodations
                    .map { accommodation ->
                        val distanceToCollege = distanceCalculator.calculateDistance(
                            startLat = collegeLocation.latitude,
                            startLng = collegeLocation.longitude,
                            endLat = accommodation.location.latitude,
                            endLng = accommodation.location.longitude
                        )
                        
                        val distanceToUser = distanceCalculator.calculateDistance(
                            startLat = userLocation.latitude,
                            startLng = userLocation.longitude,
                            endLat = accommodation.location.latitude,
                            endLng = accommodation.location.longitude
                        )

                        // Calculate recommendation score (lower is better)
                        val score = calculateRecommendationScore(
                            accommodation,
                            distanceToCollege,
                            distanceToUser,
                            userPreferences
                        )

                        SearchResult(
                            accommodation = accommodation.copy(
                                // Add recommendation score as custom field
                                customFields = accommodation.customFields?.plus("recommendationScore" to score.toString())
                                    ?: mapOf("recommendationScore" to score.toString())
                            ),
                            distanceKm = distanceToCollege,
                            distanceText = formatDistance(distanceToCollege)
                        )
                    }
                    .sortedBy { 
                        it.accommodation.customFields?.get("recommendationScore")?.toDoubleOrNull() ?: Double.MAX_VALUE
                    }
                    .take(limit)

                Result.success(scoredResults)
            }
        } catch (e: Exception) {
            kotlinx.coroutines.flow.flowOf(Result.failure(e))
        }
    }

    private fun applyLocalFilters(
        result: SearchResult,
        filters: SearchFilters,
        maxRadius: Double
    ): Boolean {
        val accommodation = result.accommodation
        
        // Distance filter
        if (result.distanceKm > maxRadius) return false
        
        // Budget filter
        filters.budget?.let { budget ->
            if (accommodation.pricePerMonth !in budget) return false
        }
        
        // Max distance filter
        filters.maxDistanceKm?.let { maxDistance ->
            if (result.distanceKm > maxDistance) return false
        }
        
        // Accommodation type filter
        if (filters.accommodationType.isNotEmpty()) {
            if (accommodation.type !in filters.accommodationType) return false
        }
        
        // Amenities filter
        if (filters.amenities.isNotEmpty()) {
            val hasAllAmenities = filters.amenities.all { requiredAmenity ->
                accommodation.amenities.any { amenity -> 
                    amenity.equals(requiredAmenity, ignoreCase = true)
                }
            }
            if (!hasAllAmenities) return false
        }
        
        // Room type filter
        filters.roomType?.let { roomType ->
            if (!accommodation.roomType.equals(roomType, ignoreCase = true)) return false
        }
        
        // Rating filter
        filters.minRating?.let { minRating ->
            if (accommodation.rating < minRating) return false
        }
        
        // Gender preference filter
        filters.genderPreference?.let { preference ->
            accommodation.genderPreference?.let { accPreference ->
                if (!accPreference.equals(preference, ignoreCase = true) && 
                    !accPreference.equals("any", ignoreCase = true)) return false
            }
        }
        
        return true
    }

    private fun calculateRecommendationScore(
        accommodation: Accommodation,
        distanceToCollege: Double,
        distanceToUser: Double,
        preferences: SearchFilters
    ): Double {
        var score = 0.0
        
        // Distance to college weight (40%)
        score += distanceToCollege * 0.4
        
        // Distance to user weight (20%)
        score += distanceToUser * 0.2
        
        // Price factor (20%) - lower price is better
        val priceScore = accommodation.pricePerMonth / 1000.0 // Normalize price
        score += priceScore * 0.2
        
        // Rating factor (10%) - higher rating is better
        val ratingScore = (5.0 - accommodation.rating) // Invert rating (lower score is better)
        score += ratingScore * 0.1
        
        // Amenities match (10%)
        val amenityMatchScore = if (preferences.amenities.isNotEmpty()) {
            val matchCount = preferences.amenities.count { prefAmenity ->
                accommodation.amenities.any { amenity -> 
                    amenity.equals(prefAmenity, ignoreCase = true)
                }
            }
            val matchRatio = matchCount.toDouble() / preferences.amenities.size
            (1.0 - matchRatio) // Lower score for better match
        } else {
            0.0
        }
        score += amenityMatchScore * 0.1
        
        return score
    }

    private fun formatDistance(distanceKm: Double): String {
        return when {
            distanceKm < 0.1 -> "< 100m"
            distanceKm < 1.0 -> "${(distanceKm * 1000).roundToInt()}m"
            else -> "${String.format("%.1f", distanceKm)}km"
        }
    }

    private fun mapFiltersToApiParams(filters: SearchFilters): Map<String, Any> {
        val params = mutableMapOf<String, Any>()
        
        filters.budget?.let {
            params["min_price"] = it.first
            params["max_price"] = it.last
        }
        
        if (filters.accommodationType.isNotEmpty()) {
            params["accommodation_types"] = filters.accommodationType
        }
        
        if (filters.amenities.isNotEmpty()) {
            params["amenities"] = filters.amenities
        }
        
        filters.roomType?.let {
            params["room_type"] = it
        }
        
        filters.minRating?.let {
            params["min_rating"] = it
        }
        
        filters.genderPreference?.let {
            params["gender_preference"] = it
        }
        
        return params
    }
}