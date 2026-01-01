package com.hingoli.hub.ui.listings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.hingoli.hub.data.model.Category
import com.hingoli.hub.data.model.ShopProduct
import com.hingoli.hub.ui.components.*
import com.hingoli.hub.ui.city.CitySelectionBottomSheet
import com.hingoli.hub.ui.city.CitySelectionViewModel
import com.hingoli.hub.ui.theme.*
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryListingsScreen(
    listingType: String,
    categoryId: Int,
    categoryName: String,
    onListingClick: (listingId: Long) -> Unit,
    onProductClick: (productId: Long) -> Unit = {}, // For selling type - click on products
    onBackClick: () -> Unit,
    onPostClick: () -> Unit,
    viewModel: CategoryListingsViewModel = hiltViewModel(),
    cityViewModel: CitySelectionViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val cityUiState by cityViewModel.uiState.collectAsState()
    val listState = rememberLazyListState()
    var searchQuery by remember { mutableStateOf("") }
    var dropdownExpanded by remember { mutableStateOf(false) }
    var showCityPicker by remember { mutableStateOf(false) }
    
    // Filter listings based on search query
    val filteredListings by remember(uiState.listings, searchQuery) {
        derivedStateOf {
            if (searchQuery.isBlank()) {
                uiState.listings
            } else {
                val queryLower = searchQuery.lowercase()
                uiState.listings.filter { listing ->
                    listing.title.lowercase().contains(queryLower) ||
                    (listing.description?.lowercase()?.contains(queryLower) == true)
                }
            }
        }
    }
    
    // Filter shop products based on search query
    val filteredShopProducts by remember(uiState.shopProducts, searchQuery) {
        derivedStateOf {
            if (searchQuery.isBlank()) {
                uiState.shopProducts
            } else {
                val queryLower = searchQuery.lowercase()
                uiState.shopProducts.filter { product ->
                    product.productName.lowercase().contains(queryLower) ||
                    (product.description?.lowercase()?.contains(queryLower) == true)
                }
            }
        }
    }
    
    // Show city selection bottom sheet
    if (showCityPicker) {
        CitySelectionBottomSheet(
            onDismiss = { showCityPicker = false },
            onCitySelected = { city ->
                // Reload listings when city changes
                viewModel.onCityChanged(city.name)
            },
            isMarathi = uiState.isMarathi,
            viewModel = cityViewModel
        )
    }
    
    // Load listings when screen opens or city changes
    LaunchedEffect(listingType, categoryId, cityUiState.selectedCity?.cityId) {
        viewModel.loadListings(listingType, categoryId, cityUiState.selectedCity?.name)
    }
    
    // Pagination - load more when reaching end
    LaunchedEffect(listState) {
        snapshotFlow {
            val lastVisibleItem = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            val totalItems = listState.layoutInfo.totalItemsCount
            lastVisibleItem >= totalItems - 3
        }.collect { shouldLoadMore ->
            if (shouldLoadMore && !uiState.isLoading && !uiState.isLoadingMore) {
                viewModel.loadMoreListings()
            }
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (uiState.isMarathi) "हिंगोली हब" else "HINGOLI HUB",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                actions = {
                    // Location selector
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .clickable { showCityPicker = true }
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = "Location",
                            tint = Primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = cityUiState.selectedCity?.getLocalizedName(uiState.isMarathi) ?: if (uiState.isMarathi) "हिंगोली" else "Hingoli",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = " ▼",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Search bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                placeholder = {
                    Text(
                        text = if (uiState.isMarathi) "${uiState.selectedSubcategory?.getLocalizedName(true) ?: categoryName} शोधा..." else "Search ${uiState.selectedSubcategory?.name ?: categoryName}...",
                        color = OnSurfaceVariant
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = OnSurfaceVariant
                    )
                },
                shape = RoundedCornerShape(24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Primary,
                    unfocusedBorderColor = BorderLight,
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                ),
                singleLine = true
            )
            
            // Subcategory dropdown filter and Post button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Subcategory dropdown
                Box(modifier = Modifier.weight(1f)) {
                    OutlinedCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { dropdownExpanded = true },
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = uiState.selectedSubcategory?.getLocalizedName(uiState.isMarathi) ?: categoryName,
                                style = MaterialTheme.typography.bodyMedium,
                                color = OnSurface,
                                fontWeight = FontWeight.Medium
                            )
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = "Select subcategory",
                                tint = OnSurfaceVariant
                            )
                        }
                    }
                    
                    // Dropdown menu
                    DropdownMenu(
                        expanded = dropdownExpanded,
                        onDismissRequest = { dropdownExpanded = false },
                        modifier = Modifier
                            .background(Color.White)
                    ) {
                        // Show all subcategories with localized names
                        uiState.subcategories.forEach { subcategory ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = subcategory.getLocalizedName(uiState.isMarathi),
                                        fontWeight = if (subcategory.categoryId == uiState.selectedSubcategory?.categoryId) 
                                            FontWeight.Bold else FontWeight.Normal,
                                        color = if (subcategory.categoryId == uiState.selectedSubcategory?.categoryId) 
                                            PrimaryBlue else OnSurface
                                    )
                                },
                                onClick = {
                                    viewModel.onSubcategorySelected(subcategory)
                                    dropdownExpanded = false
                                },
                                leadingIcon = if (subcategory.categoryId == uiState.selectedSubcategory?.categoryId) {
                                    {
                                        Icon(
                                            imageVector = Icons.Default.ArrowDropDown,
                                            contentDescription = null,
                                            tint = PrimaryBlue,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                } else null
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.width(12.dp))
                
                // Plus icon button - attractive circular design
                Surface(
                    onClick = onPostClick,
                    modifier = Modifier.size(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = AccentOrange,
                    shadowElevation = 4.dp
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add Listing",
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Listings content
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
            ) {
                when {
                    uiState.isLoading -> {
                        ShimmerListScreen()
                    }
                    uiState.error != null && filteredListings.isEmpty() && filteredShopProducts.isEmpty() -> {
                        ErrorView(
                            message = uiState.error!!,
                            onRetry = { viewModel.retry() }
                        )
                    }
                    uiState.isSellingType -> {
                        if (filteredShopProducts.isEmpty()) {
                            EmptyView(message = if (uiState.isMarathi) "कोणतीही उत्पादने आढळली नाहीत" else if (searchQuery.isBlank()) "No products found in ${uiState.selectedSubcategory?.name ?: categoryName}" else "No products match '${searchQuery}'")
                        } else {
                            LazyColumn(
                                state = listState,
                                contentPadding = PaddingValues(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                // Top banners
                                if (uiState.topBanners.isNotEmpty()) {
                                    item {
                                        BannerCarousel(
                                            banners = uiState.topBanners,
                                            onBannerClick = { banner ->
                                                banner.linkId?.let { id ->
                                                    onProductClick(id)
                                                }
                                            }
                                        )
                                    }
                                }
                                
                                items(
                                    items = filteredShopProducts,
                                    key = { it.productId }
                                ) { product ->
                                    // Simple Product Card for category listing
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { onProductClick(product.productId) },
                                        shape = RoundedCornerShape(12.dp),
                                        colors = CardDefaults.cardColors(containerColor = Color.White),
                                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(12.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            // Product Image
                                            AsyncImage(
                                                model = product.imageUrl,
                                                contentDescription = product.productName,
                                                modifier = Modifier
                                                    .size(80.dp)
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(Color(0xFFF5F5F5)),
                                                contentScale = ContentScale.Crop
                                            )
                                            
                                            Spacer(modifier = Modifier.width(12.dp))
                                            
                                            // Product Info
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = product.productName,
                                                    style = MaterialTheme.typography.bodyLarge,
                                                    fontWeight = FontWeight.Medium,
                                                    maxLines = 2,
                                                    overflow = TextOverflow.Ellipsis,
                                                    color = Color.Black
                                                )
                                                
                                                Spacer(modifier = Modifier.height(4.dp))
                                                
                                                Text(
                                                    text = "₹${String.format("%,.0f", product.price)}",
                                                    style = MaterialTheme.typography.titleMedium,
                                                    fontWeight = FontWeight.Bold,
                                                    color = PrimaryBlue
                                                )
                                                
                                                product.businessName?.let { businessName ->
                                                    Spacer(modifier = Modifier.height(4.dp))
                                                    Text(
                                                        text = businessName,
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = Color.Gray,
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                                
                                // Bottom banners
                                if (uiState.bottomBanners.isNotEmpty()) {
                                    item {
                                        BannerCarousel(
                                            banners = uiState.bottomBanners,
                                            onBannerClick = { banner ->
                                                banner.linkId?.let { id ->
                                                    onProductClick(id)
                                                }
                                            }
                                        )
                                    }
                                }
                                
                                // Loading more indicator
                                if (uiState.isLoadingMore) {
                                    item {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(16.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            CircularProgressIndicator(
                                                modifier = Modifier.size(32.dp),
                                                color = PrimaryBlue
                                            )
                                        }
                                    }
                                }
                                
                                // Bottom spacing
                                item {
                                    Spacer(modifier = Modifier.height(60.dp))
                                }
                            }
                        }
                    }
                    filteredListings.isEmpty() -> {
                        EmptyView(message = if (uiState.isMarathi) "कोणत्याही यादी आढळल्या नाहीत" else if (searchQuery.isBlank()) "No listings found in ${uiState.selectedSubcategory?.name ?: categoryName}" else "No listings match '${searchQuery}'")
                    }
                    else -> {
                        LazyColumn(
                            state = listState,
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Top banners
                            if (uiState.topBanners.isNotEmpty()) {
                                item {
                                    BannerCarousel(
                                        banners = uiState.topBanners,
                                        onBannerClick = { banner ->
                                            // Handle banner click - could navigate to listing or URL
                                            banner.linkId?.let { listingId ->
                                                onListingClick(listingId)
                                            }
                                        }
                                    )
                                }
                            }
                            
                            items(
                                items = filteredListings,
                                key = { it.listingId }
                            ) { listing ->
                                ListingCard(
                                    listing = listing,
                                    onClick = { onListingClick(listing.listingId) }
                                )
                            }
                            
                            // Bottom banners
                            if (uiState.bottomBanners.isNotEmpty()) {
                                item {
                                    BannerCarousel(
                                        banners = uiState.bottomBanners,
                                        onBannerClick = { banner ->
                                            banner.linkId?.let { listingId ->
                                                onListingClick(listingId)
                                            }
                                        }
                                    )
                                }
                            }
                            
                            // Loading more indicator
                            if (uiState.isLoadingMore) {
                                item {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(32.dp),
                                            color = PrimaryBlue
                                        )
                                    }
                                }
                            }
                            
                            // Bottom spacing for navigation bar
                            item {
                                Spacer(modifier = Modifier.height(60.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}
