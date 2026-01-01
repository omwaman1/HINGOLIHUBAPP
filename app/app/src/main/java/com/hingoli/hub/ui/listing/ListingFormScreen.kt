package com.hingoli.hub.ui.listing

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.hingoli.hub.data.model.Category
import com.hingoli.hub.data.model.City
import com.hingoli.hub.data.settings.AppLanguage
import com.hingoli.hub.data.settings.SettingsManager
import com.hingoli.hub.ui.theme.*

/**
 * Unified Listing Form Screen - handles both creating new listings and editing existing ones
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListingFormScreen(
    listingType: String? = null,
    listingId: Long? = null,
    condition: String? = null,  // For selling type: "old" or "new" - passed from navigation
    onBackClick: () -> Unit,
    onSuccess: () -> Unit,
    viewModel: ListingFormViewModel = hiltViewModel(),
    settingsManager: SettingsManager? = null
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val selectedLanguage by settingsManager?.languageFlow?.collectAsState(initial = AppLanguage.MARATHI)
        ?: remember { mutableStateOf(AppLanguage.MARATHI) }
    val isMarathi = selectedLanguage == AppLanguage.MARATHI
    
    // Image picker
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.onImageSelected(it) }
    }
    
    // Location permission and capture
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    var hasLocationPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        )
    }
    
    // Function to fetch current location
    fun fetchCurrentLocation() {
        viewModel.onLocationLoading(true)
        try {
            fusedLocationClient.getCurrentLocation(
                Priority.PRIORITY_HIGH_ACCURACY,
                CancellationTokenSource().token
            ).addOnSuccessListener { location ->
                if (location != null) {
                    viewModel.onLocationCoordinatesChanged(location.latitude, location.longitude)
                } else {
                    viewModel.onLocationError("Unable to get location. Please try again.")
                }
            }.addOnFailureListener {
                viewModel.onLocationError("Location error: ${it.message}")
            }
        } catch (e: SecurityException) {
            viewModel.onLocationError("Location permission was denied")
        }
    }
    
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        hasLocationPermission = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (hasLocationPermission) {
            fetchCurrentLocation()
        } else {
            viewModel.onLocationError("Location permission is required to post a listing")
        }
    }
    
    // Function to request location
    fun requestLocation() {
        if (hasLocationPermission) {
            fetchCurrentLocation()
        } else {
            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }
    
    // Auto-fetch location on first load (only for create mode, NOT for selling type)
    LaunchedEffect(uiState.mode, uiState.listingType, hasLocationPermission) {
        // Skip location for selling, services, or jobs - they don't need GPS coordinates
        if (uiState.listingType == "selling" || uiState.listingType == "services" || uiState.listingType == "jobs") {
            return@LaunchedEffect
        }
        if (uiState.mode == ListingFormMode.CREATE && uiState.latitude == null && !uiState.isLocationLoading) {
            if (hasLocationPermission) {
                fetchCurrentLocation()
            } else {
                locationPermissionLauncher.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                )
            }
        }
    }
    
    // Initialize based on mode - Use Unit key to ensure this runs on every composition
    // This is necessary because the ViewModel persists for the navigation destination
    LaunchedEffect(Unit) {
        if (listingId != null && listingId > 0) {
            viewModel.initializeForEdit(listingId)
        } else if (listingType != null) {
            // Pass condition for selling type (defaults to "old" if not specified)
            viewModel.initializeForCreate(listingType, condition ?: "old")
        }
    }
    
    // Handle success - show toast with API message and navigate back
    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            val message = uiState.successMessage 
                ?: if (uiState.isEditMode) "Listing updated successfully" else "Listing submitted for approval!"
            android.widget.Toast.makeText(context, message, android.widget.Toast.LENGTH_LONG).show()
            onSuccess()
        }
    }
    
    val screenTitle = if (uiState.isEditMode) "Edit Listing" else "Post Listing"
    val submitButtonText = if (uiState.isEditMode) "Update Listing" else "Post Listing"
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(screenTitle, fontWeight = FontWeight.Bold) },
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
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(
                    brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFFF8FAFC), // Very light grayish white
                            Color.White,
                            Color(0xFFF1F5F9)  // Light gray at bottom
                        )
                    )
                )
        ) {
            when {
                uiState.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // ========== LISTING TYPE ==========
                        item {
                            if (uiState.canChangeListingType) {
                                // Compute the display value for the dropdown
                                // For selling type, show the compound value based on condition
                                val displayValue = when {
                                    uiState.listingType == "selling" && uiState.condition == "new" -> "selling_new"
                                    uiState.listingType == "selling" && uiState.condition == "old" -> "selling_old"
                                    else -> uiState.listingType
                                }
                                
                                FormDropdownField(
                                    label = "Listing Type *",
                                    selectedValue = displayValue,
                                    options = listOf(
                                        "services" to "🔧 Services",
                                        "selling_new" to "🛒 Sell New Product",
                                        "selling_old" to "♻️ Sell Old Product",
                                        "business" to "🏪 Business",
                                        "jobs" to "💼 Jobs"
                                    ),
                                    onValueSelected = { viewModel.onListingTypeChange(it) }
                                )
                            } else {
                                // Show read-only type badge in edit mode
                                val badgeText = when {
                                    uiState.listingType == "selling" && uiState.condition == "new" -> "✨ New Product"
                                    uiState.listingType == "selling" && uiState.condition == "old" -> "♻️ Old Product"
                                    else -> "Type: ${uiState.listingType.replaceFirstChar { it.uppercase() }}"
                                }
                                val badgeColor = when {
                                    uiState.listingType == "services" -> Color(0xFFE0E7FF)
                                    uiState.listingType == "selling" && uiState.condition == "new" -> Color(0xFFDCFCE7)
                                    uiState.listingType == "selling" && uiState.condition == "old" -> Color(0xFFFEF3C7)
                                    uiState.listingType == "business" -> Color(0xFFCCFBF1)
                                    uiState.listingType == "jobs" -> Color(0xFFFEF3C7)
                                    else -> Color.LightGray
                                }
                                
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = badgeColor),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = badgeText,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                        
                        // ========== CATEGORY ==========
                        // Show category dropdown for all listing types (both CREATE and EDIT modes)
                        run {
                            item {
                                FormCategoryDropdown(
                                    label = "Category *",
                                    categories = uiState.categories,
                                    selectedCategory = uiState.selectedCategory,
                                    onCategorySelected = { viewModel.onCategorySelected(it) }
                                )
                            }
                            
                            if (uiState.subcategories.isNotEmpty()) {
                                item {
                                    FormCategoryDropdown(
                                        label = "Subcategory",
                                        categories = uiState.subcategories,
                                        selectedCategory = uiState.selectedSubcategory,
                                        onCategorySelected = { viewModel.onSubcategorySelected(it) }
                                    )
                                }
                            }
                        }
                        
                        // ========== MAIN IMAGE ==========
                        item {
                            FormImagePicker(
                                existingImageUrl = uiState.existingImageUrl,
                                newImageUri = uiState.selectedImageUri,
                                onPickImage = { imagePickerLauncher.launch("image/*") }
                            )
                        }
                        
                        // ========== TITLE / BUSINESS NAME ==========
                        item {
                            val isBusiness = uiState.listingType == "business"
                            OutlinedTextField(
                                value = uiState.title,
                                onValueChange = { viewModel.onTitleChange(it) },
                                label = { Text(if (isBusiness) "Business Name *" else "Title *") },
                                placeholder = { Text(if (isBusiness) "Enter your business name" else "What are you offering?") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )
                        }
                        
                        // ========== DESCRIPTION ==========
                        item {
                            OutlinedTextField(
                                value = uiState.description,
                                onValueChange = { viewModel.onDescriptionChange(it) },
                                label = { Text("Description") },
                                placeholder = { Text("Describe in detail...") },
                                modifier = Modifier.fillMaxWidth(),
                                minLines = 3,
                                maxLines = 5
                            )
                        }
                        
                        // ========== PRICE (only for selling - services uses min/max, jobs/business don't have price) ==========
                        if (uiState.listingType == "selling") {
                            item {
                                OutlinedTextField(
                                    value = uiState.price,
                                    onValueChange = { viewModel.onPriceChange(it) },
                                    label = { Text("Price (₹)") },
                                    placeholder = { Text("e.g. 500") },
                                    modifier = Modifier.fillMaxWidth(),
                                    leadingIcon = { Text("₹", modifier = Modifier.padding(start = 12.dp)) },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                                )
                            }
                        }
                        
                        // ========== LOCATION (Now included for services, still excluded for selling) ==========
                        if (uiState.listingType != "selling") {
                            item {
                                OutlinedTextField(
                                    value = uiState.location,
                                    onValueChange = { viewModel.onLocationChange(it) },
                                    label = { Text(if (uiState.listingType == "services") "Service Area / Location" else "Location Name") },
                                    placeholder = { Text(if (uiState.listingType == "services") "e.g. All of Hingoli" else "e.g. Main Market") },
                                    modifier = Modifier.fillMaxWidth(),
                                    leadingIcon = { Icon(Icons.Default.LocationOn, null) }
                                )
                            }
                        }
                        
                        // ========== GPS COORDINATES STATUS (Not needed for selling, services, or jobs type) ==========
                        if (uiState.listingType != "selling" && uiState.listingType != "services" && uiState.listingType != "jobs") {
                            item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = when {
                                        uiState.latitude != null -> Color(0xFFE8F5E9) // Green tint
                                        uiState.locationError != null -> Color(0xFFFEE2E2) // Red tint
                                        else -> MaterialTheme.colorScheme.surfaceVariant
                                    }
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    when {
                                        uiState.isLocationLoading -> {
                                            CircularProgressIndicator(
                                                modifier = Modifier.size(20.dp),
                                                strokeWidth = 2.dp
                                            )
                                            Spacer(modifier = Modifier.width(12.dp))
                                            Text("Getting your location...", style = MaterialTheme.typography.bodyMedium)
                                        }
                                        uiState.latitude != null && uiState.longitude != null -> {
                                            Icon(
                                                Icons.Default.CheckCircle,
                                                contentDescription = null,
                                                tint = Color(0xFF4CAF50),
                                                modifier = Modifier.size(20.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    "✅ Location captured",
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    fontWeight = FontWeight.Medium,
                                                    color = Color(0xFF2E7D32)
                                                )
                                                Text(
                                                    "${String.format("%.6f", uiState.latitude)}, ${String.format("%.6f", uiState.longitude)}",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = Color(0xFF558B2F)
                                                )
                                            }
                                            TextButton(onClick = { requestLocation() }) {
                                                Text("Refresh")
                                            }
                                        }
                                        uiState.locationError != null -> {
                                            Icon(
                                                Icons.Default.Warning,
                                                contentDescription = null,
                                                tint = Color(0xFFDC2626),
                                                modifier = Modifier.size(20.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    "Location required",
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    fontWeight = FontWeight.Medium,
                                                    color = Color(0xFFDC2626)
                                                )
                                                Text(
                                                    uiState.locationError!!,
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = Color(0xFFB91C1C)
                                                )
                                            }
                                            TextButton(onClick = { requestLocation() }) {
                                                Text("Try Again")
                                            }
                                        }
                                        else -> {
                                            Icon(
                                                Icons.Default.MyLocation,
                                                contentDescription = null,
                                                tint = PrimaryBlue,
                                                modifier = Modifier.size(20.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                "Tap to get your location",
                                                style = MaterialTheme.typography.bodyMedium,
                                                modifier = Modifier.weight(1f)
                                            )
                                            TextButton(onClick = { requestLocation() }) {
                                                Text("Get Location")
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        }
                        
                        // ========== CITY DROPDOWN (Not needed for selling type only) ==========
                        if (uiState.listingType != "selling") {
                            item {
                                FormCityDropdown(
                                    cities = uiState.cities,
                                    selectedCity = uiState.selectedCity,
                                    isMarathi = isMarathi,
                                    onCitySelected = { viewModel.onCitySelected(it) }
                                )
                            }
                        }
                        
                        // ============ TYPE-SPECIFIC FIELDS ============
                        
                        // Services fields
                        if (uiState.listingType == "services") {
                            item {
                                FormSectionHeader("🔧 Service Details")
                            }
                            item {
                                OutlinedTextField(
                                    value = uiState.experienceYears,
                                    onValueChange = { viewModel.onExperienceYearsChange(it) },
                                    label = { Text("Years of Experience") },
                                    placeholder = { Text("Enter how many years") },
                                    modifier = Modifier.fillMaxWidth(),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                                )
                            }
                            // Price Range (Min/Max)
                            item {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    OutlinedTextField(
                                        value = uiState.priceMin,
                                        onValueChange = { viewModel.onPriceMinChange(it) },
                                        label = { Text("Min Price (₹)") },
                                        placeholder = { Text("e.g. 100") },
                                        modifier = Modifier.weight(1f),
                                        leadingIcon = { Text("₹", modifier = Modifier.padding(start = 12.dp)) },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                                    )
                                    OutlinedTextField(
                                        value = uiState.priceMax,
                                        onValueChange = { viewModel.onPriceMaxChange(it) },
                                        label = { Text("Max Price (₹)") },
                                        placeholder = { Text("e.g. 5000") },
                                        modifier = Modifier.weight(1f),
                                        leadingIcon = { Text("₹", modifier = Modifier.padding(start = 12.dp)) },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                                    )
                                }
                            }
                        }
                        
                        // Jobs fields
                        if (uiState.listingType == "jobs") {
                            item {
                                FormSectionHeader("💼 Job Details")
                            }
                            item {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    OutlinedTextField(
                                        value = uiState.salaryMin,
                                        onValueChange = { viewModel.onSalaryMinChange(it) },
                                        label = { Text("Min Salary") },
                                        modifier = Modifier.weight(1f),
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                                    )
                                    OutlinedTextField(
                                        value = uiState.salaryMax,
                                        onValueChange = { viewModel.onSalaryMaxChange(it) },
                                        label = { Text("Max Salary") },
                                        modifier = Modifier.weight(1f),
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                                    )
                                }
                            }
                            item {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Box(modifier = Modifier.weight(1f)) {
                                        FormDropdownField(
                                            label = "Salary Period",
                                            selectedValue = uiState.salaryPeriod,
                                            options = listOf(
                                                "monthly" to "Monthly",
                                                "yearly" to "Yearly",
                                                "daily" to "Daily"
                                            ),
                                            onValueSelected = { viewModel.onSalaryPeriodChange(it) }
                                        )
                                    }
                                    Box(modifier = Modifier.weight(1f)) {
                                        FormDropdownField(
                                            label = "Employment Type",
                                            selectedValue = uiState.employmentType,
                                            options = listOf(
                                                "full_time" to "Full Time",
                                                "part_time" to "Part Time",
                                                "contract" to "Contract"
                                            ),
                                            onValueSelected = { viewModel.onEmploymentTypeChange(it) }
                                        )
                                    }
                                }
                            }
                            item {
                                FormDropdownField(
                                    label = "Work Location",
                                    selectedValue = uiState.remoteOption,
                                    options = listOf(
                                        "on_site" to "On Site",
                                        "remote" to "Remote",
                                        "hybrid" to "Hybrid"
                                    ),
                                    onValueSelected = { viewModel.onRemoteOptionChange(it) }
                                )
                            }
                            item {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    OutlinedTextField(
                                        value = uiState.vacancies,
                                        onValueChange = { viewModel.onVacanciesChange(it) },
                                        label = { Text("Vacancies") },
                                        modifier = Modifier.weight(1f),
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                                    )
                                    OutlinedTextField(
                                        value = uiState.experienceRequired,
                                        onValueChange = { viewModel.onExperienceRequiredChange(it) },
                                        label = { Text("Experience (Years)") },
                                        modifier = Modifier.weight(1f),
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                                    )
                                }
                            }
                            item {
                                OutlinedTextField(
                                    value = uiState.educationRequired,
                                    onValueChange = { viewModel.onEducationRequiredChange(it) },
                                    label = { Text("Education Required") },
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                        
                        // Selling fields
                        if (uiState.listingType == "selling") {
                            item {
                                FormSectionHeader("📦 Product Details")
                            }
                            // Condition is ALWAYS locked for selling type because:
                            // 1. User selects "Sell New Product" or "Sell Old Product" from listing type dropdown
                            // 2. This automatically sets the condition, no need for separate dropdown
                            // 3. For edit mode, condition cannot be changed
                            // Show read-only condition badge
                            item {
                                Card(
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (uiState.condition == "new") 
                                            Color(0xFFDCFCE7) else Color(0xFFFEF3C7)
                                    ),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = if (uiState.condition == "new") 
                                            "✨ New Product" else "♻️ Old / Used Product",
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                        fontWeight = FontWeight.Medium,
                                        color = if (uiState.condition == "new") 
                                            Color(0xFF166534) else Color(0xFFB45309)
                                    )
                                }
                            }
                            // MRP and Stock Quantity row
                            item {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    OutlinedTextField(
                                        value = uiState.discountedPrice,
                                        onValueChange = { viewModel.onDiscountedPriceChange(it) },
                                        label = { Text("MRP (₹)") },
                                        placeholder = { Text("Original price") },
                                        modifier = Modifier.weight(1f),
                                        singleLine = true,
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                                    )
                                    OutlinedTextField(
                                        value = uiState.stockQty,
                                        onValueChange = { viewModel.onStockQtyChange(it) },
                                        label = { Text("Stock Qty") },
                                        placeholder = { Text("Available") },
                                        modifier = Modifier.weight(1f),
                                        singleLine = true,
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                                    )
                                }
                            }
                            // Sell Online Toggle
                            item {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (uiState.sellOnline) 
                                            PrimaryBlue.copy(alpha = 0.1f) 
                                        else 
                                            Color.Gray.copy(alpha = 0.1f)
                                    )
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(
                                                "Available for Online Sale", 
                                                style = MaterialTheme.typography.bodyLarge,
                                                fontWeight = FontWeight.Medium
                                            )
                                            Text(
                                                if (uiState.sellOnline) "Customers can purchase online" else "Display only (no purchase button)",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = Color.Gray
                                            )
                                        }
                                        Switch(
                                            checked = uiState.sellOnline,
                                            onCheckedChange = { viewModel.onSellOnlineChange(it) },
                                            colors = SwitchDefaults.colors(
                                                checkedThumbColor = Color.White,
                                                checkedTrackColor = PrimaryBlue,
                                                uncheckedThumbColor = Color.White,
                                                uncheckedTrackColor = Color.Gray
                                            )
                                        )
                                    }
                                }
                            }
                            // Delivery Days Dropdown
                            item {
                                FormDropdownField(
                                    label = "Delivery Time",
                                    selectedValue = uiState.deliveryBy.toString(),
                                    options = listOf(
                                        "1" to "Today",
                                        "2" to "Tomorrow",
                                        "3" to "3 Days",
                                        "4" to "4 Days",
                                        "5" to "5 Days",
                                        "6" to "6 Days",
                                        "7" to "7 Days",
                                        "8" to "8 Days"
                                    ),
                                    onValueSelected = { viewModel.onDeliveryByChange(it.toInt()) }
                                )
                        }
                        } // Close if (selling)
                        
                        // Business fields removed - no longer collecting industry/year/team size
                        
                        // Status dropdown (edit mode only)
                        if (uiState.isEditMode) {
                            item {
                                FormSectionHeader("⚙️ Status")
                            }
                            item {
                                FormDropdownField(
                                    label = "Listing Status",
                                    selectedValue = uiState.status,
                                    options = listOf(
                                        "active" to "Active",
                                        "inactive" to "Inactive"
                                    ),
                                    onValueSelected = { viewModel.onStatusChange(it) }
                                )
                            }
                        }
                        
                        // Error message
                        if (uiState.error != null) {
                            item {
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFEE2E2))
                                ) {
                                    Text(
                                        text = uiState.error!!,
                                        color = Color(0xFFDC2626),
                                        modifier = Modifier.padding(12.dp)
                                    )
                                }
                            }
                        }
                        
                        // Submit button
                        item {
                            Button(
                                onClick = { viewModel.submitListing(context) },
                                enabled = !uiState.isSubmitting && uiState.title.isNotBlank(),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp),
                                shape = RoundedCornerShape(4.dp), // Rectangle shape
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = PrimaryBlue,
                                    disabledContainerColor = Color(0xFFCBD5E1)
                                )
                            ) {
                                if (uiState.isSubmitting) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(24.dp),
                                        color = Color.White
                                    )
                                } else {
                                    val icon = if (uiState.isEditMode) Icons.Default.Check else Icons.Default.Upload
                                    Icon(icon, null, modifier = Modifier.size(20.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(submitButtonText, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                        
                        // Bottom spacing
                        item { Spacer(modifier = Modifier.height(32.dp)) }
                    }
                }
            }
        }
    }
}

// ============ Helper Composables ============

@Composable
private fun FormSectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(top = 8.dp)
    )
}

@Composable
private fun FormImagePicker(
    existingImageUrl: String?,
    newImageUri: Uri?,
    onPickImage: () -> Unit
) {
    Column {
        Text(
            text = "Main Image",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .clip(RoundedCornerShape(4.dp)) // Rectangle shape
                .background(Color.White)
                .border(
                    width = 1.dp,
                    color = Color(0xFFE2E8F0),
                    shape = RoundedCornerShape(4.dp)
                )
                .clickable { onPickImage() },
            contentAlignment = Alignment.Center
        ) {
            when {
                newImageUri != null -> {
                    AsyncImage(
                        model = newImageUri,
                        contentDescription = "Selected image",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
                existingImageUrl != null -> {
                    AsyncImage(
                        model = existingImageUrl,
                        contentDescription = "Current image",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
                else -> {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.AddPhotoAlternate,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = PrimaryBlue
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Click to upload image", color = PrimaryBlue)
                    }
                }
            }
            
            // Overlay hint to change existing image
            if (newImageUri != null || existingImageUrl != null) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Tap to change",
                        color = Color.White,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FormCategoryDropdown(
    label: String,
    categories: List<Category>,
    selectedCategory: Category?,
    onCategorySelected: (Category) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it }
    ) {
        OutlinedTextField(
            value = selectedCategory?.name ?: "Select...",
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor()
        )
        
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            categories.forEach { category ->
                DropdownMenuItem(
                    text = { Text(category.name) },
                    onClick = {
                        onCategorySelected(category)
                        expanded = false
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FormCityDropdown(
    cities: List<City>,
    selectedCity: City?,
    isMarathi: Boolean = false,
    onCitySelected: (City) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it }
    ) {
        OutlinedTextField(
            value = selectedCity?.getLocalizedName(isMarathi) ?: if (isMarathi) "शहर निवडा..." else "Select City...",
            onValueChange = {},
            readOnly = true,
            label = { Text(if (isMarathi) "शहर *" else "City *") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor()
        )
        
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            cities.forEach { city ->
                DropdownMenuItem(
                    text = { Text(city.getLocalizedName(isMarathi)) },
                    onClick = {
                        onCitySelected(city)
                        expanded = false
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FormDropdownField(
    label: String,
    selectedValue: String,
    options: List<Pair<String, String>>,
    onValueSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val displayValue = options.find { it.first == selectedValue }?.second ?: options.first().second
    
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it }
    ) {
        OutlinedTextField(
            value = displayValue,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor()
        )
        
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { (value, display) ->
                DropdownMenuItem(
                    text = { Text(display) },
                    onClick = {
                        onValueSelected(value)
                        expanded = false
                    }
                )
            }
        }
    }
}
