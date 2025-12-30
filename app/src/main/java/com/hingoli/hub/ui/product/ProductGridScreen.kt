package com.hingoli.hub.ui.product

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import kotlinx.coroutines.launch
import com.hingoli.hub.data.model.Category
import com.hingoli.hub.data.model.Listing
import com.hingoli.hub.data.model.ShopProduct
import com.hingoli.hub.ui.components.EmptyView
import com.hingoli.hub.ui.components.ErrorView
import com.hingoli.hub.ui.components.HingoliHubTopAppBar
import com.hingoli.hub.ui.components.LoadingView
import com.hingoli.hub.ui.components.ShimmerGridScreen
import com.hingoli.hub.ui.theme.*
import com.hingoli.hub.data.settings.AppLanguage
import com.hingoli.hub.data.settings.SettingsManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductGridScreen(
    onListingClick: (Long) -> Unit,
    onProductClick: (Long) -> Unit, // Navigate to product detail
    onCartClick: () -> Unit = {},
    onCheckoutClick: () -> Unit = {}, // Navigate to checkout
    condition: String = "new", // 'new' for Shop tab, 'old' for Old tab
    viewModel: ProductGridViewModel = hiltViewModel(),
    settingsManager: SettingsManager? = null
) {
    val uiState by viewModel.uiState.collectAsState()
    val selectedLanguage by settingsManager?.languageFlow?.collectAsState(initial = AppLanguage.MARATHI) 
        ?: remember { mutableStateOf(AppLanguage.MARATHI) }
    val isMarathi = selectedLanguage == AppLanguage.MARATHI
    var showSortMenu by remember { mutableStateOf(false) }
    val gridState = rememberLazyGridState()
    val snackbarHostState = remember { SnackbarHostState() }
    
    // Collapsing top bar scroll behavior
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    
    // Show snackbar when cart message changes
    LaunchedEffect(uiState.cartMessage) {
        uiState.cartMessage?.let { message ->
            snackbarHostState.showSnackbar(
                message = message,
                duration = SnackbarDuration.Short
            )
            viewModel.clearCartMessage()
        }
    }
    
    // Set condition when screen opens
    LaunchedEffect(condition) {
        viewModel.setCondition(condition)
    }
    
    // Infinite scroll detection - load more when 6 items from end
    val shouldLoadMore by remember {
        derivedStateOf {
            val layoutInfo = gridState.layoutInfo
            val lastVisibleItem = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            val totalItems = layoutInfo.totalItemsCount
            lastVisibleItem >= totalItems - 6 && totalItems > 2
        }
    }
    
    // Trigger load more
    LaunchedEffect(shouldLoadMore) {
        if (shouldLoadMore && !uiState.isLoading && !uiState.isLoadingMore && uiState.hasMorePages) {
            viewModel.loadNextPage()
        }
    }
    
    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            // Collapsing Top App Bar
            TopAppBar(
                title = {
                    Text(
                        text = if (condition == "old") {
                            if (isMarathi) "जुन्या वस्तू" else "Old Items"
                        } else {
                            if (isMarathi) "खरेदी" else "Shop"
                        },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                actions = {
                    // Cart icon with badge
                    Box {
                        IconButton(onClick = onCartClick) {
                            Icon(
                                imageVector = Icons.Default.ShoppingCart,
                                contentDescription = "Cart",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        if (uiState.cartItemCount > 0) {
                            Badge(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .offset(x = (-4).dp, y = 4.dp),
                                containerColor = Color(0xFF22C55E)
                            ) {
                                Text(
                                    text = if (uiState.cartItemCount > 9) "9+" else uiState.cartItemCount.toString(),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    scrolledContainerColor = Color.White
                ),
                scrollBehavior = scrollBehavior
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFFF3F4F6)) // Light gray background for entire content
        ) {
            // Sticky Header - Search & Compact Categories (stays visible)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                // Compact Search Bar
                CompactSearchBar(
                    query = uiState.searchQuery,
                    onQueryChange = viewModel::onSearchQueryChange,
                    onSearch = viewModel::onSearch,
                    showSortMenu = showSortMenu,
                    onSortMenuToggle = { showSortMenu = it },
                    currentSortOrder = uiState.sortOrder,
                    onSortChange = { 
                        viewModel.onSortChange(it)
                        showSortMenu = false
                    },
                    isMarathi = isMarathi
                )
                
                Spacer(modifier = Modifier.height(6.dp))
                
                // Compact Category Chips - smaller
                CompactCategoryChips(
                    categories = uiState.categories,
                    selectedCategoryId = uiState.selectedCategoryId,
                    onCategorySelected = viewModel::onCategorySelected,
                    isMarathi = isMarathi
                )
            }
            
            // Products Grid
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                state = gridState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(all = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Content states
                when {
                    uiState.isLoading && uiState.products.isEmpty() -> {
                        item(span = { GridItemSpan(3) }, key = "loading_shimmer") {
                            ShimmerGridScreen()
                        }
                    }
                    uiState.error != null && uiState.products.isEmpty() -> {
                        item(span = { GridItemSpan(3) }, key = "error") {
                            ErrorView(
                                message = uiState.error!!,
                                onRetry = viewModel::refresh
                            )
                        }
                    }
                    uiState.shopProducts.isEmpty() -> {
                        item(span = { GridItemSpan(3) }, key = "empty") {
                            EmptyView(message = "No products found")
                        }
                    }
                    else -> {
                        // Shop Products
                        uiState.shopProducts.forEachIndexed { index, product ->
                            item(key = "shop_${index}_${product.productId}") {
                                val isInCart = product.productId in uiState.productsInCart
                                val isAdding = uiState.addingToCartProductId == product.productId
                                val isOwner = uiState.currentUserId > 0 && product.userId == uiState.currentUserId
                                ShopProductCard(
                                    product = product,
                                    isInCart = isInCart,
                                    isAdding = isAdding,
                                    isOwner = isOwner,
                                    onClick = { onProductClick(product.productId) },
                                    onAddToCart = { viewModel.addToCart(product) },
                                    onCheckoutClick = onCheckoutClick
                                )
                            }
                        }
                        
                        // Loading more indicator
                        if (uiState.isLoadingMore) {
                            item(span = { GridItemSpan(3) }, key = "loading_more") {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(24.dp),
                                        color = Primary
                                    )
                                }
                            }
                        }
                        
                        // Bottom spacing
                        item(span = { GridItemSpan(3) }, key = "bottom_spacer") {
                            Spacer(modifier = Modifier.height(60.dp))
                        }
                    }
                }
            }
        }
    }
}

// Compact Search Bar - smaller height
@Composable
private fun CompactSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    showSortMenu: Boolean,
    onSortMenuToggle: (Boolean) -> Unit,
    currentSortOrder: SortOrder,
    onSortChange: (SortOrder) -> Unit,
    isMarathi: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Compact Search Field
        Surface(
            modifier = Modifier
                .weight(1f)
                .height(40.dp), // Smaller height
            shape = RoundedCornerShape(8.dp),
            color = Color(0xFFF5F5F5)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = Color(0xFF9CA3AF),
                    modifier = Modifier.size(18.dp)
                )
                
                Spacer(modifier = Modifier.width(6.dp))
                
                androidx.compose.foundation.text.BasicTextField(
                    value = query,
                    onValueChange = onQueryChange,
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodySmall.copy(
                        color = Color.Black
                    ),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = { onSearch() }),
                    decorationBox = { innerTextField ->
                        Box {
                            if (query.isEmpty()) {
                                Text(
                                    text = if (isMarathi) "शोधा..." else "Search...",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFF9CA3AF)
                                )
                            }
                            innerTextField()
                        }
                    }
                )
            }
        }
        
        // Compact Sort Button
        Box {
            Surface(
                modifier = Modifier.size(40.dp),
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFFF5F5F5)
            ) {
                IconButton(
                    onClick = { onSortMenuToggle(true) },
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.SwapVert,
                        contentDescription = "Sort",
                        tint = if (currentSortOrder != SortOrder.NONE) Primary else Color(0xFF6B7280),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            
            DropdownMenu(
                expanded = showSortMenu,
                onDismissRequest = { onSortMenuToggle(false) }
            ) {
                SortOrder.values().forEach { sortOption ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = sortOption.displayName,
                                fontWeight = if (sortOption == currentSortOrder) FontWeight.Bold else FontWeight.Normal,
                                color = if (sortOption == currentSortOrder) Primary else Color.Unspecified,
                                style = MaterialTheme.typography.bodySmall
                            )
                        },
                        onClick = { onSortChange(sortOption) }
                    )
                }
            }
        }
    }
}

// Compact Category Chips - smaller size
@Composable
private fun CompactCategoryChips(
    categories: List<Category>,
    selectedCategoryId: Int?,
    onCategorySelected: (Int?) -> Unit,
    isMarathi: Boolean = false
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // "All" icon chip
        item {
            CompactIconChip(
                name = if (isMarathi) "सर्व" else "All",
                imageUrl = null,
                isSelected = selectedCategoryId == null,
                onClick = { onCategorySelected(null) }
            )
        }
        
        // Category chips with icons
        items(categories) { category ->
            CompactIconChip(
                name = category.getLocalizedName(isMarathi),
                imageUrl = category.imageUrl ?: category.iconUrl,
                isSelected = selectedCategoryId == category.categoryId,
                onClick = { onCategorySelected(category.categoryId) }
            )
        }
    }
}

// Compact icon chip - smaller size with image
@Composable
private fun CompactIconChip(
    name: String,
    imageUrl: String?,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .width(56.dp)
            .clickable { onClick() },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Small icon container
        Surface(
            modifier = Modifier.size(44.dp),
            shape = RoundedCornerShape(10.dp),
            color = if (isSelected) Primary.copy(alpha = 0.1f) else Color(0xFFF5F5F5),
            border = if (isSelected) {
                androidx.compose.foundation.BorderStroke(1.5.dp, Primary)
            } else {
                null
            }
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                if (!imageUrl.isNullOrEmpty()) {
                    AsyncImage(
                        model = imageUrl,
                        contentDescription = name,
                        modifier = Modifier
                            .size(28.dp)
                            .clip(RoundedCornerShape(6.dp)),
                        contentScale = ContentScale.Fit
                    )
                } else {
                    // "All" icon
                    Icon(
                        imageVector = Icons.Default.Apps,
                        contentDescription = name,
                        modifier = Modifier.size(24.dp),
                        tint = if (isSelected) Primary else Color(0xFF6B7280)
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(2.dp))
        
        // Category name - small text
        Text(
            text = name,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            color = if (isSelected) Primary else Color(0xFF6B7280),
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun CategoryChips(
    categories: List<Category>,
    selectedCategoryId: Int?,
    onCategorySelected: (Int?) -> Unit,
    isMarathi: Boolean = false
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.padding(vertical = 8.dp)
    ) {
        // "All" chip first - with Apps/Grid icon
        item {
            AllCategoryChip(
                name = if (isMarathi) "सर्व" else "All",
                isSelected = selectedCategoryId == null,
                onClick = { onCategorySelected(null) }
            )
        }
        
        // Category chips from API with images
        items(categories) { category ->
            CategoryChipWithIcon(
                name = category.getLocalizedName(isMarathi),
                imageUrl = category.imageUrl ?: category.iconUrl,
                icon = category.name,
                isSelected = selectedCategoryId == category.categoryId,
                onClick = { onCategorySelected(category.categoryId) }
            )
        }
    }
}

@Composable
private fun CategoryChipWithIcon(
    name: String,
    imageUrl: String?,
    icon: String? = null,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .width(72.dp)
            .clickable { onClick() },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Icon/Image container with rounded background
        Surface(
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(12.dp)),
            shape = RoundedCornerShape(12.dp),
            color = if (isSelected) Primary.copy(alpha = 0.1f) else Color(0xFFF5F5F5),
            border = if (isSelected) {
                androidx.compose.foundation.BorderStroke(2.dp, Primary)
            } else {
                null
            }
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                if (!imageUrl.isNullOrEmpty()) {
                    // Show category image from API
                    AsyncImage(
                        model = imageUrl,
                        contentDescription = name,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Fit
                    )
                } else {
                    // Show default icon based on category name
                    Text(
                        text = getCategoryEmoji(icon ?: name),
                        fontSize = 28.sp
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(4.dp))
        
        // Category name
        Text(
            text = name,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            color = if (isSelected) Primary else Color(0xFF6B7280),
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

// Special "All" category chip with Material Icon
@Composable
private fun AllCategoryChip(
    name: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .width(72.dp)
            .clickable { onClick() },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Icon container with rounded background
        Surface(
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(12.dp)),
            shape = RoundedCornerShape(12.dp),
            color = if (isSelected) Primary.copy(alpha = 0.1f) else Color(0xFFF5F5F5),
            border = if (isSelected) {
                androidx.compose.foundation.BorderStroke(2.dp, Primary)
            } else {
                null
            }
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                // Apps/Grid icon for "All"
                Icon(
                    imageVector = Icons.Default.Apps,
                    contentDescription = name,
                    modifier = Modifier.size(32.dp),
                    tint = if (isSelected) Primary else Color(0xFF6B7280)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(4.dp))
        
        // Category name
        Text(
            text = name,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            color = if (isSelected) Primary else Color(0xFF6B7280),
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

// Helper function to get emoji/icon for categories
private fun getCategoryEmoji(name: String): String {
    return when (name.lowercase()) {
        "all", "सर्व" -> "📦"
        "electronics", "इलेक्ट्रॉनिक्स" -> "📱"
        "fashion", "फॅशन" -> "👗"
        "home", "घर", "furniture", "फर्निचर" -> "🏠"
        "groceries", "किराणा" -> "🛒"
        "fruits", "फळे" -> "🍎"
        "vegetables", "भाज्या" -> "🥦"
        "dairy", "दुग्ध" -> "🧀"
        "bakery", "बेकरी" -> "🍞"
        "snacks", "स्नॅक्स" -> "🍿"
        "beverages", "पेये" -> "🧃"
        "beauty", "सौंदर्य" -> "💄"
        "sports", "खेळ" -> "⚽"
        "books", "पुस्तके" -> "📚"
        "toys", "खेळणी" -> "🧸"
        "vehicles", "वाहने" -> "🚗"
        "mobile", "मोबाईल" -> "📱"
        "laptop", "लॅपटॉप" -> "💻"
        "clothes", "कपडे" -> "👕"
        "shoes", "बूट", "footwear" -> "👟"
        "watches", "घड्याळे" -> "⌚"
        "jewelry", "दागिने" -> "💍"
        else -> "📦"
    }
}

// Keep old CategoryChip for backward compatibility (can be removed later)
@Composable
private fun CategoryChip(
    name: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        color = if (isSelected) Primary else Color(0xFFE5E5E5),
        contentColor = if (isSelected) Color.White else Color(0xFF6B7280)
    ) {
        Text(
            text = name,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}

@Composable
private fun ShopProductCard(
    product: ShopProduct,
    isInCart: Boolean = false,
    isAdding: Boolean = false,
    isOwner: Boolean = false,
    onClick: () -> Unit,
    onAddToCart: () -> Unit = {},
    onCheckoutClick: () -> Unit = {}
) {
    val context = LocalContext.current
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp), // More rounded corners
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp) // Subtle shadow for depth
    ) {
        Column {
            // Product Image - compact
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .background(Color(0xFFF5F5F5))
            ) {
                AsyncImage(
                    model = product.imageUrl,
                    contentDescription = product.productName,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
            
            // Product Info - compact
            Column(
                modifier = Modifier.padding(6.dp)
            ) {
                // Product Name
                Text(
                    text = product.productName,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = Color.Black
                )
                
                Spacer(modifier = Modifier.height(2.dp))
                
                // Price Row - Current price + Original price strikethrough
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Current Price (green)
                    Text(
                        text = "₹${String.format("%,.0f", product.price)}",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF22C55E) // Green color
                    )
                    
                    // Original/Discounted price strikethrough (gray)
                    product.discountedPrice?.let { originalPrice ->
                        if (originalPrice > product.price) {
                            Text(
                                text = "₹${String.format("%,.0f", originalPrice)}",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF9CA3AF),
                                textDecoration = TextDecoration.LineThrough
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(2.dp))
                
                // Rating Row - compact
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = "4.5",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFFF59E0B)
                    )
                    
                    Icon(
                        imageVector = Icons.Filled.Star,
                        contentDescription = null,
                        tint = Color(0xFFF59E0B),
                        modifier = Modifier.size(10.dp)
                    )
                    
                    Text(
                        text = "125",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF9CA3AF)
                    )
                }
                
                Spacer(modifier = Modifier.height(6.dp))
                
                // Button: Add to Cart - Rectangle shape
                // Disabled for owner's products
                Button(
                    onClick = { if (!isOwner) onAddToCart() },
                    enabled = !isAdding && !isOwner,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(28.dp),
                    shape = RoundedCornerShape(4.dp), // Rectangle with slight corner
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isOwner) Color(0xFF9CA3AF) else Color(0xFF22C55E), // Gray for owner, Green for add
                        disabledContainerColor = Color(0xFFE5E5E5)
                    )
                ) {
                    if (isAdding) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(14.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            text = if (isOwner) "Your Product" else "Add to Cart",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}
