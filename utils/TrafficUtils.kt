/**
     * Get travel mode duration comparison for Chhattisgarh cities
     * @param walkingDuration Walking duration in seconds
     * @param cyclingDuration Cycling duration in seconds  
     * @param autoRickshawDuration Auto-rickshaw duration in seconds (common in CG)
     * @param busDuration City bus duration in seconds
     * @param bikeRideDuration Bike/scooter ride duration in seconds
     * @return List of travel mode options with durations
     */
    fun getCGTravelModeComparison(
        walkingDuration: Double? = null,
        cyclingDuration: Double? = null,
        autoRickshawDuration: Double? = null,
        busDuration: Double? = null,
        bikeRideDuration: Double? = null
    ): List<TravelMode> {
        val modes = mutableListOf<TravelMode>()
        
        walkingDuration?.let {
            modes.add(TravelMode(
                type = "Walking",
                duration = formatDuration(it),
                durationSeconds = it,
                icon = "🚶",
                color = "#4CAF50",
                description = "Best for short distances in CG heat"
            ))
        }
        
        cyclingDuration?.let {
            modes.add(TravelMode(
                type = "Cycling",
                duration = formatDuration(it),
                durationSeconds = it,
                icon = "🚴",
                color = "#2196F3",
                description = "Eco-friendly, avoid peak summer hours"
            ))
        }
        
        autoRickshawDuration?.let {
            modes.add(TravelMode(
                type = "Auto Rickshaw",
                duration = formatDuration(it),
                durationSeconds = it,
                icon = "🛺",
                color = "#FF9800",
                description = "Most popular in CG cities, ₹10-15/km"
            ))
        }
        
        bikeRideDuration?.let {
            modes.add(TravelMode(
                type = "Bike/Scooter",
                duration = formatDuration(it),
                durationSeconds = it,
                icon = "🏍️",
                color = "#9C27B0",
                description = "Quick and convenient for CG roads"
            ))
        }
        
        busDuration?.let {
            modes.add(TravelMode(
                type = "City Bus",
                duration = formatDuration(it),
                durationSeconds = it,
                icon = "🚌",
                color = "#607D8B",
                description = "Budget-friendly, ₹5-10 fare in CG"
            ))
        }
        
        return modes.sortedBy { it.durationSeconds }
    }
    
    /**
     * Get CG-specific traffic alert message
     * @param trafficStatus Current traffic status
     * @param cityName City in Chhattisgarh
     * @param isStudentCommute Whether this is for student commute
     * @return Alert message for users
     */
    fun getCGTrafficAlert(
        trafficStatus: TrafficStatus, 
        cityName: String? = null,
        isStudentCommute: Boolean = true
    ): String? {
        val isCapital = cityName?.lowercase() == "raipur"
        
        return when (trafficStatus.congestionLevel) {
            CongestionLevel.HEAVY, CongestionLevel.SEVERE -> {
                if (isStudentCommute) {
                    if (isCapital) {
                        "⚠️ Heavy traffic in Raipur! Consider auto-rickshaw or leave ${formatDuration(trafficStatus.delaySeconds)} earlier for college."
                    } else {
                        "⚠️ Traffic ahead! ${trafficStatus.delayText} - maybe take the bypass road?"
                    }
                } else {
                    "⚠️ Traffic detected in ${cityName ?: "city"}. ${trafficStatus.delayText} expected."
                }
            }
            CongestionLevel.MODERATE -> {
                if (trafficStatus.delaySeconds > 300) { // 5+ minutes delay
                    if (isCapital) {
                        "⏰ Moderate traffic in Raipur. ${trafficStatus.delayText} - avoid main roads if possible."
                    } else {
                        "⏰ Some traffic ahead. ${trafficStatus.delayText} - still manageable in CG."
                    }
                } else null
            }
            else -> null
        }
    }
    
    /**
     * Get seasonal travel recommendations for Chhattisgarh
     * @return Season-specific travel advice
     */
    fun getCGSeasonalTravelAdvice(): String {
        val calendar = Calendar.getInstance()
        val month = calendar.get(Calendar.MONTH)
        
        return when (month) {
            Calendar.APRIL, Calendar.MAY, Calendar.JUNE -> {
                "🌡️ Summer in CG: Travel early morning (6-9 AM) or evening (6-8 PM). Stay hydrated!"
            }
            Calendar.JULY, Calendar.AUGUST, Calendar.SEPTEMBER -> {
                "🌧️ Monsoon season: Roads may be waterlogged. Allow extra time and carry umbrella."
            }
            Calendar.OCTOBER, Calendar.NOVEMBER, Calendar.FEBRUARY -> {
                "🌤️ Perfect weather in CG! Great time for walking or cycling to college."
            }
            Calendar.DECEMBER, Calendar.JANUARY -> {
                "🌫️ Winter mornings can be foggy in CG. Visibility may be low before 9 AM."
            }
            else -> "Plan your travel according to CG weather conditions."
        }
    }package com.aalay.app.utils

import android.content.Context
import androidx.core.content.ContextCompat
import com.aalay.app.R
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt

/**
 * Utility class for traffic-related operations
 * Converts Mapbox duration to readable format and handles congestion badges
 */
object TrafficUtils {
    
    /**
     * Convert duration in seconds to human-readable format
     * @param durationSeconds Duration in seconds
     * @param includeSeconds Whether to include seconds in output
     * @return Formatted duration string (e.g., "25 mins", "1h 30m", "2h 45m 30s")
     */
    fun formatDuration(durationSeconds: Double, includeSeconds: Boolean = false): String {
        val totalSeconds = durationSeconds.roundToInt()
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60
        
        return when {
            hours > 0 -> {
                if (includeSeconds && seconds > 0) {
                    "${hours}h ${minutes}m ${seconds}s"
                } else if (minutes > 0) {
                    "${hours}h ${minutes}m"
                } else {
                    "${hours}h"
                }
            }
            minutes > 0 -> {
                if (includeSeconds && seconds > 0) {
                    "${minutes}m ${seconds}s"
                } else {
                    if (minutes == 1) "1 min" else "$minutes mins"
                }
            }
            else -> {
                if (seconds <= 30) "< 1 min" else "1 min"
            }
        }
    }
    
    /**
     * Get short duration format for compact display
     * @param durationSeconds Duration in seconds
     * @return Short formatted string (e.g., "25m", "1h30m")
     */
    fun formatDurationShort(durationSeconds: Double): String {
        val totalSeconds = durationSeconds.roundToInt()
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        
        return when {
            hours > 0 -> {
                if (minutes > 0) "${hours}h${minutes}m" else "${hours}h"
            }
            minutes > 0 -> "${minutes}m"
            else -> "<1m"
        }
    }
    
    /**
     * Calculate traffic congestion level based on duration difference
     * @param normalDuration Duration without traffic (seconds)
     * @param trafficDuration Duration with current traffic (seconds)
     * @return Congestion level
     */
    fun getCongestionLevel(normalDuration: Double, trafficDuration: Double): CongestionLevel {
        val delayRatio = trafficDuration / normalDuration
        
        return when {
            delayRatio <= 1.1 -> CongestionLevel.LOW
            delayRatio <= 1.3 -> CongestionLevel.MODERATE
            delayRatio <= 1.6 -> CongestionLevel.HEAVY
            else -> CongestionLevel.SEVERE
        }
    }
    
    /**
     * Get congestion badge information
     * @param congestionLevel Congestion level
     * @return Congestion badge data
     */
    fun getCongestionBadge(congestionLevel: CongestionLevel): CongestionBadge {
        return when (congestionLevel) {
            CongestionLevel.LOW -> CongestionBadge(
                text = "Light Traffic",
                color = "#4CAF50",
                backgroundColor = "#E8F5E8",
                icon = "🟢"
            )
            CongestionLevel.MODERATE -> CongestionBadge(
                text = "Moderate Traffic",
                color = "#FF9800",
                backgroundColor = "#FFF3E0",
                icon = "🟡"
            )
            CongestionLevel.HEAVY -> CongestionBadge(
                text = "Heavy Traffic",
                color = "#F44336",
                backgroundColor = "#FFEBEE",
                icon = "🔴"
            )
            CongestionLevel.SEVERE -> CongestionBadge(
                text = "Severe Traffic",
                color = "#B71C1C",
                backgroundColor = "#FFCDD2",
                icon = "🚨"
            )
        }
    }
    
    /**
     * Get traffic status with delay information
     * @param normalDuration Normal duration in seconds
     * @param trafficDuration Current traffic duration in seconds
     * @return Traffic status with delay info
     */
    fun getTrafficStatus(normalDuration: Double, trafficDuration: Double): TrafficStatus {
        val congestionLevel = getCongestionLevel(normalDuration, trafficDuration)
        val delaySeconds = trafficDuration - normalDuration
        val delayMinutes = (delaySeconds / 60).roundToInt()
        
        val delayText = when {
            delayMinutes <= 0 -> "No delay"
            delayMinutes == 1 -> "1 min delay"
            delayMinutes < 60 -> "$delayMinutes mins delay"
            else -> {
                val hours = delayMinutes / 60
                val mins = delayMinutes % 60
                if (mins > 0) "${hours}h ${mins}m delay" else "${hours}h delay"
            }
        }
        
        return TrafficStatus(
            congestionLevel = congestionLevel,
            delaySeconds = delaySeconds,
            delayText = delayText,
            badge = getCongestionBadge(congestionLevel)
        )
    }
    
    /**
     * Get best time to travel in Chhattisgarh considering local patterns
     * @param cityName City in Chhattisgarh
     * @return Recommended travel times
     */
    fun getBestTravelTimes(cityName: String? = null): List<TravelTimeRecommendation> {
        val calendar = Calendar.getInstance()
        val currentHour = calendar.get(Calendar.HOUR_OF_DAY)
        val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
        
        val recommendations = mutableListOf<TravelTimeRecommendation>()
        
        // Weekend vs weekday recommendations for CG
        if (dayOfWeek == Calendar.SATURDAY || dayOfWeek == Calendar.SUNDAY) {
            recommendations.addAll(getCGWeekendRecommendations())
        } else {
            recommendations.addAll(getCGWeekdayRecommendations(currentHour, cityName))
        }
        
        return recommendations.sortedBy { it.priority }
    }
    
    private fun getCGWeekdayRecommendations(currentHour: Int, cityName: String?): List<TravelTimeRecommendation> {
        val isCapital = cityName?.lowercase() == "raipur"
        
        return listOf(
            TravelTimeRecommendation(
                time = "Early Morning (6-8 AM)",
                trafficLevel = CongestionLevel.LOW,
                description = if (isCapital) "Best time in Raipur, light traffic" else "Perfect for college commute",
                priority = 1,
                isCurrent = currentHour in 6..8
            ),
            TravelTimeRecommendation(
                time = "Mid Morning (9:30-11 AM)",
                trafficLevel = CongestionLevel.LOW,
                description = "Post office hours, smooth roads",
                priority = 2,
                isCurrent = currentHour in 9..11
            ),
            TravelTimeRecommendation(
                time = "Afternoon (2-4 PM)",
                trafficLevel = if (isCapital) CongestionLevel.MODERATE else CongestionLevel.LOW,
                description = if (isCapital) "Some traffic in Raipur city center" else "Generally smooth in CG cities",
                priority = 3,
                isCurrent = currentHour in 14..16
            ),
            TravelTimeRecommendation(
                time = "Evening (6-8 PM)",
                trafficLevel = if (isCapital) CongestionLevel.HEAVY else CongestionLevel.MODERATE,
                description = if (isCapital) "Evening rush in capital" else "College closing time",
                priority = 4,
                isCurrent = currentHour in 18..20
            )
        )
    }
    
    private fun getCGWeekendRecommendations(): List<TravelTimeRecommendation> {
        return listOf(
            TravelTimeRecommendation(
                time = "Morning (8-11 AM)",
                trafficLevel = CongestionLevel.LOW,
                description = "Peaceful weekend mornings in CG",
                priority = 1,
                isCurrent = false
            ),
            TravelTimeRecommendation(
                time = "Afternoon (12-3 PM)",
                trafficLevel = CongestionLevel.LOW,
                description = "Light weekend traffic",
                priority = 2,
                isCurrent = false
            ),
            TravelTimeRecommendation(
                time = "Evening (5-7 PM)",
                trafficLevel = CongestionLevel.MODERATE,
                description = "Some market activity",
                priority = 3,
                isCurrent = false
            )
        )
    }
    
    /**
     * Calculate estimated arrival time
     * @param durationSeconds Travel duration in seconds
     * @param departureTime Departure time (default: current time)
     * @return Formatted arrival time
     */
    fun calculateArrivalTime(
        durationSeconds: Double,
        departureTime: Long = System.currentTimeMillis()
    ): String {
        val arrivalTime = departureTime + (durationSeconds * 1000).toLong()
        val sdf = SimpleDateFormat("h:mm a", Locale.getDefault())
        return sdf.format(Date(arrivalTime))
    }
    
    /**
     * Get travel mode duration comparison
     * @param walkingDuration Walking duration in seconds
     * @param cyclingDuration Cycling duration in seconds  
     * @param drivingDuration Driving duration in seconds
     * @param transitDuration Public transit duration in seconds
     * @return List of travel mode options with durations
     */
    fun getTravelModeComparison(
        walkingDuration: Double? = null,
        cyclingDuration: Double? = null,
        drivingDuration: Double? = null,
        transitDuration: Double? = null
    ): List<TravelMode> {
        val modes = mutableListOf<TravelMode>()
        
        walkingDuration?.let {
            modes.add(TravelMode(
                type = "Walking",
                duration = formatDuration(it),
                durationSeconds = it,
                icon = "🚶",
                color = "#4CAF50",
                description = "Good exercise, eco-friendly"
            ))
        }
        
        cyclingDuration?.let {
            modes.add(TravelMode(
                type = "Cycling",
                duration = formatDuration(it),
                durationSeconds = it,
                icon = "🚴",
                color = "#2196F3",
                description = "Fast and healthy option"
            ))
        }
        
        drivingDuration?.let {
            modes.add(TravelMode(
                type = "Driving",
                duration = formatDuration(it),
                durationSeconds = it,
                icon = "🚗",
                color = "#FF9800",
                description = "Fastest but traffic dependent"
            ))
        }
        
        transitDuration?.let {
            modes.add(TravelMode(
                type = "Public Transit",
                duration = formatDuration(it),
                durationSeconds = it,
                icon = "🚌",
                color = "#9C27B0",
                description = "Cost-effective for students"
            ))
        }
        
        return modes.sortedBy { it.durationSeconds }
    }
    
    /**
     * Parse Mapbox traffic congestion data
     * @param congestionArray Array of congestion values from Mapbox
     * @return Processed congestion information
     */
    fun parseMapboxCongestion(congestionArray: List<String>?): CongestionSummary {
        if (congestionArray.isNullOrEmpty()) {
            return CongestionSummary(
                overallLevel = CongestionLevel.LOW,
                segments = emptyList(),
                averageCongestion = 0.0
            )
        }
        
        val segments = congestionArray.map { congestionValue ->
            when (congestionValue) {
                "low" -> CongestionLevel.LOW
                "moderate" -> CongestionLevel.MODERATE
                "heavy" -> CongestionLevel.HEAVY
                "severe" -> CongestionLevel.SEVERE
                else -> CongestionLevel.LOW
            }
        }
        
        val averageCongestion = segments.map { level ->
            when (level) {
                CongestionLevel.LOW -> 1.0
                CongestionLevel.MODERATE -> 2.0
                CongestionLevel.HEAVY -> 3.0
                CongestionLevel.SEVERE -> 4.0
            }
        }.average()
        
        val overallLevel = when {
            averageCongestion <= 1.5 -> CongestionLevel.LOW
            averageCongestion <= 2.5 -> CongestionLevel.MODERATE
            averageCongestion <= 3.5 -> CongestionLevel.HEAVY
            else -> CongestionLevel.SEVERE
        }
        
        return CongestionSummary(
            overallLevel = overallLevel,
            segments = segments,
            averageCongestion = averageCongestion
        )
    }
    
    /**
     * Get traffic alert message based on current conditions
     * @param trafficStatus Current traffic status
     * @param isStudentCommute Whether this is for student commute
     * @return Alert message for users
     */
    fun getTrafficAlert(trafficStatus: TrafficStatus, isStudentCommute: Boolean = true): String? {
        return when (trafficStatus.congestionLevel) {
            CongestionLevel.HEAVY, CongestionLevel.SEVERE -> {
                if (isStudentCommute) {
                    "⚠️ Heavy traffic ahead! Consider leaving ${formatDuration(trafficStatus.delaySeconds)} earlier for classes."
                } else {
                    "⚠️ Heavy traffic detected. ${trafficStatus.delayText} expected."
                }
            }
            CongestionLevel.MODERATE -> {
                if (trafficStatus.delaySeconds > 300) { // 5+ minutes delay
                    "⏰ Moderate traffic. ${trafficStatus.delayText} - plan accordingly."
                } else null
            }
            else -> null
        }
    }
}

/**
 * Enum for congestion levels
 */
enum class CongestionLevel {
    LOW, MODERATE, HEAVY, SEVERE
}

/**
 * Data class for congestion badge display
 */
data class CongestionBadge(
    val text: String,
    val color: String,
    val backgroundColor: String,
    val icon: String
)

/**
 * Data class for traffic status
 */
data class TrafficStatus(
    val congestionLevel: CongestionLevel,
    val delaySeconds: Double,
    val delayText: String,
    val badge: CongestionBadge
)

/**
 * Data class for travel time recommendations
 */
data class TravelTimeRecommendation(
    val time: String,
    val trafficLevel: CongestionLevel,
    val description: String,
    val priority: Int,
    val isCurrent: Boolean
)

/**
 * Data class for travel mode options
 */
data class TravelMode(
    val type: String,
    val duration: String,
    val durationSeconds: Double,
    val icon: String,
    val color: String,
    val description: String
)

/**
 * Data class for congestion summary
 */
data class CongestionSummary(
    val overallLevel: CongestionLevel,
    val segments: List<CongestionLevel>,
    val averageCongestion: Double
)