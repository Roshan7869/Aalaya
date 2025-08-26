package com.aalay.app.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.aalay.app.data.local.entities.LocationEntity
import com.aalay.app.data.local.entities.LocationType
import com.aalay.app.ui.viewmodels.LocationViewModel

// Doodle-style colors
object DoodleColors {
    val Primary = Color(0xFF1E88E5)
    val Secondary = Color(0xFFFF6B6B)
    val Accent = Color(0xFF4ECDC4)
    val Success = Color(0xFF45B7D1)
    val Warning = Color(0xFFFFD93D)
    val Background = Color(0xFFFFFBF7)
    val Surface = Color(0xFFFFFFFF)
    val OnSurface = Color(0xFF2D3436)
    val OnPrimary = Color.White
}

/**
 * Main Bhilai Location Screen with doodle-style UI
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BhilaiLocationScreen(
    viewModel: LocationViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val locations by viewModel.locations.collectAsStateWithLifecycle()
    val featuredLocations by viewModel.featuredLocations.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedType by viewModel.selectedType.collectAsStateWithLifecycle()
    val stats by viewModel.stats.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DoodleColors.Background)
            .padding(16.dp)
    ) {
        // Header Section
        DoodleHeader(
            stats = stats,
            onRefresh = { viewModel.refreshLocations() }
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Search and Filter Section
        SearchAndFilterSection(
            searchQuery = searchQuery,
            selectedType = selectedType,
            onSearchQueryChange = viewModel::updateSearchQuery,
            onClearSearch = viewModel::clearSearch,
            onTypeFilterChange = viewModel::setLocationTypeFilter
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Content Section
        if (uiState.isLoading) {
            DoodleLoadingIndicator()
        } else if (uiState.error != null) {
            DoodleErrorMessage(
                error = uiState.error!!,
                onRetry = viewModel::refreshLocations,
                onDismiss = viewModel::clearError
            )
        } else {
            LocationContent(
                locations = locations,
                featuredLocations = featuredLocations,
                searchQuery = searchQuery,
                viewModel = viewModel
            )
        }
    }
}

@Composable
private fun DoodleHeader(
    stats: com.aalay.app.ui.viewmodels.LocationStats?,
    onRefresh: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = DoodleColors.Surface),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "🏠 Bhilai Locations",
                    style = TextStyle(
                        fontFamily = FontFamily.Cursive,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = DoodleColors.Primary
                    )
                )
                
                stats?.let {
                    Text(
                        text = "📍 ${it.totalCount} places • ${it.roomCount} rooms • ${it.messCount} mess",
                        style = TextStyle(
                            fontFamily = FontFamily.Cursive,
                            fontSize = 14.sp,
                            color = DoodleColors.OnSurface.copy(alpha = 0.7f)
                        )
                    )
                }
            }
            
            DoodleButton(
                onClick = onRefresh,
                icon = Icons.Default.Refresh,
                text = "Refresh"
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchAndFilterSection(
    searchQuery: String,
    selectedType: LocationType?,
    onSearchQueryChange: (String) -> Unit,
    onClearSearch: () -> Unit,
    onTypeFilterChange: (LocationType?) -> Unit
) {
    Column {
        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChange,
            placeholder = {
                Text(
                    text = "🔍 Search rooms, mess, or areas...",
                    style = TextStyle(fontFamily = FontFamily.Cursive)
                )
            },
            leadingIcon = {
                Icon(Icons.Default.Search, contentDescription = "Search")
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = onClearSearch) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear search")
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(25.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = DoodleColors.Primary,
                unfocusedBorderColor = DoodleColors.Primary.copy(alpha = 0.5f)
            )
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Filter Chips
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(listOf(null) + LocationType.values()) { type ->
                DoodleFilterChip(
                    text = type?.displayName ?: "All",
                    icon = when (type) {
                        LocationType.ROOM -> "🏠"
                        LocationType.MESS -> "🍽️"
                        LocationType.BOTH -> "🏠🍽️"
                        null -> "📍"
                    },
                    selected = selectedType == type,
                    onClick = { onTypeFilterChange(type) }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DoodleFilterChip(
    text: String,
    icon: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(text = icon, fontSize = 14.sp)
                Text(
                    text = text,
                    style = TextStyle(
                        fontFamily = FontFamily.Cursive,
                        fontWeight = FontWeight.Medium
                    )
                )
            }
        },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = DoodleColors.Primary,
            selectedLabelColor = DoodleColors.OnPrimary,
            containerColor = DoodleColors.Surface,
            labelColor = DoodleColors.OnSurface
        ),
        shape = RoundedCornerShape(20.dp),
        border = FilterChipDefaults.filterChipBorder(
            borderColor = if (selected) DoodleColors.Primary else DoodleColors.Primary.copy(alpha = 0.3f),
            borderWidth = 2.dp
        )
    )
}

@Composable
private fun LocationContent(
    locations: List<LocationEntity>,
    featuredLocations: List<LocationEntity>,
    searchQuery: String,
    viewModel: LocationViewModel
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Featured locations section (when not searching)
        if (searchQuery.isEmpty() && featuredLocations.isNotEmpty()) {
            item {
                FeaturedLocationsSection(
                    featuredLocations = featuredLocations,
                    viewModel = viewModel
                )
            }
        }
        
        // All locations section
        if (locations.isNotEmpty()) {
            item {
                Text(
                    text = if (searchQuery.isEmpty()) "📋 All Locations" else "🔍 Search Results",
                    style = TextStyle(
                        fontFamily = FontFamily.Cursive,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = DoodleColors.Primary
                    ),
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
            
            items(locations) { location ->
                DoodleLocationCard(
                    location = location,
                    viewModel = viewModel
                )
            }
        } else {
            item {
                DoodleEmptyState(
                    message = if (searchQuery.isEmpty()) 
                        "No locations found. Try refreshing!" 
                    else 
                        "No results for \"$searchQuery\""
                )
            }
        }
    }
}

@Composable
private fun FeaturedLocationsSection(
    featuredLocations: List<LocationEntity>,
    viewModel: LocationViewModel
) {
    Column {
        Text(
            text = "⭐ Featured Places",
            style = TextStyle(
                fontFamily = FontFamily.Cursive,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = DoodleColors.Secondary
            ),
            modifier = Modifier.padding(vertical = 8.dp)
        )
        
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(featuredLocations) { location ->
                DoodleFeaturedCard(
                    location = location,
                    viewModel = viewModel
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun DoodleLocationCard(
    location: LocationEntity,
    viewModel: LocationViewModel
) {
    val uriHandler = LocalUriHandler.current
    val distance = viewModel.getDistanceFromCenter(location)
    val isWalkingDistance = viewModel.isWalkingDistance(location)
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                val url = viewModel.getNavigationUrl(location)
                uriHandler.openUri(url)
            },
        colors = CardDefaults.cardColors(containerColor = DoodleColors.Surface),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = if (location.type == "room") "🏠" else "🍽️",
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = location.name,
                            style = TextStyle(
                                fontFamily = FontFamily.Cursive,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = DoodleColors.OnSurface
                            ),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    
                    if (location.isVerified) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "✅", fontSize = 12.sp)
                            Text(
                                text = "Verified",
                                style = TextStyle(
                                    fontFamily = FontFamily.Cursive,
                                    fontSize = 12.sp,
                                    color = DoodleColors.Success
                                )
                            )
                        }
                    }
                }
                
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = distance,
                        style = TextStyle(
                            fontFamily = FontFamily.Cursive,
                            fontSize = 12.sp,
                            color = if (isWalkingDistance) DoodleColors.Success else DoodleColors.OnSurface.copy(alpha = 0.7f)
                        )
                    )
                    if (isWalkingDistance) {
                        Text(
                            text = "🚶‍♂️ Walking",
                            style = TextStyle(
                                fontFamily = FontFamily.Cursive,
                                fontSize = 10.sp,
                                color = DoodleColors.Success
                            )
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Address
            Text(
                text = "📍 ${location.address}",
                style = TextStyle(
                    fontFamily = FontFamily.Cursive,
                    fontSize = 14.sp,
                    color = DoodleColors.OnSurface.copy(alpha = 0.8f)
                ),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            
            // Price and Rating
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                location.getFormattedPrice()?.let { price ->
                    Text(
                        text = "💰 $price",
                        style = TextStyle(
                            fontFamily = FontFamily.Cursive,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = DoodleColors.Secondary
                        )
                    )
                }
                
                if (location.totalRatings > 0) {
                    Text(
                        text = location.getRatingDisplay(),
                        style = TextStyle(
                            fontFamily = FontFamily.Cursive,
                            fontSize = 12.sp,
                            color = DoodleColors.Warning
                        )
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Navigation Button
            DoodleNavigationButton(
                location = location,
                onClick = {
                    val url = viewModel.getNavigationUrl(location)
                    uriHandler.openUri(url)
                }
            )
        }
    }
}

@Composable
private fun DoodleFeaturedCard(
    location: LocationEntity,
    viewModel: LocationViewModel
) {
    val uriHandler = LocalUriHandler.current
    
    Card(
        modifier = Modifier
            .width(200.dp)
            .clickable {
                val url = viewModel.getNavigationUrl(location)
                uriHandler.openUri(url)
            },
        colors = CardDefaults.cardColors(containerColor = DoodleColors.Accent.copy(alpha = 0.1f)),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(2.dp, DoodleColors.Accent.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "⭐", fontSize = 16.sp)
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = location.name,
                    style = TextStyle(
                        fontFamily = FontFamily.Cursive,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = DoodleColors.OnSurface
                    ),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            DoodleNavigationButton(
                location = location,
                onClick = {
                    val url = viewModel.getNavigationUrl(location)
                    uriHandler.openUri(url)
                },
                compact = true
            )
        }
    }
}

@Composable
private fun DoodleNavigationButton(
    location: LocationEntity,
    onClick: () -> Unit,
    compact: Boolean = false
) {
    DoodleButton(
        onClick = onClick,
        icon = Icons.Default.Navigation,
        text = if (compact) "Navigate" else "🗺️ Navigate to ${location.name}",
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun DoodleButton(
    onClick: () -> Unit,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        colors = ButtonDefaults.buttonColors(
            containerColor = DoodleColors.Primary,
            contentColor = DoodleColors.OnPrimary
        ),
        shape = RoundedCornerShape(25.dp),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            style = TextStyle(
                fontFamily = FontFamily.Cursive,
                fontWeight = FontWeight.Bold,
                textDecoration = TextDecoration.Underline
            )
        )
    }
}

@Composable
private fun DoodleLoadingIndicator() {
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator(
                color = DoodleColors.Primary,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "🏠 Finding awesome places...",
                style = TextStyle(
                    fontFamily = FontFamily.Cursive,
                    fontSize = 16.sp,
                    color = DoodleColors.OnSurface.copy(alpha = 0.7f)
                )
            )
        }
    }
}

@Composable
private fun DoodleErrorMessage(
    error: String,
    onRetry: () -> Unit,
    onDismiss: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = DoodleColors.Secondary.copy(alpha = 0.1f)),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(2.dp, DoodleColors.Secondary.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "😅 Oops!",
                fontSize = 32.sp
            )
            Text(
                text = error,
                style = TextStyle(
                    fontFamily = FontFamily.Cursive,
                    fontSize = 14.sp,
                    color = DoodleColors.OnSurface
                ),
                modifier = Modifier.padding(vertical = 8.dp)
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                DoodleButton(
                    onClick = onRetry,
                    icon = Icons.Default.Refresh,
                    text = "Try Again"
                )
                OutlinedButton(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(25.dp)
                ) {
                    Text(
                        text = "Dismiss",
                        style = TextStyle(fontFamily = FontFamily.Cursive)
                    )
                }
            }
        }
    }
}

@Composable
private fun DoodleEmptyState(message: String) {
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "🤷‍♂️",
                fontSize = 64.sp
            )
            Text(
                text = message,
                style = TextStyle(
                    fontFamily = FontFamily.Cursive,
                    fontSize = 16.sp,
                    color = DoodleColors.OnSurface.copy(alpha = 0.7f)
                ),
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}