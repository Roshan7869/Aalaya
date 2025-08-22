package com.aalay.app.services

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.os.IBinder
import android.os.Looper
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.aalay.app.AalayApplication
import com.aalay.app.R
import com.aalay.app.ui.activities.MainActivity
import com.google.android.gms.location.*
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import timber.log.Timber
import javax.inject.Inject

/**
 * Location service for tracking user location in background
 * Used for proximity alerts and location-based recommendations
 */
@AndroidEntryPoint
class LocationService : Service() {

    companion object {
        private const val NOTIFICATION_ID = 2000
        private const val LOCATION_UPDATE_INTERVAL = 30000L // 30 seconds
        private const val LOCATION_FASTEST_INTERVAL = 15000L // 15 seconds
        private const val LOCATION_MAX_WAIT_TIME = 60000L // 1 minute
        
        const val ACTION_START_LOCATION_SERVICE = "START_LOCATION_SERVICE"
        const val ACTION_STOP_LOCATION_SERVICE = "STOP_LOCATION_SERVICE"
    }

    @Inject
    lateinit var notificationManager: NotificationManager

    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback
    
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var isLocationUpdatesActive = false

    override fun onCreate() {
        super.onCreate()
        
        // Initialize notification manager if not injected
        if (!::notificationManager.isInitialized) {
            notificationManager = ContextCompat.getSystemService(
                this,
                NotificationManager::class.java
            ) as NotificationManager
        }

        // Initialize location components
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        
        createLocationRequest()
        createLocationCallback()
        
        Timber.d("LocationService created")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START_LOCATION_SERVICE -> {
                startLocationUpdates()
                startForeground(NOTIFICATION_ID, createNotification())
                Timber.d("Location service started")
            }
            ACTION_STOP_LOCATION_SERVICE -> {
                stopLocationUpdates()
                stopSelf()
                Timber.d("Location service stopped")
            }
        }
        
        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        stopLocationUpdates()
        serviceScope.cancel()
        Timber.d("LocationService destroyed")
    }

    private fun createLocationRequest() {
        locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_BALANCED_POWER_ACCURACY,
            LOCATION_UPDATE_INTERVAL
        ).apply {
            setMinUpdateDistanceMeters(10f) // Update only if moved 10 meters
            setGranularity(Granularity.GRANULARITY_PERMISSION_LEVEL)
            setWaitForAccurateLocation(false)
            setMinUpdateIntervalMillis(LOCATION_FASTEST_INTERVAL)
            setMaxUpdateDelayMillis(LOCATION_MAX_WAIT_TIME)
        }.build()
    }

    private fun createLocationCallback() {
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                super.onLocationResult(locationResult)
                
                val location = locationResult.lastLocation
                location?.let { currentLocation ->
                    processLocationUpdate(currentLocation)
                }
            }

            override fun onLocationAvailability(locationAvailability: LocationAvailability) {
                super.onLocationAvailability(locationAvailability)
                
                if (!locationAvailability.isLocationAvailable) {
                    Timber.w("Location is not available")
                } else {
                    Timber.d("Location is available")
                }
            }
        }
    }

    private fun startLocationUpdates() {
        if (!hasLocationPermission()) {
            Timber.w("Location permission not granted")
            stopSelf()
            return
        }

        if (isLocationUpdatesActive) {
            Timber.d("Location updates already active")
            return
        }

        try {
            fusedLocationProviderClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
            isLocationUpdatesActive = true
            Timber.d("Location updates started")
            
        } catch (unlikely: SecurityException) {
            Timber.e(unlikely, "Lost location permission. Could not request updates.")
            stopSelf()
        }
    }

    private fun stopLocationUpdates() {
        if (!isLocationUpdatesActive) {
            return
        }

        fusedLocationProviderClient.removeLocationUpdates(locationCallback)
        isLocationUpdatesActive = false
        Timber.d("Location updates stopped")
    }

    private fun processLocationUpdate(location: Location) {
        Timber.d(
            "Location update: ${location.latitude}, ${location.longitude}, " +
            "accuracy: ${location.accuracy}m"
        )

        // Process location in background
        serviceScope.launch {
            try {
                // Update location in local database
                updateLocationInDatabase(location)
                
                // Check for nearby accommodations and send notifications if needed
                checkNearbyAccommodations(location)
                
                // Check proximity to saved colleges
                checkCollegeProximity(location)
                
            } catch (e: Exception) {
                Timber.e(e, "Error processing location update")
            }
        }
    }

    private suspend fun updateLocationInDatabase(location: Location) {
        // TODO: Implement database update
        // Save current location to local database for offline access
        Timber.d("TODO: Update location in database")
    }

    private suspend fun checkNearbyAccommodations(location: Location) {
        // TODO: Implement nearby accommodations check
        // Check for new accommodations within user's preferred radius
        // Send notifications for new listings if preferences allow
        Timber.d("TODO: Check nearby accommodations")
    }

    private suspend fun checkCollegeProximity(location: Location) {
        // TODO: Implement college proximity check
        // Notify user when they're near their college or saved colleges
        // This can help with automatic check-ins or location-based features
        Timber.d("TODO: Check college proximity")
    }

    private fun hasLocationPermission(): Boolean {
        return ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun createNotification() = NotificationCompat.Builder(
        this,
        AalayApplication.NOTIFICATION_CHANNEL_DEFAULT
    ).apply {
        setContentTitle(getString(R.string.app_name))
        setContentText("Tracking location for better recommendations")
        setSmallIcon(R.drawable.ic_location_on)
        setColor(ContextCompat.getColor(this@LocationService, R.color.primary_color))
        setOngoing(true)
        setCategory(NotificationCompat.CATEGORY_SERVICE)
        setPriority(NotificationCompat.PRIORITY_LOW)
        setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
        
        // Add action to stop the service
        val stopIntent = Intent(this@LocationService, LocationService::class.java).apply {
            action = ACTION_STOP_LOCATION_SERVICE
        }
        val stopPendingIntent = PendingIntent.getService(
            this@LocationService,
            0,
            stopIntent,
            PendingIntent.FLAG_IMMUTABLE
        )
        addAction(
            R.drawable.ic_stop,
            "Stop",
            stopPendingIntent
        )
        
        // Add intent to open main activity
        val mainIntent = Intent(this@LocationService, MainActivity::class.java)
        val mainPendingIntent = PendingIntent.getActivity(
            this@LocationService,
            0,
            mainIntent,
            PendingIntent.FLAG_IMMUTABLE
        )
        setContentIntent(mainPendingIntent)
        
    }.build()
}
