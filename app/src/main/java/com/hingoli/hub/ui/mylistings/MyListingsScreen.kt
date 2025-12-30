package com.hingoli.hub.ui.mylistings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.hingoli.hub.data.model.Listing
import com.hingoli.hub.ui.components.EmptyView
import com.hingoli.hub.ui.components.ErrorView
import com.hingoli.hub.ui.components.LoadingView
import com.hingoli.hub.ui.components.ShimmerListScreen
import com.hingoli.hub.ui.theme.*
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.compose.ui.platform.LocalLifecycleOwner

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyListingsScreen(
    onBackClick: () -> Unit,
    onListingClick: (Long) -> Unit,
    onShopProductClick: (Long) -> Unit = {}, // For selling type items (shop products)
    onEditClick: (Long) -> Unit,
    onEditProductClick: (Long) -> Unit = {}, // For editing products (separate from listings)
    onPostClick: (String) -> Unit,
    onPostProductClick: (String, String) -> Unit = { type, condition -> onPostClick(type) },  // (listingType, condition) for products
    viewModel: MyListingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val lifecycleOwner = LocalLifecycleOwner.current
    
    // Refresh listings when screen becomes visible (after returning from post/edit)
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.loadMyListings()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }
    
    // Filter options - now localized (removed sell_old and sell_new as they are in side menu)
    val filterOptions = listOf(
        "all" to Strings.get("All", "सर्व", uiState.isMarathi),
        "services" to Strings.get("Services", "सेवा", uiState.isMarathi),
        "business" to Strings.get("Business", "व्यवसाय", uiState.isMarathi),
        "jobs" to Strings.get("Jobs", "नोकरी", uiState.isMarathi),
        "selling" to Strings.get("Products", "प्रोडक्ट्स", uiState.isMarathi)
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(Strings.get("My Listings", "माझ्या जाहिराती", uiState.isMarathi), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PrimaryBlue,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { 
                    // Navigate based on current filter
                    when (uiState.selectedFilter) {
                        "selling" -> onPostProductClick("selling", "old")  // Default to old for selling
                        else -> onPostClick(uiState.selectedFilter.takeIf { it != "all" } ?: "services")
                    }
                },
                containerColor = PrimaryBlue
            ) {
                Icon(Icons.Default.Add, "Post Listing", tint = Color.White)
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Filter chips row
            androidx.compose.foundation.lazy.LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(horizontal = 16.dp)
            ) {
                items(filterOptions.size) { index ->
                    val (key, label) = filterOptions[index]
                    FilterChip(
                        selected = uiState.selectedFilter == key,
                        onClick = { viewModel.setFilter(key) },
                        label = { Text(label) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = PrimaryBlue,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }
            
            // Content
            Box(modifier = Modifier.fillMaxSize()) {
                when {
                    uiState.isLoading -> ShimmerListScreen()
                    uiState.error != null -> ErrorView(
                        message = uiState.error!!,
                        onRetry = { viewModel.loadMyListings() }
                    )
                    uiState.listings.isEmpty() -> EmptyView(
                        message = if (uiState.selectedFilter == "all") 
                            "No listings yet.\nTap + to post your first listing!"
                        else
                            "No ${filterOptions.first { it.first == uiState.selectedFilter }.second.lowercase()} listings found."
                    )
                    else -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(uiState.listings, key = { it.listingId }) { listing ->
                                MyListingsItemCard(
                                    listing = listing,
                                    isMarathi = uiState.isMarathi,
                                    selectedFilter = uiState.selectedFilter,
                                    onClick = { 
                                        // Route selling items to shop product detail, others to listing detail
                                        if (listing.listingType == "selling") {
                                            onShopProductClick(listing.listingId)
                                        } else {
                                            onListingClick(listing.listingId)
                                        }
                                    },
                                    onEditClick = { 
                                        // Route selling items to product edit, others to listing edit
                                        if (listing.listingType == "selling") {
                                            onEditProductClick(listing.listingId)
                                        } else {
                                            onEditClick(listing.listingId)
                                        }
                                    },
                                    onDeleteClick = { viewModel.deleteListing(listing.listingId, listing.listingType) }
                                )
                            }
                            
                            item { Spacer(modifier = Modifier.height(80.dp)) }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MyListingsItemCard(
    listing: Listing,
    isMarathi: Boolean,
    selectedFilter: String = "all",
    onClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text(Strings.deleteListing(isMarathi)) },
            text = { Text(Strings.deleteListingConfirm(listing.title, isMarathi)) },
            confirmButton = {
                TextButton(
                    onClick = { 
                        onDeleteClick()
                        showDeleteDialog = false 
                    }
                ) {
                    Text(Strings.delete(isMarathi), color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text(Strings.cancel(isMarathi))
                }
            }
        )
    }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Image
            AsyncImage(
                model = listing.mainImageUrl ?: listing.images?.firstOrNull(),
                contentDescription = listing.title,
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )
            
            // Content
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = listing.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = when(listing.listingType) {
                            "services" -> Color(0xFFE0E7FF)
                            "selling" -> if (listing.condition == "old") Color(0xFFFEF3C7) else Color(0xFFDCFCE7)
                            "business" -> Color(0xFFCCFBF1)
                            "jobs" -> Color(0xFFFEF3C7)
                            else -> Color.LightGray
                        }
                    ) {
                        Text(
                            text = when {
                                listing.listingType == "selling" && listing.condition == "old" -> 
                                    Strings.get("OLD", "जुने", isMarathi)
                                listing.listingType == "selling" && listing.condition == "new" -> 
                                    Strings.get("NEW", "नवीन", isMarathi)
                                listing.listingType == "selling" -> 
                                    Strings.get("Product", "प्रोडक्ट", isMarathi)
                                else -> listing.listingType.replaceFirstChar { it.uppercase() }
                            },
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            color = when(listing.listingType) {
                                "services" -> Color(0xFF3730A3)
                                "selling" -> if (listing.condition == "old") Color(0xFFB45309) else Color(0xFF166534)
                                "business" -> Color(0xFF0F766E)
                                "jobs" -> Color(0xFFB45309)
                                else -> Color.DarkGray
                            }
                        )
                    }
                    
                    // Status badge
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = when(listing.status) {
                            "active" -> Color(0xFFDCFCE7)
                            "pending" -> Color(0xFFFEF3C7)
                            else -> Color(0xFFFEE2E2)
                        }
                    ) {
                        Text(
                            text = listing.status.replaceFirstChar { it.uppercase() },
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            color = when(listing.status) {
                                "active" -> Color(0xFF166534)
                                "pending" -> Color(0xFF92400E)
                                else -> Color(0xFF991B1B)
                            }
                        )
                    }
                }
                
                // Price
                listing.price?.let { price ->
                    Text(
                        text = "₹${String.format("%,.0f", price)}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryBlue
                    )
                }
                
                Text(
                    text = listing.city ?: "",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
            
            // Actions
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                IconButton(
                    onClick = onEditClick,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        Icons.Default.Edit,
                        "Edit",
                        tint = PrimaryBlue,
                        modifier = Modifier.size(20.dp)
                    )
                }
                IconButton(
                    onClick = { showDeleteDialog = true },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        Icons.Default.Delete,
                        "Delete",
                        tint = Color.Red,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}
