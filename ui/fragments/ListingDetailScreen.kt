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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.aalay.app.R
import com.aalay.app.data.models.*
import com.aalay.app.ui.viewmodels.ListingDetailViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListingDetailScreen(
    listingId: String,
    viewModel: ListingDetailViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToBooking: (String) -> Unit
) {
    // Mock data for demonstration
    val accommodation = remember { getSampleAccommodationDetail(listingId) }
    var isFavorite by remember { mutableStateOf(false) }
    
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Top App Bar
        TopAppBar(
            title = { Text("Property Details") },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = stringResource(R.string.action_back)
                    )
                }
            },
            actions = {
                IconButton(onClick = { isFavorite = !isFavorite }) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = if (isFavorite) "Remove from favorites" else "Add to favorites",
                        tint = if (isFavorite) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                    )
                }
                IconButton(onClick = { /* TODO: Share */ }) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = stringResource(R.string.action_share)
                    )
                }
            }
        )
        
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                ImageGallerySection(accommodation)
            }
            
            item {
                BasicInfoSection(accommodation)
            }
            
            item {
                PricingSection(accommodation)
            }
            
            item {
                AmenitiesSection(accommodation)
            }
            
            item {
                DescriptionSection(accommodation)
            }
            
            item {
                LocationSection(accommodation)
            }
            
            item {
                OwnerInfoSection(accommodation)
            }
            
            item {
                ReviewsSection(accommodation)
            }
        }
        
        // Bottom Action Buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = { /* TODO: Contact owner */ },
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = Icons.Default.Phone,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.action_contact_owner))
            }
            
            Button(
                onClick = { onNavigateToBooking(accommodation.id) },
                modifier = Modifier.weight(1f)
            ) {
                Text(stringResource(R.string.action_book_now))
            }
        }
    }
}

@Composable
private fun ImageGallerySection(accommodation: Accommodation) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp)
                .clip(RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            // Image placeholder
            Icon(
                imageVector = Icons.Default.Home,
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = MaterialTheme.colorScheme.outline
            )
            
            // Image counter overlay
            Card(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
                )
            ) {
                Text(
                    text = "1/5",
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
private fun BasicInfoSection(accommodation: Accommodation) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = accommodation.title,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.LocationOn,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.outline
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "${accommodation.address}, ${accommodation.city}",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "${accommodation.rating} (${accommodation.reviewCount} reviews)",
                style = MaterialTheme.typography.bodyLarge
            )
        }
        
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AssistChip(
                onClick = { },
                label = { Text(accommodation.type.name) }
            )
            AssistChip(
                onClick = { },
                label = { Text(accommodation.roomType.name) }
            )
            AssistChip(
                onClick = { },
                label = { Text(accommodation.genderPreference.name) }
            )
        }
    }
}

@Composable
private fun PricingSection(accommodation: Accommodation) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Pricing Details",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(R.string.listing_monthly_rent),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = "₹${accommodation.monthlyRent.toInt()}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(R.string.listing_security_deposit),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                )
                Text(
                    text = "₹${accommodation.securityDeposit.toInt()}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                )
            }
            
            accommodation.electricityCharges?.let { charges ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Electricity Charges",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                    Text(
                        text = "₹${charges.toInt()}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                }
            }
        }
    }
}

@Composable
private fun AmenitiesSection(accommodation: Accommodation) {
    Column {
        Text(
            text = stringResource(R.string.listing_amenities),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(accommodation.amenities) { amenity ->
                AssistChip(
                    onClick = { },
                    label = { Text(amenity) },
                    leadingIcon = {
                        Icon(
                            imageVector = getAmenityIcon(amenity),
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
private fun DescriptionSection(accommodation: Accommodation) {
    Column {
        Text(
            text = "Description",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = accommodation.description,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            InfoItem(
                icon = Icons.Default.Bed,
                label = "Total Rooms",
                value = accommodation.totalRooms.toString()
            )
            InfoItem(
                icon = Icons.Default.Bathtub,
                label = "Bathrooms", 
                value = accommodation.bathrooms.toString()
            )
            InfoItem(
                icon = Icons.Default.CheckCircle,
                label = "Available",
                value = accommodation.availableRooms.toString()
            )
        }
    }
}

@Composable
private fun InfoItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun LocationSection(accommodation: Accommodation) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = stringResource(R.string.listing_location),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "${accommodation.address}, ${accommodation.city}, ${accommodation.state} ${accommodation.postalCode}",
                style = MaterialTheme.typography.bodyLarge
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Map placeholder
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Map,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.outline
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Interactive Map",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun OwnerInfoSection(accommodation: Accommodation) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Owner avatar placeholder
            Card(
                modifier = Modifier.size(60.dp),
                shape = RoundedCornerShape(50),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier.size(30.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = accommodation.ownerName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Property Owner",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (accommodation.ownerVerified) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Verified,
                            contentDescription = "Verified",
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Verified Owner",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
            
            OutlinedButton(
                onClick = { /* TODO: Contact owner */ }
            ) {
                Icon(
                    imageVector = Icons.Default.Message,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Contact")
            }
        }
    }
}

@Composable
private fun ReviewsSection(accommodation: Accommodation) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.listing_reviews),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            TextButton(onClick = { /* TODO: View all reviews */ }) {
                Text("View All")
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Sample reviews
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            repeat(2) { index ->
                ReviewItem(
                    reviewerName = "Student ${index + 1}",
                    rating = 4.0f + index * 0.5f,
                    review = "Great place to stay! The facilities are excellent and the location is perfect for students. Highly recommended.",
                    date = "${30 - index * 15} days ago"
                )
            }
        }
    }
}

@Composable
private fun ReviewItem(
    reviewerName: String,
    rating: Float,
    review: String,
    date: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = reviewerName,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = date,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(5) { index ->
                    Icon(
                        imageVector = if (index < rating.toInt()) Icons.Default.Star else Icons.Default.StarBorder,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = rating.toString(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = review,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

private fun getAmenityIcon(amenity: String): androidx.compose.ui.graphics.vector.ImageVector {
    return when (amenity.lowercase()) {
        "wifi" -> Icons.Default.Wifi
        "ac" -> Icons.Default.AcUnit
        "parking" -> Icons.Default.LocalParking
        "laundry" -> Icons.Default.LocalLaundryService
        "mess", "kitchen" -> Icons.Default.Restaurant
        "gym" -> Icons.Default.FitnessCenter
        "study room" -> Icons.Default.MenuBook
        "power backup" -> Icons.Default.Power
        "security" -> Icons.Default.Security
        "water supply" -> Icons.Default.WaterDrop
        else -> Icons.Default.CheckCircle
    }
}

private fun getSampleAccommodationDetail(listingId: String): Accommodation {
    return Accommodation(
        id = listingId,
        title = "Modern Student PG near Engineering College",
        description = "A comfortable and well-maintained PG accommodation perfect for students. Located in a safe neighborhood with easy access to colleges and public transportation. The rooms are spacious and well-ventilated with all necessary amenities included.",
        type = AccommodationType.PG,
        roomType = RoomType.SINGLE,
        genderPreference = GenderPreference.MALE,
        location = LatLong(21.1938, 81.2858),
        address = "College Road, Supela",
        city = "Durg",
        state = "Chhattisgarh",
        postalCode = "491001",
        monthlyRent = 8000.0,
        securityDeposit = 16000.0,
        electricityCharges = 1500.0,
        maintenanceCharges = 500.0,
        totalRooms = 20,
        availableRooms = 3,
        bathrooms = 10,
        amenities = listOf("WiFi", "AC", "Mess", "Laundry", "Security", "Power Backup", "Water Supply"),
        images = emptyList(),
        ownerId = "owner1",
        ownerName = "Ram Kumar Sharma",
        ownerPhone = "+91-9876543210",
        ownerEmail = "ram@example.com",
        ownerVerified = true,
        rating = 4.2f,
        reviewCount = 15,
        hasWifi = true,
        hasAc = true,
        hasMess = true,
        hasLaundry = true,
        hasPowerBackup = true,
        minimumStayMonths = 6,
        maximumStayMonths = 12
    )
}
