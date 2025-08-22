package com.aalay.app.ui.activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.core.net.toUri
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

@AndroidEntryPoint
class DeepLinkActivity : ComponentActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        handleDeepLink(intent)
    }
    
    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        intent?.let { handleDeepLink(it) }
    }
    
    private fun handleDeepLink(intent: Intent) {
        val data: Uri? = intent.data
        
        if (data != null) {
            Timber.d("DeepLink received: $data")
            
            when {
                data.path?.startsWith("/accommodation/") == true -> {
                    val accommodationId = data.lastPathSegment
                    navigateToAccommodation(accommodationId)
                }
                data.path?.startsWith("/search") == true -> {
                    val query = data.getQueryParameter("q")
                    val location = data.getQueryParameter("location")
                    navigateToSearch(query, location)
                }
                data.path?.startsWith("/profile") == true -> {
                    navigateToProfile()
                }
                else -> {
                    navigateToMain()
                }
            }
        } else {
            navigateToMain()
        }
    }
    
    private fun navigateToAccommodation(accommodationId: String?) {
        val intent = Intent(this, MainActivity::class.java).apply {
            putExtra("navigate_to", "accommodation_detail")
            putExtra("accommodation_id", accommodationId)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        finish()
    }
    
    private fun navigateToSearch(query: String?, location: String?) {
        val intent = Intent(this, MainActivity::class.java).apply {
            putExtra("navigate_to", "search")
            putExtra("search_query", query)
            putExtra("search_location", location)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        finish()
    }
    
    private fun navigateToProfile() {
        val intent = Intent(this, MainActivity::class.java).apply {
            putExtra("navigate_to", "profile")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        finish()
    }
    
    private fun navigateToMain() {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        finish()
    }
}