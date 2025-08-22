package com.aalay.app

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import androidx.core.content.ContextCompat
import com.aalay.app.config.ConfigManager
import com.google.firebase.FirebaseApp
import com.google.firebase.crashlytics.FirebaseCrashlytics
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber
import javax.inject.Inject

/**
 * Main Application class for Aalay Student Housing App
 * Handles app-wide initialization and dependency injection
 */
@HiltAndroidApp
class AalayApplication : Application() {
    
    @Inject
    lateinit var configManager: ConfigManager

    companion object {
        const val NOTIFICATION_CHANNEL_DEFAULT = "aalay_default"
        const val NOTIFICATION_CHANNEL_BOOKINGS = "aalay_bookings"
        const val NOTIFICATION_CHANNEL_ROOMMATE = "aalay_roommate"
        const val NOTIFICATION_CHANNEL_DEALS = "aalay_deals"
    }

    override fun onCreate() {
        super.onCreate()
        
        // Initialize configuration first
        initializeConfiguration()
        
        // Initialize Firebase
        FirebaseApp.initializeApp(this)
        
        // Initialize logging
        initializeLogging()
        
        // Create notification channels
        createNotificationChannels()
        
        // Initialize Crashlytics
        initializeCrashlytics()
        
        Timber.d("Aalay Application initialized successfully")
    }

    private fun initializeConfiguration() {
        // Validate configuration
        if (::configManager.isInitialized) {
            val validationResult = configManager.validateConfiguration()
            
            if (!validationResult.isValid) {
                Timber.e("Configuration validation failed: ${validationResult.errors.joinToString(", ")}")
            }
            
            if (validationResult.warnings.isNotEmpty()) {
                Timber.w("Configuration warnings: ${validationResult.warnings.joinToString(", ")}")
            }
            
            Timber.d("Configuration environment: ${configManager.environment}")
            Timber.d("API Base URL: ${configManager.apiBaseUrl}")
        } else {
            Timber.w("ConfigManager not initialized via Hilt yet")
        }
    }

    private fun initializeLogging() {
        if (::configManager.isInitialized && configManager.isLoggingEnabled) {
            // Plant debug tree for development
            Timber.plant(Timber.DebugTree())
        } else if (!BuildConfig.DEBUG) {
            // Plant production tree with crash reporting
            Timber.plant(CrashlyticsTree())
        }
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = ContextCompat.getSystemService(
                this,
                NotificationManager::class.java
            ) as NotificationManager

            // Default notifications channel
            val defaultChannel = NotificationChannel(
                NOTIFICATION_CHANNEL_DEFAULT,
                getString(R.string.notification_channel_default_name),
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = getString(R.string.notification_channel_default_description)
                enableLights(true)
                lightColor = ContextCompat.getColor(this@AalayApplication, R.color.primary_color)
                enableVibration(true)
            }

            // Booking notifications channel
            val bookingsChannel = NotificationChannel(
                NOTIFICATION_CHANNEL_BOOKINGS,
                getString(R.string.notification_channel_bookings_name),
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = getString(R.string.notification_channel_bookings_description)
                enableLights(true)
                lightColor = ContextCompat.getColor(this@AalayApplication, R.color.success_color)
                enableVibration(true)
            }

            // Roommate matching channel
            val roommateChannel = NotificationChannel(
                NOTIFICATION_CHANNEL_ROOMMATE,
                getString(R.string.notification_channel_roommate_name),
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = getString(R.string.notification_channel_roommate_description)
                enableLights(true)
                lightColor = ContextCompat.getColor(this@AalayApplication, R.color.accent_color)
            }

            // Deals and offers channel
            val dealsChannel = NotificationChannel(
                NOTIFICATION_CHANNEL_DEALS,
                getString(R.string.notification_channel_deals_name),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = getString(R.string.notification_channel_deals_description)
            }

            notificationManager.createNotificationChannels(
                listOf(defaultChannel, bookingsChannel, roommateChannel, dealsChannel)
            )
        }
    }

    private fun initializeCrashlytics() {
        val enableCrashlytics = if (::configManager.isInitialized) {
            configManager.isFirebaseCrashlyticsEnabled
        } else {
            !BuildConfig.DEBUG
        }
        
        if (enableCrashlytics) {
            FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(true)
            
            // Set custom keys for debugging
            FirebaseCrashlytics.getInstance().apply {
                setCustomKey("app_version", BuildConfig.VERSION_NAME)
                setCustomKey("version_code", BuildConfig.VERSION_CODE)
                setCustomKey("build_type", BuildConfig.BUILD_TYPE)
                
                if (::configManager.isInitialized) {
                    setCustomKey("environment", configManager.environment)
                    setCustomKey("api_base_url", configManager.apiBaseUrl)
                }
            }
        }
    }
}

/**
 * Custom Timber tree for production crash reporting
 */
class CrashlyticsTree : Timber.Tree() {
    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        if (priority == android.util.Log.VERBOSE || priority == android.util.Log.DEBUG) {
            return
        }

        val crashlytics = FirebaseCrashlytics.getInstance()
        
        // Set custom key for priority and tag
        crashlytics.setCustomKey("priority", priority)
        crashlytics.setCustomKey("tag", tag ?: "Unknown")
        
        // Log message to Crashlytics
        crashlytics.log(message)
        
        // Report exception if available
        t?.let { throwable ->
            crashlytics.recordException(throwable)
        }
    }
}
