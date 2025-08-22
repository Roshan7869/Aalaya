package com.aalay.app.ui.activities

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.aalay.app.R
import com.aalay.app.ui.fragments.*
import com.aalay.app.ui.theme.AalayTheme
import com.aalay.app.ui.viewmodels.SearchViewModel
import com.aalay.app.ui.viewmodels.ListingDetailViewModel
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        setContent {
            AalayTheme {
                val systemUiController = rememberSystemUiController()
                val useDarkIcons = !isSystemInDarkTheme()
                
                SideEffect {
                    systemUiController.setSystemBarsColor(
                        color = androidx.compose.ui.graphics.Color.Transparent,
                        darkIcons = useDarkIcons
                    )
                }
                
                AalayMainScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AalayMainScreen() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    
    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surfaceContainer,
                tonalElevation = 8.dp
            ) {
                bottomNavItems.forEach { item ->
                    NavigationBarItem(
                        icon = {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = item.label
                            )
                        },
                        label = { 
                            Text(
                                text = item.label,
                                style = MaterialTheme.typography.labelSmall
                            ) 
                        },
                        selected = currentDestination?.hierarchy?.any { 
                            it.route == item.route 
                        } == true,
                        onClick = {
                            navController.navigate(item.route) {
                                // Pop up to the start destination of the graph to
                                // avoid building up a large stack of destinations
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                // Avoid multiple copies of the same destination
                                launchSingleTop = true
                                // Restore state when reselecting a previously selected item
                                restoreState = true
                            }
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.onSecondaryContainer,
                            selectedTextColor = MaterialTheme.colorScheme.onSecondaryContainer,
                            indicatorColor = MaterialTheme.colorScheme.secondaryContainer,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            composable("home") {
                HomeScreen(
                    onNavigateToSearch = { 
                        navController.navigate("search")
                    },
                    onNavigateToListing = { listingId ->
                        navController.navigate("listing_detail/$listingId")
                    }
                )
            }
            
            composable("search") {
                val searchViewModel: SearchViewModel = hiltViewModel()
                SearchScreen(
                    viewModel = searchViewModel,
                    onNavigateToListing = { listingId ->
                        navController.navigate("listing_detail/$listingId")
                    },
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }
            
            composable("listing_detail/{listingId}") { backStackEntry ->
                val listingId = backStackEntry.arguments?.getString("listingId") ?: ""
                val listingDetailViewModel: ListingDetailViewModel = hiltViewModel()
                
                ListingDetailScreen(
                    listingId = listingId,
                    viewModel = listingDetailViewModel,
                    onNavigateBack = {
                        navController.popBackStack()
                    },
                    onNavigateToBooking = { bookingData ->
                        navController.navigate("booking/$bookingData")
                    }
                )
            }
            
            composable("roommate") {
                RoommateScreen(
                    onNavigateToChat = { userId ->
                        navController.navigate("chat/$userId")
                    }
                )
            }
            
            composable("profile") {
                ProfileScreen(
                    onNavigateToSettings = {
                        navController.navigate("settings")
                    },
                    onNavigateToBookingHistory = {
                        navController.navigate("booking_history")
                    }
                )
            }
            
            composable("wishlist") {
                WishlistScreen(
                    onNavigateToListing = { listingId ->
                        navController.navigate("listing_detail/$listingId")
                    }
                )
            }
        }
    }
}

data class BottomNavItem(
    val route: String,
    val icon: ImageVector,
    val label: String
)

val bottomNavItems = listOf(
    BottomNavItem(
        route = "home",
        icon = Icons.Filled.Home,
        label = "Home"
    ),
    BottomNavItem(
        route = "search",
        icon = Icons.Filled.Search,
        label = "Search"
    ),
    BottomNavItem(
        route = "wishlist",
        icon = Icons.Filled.Favorite,
        label = "Wishlist"
    ),
    BottomNavItem(
        route = "roommate",
        icon = Icons.Filled.People,
        label = "Roommate"
    ),
    BottomNavItem(
        route = "profile",
        icon = Icons.Filled.Person,
        label = "Profile"
    )
)