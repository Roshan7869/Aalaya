package com.aalay.app.domain

import com.aalay.app.data.models.BookingRequest
import com.aalay.app.data.models.StudentUser
import com.aalay.app.data.repository.AccommodationRepository
import com.aalay.app.data.repository.StudentAuthRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.util.*
import javax.inject.Inject

data class BookingDetails(
    val accommodationId: String,
    val checkInDate: Date,
    val checkOutDate: Date? = null, // null for long-term stays
    val roommates: List<String> = emptyList(), // Student IDs of roommates
    val specialRequests: String? = null,
    val isSharedBooking: Boolean = false,
    val paymentMethod: PaymentMethod,
    val emergencyContact: EmergencyContact? = null,
    val parentalConsent: Boolean = false // Required for minors
)

data class EmergencyContact(
    val name: String,
    val phone: String,
    val relationship: String
)

enum class PaymentMethod {
    CREDIT_CARD,
    DEBIT_CARD,
    UPI,
    WALLET,
    STUDENT_LOAN,
    BANK_TRANSFER
}

data class BookingValidation(
    val isValid: Boolean,
    val errors: List<String> = emptyList(),
    val warnings: List<String> = emptyList()
)

data class BookingConfirmation(
    val bookingId: String,
    val accommodationId: String,
    val studentId: String,
    val totalAmount: Double,
    val securityDeposit: Double,
    val bookingDate: Date,
    val status: BookingStatus,
    val confirmationCode: String,
    val paymentReference: String? = null,
    val checkInInstructions: String? = null
)

enum class BookingStatus {
    PENDING,
    CONFIRMED,
    CANCELLED,
    COMPLETED,
    PAYMENT_FAILED,
    UNDER_REVIEW
}

class BookPGUseCase @Inject constructor(
    private val accommodationRepository: AccommodationRepository,
    private val studentAuthRepository: StudentAuthRepository
) {

    /**
     * Main booking function for PG/accommodation
     */
    suspend operator fun invoke(
        bookingDetails: BookingDetails
    ): Flow<Result<BookingConfirmation>> = flow {
        try {
            // Get current student
            val currentStudent = studentAuthRepository.getCurrentStudent()
                ?: throw Exception("Student not authenticated")

            // Validate booking details
            val validation = validateBooking(bookingDetails, currentStudent)
            if (!validation.isValid) {
                throw Exception("Booking validation failed: ${validation.errors.joinToString(", ")}")
            }

            emit(Result.success(BookingConfirmation(
                bookingId = "temp_processing",
                accommodationId = bookingDetails.accommodationId,
                studentId = currentStudent.id,
                totalAmount = 0.0,
                securityDeposit = 0.0,
                bookingDate = Date(),
                status = BookingStatus.PENDING,
                confirmationCode = "PROCESSING"
            )))

            // Get accommodation details
            val accommodation = accommodationRepository.getAccommodationById(bookingDetails.accommodationId)
                ?: throw Exception("Accommodation not found")

            // Check availability
            val isAvailable = accommodationRepository.checkAvailability(
                accommodationId = bookingDetails.accommodationId,
                checkInDate = bookingDetails.checkInDate,
                checkOutDate = bookingDetails.checkOutDate,
                roommates = bookingDetails.roommates
            )

            if (!isAvailable) {
                throw Exception("Accommodation is not available for the selected dates")
            }

            // Calculate pricing
            val pricingDetails = calculatePricing(
                accommodation = accommodation,
                bookingDetails = bookingDetails,
                student = currentStudent
            )

            // Create booking request
            val bookingRequest = BookingRequest(
                id = UUID.randomUUID().toString(),
                studentId = currentStudent.id,
                accommodationId = bookingDetails.accommodationId,
                checkInDate = bookingDetails.checkInDate,
                checkOutDate = bookingDetails.checkOutDate,
                roommates = bookingDetails.roommates,
                totalAmount = pricingDetails.totalAmount,
                securityDeposit = pricingDetails.securityDeposit,
                specialRequests = bookingDetails.specialRequests,
                paymentMethod = bookingDetails.paymentMethod.name,
                emergencyContact = bookingDetails.emergencyContact,
                createdAt = Date(),
                status = BookingStatus.PENDING.name,
                isSharedBooking = bookingDetails.isSharedBooking
            )

            // Process payment (this would integrate with actual payment gateway)
            val paymentResult = processPayment(bookingRequest, bookingDetails.paymentMethod)
            
            if (!paymentResult.success) {
                throw Exception("Payment failed: ${paymentResult.error}")
            }

            // Submit booking to repository
            val confirmedBooking = accommodationRepository.createBooking(
                bookingRequest.copy(
                    status = BookingStatus.CONFIRMED.name,
                    paymentReference = paymentResult.transactionId
                )
            )

            // Send confirmation notifications
            sendBookingNotifications(confirmedBooking, currentStudent)

            // Update student's booking history
            updateStudentBookingHistory(currentStudent.id, confirmedBooking.id)

            val confirmation = BookingConfirmation(
                bookingId = confirmedBooking.id,
                accommodationId = confirmedBooking.accommodationId,
                studentId = confirmedBooking.studentId,
                totalAmount = confirmedBooking.totalAmount,
                securityDeposit = confirmedBooking.securityDeposit,
                bookingDate = confirmedBooking.createdAt,
                status = BookingStatus.valueOf(confirmedBooking.status),
                confirmationCode = generateConfirmationCode(confirmedBooking.id),
                paymentReference = confirmedBooking.paymentReference,
                checkInInstructions = accommodation.checkInInstructions
            )

            emit(Result.success(confirmation))

        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    /**
     * Validate booking for shared accommodation with roommates
     */
    suspend fun validateSharedBooking(
        bookingDetails: BookingDetails,
        roommateIds: List<String>
    ): Flow<Result<BookingValidation>> = flow {
        try {
            val validation = BookingValidation(isValid = true)
            val errors = mutableListOf<String>()
            val warnings = mutableListOf<String>()

            // Validate each roommate
            for (roommateId in roommateIds) {
                val roommate = studentAuthRepository.getStudentById(roommateId)
                if (roommate == null) {
                    errors.add("Roommate with ID $roommateId not found")
                    continue
                }

                // Check if roommate is verified
                if (!roommate.isVerified) {
                    warnings.add("Roommate ${roommate.firstName} is not verified")
                }

                // Check for conflicts in booking dates
                val hasConflictingBooking = accommodationRepository.hasConflictingBooking(
                    studentId = roommateId,
                    checkInDate = bookingDetails.checkInDate,
                    checkOutDate = bookingDetails.checkOutDate
                )

                if (hasConflictingBooking) {
                    errors.add("Roommate ${roommate.firstName} has conflicting booking")
                }
            }

            emit(Result.success(
                validation.copy(
                    isValid = errors.isEmpty(),
                    errors = errors,
                    warnings = warnings
                )
            ))

        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    /**
     * Cancel booking with student-friendly policies
     */
    suspend fun cancelBooking(
        bookingId: String,
        reason: String
    ): Flow<Result<Boolean>> = flow {
        try {
            val booking = accommodationRepository.getBookingById(bookingId)
                ?: throw Exception("Booking not found")

            val currentStudent = studentAuthRepository.getCurrentStudent()
                ?: throw Exception("Student not authenticated")

            if (booking.studentId != currentStudent.id) {
                throw Exception("Unauthorized to cancel this booking")
            }

            // Calculate cancellation fee based on student-friendly policy
            val cancellationPolicy = calculateCancellationPolicy(booking, reason)
            
            // Process refund if applicable
            if (cancellationPolicy.refundAmount > 0) {
                val refundResult = processRefund(booking, cancellationPolicy.refundAmount)
                if (!refundResult.success) {
                    throw Exception("Refund processing failed")
                }
            }

            // Update booking status
            val cancelledBooking = accommodationRepository.updateBookingStatus(
                bookingId = bookingId,
                status = BookingStatus.CANCELLED,
                cancellationReason = reason,
                cancellationFee = cancellationPolicy.cancellationFee,
                refundAmount = cancellationPolicy.refundAmount
            )

            // Notify accommodation owner
            sendCancellationNotification(cancelledBooking, reason)

            emit(Result.success(true))

        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    private suspend fun validateBooking(
        bookingDetails: BookingDetails,
        student: StudentUser
    ): BookingValidation {
        val errors = mutableListOf<String>()
        val warnings = mutableListOf<String>()

        // Check if student is verified
        if (!student.isVerified) {
            errors.add("Student verification required for booking")
        }

        // Validate dates
        val now = Date()
        if (bookingDetails.checkInDate.before(now)) {
            errors.add("Check-in date cannot be in the past")
        }

        bookingDetails.checkOutDate?.let { checkOut ->
            if (checkOut.before(bookingDetails.checkInDate)) {
                errors.add("Check-out date must be after check-in date")
            }
        }

        // Validate emergency contact for minors
        if (student.age < 18 && bookingDetails.emergencyContact == null) {
            errors.add("Emergency contact required for students under 18")
        }

        // Validate parental consent for minors
        if (student.age < 18 && !bookingDetails.parentalConsent) {
            errors.add("Parental consent required for students under 18")
        }

        // Check payment method
        if (bookingDetails.paymentMethod == PaymentMethod.STUDENT_LOAN && !student.isEligibleForStudentLoan) {
            warnings.add("Student loan eligibility needs verification")
        }

        return BookingValidation(
            isValid = errors.isEmpty(),
            errors = errors,
            warnings = warnings
        )
    }

    private suspend fun calculatePricing(
        accommodation: com.aalay.app.data.models.Accommodation,
        bookingDetails: BookingDetails,
        student: StudentUser
    ): PricingDetails {
        var totalAmount = accommodation.pricePerMonth.toDouble()
        var securityDeposit = accommodation.securityDeposit.toDouble()

        // Apply student discounts
        if (student.isVerified && student.collegeEmail != null) {
            totalAmount *= 0.95 // 5% student discount
        }

        // Shared booking discount
        if (bookingDetails.isSharedBooking && bookingDetails.roommates.isNotEmpty()) {
            val shareCount = bookingDetails.roommates.size + 1
            totalAmount /= shareCount
            securityDeposit /= shareCount
        }

        // Long-term stay discount
        bookingDetails.checkOutDate?.let { checkOut ->
            val daysDifference = (checkOut.time - bookingDetails.checkInDate.time) / (1000 * 60 * 60 * 24)
            if (daysDifference > 90) { // More than 3 months
                totalAmount *= 0.9 // 10% long-term discount
            }
        }

        return PricingDetails(
            baseAmount = accommodation.pricePerMonth.toDouble(),
            totalAmount = totalAmount,
            securityDeposit = securityDeposit,
            discounts = calculateDiscounts(accommodation, student, bookingDetails)
        )
    }

    private fun calculateDiscounts(
        accommodation: com.aalay.app.data.models.Accommodation,
        student: StudentUser,
        bookingDetails: BookingDetails
    ): List<Discount> {
        val discounts = mutableListOf<Discount>()

        if (student.isVerified && student.collegeEmail != null) {
            discounts.add(Discount("Student Verification", 5.0, DiscountType.PERCENTAGE))
        }

        if (bookingDetails.isSharedBooking) {
            discounts.add(Discount("Shared Booking", 50.0, DiscountType.PERCENTAGE))
        }

        return discounts
    }

    private suspend fun processPayment(
        bookingRequest: BookingRequest,
        paymentMethod: PaymentMethod
    ): PaymentResult {
        // This would integrate with actual payment gateway
        // For now, simulate payment processing
        return PaymentResult(
            success = true,
            transactionId = UUID.randomUUID().toString(),
            error = null
        )
    }

    private suspend fun processRefund(
        booking: BookingRequest,
        refundAmount: Double
    ): PaymentResult {
        // This would integrate with actual payment gateway for refunds
        return PaymentResult(
            success = true,
            transactionId = UUID.randomUUID().toString(),
            error = null
        )
    }

    private fun calculateCancellationPolicy(
        booking: BookingRequest,
        reason: String
    ): CancellationPolicy {
        val daysTillCheckIn = (booking.checkInDate.time - Date().time) / (1000 * 60 * 60 * 24)
        
        return when {
            reason.contains("exam", ignoreCase = true) -> {
                // Student-friendly exam policy
                CancellationPolicy(
                    cancellationFee = 0.0,
                    refundAmount = booking.totalAmount,
                    reason = "Exam period - No cancellation fee"
                )
            }
            daysTillCheckIn > 30 -> {
                CancellationPolicy(
                    cancellationFee = booking.totalAmount * 0.1,
                    refundAmount = booking.totalAmount * 0.9,
                    reason = "More than 30 days notice"
                )
            }
            daysTillCheckIn > 7 -> {
                CancellationPolicy(
                    cancellationFee = booking.totalAmount * 0.25,
                    refundAmount = booking.totalAmount * 0.75,
                    reason = "7-30 days notice"
                )
            }
            else -> {
                CancellationPolicy(
                    cancellationFee = booking.totalAmount * 0.5,
                    refundAmount = booking.totalAmount * 0.5,
                    reason = "Less than 7 days notice"
                )
            }
        }
    }

    private fun generateConfirmationCode(bookingId: String): String {
        return "AL${bookingId.takeLast(6).uppercase()}"
    }

    private suspend fun sendBookingNotifications(booking: BookingRequest, student: StudentUser) {
        // Implementation for sending notifications
        // Email, SMS, push notifications
    }

    private suspend fun sendCancellationNotification(booking: BookingRequest, reason: String) {
        // Implementation for sending cancellation notifications
    }

    private suspend fun updateStudentBookingHistory(studentId: String, bookingId: String) {
        // Implementation for updating student's booking history
    }
}

data class PricingDetails(
    val baseAmount: Double,
    val totalAmount: Double,
    val securityDeposit: Double,
    val discounts: List<Discount>
)

data class Discount(
    val name: String,
    val value: Double,
    val type: DiscountType
)

enum class DiscountType {
    PERCENTAGE,
    FIXED_AMOUNT
}

data class PaymentResult(
    val success: Boolean,
    val transactionId: String?,
    val error: String?
)

data class CancellationPolicy(
    val cancellationFee: Double,
    val refundAmount: Double,
    val reason: String
)