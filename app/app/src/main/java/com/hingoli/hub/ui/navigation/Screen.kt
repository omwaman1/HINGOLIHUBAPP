package com.hingoli.hub.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Handyman
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Sell
import androidx.compose.material.icons.filled.BusinessCenter
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Handyman
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material.icons.outlined.Business
import androidx.compose.material.icons.outlined.Sell
import androidx.compose.material.icons.outlined.BusinessCenter
import androidx.compose.material.icons.outlined.VideoLibrary
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Home : Screen("home")
    object Services : Screen("services")
    object Shop : Screen("shop")           // New items (condition=new)
    object Old : Screen("old")             // Old items (condition=old)
    object OldProducts : Screen("old_products/{categoryId}/{categoryName}") {
        fun createRoute(categoryId: Int, categoryName: String): String {
            val encodedName = java.net.URLEncoder.encode(categoryName, "UTF-8")
            return "old_products/$categoryId/$encodedName"
        }
    }
    object OldProductDetail : Screen("old_product/{productId}") {
        fun createRoute(productId: Long): String {
            return "old_product/$productId"
        }
    }
    object Selling : Screen("selling")     // Legacy - redirects based on condition
    object Businesses : Screen("businesses")
    object Jobs : Screen("jobs")           // Now in drawer menu
    object Reels : Screen("reels")         // Instagram Reels - center of bottom nav
    object Listings : Screen("listings/{listingType}/{categoryId}/{categoryName}") {
        fun createRoute(listingType: String, categoryId: Int, categoryName: String): String {
            val encodedName = java.net.URLEncoder.encode(categoryName, "UTF-8")
            return "listings/$listingType/$categoryId/$encodedName"
        }
    }
    object ListingDetail : Screen("listing/{listingId}") {
        fun createRoute(listingId: Long): String {
            return "listing/$listingId"
        }
    }
    object ChatList : Screen("chat_list")
    object Conversation : Screen("conversation/{conversationId}/{listingTitle}") {
        fun createRoute(conversationId: String, listingTitle: String): String {
            return "conversation/$conversationId/${java.net.URLEncoder.encode(listingTitle, "UTF-8")}"
        }
    }
    object PostListing : Screen("post_listing/{listingType}") {
        fun createRoute(listingType: String = "services"): String {
            return "post_listing/$listingType"
        }
        fun createRouteWithCondition(listingType: String, condition: String): String {
            return "post_listing/$listingType?condition=$condition"
        }
    }
    object PostProduct : Screen("post_product/{listingType}/{condition}") {
        fun createRoute(listingType: String = "selling", condition: String = "old"): String {
            return "post_product/$listingType/$condition"
        }
    }
    object EditListing : Screen("edit_listing/{listingId}") {
        fun createRoute(listingId: Long): String {
            return "edit_listing/$listingId"
        }
    }
    object EditProduct : Screen("edit_product/{productId}") {
        fun createRoute(productId: Long): String {
            return "edit_product/$productId"
        }
    }
    object MyListings : Screen("my_listings")
    object EditProfile : Screen("edit_profile")
    object EditProfileForAction : Screen("edit_profile_for_action/{pendingAction}/{listingId}") {
        fun createRoute(pendingAction: String, listingId: Long): String {
            return "edit_profile_for_action/$pendingAction/$listingId"
        }
    }
    object Notifications : Screen("notifications")
    object WebPage : Screen("webpage/{title}/{url}") {
        fun createRoute(title: String, url: String): String {
            return "webpage/${java.net.URLEncoder.encode(title, "UTF-8")}/${java.net.URLEncoder.encode(url, "UTF-8")}"
        }
    }
    
    // E-commerce Screens
    object ShopProductDetail : Screen("shop_product/{productId}") {
        fun createRoute(productId: Long): String {
            return "shop_product/$productId"
        }
    }
    object Cart : Screen("cart")
    object Checkout : Screen("checkout")
    object Orders : Screen("orders")
    object OrderDetail : Screen("order/{orderId}") {
        fun createRoute(orderId: Long): String {
            return "order/$orderId"
        }
    }
    object OrderSuccess : Screen("order_success/{orderId}/{orderNumber}") {
        fun createRoute(orderId: Long, orderNumber: String): String {
            return "order_success/$orderId/$orderNumber"
        }
    }
}

data class BottomNavItem(
    val title: String,
    val titleMr: String,
    val screen: Screen,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

// New bottom navigation: Home, Services, Reels, Business, Old
val bottomNavItems = listOf(
    BottomNavItem(
        title = "Home",
        titleMr = "होम",
        screen = Screen.Home,
        selectedIcon = Icons.Filled.Home,
        unselectedIcon = Icons.Outlined.Home
    ),
    BottomNavItem(
        title = "Services",
        titleMr = "सेवा",
        screen = Screen.Services,
        selectedIcon = Icons.Filled.Handyman,
        unselectedIcon = Icons.Outlined.Handyman
    ),
    BottomNavItem(
        title = "Reels",
        titleMr = "रील्स",
        screen = Screen.Reels,
        selectedIcon = Icons.Filled.SlowMotionVideo,
        unselectedIcon = Icons.Outlined.SlowMotionVideo
    ),
    BottomNavItem(
        title = "Business",
        titleMr = "व्यवसाय",
        screen = Screen.Businesses,
        selectedIcon = Icons.Filled.Business,
        unselectedIcon = Icons.Outlined.Business
    ),
    BottomNavItem(
        title = "Old",
        titleMr = "जुने",
        screen = Screen.Old,
        selectedIcon = Icons.Filled.Sell,
        unselectedIcon = Icons.Outlined.Sell
    )
)

// Drawer menu items (Jobs moved here)
data class DrawerMenuItem(
    val title: String,
    val titleMr: String,
    val screen: Screen,
    val icon: ImageVector
)

val drawerMenuItems = listOf(
    DrawerMenuItem(
        title = "Shop",
        titleMr = "शॉप",
        screen = Screen.Shop,
        icon = Icons.Filled.ShoppingCart
    ),
    DrawerMenuItem(
        title = "Jobs",
        titleMr = "नोकरी",
        screen = Screen.Jobs,
        icon = Icons.Filled.BusinessCenter
    )
)
