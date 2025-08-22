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
import com.aalay.app.ui.screens.ProfileScreen
import com.aalay.app.ui.viewmodels.ProfileViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ProfileActivity : ComponentActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        setContent {
            AalayTheme {
                val viewModel: ProfileViewModel = hiltViewModel()
                
                ProfileScreen(
                    viewModel = viewModel,
                    onNavigateBack = {
                        finish()
                    },
                    onNavigateToSettings = {
                        // Handle navigation to settings
                    },
                    onNavigateToBookingHistory = {
                        // Handle navigation to booking history
                    }
                )
            }
        }
    }
}