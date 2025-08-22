package com.aalay.app.ui.activities

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.hilt.navigation.compose.hiltViewModel
import com.aalay.app.ui.theme.AalayTheme
import com.aalay.app.ui.screens.RoomDetailScreen
import com.aalay.app.ui.viewmodels.ListingDetailViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RoomDetailActivity : ComponentActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        val accommodationId = intent.getStringExtra("accommodation_id") ?: ""
        
        setContent {
            AalayTheme {
                val viewModel: ListingDetailViewModel = hiltViewModel()
                
                RoomDetailScreen(
                    accommodationId = accommodationId,
                    viewModel = viewModel,
                    onNavigateBack = {
                        finish()
                    },
                    onNavigateToBooking = { bookingData ->
                        // Handle booking navigation
                        finish()
                    }
                )
            }
        }
    }
}