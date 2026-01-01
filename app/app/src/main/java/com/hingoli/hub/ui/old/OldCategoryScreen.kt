package com.hingoli.hub.ui.old

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.hingoli.hub.data.model.OldCategory
import com.hingoli.hub.data.settings.AppLanguage
import com.hingoli.hub.data.settings.SettingsManager
import com.hingoli.hub.ui.components.*
import com.hingoli.hub.ui.city.CitySelectionBottomSheet
import com.hingoli.hub.ui.city.CitySelectionViewModel
import com.hingoli.hub.ui.theme.*

/**
 * Old Category Screen for browsing used/second-hand products.
 * Shows categories from old_categories table.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OldCategoryScreen(
    onCategoryClick: (categoryId: Int, categoryName: String) -> Unit,
    onSubcategoryClick: (categoryId: Int, subcategoryId: Int, subcategoryName: String) -> Unit = { _, _, _ -> },
    onMenuClick: () -> Unit = {},
    onPostClick: () -> Unit = {},
    viewModel: OldCategoryViewModel = hiltViewModel(),
    cityViewModel: CitySelectionViewModel = hiltViewModel(),
    settingsManager: SettingsManager? = null
) {
    val uiState by viewModel.uiState.collectAsState()
    val cityUiState by cityViewModel.uiState.collectAsState()
    var showCityPicker by remember { mutableStateOf(false) }
    
    val selectedLanguage by settingsManager?.languageFlow?.collectAsState(initial = AppLanguage.MARATHI) ?: remember { mutableStateOf(AppLanguage.MARATHI) }
    val isMarathi = selectedLanguage == AppLanguage.MARATHI
    
    LaunchedEffect(Unit) {
        viewModel.loadCategories()
    }
    
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
            val cityDisplayName = cityUiState.selectedCity?.getLocalizedName(isMarathi) ?: if (isMarathi) "हिंगोली" else "Hingoli"
            val screenTitle = if (isMarathi) "जुने सामान" else "Used Items"
            
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
                uiState.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Primary)
                    }
                }
                uiState.error != null -> {
                    ErrorView(
                        message = uiState.error ?: "Something went wrong",
                        onRetry = { viewModel.loadCategories() }
                    )
                }
                else -> {
                    OldCategoryContent(
                        sections = uiState.categorySections,
                        isMarathi = isMarathi,
                        topBanners = uiState.topBanners,
                        bottomBanners = uiState.bottomBanners,
                        onCategoryClick = { category ->
                            onCategoryClick(category.id, category.getLocalizedName(isMarathi))
                        },
                        onSubcategoryClick = { category, subcategory ->
                            onSubcategoryClick(category.id, subcategory.id, subcategory.getLocalizedName(isMarathi))
                        },
                        onBannerClick = { /* Handle banner click */ }
                    )
                }
            }
        }
    }
}

@Composable
private fun OldCategoryContent(
    sections: List<OldCategorySection>,
    isMarathi: Boolean,
    topBanners: List<Banner>,
    bottomBanners: List<Banner>,
    onCategoryClick: (OldCategory) -> Unit,
    onSubcategoryClick: (OldCategory, OldCategory) -> Unit,
    onBannerClick: (Banner) -> Unit
) {
    if (sections.isEmpty()) {
        EmptyView(message = if (isMarathi) "कोणत्याही श्रेणी नाहीत" else "No categories found")
        return
    }
    
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 8.dp)
    ) {
        // Top banners
        if (topBanners.isNotEmpty()) {
            item(key = "top_banners") {
                BannerCarousel(
                    banners = topBanners,
                    onBannerClick = onBannerClick
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
        
        // Category sections with subcategories
        items(sections, key = { it.category.id }) { section ->
            OldCategorySectionCard(
                section = section,
                isMarathi = isMarathi,
                onCategoryClick = { onCategoryClick(section.category) },
                onSubcategoryClick = { subcategory -> onSubcategoryClick(section.category, subcategory) }
            )
        }
        
        // Bottom banners
        if (bottomBanners.isNotEmpty()) {
            item(key = "bottom_banners") {
                Spacer(modifier = Modifier.height(8.dp))
                BannerCarousel(
                    banners = bottomBanners,
                    onBannerClick = onBannerClick
                )
            }
        }
        
        // Bottom spacing for navigation bar
        item { Spacer(modifier = Modifier.height(80.dp)) }
    }
}

@Composable
private fun OldCategorySectionCard(
    section: OldCategorySection,
    isMarathi: Boolean,
    onCategoryClick: () -> Unit,
    onSubcategoryClick: (OldCategory) -> Unit
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
            // Category header - clickable
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onCategoryClick() }
                    .padding(bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Main category - just show text, no image
                Text(
                    text = section.category.getLocalizedName(isMarathi),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = OnSurface
                )
                
                Spacer(modifier = Modifier.weight(1f))
                
                // Product count badge
                if (section.category.productCount > 0) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Primary.copy(alpha = 0.1f)
                    ) {
                        Text(
                            text = "${section.category.productCount}",
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
    subcategories: List<OldCategory>,
    isMarathi: Boolean,
    onSubcategoryClick: (OldCategory) -> Unit
) {
    val chunkedSubcategories = subcategories.chunked(4)
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp)
    ) {
        chunkedSubcategories.forEach { rowItems ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                rowItems.forEach { subcategory ->
                    SubcategoryCard(
                        subcategory = subcategory,
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
    subcategory: OldCategory,
    isMarathi: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .padding(4.dp)
            .clickable { onClick() },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Image card
        Card(
            modifier = Modifier.size(64.dp),
            shape = RoundedCornerShape(10.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceVariant)
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                if (subcategory.imageUrl != null) {
                    AsyncImage(
                        model = subcategory.imageUrl,
                        contentDescription = subcategory.name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    // Fallback icon - first letter
                    Text(
                        text = subcategory.name.take(1).uppercase(),
                        style = MaterialTheme.typography.titleMedium,
                        color = Primary.copy(alpha = 0.6f),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(4.dp))
        
        // Subcategory name
        Text(
            text = subcategory.getLocalizedName(isMarathi),
            style = MaterialTheme.typography.labelSmall,
            color = OnSurfaceVariant,
            fontWeight = FontWeight.Medium,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            lineHeight = 12.sp,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
