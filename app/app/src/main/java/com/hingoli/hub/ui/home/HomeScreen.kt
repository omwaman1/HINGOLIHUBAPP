package com.hingoli.hub.ui.home

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.ui.text.input.ImeAction
import coil.compose.AsyncImage
import com.hingoli.hub.data.model.Listing
import com.hingoli.hub.data.model.ShopProduct
import com.hingoli.hub.data.settings.AppLanguage
import com.hingoli.hub.data.settings.SettingsManager
import com.hingoli.hub.ui.city.CitySelectionBottomSheet
import com.hingoli.hub.ui.city.CitySelectionViewModel
import com.hingoli.hub.ui.components.*
import com.hingoli.hub.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onListingClick: (listingId: Long) -> Unit,
    onShopProductClick: (productId: Long) -> Unit = {},
    onMenuClick: () -> Unit = {},
    onPostClick: () -> Unit = {},
    onNotificationsClick: () -> Unit = {},
    onViewAllClick: (listingType: String) -> Unit = {},
    onSearchClick: (query: String, filter: CategoryFilter) -> Unit = { _, _ -> },
    viewModel: HomeViewModel = hiltViewModel(),
    cityViewModel: CitySelectionViewModel = hiltViewModel(),
    settingsManager: SettingsManager? = null
) {
    val uiState by viewModel.uiState.collectAsState()
    val cityUiState by cityViewModel.uiState.collectAsState()
    var showCityPicker by remember { mutableStateOf(false) }
    val selectedLanguage by settingsManager?.languageFlow?.collectAsState(initial = AppLanguage.MARATHI) 
        ?: remember { mutableStateOf(AppLanguage.MARATHI) }
    val isMarathi = selectedLanguage == AppLanguage.MARATHI
    
    // City selection bottom sheet
    if (showCityPicker) {
        CitySelectionBottomSheet(
            onDismiss = { showCityPicker = false },
            onCitySelected = { city ->
                viewModel.onCityChanged(city.name)
            },
            isMarathi = isMarathi,
            viewModel = cityViewModel
        )
    }
    
    // Load data when city changes
    LaunchedEffect(cityUiState.selectedCity?.cityId) {
        viewModel.loadHomeData(cityUiState.selectedCity?.name)
    }
    
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = if (isMarathi) "हिंगोली हब" else "HINGOLI HUB",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = Primary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onMenuClick) {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = "Menu",
                            tint = OnSurface
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onNotificationsClick) {
                        Icon(
                            imageVector = Icons.Outlined.Notifications,
                            contentDescription = "Notifications",
                            tint = Primary
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = Primary
                )
            )
        },
        containerColor = Color.White
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            // Combined Search bar with city selector - expands when focused
            item(key = "search_with_location") {
                var isSearchExpanded by remember { mutableStateOf(false) }
                val focusRequester = remember { androidx.compose.ui.focus.FocusRequester() }
                
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(28.dp),
                    color = Color(0xFFF5F5F5)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Location selector (hidden when search is expanded)
                        AnimatedVisibility(
                            visible = !isSearchExpanded,
                            enter = fadeIn() + expandHorizontally(),
                            exit = fadeOut() + shrinkHorizontally()
                        ) {
                            Row(
                                modifier = Modifier
                                    .clickable { showCityPicker = true }
                                    .padding(start = 8.dp, end = 4.dp, top = 14.dp, bottom = 14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = "Location",
                                    tint = Primary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(2.dp))
                                Text(
                                    text = cityUiState.selectedCity?.getLocalizedName(isMarathi) ?: if (isMarathi) "हिंगोली" else "Hingoli",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Medium,
                                    color = OnSurface,
                                    maxLines = 1
                                )
                                Icon(
                                    imageVector = Icons.Default.KeyboardArrowDown,
                                    contentDescription = "Change",
                                    tint = OnSurfaceVariant,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                        
                        // Divider (hidden when search is expanded)
                        AnimatedVisibility(
                            visible = !isSearchExpanded
                        ) {
                            Box(
                                modifier = Modifier
                                    .width(1.dp)
                                    .height(24.dp)
                                    .background(Color(0xFFE0E0E0))
                            )
                        }
                        
                        // Search input - expands to full width when focused
                        TextField(
                            value = uiState.searchQuery,
                            onValueChange = { viewModel.onSearchQueryChanged(it) },
                            modifier = Modifier
                                .weight(1f)
                                .focusRequester(focusRequester)
                                .onFocusChanged { focusState ->
                                    isSearchExpanded = focusState.isFocused
                                },
                            placeholder = {
                                Text(
                                    text = if (isMarathi) "सेवा, व्यवसाय, नोकरी शोधा..." else "Search services, business, jobs...",
                                    color = OnSurfaceVariant,
                                    style = MaterialTheme.typography.bodyMedium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = "Search",
                                    tint = if (isSearchExpanded) Primary else OnSurfaceVariant
                                )
                            },
                            trailingIcon = {
                                if (isSearchExpanded && uiState.searchQuery.isNotEmpty()) {
                                    IconButton(
                                        onClick = {
                                            viewModel.onSearchQueryChanged("")
                                            viewModel.loadHomeData(cityUiState.selectedCity?.name)
                                        }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Clear",
                                            tint = OnSurfaceVariant,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            },
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent
                            ),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                imeAction = ImeAction.Search
                            ),
                            keyboardActions = KeyboardActions(
                                onSearch = {
                                    viewModel.searchListings()
                                }
                            )
                        )
                    }
                }
            }
            
            // Banner Carousel from API - only show if banners available
            if (uiState.banners.isNotEmpty()) {
                item(key = "banner") {
                    BannerCarousel(
                        banners = uiState.banners,
                        onBannerClick = { banner ->
                            banner.linkId?.let { onListingClick(it) }
                        }
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
            
            // Loading state - show shimmer skeleton
            if (uiState.isLoading) {
                item(key = "loading_shimmer") {
                    ShimmerHomeScreen()
                }
            } else {
                // 1. Services Section
                if (uiState.servicesListings.isNotEmpty()) {
                    item(key = "services_header") {
                        CategorySectionHeader(
                            title = if (isMarathi) "हिंगोली सेवा" else "Services in Hingoli",
                            onViewAllClick = { onViewAllClick("services") },
                            accentColor = ColorServices,
                            isMarathi = isMarathi
                        )
                    }
                    item(key = "services_listings") {
                        GridListingSection(
                            listings = uiState.servicesListings.take(6),
                            onListingClick = onListingClick,
                            accentColor = ColorServices
                        )
                    }
                }
                
                // 2. Shop Section (Products)
                if (uiState.shopProducts.isNotEmpty()) {
                    item(key = "shop_header") {
                        CategorySectionHeader(
                            title = if (isMarathi) "खरेदी" else "Shop Local Products",
                            onViewAllClick = { onViewAllClick("shop") },
                            accentColor = ColorShop,
                            isMarathi = isMarathi
                        )
                    }
                    item(key = "shop_products") {
                        GridShopProductSection(
                            products = uiState.shopProducts.take(6),
                            onProductClick = onShopProductClick,
                            accentColor = ColorShop
                        )
                    }
                }
                
                // 3. Old Items Section (Buy & Sell Old Things) - shows products with condition='old'
                if (uiState.oldProducts.isNotEmpty()) {
                    item(key = "old_header") {
                        CategorySectionHeader(
                            title = if (isMarathi) "जुन्या वस्तू खरेदी विक्री" else "Buy Sell Old Things",
                            onViewAllClick = { onViewAllClick("old") },
                            accentColor = ColorRealEstate,
                            isMarathi = isMarathi
                        )
                    }
                    item(key = "old_products") {
                        GridShopProductSection(
                            products = uiState.oldProducts.take(6),
                            onProductClick = onShopProductClick,
                            accentColor = ColorRealEstate
                        )
                    }
                }
                
                // 4. Jobs Section
                if (uiState.jobsListings.isNotEmpty()) {
                    item(key = "jobs_header") {
                        CategorySectionHeader(
                            title = if (isMarathi) "नोकरी शोधा" else "Find Jobs",
                            onViewAllClick = { onViewAllClick("jobs") },
                            accentColor = ColorJobs,
                            isMarathi = isMarathi
                        )
                    }
                    item(key = "jobs_listings") {
                        GridListingSection(
                            listings = uiState.jobsListings.take(6),
                            onListingClick = onListingClick,
                            accentColor = ColorJobs
                        )
                    }
                }
                
                // 5. Business Section
                if (uiState.businessListings.isNotEmpty()) {
                    item(key = "business_header") {
                        CategorySectionHeader(
                            title = if (isMarathi) "स्थानिक व्यवसाय" else "Local Businesses",
                            onViewAllClick = { onViewAllClick("business") },
                            accentColor = ColorBusiness,
                            isMarathi = isMarathi
                        )
                    }
                    item(key = "business_listings") {
                        GridListingSection(
                            listings = uiState.businessListings.take(6),
                            onListingClick = onListingClick,
                            accentColor = ColorBusiness
                        )
                    }
                }
                
                // Bottom Banners (home_bottom placement)
                if (uiState.bottomBanners.isNotEmpty()) {
                    item(key = "bottom_banner") {
                        Spacer(modifier = Modifier.height(8.dp))
                        BannerCarousel(
                            banners = uiState.bottomBanners,
                            onBannerClick = { /* No action */ }
                        )
                    }
                }
                
                // Social Media Footer
                item(key = "social_media_footer") {
                    Spacer(modifier = Modifier.height(16.dp))
                    SocialMediaFooter(
                        isMarathi = isMarathi
                    )
                }
                
                // Empty state
                val hasListings = uiState.servicesListings.isNotEmpty() || 
                                 uiState.oldProducts.isNotEmpty() ||
                                 uiState.businessListings.isNotEmpty() ||
                                 uiState.jobsListings.isNotEmpty() ||
                                 uiState.shopProducts.isNotEmpty()
                
                if (!hasListings) {
                    item(key = "empty") {
                        EmptyView(
                            message = if (isMarathi) "सध्या कोणतीही जाहिरात नाही" else "No listings found"
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CategorySectionHeader(
    title: String,
    onViewAllClick: () -> Unit,
    accentColor: Color = Primary,
    isMarathi: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = OnSurface
        )
        // Clean minimalist pill button
        Surface(
            modifier = Modifier.clickable { onViewAllClick() },
            shape = RoundedCornerShape(16.dp),
            color = Color.White,
            border = androidx.compose.foundation.BorderStroke(1.dp, BorderLight)
        ) {
            Text(
                text = if (isMarathi) "सर्व पहा" else "View All",
                style = MaterialTheme.typography.bodySmall,
                color = OnSurface,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
            )
        }
    }
}

@Composable
private fun HorizontalListingRow(
    listings: List<Listing>,
    onListingClick: (Long) -> Unit
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(
            items = listings,
            key = { it.listingId }
        ) { listing ->
            HomeListingCard(
                listing = listing,
                onClick = { onListingClick(listing.listingId) }
            )
        }
    }
    Spacer(modifier = Modifier.height(8.dp))
}

@Composable
private fun DefaultPromotionalBanner() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .height(160.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.horizontalGradient(
                    colors = listOf(
                        Primary.copy(alpha = 0.9f),
                        Primary.copy(alpha = 0.7f)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(24.dp)
        ) {
            Text(
                text = "Local Talent, Global",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = "Reach. Discover More!",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
        
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            repeat(3) { index ->
                Box(
                    modifier = Modifier
                        .size(if (index == 0) 8.dp else 6.dp)
                        .clip(CircleShape)
                        .background(if (index == 0) Color.White else Color.White.copy(alpha = 0.5f))
                )
            }
        }
    }
}

@Composable
private fun HomeListingCard(
    listing: Listing,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(140.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column {
            AsyncImage(
                model = listing.mainImageUrl ?: "https://via.placeholder.com/140",
                contentDescription = listing.title,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)),
                contentScale = ContentScale.Crop
            )
            
            Column(
                modifier = Modifier.padding(8.dp)
            ) {
                Text(
                    text = listing.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = OnSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 18.sp
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(5) { index ->
                        Icon(
                            imageVector = Icons.Filled.Star,
                            contentDescription = null,
                            tint = if (index < listing.avgRating.toInt()) StarFilled else Color(0xFFE0E0E0),
                            modifier = Modifier.size(12.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "(${String.format("%.1f", listing.avgRating)})",
                        style = MaterialTheme.typography.labelSmall,
                        color = OnSurfaceVariant
                    )
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = when {
                        listing.listingType == "jobs" -> {
                            val min = listing.salaryMin
                            val max = listing.salaryMax
                            when {
                                min != null && max != null -> "₹ ${String.format("%,.0f", min)} - ${String.format("%,.0f", max)}"
                                min != null -> "₹ ${String.format("%,.0f", min)}+"
                                max != null -> "Up to ₹ ${String.format("%,.0f", max)}"
                                else -> "Contact"
                            }
                        }
                        listing.price != null -> "₹ ${String.format("%,.0f", listing.price)}"
                        else -> "Contact"
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = SuccessGreen
                )
            }
        }
    }
}

@Composable
private fun HorizontalShopProductRow(
    products: List<ShopProduct>,
    onProductClick: (Long) -> Unit,
    accentColor: Color = Primary // Default
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(
            items = products,
            key = { it.productId }
        ) { product ->
            HomeShopProductCard(
                product = product,
                onClick = { onProductClick(product.productId) },
                accentColor = accentColor
            )
        }
    }
    Spacer(modifier = Modifier.height(8.dp))
}

@Composable
private fun HomeShopProductCard(
    product: ShopProduct,
    onClick: () -> Unit,
    accentColor: Color = SuccessGreen
) {
    Card(
        modifier = Modifier
            .width(140.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, accentColor.copy(alpha = 0.3f))
    ) {
        Column {
            AsyncImage(
                model = product.imageUrl ?: "https://via.placeholder.com/140",
                contentDescription = product.productName,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)),
                contentScale = ContentScale.Crop
            )
            
            Column(
                modifier = Modifier.padding(8.dp)
            ) {
                Text(
                    text = product.productName,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = OnSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 18.sp
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                // Show discounted price if available
                if (product.discountedPrice != null && product.discountedPrice < product.price) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "₹${String.format("%,.0f", product.discountedPrice)}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = accentColor
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "₹${String.format("%,.0f", product.price)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = OnSurfaceVariant,
                            textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough
                        )
                    }
                } else {
                    Text(
                        text = "₹${String.format("%,.0f", product.price)}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = accentColor
                    )
                }
                
                // Business name
                if (!product.businessName.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = product.businessName,
                        style = MaterialTheme.typography.labelSmall,
                        color = OnSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

// Grid Layout Components for better vertical scrolling

@Composable
private fun GridListingSection(
    listings: List<Listing>,
    onListingClick: (Long) -> Unit,
    accentColor: Color = Primary // Default
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        // Display in 3-column grid (2 rows of 3)
        listings.chunked(3).forEach { rowItems ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                rowItems.forEach { listing ->
                    GridListingCard(
                        listing = listing,
                        onClick = { onListingClick(listing.listingId) },
                        modifier = Modifier.weight(1f),
                        accentColor = accentColor
                    )
                }
                // Fill empty space for incomplete rows
                repeat(3 - rowItems.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun GridListingCard(
    listing: Listing,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    accentColor: Color = SuccessGreen
) {
    // Clean flat design using Surface with border
    Surface(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        color = Color.White,
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderLight)
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Image - square
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFF0F0F0)),
                contentAlignment = Alignment.Center
            ) {
                if (listing.mainImageUrl != null) {
                    AsyncImage(
                        model = listing.mainImageUrl,
                        contentDescription = listing.title,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = Color(0xFFBDBDBD),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(6.dp))
            
            // Title
            Text(
                text = listing.title,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
                color = OnSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            
            // Rating
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(5) { index ->
                    Icon(
                        imageVector = Icons.Filled.Star,
                        contentDescription = null,
                        tint = if (index < listing.avgRating.toInt()) StarFilled else Color(0xFFE0E0E0),
                        modifier = Modifier.size(10.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(2.dp))
            
            // Price
            Text(
                text = when {
                    listing.listingType == "jobs" -> {
                        val min = listing.salaryMin
                        val max = listing.salaryMax
                        when {
                            min != null && max != null -> "₹${String.format("%,.0f", min)} - ${String.format("%,.0f", max)}"
                            min != null -> "₹${String.format("%,.0f", min)}+"
                            max != null -> "Up to ₹${String.format("%,.0f", max)}"
                            else -> "Contact"
                        }
                    }
                    else -> if (listing.price != null) "₹${String.format("%,.0f", listing.price)}" else "Contact"
                },
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                color = OnSurface,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun GridShopProductSection(
    products: List<ShopProduct>,
    onProductClick: (Long) -> Unit,
    accentColor: Color = Primary // Default
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        // Display in 3-column grid
        products.chunked(3).forEach { rowItems ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                rowItems.forEach { product ->
                    GridShopProductCard(
                        product = product,
                        onClick = { onProductClick(product.productId) },
                        modifier = Modifier.weight(1f),
                        accentColor = accentColor
                    )
                }
                // Fill empty space for incomplete rows
                repeat(3 - rowItems.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun GridShopProductCard(
    product: ShopProduct,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    accentColor: Color = SuccessGreen
) {
    // Clean flat design using Surface with border
    Surface(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        color = Color.White,
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderLight)
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Product image - square with "Old" tag
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFF0F0F0)),
                contentAlignment = Alignment.Center
            ) {
                if (product.imageUrl != null) {
                    AsyncImage(
                        model = product.imageUrl,
                        contentDescription = product.productName,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = Color(0xFFBDBDBD),
                        modifier = Modifier.size(24.dp)
                    )
                }
                
                // Show condition tag for products
                if (product.condition == "old") {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(4.dp)
                            .background(
                                color = Color(0xFFFF6B35),
                                shape = RoundedCornerShape(4.dp)
                            )
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "Old",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 9.sp
                        )
                    }
                } else if (product.condition == "new") {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(4.dp)
                            .background(
                                color = Color(0xFF4CAF50),
                                shape = RoundedCornerShape(4.dp)
                            )
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "New",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 9.sp
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(6.dp))
            
            // Product name
            Text(
                text = product.productName,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
                color = OnSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            
            Spacer(modifier = Modifier.height(2.dp))
            
            // Price
            Text(
                text = if (product.discountedPrice != null && product.discountedPrice < product.price) {
                    "\u20b9${String.format("%,.0f", product.discountedPrice)}"
                } else {
                    "\u20b9${String.format("%,.0f", product.price)}"
                },
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                color = OnSurface
            )
        }
    }
}

/**
 * Social Media Footer with buttons for YouTube, Instagram, Facebook and Play Store
 */
@Composable
private fun SocialMediaFooter(
    isMarathi: Boolean = false
) {
    val context = LocalContext.current
    
    // URLs
    val youtubeUrl = "https://www.youtube.com/@hingolihub"
    val instagramUrl = "https://www.instagram.com/hingolihub"
    val facebookUrl = "https://www.facebook.com/hingolihub"
    val playStoreUrl = "https://play.google.com/store/apps/details?id=com.hingoli.hub"
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Title
        Text(
            text = if (isMarathi) "आम्हाला फॉलो करा" else "Follow Us",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = OnSurface,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        
        // First Row - YouTube & Instagram
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SocialMediaButton(
                modifier = Modifier.weight(1f),
                iconUrl = "https://cdn-icons-png.flaticon.com/512/1384/1384060.png",
                label = "YouTube",
                backgroundColor = Color(0xFFFF0000),
                onClick = {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(youtubeUrl))
                    context.startActivity(intent)
                }
            )
            
            SocialMediaButton(
                modifier = Modifier.weight(1f),
                iconUrl = "https://cdn-icons-png.flaticon.com/512/174/174855.png",
                label = "Instagram",
                backgroundColor = Color(0xFFE4405F),
                onClick = {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(instagramUrl))
                    context.startActivity(intent)
                }
            )
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Second Row - Facebook & Play Store
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SocialMediaButton(
                modifier = Modifier.weight(1f),
                iconUrl = "https://cdn-icons-png.flaticon.com/512/124/124010.png",
                label = "Facebook",
                backgroundColor = Color(0xFF1877F2),
                onClick = {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(facebookUrl))
                    context.startActivity(intent)
                }
            )
            
            SocialMediaButton(
                modifier = Modifier.weight(1f),
                iconUrl = "https://cdn-icons-png.flaticon.com/512/300/300218.png",
                label = "Play Store",
                backgroundColor = Color(0xFF01875F),
                onClick = {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(playStoreUrl))
                    context.startActivity(intent)
                }
            )
        }
        
        // Bottom spacing for navigation bar
        Spacer(modifier = Modifier.height(80.dp))
    }
}

/**
 * Social media button with icon from URL and name
 */
@Composable
private fun SocialMediaButton(
    modifier: Modifier = Modifier,
    iconUrl: String,
    label: String,
    backgroundColor: Color,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier
            .height(48.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(10.dp),
        color = backgroundColor,
        shadowElevation = 4.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon from URL
            AsyncImage(
                model = iconUrl,
                contentDescription = label,
                modifier = Modifier.size(22.dp),
                contentScale = ContentScale.Fit
            )
            
            Spacer(modifier = Modifier.width(8.dp))
            
            // Label
            Text(
                text = label,
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1
            )
        }
    }
}
