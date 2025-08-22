package com.aalay.app.ui.fragments

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.aalay.app.R
import com.aalay.app.data.models.Accommodation
import com.aalay.app.data.models.AccommodationType
import com.aalay.app.data.models.RoomType
import com.aalay.app.data.models.GenderPreference
import com.aalay.app.data.models.LatLong

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToSearch: () -> Unit,
    onNavigateToListing: (String) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Welcome Section
        WelcomeSection()
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Search Bar
        SearchBar(
            query = searchQuery,
            onQueryChange = { searchQuery = it },
            onSearchClick = onNavigateToSearch
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Main Content
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            item {
                QuickFiltersSection()
            }
            
            item {
                FeaturedListingsSection(onNavigateToListing)
            }
            
            item {
                NearbyAccommodationsSection(onNavigateToListing)
            }
            
            item {
                PopularAreasSection()
            }
        }
    }
}

@Composable
private fun WelcomeSection() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = stringResource(R.string.home_welcome),
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.app_tagline),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearchClick: () -> Unit
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = Modifier.fillMaxWidth(),
        placeholder = { 
            Text(stringResource(R.string.home_search_hint)) 
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = stringResource(R.string.nav_search)
            )
        },
        trailingIcon = {
            IconButton(onClick = onSearchClick) {
                Icon(
                    imageVector = Icons.Default.Tune,
                    contentDescription = stringResource(R.string.action_filter)
                )
            }
        },
        singleLine = true,
        shape = RoundedCornerShape(12.dp)
    )
}

@Composable
private fun QuickFiltersSection() {
    Column {
        Text(
            text = stringResource(R.string.home_quick_filters),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(getQuickFilters()) { filter ->
                FilterChip(
                    onClick = { /* TODO: Handle filter click */ },
                    label = { Text(filter.name) },
                    selected = false,
                    leadingIcon = {
                        Icon(
                            imageVector = filter.icon,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                )
            }
        }
    }
}

@Composable
private fun FeaturedListingsSection(onNavigateToListing: (String) -> Unit) {
    val featuredListings = remember { getSampleAccommodations() }
    
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.home_featured_listings),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            TextButton(onClick = { /* TODO: View all featured */ }) {
                Text("View All")
            }
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(horizontal = 4.dp)
        ) {
            items(featuredListings.take(5)) { accommodation ->
                AccommodationCard(
                    accommodation = accommodation,
                    onClick = { onNavigateToListing(accommodation.id) }
                )
            }
        }
    }
}

@Composable
private fun NearbyAccommodationsSection(onNavigateToListing: (String) -> Unit) {
    val nearbyListings = remember { getSampleAccommodations() }
    
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.home_nearby_accommodations),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            TextButton(onClick = { /* TODO: View all nearby */ }) {
                Text("View All")
            }
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(horizontal = 4.dp)
        ) {
            items(nearbyListings.take(5)) { accommodation ->
                AccommodationCard(
                    accommodation = accommodation,
                    onClick = { onNavigateToListing(accommodation.id) }
                )
            }
        }
    }
}

@Composable
private fun PopularAreasSection() {
    val popularAreas = remember { getPopularAreas() }
    
    Column {
        Text(
            text = stringResource(R.string.home_popular_areas),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(horizontal = 4.dp)
        ) {
            items(popularAreas) { area ->
                PopularAreaCard(area = area)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AccommodationCard(
    accommodation: Accommodation,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .width(280.dp)
            .height(200.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column {
            // Image placeholder
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Home,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.outline
                )
            }
            
            Column(
                modifier = Modifier.padding(12.dp)
            ) {
                Text(
                    text = accommodation.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.outline
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = accommodation.city,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "₹${accommodation.monthlyRent.toInt()}/month",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PopularAreaCard(area: PopularArea) {
    Card(
        onClick = { /* TODO: Handle area click */ },
        modifier = Modifier
            .width(160.dp)
            .height(100.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.LocationCity,
                    contentDescription = null,
                    modifier = Modifier.size(32.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = area.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${area.listingCount} listings",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// Data classes and sample data
data class QuickFilter(
    val name: String,
    val icon: ImageVector
)

data class PopularArea(
    val name: String,
    val listingCount: Int
)

private fun getQuickFilters(): List<QuickFilter> {
    return listOf(
        QuickFilter("PG", Icons.Default.Home),
        QuickFilter("Hostel", Icons.Default.Business),
        QuickFilter("Apartment", Icons.Default.Apartment),
        QuickFilter("Near College", Icons.Default.School),
        QuickFilter("Budget Friendly", Icons.Default.AttachMoney),
        QuickFilter("Girls Only", Icons.Default.Female),
        QuickFilter("Boys Only", Icons.Default.Male)
    )
}

private fun getPopularAreas(): List<PopularArea> {
    return listOf(
        PopularArea("Supela", 45),
        PopularArea("Bhilai", 32),
        PopularArea("Power House", 28),
        PopularArea("Smriti Nagar", 22),
        PopularArea("Station Road", 18)
    )
}

private fun getSampleAccommodations(): List<Accommodation> {
    return listOf(
        Accommodation(
            id = "1",
            title = "Modern PG near BIT Durg",
            description = "Comfortable accommodation with all amenities",
            type = AccommodationType.PG,
            roomType = RoomType.SINGLE,
            genderPreference = GenderPreference.MALE,
            location = LatLong(21.1938, 81.2858),
            address = "Near BIT Durg, Supela",
            city = "Durg",
            state = "Chhattisgarh",
            postalCode = "491001",
            monthlyRent = 8000.0,
            securityDeposit = 16000.0,
            totalRooms = 20,
            availableRooms = 3,
            bathrooms = 10,
            amenities = listOf("WiFi", "AC", "Mess", "Laundry"),
            images = emptyList(),
            ownerId = "owner1",
            ownerName = "Ram Kumar",
            ownerPhone = "+91-9876543210",
            ownerEmail = "ram@example.com",
            rating = 4.2f,
            reviewCount = 15,
            hasWifi = true,
            hasAc = true,
            hasMess = true
        ),
        Accommodation(
            id = "2", 
            title = "Girls Hostel near CSVTU",
            description = "Safe and secure accommodation for female students",
            type = AccommodationType.HOSTEL,
            roomType = RoomType.DOUBLE,
            genderPreference = GenderPreference.FEMALE,
            location = LatLong(21.2089, 81.3792),
            address = "CSVTU Campus Road, Bhilai",
            city = "Bhilai",
            state = "Chhattisgarh", 
            postalCode = "490009",
            monthlyRent = 6500.0,
            securityDeposit = 13000.0,
            totalRooms = 50,
            availableRooms = 8,
            bathrooms = 25,
            amenities = listOf("WiFi", "Security", "Mess", "Study Room"),
            images = emptyList(),
            ownerId = "owner2",
            ownerName = "Sita Devi",
            ownerPhone = "+91-9876543211", 
            ownerEmail = "sita@example.com",
            rating = 4.5f,
            reviewCount = 28,
            hasWifi = true,
            hasStudyRoom = true,
            hasMess = true
        ),
        Accommodation(
            id = "3",
            title = "Shared Apartment near NIT",
            description = "Fully furnished shared apartment with modern amenities",
            type = AccommodationType.APARTMENT,
            roomType = RoomType.SHARED,
            genderPreference = GenderPreference.NO_PREFERENCE,
            location = LatLong(21.2514, 81.6296),
            address = "Sector 9, Near NIT Raipur",
            city = "Raipur",
            state = "Chhattisgarh",
            postalCode = "492010",
            monthlyRent = 12000.0,
            securityDeposit = 24000.0,
            totalRooms = 4,
            availableRooms = 1,
            bathrooms = 2,
            amenities = listOf("WiFi", "AC", "Parking", "Kitchen"),
            images = emptyList(),
            ownerId = "owner3",
            ownerName = "Mohan Singh",
            ownerPhone = "+91-9876543212",
            ownerEmail = "mohan@example.com", 
            rating = 4.0f,
            reviewCount = 12,
            hasWifi = true,
            hasAc = true,
            hasParking = true
        )
    )
}
