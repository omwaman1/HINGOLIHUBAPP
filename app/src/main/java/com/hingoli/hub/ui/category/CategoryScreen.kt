package com.hingoli.hub.ui.category

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.hingoli.hub.data.model.Banner
import com.hingoli.hub.data.model.Category
import com.hingoli.hub.data.settings.AppLanguage
import com.hingoli.hub.data.settings.SettingsManager
import com.hingoli.hub.ui.components.*
import com.hingoli.hub.ui.city.CitySelectionBottomSheet
import com.hingoli.hub.ui.city.CitySelectionViewModel
import com.hingoli.hub.ui.theme.*

/**
 * Unified Category Screen for all listing types
 * Replaces: ServicesScreen, SellingScreen, JobsScreen, BusinessesScreen
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryScreen(
    listingType: String,
    onCategoryClick: (categoryId: Int, categoryName: String) -> Unit,
    onMenuClick: () -> Unit = {},
    onPostClick: () -> Unit = {},
    viewModel: CategoryViewModel = hiltViewModel(),
    cityViewModel: CitySelectionViewModel = hiltViewModel(),
    settingsManager: SettingsManager? = null
) {
    val uiState by viewModel.uiState.collectAsState()
    val cityUiState by cityViewModel.uiState.collectAsState()
    var showCityPicker by remember { mutableStateOf(false) }
    val selectedLanguage by settingsManager?.languageFlow?.collectAsState(initial = AppLanguage.MARATHI) ?: remember { mutableStateOf(AppLanguage.MARATHI) }
    val isMarathi = selectedLanguage == AppLanguage.MARATHI
    
    // Load categories for this specific listingType
    LaunchedEffect(listingType) {
        viewModel.loadCategories(listingType)
    }
    
    // City selection bottom sheet
    if (showCityPicker) {
        CitySelectionBottomSheet(
            onDismiss = { showCityPicker = false },
            onCitySelected = { /* Handled by ViewModel */ },
            isMarathi = isMarathi,
            viewModel = cityViewModel
        )
    }
    
    Scaffold(
        topBar = {
            // Dynamic title based on listing type
            val cityDisplayName = cityUiState.selectedCity?.getLocalizedName(isMarathi) ?: if (isMarathi) "हिंगोली" else "Hingoli"
            val screenTitle = when (listingType) {
                "services" -> if (isMarathi) "सेवा - $cityDisplayName" else "Services in $cityDisplayName"
                "business" -> if (isMarathi) "स्थानिक व्यवसाय" else "Local Businesses"
                "selling" -> if (isMarathi) "जुन्या वस्तू खरेदी विक्री" else "Buy Sell Old Things"
                "jobs" -> if (isMarathi) "नोकरी - $cityDisplayName" else "Jobs in $cityDisplayName"
                else -> if (isMarathi) "हिंगोली हब" else "HINGOLI HUB"
            }
            
            // Using shared TopAppBar component
            HingoliHubTopAppBar(
                title = screenTitle,
                onMenuClick = onMenuClick,
                onCityClick = { showCityPicker = true },
                cityName = cityDisplayName,
                isMarathi = isMarathi
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            when {
                uiState.isLoading -> ShimmerListScreen()
                uiState.error != null -> ErrorView(
                    message = uiState.error!!,
                    onRetry = { viewModel.loadCategories(listingType) }
                )
                else -> {
                    CategoryContent(
                        sections = uiState.categorySections,
                        isMarathi = isMarathi,
                        topBanners = uiState.topBanners,
                        bottomBanners = uiState.bottomBanners,
                        onSubcategoryClick = { subcategory ->
                            onCategoryClick(subcategory.categoryId, subcategory.getLocalizedName(isMarathi))
                        },
                        onBannerClick = { banner ->
                            // Handle banner click if needed
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun CategoryContent(
    sections: List<CategorySection>,
    isMarathi: Boolean,
    topBanners: List<Banner>,
    bottomBanners: List<Banner>,
    onSubcategoryClick: (Category) -> Unit,
    onBannerClick: (Banner) -> Unit
) {
    if (sections.isEmpty()) {
        EmptyView(message = "No categories found")
        return
    }
    
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 8.dp)
    ) {
        // Top banners - full width at the top
        if (topBanners.isNotEmpty()) {
            item(key = "top_banners") {
                BannerCarousel(
                    banners = topBanners,
                    onBannerClick = onBannerClick
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
        
        // Category sections with subcategories in cards
        items(sections, key = { it.category.categoryId }) { section ->
            CategorySectionCard(
                section = section,
                isMarathi = isMarathi,
                onSubcategoryClick = onSubcategoryClick
            )
        }
        
        // Bottom banners - full width at the bottom
        if (bottomBanners.isNotEmpty()) {
            item(key = "bottom_banners") {
                Spacer(modifier = Modifier.height(8.dp))
                BannerCarousel(
                    banners = bottomBanners,
                    onBannerClick = onBannerClick
                )
            }
        }
        
        // Bottom spacing
        item { Spacer(modifier = Modifier.height(80.dp)) }
    }
}

@Composable
private fun CategorySectionCard(
    section: CategorySection,
    isMarathi: Boolean,
    onSubcategoryClick: (Category) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Category header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Main category name
                Text(
                    text = section.category.getLocalizedName(isMarathi),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Spacer(modifier = Modifier.weight(1f))
                
                // Listing count badge
                if (section.category.listingCount > 0) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Primary.copy(alpha = 0.1f)
                    ) {
                        Text(
                            text = "${section.category.listingCount}",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = Primary
                        )
                    }
                }
            }
            
            // Subcategories grid (4 per row)
            if (section.subcategories.isNotEmpty()) {
                SubcategoryGrid(
                    subcategories = section.subcategories,
                    isMarathi = isMarathi,
                    onSubcategoryClick = onSubcategoryClick
                )
            }
        }
    }
}

@Composable
private fun SubcategoryGrid(
    subcategories: List<Category>,
    isMarathi: Boolean,
    onSubcategoryClick: (Category) -> Unit
) {
    val chunkedSubcategories = subcategories.chunked(4)
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
    ) {
        chunkedSubcategories.forEach { rowItems ->
            Row(modifier = Modifier.fillMaxWidth()) {
                rowItems.forEach { subcategory ->
                    SubcategoryCard(
                        category = subcategory,
                        isMarathi = isMarathi,
                        onClick = { onSubcategoryClick(subcategory) },
                        modifier = Modifier.weight(1f)
                    )
                }
                // Fill empty slots
                repeat(4 - rowItems.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun SubcategoryCard(
    category: Category,
    isMarathi: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .padding(4.dp)  // Reduced from 8dp
            .clickable { onClick() },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Image card - smaller
        Card(
            modifier = Modifier
                .size(64.dp),  // Fixed smaller size instead of aspectRatio
            shape = RoundedCornerShape(10.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                if (category.imageUrl != null || category.iconUrl != null) {
                    AsyncImage(
                        model = category.imageUrl ?: category.iconUrl,
                        contentDescription = category.getLocalizedName(isMarathi),
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(10.dp)),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Text(
                        text = category.getLocalizedName(isMarathi).take(2).uppercase(),
                        style = MaterialTheme.typography.titleMedium,
                        color = Primary.copy(alpha = 0.6f),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(4.dp))
        
        // Category name - smaller text
        Text(
            text = category.getLocalizedName(isMarathi),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Medium,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            lineHeight = 12.sp,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
