package com.aalay.app

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import androidx.core.content.ContextCompat
import com.google.firebase.FirebaseApp
import com.google.firebase.crashlytics.FirebaseCrashlytics
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber

/**
 * Main Application class for Aalay Student Housing App
 * Handles app-wide initialization and dependency injection
 */
@HiltAndroidApp
class AalayApplication : Application() {

    companion object {
        const val NOTIFICATION_CHANNEL_DEFAULT = "aalay_default"
        const val NOTIFICATION_CHANNEL_BOOKINGS = "aalay_bookings"
        const val NOTIFICATION_CHANNEL_ROOMMATE = "aalay_roommate"
        const val NOTIFICATION_CHANNEL_DEALS = "aalay_deals"
    }

    override fun onCreate() {
        super.onCreate()
        
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

    private fun initializeLogging() {
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        } else {
            Timber.plant(CrashlyticsTree())
        }
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = ContextCompat.getSystemService(
                this,
                NotificationManager::class.java
            ) as NotificationManager

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

            val roommateChannel = NotificationChannel(
                NOTIFICATION_CHANNEL_ROOMMATE,
                getString(R.string.notification_channel_roommate_name),
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = getString(R.string.notification_channel_roommate_description)
                enableLights(true)
                lightColor = ContextCompat.getColor(this@AalayApplication, R.color.accent_color)
            }

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
        if (!BuildConfig.DEBUG) {
            FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(true)
            FirebaseCrashlytics.getInstance().apply {
                setCustomKey("app_version", BuildConfig.VERSION_NAME)
                setCustomKey("version_code", BuildConfig.VERSION_CODE)
                setCustomKey("build_type", BuildConfig.BUILD_TYPE)
            }
        }
    }
}

class CrashlyticsTree : Timber.Tree() {
    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        if (priority == android.util.Log.VERBOSE || priority == android.util.Log.DEBUG) {
            return
        }

        val crashlytics = FirebaseCrashlytics.getInstance()
        crashlytics.setCustomKey("priority", priority)
        crashlytics.setCustomKey("tag", tag ?: "Unknown")
        crashlytics.log(message)
        t?.let { throwable ->
            crashlytics.recordException(throwable)
        }
    }
}
