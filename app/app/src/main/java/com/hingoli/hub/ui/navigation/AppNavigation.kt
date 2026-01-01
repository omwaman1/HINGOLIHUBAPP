package com.hingoli.hub.ui.navigation

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.hingoli.hub.MainActivity
import com.hingoli.hub.data.repository.AuthRepository
import com.hingoli.hub.ui.auth.LoginScreen
import com.hingoli.hub.ui.category.CategoryScreen
import com.hingoli.hub.ui.chat.ConversationsListScreen
import com.hingoli.hub.ui.chat.ConversationScreen
import com.hingoli.hub.ui.detail.ListingDetailScreen
import com.hingoli.hub.ui.ecommerce.CartScreen
import com.hingoli.hub.ui.ecommerce.CheckoutScreen
import com.hingoli.hub.ui.product.ProductDetailScreen
import com.hingoli.hub.ui.ecommerce.OrderHistoryScreen
import com.hingoli.hub.ui.ecommerce.OrderDetailScreen
import com.hingoli.hub.ui.home.HomeScreen
import com.hingoli.hub.ui.listings.CategoryListingsScreen
import com.hingoli.hub.ui.mylistings.MyListingsScreen
import com.hingoli.hub.ui.product.ProductGridScreen
import com.hingoli.hub.data.settings.SettingsManager
import java.net.URLDecoder

/**
 * Extension function to find the Activity from a Context.
 * This properly handles ContextWrapper chains which is common in Compose.
 */
fun Context.findActivity(): Activity? {
    var context = this
    while (context is ContextWrapper) {
        if (context is Activity) return context
        context = context.baseContext
    }
    return null
}

@Composable
fun AppNavigation(
    navController: NavHostController,
    startDestination: String,
    onMenuClick: () -> Unit = {},
    settingsManager: SettingsManager? = null,
    authRepository: AuthRepository? = null,
    sellingCondition: String = "old"
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // Login Screen
        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }
        
        // Home Screen - Main landing page with filter chips
        composable(Screen.Home.route) {
            HomeScreen(
                onListingClick = { listingId ->
                    navController.navigate(Screen.ListingDetail.createRoute(listingId))
                },
                onShopProductClick = { productId ->
                    navController.navigate(Screen.ShopProductDetail.createRoute(productId))
                },
                onMenuClick = onMenuClick,
                onPostClick = {
                    navController.navigate(Screen.PostListing.createRoute("services"))
                },
                onNotificationsClick = {
                    navController.navigate(Screen.Notifications.route)
                },
                onViewAllClick = { listingType ->
                    when (listingType) {
                        "services" -> navController.navigate(Screen.Services.route)
                        "selling" -> navController.navigate(Screen.Selling.route)
                        "business" -> navController.navigate(Screen.Businesses.route)
                        "jobs" -> navController.navigate(Screen.Jobs.route)
                        "shop" -> navController.navigate(Screen.Shop.route)
                        "old" -> navController.navigate(Screen.Old.route)
                    }
                },
                settingsManager = settingsManager
            )
        }
        
        // Services Screen - using unified CategoryScreen
        composable(Screen.Services.route) {
            CategoryScreen(
                listingType = "services",
                onCategoryClick = { categoryId, categoryName ->
                    navController.navigate(
                        Screen.Listings.createRoute("services", categoryId, categoryName)
                    )
                },
                onMenuClick = onMenuClick,
                onPostClick = {
                    navController.navigate(Screen.PostListing.createRoute("services"))
                },
                settingsManager = settingsManager
            )
        }

        // Shop Screen - New items (condition=new) with e-commerce experience
        composable(Screen.Shop.route) {
            ProductGridScreen(
                onListingClick = { productId ->
                    // Navigate to shop product detail screen
                    navController.navigate(Screen.ShopProductDetail.createRoute(productId))
                },
                onProductClick = { productId ->
                    // Navigate to shop product detail screen
                    navController.navigate(Screen.ShopProductDetail.createRoute(productId))
                },
                onCartClick = {
                    navController.navigate(Screen.Cart.route)
                },
                onCheckoutClick = {
                    navController.navigate(Screen.Checkout.route)
                },
                settingsManager = settingsManager
            )
        }
        
        // Reels Screen - Instagram Reels in app
        composable(Screen.Reels.route) {
            com.hingoli.hub.ui.reels.ReelsScreen(
                onMenuClick = onMenuClick,
                onBackClick = { navController.popBackStack() },
                settingsManager = settingsManager!!
            )
        }
        
        // Old Screen - Used/Old items with category browsing from old_categories
        composable(Screen.Old.route) {
            com.hingoli.hub.ui.old.OldCategoryScreen(
                onCategoryClick = { categoryId, categoryName ->
                    navController.navigate(
                        Screen.OldProducts.createRoute(categoryId, categoryName)
                    )
                },
                onSubcategoryClick = { categoryId, subcategoryId, subcategoryName ->
                    navController.navigate(
                        Screen.OldProducts.createRoute(subcategoryId, subcategoryName)
                    )
                },
                onMenuClick = onMenuClick,
                onPostClick = {
                    navController.navigate(Screen.PostListing.createRouteWithCondition("selling", "old"))
                },
                settingsManager = settingsManager
            )
        }
        
        // Old Products List Screen
        composable(
            route = Screen.OldProducts.route,
            arguments = listOf(
                navArgument("categoryId") { type = NavType.IntType },
                navArgument("categoryName") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val categoryId = backStackEntry.arguments?.getInt("categoryId") ?: 0
            val categoryName = java.net.URLDecoder.decode(
                backStackEntry.arguments?.getString("categoryName") ?: "",
                "UTF-8"
            )
            com.hingoli.hub.ui.old.OldProductListScreen(
                categoryId = categoryId,
                categoryName = categoryName,
                onBackClick = { navController.popBackStack() },
                onProductClick = { productId ->
                    navController.navigate(Screen.OldProductDetail.createRoute(productId))
                },
                settingsManager = settingsManager
            )
        }
        
        // Old Product Detail Screen - uses ProductDetailScreen for consistent UX
        composable(
            route = Screen.OldProductDetail.route,
            arguments = listOf(
                navArgument("productId") { type = NavType.LongType }
            )
        ) { backStackEntry ->
            val productId = backStackEntry.arguments?.getLong("productId") ?: 0L
            val context = androidx.compose.ui.platform.LocalContext.current
            
            ProductDetailScreen(
                onBack = { navController.popBackStack() },
                onGoToCart = { navController.navigate(Screen.Cart.route) },
                onBuyNow = { _ ->
                    navController.navigate(Screen.Checkout.route)
                },
                onChatClick = { conversationId, sellerName ->
                    navController.navigate(Screen.Conversation.createRoute(conversationId, sellerName))
                },
                onCallClick = { currentUserId, currentUserName, sellerId, sellerName ->
                    val callId = "call_${System.currentTimeMillis()}"
                    com.hingoli.hub.ui.call.VoiceCallActivity.start(
                        context = context,
                        callId = callId,
                        userId = currentUserId.toString(),
                        userName = currentUserName,
                        conversationId = "product_call_$sellerId",
                        targetUserId = sellerId,
                        isIncoming = false
                    )
                }
            )
        }

        // Selling Screen (Legacy - keeps backward compatibility)
        composable(Screen.Selling.route) {
            if (sellingCondition == "new") {
                ProductGridScreen(
                    onListingClick = { productId ->
                        navController.navigate(Screen.ShopProductDetail.createRoute(productId))
                    },
                    onProductClick = { productId ->
                        navController.navigate(Screen.ShopProductDetail.createRoute(productId))
                    },
                    onCartClick = {
                        navController.navigate(Screen.Cart.route)
                    },
                    onCheckoutClick = {
                        navController.navigate(Screen.Checkout.route)
                    },
                    settingsManager = settingsManager
                )
            } else {
                CategoryScreen(
                    listingType = "selling",
                    onCategoryClick = { categoryId, categoryName ->
                        navController.navigate(
                            Screen.Listings.createRoute("selling", categoryId, categoryName)
                        )
                    },
                    onMenuClick = onMenuClick,
                    onPostClick = {
                        navController.navigate(Screen.PostListing.createRoute("selling"))
                    },
                    settingsManager = settingsManager
                )
            }
        }
        
        // Businesses Screen - using unified CategoryScreen
        composable(Screen.Businesses.route) {
            CategoryScreen(
                listingType = "business",
                onCategoryClick = { categoryId, categoryName ->
                    navController.navigate(
                        Screen.Listings.createRoute("business", categoryId, categoryName)
                    )
                },
                onMenuClick = onMenuClick,
                onPostClick = {
                    navController.navigate(Screen.PostListing.createRoute("business"))
                },
                settingsManager = settingsManager
            )
        }

        // Jobs Screen - Direct listings with filter chips (no category selection)
        composable(Screen.Jobs.route) {
            com.hingoli.hub.ui.jobs.JobsScreen(
                onListingClick = { listingId ->
                    navController.navigate(Screen.ListingDetail.createRoute(listingId))
                },
                onMenuClick = onMenuClick,
                onPostClick = {
                    navController.navigate(Screen.PostListing.createRoute("jobs"))
                },
                settingsManager = settingsManager
            )
        }
        
        // Listings Screen
        composable(
            route = Screen.Listings.route,
            arguments = listOf(
                navArgument("listingType") { type = NavType.StringType },
                navArgument("categoryId") { type = NavType.IntType },
                navArgument("categoryName") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val listingType = backStackEntry.arguments?.getString("listingType") ?: "services"
            val categoryId = backStackEntry.arguments?.getInt("categoryId") ?: 0
            val categoryName = backStackEntry.arguments?.getString("categoryName")?.let {
                URLDecoder.decode(it, "UTF-8")
            } ?: ""
            
            CategoryListingsScreen(
                listingType = listingType,
                categoryId = categoryId,
                categoryName = categoryName,
                onListingClick = { listingId ->
                    navController.navigate(Screen.ListingDetail.createRoute(listingId))
                },
                onProductClick = { productId ->
                    navController.navigate(Screen.ShopProductDetail.createRoute(productId))
                },
                onBackClick = {
                    navController.popBackStack()
                },
                onPostClick = {
                    navController.navigate(Screen.PostListing.createRoute(listingType))
                }
            )
        }
        
        // Listing Detail Screen
        composable(
            route = Screen.ListingDetail.route,
            arguments = listOf(
                navArgument("listingId") { type = NavType.LongType }
            )
        ) { backStackEntry ->
            val listingId = backStackEntry.arguments?.getLong("listingId") ?: 0L
            
            ListingDetailScreen(
                listingId = listingId,
                onBackClick = {
                    navController.popBackStack()
                },
                onChatClick = { conversationId, listingTitle ->
                    navController.navigate(Screen.Conversation.createRoute(conversationId, listingTitle))
                },
                onProfileIncomplete = { actionType ->
                    navController.navigate(Screen.EditProfileForAction.createRoute(actionType, listingId))
                },
                onProductClick = { productId ->
                    navController.navigate(Screen.ShopProductDetail.createRoute(productId))
                },
                settingsManager = settingsManager
            )
        }
        
        // Chat List Screen
        composable(Screen.ChatList.route) {
            ConversationsListScreen(
                onBackClick = { navController.popBackStack() },
                onConversationClick = { conversationId, listingTitle ->
                    navController.navigate(Screen.Conversation.createRoute(conversationId, listingTitle))
                }
            )
        }
        
        // Conversation Screen
        composable(
            route = Screen.Conversation.route,
            arguments = listOf(
                navArgument("conversationId") { type = NavType.StringType },
                navArgument("listingTitle") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val conversationId = backStackEntry.arguments?.getString("conversationId") ?: ""
            val listingTitle = backStackEntry.arguments?.getString("listingTitle")?.let {
                URLDecoder.decode(it, "UTF-8")
            } ?: ""
            
            ConversationScreen(
                listingTitle = listingTitle,
                onBackClick = { navController.popBackStack() }
        )
        }
        
        // My Listings Screen
        composable(Screen.MyListings.route) {
            MyListingsScreen(
                onBackClick = { navController.popBackStack() },
                onListingClick = { listingId ->
                    navController.navigate(Screen.ListingDetail.createRoute(listingId))
                },
                onShopProductClick = { productId ->
                    navController.navigate(Screen.ShopProductDetail.createRoute(productId))
                },
                onEditClick = { listingId ->
                    navController.navigate(Screen.EditListing.createRoute(listingId))
                },
                onEditProductClick = { productId ->
                    navController.navigate(Screen.EditProduct.createRoute(productId))
                },
                onPostClick = { listingType ->
                    navController.navigate(Screen.PostListing.createRoute(listingType))
                },
                onPostProductClick = { listingType, condition ->
                    navController.navigate(Screen.PostProduct.createRoute(listingType, condition))
                }
            )
        }
        
        // Post Listing Screen (Unified Form - Create Mode)
        composable(
            route = "${Screen.PostListing.route}?condition={condition}",
            arguments = listOf(
                navArgument("listingType") { 
                    type = NavType.StringType
                    defaultValue = "services"
                },
                navArgument("condition") { 
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            val listingType = backStackEntry.arguments?.getString("listingType") ?: "services"
            val condition = backStackEntry.arguments?.getString("condition")
            com.hingoli.hub.ui.listing.ListingFormScreen(
                listingType = listingType,
                listingId = null,
                condition = condition,
                onBackClick = { navController.popBackStack() },
                onSuccess = { navController.popBackStack() },
                settingsManager = settingsManager
            )
        }
        
        // Post Product Screen (with condition - for Sell Old / Sell New tabs)
        composable(
            route = Screen.PostProduct.route,
            arguments = listOf(
                navArgument("listingType") { 
                    type = NavType.StringType
                    defaultValue = "selling"
                },
                navArgument("condition") { 
                    type = NavType.StringType
                    defaultValue = "old"
                }
            )
        ) { backStackEntry ->
            val listingType = backStackEntry.arguments?.getString("listingType") ?: "selling"
            val condition = backStackEntry.arguments?.getString("condition") ?: "old"
            com.hingoli.hub.ui.listing.ListingFormScreen(
                listingType = listingType,
                listingId = null,
                condition = condition,
                onBackClick = { navController.popBackStack() },
                onSuccess = { navController.popBackStack() },
                settingsManager = settingsManager
            )
        }
        
        // Edit Listing Screen (Unified Form - Edit Mode)
        composable(
            route = Screen.EditListing.route,
            arguments = listOf(
                navArgument("listingId") { 
                    type = NavType.LongType
                }
            )
        ) { backStackEntry ->
            val listingId = backStackEntry.arguments?.getLong("listingId") ?: 0L
            com.hingoli.hub.ui.listing.ListingFormScreen(
                listingType = null,
                listingId = listingId,
                onBackClick = { navController.popBackStack() },
                onSuccess = { navController.popBackStack() },
                settingsManager = settingsManager
            )
        }
        
        // Edit Product Screen
        composable(
            route = Screen.EditProduct.route,
            arguments = listOf(
                navArgument("productId") { 
                    type = NavType.LongType
                }
            )
        ) { backStackEntry ->
            val productId = backStackEntry.arguments?.getLong("productId") ?: 0L
            com.hingoli.hub.ui.product.ProductFormScreen(
                productId = productId,
                onBackClick = { navController.popBackStack() },
                onSuccess = { navController.popBackStack() }
            )
        }
        
        // Edit Profile Screen
        composable(Screen.EditProfile.route) {
            com.hingoli.hub.ui.profile.EditProfileScreen(
                onBackClick = { navController.popBackStack() }
            )
        }
        
        // Edit Profile Screen with pending action (for profile completion before Call/Chat)
        composable(
            route = Screen.EditProfileForAction.route,
            arguments = listOf(
                navArgument("pendingAction") { type = NavType.StringType },
                navArgument("listingId") { type = NavType.LongType }
            )
        ) { backStackEntry ->
            val pendingAction = backStackEntry.arguments?.getString("pendingAction") ?: ""
            val listingId = backStackEntry.arguments?.getLong("listingId") ?: 0L
            
            com.hingoli.hub.ui.profile.EditProfileScreen(
                onBackClick = { navController.popBackStack() },
                pendingAction = pendingAction,
                listingId = listingId,
                onProfileCompleted = { action, lid ->
                    // Navigate back to listing detail - profile is now complete
                    // Pop back to listing detail and it will re-check profile status
                    navController.popBackStack(Screen.ListingDetail.createRoute(lid), inclusive = false)
                }
            )
        }
        
        // Notifications Screen
        composable(Screen.Notifications.route) {
            com.hingoli.hub.ui.notifications.NotificationsScreen(
                onBackClick = { navController.popBackStack() },
                onNotificationClick = { notification ->
                    // Navigate to listing if listing_id present
                    notification.listingId?.let { listingId ->
                        navController.navigate(Screen.ListingDetail.createRoute(listingId))
                    }
                }
            )
        }
        
        // WebPage Screen (for Privacy Policy, Terms of Service)
        composable(
            route = Screen.WebPage.route,
            arguments = listOf(
                navArgument("title") { type = NavType.StringType },
                navArgument("url") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val title = java.net.URLDecoder.decode(
                backStackEntry.arguments?.getString("title") ?: "",
                "UTF-8"
            )
            val url = java.net.URLDecoder.decode(
                backStackEntry.arguments?.getString("url") ?: "",
                "UTF-8"
            )
            com.hingoli.hub.ui.webpage.WebPageScreen(
                title = title,
                url = url,
                onBackClick = { navController.popBackStack() }
            )
        }
        
        // ==================== E-COMMERCE SCREENS ====================
        
        // Shop Product Detail (for shop products from businesses)
        composable(
            route = Screen.ShopProductDetail.route,
            arguments = listOf(navArgument("productId") { type = NavType.LongType })
        ) {
            val context = androidx.compose.ui.platform.LocalContext.current
            
            ProductDetailScreen(
                onBack = { navController.popBackStack() },
                onGoToCart = { navController.navigate(Screen.Cart.route) },
                onBuyNow = { productId ->
                    // Navigate to checkout
                    navController.navigate(Screen.Checkout.route)
                },
                onChatClick = { conversationId, sellerName ->
                    // Navigate to conversation screen
                    navController.navigate(Screen.Conversation.createRoute(conversationId, sellerName))
                },
                onCallClick = { currentUserId, currentUserName, sellerId, sellerName ->
                    // Initiate in-app voice call using VoiceCallActivity
                    val callId = "call_${System.currentTimeMillis()}"
                    
                    com.hingoli.hub.ui.call.VoiceCallActivity.start(
                        context = context,
                        callId = callId,
                        userId = currentUserId.toString(),
                        userName = currentUserName,
                        conversationId = "product_call_$sellerId",
                        targetUserId = sellerId,
                        isIncoming = false
                    )
                }
            )
        }
        
        // Cart Screen
        composable(Screen.Cart.route) {
            CartScreen(
                onBack = { navController.popBackStack() },
                onCheckout = { navController.navigate(Screen.Checkout.route) },
                onProductClick = { productId ->
                    navController.navigate(Screen.ShopProductDetail.createRoute(productId))
                }
            )
        }
        
        // Checkout Screen
        composable(Screen.Checkout.route) {
            val context = androidx.compose.ui.platform.LocalContext.current
            val activity = context.findActivity() as? MainActivity
            
            CheckoutScreen(
                onBack = { navController.popBackStack() },
                onOrderSuccess = { orderId, orderNumber ->
                    navController.navigate(Screen.OrderSuccess.createRoute(orderId, orderNumber)) {
                        popUpTo(Screen.Checkout.route) { inclusive = true }
                    }
                },
                onRazorpayPayment = { orderId, razorpayOrderId, amount, onSuccess ->
                    if (activity != null) {
                        val userName = "User"
                        val userPhone = "0000000000"
                        
                        activity.startPayment(
                            orderId = orderId,
                            razorpayOrderId = razorpayOrderId,
                            amount = amount,
                            userName = userName,
                            userPhone = userPhone,
                            onSuccess = onSuccess,
                            onError = { /* Silent fail */ }
                        )
                    }
                },
                onTermsClick = {
                    navController.navigate(
                        Screen.WebPage.createRoute(
                            "Terms of Service",
                            "https://hellohingoli.com/api/terms-of-service.html"
                        )
                    )
                },
                onRefundClick = {
                    navController.navigate(
                        Screen.WebPage.createRoute(
                            "Refund Policy",
                            "https://hellohingoli.com/api/refund-policy.html"
                        )
                    )
                }
            )
        }
        
        // Orders Screen
        composable(Screen.Orders.route) {
            OrderHistoryScreen(
                onBack = { navController.popBackStack() },
                onOrderClick = { orderId ->
                    navController.navigate(Screen.OrderDetail.createRoute(orderId))
                }
            )
        }
        
        // Order Detail Screen
        composable(
            route = Screen.OrderDetail.route,
            arguments = listOf(navArgument("orderId") { type = NavType.LongType })
        ) { backStackEntry ->
            val orderId = backStackEntry.arguments?.getLong("orderId") ?: 0L
            OrderDetailScreen(
                orderId = orderId,
                onBack = { navController.popBackStack() }
            )
        }
        
        // Order Success Screen
        composable(
            route = Screen.OrderSuccess.route,
            arguments = listOf(
                navArgument("orderId") { type = NavType.LongType },
                navArgument("orderNumber") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val orderId = backStackEntry.arguments?.getLong("orderId") ?: 0L
            val orderNumber = backStackEntry.arguments?.getString("orderNumber") ?: ""
            
            com.hingoli.hub.ui.ecommerce.OrderSuccessScreen(
                orderId = orderId,
                orderNumber = orderNumber,
                onContinueShopping = {
                    navController.navigate(Screen.Selling.route) {
                        popUpTo(Screen.Home.route)
                    }
                },
                onViewOrders = {
                    navController.navigate(Screen.Orders.route) {
                        popUpTo(Screen.Home.route)
                    }
                }
            )
        }
    }
}

