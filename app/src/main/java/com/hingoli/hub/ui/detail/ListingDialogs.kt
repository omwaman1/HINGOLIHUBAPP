package com.hingoli.hub.ui.detail

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.hingoli.hub.data.model.Category
import com.hingoli.hub.ui.theme.*

/**
 * Dialogs extracted from ListingDetailScreen for better code organization.
 * These dialogs are used by listing owners to manage their listings.
 */

// ==================== ADD PRICE ITEM DIALOG ====================

@Composable
fun AddPriceItemDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, price: Double, description: String?) -> Unit,
    isMarathi: Boolean = false
) {
    var name by remember { mutableStateOf("") }
    var priceText by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(Strings.get("Add Price Item", "किंमत आयटम जोडा", isMarathi), fontWeight = FontWeight.SemiBold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(Strings.get("Item Name *", "आयटमचे नाव *", isMarathi)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = priceText,
                    onValueChange = { priceText = it.filter { c -> c.isDigit() || c == '.' } },
                    label = { Text(Strings.get("Price (₹) *", "किंमत (₹) *", isMarathi)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text(Strings.get("Description (optional)", "वर्णन (ऐच्छिक)", isMarathi)) },
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
                Text(Strings.add(isMarathi))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(Strings.cancel(isMarathi))
            }
        }
    )
}

// ==================== EDIT DESCRIPTION DIALOG ====================

@Composable
fun EditDescriptionDialog(
    currentDescription: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
    isMarathi: Boolean = false
) {
    var description by remember { mutableStateOf(currentDescription) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(Strings.get("Edit Description", "वर्णन संपादित करा", isMarathi), fontWeight = FontWeight.SemiBold) },
        text = {
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text(Strings.get("Description", "वर्णन", isMarathi)) },
                minLines = 4,
                maxLines = 8,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            Button(onClick = { onConfirm(description.trim()) }) {
                Text(Strings.save(isMarathi))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(Strings.cancel(isMarathi))
            }
        }
    )
}

// ==================== ADD REVIEW DIALOG ====================

@Composable
fun AddReviewDialog(
    onDismiss: () -> Unit,
    onConfirm: (rating: Int, title: String?, content: String?) -> Unit,
    isMarathi: Boolean = false
) {
    var rating by remember { mutableStateOf(5) }
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(Strings.get("Write a Review", "पुनरावलोकन लिहा", isMarathi), fontWeight = FontWeight.SemiBold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // Star Rating
                Text(
                    text = Strings.get("Your Rating", "तुमचे रेटिंग", isMarathi),
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
                    label = { Text(Strings.get("Title (optional)", "शीर्षक (ऐच्छिक)", isMarathi)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                
                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    label = { Text(Strings.get("Your Review", "तुमचे पुनरावलोकन", isMarathi)) },
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
                Text(Strings.get("Submit Review", "पुनरावलोकन सबमिट करा", isMarathi))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(Strings.cancel(isMarathi))
            }
        }
    )
}

// ==================== ADD PRODUCT REVIEW DIALOG (with Image Picker) ====================

@Composable
fun AddProductReviewDialog(
    onDismiss: () -> Unit,
    onConfirm: (rating: Int, title: String?, content: String?, imageUris: List<Uri>) -> Unit,
    isSubmitting: Boolean = false,
    isMarathi: Boolean = false
) {
    var rating by remember { mutableStateOf(5) }
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var selectedImages by remember { mutableStateOf<List<Uri>>(emptyList()) }
    
    val multipleImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris: List<Uri> ->
        // Limit to 3 images
        selectedImages = (selectedImages + uris).take(3)
    }
    
    AlertDialog(
        onDismissRequest = { if (!isSubmitting) onDismiss() },
        title = { Text(Strings.get("Write a Review", "पुनरावलोकन लिहा", isMarathi), fontWeight = FontWeight.SemiBold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // Star Rating
                Text(
                    text = Strings.get("Your Rating", "तुमचे रेटिंग", isMarathi),
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
                    label = { Text(Strings.get("Title (optional)", "शीर्षक (ऐच्छिक)", isMarathi)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                
                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    label = { Text(Strings.get("Your Review", "तुमचे पुनरावलोकन", isMarathi)) },
                    minLines = 3,
                    maxLines = 5,
                    modifier = Modifier.fillMaxWidth()
                )
                
                // Image picker button
                OutlinedButton(
                    onClick = { multipleImageLauncher.launch("image/*") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, if (selectedImages.isNotEmpty()) AccentGreen else Color(0xFFE2E8F0))
                ) {
                    Icon(
                        Icons.Default.PhotoCamera,
                        contentDescription = null,
                        tint = if (selectedImages.isNotEmpty()) AccentGreen else Color.Gray
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = when {
                            selectedImages.isEmpty() -> Strings.get("Add Photos (up to 3)", "फोटो जोडा (३ पर्यंत)", isMarathi)
                            selectedImages.size == 1 -> Strings.get("1 photo selected ✓", "१ फोटो निवडला ✓", isMarathi)
                            else -> Strings.get("${selectedImages.size} photos selected ✓", "${selectedImages.size} फोटो निवडले ✓", isMarathi)
                        },
                        color = if (selectedImages.isNotEmpty()) AccentGreen else Color.Gray
                    )
                }
                
                // Show selected image count hint
                if (selectedImages.isNotEmpty()) {
                    Text(
                        text = Strings.get("Tap again to add more", "अधिक जोडण्यासाठी पुन्हा टॅप करा", isMarathi),
                        style = MaterialTheme.typography.labelSmall,
                        color = OnSurfaceVariant
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(
                        rating,
                        title.takeIf { it.isNotBlank() },
                        content.takeIf { it.isNotBlank() },
                        selectedImages
                    )
                },
                enabled = rating >= 1 && !isSubmitting
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(Strings.get("Submit Review", "पुनरावलोकन सबमिट करा", isMarathi))
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isSubmitting
            ) {
                Text(Strings.cancel(isMarathi))
            }
        }
    )
}

// ==================== ADD PRODUCT/SERVICE DIALOG ====================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddProductDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, description: String?, price: Double, imageUri: Uri?, condition: String, sellOnline: Boolean, categoryId: Int?, subcategoryId: Int?) -> Unit,
    categories: List<Category> = emptyList(),
    subcategories: List<Category> = emptyList(),
    onCategorySelected: (Int) -> Unit = {},
    isServiceListing: Boolean = false,
    isMarathi: Boolean = false
) {
    val itemLabel = if (isServiceListing) 
        Strings.get("Service", "सेवा", isMarathi) 
    else 
        Strings.get("Product", "वस्तू", isMarathi)
    
    var productName by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var priceText by remember { mutableStateOf("") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var condition by remember { mutableStateOf("new") }
    var sellOnline by remember { mutableStateOf(false) }
    var selectedCategoryId by remember { mutableStateOf<Int?>(null) }
    var selectedSubcategoryId by remember { mutableStateOf<Int?>(null) }
    var categoryDropdownExpanded by remember { mutableStateOf(false) }
    var subcategoryDropdownExpanded by remember { mutableStateOf(false) }
    
    val selectedCategoryName = categories.find { it.categoryId == selectedCategoryId }?.getLocalizedName(isMarathi) ?: ""
    val selectedSubcategoryName = subcategories.find { it.categoryId == selectedSubcategoryId }?.getLocalizedName(isMarathi) ?: ""
    
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedImageUri = uri
    }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.clip(RoundedCornerShape(4.dp)),
        containerColor = Color.White,
        title = { 
            Text(
                text = "${Strings.add(isMarathi)} $itemLabel",
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1E293B)
            ) 
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(Color.White, Color(0xFFF8FAFC))
                        )
                    ),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.heightIn(max = 400.dp)
                ) {
                    item {
                        OutlinedTextField(
                            value = productName,
                            onValueChange = { productName = it },
                            label = { Text("$itemLabel ${Strings.name(isMarathi)} *") },
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
                            label = { Text(Strings.get("Description", "वर्णन", isMarathi)) },
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
                            label = { Text(Strings.get("Price (₹) *", "किंमत (₹) *", isMarathi)) },
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
                    
                    // Condition selector - only show for products, not services
                    if (!isServiceListing) {
                        item {
                            Text(
                                text = Strings.get("Condition", "स्थिती", isMarathi),
                                style = MaterialTheme.typography.labelMedium,
                                color = Color(0xFF64748B)
                            )
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.padding(top = 4.dp)
                            ) {
                                FilterChip(
                                    selected = condition == "new",
                                    onClick = { condition = "new" },
                                    label = { Text(Strings.new(isMarathi)) },
                                    shape = RoundedCornerShape(4.dp),
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = PrimaryBlue,
                                        selectedLabelColor = Color.White
                                    )
                                )
                                FilterChip(
                                    selected = condition == "old",
                                    onClick = { condition = "old" },
                                    label = { Text(Strings.used(isMarathi)) },
                                    shape = RoundedCornerShape(4.dp),
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = PrimaryBlue,
                                        selectedLabelColor = Color.White
                                    )
                                )
                            }
                        }
                        
                        // Sell Online toggle - only for products
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = Strings.sellOnline(isMarathi),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Switch(
                                    checked = sellOnline,
                                    onCheckedChange = { sellOnline = it },
                                    colors = SwitchDefaults.colors(
                                        checkedTrackColor = AccentGreen,
                                        checkedThumbColor = Color.White
                                    )
                                )
                            }
                        }
                    }
                    
                    // Category dropdown
                    if (categories.isNotEmpty()) {
                        item {
                            ExposedDropdownMenuBox(
                                expanded = categoryDropdownExpanded,
                                onExpandedChange = { categoryDropdownExpanded = !categoryDropdownExpanded }
                            ) {
                                OutlinedTextField(
                                    value = selectedCategoryName,
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text(Strings.category(isMarathi)) },
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryDropdownExpanded) },
                                    modifier = Modifier.fillMaxWidth().menuAnchor(),
                                    shape = RoundedCornerShape(4.dp),
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
                                    modifier = Modifier.background(Color.White)
                                ) {
                                    categories.forEach { category ->
                                        DropdownMenuItem(
                                            text = { Text(category.getLocalizedName(isMarathi), color = Color.Black) },
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
                    }
                    
                    // Subcategory dropdown
                    if (subcategories.isNotEmpty() && selectedCategoryId != null) {
                        item {
                            ExposedDropdownMenuBox(
                                expanded = subcategoryDropdownExpanded,
                                onExpandedChange = { subcategoryDropdownExpanded = !subcategoryDropdownExpanded }
                            ) {
                                OutlinedTextField(
                                    value = selectedSubcategoryName,
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text(Strings.subcategory(isMarathi)) },
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = subcategoryDropdownExpanded) },
                                    modifier = Modifier.fillMaxWidth().menuAnchor(),
                                    shape = RoundedCornerShape(4.dp),
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
                                    modifier = Modifier.background(Color.White)
                                ) {
                                    subcategories.forEach { subcategory ->
                                        DropdownMenuItem(
                                            text = { Text(subcategory.getLocalizedName(isMarathi), color = Color.Black) },
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
                                text = if (selectedImageUri != null) 
                                    Strings.get("Image Selected ✓", "इमेज निवडली ✓", isMarathi) 
                                else 
                                    Strings.get("Add Image", "इमेज जोडा", isMarathi),
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
                Text("${Strings.add(isMarathi)} $itemLabel", fontWeight = FontWeight.SemiBold)
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(4.dp)
            ) {
                Text(Strings.cancel(isMarathi), color = Color.Gray)
            }
        },
        shape = RoundedCornerShape(4.dp)
    )
}

// ==================== DELETE CONFIRMATION DIALOG ====================

@Composable
fun DeleteConfirmationDialog(
    title: String,
    message: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    isMarathi: Boolean = false
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, fontWeight = FontWeight.SemiBold) },
        text = { Text(message) },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
            ) {
                Text(Strings.delete(isMarathi))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(Strings.cancel(isMarathi))
            }
        }
    )
}
