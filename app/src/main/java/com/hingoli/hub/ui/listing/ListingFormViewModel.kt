package com.hingoli.hub.ui.listing

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hingoli.hub.data.api.ApiService
import com.hingoli.hub.data.api.TokenManager
import com.hingoli.hub.data.model.Category
import com.hingoli.hub.data.model.City
import com.hingoli.hub.data.repository.SharedDataRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import javax.inject.Inject

/**
 * Mode for the listing form - either creating a new listing or editing an existing one
 */
enum class ListingFormMode {
    CREATE,
    EDIT
}

data class ListingFormUiState(
    // Mode
    val mode: ListingFormMode = ListingFormMode.CREATE,
    val listingId: Long? = null,
    
    // Common fields
    val listingType: String = "services",
    val title: String = "",
    val description: String = "",
    val location: String = "",
    val price: String = "",
    val status: String = "active",
    
    // Image
    val existingImageUrl: String? = null,
    val selectedImageUri: Uri? = null,
    
    // Category
    val categories: List<Category> = emptyList(),
    val selectedCategory: Category? = null,
    val subcategories: List<Category> = emptyList(),
    val selectedSubcategory: Category? = null,
    // Pending category/subcategory IDs for edit mode (to auto-select after loading)
    val pendingCategoryId: Int? = null,
    val pendingSubcategoryId: Int? = null,
    
    // City
    val cities: List<City> = emptyList(),
    val selectedCity: City? = null,
    
    // Services-specific
    val experienceYears: String = "",
    val priceMin: String = "",    // Minimum price for services
    val priceMax: String = "",    // Maximum price for services
    
    // Jobs-specific
    val salaryMin: String = "",
    val salaryMax: String = "",
    val salaryPeriod: String = "monthly",
    val employmentType: String = "full_time",
    val remoteOption: String = "on_site",
    val vacancies: String = "1",
    val experienceRequired: String = "0",
    val educationRequired: String = "",
    
    // Selling-specific (default to 'old' for used products)
    val condition: String = "old",
    val discountedPrice: String = "",  // MRP (original price before discount)
    val stockQty: String = "",          // Stock quantity
    val sellOnline: Boolean = true,     // Available for online purchase
    
    // Business-specific
    val businessName: String = "",
    val industry: String = "",
    val establishedYear: String = "",
    val employeeCount: String = "1-10",
    
    // Location coordinates
    val latitude: Double? = null,
    val longitude: Double? = null,
    val isLocationLoading: Boolean = false,
    val locationError: String? = null,
    
    // UI state
    val isLoading: Boolean = false,
    val isSubmitting: Boolean = false,
    val isSuccess: Boolean = false,
    val successMessage: String? = null,
    val error: String? = null
) {
    val isEditMode: Boolean get() = mode == ListingFormMode.EDIT
    val canChangeListingType: Boolean get() = mode == ListingFormMode.CREATE
}

/**
 * ListingFormViewModel - uses SharedDataRepository cache for categories and cities.
 * No redundant API calls - all dropdown data comes from prefetched cache.
 */
@HiltViewModel
class ListingFormViewModel @Inject constructor(
    private val apiService: ApiService,
    private val sharedDataRepository: SharedDataRepository,
    private val tokenManager: TokenManager
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(ListingFormUiState())
    val uiState: StateFlow<ListingFormUiState> = _uiState.asStateFlow()
    
    /**
     * Initialize for creating a new listing
     */
    fun initializeForCreate(listingType: String) {
        _uiState.value = ListingFormUiState(
            mode = ListingFormMode.CREATE,
            listingType = listingType
        )
        loadCategories(listingType)
        loadCities()
    }
    
    /**
     * Initialize for editing an existing listing
     */
    fun initializeForEdit(listingId: Long) {
        _uiState.value = _uiState.value.copy(
            mode = ListingFormMode.EDIT,
            listingId = listingId,
            isLoading = true
        )
        loadListing(listingId)
    }
    
    private fun loadListing(listingId: Long) {
        viewModelScope.launch {
            try {
                val response = apiService.getListingById(listingId)
                if (response.isSuccessful && response.body()?.success == true) {
                    val listing = response.body()?.data
                    if (listing != null) {
                        _uiState.value = _uiState.value.copy(
                            listingId = listing.listingId,
                            listingType = listing.listingType,
                            title = listing.title,
                            description = listing.description ?: "",
                            location = listing.location ?: "",
                            price = listing.price?.toLong()?.toString() ?: "",
                            status = listing.status,
                            existingImageUrl = listing.mainImageUrl,
                            // Store pending category IDs for auto-selection
                            pendingCategoryId = listing.categoryId,
                            pendingSubcategoryId = listing.subcategoryId,
                            // Jobs fields
                            salaryMin = listing.salaryMin?.toLong()?.toString() ?: "",
                            salaryMax = listing.salaryMax?.toLong()?.toString() ?: "",
                            salaryPeriod = listing.salaryPeriod ?: "monthly",
                            employmentType = listing.employmentType ?: "full_time",
                            remoteOption = listing.workLocationType ?: "on_site",
                            // Services fields
                            experienceYears = listing.serviceDetails?.experienceYears?.toString() 
                                ?: listing.experienceYears?.toString() ?: "",
                            priceMin = listing.serviceDetails?.priceMin?.toLong()?.toString() ?: "",
                            priceMax = listing.serviceDetails?.priceMax?.toLong()?.toString() ?: "",
                            isLoading = false
                        )
                        // Load categories for the listing type (will auto-select based on pendingCategoryId)
                        loadCategories(listing.listingType)
                        loadCities()
                    } else {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = "Listing not found"
                        )
                    }
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Failed to load listing"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to load listing"
                )
            }
        }
    }
    
    /**
     * Load categories from SharedDataRepository cache
     */
    private fun loadCategories(listingType: String) {
        viewModelScope.launch {
            try {
                val allCategories = sharedDataRepository.getCategories(listingType)
                val mainCategories = allCategories.filter { it.parentId == null }
                
                val pendingCatId = _uiState.value.pendingCategoryId
                val matchingCategory = if (pendingCatId != null) {
                    mainCategories.find { it.categoryId == pendingCatId }
                } else null
                
                _uiState.value = _uiState.value.copy(
                    categories = mainCategories,
                    selectedCategory = matchingCategory
                )
                
                if (matchingCategory != null) {
                    loadSubcategories(matchingCategory.categoryId)
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = "Failed to load categories")
            }
        }
    }
    
    /**
     * Load subcategories from SharedDataRepository cache
     */
    private fun loadSubcategories(categoryId: Int) {
        viewModelScope.launch {
            try {
                val subcategories = sharedDataRepository.getSubcategoriesForParent(categoryId)
                
                val pendingSubId = _uiState.value.pendingSubcategoryId
                val matchingSubcategory = if (pendingSubId != null) {
                    subcategories.find { it.categoryId == pendingSubId }
                } else null
                
                _uiState.value = _uiState.value.copy(
                    subcategories = subcategories,
                    selectedSubcategory = matchingSubcategory
                )
            } catch (e: Exception) {
                // Silent fail - subcategories not critical
            }
        }
    }
    
    /**
     * Load cities from SharedDataRepository cache
     */
    private fun loadCities() {
        viewModelScope.launch {
            try {
                val cities = sharedDataRepository.getCities()
                val defaultCity = cities.find { it.name == "Hingoli" } ?: cities.firstOrNull()
                _uiState.value = _uiState.value.copy(
                    cities = cities,
                    selectedCity = _uiState.value.selectedCity ?: defaultCity
                )
            } catch (e: Exception) {
                // Silent fail - cities from cache
            }
        }
    }
    
    // Field update functions
    fun onListingTypeChange(value: String) {
        if (!_uiState.value.canChangeListingType) return
        _uiState.value = _uiState.value.copy(
            listingType = value,
            selectedCategory = null,
            selectedSubcategory = null,
            subcategories = emptyList(),
            error = null
        )
        loadCategories(value)
    }
    
    fun onTitleChange(value: String) {
        _uiState.value = _uiState.value.copy(title = value, error = null)
    }
    
    fun onDescriptionChange(value: String) {
        _uiState.value = _uiState.value.copy(description = value, error = null)
    }
    
    fun onLocationChange(value: String) {
        _uiState.value = _uiState.value.copy(location = value, error = null)
    }
    
    fun onPriceChange(value: String) {
        _uiState.value = _uiState.value.copy(price = value, error = null)
    }
    
    fun onCategorySelected(category: Category) {
        _uiState.value = _uiState.value.copy(
            selectedCategory = category,
            selectedSubcategory = null,
            subcategories = emptyList(),
            error = null
        )
        loadSubcategories(category.categoryId)
    }
    
    fun onSubcategorySelected(subcategory: Category) {
        _uiState.value = _uiState.value.copy(selectedSubcategory = subcategory, error = null)
    }
    
    fun onCitySelected(city: City) {
        _uiState.value = _uiState.value.copy(selectedCity = city, error = null)
    }
    
    // Location coordinates
    fun onLocationCoordinatesChanged(latitude: Double, longitude: Double) {
        _uiState.value = _uiState.value.copy(
            latitude = latitude,
            longitude = longitude,
            isLocationLoading = false,
            locationError = null,
            error = null
        )
    }
    
    fun onLocationLoading(loading: Boolean) {
        _uiState.value = _uiState.value.copy(isLocationLoading = loading)
    }
    
    fun onLocationError(message: String) {
        _uiState.value = _uiState.value.copy(
            isLocationLoading = false,
            locationError = message
        )
    }
    
    fun clearLocationError() {
        _uiState.value = _uiState.value.copy(locationError = null)
    }
    
    fun onImageSelected(uri: Uri) {
        _uiState.value = _uiState.value.copy(selectedImageUri = uri, error = null)
    }
    
    fun onStatusChange(value: String) {
        _uiState.value = _uiState.value.copy(status = value, error = null)
    }
    
    // Services
    fun onExperienceYearsChange(value: String) {
        _uiState.value = _uiState.value.copy(experienceYears = value)
    }
    
    fun onPriceMinChange(value: String) {
        _uiState.value = _uiState.value.copy(priceMin = value)
    }
    
    fun onPriceMaxChange(value: String) {
        _uiState.value = _uiState.value.copy(priceMax = value)
    }
    
    // Jobs
    fun onSalaryMinChange(value: String) {
        _uiState.value = _uiState.value.copy(salaryMin = value)
    }
    
    fun onSalaryMaxChange(value: String) {
        _uiState.value = _uiState.value.copy(salaryMax = value)
    }
    
    fun onSalaryPeriodChange(value: String) {
        _uiState.value = _uiState.value.copy(salaryPeriod = value)
    }
    
    fun onEmploymentTypeChange(value: String) {
        _uiState.value = _uiState.value.copy(employmentType = value)
    }
    
    fun onRemoteOptionChange(value: String) {
        _uiState.value = _uiState.value.copy(remoteOption = value)
    }
    
    fun onVacanciesChange(value: String) {
        _uiState.value = _uiState.value.copy(vacancies = value)
    }
    
    fun onExperienceRequiredChange(value: String) {
        _uiState.value = _uiState.value.copy(experienceRequired = value)
    }
    
    fun onEducationRequiredChange(value: String) {
        _uiState.value = _uiState.value.copy(educationRequired = value)
    }
    
    // Selling
    fun onConditionChange(value: String) {
        _uiState.value = _uiState.value.copy(condition = value)
    }
    
    fun onDiscountedPriceChange(value: String) {
        _uiState.value = _uiState.value.copy(discountedPrice = value)
    }
    
    fun onStockQtyChange(value: String) {
        _uiState.value = _uiState.value.copy(stockQty = value)
    }
    
    fun onSellOnlineChange(value: Boolean) {
        _uiState.value = _uiState.value.copy(sellOnline = value)
    }
    
    // Business
    fun onBusinessNameChange(value: String) {
        _uiState.value = _uiState.value.copy(businessName = value)
    }
    
    fun onIndustryChange(value: String) {
        _uiState.value = _uiState.value.copy(industry = value)
    }
    
    fun onEstablishedYearChange(value: String) {
        _uiState.value = _uiState.value.copy(establishedYear = value)
    }
    
    fun onEmployeeCountChange(value: String) {
        _uiState.value = _uiState.value.copy(employeeCount = value)
    }
    
    /**
     * Submit the listing - either create or update based on mode
     */
    fun submitListing(context: Context) {
        val state = _uiState.value
        val isSellingType = state.listingType == "selling"
        
        // Validation
        if (state.title.isBlank()) {
            _uiState.value = state.copy(error = "Title is required")
            return
        }
        if (state.mode == ListingFormMode.CREATE && state.selectedCategory == null) {
            _uiState.value = state.copy(error = "Please select a category")
            return
        }
        // City and location are NOT required for selling type only (services and jobs still need city)
        val skipLocationFields = isSellingType
        if (!skipLocationFields && state.selectedCity == null) {
            _uiState.value = state.copy(error = "Please select a city")
            return
        }
        
        viewModelScope.launch {
            _uiState.value = state.copy(isSubmitting = true, error = null)
            
            try {
                // Prepare image if selected (compressed to WebP)
                var imagePart: MultipartBody.Part? = null
                state.selectedImageUri?.let { uri ->
                    val file = uriToFile(context, uri)
                    if (file != null) {
                        val mimeType = if (file.extension == "webp") "image/webp" else "image/jpeg"
                        val requestFile = file.asRequestBody(mimeType.toMediaTypeOrNull())
                        imagePart = MultipartBody.Part.createFormData("main_image", file.name, requestFile)
                    }
                }
                
                // Build request body
                val requestMap = mutableMapOf<String, okhttp3.RequestBody>()
                
                fun addField(key: String, value: String) {
                    if (value.isNotBlank()) {
                        requestMap[key] = value.toRequestBody("text/plain".toMediaTypeOrNull())
                    }
                }
                
                // Common fields
                addField("listing_type", state.listingType)
                addField("title", state.title)
                addField("description", state.description)
                state.selectedCategory?.let { addField("category_id", it.categoryId.toString()) }
                state.selectedSubcategory?.let { addField("subcategory_id", it.categoryId.toString()) }
                
                // Location fields - only for non-selling and non-services types
                if (!skipLocationFields) {
                    addField("location", state.location)
                    addField("city", state.selectedCity?.name ?: "Hingoli")
                    addField("state", "Maharashtra")
                }
                
                // Price - NOT sent for business listings (no price column in listings table)
                if (state.price.isNotBlank() && state.listingType != "business") {
                    addField("price", state.price)
                }
                
                // For edit mode, add user_id and status
                if (state.mode == ListingFormMode.EDIT) {
                    addField("status", state.status)
                } else {
                    // For create mode, get user_id from token manager
                    val userId = tokenManager.getUserId()
                    if (userId != null) {
                        addField("user_id", userId.toString())
                    }
                }
                
                // Type-specific fields
                when (state.listingType) {
                    "services" -> {
                        addField("experience_years", state.experienceYears)
                        addField("price_min", state.priceMin)
                        addField("price_max", state.priceMax)
                    }
                    "jobs" -> {
                        addField("salary_min", state.salaryMin)
                        addField("salary_max", state.salaryMax)
                        addField("salary_period", state.salaryPeriod)
                        addField("employment_type", state.employmentType)
                        addField("remote_option", state.remoteOption)
                        addField("vacancies", state.vacancies)
                        addField("experience_required", state.experienceRequired)
                        addField("education_required", state.educationRequired)
                    }
                    "selling" -> {
                        addField("condition", state.condition)
                        if (state.discountedPrice.isNotBlank()) {
                            addField("discounted_price", state.discountedPrice)
                        }
                        if (state.stockQty.isNotBlank()) {
                            addField("stock_qty", state.stockQty)
                        }
                        addField("sell_online", if (state.sellOnline) "1" else "0")
                    }
                    "business" -> {
                        // Use title as business name (they're the same field now)
                        addField("business_name", state.title)
                        addField("industry", state.industry)
                        addField("established_year", state.establishedYear)
                        addField("employee_count", state.employeeCount)
                    }
                }
                
                // Make API call based on mode
                val response = if (state.mode == ListingFormMode.EDIT && state.listingId != null) {
                    apiService.updateListing(state.listingId, requestMap, imagePart)
                } else {
                    apiService.createListing(requestMap, imagePart)
                }
                
                if (response.isSuccessful && response.body()?.success == true) {
                    val successMsg = response.body()?.message ?: "Success"
                    _uiState.value = _uiState.value.copy(
                        isSubmitting = false,
                        isSuccess = true,
                        successMessage = successMsg
                    )
                } else {
                    // Get more detailed error info
                    val errorMessage = response.body()?.message 
                        ?: response.errorBody()?.string()
                        ?: "Failed to save listing (HTTP ${response.code()})"
                    _uiState.value = _uiState.value.copy(
                        isSubmitting = false,
                        error = errorMessage
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isSubmitting = false,
                    error = e.message ?: "Failed to save listing"
                )
            }
        }
    }
    
    /**
     * Compress image from URI before upload
     * Converts to WebP format and resizes to max 1920x1080
     */
    private fun uriToFile(context: Context, uri: Uri): File? {
        return com.hingoli.hub.util.ImageCompressor.compressImage(
            context = context,
            imageUri = uri,
            maxWidth = 1920,
            maxHeight = 1080,
            quality = 80,
            useWebP = true
        )
    }
}
