package com.hingoli.delivery

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.hingoli.delivery.ui.auth.AuthViewModel
import com.hingoli.delivery.ui.auth.LoginScreen
import com.hingoli.delivery.ui.earnings.EarningsScreen
import com.hingoli.delivery.ui.orders.AvailableOrdersScreen
import com.hingoli.delivery.ui.orders.MyDeliveriesScreen
import com.hingoli.delivery.ui.profile.ProfileScreen
import com.hingoli.delivery.ui.theme.DeliveryAppTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DeliveryAppTheme {
                DeliveryAppNavigation()
            }
        }
    }
}

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object AvailableOrders : Screen("available", "Orders", Icons.Default.ListAlt)
    object MyDeliveries : Screen("deliveries", "Deliveries", Icons.Default.DeliveryDining)
    object Earnings : Screen("earnings", "Earnings", Icons.Default.Wallet)
    object Profile : Screen("profile", "Profile", Icons.Default.Person)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeliveryAppNavigation() {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = hiltViewModel()
    val authState by authViewModel.uiState.collectAsState()
    
    val bottomNavItems = listOf(
        Screen.AvailableOrders,
        Screen.MyDeliveries,
        Screen.Earnings,
        Screen.Profile
    )
    
    // Show loading while checking saved auth status
    if (authState.isCheckingAuth) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    } else if (!authState.isLoggedIn) {
        LoginScreen(
            viewModel = authViewModel,
            onLoginSuccess = { /* Navigation handled by state */ }
        )
    } else {
        Scaffold(
            bottomBar = {
                NavigationBar {
                    val navBackStackEntry by navController.currentBackStackEntryAsState()
                    val currentDestination = navBackStackEntry?.destination
                    
                    bottomNavItems.forEach { screen ->
                        NavigationBarItem(
                            icon = { Icon(screen.icon, contentDescription = screen.title) },
                            label = { Text(screen.title) },
                            selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = Screen.AvailableOrders.route,
                modifier = Modifier.padding(innerPadding)
            ) {
                composable(Screen.AvailableOrders.route) {
                    AvailableOrdersScreen()
                }
                composable(Screen.MyDeliveries.route) {
                    MyDeliveriesScreen()
                }
                composable(Screen.Earnings.route) {
                    EarningsScreen()
                }
                composable(Screen.Profile.route) {
                    ProfileScreen(
                        onLogout = {
                            authViewModel.logout()
                        }
                    )
                }
            }
        }
    }
}
