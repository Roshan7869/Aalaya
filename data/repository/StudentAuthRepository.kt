package com.aalay.app.data.repository

import android.content.Context
import android.net.Uri
import com.aalay.app.data.local.dao.StudentPreferencesDao
import com.aalay.app.data.local.entity.StudentPreferencesEntity
import com.aalay.app.data.remote.AccommodationApiService
import com.aalay.app.data.models.*
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StudentAuthRepository @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage,
    private val apiService: AccommodationApiService,
    private val studentPreferencesDao: StudentPreferencesDao,
    @ApplicationContext private val context: Context
) {
    
    private val googleSignInClient: GoogleSignInClient by lazy {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(context.getString(com.aalay.app.R.string.default_web_client_id))
            .requestEmail()
            .build()
        GoogleSignIn.getClient(context, gso)
    }
    
    // User authentication state flow
    val authStateFlow: Flow<AuthState> = callbackFlow {
        val authStateListener = FirebaseAuth.AuthStateListener { auth ->
            val user = auth.currentUser
            if (user != null) {
                trySend(AuthState.Authenticated(user.toStudentUser()))
            } else {
                trySend(AuthState.Unauthenticated)
            }
        }
        
        firebaseAuth.addAuthStateListener(authStateListener)
        
        awaitClose {
            firebaseAuth.removeAuthStateListener(authStateListener)
        }
    }
    
    /**
     * Register student with email and password
     */
    suspend fun registerWithEmail(
        email: String,
        password: String,
        studentInfo: StudentRegistrationInfo
    ): Result<StudentUser> {
        return try {
            // Validate student email domain if provided
            if (isStudentEmail(email)) {
                studentInfo.isStudentEmailVerified = true
            }
            
            val authResult = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
            val firebaseUser = authResult.user ?: throw Exception("Registration failed")
            
            // Send email verification
            firebaseUser.sendEmailVerification().await()
            
            // Create student profile
            val studentUser = StudentUser(
                id = firebaseUser.uid,
                email = email,
                fullName = studentInfo.fullName,
                phoneNumber = studentInfo.phoneNumber,
                collegeName = studentInfo.collegeName,
                courseOfStudy = studentInfo.courseOfStudy,
                graduationYear = studentInfo.graduationYear,
                isStudentVerified = studentInfo.isStudentEmailVerified,
                isEmailVerified = false,
                profilePhotoUrl = null,
                createdAt = System.currentTimeMillis(),
                lastLoginAt = System.currentTimeMillis()
            )
            
            // Save to Firestore
            saveStudentProfile(studentUser)
            
            // Initialize preferences
            initializeStudentPreferences(studentUser.id, studentInfo)
            
            Result.success(studentUser)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Login with email and password
     */
    suspend fun loginWithEmail(email: String, password: String): Result<StudentUser> {
        return try {
            val authResult = firebaseAuth.signInWithEmailAndPassword(email, password).await()
            val firebaseUser = authResult.user ?: throw Exception("Login failed")
            
            // Update last login time
            updateLastLogin(firebaseUser.uid)
            
            // Get student profile
            val studentUser = getStudentProfile(firebaseUser.uid)
                ?: throw Exception("Student profile not found")
            
            Result.success(studentUser)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Login with phone OTP
     */
    suspend fun sendOTP(phoneNumber: String): Result<String> {
        return try {
            // This would typically use Firebase Phone Auth
            // For now, we'll simulate OTP sending
            val verificationId = "verification_$phoneNumber"
            
            // In real implementation, use PhoneAuthProvider.verifyPhoneNumber()
            Result.success(verificationId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun verifyOTP(verificationId: String, otp: String): Result<StudentUser> {
        return try {
            // Create credential and sign in
            val credential = PhoneAuthProvider.getCredential(verificationId, otp)
            val authResult = firebaseAuth.signInWithCredential(credential).await()
            val firebaseUser = authResult.user ?: throw Exception("OTP verification failed")
            
            // Check if student profile exists, create minimal one if not
            var studentUser = getStudentProfile(firebaseUser.uid)
            if (studentUser == null) {
                studentUser = StudentUser(
                    id = firebaseUser.uid,
                    phoneNumber = firebaseUser.phoneNumber,
                    isPhoneVerified = true,
                    createdAt = System.currentTimeMillis(),
                    lastLoginAt = System.currentTimeMillis()
                )
                saveStudentProfile(studentUser)
            } else {
                updateLastLogin(firebaseUser.uid)
            }
            
            Result.success(studentUser)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Google Sign In
     */
    suspend fun signInWithGoogle(account: GoogleSignInAccount): Result<StudentUser> {
        return try {
            val credential = GoogleAuthProvider.getCredential(account.idToken, null)
            val authResult = firebaseAuth.signInWithCredential(credential).await()
            val firebaseUser = authResult.user ?: throw Exception("Google sign-in failed")
            
            // Check if student profile exists
            var studentUser = getStudentProfile(firebaseUser.uid)
            if (studentUser == null) {
                // Create new student profile from Google account
                studentUser = StudentUser(
                    id = firebaseUser.uid,
                    email = firebaseUser.email,
                    fullName = firebaseUser.displayName,
                    profilePhotoUrl = firebaseUser.photoUrl?.toString(),
                    isEmailVerified = firebaseUser.isEmailVerified,
                    isStudentVerified = isStudentEmail(firebaseUser.email ?: ""),
                    createdAt = System.currentTimeMillis(),
                    lastLoginAt = System.currentTimeMillis()
                )
                saveStudentProfile(studentUser)
            } else {
                updateLastLogin(firebaseUser.uid)
            }
            
            Result.success(studentUser)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Facebook Sign In (placeholder implementation)
     */
    suspend fun signInWithFacebook(accessToken: String): Result<StudentUser> {
        return try {
            val credential = FacebookAuthProvider.getCredential(accessToken)
            val authResult = firebaseAuth.signInWithCredential(credential).await()
            val firebaseUser = authResult.user ?: throw Exception("Facebook sign-in failed")
            
            // Similar implementation to Google sign-in
            var studentUser = getStudentProfile(firebaseUser.uid)
            if (studentUser == null) {
                studentUser = StudentUser(
                    id = firebaseUser.uid,
                    email = firebaseUser.email,
                    fullName = firebaseUser.displayName,
                    profilePhotoUrl = firebaseUser.photoUrl?.toString(),
                    isEmailVerified = firebaseUser.isEmailVerified,
                    createdAt = System.currentTimeMillis(),
                    lastLoginAt = System.currentTimeMillis()
                )
                saveStudentProfile(studentUser)
            } else {
                updateLastLogin(firebaseUser.uid)
            }
            
            Result.success(studentUser)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Student verification with ID upload
     */
    suspend fun verifyStudentWithId(
        studentId: String,
        idImageUri: Uri,
        studentInfo: StudentVerificationInfo
    ): Result<Boolean> {
        return try {
            // Upload student ID image to Firebase Storage
            val imageRef = storage.reference.child("student_ids/$studentId/${System.currentTimeMillis()}.jpg")
            val uploadTask = imageRef.putFile(idImageUri).await()
            val downloadUrl = uploadTask.storage.downloadUrl.await()
            
            // Create verification request
            val verificationRequest = StudentVerificationRequest(
                studentId = studentId,
                idImageUrl = downloadUrl.toString(),
                collegeName = studentInfo.collegeName,
                courseOfStudy = studentInfo.courseOfStudy,
                studentIdNumber = studentInfo.studentIdNumber,
                graduationYear = studentInfo.graduationYear,
                submittedAt = System.currentTimeMillis()
            )
            
            // Submit to backend for manual verification
            val response = apiService.submitStudentVerification(verificationRequest)
            if (response.isSuccessful) {
                // Update student profile with pending verification status
                updateStudentVerificationStatus(studentId, VerificationStatus.PENDING)
                Result.success(true)
            } else {
                Result.failure(Exception("Failed to submit verification"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get current student profile
     */
    suspend fun getCurrentStudent(): StudentUser? {
        val currentUser = firebaseAuth.currentUser ?: return null
        return getStudentProfile(currentUser.uid)
    }
    
    /**
     * Update student profile
     */
    suspend fun updateStudentProfile(
        studentId: String,
        updates: StudentProfileUpdate
    ): Result<StudentUser> {
        return try {
            val currentProfile = getStudentProfile(studentId)
                ?: throw Exception("Student profile not found")
            
            val updatedProfile = currentProfile.copy(
                fullName = updates.fullName ?: currentProfile.fullName,
                phoneNumber = updates.phoneNumber ?: currentProfile.phoneNumber,
                collegeName = updates.collegeName ?: currentProfile.collegeName,
                courseOfStudy = updates.courseOfStudy ?: currentProfile.courseOfStudy,
                graduationYear = updates.graduationYear ?: currentProfile.graduationYear,
                bio = updates.bio ?: currentProfile.bio,
                interests = updates.interests ?: currentProfile.interests,
                updatedAt = System.currentTimeMillis()
            )
            
            saveStudentProfile(updatedProfile)
            Result.success(updatedProfile)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Upload and update profile photo
     */
    suspend fun updateProfilePhoto(studentId: String, photoUri: Uri): Result<String> {
        return try {
            val photoRef = storage.reference.child("profile_photos/$studentId.jpg")
            val uploadTask = photoRef.putFile(photoUri).await()
            val downloadUrl = uploadTask.storage.downloadUrl.await().toString()
            
            // Update profile with new photo URL
            firestore.collection("students")
                .document(studentId)
                .update("profilePhotoUrl", downloadUrl)
                .await()
            
            Result.success(downloadUrl)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Reset password
     */
    suspend fun resetPassword(email: String): Result<Boolean> {
        return try {
            firebaseAuth.sendPasswordResetEmail(email).await()
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Sign out
     */
    suspend fun signOut(): Result<Boolean> {
        return try {
            firebaseAuth.signOut()
            googleSignInClient.signOut().await()
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Delete account
     */
    suspend fun deleteAccount(studentId: String): Result<Boolean> {
        return try {
            // Delete from Firestore
            firestore.collection("students").document(studentId).delete().await()
            
            // Delete local preferences
            studentPreferencesDao.deletePreferences(studentId)
            
            // Delete Firebase Auth account
            firebaseAuth.currentUser?.delete()?.await()
            
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Sign in with email and password (simplified method for AuthViewModel)
     */
    suspend fun signInWithEmail(email: String, password: String): Result<StudentUser> {
        return loginWithEmail(email, password)
    }
    
    /**
     * Sign up with email and password (simplified method for AuthViewModel)
     */
    suspend fun signUpWithEmail(
        email: String,
        password: String,
        firstName: String,
        lastName: String
    ): Result<StudentUser> {
        val registrationInfo = StudentRegistrationInfo(
            fullName = "$firstName $lastName",
            email = email
        )
        return registerWithEmail(email, password, registrationInfo)
    }
    
    /**
     * Sign in with Google (simplified method for AuthViewModel)
     */
    suspend fun signInWithGoogle(): Result<StudentUser> {
        return try {
            // This would typically handle the Google Sign-In flow
            // For now, return a generic error to indicate setup needed
            Result.failure(Exception("Google Sign-In requires additional setup"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get current user (for ProfileViewModel)
     */
    suspend fun getCurrentUser(): StudentUser? {
        return getCurrentStudent()
    }
    
    /**
     * Update user profile (for ProfileViewModel)
     */
    suspend fun updateUserProfile(updatedUser: StudentUser): Result<StudentUser> {
        return try {
            saveStudentProfile(updatedUser)
            Result.success(updatedUser)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Private helper methods
    
    private suspend fun getStudentProfile(studentId: String): StudentUser? {
        return try {
            val doc = firestore.collection("students").document(studentId).get().await()
            doc.toObject(StudentUser::class.java)
        } catch (e: Exception) {
            null
        }
    }
    
    private suspend fun saveStudentProfile(studentUser: StudentUser) {
        firestore.collection("students")
            .document(studentUser.id)
            .set(studentUser)
            .await()
    }
    
    private suspend fun updateLastLogin(studentId: String) {
        firestore.collection("students")
            .document(studentId)
            .update("lastLoginAt", System.currentTimeMillis())
            .await()
    }
    
    private suspend fun updateStudentVerificationStatus(
        studentId: String,
        status: VerificationStatus
    ) {
        firestore.collection("students")
            .document(studentId)
            .update("verificationStatus", status.name)
            .await()
    }
    
    private suspend fun initializeStudentPreferences(
        studentId: String,
        registrationInfo: StudentRegistrationInfo
    ) {
        val preferences = StudentPreferencesEntity(
            studentId = studentId,
            preferredColleges = registrationInfo.collegeName?.let { listOf(it) } ?: emptyList(),
            preferredBudget = registrationInfo.budgetRange?.toDouble(),
            preferredAmenities = registrationInfo.preferredAmenities ?: emptyList(),
            preferredRoomType = registrationInfo.preferredRoomType,
            lastUpdated = System.currentTimeMillis()
        )
        
        studentPreferencesDao.insertOrUpdatePreferences(preferences)
    }
    
    private fun isStudentEmail(email: String): Boolean {
        val studentDomains = listOf(
            ".edu", ".ac.", ".edu.", "student.", "university", "college"
        )
        return studentDomains.any { domain -> 
            email.contains(domain, ignoreCase = true) 
        }
    }
}

// Extension function to convert Firebase user to StudentUser
private fun FirebaseUser.toStudentUser(): StudentUser {
    return StudentUser(
        id = this.uid,
        email = this.email,
        fullName = this.displayName,
        phoneNumber = this.phoneNumber,
        profilePhotoUrl = this.photoUrl?.toString(),
        isEmailVerified = this.isEmailVerified,
        isPhoneVerified = this.phoneNumber != null,
        lastLoginAt = System.currentTimeMillis()
    )
}

// Data classes for authentication
data class StudentRegistrationInfo(
    val fullName: String,
    val email: String,
    val phoneNumber: String? = null,
    val collegeName: String? = null,
    val courseOfStudy: String? = null,
    val graduationYear: Int? = null,
    val budgetRange: String? = null,
    val preferredAmenities: List<String>? = null,
    val preferredRoomType: String? = null,
    var isStudentEmailVerified: Boolean = false
)

data class StudentVerificationInfo(
    val collegeName: String,
    val courseOfStudy: String,
    val studentIdNumber: String,
    val graduationYear: Int
)

data class StudentVerificationRequest(
    val studentId: String,
    val idImageUrl: String,
    val collegeName: String,
    val courseOfStudy: String,
    val studentIdNumber: String,
    val graduationYear: Int,
    val submittedAt: Long
)

data class StudentProfileUpdate(
    val fullName: String? = null,
    val phoneNumber: String? = null,
    val collegeName: String? = null,
    val courseOfStudy: String? = null,
    val graduationYear: Int? = null,
    val bio: String? = null,
    val interests: List<String>? = null
)

enum class VerificationStatus {
    PENDING, APPROVED, REJECTED
}

sealed class AuthState {
    object Unauthenticated : AuthState()
    data class Authenticated(val user: StudentUser) : AuthState()
}