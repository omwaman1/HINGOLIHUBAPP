package com.hingoli.hub.ui.jobs

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.hingoli.hub.data.model.Category
import com.hingoli.hub.data.settings.AppLanguage
import com.hingoli.hub.data.settings.SettingsManager
import com.hingoli.hub.ui.components.*
import com.hingoli.hub.ui.city.CitySelectionBottomSheet
import com.hingoli.hub.ui.city.CitySelectionViewModel
import com.hingoli.hub.ui.theme.*

/**
 * Jobs Screen with Category Filter Chips and Direct Listings
 * Shows job listings directly with filter chips for category selection
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JobsScreen(
    onListingClick: (listingId: Long) -> Unit,
    onMenuClick: () -> Unit = {},
    onPostClick: () -> Unit = {},
    viewModel: JobsViewModel = hiltViewModel(),
    cityViewModel: CitySelectionViewModel = hiltViewModel(),
    settingsManager: SettingsManager? = null
) {
    val uiState by viewModel.uiState.collectAsState()
    val cityUiState by cityViewModel.uiState.collectAsState()
    val listState = rememberLazyListState()
    var showCityPicker by remember { mutableStateOf(false) }
    val selectedLanguage by settingsManager?.languageFlow?.collectAsState(initial = AppLanguage.MARATHI) 
        ?: remember { mutableStateOf(AppLanguage.MARATHI) }
    val isMarathi = selectedLanguage == AppLanguage.MARATHI
    
    // Load data on first launch
    LaunchedEffect(Unit) {
        viewModel.loadJobsData(cityUiState.selectedCity?.name)
    }
    
    // Reload when city changes
    LaunchedEffect(cityUiState.selectedCity?.cityId) {
        viewModel.onCityChanged(cityUiState.selectedCity?.name)
    }
    
    // City picker
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
    
    // Pagination
    LaunchedEffect(listState) {
        snapshotFlow {
            val lastVisibleItem = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            val totalItems = listState.layoutInfo.totalItemsCount
            lastVisibleItem >= totalItems - 3
        }.collect { shouldLoadMore ->
            if (shouldLoadMore && !uiState.isLoading && !uiState.isLoadingMore) {
                viewModel.loadMoreJobs()
            }
        }
    }
    
    Scaffold(
        topBar = {
            HingoliHubTopAppBar(
                title = if (isMarathi) {
                    "${cityUiState.selectedCity?.nameMr ?: "???????"} ????? ???????"
                } else {
                    "Jobs in ${cityUiState.selectedCity?.name ?: "Hingoli"}"
                },
                onMenuClick = onMenuClick,
                onCityClick = { showCityPicker = true },
                cityName = cityUiState.selectedCity?.getLocalizedName(isMarathi) ?: if (isMarathi) "???????" else "Hingoli",
                isMarathi = isMarathi
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Search bar with Plus icon
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Search field
                OutlinedTextField(
                    value = uiState.searchQuery,
                    onValueChange = { viewModel.onSearchQueryChanged(it) },
                    modifier = Modifier.weight(1f),
                    placeholder = {
                        Text(
                            text = if (isMarathi) "????? ????..." else "Search jobs...",
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
                
                // Plus icon button - attractive design
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
                            contentDescription = "Add Job",
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            }
            
            // Category Filter Chips
            CategoryFilterChips(
                categories = uiState.categories,
                selectedCategoryId = uiState.selectedCategoryId,
                isMarathi = isMarathi,
                onCategorySelected = { category ->
                    viewModel.onCategorySelected(category?.categoryId)
                }
            )
            
            // Job Listings
            when {
                uiState.isLoading && uiState.listings.isEmpty() -> ShimmerListScreen()
                uiState.error != null && uiState.listings.isEmpty() -> ErrorView(
                    message = uiState.error!!,
                    onRetry = { viewModel.loadJobsData(cityUiState.selectedCity?.name) }
                )
                uiState.listings.isEmpty() -> EmptyView(message = "No jobs found")
                else -> {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(
                            items = uiState.listings,
                            key = { it.listingId }
                        ) { listing ->
                            ListingCard(
                                listing = listing,
                                onClick = { onListingClick(listing.listingId) }
                            )
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
                                    CircularProgressIndicator(modifier = Modifier.size(32.dp))
                                }
                            }
                        }
                        
                        // Bottom spacing
                        item { Spacer(modifier = Modifier.height(80.dp)) }
                    }
                }
            }
        }
    }
}

/**
 * Horizontal scrollable category filter chips
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CategoryFilterChips(
    categories: List<Category>,
    selectedCategoryId: Int?,
    isMarathi: Boolean,
    onCategorySelected: (Category?) -> Unit
) {
    val scrollState = rememberScrollState()
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // "All" chip
        FilterChip(
            selected = selectedCategoryId == null,
            onClick = { onCategorySelected(null) },
            label = { 
                Text(
                    text = if (isMarathi) "????" else "All",
                    fontWeight = if (selectedCategoryId == null) FontWeight.Bold else FontWeight.Normal
                ) 
            },
            colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = Primary,
                selectedLabelColor = Color.White
            ),
            shape = RoundedCornerShape(20.dp)
        )
        
        // Category chips
        categories.forEach { category ->
            val isSelected = selectedCategoryId == category.categoryId
            FilterChip(
                selected = isSelected,
                onClick = { onCategorySelected(category) },
                label = { 
                    Text(
                        text = category.getLocalizedName(isMarathi),
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    ) 
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Primary,
                    selectedLabelColor = Color.White
                ),
                shape = RoundedCornerShape(20.dp)
            )
        }
    }
}
