package com.aalay.app.utils

import android.content.Context
import android.util.Base64
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.aalay.app.BuildConfig
import com.aalay.app.config.ConfigManager
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Security configuration manager for Aalay app
 * Handles secure storage of API keys, tokens, and sensitive data
 */
@Singleton
class SecurityConfig @Inject constructor(
    @ApplicationContext private val context: Context,
    private val configManager: ConfigManager
) {
    
    companion object {
        private const val ENCRYPTED_PREFS_FILE = "aalay_secure_prefs"
        private const val KEY_API_TOKEN = "api_token"
        private const val KEY_USER_AUTH_TOKEN = "user_auth_token"
        private const val KEY_REFRESH_TOKEN = "refresh_token"
        private const val KEY_DEVICE_ID = "device_id"
    }
    
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()
    
    private val encryptedSharedPreferences = EncryptedSharedPreferences.create(
        context,
        ENCRYPTED_PREFS_FILE,
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )
    
    /**
     * Get API base URL based on build configuration
     */
    fun getApiBaseUrl(): String {
        return configManager.apiBaseUrl
    }
    
    /**
     * Get Mapbox API key based on build configuration
     */
    fun getMapboxApiKey(): String {
        return configManager.mapboxAccessToken
    }
    
    /**
     * Store user authentication token securely
     */
    fun storeAuthToken(token: String) {
        encryptedSharedPreferences.edit()
            .putString(KEY_USER_AUTH_TOKEN, token)
            .apply()
    }
    
    /**
     * Retrieve user authentication token
     */
    fun getAuthToken(): String? {
        return encryptedSharedPreferences.getString(KEY_USER_AUTH_TOKEN, null)
    }
    
    /**
     * Store refresh token securely
     */
    fun storeRefreshToken(token: String) {
        encryptedSharedPreferences.edit()
            .putString(KEY_REFRESH_TOKEN, token)
            .apply()
    }
    
    /**
     * Retrieve refresh token
     */
    fun getRefreshToken(): String? {
        return encryptedSharedPreferences.getString(KEY_REFRESH_TOKEN, null)
    }
    
    /**
     * Store API token securely
     */
    fun storeApiToken(token: String) {
        encryptedSharedPreferences.edit()
            .putString(KEY_API_TOKEN, token)
            .apply()
    }
    
    /**
     * Retrieve API token
     */
    fun getApiToken(): String? {
        return encryptedSharedPreferences.getString(KEY_API_TOKEN, null)
    }
    
    /**
     * Generate or retrieve device ID
     */
    fun getDeviceId(): String {
        var deviceId = encryptedSharedPreferences.getString(KEY_DEVICE_ID, null)
        if (deviceId == null) {
            deviceId = generateDeviceId()
            encryptedSharedPreferences.edit()
                .putString(KEY_DEVICE_ID, deviceId)
                .apply()
        }
        return deviceId
    }
    
    /**
     * Clear all stored tokens (for logout)
     */
    fun clearAllTokens() {
        encryptedSharedPreferences.edit()
            .remove(KEY_USER_AUTH_TOKEN)
            .remove(KEY_REFRESH_TOKEN)
            .remove(KEY_API_TOKEN)
            .apply()
    }
    
    /**
     * Get certificate pins for network security
     */
    fun getCertificatePins(): List<String> {
        val pins = mutableListOf<String>()
        configManager.primaryCertPin?.let { pins.add(it) }
        configManager.backupCertPin?.let { pins.add(it) }
        return pins.ifEmpty { 
            listOf(
                "sha256/DEFAULT_PRIMARY_PIN",
                "sha256/DEFAULT_BACKUP_PIN"
            )
        }
    }
    
    /**
     * Check if app is running in debug mode
     */
    fun isDebugMode(): Boolean {
        return configManager.isDebugMode
    }
    
    /**
     * Check if app is running in production
     */
    fun isProduction(): Boolean {
        return configManager.environment == "production"
    }
    
    /**
     * Get obfuscated user agent for API calls
     */
    fun getUserAgent(): String {
        return "Aalay/${BuildConfig.VERSION_NAME} (Android ${android.os.Build.VERSION.RELEASE}; ${android.os.Build.MODEL})"
    }
    
    /**
     * Generate request signature for API security
     */
    fun generateRequestSignature(endpoint: String, timestamp: Long, nonce: String): String {
        val data = "$endpoint$timestamp$nonce"
        // In production, use proper HMAC-SHA256 with secret key
        return Base64.encodeToString(data.toByteArray(), Base64.NO_WRAP)
    }
    
    /**
     * Validate API response signature
     */
    fun validateResponseSignature(response: String, signature: String): Boolean {
        // Implement response signature validation
        // For security, this should use proper cryptographic verification
        return true // Placeholder implementation
    }
    
    /**
     * Get biometric encryption configuration
     */
    fun getBiometricConfig(): BiometricConfig {
        return BiometricConfig(
            title = "Aalay Security",
            subtitle = "Use your biometric to authenticate",
            description = "Secure access to your account and bookings",
            negativeButtonText = "Use Password"
        )
    }
    
    private fun generateDeviceId(): String {
        val timestamp = System.currentTimeMillis()
        val random = (0..999999).random()
        val deviceInfo = "${android.os.Build.MODEL}_${android.os.Build.BRAND}"
        return Base64.encodeToString("$deviceInfo$timestamp$random".toByteArray(), Base64.NO_WRAP)
            .take(16) // Limit to 16 characters
    }
}

/**
 * Biometric authentication configuration
 */
data class BiometricConfig(
    val title: String,
    val subtitle: String,
    val description: String,
    val negativeButtonText: String
)

/**
 * Network security interceptor for OkHttp
 */
class SecurityInterceptor @Inject constructor(
    private val securityConfig: SecurityConfig
) : okhttp3.Interceptor {
    
    override fun intercept(chain: okhttp3.Interceptor.Chain): okhttp3.Response {
        val originalRequest = chain.request()
        val timestamp = System.currentTimeMillis()
        val nonce = generateNonce()
        
        val secureRequest = originalRequest.newBuilder()
            .addHeader("User-Agent", securityConfig.getUserAgent())
            .addHeader("X-Device-ID", securityConfig.getDeviceId())
            .addHeader("X-Timestamp", timestamp.toString())
            .addHeader("X-Nonce", nonce)
            .addHeader("X-Signature", securityConfig.generateRequestSignature(
                originalRequest.url.encodedPath, timestamp, nonce))
            .apply {
                securityConfig.getAuthToken()?.let { token ->
                    addHeader("Authorization", "Bearer $token")
                }
                securityConfig.getApiToken()?.let { apiToken ->
                    addHeader("X-API-Token", apiToken)
                }
            }
            .build()
        
        return chain.proceed(secureRequest)
    }
    
    private fun generateNonce(): String {
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"
        return (1..16)
            .map { chars.random() }
            .joinToString("")
    }
}