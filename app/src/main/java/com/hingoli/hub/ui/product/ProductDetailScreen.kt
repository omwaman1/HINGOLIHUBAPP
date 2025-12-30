package com.hingoli.hub.ui.product

import android.widget.Toast
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.hingoli.hub.data.model.Review
import com.hingoli.hub.data.model.ShopProduct
import com.hingoli.hub.ui.components.ShimmerDetailScreen
import com.hingoli.hub.ui.detail.AddProductReviewDialog
import com.hingoli.hub.ui.detail.components.ReviewsTab
import com.hingoli.hub.ui.theme.Primary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailScreen(
    onBack: () -> Unit,
    onGoToCart: () -> Unit,
    onBuyNow: (Long) -> Unit,
    onChatClick: ((conversationId: String, sellerName: String) -> Unit)? = null,
    onCallClick: ((currentUserId: Long, currentUserName: String, sellerId: Long, sellerName: String) -> Unit)? = null,
    viewModel: ProductDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var showAddReviewDialog by remember { mutableStateOf(false) }
    
    // Show cart message toast
    LaunchedEffect(uiState.cartMessage) {
        uiState.cartMessage?.let { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            viewModel.clearCartMessage()
        }
    }
    
    // Show review submit message toast
    LaunchedEffect(uiState.reviewSubmitMessage) {
        uiState.reviewSubmitMessage?.let { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            viewModel.clearReviewSubmitMessage()
            if (message.contains("successfully")) {
                showAddReviewDialog = false
            }
        }
    }
    
    // Navigate to checkout when buy now is ready
    LaunchedEffect(uiState.buyNowReady) {
        if (uiState.buyNowReady) {
            viewModel.clearBuyNowReady()
            uiState.product?.let { product ->
                onBuyNow(product.productId)
            }
        }
    }
    
    Scaffold(
        topBar = {
            // Check if this is an old product using the isOldProduct flag
                    val isOldProduct = uiState.product?.isOldProduct == true
            TopAppBar(
                title = { Text("Product Details") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    // Share button
                    uiState.product?.let { product ->
                        IconButton(
                            onClick = {
                                val shareText = buildString {
                                    append("Check out \"${product.productName}\" on HINGOLI HUB!\n\n")
                                    append("💰 Price: ₹${String.format("%,.0f", product.price)}\n")
                                    product.discountedPrice?.let { original ->
                                        if (original > product.price) {
                                            val discount = ((original - product.price) / original * 100).toInt()
                                            append("🏷️ $discount% OFF!\n")
                                        }
                                    }
                                    product.city?.let { append("📍 $it\n") }
                                    append("\n🔗 Download HINGOLI HUB:\n")
                                    append("https://play.google.com/store/apps/details?id=com.hingoli.hub")
                                }
                                val shareIntent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(android.content.Intent.EXTRA_TEXT, shareText)
                                    putExtra(android.content.Intent.EXTRA_SUBJECT, product.productName)
                                }
                                context.startActivity(android.content.Intent.createChooser(shareIntent, "Share via"))
                            }
                        ) {
                            Icon(Icons.Default.Share, contentDescription = "Share")
                        }
                    }
                    
                    // Cart icon with badge - only for new products
                    if (!isOldProduct) {
                        BadgedBox(
                            badge = {
                                if (uiState.cartItemCount > 0) {
                                    Badge(
                                        containerColor = Color(0xFF22C55E),
                                        contentColor = Color.White
                                    ) {
                                        Text(uiState.cartItemCount.toString())
                                    }
                                }
                            }
                        ) {
                            IconButton(onClick = onGoToCart) {
                                Icon(Icons.Default.ShoppingCart, contentDescription = "Cart")
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )
        },
        bottomBar = {
            if (uiState.product != null) {
                val isOldProduct = uiState.product!!.isOldProduct
                if (isOldProduct && onCallClick != null && onChatClick != null) {
                    // Check if current user is the owner
                    val productUserId = uiState.product!!.userId
                    val currentUserId = uiState.currentUserId
                    val isOwner = productUserId != null && productUserId == currentUserId
                    
                    // Show Call and Chat buttons for old products
                    ContactSellerBar(
                        isCreatingChat = uiState.isCreatingChat,
                        isOwner = isOwner,
                        onCallClick = {
                            // Initiate in-app call with all needed info
                            val (_, sellerId, sellerName) = viewModel.getSellerCallInfo()
                            val currentUserId = uiState.currentUserId
                            val currentUserName = viewModel.getCurrentUserName()
                            if (sellerId != null && sellerName != null && currentUserId > 0) {
                                onCallClick(currentUserId, currentUserName, sellerId, sellerName)
                            }
                        },
                        onChatClick = {
                            // Start chat via ViewModel
                            viewModel.startChat { conversationId, sellerName ->
                                onChatClick(conversationId, sellerName)
                            }
                        }
                    )
                } else if (uiState.product!!.sellOnline) {
                    // Show cart/buy buttons for new products ONLY if sellOnline is true
                    BottomActionBar(
                        price = uiState.product!!.price,
                        discountedPrice = uiState.product!!.discountedPrice,
                        isAddingToCart = uiState.isAddingToCart,
                        onAddToCart = viewModel::addToCart,
                        onBuyNow = viewModel::buyNow
                    )
                }
                // If sellOnline is false and not old product, show "Contact for Purchase" bar
                else {
                    NotForSaleBar()
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                uiState.isLoading -> ShimmerDetailScreen()
                uiState.error != null -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(uiState.error!!, color = Color.Red)
                    }
                }
                uiState.product != null -> {
                    ProductContent(
                        product = uiState.product!!,
                        quantity = uiState.quantity,
                        onIncrement = viewModel::incrementQuantity,
                        onDecrement = viewModel::decrementQuantity,
                        isOldProduct = uiState.product!!.isOldProduct,
                        reviews = uiState.reviews,
                        isLoadingReviews = uiState.isLoadingReviews,
                        reviewCount = uiState.reviewCount,
                        avgRating = uiState.avgRating,
                        canWriteReview = uiState.canWriteReview,
                        onWriteReviewClick = { showAddReviewDialog = true }
                    )
                }
            }
        }
    }
    
    // Add Review Dialog
    if (showAddReviewDialog) {
        AddProductReviewDialog(
            onDismiss = { showAddReviewDialog = false },
            onConfirm = { rating, title, content, imageUris ->
                viewModel.submitReview(
                    context = context,
                    rating = rating,
                    title = title,
                    content = content,
                    imageUris = imageUris
                )
            },
            isSubmitting = uiState.isSubmittingReview
        )
    }
}

@Composable
private fun ProductContent(
    product: ShopProduct,
    quantity: Int,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
    isOldProduct: Boolean = false,
    reviews: List<Review> = emptyList(),
    isLoadingReviews: Boolean = false,
    reviewCount: Int = 0,
    avgRating: Double = 0.0,
    canWriteReview: Boolean = false,
    onWriteReviewClick: () -> Unit = {}
) {
    val scrollState = rememberScrollState()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
    ) {
        // Product Image - relative height (40% of screen)
        val configuration = LocalConfiguration.current
        val screenHeight = configuration.screenHeightDp.dp
        
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(screenHeight * 0.3f)
                .background(Color(0xFFF5F5F5))
        ) {
            AsyncImage(
                model = product.imageUrl,
                contentDescription = product.productName,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit
            )
            
            // Stock badge
            product.stockQty?.let { stock ->
                if (stock > 0 && stock <= 5) {
                    Surface(
                        modifier = Modifier
                            .padding(16.dp)
                            .align(Alignment.TopStart),
                        color = Color(0xFFFF9800),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = "Only $stock left",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else if (stock == 0) {
                    Surface(
                        modifier = Modifier
                            .padding(16.dp)
                            .align(Alignment.TopStart),
                        color = Color.Red,
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = "Out of Stock",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
        
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Title
            Text(
                text = product.productName,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Price Section
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "₹${String.format("%,.0f", product.price)}",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = Primary
                )
                
                product.discountedPrice?.let { originalPrice ->
                    if (originalPrice > product.price) {
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "₹${String.format("%,.0f", originalPrice)}",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.Gray,
                            textDecoration = TextDecoration.LineThrough
                        )
                        
                        val discount = ((originalPrice - product.price) / originalPrice * 100).toInt()
                        if (discount > 0) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                color = Color(0xFFE8F5E9),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = "$discount% OFF",
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    color = Color(0xFF2E7D32),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Stock Status
            product.stockQty?.let { stock ->
                Text(
                    text = if (stock > 0) "In Stock ($stock available)" else "Out of Stock",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (stock > 0) Color(0xFF4CAF50) else Color.Red,
                    fontWeight = FontWeight.Medium
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Rating
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                val displayRating = avgRating.toFloat()
                repeat(5) { index ->
                    Icon(
                        imageVector = if (index < displayRating) Icons.Default.Star else Icons.Default.StarBorder,
                        contentDescription = null,
                        tint = Color(0xFFFFC107),
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (reviewCount > 0) "${String.format("%.1f", avgRating)} ($reviewCount reviews)" else "No reviews yet",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(16.dp))
            
            // Quantity Selector - only for new products
            if (!isOldProduct) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Quantity:",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                    
                    Spacer(modifier = Modifier.width(16.dp))
                    
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, Color.LightGray)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = onDecrement,
                                enabled = quantity > 1
                            ) {
                                Icon(Icons.Default.Remove, contentDescription = "Decrease")
                            }
                            
                            Text(
                                text = quantity.toString(),
                                modifier = Modifier.padding(horizontal = 8.dp),
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold
                            )
                            
                            IconButton(
                                onClick = onIncrement,
                                enabled = quantity < 10
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "Increase")
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider()
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Description
            Text(
                text = "Description",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = product.description ?: "No description available.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.DarkGray,
                lineHeight = 22.sp
            )
            
            // Category
            product.categoryName?.let { category ->
                Spacer(modifier = Modifier.height(12.dp))
                Row {
                    Text(
                        text = "Category: ",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = category,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Primary
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(16.dp))
            
            // Seller/Business Info
            Text(
                text = "Seller",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFFF5F5F5),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Seller Avatar
                    val sellerName = product.businessName ?: "Shop"
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(Primary.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = sellerName.firstOrNull()?.uppercase() ?: "S",
                            style = MaterialTheme.typography.titleLarge,
                            color = Primary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(12.dp))
                    
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = sellerName,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.Verified,
                                contentDescription = "Verified",
                                tint = Primary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        product.city?.let { city ->
                            Text(
                                text = "📍 $city",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(16.dp))
            
            // Reviews Section
            Text(
                text = "Customer Reviews",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            ReviewsTab(
                reviews = reviews,
                isLoading = isLoadingReviews,
                canWriteReview = canWriteReview,
                onWriteReviewClick = onWriteReviewClick
            )
            
            // Bottom padding for action bar
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
private fun BottomActionBar(
    price: Double,
    discountedPrice: Double?,
    isAddingToCart: Boolean,
    onAddToCart: () -> Unit,
    onBuyNow: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shadowElevation = 8.dp,
        color = Color.White
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Add to Cart Button
            OutlinedButton(
                onClick = onAddToCart,
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp),
                enabled = !isAddingToCart,
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Primary)
            ) {
                if (isAddingToCart) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Primary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(Icons.Default.ShoppingCart, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Add to Cart")
                }
            }
            
            // Buy Now Button
            Button(
                onClick = onBuyNow,
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Primary)
            ) {
                Text("Buy Now", fontWeight = FontWeight.Bold)
            }
        }
    }
}

/**
 * "Not For Sale" bar - shown when sellOnline is false
 * Indicates product is display only, contact seller directly
 */
@Composable
private fun NotForSaleBar() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shadowElevation = 8.dp,
        color = Color(0xFFFEF3C7)  // Light amber/warning color
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = null,
                tint = Color(0xFFB45309),  // Amber color
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Display Only - Contact Seller for Purchase",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = Color(0xFFB45309)
            )
        }
    }
}

/**
 * Contact Seller Bar for old products - shows Call and Chat buttons
 * Buttons are disabled and greyed out if the current user is the owner
 */
@Composable
private fun ContactSellerBar(
    isCreatingChat: Boolean = false,
    isOwner: Boolean = false,
    onCallClick: () -> Unit,
    onChatClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shadowElevation = 8.dp,
        color = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(16.dp)
        ) {
            // Show "This is your listing" message when owner
            if (isOwner) {
                Text(
                    text = "This is your listing",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Call Button - disabled for owner
                OutlinedButton(
                    onClick = onCallClick,
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp),
                    enabled = !isOwner,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, if (isOwner) Color.Gray else Primary)
                ) {
                    Icon(
                        imageVector = Icons.Default.Call,
                        contentDescription = null,
                        tint = if (isOwner) Color.Gray else Primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Call Seller",
                        color = if (isOwner) Color.Gray else Primary
                    )
                }
                
                // Chat Button - disabled for owner
                Button(
                    onClick = onChatClick,
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isOwner) Color.Gray else Primary,
                        disabledContainerColor = Color.Gray
                    ),
                    enabled = !isCreatingChat && !isOwner
                ) {
                    if (isCreatingChat) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Chat,
                            contentDescription = null,
                            tint = Color.White
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Chat", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
