package com.aalay.app.ui.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aalay.app.R
import com.aalay.app.ui.theme.AalayTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay

@SuppressLint("CustomSplashScreen")
@AndroidEntryPoint
class SplashActivity : ComponentActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        setContent {
            AalayTheme {
                SplashScreen(
                    onNavigateToMain = {
                        startActivity(Intent(this, MainActivity::class.java))
                        finish()
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SplashScreen(
    onNavigateToMain: () -> Unit
) {
    var currentStep by remember { mutableIntStateOf(0) }
    var selectedCollege by remember { mutableStateOf<College?>(null) }
    var selectedCity by remember { mutableStateOf("Durg") }
    
    // Auto-advance splash after 2 seconds
    LaunchedEffect(Unit) {
        if (currentStep == 0) {
            delay(2000)
            currentStep = 1
        }
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.primary)
    ) {
        AnimatedContent(
            targetState = currentStep,
            transitionSpec = {
                slideInHorizontally(initialOffsetX = { it }) with 
                slideOutHorizontally(targetOffsetX = { -it })
            }
        ) { step ->
            when (step) {
                0 -> SplashLogo()
                1 -> CollegeSelectionStep(
                    selectedCity = selectedCity,
                    onCitySelected = { selectedCity = it },
                    selectedCollege = selectedCollege,
                    onCollegeSelected = { selectedCollege = it },
                    onContinue = { currentStep = 2 }
                )
                2 -> OnboardingStep(
                    selectedCollege = selectedCollege,
                    onGetStarted = onNavigateToMain
                )
            }
        }
    }
}

@Composable
fun SplashLogo() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // App Logo Placeholder
        Card(
            modifier = Modifier.size(120.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.onPrimary
            )
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Home,
                    contentDescription = "Aalay Logo",
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = "Aalay",
            style = MaterialTheme.typography.headlineLarge.copy(
                fontSize = 42.sp,
                fontWeight = FontWeight.Bold
            ),
            color = MaterialTheme.colorScheme.onPrimary
        )
        
        Text(
            text = "Student Housing Made Easy",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
        )
        
        Spacer(modifier = Modifier.height(48.dp))
        
        CircularProgressIndicator(
            color = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier.size(24.dp)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CollegeSelectionStep(
    selectedCity: String,
    onCitySelected: (String) -> Unit,
    selectedCollege: College?,
    onCollegeSelected: (College) -> Unit,
    onContinue: () -> Unit
) {
    val chhattisgarh_cities = listOf("Durg", "Raipur", "Bhilai", "Korba", "Bilaspur", "Jagdalpur")
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(80.dp))
        
        Text(
            text = "Welcome to Aalay! 🏠",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold
            ),
            color = MaterialTheme.colorScheme.onPrimary,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Let's find you the perfect accommodation near your college",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f),
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(48.dp))
        
        // City Selection
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Select Your City",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    )
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(chhattisgarh_cities.size) { index ->
                        val city = chhattisgarh_cities[index]
                        FilterChip(
                            onClick = { onCitySelected(city) },
                            label = { Text(city) },
                            selected = selectedCity == city,
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        )
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // College Selection
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Select Your College/University",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    )
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                val colleges = getCollegesForCity(selectedCity)
                LazyColumn(
                    modifier = Modifier.heightIn(max = 200.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(colleges.size) { index ->
                        val college = colleges[index]
                        Card(
                            onClick = { onCollegeSelected(college) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = if (selectedCollege == college) 
                                    MaterialTheme.colorScheme.primaryContainer
                                else 
                                    MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.School,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = college.name,
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = FontWeight.Medium
                                        )
                                    )
                                    Text(
                                        text = college.type,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                if (selectedCollege == college) {
                                    Icon(
                                        imageVector = Icons.Filled.CheckCircle,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.weight(1f))
        
        Button(
            onClick = onContinue,
            enabled = selectedCollege != null,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.tertiary
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = "Continue",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold
                )
            )
        }
    }
}

@Composable
fun OnboardingStep(
    selectedCollege: College?,
    onGetStarted: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(60.dp))
        
        // Feature highlights
        val features = listOf(
            OnboardingFeature(
                icon = Icons.Filled.LocationOn,
                title = "Smart Location Search",
                description = "Find accommodations with real-time traffic data to ${selectedCollege?.name ?: "your college"}"
            ),
            OnboardingFeature(
                icon = Icons.Filled.People,
                title = "Roommate Matching",
                description = "Connect with compatible roommates and split costs easily"
            ),
            OnboardingFeature(
                icon = Icons.Filled.Star,
                title = "Student-Verified Reviews",
                description = "Real reviews from students just like you"
            ),
            OnboardingFeature(
                icon = Icons.Filled.Schedule,
                title = "Instant Booking",
                description = "Book your perfect room in seconds with our streamlined process"
            )
        )
        
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            item {
                Text(
                    text = "Everything you need for student housing! 🎓",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.onPrimary,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(32.dp))
            }
            
            items(features.size) { index ->
                FeatureCard(feature = features[index])
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Button(
            onClick = onGetStarted,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.tertiary
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Get Started",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    )
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.Filled.ArrowForward,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun FeatureCard(feature: OnboardingFeature) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Card(
                modifier = Modifier.size(48.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = feature.icon,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = feature.title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    )
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = feature.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// Data Classes
data class College(
    val id: String,
    val name: String,
    val type: String,
    val city: String,
    val latitude: Double,
    val longitude: Double
)

data class OnboardingFeature(
    val icon: ImageVector,
    val title: String,
    val description: String
)

// Sample data for Chhattisgarh colleges
fun getCollegesForCity(city: String): List<College> {
    return when (city) {
        "Durg" -> listOf(
            College("1", "BIT Durg", "Engineering College", "Durg", 21.1938, 81.2858),
            College("2", "Govt. Engineering College Durg", "Engineering College", "Durg", 21.1900, 81.2900),
            College("3", "CSVTU Bhilai", "University", "Durg", 21.2089, 81.3792)
        )
        "Raipur" -> listOf(
            College("4", "NIT Raipur", "Engineering College", "Raipur", 21.2514, 81.6296),
            College("5", "AIIMS Raipur", "Medical College", "Raipur", 21.2426, 81.6277),
            College("6", "IIT Bhilai", "Engineering College", "Raipur", 21.2500, 81.6000)
        )
        "Bhilai" -> listOf(
            College("7", "SSGI Bhilai", "Engineering College", "Bhilai", 21.2089, 81.3792),
            College("8", "Rungta College", "Engineering College", "Bhilai", 21.2100, 81.3800)
        )
        "Bilaspur" -> listOf(
            College("9", "Guru Ghasidas University", "Central University", "Bilaspur", 22.0797, 82.1391),
            College("10", "AIIMS Bilaspur", "Medical College", "Bilaspur", 22.0900, 82.1500)
        )
        "Korba" -> listOf(
            College("11", "Govt. Engineering College Korba", "Engineering College", "Korba", 22.3595, 82.7501)
        )
        "Jagdalpur" -> listOf(
            College("12", "Bastar University", "State University", "Jagdalpur", 19.0822, 82.0347)
        )
        else -> emptyList()
    }
}