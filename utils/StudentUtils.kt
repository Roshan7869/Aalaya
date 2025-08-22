package com.aalay.app.utils

import android.text.TextUtils
import android.util.Patterns
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Pattern

/**
 * Utility class for student-specific operations and validations
 */
object StudentUtils {
    
    // Common Chhattisgarh educational institutions and their domains
    private val CG_EDUCATIONAL_DOMAINS = setOf(
        "iitrpr.ac.in", "nit-raipur.ac.in", "aiimsbilaspur.edu.in",
        "ssipmt.com", "kiit.ac.in", "csvtu.ac.in", "bvv.edu.in",
        "isbm.edu.in", "rungta.ac.in", "amity.edu", 
        "curaj.ac.in", "christuniversity.in", "mats.ac.in"
    )
    
    // Major cities in Chhattisgarh for budget calculation
    private val CG_MAJOR_CITIES = mapOf(
        "Raipur" to 1, // Tier 1 - Capital city
        "Bilaspur" to 2, // Tier 2
        "Korba" to 2, // Tier 2 - Industrial city
        "Bhilai" to 2, // Tier 2 - Steel city
        "Durg" to 2, // Tier 2
        "Rajnandgaon" to 3, // Tier 3
        "Jagdalpur" to 3, // Tier 3
        "Ambikapur" to 3, // Tier 3
        "Dhamtari" to 3, // Tier 3
        "Mahasamund" to 3 // Tier 3
    )
    
    // Academic year calculation
    private val ACADEMIC_YEAR_START_MONTH = Calendar.JULY
    
    /**
     * Validate if email belongs to a Chhattisgarh educational institution
     * @param email Email address to validate
     * @return True if it's from CG educational institution
     */
    fun isStudentEmail(email: String?): Boolean {
        if (email.isNullOrBlank() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            return false
        }
        
        val domain = email.substringAfterLast("@").lowercase()
        
        // Check for CG educational domains first
        if (CG_EDUCATIONAL_DOMAINS.contains(domain)) {
            return true
        }
        
        // Check for common student domains
        return domain.endsWith(".edu") || domain.endsWith(".ac.in") || 
               domain.endsWith(".edu.in") || domain.contains("student") || 
               domain.contains("college") || domain.contains("university")
    }
    
    /**
     * Extract institution name from Chhattisgarh student email
     * @param email Student email address
     * @return Institution name or null if not extractable
     */
    fun extractInstitutionFromEmail(email: String?): String? {
        if (!isStudentEmail(email)) return null
        
        val domain = email!!.substringAfterLast("@")
        
        // Map known CG institutions
        return when (domain.lowercase()) {
            "iitrpr.ac.in" -> "IIT Raipur"
            "nit-raipur.ac.in" -> "NIT Raipur"
            "aiimsbilaspur.edu.in" -> "AIIMS Bilaspur"
            "csvtu.ac.in" -> "CSVTU"
            "bvv.edu.in" -> "Bharati Vidyapeeth University"
            "isbm.edu.in" -> "ISBM University"
            "rungta.ac.in" -> "Rungta College"
            "mats.ac.in" -> "MATS University"
            else -> {
                // Try to extract from domain
                val parts = domain.split(".")
                parts.firstOrNull()?.replaceFirstChar { 
                    if (it.isLowerCase()) it.titlecase() else it.toString() 
                }
            }
        }
    }
    
    /**
     * Validate student ID format
     * Supports various formats: alphanumeric, with hyphens, etc.
     * @param studentId Student ID to validate
     * @return True if valid format
     */
    fun isValidStudentId(studentId: String?): Boolean {
        if (studentId.isNullOrBlank()) return false
        
        // Student ID should be 6-20 characters, alphanumeric with optional hyphens/underscores
        val pattern = Pattern.compile("^[A-Za-z0-9_-]{6,20}$")
        return pattern.matcher(studentId.trim()).matches()
    }
    
    /**
     * Calculate current academic year
     * @return Academic year string (e.g., "2024-25")
     */
    fun getCurrentAcademicYear(): String {
        val calendar = Calendar.getInstance()
        val currentYear = calendar.get(Calendar.YEAR)
        val currentMonth = calendar.get(Calendar.MONTH)
        
        return if (currentMonth >= ACADEMIC_YEAR_START_MONTH) {
            // After July, new academic year has started
            "$currentYear-${(currentYear + 1) % 100}"
        } else {
            // Before July, still in previous academic year
            "${currentYear - 1}-${currentYear % 100}"
        }
    }
    
    /**
     * Get semester based on current date
     * @return Current semester (1 or 2)
     */
    fun getCurrentSemester(): Int {
        val calendar = Calendar.getInstance()
        val currentMonth = calendar.get(Calendar.MONTH)
        
        return when (currentMonth) {
            Calendar.JULY, Calendar.AUGUST, Calendar.SEPTEMBER, 
            Calendar.OCTOBER, Calendar.NOVEMBER, Calendar.DECEMBER -> 1
            else -> 2
        }
    }
    
    /**
     * Calculate student budget category based on Chhattisgarh cost of living
     * @param monthlyBudget Monthly budget in rupees
     * @param cityName City in Chhattisgarh
     * @return Budget category for filtering
     */
    fun getBudgetCategory(monthlyBudget: Double, cityName: String? = null): BudgetCategory {
        val cityTier = getCityTier(cityName)
        
        return when (cityTier) {
            1 -> { // Raipur (Capital)
                when {
                    monthlyBudget <= 6000 -> BudgetCategory.BUDGET
                    monthlyBudget <= 12000 -> BudgetCategory.MODERATE
                    monthlyBudget <= 20000 -> BudgetCategory.COMFORTABLE
                    else -> BudgetCategory.PREMIUM
                }
            }
            2 -> { // Bilaspur, Bhilai, Durg, Korba
                when {
                    monthlyBudget <= 4000 -> BudgetCategory.BUDGET
                    monthlyBudget <= 8000 -> BudgetCategory.MODERATE
                    monthlyBudget <= 15000 -> BudgetCategory.COMFORTABLE
                    else -> BudgetCategory.PREMIUM
                }
            }
            else -> { // Smaller cities
                when {
                    monthlyBudget <= 3000 -> BudgetCategory.BUDGET
                    monthlyBudget <= 6000 -> BudgetCategory.MODERATE
                    monthlyBudget <= 12000 -> BudgetCategory.COMFORTABLE
                    else -> BudgetCategory.PREMIUM
                }
            }
        }
    }
    
    /**
     * Get recommended budget range for Chhattisgarh cities
     * @param cityName City name in CG
     * @return Pair of min and max recommended budget
     */
    fun getRecommendedBudgetRange(cityName: String? = null): Pair<Double, Double> {
        val cityTier = getCityTier(cityName)
        
        return when (cityTier) {
            1 -> Pair(5000.0, 18000.0) // Raipur
            2 -> Pair(3500.0, 12000.0) // Bilaspur, Bhilai, etc.
            else -> Pair(2500.0, 8000.0) // Smaller cities
        }
    }
    
    /**
     * Get city tier for Chhattisgarh cities
     * @param cityName City name
     * @return City tier (1, 2, or 3)
     */
    fun getCityTier(cityName: String?): Int {
        return CG_MAJOR_CITIES[cityName] ?: 3
    }
    
    /**
     * Get popular colleges/universities in Chhattisgarh by city
     * @param cityName City name
     * @return List of popular educational institutions
     */
    fun getPopularCollegesInCity(cityName: String?): List<String> {
        return when (cityName?.lowercase()) {
            "raipur" -> listOf(
                "IIT Raipur", "NIT Raipur", "CSVTU", "Raipur Institute of Technology",
                "Shri Shankaracharya Technical Campus", "Government Engineering College Raipur"
            )
            "bilaspur" -> listOf(
                "AIIMS Bilaspur", "Government Engineering College Bilaspur",
                "Bilaspur University", "Dr. C.V. Raman University"
            )
            "bhilai" -> listOf(
                "Government Engineering College Bhilai", "Bhilai Institute of Technology",
                "Rungta College of Engineering & Technology"
            )
            "durg" -> listOf(
                "Government Polytechnic Durg", "MATS University",
                "Chouksey Engineering College"
            )
            "korba" -> listOf(
                "Government Engineering College Korba", "Korba Technical Campus"
            )
            else -> listOf("Local Colleges", "Government Institutions")
        }
    }
    
    /**
     * Validate age for student accommodation
     * @param age User's age
     * @return True if age is appropriate for student housing
     */
    fun isValidStudentAge(age: Int): Boolean {
        return age in 16..35 // Typical student age range
    }
    
    /**
     * Format academic year display
     * @param year Academic year string
     * @return Formatted display string
     */
    fun formatAcademicYear(year: String): String {
        return "Academic Year $year"
    }
    
    /**
     * Check if current period is admission season
     * @return True if it's admission season (May-August)
     */
    fun isAdmissionSeason(): Boolean {
        val calendar = Calendar.getInstance()
        val currentMonth = calendar.get(Calendar.MONTH)
        
        return currentMonth in Calendar.MAY..Calendar.AUGUST
    }
    
    /**
     * Check if current period is exam season
     * @return True if it's exam season
     */
    fun isExamSeason(): Boolean {
        val calendar = Calendar.getInstance()
        val currentMonth = calendar.get(Calendar.MONTH)
        
        return currentMonth in arrayOf(Calendar.APRIL, Calendar.MAY, Calendar.NOVEMBER, Calendar.DECEMBER)
    }
    
    /**
     * Generate student-friendly cancellation policy message
     * @param isExamPeriod Whether it's exam period
     * @return Cancellation policy text
     */
    fun getStudentCancellationPolicy(isExamPeriod: Boolean = isExamSeason()): String {
        return if (isExamPeriod) {
            "Student-friendly: Free cancellation during exam period. No questions asked!"
        } else {
            "Flexible cancellation: Cancel up to 7 days before check-in for full refund"
        }
    }
    
    /**
     * Get course duration options
     * @return List of common course durations
     */
    fun getCourseDurationOptions(): List<CourseDuration> {
        return listOf(
            CourseDuration.SEMESTER,
            CourseDuration.YEAR,
            CourseDuration.TWO_YEARS,
            CourseDuration.THREE_YEARS,
            CourseDuration.FOUR_YEARS,
            CourseDuration.FIVE_YEARS
        )
    }
    
    /**
     * Calculate stay duration based on course
     * @param courseDuration Course duration
     * @param currentSemester Current semester number
     * @return Estimated months of stay needed
     */
    fun calculateStayDuration(courseDuration: CourseDuration, currentSemester: Int): Int {
        val totalMonths = when (courseDuration) {
            CourseDuration.SEMESTER -> 6
            CourseDuration.YEAR -> 12
            CourseDuration.TWO_YEARS -> 24
            CourseDuration.THREE_YEARS -> 36
            CourseDuration.FOUR_YEARS -> 48
            CourseDuration.FIVE_YEARS -> 60
        }
        
        val monthsPerSemester = 6
        val completedMonths = (currentSemester - 1) * monthsPerSemester
        
        return maxOf(0, totalMonths - completedMonths)
    }
    
    /**
     * Get student verification status display
     * @param isVerified Verification status
     * @param institutionName Institution name
     * @return Display string for verification status
     */
    fun getVerificationStatusDisplay(isVerified: Boolean, institutionName: String?): String {
        return if (isVerified && !institutionName.isNullOrBlank()) {
            "✓ Verified Student at $institutionName"
        } else if (isVerified) {
            "✓ Verified Student"
        } else {
            "Verification Pending"
        }
    }
}

enum class BudgetCategory(val displayName: String, val range: String) {
    BUDGET("Budget", "Under ₹4K"),
    MODERATE("Moderate", "₹4K - ₹8K"),
    COMFORTABLE("Comfortable", "₹8K - ₹15K"),
    PREMIUM("Premium", "Above ₹15K")
}

enum class CourseDuration(val displayName: String, val months: Int) {
    SEMESTER("1 Semester", 6),
    YEAR("1 Year", 12),
    TWO_YEARS("2 Years", 24),
    THREE_YEARS("3 Years", 36),
    FOUR_YEARS("4 Years", 48),
    FIVE_YEARS("5 Years", 60)
}