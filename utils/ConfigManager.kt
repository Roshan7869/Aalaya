package com.aalay.app.config

import android.content.Context
import com.aalay.app.BuildConfig
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.FileInputStream
import java.io.InputStream
import java.util.Properties
import javax.inject.Inject
import javax.inject.Singleton
import timber.log.Timber

/**
 * Centralized configuration manager for Aalay app
 * Handles all environment variables, API keys, and application settings
 * 
 * Usage:
 * - Development: Reads from .env file in assets folder
 * - Production: Uses BuildConfig values
 * - Runtime: Provides fallback values and validation
 */
@Singleton
class ConfigManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    
    private val properties = Properties()
    private var isInitialized = false
    
    companion object {
        private const val ENV_FILE_NAME = ".env"
        private const val TAG = "ConfigManager"
        
        // Default fallback values
        private const val DEFAULT_API_TIMEOUT = 30L
        private const val DEFAULT_RETRY_COUNT = 3
        private const val DEFAULT_DB_VERSION = 1
    }
    
    init {
        initializeConfig()
    }
    
    private fun initializeConfig() {
        try {
            // Try to load .env file from assets
            loadEnvFile()
            isInitialized = true
            Timber.d("Configuration loaded successfully")
        } catch (e: Exception) {
            Timber.w(e, "Failed to load .env file, using BuildConfig values")
            isInitialized = true
        }
    }
    
    private fun loadEnvFile() {
        try {
            val inputStream: InputStream = context.assets.open(ENV_FILE_NAME)
            properties.load(inputStream)
            inputStream.close()
        } catch (e: Exception) {
            // If .env file not found in assets, try to load from external storage (development)
            try {
                val externalFile = java.io.File(context.getExternalFilesDir(null), ENV_FILE_NAME)
                if (externalFile.exists()) {
                    val inputStream = FileInputStream(externalFile)
                    properties.load(inputStream)
                    inputStream.close()
                }
            } catch (ex: Exception) {
                Timber.d("No external .env file found")
            }
        }
    }
    
    // ============================================================================
    // APPLICATION CONFIGURATION
    // ============================================================================
    
    val appName: String
        get() = getProperty("APP_NAME", "Aalay")
    
    val appVersion: String
        get() = getProperty("APP_VERSION", BuildConfig.VERSION_NAME)
    
    val packageName: String
        get() = getProperty("PACKAGE_NAME", BuildConfig.APPLICATION_ID)
    
    val environment: String
        get() = getProperty("ENVIRONMENT", if (BuildConfig.DEBUG) "development" else "production")
    
    val isDebugMode: Boolean
        get() = getBooleanProperty("DEBUG_MODE", BuildConfig.DEBUG)
    
    val isLoggingEnabled: Boolean
        get() = getBooleanProperty("ENABLE_LOGGING", BuildConfig.DEBUG)
    
    // ============================================================================
    // API CONFIGURATION
    // ============================================================================
    
    val apiBaseUrl: String
        get() = when (environment) {
            "development" -> getProperty("API_BASE_URL_DEV", BuildConfig.API_BASE_URL)
            "staging" -> getProperty("API_BASE_URL_STAGING", BuildConfig.API_BASE_URL)
            "production" -> getProperty("API_BASE_URL_PROD", BuildConfig.API_BASE_URL)
            else -> BuildConfig.API_BASE_URL
        }
    
    val apiTimeout: Long
        get() = getLongProperty("API_TIMEOUT_SECONDS", DEFAULT_API_TIMEOUT)
    
    val apiRetryCount: Int
        get() = getIntProperty("API_RETRY_COUNT", DEFAULT_RETRY_COUNT)
    
    val apiKey: String?
        get() = getProperty("API_KEY")
    
    val apiSecret: String?
        get() = getProperty("API_SECRET")
    
    val clientId: String?
        get() = getProperty("CLIENT_ID")
    
    val clientSecret: String?
        get() = getProperty("CLIENT_SECRET")
    
    // ============================================================================
    // FIREBASE CONFIGURATION
    // ============================================================================
    
    val firebaseProjectId: String
        get() = when (environment) {
            "development" -> getProperty("FIREBASE_PROJECT_ID_DEV", "aalay-dev")
            "staging" -> getProperty("FIREBASE_PROJECT_ID_STAGING", "aalay-staging")
            "production" -> getProperty("FIREBASE_PROJECT_ID_PROD", "aalay-production")
            else -> "aalay-dev"
        }
    
    val firebaseWebApiKey: String?
        get() = getProperty("FIREBASE_WEB_API_KEY")
    
    val fcmSenderId: String?
        get() = getProperty("FCM_SENDER_ID")
    
    val fcmServerKey: String?
        get() = getProperty("FCM_SERVER_KEY")
    
    val firebaseStorageBucket: String?
        get() = getProperty("FIREBASE_STORAGE_BUCKET")
    
    // ============================================================================
    // GOOGLE SERVICES
    // ============================================================================
    
    val googleWebClientId: String?
        get() = getProperty("GOOGLE_WEB_CLIENT_ID")
    
    val googleAndroidClientId: String?
        get() = getProperty("GOOGLE_ANDROID_CLIENT_ID")
    
    val googleMapsApiKey: String?
        get() = getProperty("GOOGLE_MAPS_API_KEY")
    
    // ============================================================================
    // MAPBOX CONFIGURATION
    // ============================================================================
    
    val mapboxAccessToken: String
        get() = when (environment) {
            "development" -> getProperty("MAPBOX_ACCESS_TOKEN_DEV", BuildConfig.MAPBOX_API_KEY)
            "staging" -> getProperty("MAPBOX_ACCESS_TOKEN_STAGING", BuildConfig.MAPBOX_API_KEY)
            "production" -> getProperty("MAPBOX_ACCESS_TOKEN_PROD", BuildConfig.MAPBOX_API_KEY)
            else -> BuildConfig.MAPBOX_API_KEY
        }
    
    val mapboxStyleUrl: String
        get() = getProperty("MAPBOX_STYLE_URL", "mapbox://styles/mapbox/streets-v11")
    
    val mapboxDirectionsApiUrl: String
        get() = getProperty("MAPBOX_DIRECTIONS_API_URL", "https://api.mapbox.com/directions/v5/mapbox")
    
    val mapboxGeocodingApiUrl: String
        get() = getProperty("MAPBOX_GEOCODING_API_URL", "https://api.mapbox.com/geocoding/v5/mapbox.places")
    
    // ============================================================================
    // SOCIAL AUTHENTICATION
    // ============================================================================
    
    val facebookAppId: String?
        get() = getProperty("FACEBOOK_APP_ID")
    
    val facebookClientToken: String?
        get() = getProperty("FACEBOOK_CLIENT_TOKEN")
    
    val facebookAppSecret: String?
        get() = getProperty("FACEBOOK_APP_SECRET")
    
    // ============================================================================
    // DATABASE CONFIGURATION
    // ============================================================================
    
    val dbName: String
        get() = getProperty("DB_NAME", "aalay_database")
    
    val dbVersion: Int
        get() = getIntProperty("DB_VERSION", DEFAULT_DB_VERSION)
    
    val isDbEncryptionEnabled: Boolean
        get() = getBooleanProperty("DB_ENCRYPTION_ENABLED", true)
    
    val dbEncryptionKey: String?
        get() = getProperty("DB_ENCRYPTION_KEY")
    
    // ============================================================================
    // PAYMENT GATEWAY
    // ============================================================================
    
    val razorpayKeyId: String?
        get() = getProperty("RAZORPAY_KEY_ID")
    
    val razorpayKeySecret: String?
        get() = getProperty("RAZORPAY_KEY_SECRET")
    
    val stripePublishableKey: String?
        get() = getProperty("STRIPE_PUBLISHABLE_KEY")
    
    // ============================================================================
    // SECURITY CONFIGURATION
    // ============================================================================
    
    val isCertPinningEnabled: Boolean
        get() = getBooleanProperty("CERT_PINNING_ENABLED", !isDebugMode)
    
    val primaryCertPin: String?
        get() = getProperty("PRIMARY_CERT_PIN")
    
    val backupCertPin: String?
        get() = getProperty("BACKUP_CERT_PIN")
    
    val isApiRequestSigningEnabled: Boolean
        get() = getBooleanProperty("API_REQUEST_SIGNING_ENABLED", true)
    
    val aesEncryptionKey: String?
        get() = getProperty("AES_ENCRYPTION_KEY")
    
    // ============================================================================
    // FEATURE FLAGS
    // ============================================================================
    
    val isBiometricAuthEnabled: Boolean
        get() = getBooleanProperty("FEATURE_BIOMETRIC_AUTH", true)
    
    val isDarkModeEnabled: Boolean
        get() = getBooleanProperty("FEATURE_DARK_MODE", true)
    
    val isOfflineModeEnabled: Boolean
        get() = getBooleanProperty("FEATURE_OFFLINE_MODE", true)
    
    val isPushNotificationsEnabled: Boolean
        get() = getBooleanProperty("FEATURE_PUSH_NOTIFICATIONS", true)
    
    val isLocationServicesEnabled: Boolean
        get() = getBooleanProperty("FEATURE_LOCATION_SERVICES", true)
    
    val isCameraUploadEnabled: Boolean
        get() = getBooleanProperty("FEATURE_CAMERA_UPLOAD", true)
    
    val isSocialLoginEnabled: Boolean
        get() = getBooleanProperty("FEATURE_SOCIAL_LOGIN", true)
    
    val isPaymentGatewayEnabled: Boolean
        get() = getBooleanProperty("FEATURE_PAYMENT_GATEWAY", true)
    
    // ============================================================================
    // ANALYTICS & MONITORING
    // ============================================================================
    
    val isFirebaseAnalyticsEnabled: Boolean
        get() = getBooleanProperty("FIREBASE_ANALYTICS_ENABLED", true)
    
    val isFirebaseCrashlyticsEnabled: Boolean
        get() = getBooleanProperty("FIREBASE_CRASHLYTICS_ENABLED", !isDebugMode)
    
    val isPerformanceMonitoringEnabled: Boolean
        get() = getBooleanProperty("ENABLE_PERFORMANCE_MONITORING", true)
    
    // ============================================================================
    // DEVELOPMENT TOOLS
    // ============================================================================
    
    val isTestModeEnabled: Boolean
        get() = getBooleanProperty("ENABLE_TEST_MODE", false)
    
    val shouldMockApiResponses: Boolean
        get() = getBooleanProperty("MOCK_API_RESPONSES", false)
    
    val isNetworkLoggingEnabled: Boolean
        get() = getBooleanProperty("ENABLE_NETWORK_LOGGING", isDebugMode)
    
    val isLeakCanaryEnabled: Boolean
        get() = getBooleanProperty("ENABLE_LEAK_CANARY", isDebugMode)
    
    // ============================================================================
    // DEEP LINKING
    // ============================================================================
    
    val appLinkDomain: String
        get() = getProperty("APP_LINK_DOMAIN", "aalay.app")
    
    val deepLinkScheme: String
        get() = getProperty("DEEP_LINK_SCHEME", "aalay")
    
    val deepLinkHost: String
        get() = getProperty("DEEP_LINK_HOST", "app")
    
    // ============================================================================
    // REGIONAL SETTINGS
    // ============================================================================
    
    val defaultLanguage: String
        get() = getProperty("DEFAULT_LANGUAGE", "en")
    
    val supportedLanguages: List<String>
        get() = getProperty("SUPPORTED_LANGUAGES", "en,hi")?.split(",") ?: listOf("en", "hi")
    
    val defaultCurrency: String
        get() = getProperty("DEFAULT_CURRENCY", "INR")
    
    val defaultTimezone: String
        get() = getProperty("DEFAULT_TIMEZONE", "Asia/Kolkata")
    
    // ============================================================================
    // HELPER METHODS
    // ============================================================================
    
    private fun getProperty(key: String, defaultValue: String? = null): String? {
        return properties.getProperty(key) ?: defaultValue
    }
    
    private fun getBooleanProperty(key: String, defaultValue: Boolean = false): Boolean {
        return getProperty(key)?.toBoolean() ?: defaultValue
    }
    
    private fun getIntProperty(key: String, defaultValue: Int = 0): Int {
        return getProperty(key)?.toIntOrNull() ?: defaultValue
    }
    
    private fun getLongProperty(key: String, defaultValue: Long = 0L): Long {
        return getProperty(key)?.toLongOrNull() ?: defaultValue
    }
    
    private fun getFloatProperty(key: String, defaultValue: Float = 0f): Float {
        return getProperty(key)?.toFloatOrNull() ?: defaultValue
    }
    
    /**
     * Get all properties for debugging purposes (excludes sensitive keys)
     */
    fun getAllProperties(): Map<String, String> {
        val sensitiveKeys = listOf(
            "API_SECRET", "CLIENT_SECRET", "FIREBASE_WEB_API_KEY", "FCM_SERVER_KEY",
            "FACEBOOK_APP_SECRET", "DB_ENCRYPTION_KEY", "RAZORPAY_KEY_SECRET",
            "AES_ENCRYPTION_KEY", "RSA_PRIVATE_KEY", "AWS_SECRET_ACCESS_KEY"
        )
        
        return properties.entries.associate { (key, value) ->
            val keyStr = key.toString()
            val valueStr = if (sensitiveKeys.any { keyStr.contains(it, ignoreCase = true) }) {
                "***HIDDEN***"
            } else {
                value.toString()
            }
            keyStr to valueStr
        }
    }
    
    /**
     * Validate that all required configuration values are present
     */
    fun validateConfiguration(): ConfigValidationResult {
        val errors = mutableListOf<String>()
        val warnings = mutableListOf<String>()
        
        // Check required API configurations
        if (apiBaseUrl.isBlank()) {
            errors.add("API_BASE_URL is required")
        }
        
        if (mapboxAccessToken.isBlank()) {
            errors.add("MAPBOX_ACCESS_TOKEN is required")
        }
        
        // Check Firebase configuration for production
        if (!isDebugMode) {
            if (firebaseWebApiKey.isNullOrBlank()) {
                warnings.add("FIREBASE_WEB_API_KEY should be configured for production")
            }
            
            if (fcmSenderId.isNullOrBlank()) {
                warnings.add("FCM_SENDER_ID should be configured for push notifications")
            }
        }
        
        // Check security configuration for production
        if (!isDebugMode) {
            if (primaryCertPin.isNullOrBlank() && isCertPinningEnabled) {
                warnings.add("Certificate pinning is enabled but no certificate pins configured")
            }
            
            if (aesEncryptionKey.isNullOrBlank()) {
                warnings.add("AES encryption key should be configured for production")
            }
        }
        
        return ConfigValidationResult(
            isValid = errors.isEmpty(),
            errors = errors,
            warnings = warnings
        )
    }
    
    /**
     * Check if configuration is properly initialized
     */
    fun isConfigurationReady(): Boolean = isInitialized
}

/**
 * Result of configuration validation
 */
data class ConfigValidationResult(
    val isValid: Boolean,
    val errors: List<String>,
    val warnings: List<String>
)

/**
 * Configuration environments
 */
enum class Environment {
    DEVELOPMENT,
    STAGING,
    PRODUCTION;
    
    companion object {
        fun fromString(value: String): Environment {
            return when (value.lowercase()) {
                "development", "dev" -> DEVELOPMENT
                "staging", "stage" -> STAGING
                "production", "prod" -> PRODUCTION
                else -> DEVELOPMENT
            }
        }
    }
}