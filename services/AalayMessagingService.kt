package com.aalay.app.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.aalay.app.AalayApplication
import com.aalay.app.R
import com.aalay.app.ui.activities.MainActivity
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import javax.inject.Inject

/**
 * Firebase Cloud Messaging service for handling push notifications
 * Handles booking confirmations, roommate matches, price alerts, and promotional messages
 */
@AndroidEntryPoint
class AalayMessagingService : FirebaseMessagingService() {

    companion object {
        private const val NOTIFICATION_ID_BOOKING = 1001
        private const val NOTIFICATION_ID_ROOMMATE = 1002
        private const val NOTIFICATION_ID_DEALS = 1003
        private const val NOTIFICATION_ID_DEFAULT = 1000
    }

    @Inject
    lateinit var notificationManager: NotificationManager

    override fun onCreate() {
        super.onCreate()
        // NotificationManager will be injected by Hilt
        if (!::notificationManager.isInitialized) {
            notificationManager = ContextCompat.getSystemService(
                this,
                NotificationManager::class.java
            ) as NotificationManager
        }
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        
        Timber.d("FCM message received from: ${remoteMessage.from}")
        
        // Handle data payload
        remoteMessage.data.isNotEmpty().let { hasData ->
            if (hasData) {
                Timber.d("Message data payload: ${remoteMessage.data}")
                handleDataMessage(remoteMessage.data)
            }
        }

        // Handle notification payload
        remoteMessage.notification?.let { notification ->
            Timber.d("Message Notification Body: ${notification.body}")
            val title = notification.title ?: getString(R.string.app_name)
            val body = notification.body ?: ""
            val type = remoteMessage.data["type"] ?: "default"
            
            showNotification(title, body, type, remoteMessage.data)
        }
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Timber.d("FCM Registration Token: $token")
        
        // Send token to your server for user targeting
        sendRegistrationTokenToServer(token)
    }

    private fun handleDataMessage(data: Map<String, String>) {
        val type = data["type"] ?: "default"
        val title = data["title"] ?: getString(R.string.app_name)
        val body = data["body"] ?: ""
        
        when (type) {
            "booking_confirmation" -> {
                handleBookingNotification(title, body, data)
            }
            "booking_update" -> {
                handleBookingNotification(title, body, data)
            }
            "roommate_match" -> {
                handleRoommateNotification(title, body, data)
            }
            "roommate_request" -> {
                handleRoommateNotification(title, body, data)
            }
            "price_alert" -> {
                handleDealsNotification(title, body, data)
            }
            "new_listing" -> {
                handleDealsNotification(title, body, data)
            }
            "promotional_offer" -> {
                handleDealsNotification(title, body, data)
            }
            else -> {
                showNotification(title, body, type, data)
            }
        }
    }

    private fun handleBookingNotification(title: String, body: String, data: Map<String, String>) {
        val bookingId = data["booking_id"]
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("navigate_to", "booking_details")
            putExtra("booking_id", bookingId)
        }
        
        showNotification(
            title = title,
            body = body,
            type = "booking",
            data = data,
            intent = intent,
            notificationId = NOTIFICATION_ID_BOOKING
        )
    }

    private fun handleRoommateNotification(title: String, body: String, data: Map<String, String>) {
        val userId = data["user_id"]
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("navigate_to", "roommate")
            putExtra("user_id", userId)
        }
        
        showNotification(
            title = title,
            body = body,
            type = "roommate",
            data = data,
            intent = intent,
            notificationId = NOTIFICATION_ID_ROOMMATE
        )
    }

    private fun handleDealsNotification(title: String, body: String, data: Map<String, String>) {
        val listingId = data["listing_id"]
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            if (!listingId.isNullOrEmpty()) {
                putExtra("navigate_to", "listing_detail")
                putExtra("listing_id", listingId)
            } else {
                putExtra("navigate_to", "home")
            }
        }
        
        showNotification(
            title = title,
            body = body,
            type = "deals",
            data = data,
            intent = intent,
            notificationId = NOTIFICATION_ID_DEALS
        )
    }

    private fun showNotification(
        title: String,
        body: String,
        type: String,
        data: Map<String, String>,
        intent: Intent? = null,
        notificationId: Int = NOTIFICATION_ID_DEFAULT
    ) {
        val finalIntent = intent ?: Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        
        val pendingIntent = PendingIntent.getActivity(
            this,
            notificationId,
            finalIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val channelId = getChannelIdForType(type)
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setColor(ContextCompat.getColor(this, R.color.primary_color))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        // Add action buttons based on notification type
        when (type) {
            "booking" -> {
                val viewBookingIntent = PendingIntent.getActivity(
                    this,
                    notificationId + 1,
                    finalIntent,
                    PendingIntent.FLAG_IMMUTABLE
                )
                notificationBuilder.addAction(
                    R.drawable.ic_visibility,
                    getString(R.string.action_view_details),
                    viewBookingIntent
                )
            }
            "roommate" -> {
                val viewProfileIntent = PendingIntent.getActivity(
                    this,
                    notificationId + 1,
                    finalIntent,
                    PendingIntent.FLAG_IMMUTABLE
                )
                notificationBuilder.addAction(
                    R.drawable.ic_person,
                    "View Profile",
                    viewProfileIntent
                )
            }
            "deals" -> {
                val viewListingIntent = PendingIntent.getActivity(
                    this,
                    notificationId + 1,
                    finalIntent,
                    PendingIntent.FLAG_IMMUTABLE
                )
                notificationBuilder.addAction(
                    R.drawable.ic_visibility,
                    getString(R.string.action_view_details),
                    viewListingIntent
                )
            }
        }

        // Add big text style for longer messages
        if (body.length > 40) {
            notificationBuilder.setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(body)
                    .setBigContentTitle(title)
            )
        }

        // Add image if provided
        data["image_url"]?.let { imageUrl ->
            // In a real implementation, you would load the image using Glide or similar
            // and set it using .setLargeIcon() or .setStyle(NotificationCompat.BigPictureStyle())
        }

        notificationManager.notify(notificationId, notificationBuilder.build())
        
        Timber.d("Notification shown: $title - $body")
    }

    private fun getChannelIdForType(type: String): String {
        return when (type) {
            "booking" -> AalayApplication.NOTIFICATION_CHANNEL_BOOKINGS
            "roommate" -> AalayApplication.NOTIFICATION_CHANNEL_ROOMMATE
            "deals", "price_alert", "new_listing", "promotional_offer" -> AalayApplication.NOTIFICATION_CHANNEL_DEALS
            else -> AalayApplication.NOTIFICATION_CHANNEL_DEFAULT
        }
    }

    private fun sendRegistrationTokenToServer(token: String) {
        // TODO: Implement API call to send FCM token to your server
        // This should be done through your repository/API service
        Timber.d("TODO: Send FCM token to server: $token")
        
        // Example implementation:
        // try {
        //     aalayApiService.updateFcmToken(token)
        // } catch (e: Exception) {
        //     Timber.e(e, "Failed to send FCM token to server")
        // }
    }
}
