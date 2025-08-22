package com.aalay.app.ui.activities

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.aalay.app.ui.theme.AalayTheme
import com.aalay.app.ui.viewmodels.AuthViewModel
import com.aalay.app.ui.screens.AuthScreen
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AuthActivity : ComponentActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        setContent {
            AalayTheme {
                val authViewModel: AuthViewModel = viewModel()
                val context = LocalContext.current
                
                AuthScreen(
                    viewModel = authViewModel,
                    onNavigateToMain = {
                        startActivity(Intent(this, MainActivity::class.java))
                        finish()
                    },
                    onNavigateBack = {
                        finish()
                    }
                )
            }
        }
    }
}