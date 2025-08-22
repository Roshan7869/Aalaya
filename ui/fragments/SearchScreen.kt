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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.aalay.app.R
import com.aalay.app.data.models.*
import com.aalay.app.ui.viewmodels.SearchViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    viewModel: SearchViewModel,
    onNavigateToListing: (String) -> Unit,
    onNavigateBack: () -> Unit
) {
    var showFilters by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    
    // Mock data for demonstration
    val searchResults = remember { getSampleSearchResults() }
    
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Top App Bar
        TopAppBar(
            title = { Text(stringResource(R.string.search_title)) },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = stringResource(R.string.action_back)
                    )
                }
            },
            actions = {
                IconButton(onClick = { showFilters = !showFilters }) {
                    Icon(
                        imageVector = Icons.Default.Tune,
                        contentDescription = stringResource(R.string.action_filter)
                    )
                }
            }
        )
        
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Search Bar
            SearchTextField(
                query = searchQuery,
                onQueryChange = { searchQuery = it },
                onSearch = { /* TODO: Implement search */ }
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Filters Section
            if (showFilters) {
                FiltersSection()
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            // Quick Filter Chips
            QuickFilterChips()
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Results Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${searchResults.size} accommodations found",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Sort,
                        contentDescription = stringResource(R.string.action_sort),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Sort",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Search Results
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(searchResults) { accommodation ->
                    SearchResultCard(
                        accommodation = accommodation,
                        onClick = { onNavigateToListing(accommodation.id) }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchTextField(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = Modifier.fillMaxWidth(),
        placeholder = { 
            Text(stringResource(R.string.search_location_hint)) 
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null
            )
        },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = "Clear"
                    )
                }
            }
        },
        singleLine = true,
        shape = RoundedCornerShape(12.dp)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FiltersSection() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Filters",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Budget Range
            Text(
                text = "Budget Range",
                style = MaterialTheme.typography.titleSmall
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = "",
                    onValueChange = { },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Min") },
                    leadingIcon = { Text("₹") }
                )
                OutlinedTextField(
                    value = "",
                    onValueChange = { },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Max") },
                    leadingIcon = { Text("₹") }
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Room Type
            Text(
                text = stringResource(R.string.search_room_type),
                style = MaterialTheme.typography.titleSmall
            )
            Spacer(modifier = Modifier.height(8.dp))
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(RoomType.values()) { roomType ->
                    FilterChip(
                        onClick = { /* TODO: Handle room type selection */ },
                        label = { Text(roomType.name) },
                        selected = false
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Accommodation Type
            Text(
                text = stringResource(R.string.search_accommodation_type),
                style = MaterialTheme.typography.titleSmall
            )
            Spacer(modifier = Modifier.height(8.dp))
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(AccommodationType.values()) { type ->
                    FilterChip(
                        onClick = { /* TODO: Handle accommodation type selection */ },
                        label = { Text(type.name) },
                        selected = false
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Amenities
            Text(
                text = stringResource(R.string.search_amenities),
                style = MaterialTheme.typography.titleSmall
            )
            Spacer(modifier = Modifier.height(8.dp))
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(getAmenityOptions()) { amenity ->
                    FilterChip(
                        onClick = { /* TODO: Handle amenity selection */ },
                        label = { Text(amenity) },
                        selected = false
                    )
                }
            }
        }
    }
}

@Composable
private fun QuickFilterChips() {
    val quickFilters = remember { getQuickSearchFilters() }
    
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(quickFilters) { filter ->
            FilterChip(
                onClick = { /* TODO: Handle quick filter */ },
                label = { Text(filter) },
                selected = false,
                colors = FilterChipDefaults.filterChipColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    labelColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchResultCard(
    accommodation: Accommodation,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize()
        ) {
            // Image placeholder
            Box(
                modifier = Modifier
                    .width(120.dp)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(topStart = 12.dp, bottomStart = 12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Home,
                    contentDescription = null,
                    modifier = Modifier.size(40.dp),
                    tint = MaterialTheme.colorScheme.outline
                )
            }
            
            // Content
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(12.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
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
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.outline
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${accommodation.address}, ${accommodation.city}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    // Rating
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${accommodation.rating} (${accommodation.reviewCount} reviews)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                // Price and availability
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "₹${accommodation.monthlyRent.toInt()}/month",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Text(
                        text = "${accommodation.availableRooms} rooms available",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                }
            }
        }
    }
}

private fun getQuickSearchFilters(): List<String> {
    return listOf(
        "Near Me",
        "Under ₹10k",
        "Girls Only", 
        "Boys Only",
        "AC Available",
        "WiFi",
        "Mess Included",
        "Parking"
    )
}

private fun getAmenityOptions(): List<String> {
    return listOf(
        "WiFi",
        "AC",
        "Parking",
        "Laundry",
        "Mess",
        "Gym",
        "Study Room",
        "Power Backup",
        "Security",
        "Water Supply"
    )
}

private fun getSampleSearchResults(): List<Accommodation> {
    return listOf(
        Accommodation(
            id = "search_1",
            title = "Comfortable PG near Engineering College",
            description = "Well-furnished PG with all modern amenities",
            type = AccommodationType.PG,
            roomType = RoomType.SINGLE,
            genderPreference = GenderPreference.MALE,
            location = LatLong(21.1950, 81.2870),
            address = "College Road, Supela",
            city = "Durg",
            state = "Chhattisgarh",
            postalCode = "491001",
            monthlyRent = 7500.0,
            securityDeposit = 15000.0,
            totalRooms = 25,
            availableRooms = 5,
            bathrooms = 12,
            amenities = listOf("WiFi", "AC", "Mess", "Laundry", "Security"),
            images = emptyList(),
            ownerId = "owner_search_1",
            ownerName = "Rajesh Sharma",
            ownerPhone = "+91-9876543213",
            ownerEmail = "rajesh@example.com",
            rating = 4.3f,
            reviewCount = 18,
            hasWifi = true,
            hasAc = true,
            hasMess = true,
            hasLaundry = true
        ),
        Accommodation(
            id = "search_2",
            title = "Girls Hostel with Study Facilities",
            description = "Safe and secure hostel for female students",
            type = AccommodationType.HOSTEL,
            roomType = RoomType.DOUBLE,
            genderPreference = GenderPreference.FEMALE,
            location = LatLong(21.2100, 81.3800),
            address = "University Road, Bhilai",
            city = "Bhilai",
            state = "Chhattisgarh",
            postalCode = "490009",
            monthlyRent = 6000.0,
            securityDeposit = 12000.0,
            totalRooms = 60,
            availableRooms = 12,
            bathrooms = 30,
            amenities = listOf("WiFi", "Study Room", "Security", "Mess", "Library"),
            images = emptyList(),
            ownerId = "owner_search_2",
            ownerName = "Meera Patel",
            ownerPhone = "+91-9876543214",
            ownerEmail = "meera@example.com",
            rating = 4.6f,
            reviewCount = 32,
            hasWifi = true,
            hasStudyRoom = true,
            hasMess = true
        ),
        Accommodation(
            id = "search_3",
            title = "Premium Apartment with Parking",
            description = "Fully furnished apartment with modern amenities",
            type = AccommodationType.APARTMENT,
            roomType = RoomType.SHARED,
            genderPreference = GenderPreference.NO_PREFERENCE,
            location = LatLong(21.2520, 81.6300),
            address = "Tech City, Sector 12",
            city = "Raipur",
            state = "Chhattisgarh",
            postalCode = "492010",
            monthlyRent = 15000.0,
            securityDeposit = 30000.0,
            totalRooms = 3,
            availableRooms = 1,
            bathrooms = 2,
            amenities = listOf("WiFi", "AC", "Parking", "Kitchen", "Balcony"),
            images = emptyList(),
            ownerId = "owner_search_3",
            ownerName = "Amit Kumar",
            ownerPhone = "+91-9876543215",
            ownerEmail = "amit@example.com",
            rating = 4.1f,
            reviewCount = 8,
            hasWifi = true,
            hasAc = true,
            hasParking = true
        ),
        Accommodation(
            id = "search_4",
            title = "Budget Friendly PG with Mess",
            description = "Affordable accommodation with good food",
            type = AccommodationType.PG,
            roomType = RoomType.TRIPLE,
            genderPreference = GenderPreference.MALE,
            location = LatLong(21.1920, 81.2840),
            address = "Station Road, Power House",
            city = "Durg",
            state = "Chhattisgarh",
            postalCode = "491001",
            monthlyRent = 5500.0,
            securityDeposit = 11000.0,
            totalRooms = 30,
            availableRooms = 8,
            bathrooms = 15,
            amenities = listOf("WiFi", "Mess", "Laundry", "Water Supply"),
            images = emptyList(),
            ownerId = "owner_search_4",
            ownerName = "Suresh Agarwal",
            ownerPhone = "+91-9876543216",
            ownerEmail = "suresh@example.com",
            rating = 3.9f,
            reviewCount = 25,
            hasWifi = true,
            hasMess = true,
            hasLaundry = true
        ),
        Accommodation(
            id = "search_5",
            title = "Co-living Space with Gym",
            description = "Modern co-living space with fitness facilities",
            type = AccommodationType.APARTMENT,
            roomType = RoomType.SINGLE,
            genderPreference = GenderPreference.NO_PREFERENCE,
            location = LatLong(21.2480, 81.6250),
            address = "New Capital Road, Sector 8",
            city = "Raipur",
            state = "Chhattisgarh",
            postalCode = "492010",
            monthlyRent = 18000.0,
            securityDeposit = 36000.0,
            totalRooms = 15,
            availableRooms = 3,
            bathrooms = 8,
            amenities = listOf("WiFi", "AC", "Gym", "Kitchen", "Parking", "Security"),
            images = emptyList(),
            ownerId = "owner_search_5",
            ownerName = "Priya Singh",
            ownerPhone = "+91-9876543217",
            ownerEmail = "priya@example.com",
            rating = 4.7f,
            reviewCount = 14,
            hasWifi = true,
            hasAc = true,
            hasGym = true,
            hasParking = true
        )
    )
}
