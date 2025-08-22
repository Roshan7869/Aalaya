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
import com.aalay.app.ui.screens.BookingScreen
import com.aalay.app.ui.viewmodels.BookingViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class BookingActivity : ComponentActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        val accommodationId = intent.getStringExtra("accommodation_id") ?: ""
        val roomType = intent.getStringExtra("room_type") ?: ""
        val monthlyRent = intent.getDoubleExtra("monthly_rent", 0.0)
        
        setContent {
            AalayTheme {
                val viewModel: BookingViewModel = hiltViewModel()
                
                BookingScreen(
                    accommodationId = accommodationId,
                    roomType = roomType,
                    monthlyRent = monthlyRent,
                    viewModel = viewModel,
                    onNavigateBack = {
                        finish()
                    },
                    onBookingSuccess = {
                        // Handle successful booking
                        setResult(RESULT_OK)
                        finish()
                    }
                )
            }
        }
    }
}