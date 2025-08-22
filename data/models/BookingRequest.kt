package com.aalay.app.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

/**
 * Booking request model for accommodation bookings
 */
@Entity(tableName = "booking_requests")
data class BookingRequest(
    @PrimaryKey
    @SerializedName("id")
    val id: String,
    
    // User and accommodation details
    @SerializedName("user_id")
    val userId: String,
    
    @SerializedName("accommodation_id")
    val accommodationId: String,
    
    @SerializedName("owner_id")
    val ownerId: String,
    
    // Booking dates
    @SerializedName("check_in_date")
    val checkInDate: String, // Format: "yyyy-MM-dd"
    
    @SerializedName("check_out_date")
    val checkOutDate: String?, // null for open-ended bookings
    
    @SerializedName("duration_months")
    val durationMonths: Int,
    
    @SerializedName("duration_weeks")
    val durationWeeks: Int? = null,
    
    // Guest details
    @SerializedName("number_of_occupants")
    val numberOfOccupants: Int = 1,
    
    @SerializedName("guest_details")
    val guestDetails: List<GuestDetail> = emptyList(),
    
    @SerializedName("roommate_requests")
    val roommateRequests: List<String> = emptyList(), // User IDs of requested roommates
    
    // Booking preferences
    @SerializedName("room_preference")
    val roomPreference: RoomType,
    
    @SerializedName("floor_preference")
    val floorPreference: Int? = null,
    
    @SerializedName("special_requests")
    val specialRequests: String? = null,
    
    // Pricing details
    @SerializedName("monthly_rent")
    val monthlyRent: Double,
    
    @SerializedName("security_deposit")
    val securityDeposit: Double,
    
    @SerializedName("total_amount")
    val totalAmount: Double,
    
    @SerializedName("discount_amount")
    val discountAmount: Double? = null,
    
    @SerializedName("discount_code")
    val discountCode: String? = null,
    
    @SerializedName("additional_charges")
    val additionalCharges: Map<String, Double> = emptyMap(),
    
    // Student-specific pricing
    @SerializedName("student_discount_applied")
    val studentDiscountApplied: Boolean = false,
    
    @SerializedName("student_discount_percentage")
    val studentDiscountPercentage: Double? = null,
    
    @SerializedName("referral_discount")
    val referralDiscount: Double? = null,
    
    // Payment information
    @SerializedName("payment_method")
    val paymentMethod: PaymentMethod,
    
    @SerializedName("payment_status")
    val paymentStatus: PaymentStatus = PaymentStatus.PENDING,
    
    @SerializedName("payment_id")
    val paymentId: String? = null,
    
    @SerializedName("payment_gateway")
    val paymentGateway: String? = null,
    
    // Booking status and workflow
    @SerializedName("booking_status")
    val bookingStatus: BookingStatus = BookingStatus.REQUESTED,
    
    @SerializedName("booking_type")
    val bookingType: BookingType = BookingType.REGULAR,
    
    @SerializedName("priority_level")
    val priorityLevel: PriorityLevel = PriorityLevel.NORMAL,
    
    // Student-specific features
    @SerializedName("is_semester_booking")
    val isSemesterBooking: Boolean = false,
    
    @SerializedName("semester_details")
    val semesterDetails: SemesterDetails? = null,
    
    @SerializedName("academic_year")
    val academicYear: String? = null,
    
    @SerializedName("is_shared_booking")
    val isSharedBooking: Boolean = false,
    
    @SerializedName("shared_with_users")
    val sharedWithUsers: List<String> = emptyList(),
    
    // Approval and verification
    @SerializedName("requires_owner_approval")
    val requiresOwnerApproval: Boolean = true,
    
    @SerializedName("owner_response_deadline")
    val ownerResponseDeadline: Long? = null,
    
    @SerializedName("verification_documents")
    val verificationDocuments: List<String> = emptyList(),
    
    // Communication
    @SerializedName("communication_thread_id")
    val communicationThreadId: String? = null,
    
    @SerializedName("owner_notes")
    val ownerNotes: String? = null,
    
    @SerializedName("user_notes")
    val userNotes: String? = null,
    
    // Cancellation policy
    @SerializedName("cancellation_policy")
    val cancellationPolicy: CancellationPolicy = CancellationPolicy.FLEXIBLE,
    
    @SerializedName("free_cancellation_until")
    val freeCancellationUntil: Long? = null,
    
    @SerializedName("cancellation_fee")
    val cancellationFee: Double? = null,
    
    // Student-friendly policies
    @SerializedName("exam_period_flexibility")
    val examPeriodFlexibility: Boolean = true,
    
    @SerializedName("semester_break_adjustment")
    val semesterBreakAdjustment: Boolean = false,
    
    // Timestamps
    @SerializedName("created_at")
    val createdAt: Long = System.currentTimeMillis(),
    
    @SerializedName("updated_at")
    val updatedAt: Long = System.currentTimeMillis(),
    
    @SerializedName("approved_at")
    val approvedAt: Long? = null,
    
    @SerializedName("confirmed_at")
    val confirmedAt: Long? = null,
    
    @SerializedName("cancelled_at")
    val cancelledAt: Long? = null,
    
    @SerializedName("completed_at")
    val completedAt: Long? = null
) {
    /**
     * Calculate total booking duration in days
     */
    fun getTotalDurationDays(): Long {
        return (durationMonths * 30L) + (durationWeeks?.times(7L) ?: 0L)
    }
    
    /**
     * Check if booking is currently active
     */
    fun isActive(): Boolean {
        return bookingStatus in listOf(
            BookingStatus.CONFIRMED,
            BookingStatus.ONGOING,
            BookingStatus.CHECKED_IN
        )
    }
    
    /**
     * Check if booking can be cancelled
     */
    fun canBeCancelled(): Boolean {
        val now = System.currentTimeMillis()
        return bookingStatus in listOf(
            BookingStatus.REQUESTED,
            BookingStatus.APPROVED,
            BookingStatus.CONFIRMED
        ) && (freeCancellationUntil?.let { it > now } ?: false)
    }
    
    /**
     * Get final payable amount after all discounts
     */
    fun getFinalAmount(): Double {
        var final = totalAmount
        discountAmount?.let { final -= it }
        referralDiscount?.let { final -= it }
        return final.coerceAtLeast(0.0)
    }
}

/**
 * Guest details for multi-occupant bookings
 */
data class GuestDetail(
    @SerializedName("name")
    val name: String,
    
    @SerializedName("age")
    val age: Int,
    
    @SerializedName("gender")
    val gender: Gender,
    
    @SerializedName("relationship")
    val relationship: String? = null,
    
    @SerializedName("student_id")
    val studentId: String? = null,
    
    @SerializedName("college")
    val college: String? = null,
    
    @SerializedName("phone")
    val phone: String? = null,
    
    @SerializedName("email")
    val email: String? = null
)

/**
 * Semester-specific booking details
 */
data class SemesterDetails(
    @SerializedName("semester_name")
    val semesterName: String, // e.g., "Fall 2024", "Spring 2025"
    
    @SerializedName("semester_start_date")
    val semesterStartDate: String,
    
    @SerializedName("semester_end_date")
    val semesterEndDate: String,
    
    @SerializedName("exam_period_start")
    val examPeriodStart: String? = null,
    
    @SerializedName("exam_period_end")
    val examPeriodEnd: String? = null,
    
    @SerializedName("winter_break_start")
    val winterBreakStart: String? = null,
    
    @SerializedName("winter_break_end")
    val winterBreakEnd: String? = null,
    
    @SerializedName("summer_break_start")
    val summerBreakStart: String? = null,
    
    @SerializedName("summer_break_end")
    val summerBreakEnd: String? = null
)

/**
 * Payment methods supported
 */
enum class PaymentMethod {
    @SerializedName("credit_card")
    CREDIT_CARD,
    
    @SerializedName("debit_card")
    DEBIT_CARD,
    
    @SerializedName("upi")
    UPI,
    
    @SerializedName("net_banking")
    NET_BANKING,
    
    @SerializedName("wallet")
    WALLET,
    
    @SerializedName("student_loan")
    STUDENT_LOAN,
    
    @SerializedName("cash")
    CASH,
    
    @SerializedName("cheque")
    CHEQUE,
    
    @SerializedName("bank_transfer")
    BANK_TRANSFER
}

/**
 * Payment status tracking
 */
enum class PaymentStatus {
    @SerializedName("pending")
    PENDING,
    
    @SerializedName("processing")
    PROCESSING,
    
    @SerializedName("completed")
    COMPLETED,
    
    @SerializedName("partially_paid")
    PARTIALLY_PAID,
    
    @SerializedName("failed")
    FAILED,
    
    @SerializedName("refunded")
    REFUNDED,
    
    @SerializedName("cancelled")
    CANCELLED
}

/**
 * Booking status workflow
 */
enum class BookingStatus {
    @SerializedName("requested")
    REQUESTED,
    
    @SerializedName("pending_verification")
    PENDING_VERIFICATION,
    
    @SerializedName("under_review")
    UNDER_REVIEW,
    
    @SerializedName("approved")
    APPROVED,
    
    @SerializedName("rejected")
    REJECTED,
    
    @SerializedName("confirmed")
    CONFIRMED,
    
    @SerializedName("payment_pending")
    PAYMENT_PENDING,
    
    @SerializedName("checked_in")
    CHECKED_IN,
    
    @SerializedName("ongoing")
    ONGOING,
    
    @SerializedName("checked_out")
    CHECKED_OUT,
    
    @SerializedName("completed")
    COMPLETED,
    
    @SerializedName("cancelled")
    CANCELLED,
    
    @SerializedName("expired")
    EXPIRED,
    
    @SerializedName("disputed")
    DISPUTED
}

/**
 * Types of booking
 */
enum class BookingType {
    @SerializedName("regular")
    REGULAR,
    
    @SerializedName("instant")
    INSTANT,
    
    @SerializedName("flash_deal")
    FLASH_DEAL,
    
    @SerializedName("semester_special")
    SEMESTER_SPECIAL,
    
    @SerializedName("emergency")
    EMERGENCY,
    
    @SerializedName("group_booking")
    GROUP_BOOKING,
    
    @SerializedName("trial_stay")
    TRIAL_STAY
}

/**
 * Priority levels for booking processing
 */
enum class PriorityLevel {
    @SerializedName("low")
    LOW,
    
    @SerializedName("normal")
    NORMAL,
    
    @SerializedName("high")
    HIGH,
    
    @SerializedName("urgent")
    URGENT,
    
    @SerializedName("emergency")
    EMERGENCY
}

/**
 * Cancellation policy types
 */
enum class CancellationPolicy {
    @SerializedName("flexible")
    FLEXIBLE, // Free cancellation until 24 hours before check-in
    
    @SerializedName("moderate")
    MODERATE, // Free cancellation until 5 days before check-in
    
    @SerializedName("strict")
    STRICT, // 50% refund until 7 days before check-in
    
    @SerializedName("super_strict")
    SUPER_STRICT, // No refund after booking confirmation
    
    @SerializedName("student_friendly")
    STUDENT_FRIENDLY, // Special policy for students with exam considerations
    
    @SerializedName("semester_based")
    SEMESTER_BASED // Cancellation aligned with academic calendar
}