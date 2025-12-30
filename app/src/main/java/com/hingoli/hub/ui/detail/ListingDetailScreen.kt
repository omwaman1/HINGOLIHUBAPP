package com.hingoli.hub.ui.detail

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.hingoli.hub.data.model.*
import com.hingoli.hub.ui.components.*
import com.hingoli.hub.ui.detail.components.*
import com.hingoli.hub.ui.theme.*
import android.util.Log
import com.hingoli.hub.data.settings.AppLanguage
import com.hingoli.hub.data.settings.SettingsManager

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ListingDetailScreen(
    listingId: Long,
    onBackClick: () -> Unit,
    onChatClick: (conversationId: String, listingTitle: String) -> Unit = { _, _ -> },
    onProfileIncomplete: (actionType: String) -> Unit = {},
    onProductClick: (productId: Long) -> Unit = {},
    viewModel: ListingDetailViewModel = hiltViewModel(),
    settingsManager: SettingsManager? = null
) {
    val uiState by viewModel.uiState.collectAsState()
    
    // Call timing dialog state - managed at screen level
    var showCallTimingDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val selectedLanguage by settingsManager?.languageFlow?.collectAsState(initial = AppLanguage.MARATHI) 
        ?: remember { mutableStateOf(AppLanguage.MARATHI) }
    val isMarathi = selectedLanguage == AppLanguage.MARATHI
    
    // Image picker for adding gallery photos
    val imagePickerLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        uri?.let { viewModel.addGalleryImage(context, it) }
    }
    
    // Load listing when screen opens
    LaunchedEffect(listingId) {
        viewModel.loadListing(listingId)
    }
    
    // Refresh profile status when returning to this screen
    LaunchedEffect(Unit) {
        viewModel.refreshProfileStatus()
    }
    
    when {
        uiState.isLoading -> {
            ShimmerDetailScreen()
        }
        uiState.error != null && uiState.listing == null -> {
            ErrorView(
                message = uiState.error!!,
                onRetry = { viewModel.retry() }
            )
        }
        uiState.listing != null -> {
            val listing = uiState.listing!!
            
            // Call Timing Dialog - shown when user tries to call outside allowed hours
    if (showCallTimingDialog) {
        CallTimingDialog(
            message = viewModel.getCallTimingMessage(isMarathi = isMarathi),
            onDismiss = { showCallTimingDialog = false }
        )
    }
    
    Scaffold(
                bottomBar = {
                    // Bottom Call and Chat Bar - Only show if NOT a job
                    if (listing.listingType != "jobs") {
                        BottomActionBar(
                            listing = listing,
                            isOwnListing = uiState.isOwnListing,
                            isCreatingChat = uiState.isCreatingChat,
                            isServiceListing = listing.listingType == "services",
                            isMarathi = isMarathi,
                            onCallClick = {
                                if (!uiState.isOwnListing) {
                                    // Require profile completion before calling
                                    if (!uiState.isProfileComplete) {
                                        onProfileIncomplete("call")
                                        return@BottomActionBar
                                    }
                                    // Check call timing restriction
                                    if (!viewModel.isCallAllowed()) {
                                        showCallTimingDialog = true
                                        return@BottomActionBar
                                    }
                                    viewModel.logEnquiry("call")
                                    // Launch voice call
                                    val targetUserId = listing.user?.userId ?: 0
                                    val userIds = listOf(uiState.currentUserId, targetUserId).sorted()
                                    val callConversationId = "call_${userIds[0]}_${userIds[1]}"
                                    val callId = "call_${uiState.currentUserId}_${targetUserId}_${System.currentTimeMillis()}"
                                    com.hingoli.hub.ui.call.VoiceCallActivity.start(
                                        context = context,
                                        callId = callId,
                                        userId = uiState.currentUserId.toString(),
                                        userName = uiState.currentUserName.ifEmpty { "User" },
                                        conversationId = callConversationId,
                                        targetUserId = targetUserId
                                    )
                                }
                            },
                            onChatClick = {
                                if (!uiState.isOwnListing) {
                                    // Require profile completion before chatting
                                    if (!uiState.isProfileComplete) {
                                        onProfileIncomplete("chat")
                                        return@BottomActionBar
                                    }
                                    viewModel.logEnquiry("chat")
                                    viewModel.startChat { conversationId, listingTitle ->
                                        onChatClick(conversationId, listingTitle)
                                    }
                                }
                            }
                        )
                    }
                }
            ) { paddingValues ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .verticalScroll(scrollState)
                        .background(MaterialTheme.colorScheme.background) // Theme background
                ) {
                    // Main Image with Back Button
                    // Always show main image first, then gallery images
                    val allImages = mutableListOf<ListingImage>()
                    if (!listing.mainImageUrl.isNullOrBlank()) {
                        allImages.add(ListingImage(0, listing.mainImageUrl, null, 0))
                    }
                    listing.images?.let { galleryImages ->
                        galleryImages.forEach { img ->
                            // Avoid duplicating if main image is already in gallery
                            if (img.imageUrl != listing.mainImageUrl) {
                                allImages.add(img)
                            }
                        }
                    }
                    MainImageSection(
                        images = if (allImages.isNotEmpty()) allImages else emptyList(),
                        onBackClick = onBackClick,
                        listing = listing
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Title Section with verified badge and favorite
                    TitleSection(listing = listing)
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Stats Section
                    StatsSection(listing = listing)
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Address Section
                    AddressSection(listing = listing)
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Experience Section (for services)
                    ExperienceSection(listing = listing)

                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Content Section
                    if (listing.listingType == "jobs") {
                        // For jobs, show job details section followed by about content
                        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                            // Job Details Section
                            JobDetailsSection(listing = listing)
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            // About/Description Section
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    AboutTab(listing = listing)
                                }
                            }
                        }
                    } else {
                        // For other types, show tabs
                        TabSection(
                            selectedTab = uiState.selectedTab,
                            onTabChange = viewModel::selectTab,
                            listing = listing,
                            priceList = uiState.priceList,
                            reviews = uiState.reviews,
                            shopProducts = uiState.shopProducts,
                            isLoadingPriceList = uiState.isLoadingPriceList,
                            isLoadingReviews = uiState.isLoadingReviews,
                            isLoadingShopProducts = uiState.isLoadingShopProducts,
                            isOwnListing = uiState.isOwnListing,
                            isLoggedIn = uiState.isLoggedIn,
                            isMarathi = isMarathi,
                            onAddPriceItem = { name, price, desc -> viewModel.addPriceListItem(name, price, desc) },
                            onDeletePriceItem = { viewModel.deletePriceListItem(it) },
                            onDeleteImage = { viewModel.deleteGalleryImage(it) },
                            onAddImage = {
                                imagePickerLauncher.launch(
                                    androidx.activity.result.PickVisualMediaRequest(
                                        androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia.ImageOnly
                                    )
                                )
                            },
                            onAddReview = { rating, title, content -> viewModel.addReview(rating, title, content) },
                            onUpdateDescription = { viewModel.updateDescription(it) },
                            onBuyNowClick = { product ->
                                // Navigate to product detail screen
                                onProductClick(product.productId)
                            },
                            onAddProduct = { name, description, price, imageUri, condition, sellOnline, categoryId, subcategoryId ->
                                viewModel.addBusinessProduct(context, name, description, price, imageUri, condition, sellOnline, categoryId, subcategoryId)
                            },
                            productCategories = uiState.productCategories,
                            productSubcategories = uiState.productSubcategories,
                            onCategorySelected = { viewModel.loadProductSubcategories(it) },
                            onConditionChange = { condition -> viewModel.loadProductCategoriesForCondition(condition) },
                            onDeleteProduct = { viewModel.deleteBusinessProduct(it) }
                        )
                    }
                    
                    // Bottom Banners (listing_detail_bottom placement)
                    if (uiState.bottomBanners.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(16.dp))
                        BannerCarousel(
                            banners = uiState.bottomBanners,
                            onBannerClick = { /* No action */ }
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun MainImageSection(
    images: List<ListingImage>,
    onBackClick: () -> Unit,
    listing: Listing? = null
) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp
    val pagerState = rememberPagerState(pageCount = { images.size })
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(screenHeight * 0.30f) // 30% of screen height
    ) {
        if (images.isNotEmpty()) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                AsyncImage(
                    model = images[page].imageUrl,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Gray.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Image,
                    contentDescription = null,
                    tint = Color.Gray,
                    modifier = Modifier.size(64.dp)
                )
            }
        }
        
        // Top action buttons overlay
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .align(Alignment.TopStart),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Back button
            IconButton(
                onClick = onBackClick,
                modifier = Modifier
                    .size(40.dp)
                    .background(Color.Black.copy(alpha = 0.5f), CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            // Share button
            if (listing != null) {
                IconButton(
                    onClick = {
                        val shareText = buildString {
                            append("Check out \"${listing.title}\" on HINGOLI HUB!\n\n")
                            listing.price?.let { append("💰 Price: ₹${String.format("%,.0f", it)}\n") }
                            listing.location?.let { append("📍 Location: $it\n") }
                            ?: listing.city?.let { append("📍 Location: $it\n") }
                            append("\n📲 Download HINGOLI HUB to view more:\n")
                            append("https://play.google.com/store/apps/details?id=com.hingoli.hub")
                        }
                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, shareText)
                            putExtra(Intent.EXTRA_SUBJECT, "Check out ${listing.title} on HINGOLI HUB")
                        }
                        context.startActivity(Intent.createChooser(shareIntent, "Share via"))
                    },
                    modifier = Modifier
                        .size(40.dp)
                        .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Share",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
        
        // Page indicators
        if (images.size > 1) {
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                repeat(images.size) { index ->
                    Box(
                        modifier = Modifier
                            .size(if (pagerState.currentPage == index) 10.dp else 8.dp)
                            .clip(CircleShape)
                            .background(
                                if (pagerState.currentPage == index) Color.White
                                else Color.White.copy(alpha = 0.5f)
                            )
                    )
                }
            }
        }
    }
}

@Composable
private fun TitleSection(listing: Listing) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Title
        Text(
            text = listing.title,
            style = MaterialTheme.typography.headlineSmall,
            color = Color(0xFF6B5CE7), // Purple/blue color like the image
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f)
        )
        
        // Verified badge
        if (listing.isVerified) {
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                imageVector = Icons.Filled.Verified,
                contentDescription = "Verified",
                tint = Color(0xFF6B5CE7),
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
private fun StatsSection(listing: Listing) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Rating
        Icon(
            imageVector = Icons.Filled.Star,
            contentDescription = null,
            tint = Color(0xFFFFB400), // Amber/Gold
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = "${String.format("%.1f", listing.avgRating)} (${listing.reviewCount} reviews)",
            style = MaterialTheme.typography.bodyMedium,
            color = OnSurface,
            fontWeight = FontWeight.Medium
        )
        
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = "•",
            style = MaterialTheme.typography.bodyMedium,
            color = OnSurfaceVariant
        )
        Spacer(modifier = Modifier.width(4.dp))
        
        // Views
        Icon(
            imageVector = Icons.Outlined.Visibility,
            contentDescription = null,
            tint = OnSurfaceVariant,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = "${listing.viewCount} views",
            style = MaterialTheme.typography.bodyMedium,
            color = OnSurfaceVariant
        )
    }
}

@Composable
private fun DescriptionSection(listing: Listing) {
    if (!listing.description.isNullOrBlank()) {
        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            Text(
                text = "Description",
                style = MaterialTheme.typography.titleSmall,
                color = OnSurface,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = listing.description,
                style = MaterialTheme.typography.bodyMedium,
                color = OnSurfaceVariant,
                lineHeight = MaterialTheme.typography.bodyMedium.lineHeight * 1.5
            )
        }
    }
}

@Composable
private fun AddressSection(listing: Listing) {
    val context = LocalContext.current
    val address = listOfNotNull(
        listing.location,
        listing.city,
        listing.state
    ).joinToString(", ")
    
    if (address.isNotEmpty()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Outlined.LocationOn,
                contentDescription = null,
                tint = Color.Gray,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = address,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun ExperienceSection(listing: Listing) {
    // Only show for services listings
    if (listing.listingType != "services") return
    
    val serviceDetails = listing.serviceDetails
    val priceMin = serviceDetails?.priceMin ?: listing.priceMin
    val priceMax = serviceDetails?.priceMax ?: listing.priceMax
    val experienceYears = serviceDetails?.experienceYears ?: listing.experienceYears
    
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        // Price Range
        if (priceMin != null || priceMax != null) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Outlined.CurrencyRupee,
                    contentDescription = null,
                    tint = Color(0xFF4CAF50),
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = when {
                        priceMin != null && priceMax != null -> 
                            "?${String.format("%,.0f", priceMin)} - ?${String.format("%,.0f", priceMax)}"
                        priceMin != null -> "From ?${String.format("%,.0f", priceMin)}"
                        priceMax != null -> "Up to ?${String.format("%,.0f", priceMax)}"
                        else -> ""
                    },
                    style = MaterialTheme.typography.titleMedium,
                    color = Color(0xFF4CAF50),
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
        }
        
        // Experience Years
        experienceYears?.let { years ->
            Text(
                text = "$years Years Experience",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )
        }
    }
}

@Composable
private fun JobDetailsSection(listing: Listing) {
    val jobDetails = listing.jobDetails ?: return
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Job Details",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = OnSurface
            )
            
            // Salary
            val salaryMin = jobDetails.salaryMin
            val salaryMax = jobDetails.salaryMax
            val salaryPeriod = jobDetails.salaryPeriod?.replaceFirstChar { it.uppercase() } ?: ""
            
            if (salaryMin != null || salaryMax != null) {
                JobDetailRow(
                    icon = Icons.Outlined.CurrencyRupee,
                    label = "Salary",
                    value = when {
                        salaryMin != null && salaryMax != null -> 
                            "?${String.format("%,.0f", salaryMin)} - ?${String.format("%,.0f", salaryMax)} / $salaryPeriod"
                        salaryMin != null -> "?${String.format("%,.0f", salaryMin)} / $salaryPeriod"
                        salaryMax != null -> "Up to ?${String.format("%,.0f", salaryMax)} / $salaryPeriod"
                        else -> ""
                    }
                )
            }
            
            // Employment Type
            jobDetails.employmentType?.let { type ->
                val displayType = type
                    .replace("_", " ")
                    .split(" ")
                    .joinToString(" ") { it.replaceFirstChar { c -> c.uppercase() } }
                JobDetailRow(
                    icon = Icons.Outlined.Work,
                    label = "Employment Type",
                    value = displayType
                )
            }
            
            // Experience Required
            jobDetails.experienceRequiredYears?.let { years ->
                JobDetailRow(
                    icon = Icons.Outlined.History,
                    label = "Experience",
                    value = if (years == 0) "Fresher / No experience" else "$years+ years"
                )
            }
            
            // Education Required
            jobDetails.educationRequired?.let { education ->
                JobDetailRow(
                    icon = Icons.Outlined.School,
                    label = "Education",
                    value = education
                )
            }
            
            // Work Location Type (Remote/On-site/Hybrid)
            val locationType = jobDetails.workLocationType ?: if (jobDetails.isRemote == true) "remote" else null
            locationType?.let { type ->
                val displayType = type
                    .replace("_", " ")
                    .split(" ")
                    .joinToString(" ") { it.replaceFirstChar { c -> c.uppercase() } }
                JobDetailRow(
                    icon = if (type == "remote") Icons.Outlined.Home else Icons.Outlined.LocationOn,
                    label = "Work Location",
                    value = displayType
                )
            }
            
            // Vacancies
            jobDetails.vacancies?.let { count ->
                JobDetailRow(
                    icon = Icons.Outlined.People,
                    label = "Vacancies",
                    value = "$count position${if (count > 1) "s" else ""}"
                )
            }
            
            // Application Deadline
            jobDetails.applicationDeadline?.let { deadline ->
                JobDetailRow(
                    icon = Icons.Outlined.Event,
                    label = "Apply Before",
                    value = deadline.take(10) // Show only date part
                )
            }
            
            // Skills Required
            jobDetails.skillsRequired?.let { skills ->
                if (skills.isNotBlank()) {
                    JobDetailRow(
                        icon = Icons.Outlined.Star,
                        label = "Skills",
                        value = skills
                    )
                }
            }
        }
    }
}

@Composable
private fun JobDetailRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = PrimaryBlue,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = OnSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                color = OnSurface,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun BottomActionBar(
    listing: Listing,
    isOwnListing: Boolean = false,
    isCreatingChat: Boolean = false,
    isServiceListing: Boolean = false,
    isMarathi: Boolean = false,
    onCallClick: () -> Unit,
    onChatClick: () -> Unit
) {
    val disabledColor = Color.Gray.copy(alpha = 0.5f)
    val callButtonColor = if (isOwnListing) disabledColor else PrimaryBlue
    val chatButtonColor = if (isOwnListing) disabledColor else PrimaryBlue
    
    // Dynamic button text based on listing type - use Strings helper for localization
    val callButtonText = when {
        isOwnListing -> Strings.yourListing(isMarathi)
        isServiceListing -> Strings.bookNow(isMarathi)
        else -> Strings.callNow(isMarathi)
    }
    
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Call/Book button
            Button(
                onClick = onCallClick,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                shape = RoundedCornerShape(24.dp),
                enabled = !isOwnListing,
                colors = ButtonDefaults.buttonColors(
                    containerColor = callButtonColor,
                    disabledContainerColor = disabledColor
                )
            ) {
                Icon(
                    imageVector = Icons.Filled.Call,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = callButtonText,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold
                )
            }
            
            // Chat button
            OutlinedButton(
                onClick = onChatClick,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                shape = RoundedCornerShape(24.dp),
                enabled = !isOwnListing && !isCreatingChat,
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = chatButtonColor,
                    disabledContentColor = disabledColor
                ),
                border = BorderStroke(1.5.dp, chatButtonColor)
            ) {
                if (isCreatingChat) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = PrimaryBlue
                    )
                } else {
                    Icon(
                        imageVector = Icons.Filled.Chat,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isOwnListing) Strings.yourListing(isMarathi) else Strings.chatNow(isMarathi),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}


@Composable
private fun TabSection(
    selectedTab: Int,
    onTabChange: (Int) -> Unit,
    listing: Listing,
    priceList: List<PriceListItem>,
    reviews: List<Review>,
    shopProducts: List<ShopProduct> = emptyList(),
    isLoadingPriceList: Boolean,
    isLoadingReviews: Boolean,
    isLoadingShopProducts: Boolean = false,
    isOwnListing: Boolean = false,
    isLoggedIn: Boolean = false,
    isMarathi: Boolean = false,
    onAddPriceItem: (name: String, price: Double, description: String?) -> Unit = { _, _, _ -> },
    onDeletePriceItem: (Long) -> Unit = {},
    onDeleteImage: (Long) -> Unit = {},
    onAddImage: () -> Unit = {},
    onAddReview: (rating: Int, title: String?, content: String?) -> Unit = { _, _, _ -> },
    onUpdateDescription: (String) -> Unit = {},
    onBuyNowClick: (ShopProduct) -> Unit = {},
    onAddProduct: (name: String, description: String?, price: Double, imageUri: android.net.Uri?, condition: String, sellOnline: Boolean, categoryId: Int?, subcategoryId: Int?) -> Unit = { _, _, _, _, _, _, _, _ -> },
    onDeleteProduct: (Long) -> Unit = {},
    productCategories: List<com.hingoli.hub.data.model.Category> = emptyList(),
    productSubcategories: List<com.hingoli.hub.data.model.Category> = emptyList(),
    onCategorySelected: (Int) -> Unit = {},
    onConditionChange: (String) -> Unit = {} // Callback when condition changes in AddProductDialog
) {
    // Show Shop/Services tab for business and services listings
    val isBusiness = listing.listingType == "business"
    val isService = listing.listingType == "services"
    val hasProductsTab = isBusiness || isService
    val tabs = when {
        isBusiness -> listOf("Shop", "About", "Photos", "Reviews")
        isService -> listOf("Services", "About", "Photos", "Reviews")
        else -> listOf("About", "Photos", "Reviews")
    }
    var showAddPriceDialog by remember { mutableStateOf(false) }
    var showEditDescriptionDialog by remember { mutableStateOf(false) }
    var showAddReviewDialog by remember { mutableStateOf(false) }
    var showAddProductDialog by remember { mutableStateOf(false) }
    
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        // Tab Row
        ScrollableTabRow(
            selectedTabIndex = selectedTab,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = PrimaryBlue,
            edgePadding = 16.dp,
            divider = { HorizontalDivider(color = Color.LightGray, thickness = 1.dp) }
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { onTabChange(index) },
                    text = {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = if (selectedTab == index) FontWeight.SemiBold else FontWeight.Normal,
                            color = if (selectedTab == index) PrimaryBlue else Color.Gray
                        )
                    }
                )
            }
        }
        
        // Tab content
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                when {
                    // Shop/Services tab (for business and services at index 0)
                    hasProductsTab && selectedTab == 0 -> ShopTab(
                        products = shopProducts,
                        isLoading = isLoadingShopProducts,
                        isOwnListing = isOwnListing,
                        onBuyNowClick = onBuyNowClick,
                        onAddProductClick = { showAddProductDialog = true },
                        onDeleteProductClick = onDeleteProduct,
                        isServiceListing = isService
                    )
                    // About tab (index 1 for business/services, 0 for others)
                    selectedTab == (if (hasProductsTab) 1 else 0) -> AboutTab(
                        listing = listing,
                        isOwnListing = isOwnListing,
                        onEditClick = { showEditDescriptionDialog = true }
                    )
                    // Photos tab (index 2 for business/services, 1 for others)
                    selectedTab == (if (hasProductsTab) 2 else 1) -> PhotosTab(
                        listing = listing,
                        isOwnListing = isOwnListing,
                        onDeleteImage = onDeleteImage,
                        onAddImage = onAddImage
                    )
                    // Reviews tab (index 3 for business/services, 2 for others)
                    selectedTab == (if (hasProductsTab) 3 else 2) -> ReviewsTab(
                        reviews = reviews,
                        isLoading = isLoadingReviews,
                        canWriteReview = isLoggedIn && !isOwnListing,
                        onWriteReviewClick = { showAddReviewDialog = true }
                    )
                }
            }
        }
    }
    
    // Add Price Item Dialog
    if (showAddPriceDialog) {
        AddPriceItemDialog(
            onDismiss = { showAddPriceDialog = false },
            onConfirm = { name, price, desc ->
                onAddPriceItem(name, price, desc)
                showAddPriceDialog = false
            }
        )
    }
    
    // Edit Description Dialog
    if (showEditDescriptionDialog) {
        EditDescriptionDialog(
            currentDescription = listing.description ?: "",
            onDismiss = { showEditDescriptionDialog = false },
            onConfirm = { newDesc ->
                onUpdateDescription(newDesc)
                showEditDescriptionDialog = false
            }
        )
    }
    
    // Add Review Dialog
    if (showAddReviewDialog) {
        AddReviewDialog(
            onDismiss = { showAddReviewDialog = false },
            onConfirm = { rating, title, content ->
                onAddReview(rating, title, content)
                showAddReviewDialog = false
            }
        )
    }
    
    // Add Product/Service Dialog (for business/service owners)
    
if (showAddProductDialog) {
        AddProductDialog(
            onDismiss = { showAddProductDialog = false },
            onConfirm = { name, description, price, imageUri, condition, sellOnline, categoryId, subcategoryId ->
                onAddProduct(name, description, price, imageUri, condition, sellOnline, categoryId, subcategoryId)
                showAddProductDialog = false
            },
            categories = productCategories,
            subcategories = productSubcategories,
            onCategorySelected = onCategorySelected,
            onConditionChange = onConditionChange,
            isServiceListing = isService,
            isMarathi = isMarathi
        )
    }
}

@Composable
private fun ReviewsTab(
    reviews: List<Review>,
    isLoading: Boolean,
    canWriteReview: Boolean = false,
    onWriteReviewClick: () -> Unit = {}
) {
    Column {
        // Write Review button for logged-in non-owners
        if (canWriteReview) {
            Button(
                onClick = onWriteReviewClick,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
            ) {
                Icon(Icons.Default.RateReview, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Write a Review")
            }
            Spacer(modifier = Modifier.height(12.dp))
        }
        
        when {
            isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = PrimaryBlue)
                }
            }
            reviews.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Outlined.RateReview,
                            contentDescription = null,
                            tint = OnSurfaceVariant,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (canWriteReview) "No reviews yet. Be the first to review!" else "No reviews yet",
                            style = MaterialTheme.typography.bodyMedium,
                            color = OnSurfaceVariant
                        )
                    }
                }
            }
            else -> {
                reviews.forEach { review ->
                    ReviewCard(review = review)
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }
    }
}

@Composable
private fun ReviewCard(review: Review) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceVariant)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(PrimaryBlue),
                    contentAlignment = Alignment.Center
                ) {
                    if (review.reviewer?.avatarUrl != null) {
                        AsyncImage(
                            model = review.reviewer.avatarUrl,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Text(
                            text = review.reviewer?.username?.take(1)?.uppercase() ?: "U",
                            style = MaterialTheme.typography.titleSmall,
                            color = Color.White
                        )
                    }
                }
                
                Spacer(modifier = Modifier.width(8.dp))
                
                Column {
                    Text(
                        text = review.reviewer?.username ?: "Anonymous",
                        style = MaterialTheme.typography.titleSmall,
                        color = OnSurface,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "${review.rating} ?",
                        style = MaterialTheme.typography.bodySmall,
                        color = AccentGreen
                    )
                }
                
                Spacer(modifier = Modifier.weight(1f))
                
                review.createdAt?.let {
                    Text(
                        text = it.take(10),
                        style = MaterialTheme.typography.bodySmall,
                        color = OnSurfaceVariant
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            review.title?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.titleSmall,
                    color = OnSurface,
                    fontWeight = FontWeight.SemiBold
                )
            }
            
            review.content?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyMedium,
                    color = OnSurfaceVariant
                )
            }
            
            if (review.helpfulCount > 0) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${review.helpfulCount} found helpful",
                    style = MaterialTheme.typography.labelSmall,
                    color = OnSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun ShopTab(
    products: List<ShopProduct>,
    isLoading: Boolean,
    isOwnListing: Boolean = false,
    onBuyNowClick: (ShopProduct) -> Unit = {},
    onAddProductClick: () -> Unit = {},
    onDeleteProductClick: (Long) -> Unit = {},
    isServiceListing: Boolean = false
) {
    // Use service-appropriate labels
    val itemLabel = if (isServiceListing) "Service" else "Product"
    val itemLabelPlural = if (isServiceListing) "services" else "products"
    val emptyEmoji = if (isServiceListing) "??" else "??"
    
    // Snackbar for non-sellOnline products
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    
    Box(modifier = Modifier.fillMaxWidth()) {
        Column {
        // Add Product/Service button for owners
        if (isOwnListing) {
            OutlinedButton(
                onClick = onAddProductClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Primary)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Add $itemLabel")
            }
        }
        
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxWidth().padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Primary)
            }
        } else if (products.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxWidth().padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(emptyEmoji, fontSize = 48.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (isOwnListing) "Add your first $itemLabel!" else "No $itemLabelPlural available",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                }
            }
        } else {
            // Product grid - 3 columns
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.heightIn(max = 600.dp)
            ) {
                items(products) { product ->
                    ShopProductGridCard(
                        product = product,
                        onClick = {
                            // Only navigate if product is available for online sale
                            if (product.sellOnline) {
                                onBuyNowClick(product)
                            } else {
                                // Show snackbar for showcase-only products
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar(
                                        message = "Not available for online sale",
                                        duration = SnackbarDuration.Short
                                    )
                                }
                            }
                        },
                        isOwner = isOwnListing,
                        onDeleteClick = { onDeleteProductClick(product.productId) }
                    )
                }
            }
        }
        }
        // Snackbar at bottom of the box
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}
@Composable
private fun ShopProductGridCard(
    product: ShopProduct,
    onClick: () -> Unit,
    isOwner: Boolean = false,
    onDeleteClick: () -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(4.dp), // Rectangle shape
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0)) // Subtle border
    ) {
        Column {
            // Product Image with delete button overlay for owner
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp)) // Rectangle shape
                    .background(Color.LightGray.copy(alpha = 0.3f))
            ) {
                AsyncImage(
                    model = product.imageUrl,
                    contentDescription = product.productName,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                
                // Delete button for owner
                if (isOwner) {
                    IconButton(
                        onClick = onDeleteClick,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .size(24.dp)
                            .background(Color.Red.copy(alpha = 0.8f), CircleShape)
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Delete",
                            tint = Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
            
            // Product Info - compact for 3-column grid
            Column(modifier = Modifier.padding(6.dp)) {
                Text(
                    text = product.productName,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Spacer(modifier = Modifier.height(2.dp))
                
                Text(
                    text = "?${String.format("%,.0f", product.price)}",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = Primary
                )
            }
        }
    }
}

@Composable
private fun PriceListTab(
    items: List<PriceListItem>,
    isLoading: Boolean,
    isOwnListing: Boolean = false,
    onAddItem: () -> Unit = {},
    onDeleteItem: (Long) -> Unit = {}
) {
    Column {
        // Add button for owner
        if (isOwnListing) {
            Button(
                onClick = onAddItem,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Add Price Item")
            }
            Spacer(modifier = Modifier.height(12.dp))
        }
        
        when {
            isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = PrimaryBlue)
                }
            }
            items.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Outlined.Receipt,
                            contentDescription = null,
                            tint = OnSurfaceVariant,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (isOwnListing) "Add your first price item" else "No price list available",
                            style = MaterialTheme.typography.bodyMedium,
                            color = OnSurfaceVariant
                        )
                    }
                }
            }
            else -> {
                items.forEach { item ->
                    PriceListItemCard(
                        item = item,
                        isOwnListing = isOwnListing,
                        onDelete = { onDeleteItem(item.itemId) }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
private fun PriceListItemCard(
    item: PriceListItem,
    isOwnListing: Boolean = false,
    onDelete: () -> Unit = {}
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.itemName,
                    style = MaterialTheme.typography.titleSmall,
                    color = OnSurface,
                    fontWeight = FontWeight.Medium
                )
                item.itemDescription?.let { desc ->
                    Text(
                        text = desc,
                        style = MaterialTheme.typography.bodySmall,
                        color = OnSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            
            Text(
                text = "?${String.format("%,.0f", item.price)}",
                style = MaterialTheme.typography.titleMedium,
                color = OnSurface,
                fontWeight = FontWeight.Bold
            )
            
            if (isOwnListing) {
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = Color.Red.copy(alpha = 0.7f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun AboutTab(
    listing: Listing,
    isOwnListing: Boolean = false,
    onEditClick: () -> Unit = {}
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Edit button for owner
        if (isOwnListing) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onEditClick) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit",
                        tint = PrimaryBlue,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Edit Description", color = PrimaryBlue)
                }
            }
        }
        
        // Business Description
        if (!listing.description.isNullOrBlank()) {
            Text(
                text = "About",
                style = MaterialTheme.typography.titleMedium,
                color = OnSurface,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = listing.description,
                style = MaterialTheme.typography.bodyMedium,
                color = OnSurfaceVariant,
                lineHeight = MaterialTheme.typography.bodyMedium.lineHeight * 1.5
            )
            HorizontalDivider(color = Color.LightGray, thickness = 0.5.dp)
        } else if (isOwnListing) {
            Text(
                text = "No description yet. Tap 'Edit Description' to add one.",
                style = MaterialTheme.typography.bodyMedium,
                color = OnSurfaceVariant
            )
        }
        
        // Availability (for services)
        listing.serviceDetails?.availability?.let { availability ->
            Row(verticalAlignment = Alignment.Top) {
                Icon(
                    imageVector = Icons.Outlined.Schedule,
                    contentDescription = null,
                    tint = PrimaryBlue,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Availability",
                        style = MaterialTheme.typography.titleSmall,
                        color = OnSurface,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = availability,
                        style = MaterialTheme.typography.bodyMedium,
                        color = OnSurfaceVariant
                    )
                }
            }
        }
        
        // Experience (for services)
        listing.serviceDetails?.experienceYears?.let { years ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Outlined.WorkHistory,
                    contentDescription = null,
                    tint = PrimaryBlue,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Experience",
                        style = MaterialTheme.typography.titleSmall,
                        color = OnSurface,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "$years+ Years",
                        style = MaterialTheme.typography.bodyMedium,
                        color = OnSurfaceVariant
                    )
                }
            }
        }
        
        // ========== BUSINESS DETAILS SECTION ==========
        
        // Business Name
        listing.businessDetails?.businessName?.let { name ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Outlined.Store,
                    contentDescription = null,
                    tint = PrimaryBlue,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Business Name",
                        style = MaterialTheme.typography.titleSmall,
                        color = OnSurface,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = name,
                        style = MaterialTheme.typography.bodyMedium,
                        color = OnSurfaceVariant
                    )
                }
            }
        }
        
        // Established Year
        listing.businessDetails?.establishedYear?.let { year ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Outlined.CalendarMonth,
                    contentDescription = null,
                    tint = PrimaryBlue,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Established",
                        style = MaterialTheme.typography.titleSmall,
                        color = OnSurface,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "$year (${java.util.Calendar.getInstance().get(java.util.Calendar.YEAR) - year}+ years)",
                        style = MaterialTheme.typography.bodyMedium,
                        color = OnSurfaceVariant
                    )
                }
            }
        }
        
        // Website
        listing.businessDetails?.websiteUrl?.let { website ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Outlined.Language,
                    contentDescription = null,
                    tint = PrimaryBlue,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Website",
                        style = MaterialTheme.typography.titleSmall,
                        color = OnSurface,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = website,
                        style = MaterialTheme.typography.bodyMedium,
                        color = PrimaryBlue,
                        textDecoration = TextDecoration.Underline
                    )
                }
            }
        }
        
        // Business Email
        listing.businessDetails?.businessEmail?.let { email ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Outlined.Email,
                    contentDescription = null,
                    tint = PrimaryBlue,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Email",
                        style = MaterialTheme.typography.titleSmall,
                        color = OnSurface,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = email,
                        style = MaterialTheme.typography.bodyMedium,
                        color = OnSurfaceVariant
                    )
                }
            }
        }
        
        // Business Phone
        listing.businessDetails?.businessPhone?.let { phone ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Outlined.Phone,
                    contentDescription = null,
                    tint = PrimaryBlue,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Business Phone",
                        style = MaterialTheme.typography.titleSmall,
                        color = OnSurface,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = phone,
                        style = MaterialTheme.typography.bodyMedium,
                        color = OnSurfaceVariant
                    )
                }
            }
        }
        
        // ========== COMMON CONTACT & LOCATION ==========
        
        // Contact Information (User's phone - only for non-business and non-services listings)
        // Services should use Chat/Call buttons instead of showing phone directly
        if (listing.businessDetails == null && listing.listingType != "services") {
            listing.user?.phone?.let { phone ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.Phone,
                        contentDescription = null,
                        tint = PrimaryBlue,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Contact",
                            style = MaterialTheme.typography.titleSmall,
                            color = OnSurface,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = phone,
                            style = MaterialTheme.typography.bodyMedium,
                            color = OnSurfaceVariant
                        )
                    }
                }
            }
        }
        
        // Address
        val address = listOfNotNull(
            listing.location,
            listing.city,
            listing.state
        ).joinToString(", ")
        
        if (address.isNotEmpty()) {
            Row(verticalAlignment = Alignment.Top) {
                Icon(
                    imageVector = Icons.Outlined.LocationOn,
                    contentDescription = null,
                    tint = PrimaryBlue,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Location",
                        style = MaterialTheme.typography.titleSmall,
                        color = OnSurface,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = address,
                        style = MaterialTheme.typography.bodyMedium,
                        color = OnSurfaceVariant
                    )
                }
            }
        }
        
        // If no data available
        if (listing.description.isNullOrBlank() && 
            listing.serviceDetails?.availability == null && 
            listing.serviceDetails?.experienceYears == null &&
            listing.businessDetails?.businessType == null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Outlined.Info,
                        contentDescription = null,
                        tint = OnSurfaceVariant,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "No details available",
                        style = MaterialTheme.typography.bodyMedium,
                        color = OnSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun PhotosTab(
    listing: Listing,
    isOwnListing: Boolean = false,
    onDeleteImage: (Long) -> Unit = {},
    onAddImage: () -> Unit = {}
) {
    val images = listing.images ?: listOf(
        ListingImage(0, listing.mainImageUrl ?: "", null, 0)
    )
    
    Column {
        // Add Photo button for owner
        if (isOwnListing) {
            Button(
                onClick = onAddImage,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Add Photos")
            }
            Spacer(modifier = Modifier.height(12.dp))
        }
        
        if (images.isEmpty() || (images.size == 1 && images.first().imageUrl.isEmpty())) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Outlined.PhotoLibrary,
                        contentDescription = null,
                        tint = OnSurfaceVariant,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (isOwnListing) "No photos yet. Tap 'Add Photos' to upload." else "No photos available",
                        style = MaterialTheme.typography.bodyMedium,
                        color = OnSurfaceVariant
                    )
                }
            }
        } else {
            Text(
                text = "${images.size} Photos",
                style = MaterialTheme.typography.titleMedium,
                color = OnSurface,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(12.dp))
            
            // Photo grid
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(images) { image ->
                    Box(modifier = Modifier.size(120.dp)) {
                        Card(
                            modifier = Modifier.fillMaxSize(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            AsyncImage(
                                model = image.imageUrl,
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }
                        
                        // Delete button for owner (only for gallery images, not main)
                        if (isOwnListing && image.imageId > 0) {
                            IconButton(
                                onClick = { onDeleteImage(image.imageId) },
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .size(28.dp)
                                    .background(
                                        color = Color.Black.copy(alpha = 0.5f),
                                        shape = CircleShape
                                    )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Delete",
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun VideosTab(listing: Listing) {
    val videos = listing.videos ?: emptyList()
    val context = LocalContext.current
    
    if (videos.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Outlined.PlayCircle,
                    contentDescription = null,
                    tint = OnSurfaceVariant,
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "No videos available",
                    style = MaterialTheme.typography.bodyMedium,
                    color = OnSurfaceVariant
                )
            }
        }
    } else {
        Column {
            Text(
                text = "${videos.size} Videos",
                style = MaterialTheme.typography.titleMedium,
                color = OnSurface,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(12.dp))
            
            videos.forEach { video ->
                VideoCard(
                    video = video,
                    onClick = {
                        try {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(video.videoUrl))
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            // Handle error
                        }
                    }
                )
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

@Composable
private fun VideoCard(
    video: ListingVideo,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceVariant)
    ) {
        Row(
            modifier = Modifier.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Thumbnail with play icon overlay
            Box(
                modifier = Modifier
                    .size(width = 120.dp, height = 80.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.Black),
                contentAlignment = Alignment.Center
            ) {
                if (video.thumbnailUrl != null) {
                    AsyncImage(
                        model = video.thumbnailUrl,
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxSize()
                            .alpha(0.7f),
                        contentScale = ContentScale.Crop
                    )
                }
                
                Icon(
                    imageVector = Icons.Filled.PlayCircle,
                    contentDescription = "Play",
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = video.title ?: "Watch Video",
                    style = MaterialTheme.typography.titleSmall,
                    color = OnSurface,
                    fontWeight = FontWeight.Medium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                
                if (video.durationSeconds != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = formatDuration(video.durationSeconds),
                        style = MaterialTheme.typography.bodySmall,
                        color = OnSurfaceVariant
                    )
                }
            }
        }
    }
}

private fun formatDuration(seconds: Int): String {
    val minutes = seconds / 60
    val remainingSeconds = seconds % 60
    return String.format("%02d:%02d", minutes, remainingSeconds)
}

private fun String.capitalize(): String {
    return this.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
}

// ==================== OWNER DIALOGS ====================

@Composable
private fun AddPriceItemDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, price: Double, description: String?) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var priceText by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Price Item", fontWeight = FontWeight.SemiBold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Item Name *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = priceText,
                    onValueChange = { priceText = it.filter { c -> c.isDigit() || c == '.' } },
                    label = { Text("Price (?) *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description (optional)") },
                    maxLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val price = priceText.toDoubleOrNull() ?: 0.0
                    if (name.isNotBlank() && price > 0) {
                        onConfirm(name.trim(), price, description.takeIf { it.isNotBlank() })
                    }
                },
                enabled = name.isNotBlank() && (priceText.toDoubleOrNull() ?: 0.0) > 0
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun EditDescriptionDialog(
    currentDescription: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var description by remember { mutableStateOf(currentDescription) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Description", fontWeight = FontWeight.SemiBold) },
        text = {
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description") },
                minLines = 4,
                maxLines = 8,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            Button(onClick = { onConfirm(description.trim()) }) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun AddReviewDialog(
    onDismiss: () -> Unit,
    onConfirm: (rating: Int, title: String?, content: String?) -> Unit
) {
    var rating by remember { mutableStateOf(5) }
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Write a Review", fontWeight = FontWeight.SemiBold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // Star Rating
                Text(
                    text = "Your Rating",
                    style = MaterialTheme.typography.bodyMedium,
                    color = OnSurface
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    (1..5).forEach { star ->
                        IconButton(
                            onClick = { rating = star },
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                imageVector = if (star <= rating) Icons.Filled.Star else Icons.Outlined.Star,
                                contentDescription = "$star stars",
                                tint = if (star <= rating) Color(0xFFFFC107) else OnSurfaceVariant,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
                }
                
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title (optional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                
                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    label = { Text("Your Review") },
                    minLines = 3,
                    maxLines = 5,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(
                        rating,
                        title.takeIf { it.isNotBlank() },
                        content.takeIf { it.isNotBlank() }
                    )
                },
                enabled = rating >= 1
            ) {
                Text("Submit Review")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddProductDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, description: String?, price: Double, imageUri: android.net.Uri?, condition: String, sellOnline: Boolean, categoryId: Int?, subcategoryId: Int?) -> Unit,
    categories: List<com.hingoli.hub.data.model.Category> = emptyList(),
    subcategories: List<com.hingoli.hub.data.model.Category> = emptyList(),
    onCategorySelected: (Int) -> Unit = {},
    isServiceListing: Boolean = false
) {
    val itemLabel = if (isServiceListing) "Service" else "Product"
    
    var productName by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var priceText by remember { mutableStateOf("") }
    var selectedImageUri by remember { mutableStateOf<android.net.Uri?>(null) }
    var condition by remember { mutableStateOf("new") }
    var sellOnline by remember { mutableStateOf(false) }
    var selectedCategoryId by remember { mutableStateOf<Int?>(null) }
    var selectedSubcategoryId by remember { mutableStateOf<Int?>(null) }
    var categoryDropdownExpanded by remember { mutableStateOf(false) }
    var subcategoryDropdownExpanded by remember { mutableStateOf(false) }
    
    val selectedCategoryName = categories.find { it.categoryId == selectedCategoryId }?.name ?: ""
    val selectedSubcategoryName = subcategories.find { it.categoryId == selectedSubcategoryId }?.name ?: ""
    
    val launcher = rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.GetContent()
    ) { uri: android.net.Uri? ->
        selectedImageUri = uri
    }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.clip(RoundedCornerShape(4.dp)), // Rectangle shape
        containerColor = Color.White,
        title = { 
            Text(
                text = "Add $itemLabel",
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1E293B)
            ) 
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                            colors = listOf(Color.White, Color(0xFFF8FAFC))
                        )
                    ),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                androidx.compose.foundation.lazy.LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.heightIn(max = 400.dp)
                ) {
                    item {
                        OutlinedTextField(
                            value = productName,
                            onValueChange = { productName = it },
                            label = { Text("$itemLabel Name *") },
                            singleLine = true,
                            shape = RoundedCornerShape(4.dp),
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PrimaryBlue,
                                unfocusedBorderColor = Color(0xFFE2E8F0),
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color.White
                            )
                        )
                    }
                    
                    item {
                        OutlinedTextField(
                            value = description,
                            onValueChange = { description = it },
                            label = { Text("Description") },
                            minLines = 2,
                            maxLines = 3,
                            shape = RoundedCornerShape(4.dp),
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PrimaryBlue,
                                unfocusedBorderColor = Color(0xFFE2E8F0),
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color.White
                            )
                        )
                    }
                    
                    item {
                        OutlinedTextField(
                            value = priceText,
                            onValueChange = { priceText = it.filter { c -> c.isDigit() || c == '.' } },
                            label = { Text("Price (?) *") },
                            singleLine = true,
                            shape = RoundedCornerShape(4.dp),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PrimaryBlue,
                                unfocusedBorderColor = Color(0xFFE2E8F0),
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color.White
                            )
                        )
                    }
                    
                    // Category dropdown - using ExposedDropdownMenuBox for full field clickable
                    item {
                        ExposedDropdownMenuBox(
                            expanded = categoryDropdownExpanded,
                            onExpandedChange = { categoryDropdownExpanded = it }
                        ) {
                            OutlinedTextField(
                                value = selectedCategoryName.ifEmpty { "Select Category" },
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Category") },
                                shape = RoundedCornerShape(4.dp),
                                trailingIcon = { 
                                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryDropdownExpanded)
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = PrimaryBlue,
                                    unfocusedBorderColor = Color(0xFFE2E8F0),
                                    focusedContainerColor = Color.White,
                                    unfocusedContainerColor = Color.White
                                )
                            )
                            ExposedDropdownMenu(
                                expanded = categoryDropdownExpanded,
                                onDismissRequest = { categoryDropdownExpanded = false },
                                modifier = Modifier
                                    .background(Color.White)
                                    .heightIn(max = 250.dp)
                            ) {
                                categories.forEach { category ->
                                    DropdownMenuItem(
                                        text = { Text(category.name, color = Color.Black) },
                                        onClick = {
                                            selectedCategoryId = category.categoryId
                                            selectedSubcategoryId = null
                                            onCategorySelected(category.categoryId)
                                            categoryDropdownExpanded = false
                                        },
                                        modifier = Modifier.background(Color.White)
                                    )
                                }
                            }
                        }
                    }
                    
                    // Subcategory dropdown (only show if category selected and subcategories exist)
                    if (selectedCategoryId != null && subcategories.isNotEmpty()) {
                        item {
                            ExposedDropdownMenuBox(
                                expanded = subcategoryDropdownExpanded,
                                onExpandedChange = { subcategoryDropdownExpanded = it }
                            ) {
                                OutlinedTextField(
                                    value = selectedSubcategoryName.ifEmpty { "Select Subcategory" },
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("Subcategory") },
                                    shape = RoundedCornerShape(4.dp),
                                    trailingIcon = { 
                                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = subcategoryDropdownExpanded)
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .menuAnchor(),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = PrimaryBlue,
                                        unfocusedBorderColor = Color(0xFFE2E8F0),
                                        focusedContainerColor = Color.White,
                                        unfocusedContainerColor = Color.White
                                    )
                                )
                                ExposedDropdownMenu(
                                    expanded = subcategoryDropdownExpanded,
                                    onDismissRequest = { subcategoryDropdownExpanded = false },
                                    modifier = Modifier
                                        .background(Color.White)
                                        .heightIn(max = 250.dp)
                                ) {
                                    subcategories.forEach { subcategory ->
                                        DropdownMenuItem(
                                            text = { Text(subcategory.name, color = Color.Black) },
                                            onClick = {
                                                selectedSubcategoryId = subcategory.categoryId
                                                subcategoryDropdownExpanded = false
                                            },
                                            modifier = Modifier.background(Color.White)
                                        )
                                    }
                                }
                            }
                        }
                    }
                    
                    // Sell Online toggle - ONLY show for products, not services
                    if (!isServiceListing) {
                        item {
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(4.dp),
                                color = Color(0xFFF8FAFC),
                                border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text(
                                            text = "Sell Online",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Medium
                                        )
                                        Text(
                                            text = if (sellOnline) "Available for online purchase" else "Showcase only",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color.Gray
                                        )
                                    }
                                    Switch(
                                        checked = sellOnline,
                                        onCheckedChange = { sellOnline = it },
                                        colors = SwitchDefaults.colors(
                                            checkedThumbColor = Color.White,
                                            checkedTrackColor = PrimaryBlue
                                        )
                                    )
                                }
                            }
                        }
                    }
                    
                    // Image picker
                    item {
                        OutlinedButton(
                            onClick = { launcher.launch("image/*") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(4.dp),
                            border = BorderStroke(1.dp, if (selectedImageUri != null) AccentGreen else Color(0xFFE2E8F0))
                        ) {
                            Icon(
                                Icons.Default.PhotoCamera,
                                contentDescription = null,
                                tint = if (selectedImageUri != null) AccentGreen else Color.Gray
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (selectedImageUri != null) "Image Selected ?" else "Add Image",
                                color = if (selectedImageUri != null) AccentGreen else Color.Gray
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val price = priceText.toDoubleOrNull() ?: 0.0
                    if (productName.isNotBlank() && price > 0) {
                        onConfirm(
                            productName,
                            description.takeIf { it.isNotBlank() },
                            price,
                            selectedImageUri,
                            condition,
                            sellOnline,
                            selectedCategoryId,
                            selectedSubcategoryId
                        )
                    }
                },
                enabled = productName.isNotBlank() && (priceText.toDoubleOrNull() ?: 0.0) > 0,
                shape = RoundedCornerShape(4.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryBlue,
                    disabledContainerColor = Color(0xFFCBD5E1)
                )
            ) {
                Text("Add $itemLabel", fontWeight = FontWeight.SemiBold)
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(4.dp)
            ) {
                Text("Cancel", color = Color.Gray)
            }
        },
        shape = RoundedCornerShape(4.dp) // Rectangle dialog shape
    )
}

