package com.aalay.app.utils

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.os.Looper
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.*
import com.google.android.gms.tasks.Task
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.IOException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Utility class for location-related operations
 * Handles device location via FusedLocationProviderClient and location services
 */
class LocationUtils(private val context: Context) {
    
    private val fusedLocationClient: FusedLocationProviderClient = 
        LocationServices.getFusedLocationProviderClient(context)
    
    private val geocoder: Geocoder by lazy { Geocoder(context) }
    
    companion object {
        const val LOCATION_PERMISSION_REQUEST_CODE = 1001
        const val DEFAULT_UPDATE_INTERVAL = 10000L // 10 seconds
        const val DEFAULT_FASTEST_INTERVAL = 5000L // 5 seconds
        const val HIGH_ACCURACY_PRIORITY = LocationRequest.PRIORITY_HIGH_ACCURACY
        const val BALANCED_POWER_PRIORITY = LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY
        
        // Default coordinates for major educational cities in Chhattisgarh
        val CG_EDUCATIONAL_LOCATIONS = mapOf(
            // Major Cities
            "Raipur" to Pair(21.2514, 81.6296),
            "Bilaspur" to Pair(22.0797, 82.1409),
            "Bhilai" to Pair(21.2167, 81.3833),
            "Durg" to Pair(21.1901, 81.2849),
            "Korba" to Pair(22.3595, 82.7501),
            
            // Educational Hubs
            "IIT Raipur" to Pair(21.1301, 81.1133),
            "NIT Raipur" to Pair(21.2514, 81.6296),
            "AIIMS Bilaspur" to Pair(22.1298, 82.1409),
            "CSVTU Bhilai" to Pair(21.2167, 81.3833),
            
            // Other Cities
            "Rajnandgaon" to Pair(21.0974, 81.0370),
            "Jagdalpur" to Pair(19.0835, 82.0355),
            "Ambikapur" to Pair(23.1186, 83.1994),
            "Dhamtari" to Pair(20.7072, 81.5485),
            "Mahasamund" to Pair(21.1038, 82.0998),
            "Kanker" to Pair(20.2719, 81.4929),
            "Bastar" to Pair(19.0835, 82.0355)
        )
    }
    
    /**
     * Check if location permissions are granted
     * @return True if both fine and coarse location permissions are granted
     */
    fun hasLocationPermissions(): Boolean {
        return ActivityCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED &&
        ActivityCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }
    
    /**
     * Check if location services are enabled
     * @return True if location services are enabled
     */
    fun isLocationEnabled(): Boolean {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
               locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }
    
    /**
     * Get current location using FusedLocationProviderClient
     * @param priority Location request priority (HIGH_ACCURACY or BALANCED_POWER)
     * @return Current location or null if unavailable
     */
    @SuppressLint("MissingPermission")
    suspend fun getCurrentLocation(
        priority: Int = HIGH_ACCURACY_PRIORITY
    ): Location? = suspendCancellableCoroutine { continuation ->
        
        if (!hasLocationPermissions()) {
            continuation.resumeWithException(SecurityException("Location permission not granted"))
            return@suspendCancellableCoroutine
        }
        
        if (!isLocationEnabled()) {
            continuation.resumeWithException(IllegalStateException("Location services disabled"))
            return@suspendCancellableCoroutine
        }
        
        val locationRequest = LocationRequest.create().apply {
            this.priority = priority
            interval = 0 // Get location immediately
            fastestInterval = 0
            numUpdates = 1
        }
        
        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                val location = locationResult.lastLocation
                if (continuation.isActive) {
                    continuation.resume(location)
                }
                fusedLocationClient.removeLocationUpdates(this)
            }
            
            override fun onLocationAvailability(locationAvailability: LocationAvailability) {
                if (!locationAvailability.isLocationAvailable && continuation.isActive) {
                    continuation.resume(null)
                    fusedLocationClient.removeLocationUpdates(this)
                }
            }
        }
        
        try {
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
            
            // Fallback to last known location if real-time fails
            fusedLocationClient.lastLocation.addOnSuccessListener { lastLocation ->
                if (lastLocation != null && continuation.isActive) {
                    continuation.resume(lastLocation)
                    fusedLocationClient.removeLocationUpdates(locationCallback)
                }
            }.addOnFailureListener { exception ->
                if (continuation.isActive) {
                    continuation.resumeWithException(exception)
                }
            }
            
        } catch (e: Exception) {
            if (continuation.isActive) {
                continuation.resumeWithException(e)
            }
        }
        
        continuation.invokeOnCancellation {
            fusedLocationClient.removeLocationUpdates(locationCallback)
        }
    }
    
    /**
     * Start continuous location updates
     * @param priority Location request priority
     * @param updateInterval Update interval in milliseconds
     * @param callback Callback for location updates
     * @return LocationCallback for removing updates later
     */
    @SuppressLint("MissingPermission")
    fun startLocationUpdates(
        priority: Int = BALANCED_POWER_PRIORITY,
        updateInterval: Long = DEFAULT_UPDATE_INTERVAL,
        fastestInterval: Long = DEFAULT_FASTEST_INTERVAL,
        callback: (Location?) -> Unit
    ): LocationCallback {
        
        val locationRequest = LocationRequest.create().apply {
            this.priority = priority
            interval = updateInterval
            this.fastestInterval = fastestInterval
        }
        
        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                callback(locationResult.lastLocation)
            }
            
            override fun onLocationAvailability(locationAvailability: LocationAvailability) {
                if (!locationAvailability.isLocationAvailable) {
                    callback(null)
                }
            }
        }
        
        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )
        
        return locationCallback
    }
    
    /**
     * Stop location updates
     * @param locationCallback Callback to remove
     */
    fun stopLocationUpdates(locationCallback: LocationCallback) {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }
    
    /**
     * Get address from coordinates using Geocoder
     * @param latitude Latitude
     * @param longitude Longitude
     * @param maxResults Maximum number of results
     * @return List of addresses
     */
    suspend fun getAddressFromLocation(
        latitude: Double,
        longitude: Double,
        maxResults: Int = 1
    ): List<Address> = suspendCancellableCoroutine { continuation ->
        try {
            if (Geocoder.isPresent()) {
                val addresses = geocoder.getFromLocation(latitude, longitude, maxResults)
                continuation.resume(addresses ?: emptyList())
            } else {
                continuation.resume(emptyList())
            }
        } catch (e: IOException) {
            continuation.resumeWithException(e)
        } catch (e: IllegalArgumentException) {
            continuation.resumeWithException(e)
        }
    }
    
    /**
     * Get coordinates from address string using Geocoder
     * @param address Address string
     * @param maxResults Maximum number of results
     * @return List of addresses with coordinates
     */
    suspend fun getLocationFromAddress(
        address: String,
        maxResults: Int = 1
    ): List<Address> = suspendCancellableCoroutine { continuation ->
        try {
            if (Geocoder.isPresent()) {
                val addresses = geocoder.getFromLocationName(address, maxResults)
                continuation.resume(addresses ?: emptyList())
            } else {
                continuation.resume(emptyList())
            }
        } catch (e: IOException) {
            continuation.resumeWithException(e)
        } catch (e: IllegalArgumentException) {
            continuation.resumeWithException(e)
        }
    }
    
    /**
     * Format address for display
     * @param address Address object
     * @return Formatted address string
     */
    fun formatAddress(address: Address): String {
        val addressParts = mutableListOf<String>()
        
        address.subThoroughfare?.let { addressParts.add(it) }
        address.thoroughfare?.let { addressParts.add(it) }
        address.subLocality?.let { addressParts.add(it) }
        address.locality?.let { addressParts.add(it) }
        address.subAdminArea?.let { if (it != address.locality) addressParts.add(it) }
        address.adminArea?.let { addressParts.add(it) }
        
        return addressParts.joinToString(", ")
    }
    
    /**
     * Get city name from coordinates
     * @param latitude Latitude
     * @param longitude Longitude
     * @return City name or null
     */
    suspend fun getCityFromLocation(latitude: Double, longitude: Double): String? {
        return try {
            val addresses = getAddressFromLocation(latitude, longitude)
            addresses.firstOrNull()?.locality ?: addresses.firstOrNull()?.subAdminArea
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * Open Google Maps with directions to a destination
     * @param destinationLat Destination latitude
     * @param destinationLng Destination longitude
     * @param destinationName Optional destination name
     * @param userLat Optional user latitude (if null, uses current location)
     * @param userLng Optional user longitude (if null, uses current location)
     */
    fun openGoogleMapsDirections(
        destinationLat: Double,
        destinationLng: Double,
        destinationName: String? = null,
        userLat: Double? = null,
        userLng: Double? = null
    ) {
        val uri = if (userLat != null && userLng != null) {
            // Specific origin and destination
            Uri.parse("https://www.google.com/maps/dir/$userLat,$userLng/$destinationLat,$destinationLng")
        } else {
            // Use current location as origin
            Uri.parse("google.navigation:q=$destinationLat,$destinationLng")
        }
        
        val intent = Intent(Intent.ACTION_VIEW, uri).apply {
            setPackage("com.google.android.apps.maps")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        
        // Fallback to web if Google Maps app is not installed
        if (intent.resolveActivity(context.packageManager) != null) {
            context.startActivity(intent)
        } else {
            val webUri = Uri.parse("https://www.google.com/maps/dir/?api=1&destination=$destinationLat,$destinationLng")
            val webIntent = Intent(Intent.ACTION_VIEW, webUri).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(webIntent)
        }
    }
    
    /**
     * Create location settings request for high accuracy
     * @return LocationSettingsRequest for checking settings
     */
    fun createLocationSettingsRequest(priority: Int = HIGH_ACCURACY_PRIORITY): LocationSettingsRequest {
        val locationRequest = LocationRequest.create().apply {
            this.priority = priority
        }
        
        return LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)
            .setAlwaysShow(true)
            .build()
    }
    
    /**
     * Check location settings and prompt user to enable if needed
     * @param request LocationSettingsRequest
     * @return Task for location settings response
     */
    fun checkLocationSettings(request: LocationSettingsRequest): Task<LocationSettingsResponse> {
        val client = LocationServices.getSettingsClient(context)
        return client.checkLocationSettings(request)
    }
    
    /**
     * Get default location for a Chhattisgarh city or institution
     * @param cityName City or institution name
     * @return Pair of latitude and longitude, or null if not found
     */
    fun getDefaultLocationForCity(cityName: String): Pair<Double, Double>? {
        return CG_EDUCATIONAL_LOCATIONS[cityName.trim()] 
            ?: CG_EDUCATIONAL_LOCATIONS.entries.find { 
                it.key.contains(cityName.trim(), ignoreCase = true) 
            }?.value
    }
    
    /**
     * Get nearest major Chhattisgarh city from coordinates
     * @param latitude User's latitude
     * @param longitude User's longitude
     * @return Nearest major CG city name
     */
    fun getNearestCGCity(latitude: Double, longitude: Double): String {
        val majorCities = listOf("Raipur", "Bilaspur", "Bhilai", "Durg", "Korba")
        
        return majorCities.minByOrNull { cityName ->
            val cityCoords = CG_EDUCATIONAL_LOCATIONS[cityName]!!
            DistanceCalculator.calculateDistance(
                latitude, longitude,
                cityCoords.first, cityCoords.second
            )
        } ?: "Raipur" // Default to capital
    }
    
    /**
     * Check if coordinates are within Chhattisgarh boundaries
     * @param latitude Latitude to check
     * @param longitude Longitude to check
     * @return True if within CG boundaries
     */
    fun isWithinChhattisgarh(latitude: Double, longitude: Double): Boolean {
        // Approximate boundaries of Chhattisgarh
        val minLat = 17.46 // Southern boundary
        val maxLat = 24.08 // Northern boundary
        val minLng = 80.15 // Western boundary
        val maxLng = 84.20 // Eastern boundary
        
        return latitude in minLat..maxLat && longitude in minLng..maxLng
    }
    
    /**
     * Get popular educational areas in a CG city
     * @param cityName City name
     * @return List of popular educational areas/localities
     */
    fun getEducationalAreasInCity(cityName: String): List<String> {
        return when (cityName.lowercase()) {
            "raipur" -> listOf(
                "Shankar Nagar", "New Rajendra Nagar", "Pandri", "Tatibandh",
                "Sarona", "Devendra Nagar", "Mowa", "G.E. Road"
            )
            "bilaspur" -> listOf(
                "Torwa", "Ratanpur Road", "Link Road", "Sakri",
                "Sarkanda", "Sarkoni", "Tifra"
            )
            "bhilai" -> listOf(
                "Sector 1", "Sector 2", "Sector 6", "Sector 9",
                "Supela", "Khursipar", "Nehru Nagar"
            )
            "durg" -> listOf(
                "Power House", "Shanti Nagar", "Mohan Nagar",
                "Pragati Nagar", "Steel City"
            )
            "korba" -> listOf(
                "Transport Nagar", "Power Colony", "Darri",
                "Banki Mongra", "Area Colony"
            )
            else -> listOf("City Center", "College Area", "Main Road")
        }
    }
    
    /**
     * Calculate bearing between two points
     * @param lat1 Start latitude
     * @param lng1 Start longitude
     * @param lat2 End latitude
     * @param lng2 End longitude
     * @return Bearing in degrees
     */
    fun calculateBearing(lat1: Double, lng1: Double, lat2: Double, lng2: Double): Double {
        val lat1Rad = Math.toRadians(lat1)
        val lat2Rad = Math.toRadians(lat2)
        val deltaLng = Math.toRadians(lng2 - lng1)
        
        val y = kotlin.math.sin(deltaLng) * kotlin.math.cos(lat2Rad)
        val x = kotlin.math.cos(lat1Rad) * kotlin.math.sin(lat2Rad) - 
                kotlin.math.sin(lat1Rad) * kotlin.math.cos(lat2Rad) * kotlin.math.cos(deltaLng)
        
        val bearing = Math.toDegrees(kotlin.math.atan2(y, x))
        return (bearing + 360) % 360 // Normalize to 0-360 degrees
    }
    
    /**
     * Get compass direction from bearing
     * @param bearing Bearing in degrees
     * @return Compass direction (N, NE, E, SE, S, SW, W, NW)
     */
    fun getCompassDirection(bearing: Double): String {
        val directions = arrayOf("N", "NE", "E", "SE", "S", "SW", "W", "NW")
        val index = ((bearing + 22.5) / 45.0).toInt() % 8
        return directions[index]
    }
}

/**
 * Data class for location-related results
 */
data class LocationResult(
    val latitude: Double,
    val longitude: Double,
    val accuracy: Float? = null,
    val address: String? = null,
    val city: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * Sealed class for location operation results
 */
sealed class LocationOperationResult {
    data class Success(val location: LocationResult) : LocationOperationResult()
    data class Error(val exception: Exception, val message: String) : LocationOperationResult()
    object PermissionDenied : LocationOperationResult()
    object LocationDisabled : LocationOperationResult()
    object Timeout : LocationOperationResult()
}