package com.hingoli.hub

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.hingoli.hub.data.repository.AuthRepository
import com.hingoli.hub.data.settings.AppLanguage
import com.hingoli.hub.data.settings.SettingsManager
import com.hingoli.hub.payment.RazorpayPaymentHelper
import com.hingoli.hub.ui.components.FullScreenDrawerContent
import com.hingoli.hub.ui.navigation.AppNavigation
import com.hingoli.hub.ui.navigation.Screen
import com.hingoli.hub.ui.navigation.bottomNavItems
import com.hingoli.hub.ui.theme.HingoliHubTheme
import com.razorpay.Checkout
import com.razorpay.PaymentData
import com.razorpay.PaymentResultWithDataListener
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity(), PaymentResultWithDataListener {
    
    // Payment callback holder
    private var paymentSuccessCallback: ((String, String) -> Unit)? = null
    private var paymentErrorCallback: ((String) -> Unit)? = null
    private var currentOrderId: Long? = null
    
    @Inject
    lateinit var authRepository: AuthRepository
    
    @Inject
    lateinit var settingsManager: SettingsManager
    
    @Inject
    lateinit var chatRepository: com.hingoli.hub.data.repository.ChatRepository
    
    @Inject
    lateinit var apiService: com.hingoli.hub.data.api.ApiService
    
    @Inject
    lateinit var sharedDataRepository: com.hingoli.hub.data.repository.SharedDataRepository
    
    // Permission launcher for notifications (Android 13+)
    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            Log.d("MainActivity", "✅ Notification permission granted")
        } else {
            Log.w("MainActivity", "❌ Notification permission denied")
            Toast.makeText(
                this, 
                "Notifications disabled. You won't receive call or message alerts.", 
                Toast.LENGTH_LONG
            ).show()
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        // Print app hash for SMS Retriever (check Logcat with tag: AppSignatureHelper)
        com.hingoli.hub.util.AppSignatureHelper(this).printHash()
        
        // Request notification permission on Android 13+ (API 33+)
        requestNotificationPermission()
        
        // Initialize Razorpay
        Checkout.preload(applicationContext)
        
        // Check if user is logged in - login required at start
        val isLoggedIn = runBlocking { authRepository.isLoggedIn() }
        val startDestination = if (isLoggedIn) Screen.Home.route else Screen.Login.route
        
        // Get user info for drawer
        val userName = runBlocking { authRepository.getCurrentUserName() }
        val userPhone = runBlocking { authRepository.getCurrentUserPhone() }
        
        // Get deep link extras from notification
        val listingId = intent?.getStringExtra("listingId")?.toLongOrNull()
        val conversationId = intent?.getStringExtra("conversationId")
        val deepLink = intent?.getStringExtra("deepLink")
        val notificationType = intent?.getStringExtra("notificationType")
        
        // Parse reel deep link from URL
        val reelId = parseReelDeepLink(intent)
        
        setContent {
            HingoliHubTheme {
                MainScreen(
                    startDestination = startDestination,
                    userName = userName,
                    userPhone = userPhone,
                    authRepository = authRepository,
                    settingsManager = settingsManager,
                    chatRepository = chatRepository,
                    apiService = apiService,
                    sharedDataRepository = sharedDataRepository,
                    initialListingId = listingId,
                    initialConversationId = conversationId,
                    initialDeepLink = deepLink,
                    initialNotificationType = notificationType,
                    initialReelId = reelId
                )
            }
        }
    }
    
    /**
     * Parse reel ID from deep link intent
     * Handles: https://hellohingoli.com/apiv5/reel/{id}
     *          hingoliHub://reel/{id}
     */
    private fun parseReelDeepLink(intent: Intent?): Int? {
        val data = intent?.data
        Log.d("DeepLink", "=== Deep Link Debug ===")
        Log.d("DeepLink", "Intent action: ${intent?.action}")
        Log.d("DeepLink", "Intent data URI: $data")
        Log.d("DeepLink", "Scheme: ${data?.scheme}")
        Log.d("DeepLink", "Host: ${data?.host}")
        Log.d("DeepLink", "Path: ${data?.path}")
        Log.d("DeepLink", "PathSegments: ${data?.pathSegments}")
        Log.d("DeepLink", "LastPathSegment: ${data?.lastPathSegment}")
        
        if (data == null) {
            Log.d("DeepLink", "No data in intent")
            return null
        }
        
        return try {
            val reelId = when {
                // Custom scheme: hingolihub://reel/123
                data.scheme?.equals("hingolihub", ignoreCase = true) == true && data.host == "reel" -> {
                    Log.d("DeepLink", "Matched custom scheme hingolihub://reel")
                    // For hingoliHub://reel/123, pathSegments would be ["123"]
                    val id = data.pathSegments?.firstOrNull()?.toIntOrNull()
                        ?: data.lastPathSegment?.toIntOrNull()
                    Log.d("DeepLink", "Extracted reel ID from custom scheme: $id")
                    id
                }
                // HTTPS: https://hellohingoli.com/apiv5/reel/123
                data.host == "hellohingoli.com" && data.path?.contains("/reel/") == true -> {
                    Log.d("DeepLink", "Matched HTTPS hellohingoli.com/reel")
                    val id = data.lastPathSegment?.toIntOrNull()
                    Log.d("DeepLink", "Extracted reel ID from HTTPS: $id")
                    id
                }
                else -> {
                    Log.d("DeepLink", "No match for deep link pattern")
                    null
                }
            }
            Log.d("DeepLink", "Final reel ID: $reelId")
            reelId
        } catch (e: Exception) {
            Log.e("DeepLink", "Failed to parse reel deep link: ${e.message}")
            null
        }
    }
    
    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED -> {
                    Log.d("MainActivity", "✅ Notification permission already granted")
                }
                shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS) -> {
                    // Show rationale and request permission
                    Log.d("MainActivity", "📣 Showing notification permission rationale")
                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
                else -> {
                    // Request permission directly
                    Log.d("MainActivity", "📣 Requesting notification permission")
                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        }
    }
    
    /**
     * Start Razorpay payment
     */
    fun startPayment(
        orderId: Long,
        razorpayOrderId: String,
        amount: Double,
        userName: String,
        userPhone: String,
        userEmail: String? = null,
        onSuccess: (paymentId: String, signature: String) -> Unit,
        onError: (String) -> Unit
    ) {
        currentOrderId = orderId
        paymentSuccessCallback = onSuccess
        paymentErrorCallback = onError
        
        RazorpayPaymentHelper.startPayment(
            activity = this,
            razorpayOrderId = razorpayOrderId,
            amount = amount,
            userName = userName,
            userPhone = userPhone,
            userEmail = userEmail
        )
    }
    
    override fun onPaymentSuccess(razorpayPaymentId: String?, paymentData: PaymentData?) {
        Log.d("Razorpay", "✅ Payment successful: $razorpayPaymentId")
        Log.d("Razorpay", "Payment Data - OrderId: ${paymentData?.orderId}, PaymentId: ${paymentData?.paymentId}, Signature: ${paymentData?.signature}")
        
        razorpayPaymentId?.let { paymentId ->
            // Get signature from PaymentData - this is required for backend verification
            val signature = paymentData?.signature ?: ""
            paymentSuccessCallback?.invoke(paymentId, signature)
        }
        clearPaymentCallbacks()
    }
    
    override fun onPaymentError(code: Int, response: String?, paymentData: PaymentData?) {
        Log.e("Razorpay", "❌ Payment failed: $code - $response")
        Log.e("Razorpay", "Payment Data on error: OrderId: ${paymentData?.orderId}")
        Toast.makeText(this, "Payment failed: ${response ?: "Unknown error"}", Toast.LENGTH_LONG).show()
        paymentErrorCallback?.invoke(response ?: "Payment failed")
        clearPaymentCallbacks()
    }
    
    private fun clearPaymentCallbacks() {
        paymentSuccessCallback = null
        paymentErrorCallback = null
        currentOrderId = null
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    startDestination: String,
    userName: String?,
    userPhone: String?,
    authRepository: AuthRepository,
    settingsManager: SettingsManager,
    chatRepository: com.hingoli.hub.data.repository.ChatRepository,
    apiService: com.hingoli.hub.data.api.ApiService,
    sharedDataRepository: com.hingoli.hub.data.repository.SharedDataRepository,
    initialListingId: Long? = null,
    initialConversationId: String? = null,
    initialDeepLink: String? = null,
    initialNotificationType: String? = null,
    initialReelId: Int? = null
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val selectedLanguage by settingsManager.languageFlow.collectAsState(initial = AppLanguage.MARATHI)
    val isMarathi = selectedLanguage == AppLanguage.MARATHI
    
    // Force update state
    var showForceUpdateDialog by remember { mutableStateOf(false) }
    var forceUpdateMessage by remember { mutableStateOf("") }
    var forceUpdateUrl by remember { mutableStateOf("") }
    
    // Check for force update on app start
    LaunchedEffect(Unit) {
        try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            val currentVersion = packageInfo.versionName ?: "1.0.0"
            
            val response = apiService.getAppConfig(currentVersion)
            if (response.isSuccessful) {
                response.body()?.data?.let { config ->
                    if (config.forceUpdate || config.updateRequired) {
                        forceUpdateMessage = if (isMarathi) config.updateMessageMr else config.updateMessage
                        forceUpdateUrl = config.playStoreUrl
                        showForceUpdateDialog = true
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("ForceUpdate", "Failed to check for update: ${e.message}")
            // Don't block app if version check fails
        }
    }
    
    // Show Force Update Dialog if required
    if (showForceUpdateDialog) {
        com.hingoli.hub.ui.components.ForceUpdateDialog(
            message = forceUpdateMessage,
            playStoreUrl = forceUpdateUrl,
            isMarathi = isMarathi
        )
    }
    
    // User state - refreshes when navigation changes
    var currentUserName by remember { mutableStateOf(userName) }
    var currentUserPhone by remember { mutableStateOf(userPhone) }
    
    // Handle deep link navigation on launch - ALL require login
    LaunchedEffect(Unit) {
        val isUserLoggedIn = authRepository.getCurrentUserName() != null
        
        if (!isUserLoggedIn) {
            // Redirect to login if any deep link is present but user not logged in
            if (initialNotificationType != null || initialListingId != null || 
                initialConversationId != null || initialReelId != null) {
                Log.d("DeepLink", "User not logged in, redirecting to login")
                navController.navigate(Screen.Login.route)
                return@LaunchedEffect
            }
        }
        
        // User is logged in - handle deep links
        if (initialNotificationType == "admin_notification") {
            navController.navigate(Screen.Notifications.route)
        }
        initialListingId?.let { navController.navigate(Screen.ListingDetail.createRoute(it)) }
        initialConversationId?.let { navController.navigate(Screen.Conversation.createRoute(it, "Chat")) }
        initialReelId?.let { reelId ->
            Log.d("DeepLink", "Opening reel: $reelId")
            navController.navigate(Screen.Reels.route)
        }
    }
    
    // Refresh user details on main screens (after login)
    LaunchedEffect(currentDestination?.route) {
        if (currentDestination?.route in listOf(
                Screen.Home.route, Screen.Services.route, Screen.Shop.route,
                Screen.Old.route, Screen.Businesses.route, Screen.Jobs.route
            )) {
            currentUserName = authRepository.getCurrentUserName()
            currentUserPhone = authRepository.getCurrentUserPhone()
        }
    }
    
    // Bottom bar visible only on main tab screens
    val showBottomBar = remember(currentDestination) {
        currentDestination?.route in listOf(
            Screen.Home.route, Screen.Services.route, Screen.Shop.route,
            Screen.Old.route, Screen.Businesses.route, Screen.Reels.route
        )
    }
    
    // Drawer enabled except on login screen
    val showDrawer = currentDestination?.route != Screen.Login.route
    
    // Track login state
    var isLoggedIn by remember { mutableStateOf(currentUserName != null) }
    
    // Update login state when user details change
    LaunchedEffect(currentUserName) {
        isLoggedIn = currentUserName != null
    }
    
    // Helper to require login - returns true if logged in, false if redirected to login
    val requireLogin: () -> Boolean = {
        if (isLoggedIn) {
            true
        } else {
            scope.launch { drawerState.close() }
            navController.navigate(Screen.Login.route)
            false
        }
    }
    
    // Navigation helper - closes drawer and navigates to route
    val navigateTo: (String, Boolean) -> Unit = { route, popToStart ->
        scope.launch { drawerState.close() }
        navController.navigate(route) {
            if (popToStart) {
                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
            }
            launchSingleTop = true
        }
    }
    
    // Navigation helper with auth check - only navigates if logged in
    val navigateWithAuth: (String) -> Unit = { route ->
        if (requireLogin()) {
            scope.launch { drawerState.close() }
            navController.navigate(route) { launchSingleTop = true }
        }
    }
    
    // Logout handler
    val handleLogout: () -> Unit = {
        scope.launch {
            authRepository.logout()
            // Clear all cached data to prevent data leakage between users
            sharedDataRepository.clearCache()
            // Clear user state
            currentUserName = null
            currentUserPhone = null
            isLoggedIn = false
            drawerState.close()
            navController.navigate(Screen.Login.route) {
                popUpTo(0) { inclusive = true }
            }
        }
    }
    
    // Placeholder click handler for coming soon features
    val showComingSoon: () -> Unit = {
        Toast.makeText(context, "Coming Soon!", Toast.LENGTH_SHORT).show()
        scope.launch { drawerState.close() }
    }
    
    // Share app handler
    val handleShare: () -> Unit = {
        val shareText = if (selectedLanguage == AppLanguage.MARATHI) {
            "हिंगोली हब - तुमचे स्थानिक मार्केटप्लेस!\n\nडाउनलोड करा: https://play.google.com/store/apps/details?id=com.hingoli.hub"
        } else {
            "Check out HINGOLI HUB - Your Local Marketplace!\n\nhttps://play.google.com/store/apps/details?id=com.hingoli.hub"
        }
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "HINGOLI HUB App")
            putExtra(Intent.EXTRA_TEXT, shareText)
        }
        context.startActivity(Intent.createChooser(shareIntent, if (selectedLanguage == AppLanguage.MARATHI) "शेअर करा" else "Share via"))
        scope.launch { drawerState.close() }
    }
    
    
    if (showDrawer) {
        
        // Handle back button press - close drawer if open (must be inside the drawer scope)
        BackHandler(enabled = drawerState.isOpen) {
            scope.launch { drawerState.close() }
        }
        
        ModalNavigationDrawer(
            drawerState = drawerState,
            gesturesEnabled = showBottomBar,
            drawerContent = {
                ModalDrawerSheet(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(0.85f), // 85% of screen width instead of full screen
                    drawerContainerColor = androidx.compose.ui.graphics.Color.White
                ) {
                    FullScreenDrawerContent(
                        userName = currentUserName,
                        userPhone = currentUserPhone,
                        selectedLanguage = selectedLanguage,
                        onCloseClick = { scope.launch { drawerState.close() } },
                        onEditProfileClick = { navigateWithAuth(Screen.EditProfile.route) },
                        onHomeClick = { navigateTo(Screen.Home.route, true) },
                        onChatsClick = { navigateWithAuth(Screen.ChatList.route) },
                        onMyOrdersClick = { navigateWithAuth(Screen.Orders.route) },
                        onMyListingsClick = { navigateWithAuth(Screen.MyListings.route) },
                        onJobsClick = { navigateTo(Screen.Jobs.route, false) },
                        onShopClick = { navigateTo(Screen.Shop.route, false) },
                        // Registration & Selling callbacks
                        onServiceRegistrationClick = { navigateWithAuth(Screen.PostListing.createRoute("services")) },
                        onBusinessRegistrationClick = { navigateWithAuth(Screen.PostListing.createRoute("business")) },
                        onSellNewProductClick = { navigateWithAuth(Screen.PostListing.createRouteWithCondition("selling", "new")) },
                        onSellOldProductClick = { navigateWithAuth(Screen.PostListing.createRouteWithCondition("selling", "old")) },
                        onHelpClick = {
                            if (requireLogin()) {
                                scope.launch { 
                                    drawerState.close()
                                    // Get current user ID and create/find support conversation
                                    val userId = authRepository.getCurrentUserId()
                                    if (userId != null) {
                                        try {
                                            val conversationId = chatRepository.getOrCreateSupportConversation(
                                                currentUserId = userId
                                            )
                                            navController.navigate(Screen.Conversation.createRoute(conversationId, "Help & Support"))
                                        } catch (e: Exception) {
                                            android.util.Log.e("MainActivity", "Failed to create support chat: ${e.message}")
                                        }
                                    }
                                }
                            }
                        },
                        onRateUsClick = {
                            scope.launch { drawerState.close() }
                            // Open Play Store app page
                            try {
                                context.startActivity(Intent(Intent.ACTION_VIEW, android.net.Uri.parse("market://details?id=com.hingoli.hub")))
                            } catch (e: android.content.ActivityNotFoundException) {
                                // Fall back to browser if Play Store not installed
                                context.startActivity(Intent(Intent.ACTION_VIEW, android.net.Uri.parse("https://play.google.com/store/apps/details?id=com.hingoli.hub")))
                            }
                        },
                        onShareClick = handleShare,
                        onPrivacyClick = {
                            scope.launch { drawerState.close() }
                            navController.navigate(
                                Screen.WebPage.createRoute(
                                    if (selectedLanguage == AppLanguage.MARATHI) "गोपनीयता धोरण" else "Privacy Policy",
                                    "https://hellohingoli.com/api/privacy-policy.html"
                                )
                            )
                        },
                        onTermsClick = {
                            scope.launch { drawerState.close() }
                            navController.navigate(
                                Screen.WebPage.createRoute(
                                    if (selectedLanguage == AppLanguage.MARATHI) "सेवा अटी" else "Terms of Service",
                                    "https://hellohingoli.com/api/terms-of-service.html"
                                )
                            )
                        },
                        onAboutClick = {
                            scope.launch { drawerState.close() }
                            navController.navigate(
                                Screen.WebPage.createRoute( 
                                    if (selectedLanguage == AppLanguage.MARATHI) "अ‍ॅप माहिती" else "About App",
                                    "https://hellohingoli.com/api/about-app.html"
                                )
                            )
                        },
                        onRefundClick = {
                            scope.launch { drawerState.close() }
                            navController.navigate(
                                Screen.WebPage.createRoute(
                                    if (selectedLanguage == AppLanguage.MARATHI) "परतावा धोरण" else "Refund Policy",
                                    "https://hellohingoli.com/api/refund-policy.html"
                                )
                            )
                        },
                        onLogoutClick = handleLogout,
                        onLanguageChange = { language ->
                            scope.launch {
                                settingsManager.setLanguage(language)
                            }
                        }
                    )
                }
            }
        ) {
            MainScaffold(
                navController = navController,
                startDestination = startDestination,
                showBottomBar = showBottomBar,
                currentDestination = currentDestination,
                onMenuClick = { 
                    scope.launch { drawerState.open() } 
                },
                selectedLanguage = selectedLanguage,
                settingsManager = settingsManager,
                authRepository = authRepository
            )
        }
    } else {
        MainScaffold(
            navController = navController,
            startDestination = startDestination,
            showBottomBar = false,
            currentDestination = currentDestination,
            selectedLanguage = selectedLanguage,
            settingsManager = settingsManager,
            authRepository = authRepository
        )
    }
}

@Composable
private fun MainScaffold(
    navController: androidx.navigation.NavHostController,
    startDestination: String,
    showBottomBar: Boolean,
    currentDestination: androidx.navigation.NavDestination?,
    onMenuClick: () -> Unit = {},
    selectedLanguage: AppLanguage = AppLanguage.MARATHI,
    settingsManager: SettingsManager? = null,
    authRepository: AuthRepository? = null
) {
    // State for selling condition toggle (old/new)
    var sellingCondition by remember { mutableStateOf("old") }
    
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            AnimatedVisibility(
                visible = showBottomBar,
                enter = slideInVertically(initialOffsetY = { it }),
                exit = slideOutVertically(targetOffsetY = { it })
            ) {
                // Check if on Reels screen for dark theme bottom bar
                val isReelsScreen = currentDestination?.route == Screen.Reels.route
                
                // Bottom navigation without toggle (Shop and Old are now separate tabs)
                NavigationBar(
                    modifier = Modifier.height(56.dp), // Compact height
                    containerColor = if (isReelsScreen) androidx.compose.ui.graphics.Color.Black else androidx.compose.ui.graphics.Color.White,
                    tonalElevation = 0.dp
                ) {
                    bottomNavItems.forEach { item ->
                        val isSelected = currentDestination?.hierarchy?.any { 
                            it.route == item.screen.route 
                        } == true
                        
                        NavigationBarItem(
                            icon = {
                                Icon(
                                    imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                                    contentDescription = item.title,
                                    modifier = Modifier.size(32.dp)
                                )
                            },
                            label = null, // Icons only, no labels
                            selected = isSelected,
                            onClick = {
                                // For Home, navigate directly to Home route (not restoring potentially wrong state)
                                if (item.screen == Screen.Home) {
                                    navController.navigate(Screen.Home.route) {
                                        popUpTo(Screen.Home.route) {
                                            inclusive = true
                                        }
                                        launchSingleTop = true
                                    }
                                } else {
                                    navController.navigate(item.screen.route) {
                                        popUpTo(Screen.Home.route) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            },
                            colors = if (isReelsScreen) {
                                // Dark theme colors for Reels
                                NavigationBarItemDefaults.colors(
                                    selectedIconColor = androidx.compose.ui.graphics.Color.White,
                                    selectedTextColor = androidx.compose.ui.graphics.Color.White,
                                    unselectedIconColor = androidx.compose.ui.graphics.Color(0xFF888888),
                                    unselectedTextColor = androidx.compose.ui.graphics.Color(0xFF888888),
                                    indicatorColor = androidx.compose.ui.graphics.Color.Transparent
                                )
                            } else {
                                // Light theme colors for other screens
                                NavigationBarItemDefaults.colors(
                                    selectedIconColor = com.hingoli.hub.ui.theme.Primary,
                                    selectedTextColor = com.hingoli.hub.ui.theme.Primary,
                                    unselectedIconColor = androidx.compose.ui.graphics.Color(0xFF9E9E9E),
                                    unselectedTextColor = androidx.compose.ui.graphics.Color(0xFF9E9E9E),
                                    indicatorColor = androidx.compose.ui.graphics.Color.Transparent
                                )
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        AppNavigation(
            navController = navController,
            startDestination = startDestination,
            onMenuClick = onMenuClick,
            settingsManager = settingsManager,
            authRepository = authRepository,
            sellingCondition = sellingCondition
        )
    }
}

@Composable
private fun SellingConditionToggle(
    selectedCondition: String,
    onConditionChange: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.Center
    ) {
        Surface(
            shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
            color = androidx.compose.ui.graphics.Color(0xFFF0F0F0),
            modifier = Modifier.height(44.dp)
        ) {
            Row(
                modifier = Modifier.padding(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Old button
                Surface(
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(6.dp),
                    color = if (selectedCondition == "old") 
                        androidx.compose.ui.graphics.Color.White 
                    else 
                        androidx.compose.ui.graphics.Color.Transparent,
                    shadowElevation = if (selectedCondition == "old") 3.dp else 0.dp,
                    modifier = Modifier
                        .clickable { onConditionChange("old") }
                ) {
                    Text(
                        text = "Old",
                        modifier = Modifier.padding(horizontal = 28.dp, vertical = 8.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = if (selectedCondition == "old") 
                            androidx.compose.ui.text.font.FontWeight.Bold 
                        else 
                            androidx.compose.ui.text.font.FontWeight.Medium,
                        color = if (selectedCondition == "old") 
                            com.hingoli.hub.ui.theme.Primary 
                        else 
                            androidx.compose.ui.graphics.Color(0xFF757575)
                    )
                }
                
                // New button
                Surface(
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(6.dp),
                    color = if (selectedCondition == "new") 
                        androidx.compose.ui.graphics.Color.White 
                    else 
                        androidx.compose.ui.graphics.Color.Transparent,
                    shadowElevation = if (selectedCondition == "new") 3.dp else 0.dp,
                    modifier = Modifier
                        .clickable { onConditionChange("new") }
                ) {
                    Text(
                        text = "New",
                        modifier = Modifier.padding(horizontal = 28.dp, vertical = 8.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = if (selectedCondition == "new") 
                            androidx.compose.ui.text.font.FontWeight.Bold 
                        else 
                            androidx.compose.ui.text.font.FontWeight.Medium,
                        color = if (selectedCondition == "new") 
                            com.hingoli.hub.ui.theme.Primary 
                        else 
                            androidx.compose.ui.graphics.Color(0xFF757575)
                    )
                }
            }
        }
    }
}

