package com.hingoli.hub.ui.product

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.hingoli.hub.ui.theme.PrimaryBlue

import androidx.compose.ui.platform.LocalContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductFormScreen(
    productId: Long,
    onBackClick: () -> Unit,
    onSuccess: () -> Unit,
    viewModel: ProductFormViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    
    // Image picker launcher
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.onImageSelected(it) }
    }
    
    // Load product on first composition
    LaunchedEffect(productId) {
        if (productId > 0) {
            viewModel.loadProduct(productId)
        }
    }
    
    // Handle success - show toast and navigate back
    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            android.widget.Toast.makeText(context, "Product updated successfully", android.widget.Toast.LENGTH_SHORT).show()
            onSuccess()
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Product", fontWeight = FontWeight.Bold) },
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
        ) {
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = PrimaryBlue
                    )
                }
                else -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Product Image with Edit capability
                        Text("Product Image", style = MaterialTheme.typography.labelLarge)
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                                .clickable { imagePickerLauncher.launch("image/*") },
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                // Show selected image URI or existing image URL
                                val imageModel = uiState.selectedImageUri ?: uiState.imageUrl.takeIf { it.isNotBlank() }
                                
                                if (imageModel != null) {
                                    AsyncImage(
                                        model = imageModel,
                                        contentDescription = "Product Image",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                    // Overlay for edit hint
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(Color.Black.copy(alpha = 0.3f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Icon(
                                                Icons.Default.CameraAlt,
                                                contentDescription = "Change Image",
                                                tint = Color.White,
                                                modifier = Modifier.size(32.dp)
                                            )
                                            Text(
                                                "Tap to change",
                                                color = Color.White,
                                                style = MaterialTheme.typography.bodySmall
                                            )
                                        }
                                    }
                                } else {
                                    // No image - show placeholder
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Icon(
                                            Icons.Default.CameraAlt,
                                            contentDescription = "Add Image",
                                            tint = Color.Gray,
                                            modifier = Modifier.size(48.dp)
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            "Tap to add image",
                                            color = Color.Gray,
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                    }
                                }
                            }
                        }
                        
                        // Product Name
                        OutlinedTextField(
                            value = uiState.productName,
                            onValueChange = viewModel::onProductNameChange,
                            label = { Text("Product Name *") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        
                        // Description
                        OutlinedTextField(
                            value = uiState.description,
                            onValueChange = viewModel::onDescriptionChange,
                            label = { Text("Description") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 3,
                            maxLines = 5
                        )
                        
                        // Price Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedTextField(
                                value = uiState.price,
                                onValueChange = viewModel::onPriceChange,
                                label = { Text("Price (₹) *") },
                                modifier = Modifier.weight(1f),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true
                            )
                            
                            OutlinedTextField(
                                value = uiState.discountedPrice,
                                onValueChange = viewModel::onDiscountedPriceChange,
                                label = { Text("MRP (₹)") },
                                modifier = Modifier.weight(1f),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true
                            )
                        }
                        
                        // Stock Quantity
                        OutlinedTextField(
                            value = uiState.stockQty,
                            onValueChange = viewModel::onStockQtyChange,
                            label = { Text("Stock Quantity") },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true
                        )
                        
                        // Main Category Dropdown
                        var categoryExpanded by remember { mutableStateOf(false) }
                        val selectedCategory = uiState.categories.find { it.categoryId == uiState.categoryId }
                        
                        ExposedDropdownMenuBox(
                            expanded = categoryExpanded,
                            onExpandedChange = { categoryExpanded = !categoryExpanded }
                        ) {
                            OutlinedTextField(
                                value = selectedCategory?.name ?: "Select Category",
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Main Category") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor()
                            )
                            ExposedDropdownMenu(
                                expanded = categoryExpanded,
                                onDismissRequest = { categoryExpanded = false }
                            ) {
                                uiState.categories.forEach { category ->
                                    DropdownMenuItem(
                                        text = { Text(category.name) },
                                        onClick = {
                                            viewModel.onCategoryChange(category.categoryId)
                                            categoryExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                        
                        // Subcategory Dropdown (only show if subcategories available)
                        if (uiState.subcategories.isNotEmpty()) {
                            var subcategoryExpanded by remember { mutableStateOf(false) }
                            val selectedSubcategory = uiState.subcategories.find { it.categoryId == uiState.subcategoryId }
                            
                            ExposedDropdownMenuBox(
                                expanded = subcategoryExpanded,
                                onExpandedChange = { subcategoryExpanded = !subcategoryExpanded }
                            ) {
                                OutlinedTextField(
                                    value = selectedSubcategory?.name ?: "Select Subcategory",
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("Subcategory") },
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = subcategoryExpanded) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .menuAnchor()
                                )
                                ExposedDropdownMenu(
                                    expanded = subcategoryExpanded,
                                    onDismissRequest = { subcategoryExpanded = false }
                                ) {
                                    uiState.subcategories.forEach { subcategory ->
                                        DropdownMenuItem(
                                            text = { Text(subcategory.name) },
                                            onClick = {
                                                viewModel.onSubcategoryChange(subcategory.categoryId)
                                                subcategoryExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                        
                        // Condition
                        Text("Condition", style = MaterialTheme.typography.labelLarge)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            FilterChip(
                                selected = uiState.condition == "new",
                                onClick = { viewModel.onConditionChange("new") },
                                label = { Text("New") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = PrimaryBlue,
                                    selectedLabelColor = Color.White
                                )
                            )
                            FilterChip(
                                selected = uiState.condition == "old",
                                onClick = { viewModel.onConditionChange("old") },
                                label = { Text("Used") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = PrimaryBlue,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                        
                        // Sell Online Toggle - Fixed colors
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
                                        if (uiState.sellOnline) "Product can be purchased online" else "Display only, not for sale",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.Gray
                                    )
                                }
                                Switch(
                                    checked = uiState.sellOnline,
                                    onCheckedChange = viewModel::onSellOnlineChange,
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Color.White,
                                        checkedTrackColor = PrimaryBlue,
                                        uncheckedThumbColor = Color.White,
                                        uncheckedTrackColor = Color.Gray
                                    )
                                )
                            }
                        }
                        
                        // Error Message
                        if (uiState.error != null) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.errorContainer
                                )
                            ) {
                                Text(
                                    text = uiState.error!!,
                                    modifier = Modifier.padding(16.dp),
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                            }
                        }
                        
                        // Save Button
                        Button(
                            onClick = viewModel::saveProduct,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            enabled = !uiState.isSaving,
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                        ) {
                            if (uiState.isSaving) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text("Save Changes", fontWeight = FontWeight.Bold)
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(32.dp))
                    }
                }
            }
        }
    }
}
