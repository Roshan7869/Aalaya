package com.aalay.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.aalay.app.data.models.BookingRequest
import com.aalay.app.ui.viewmodels.BookingViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingScreen(
    accommodationId: String,
    roomType: String,
    monthlyRent: Double,
    viewModel: BookingViewModel,
    onNavigateBack: () -> Unit,
    onBookingSuccess: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    
    var checkInDate by remember { mutableStateOf(LocalDate.now().plusDays(1)) }
    var stayDuration by remember { mutableStateOf(12) } // months
    var specialRequests by remember { mutableStateOf("") }
    
    // Navigate on successful booking
    LaunchedEffect(uiState.isBookingConfirmed) {
        if (uiState.isBookingConfirmed) {
            onBookingSuccess()
        }
    }
    
    Column(
        modifier = modifier.fillMaxSize()
    ) {
        // Top App Bar
        TopAppBar(
            title = { Text("Book Accommodation") },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                }
            }
        )
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Accommodation Summary
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Booking Summary",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Room Type:")
                        Text(roomType, fontWeight = FontWeight.Medium)
                    }
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Monthly Rent:")
                        Text("₹${monthlyRent.toInt()}", fontWeight = FontWeight.Medium)
                    }
                }
            }
            
            // Check-in Date Selection
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Check-in Details",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    OutlinedTextField(
                        value = checkInDate.format(DateTimeFormatter.ofPattern("dd MMM yyyy")),
                        onValueChange = { },
                        label = { Text("Check-in Date") },
                        readOnly = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text("Stay Duration (months)")
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        OutlinedButton(
                            onClick = { if (stayDuration > 1) stayDuration-- }
                        ) {
                            Text("-")
                        }
                        
                        Text(
                            text = "$stayDuration months",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.weight(1f)
                        )
                        
                        OutlinedButton(
                            onClick = { if (stayDuration < 24) stayDuration++ }
                        ) {
                            Text("+")
                        }
                    }
                }
            }
            
            // Special Requests
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Special Requests",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    OutlinedTextField(
                        value = specialRequests,
                        onValueChange = { specialRequests = it },
                        label = { Text("Any special requirements or requests") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                        maxLines = 5
                    )
                }
            }
            
            // Payment Summary
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Payment Summary",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    val totalAmount = monthlyRent * stayDuration
                    val securityDeposit = monthlyRent * 2 // Assume 2 months security
                    
                    PaymentRow("Monthly Rent", "₹${monthlyRent.toInt()}")
                    PaymentRow("Duration", "$stayDuration months")
                    PaymentRow("Security Deposit", "₹${securityDeposit.toInt()}")
                    
                    Divider(modifier = Modifier.padding(vertical = 8.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Total Amount",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "₹${(totalAmount + securityDeposit).toInt()}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
            
            // Confirm Booking Button
            Button(
                onClick = {
                    val bookingRequest = BookingRequest(
                        accommodationId = accommodationId,
                        studentId = "current_student_id", // This should come from auth state
                        roomType = roomType,
                        checkInDate = checkInDate.toString(),
                        stayDuration = stayDuration,
                        totalAmount = monthlyRent * stayDuration + (monthlyRent * 2),
                        specialRequests = specialRequests.takeIf { it.isNotBlank() }
                    )
                    viewModel.submitBooking(bookingRequest)
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text("Confirm Booking")
            }
            
            // Error Message
            uiState.error?.let { error ->
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun PaymentRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label)
        Text(text = value, fontWeight = FontWeight.Medium)
    }
}