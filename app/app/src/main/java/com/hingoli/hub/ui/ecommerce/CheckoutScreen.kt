package com.hingoli.hub.ui.ecommerce

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.hingoli.hub.data.model.UserAddress
import com.hingoli.hub.ui.components.LoadingView
import com.hingoli.hub.ui.components.ShimmerListScreen
import com.hingoli.hub.ui.theme.Primary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutScreen(
    onBack: () -> Unit,
    onOrderSuccess: (orderId: Long, orderNumber: String) -> Unit,
    onRazorpayPayment: (orderId: Long, razorpayOrderId: String, amount: Double, onSuccess: (paymentId: String, signature: String) -> Unit) -> Unit,
    onTermsClick: () -> Unit = {},
    onRefundClick: () -> Unit = {},
    viewModel: CheckoutViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var termsAccepted by remember { mutableStateOf(false) }
    
    // Show error toast
    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearError()
        }
    }
    
    // Handle order success for COD
    LaunchedEffect(uiState.orderResult) {
        val result = uiState.orderResult
        if (result != null && uiState.paymentMethod == "cod") {
            onOrderSuccess(result.orderId, result.orderNumber)
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Checkout") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )
        },
        bottomBar = {
            if (!uiState.isLoading && uiState.cartItems.isNotEmpty()) {
                CheckoutBottomBar(
                    total = uiState.cartTotal,
                    isPlacingOrder = uiState.isPlacingOrder,
                    termsAccepted = termsAccepted,
                    onPlaceOrder = {
                        viewModel.placeOrder { orderId, razorpayOrderId, amount ->
                            onRazorpayPayment(orderId, razorpayOrderId, amount) { paymentId, signature ->
                                viewModel.verifyPayment(orderId, paymentId, signature) {
                                    uiState.orderResult?.let { result ->
                                        onOrderSuccess(result.orderId, result.orderNumber)
                                    }
                                }
                            }
                        }
                    }
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                uiState.isLoading -> ShimmerListScreen()
                else -> {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Delivery Address Section
                        item {
                            AddressSection(
                                addresses = uiState.addresses,
                                selectedAddressId = uiState.selectedAddressId,
                                onAddressSelect = viewModel::selectAddress,
                                onAddAddress = viewModel::toggleAddAddress
                            )
                        }
                        
                        // Payment Method Section
                        item {
                            PaymentMethodSection(
                                selectedMethod = uiState.paymentMethod,
                                onMethodSelect = viewModel::setPaymentMethod
                            )
                        }
                        
                        // Order Summary Section
                        item {
                            OrderSummarySection(
                                itemCount = uiState.cartItems.size,
                                subtotal = uiState.cartTotal,
                                shippingFee = 0.0,
                                total = uiState.cartTotal
                            )
                        }
                        
                        // Terms and Refund Policy Checkbox
                        item {
                            TermsCheckboxSection(
                                checked = termsAccepted,
                                onCheckedChange = { termsAccepted = it },
                                onTermsClick = onTermsClick,
                                onRefundClick = onRefundClick
                            )
                        }
                        
                        // Bottom padding
                        item {
                            Spacer(modifier = Modifier.height(80.dp))
                        }
                    }
                }
            }
        }
    }
    
    // Add Address Dialog
    if (uiState.showAddAddress) {
        AddAddressDialog(
            onDismiss = viewModel::toggleAddAddress,
            onSave = viewModel::addAddress
        )
    }
}

@Composable
private fun AddressSection(
    addresses: List<UserAddress>,
    selectedAddressId: Long?,
    onAddressSelect: (Long) -> Unit,
    onAddAddress: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Delivery Address",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                TextButton(onClick = onAddAddress) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add New")
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            if (addresses.isEmpty()) {
                Text(
                    text = "No addresses saved. Please add a delivery address.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
            } else {
                addresses.forEach { address ->
                    AddressCard(
                        address = address,
                        isSelected = address.addressId == selectedAddressId,
                        onSelect = { onAddressSelect(address.addressId) }
                    )
                    if (address != addresses.last()) {
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun AddressCard(
    address: UserAddress,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect() },
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = if (isSelected) Primary else Color.LightGray
        )
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            RadioButton(
                selected = isSelected,
                onClick = onSelect,
                colors = RadioButtonDefaults.colors(selectedColor = Primary)
            )
            
            Spacer(modifier = Modifier.width(8.dp))
            
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = address.name,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                    if (address.isDefault) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            color = Primary.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = "DEFAULT",
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = Primary
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = address.phone,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
                
                Spacer(modifier = Modifier.height(2.dp))
                
                Text(
                    text = address.fullAddress,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.DarkGray
                )
            }
        }
    }
}

@Composable
private fun PaymentMethodSection(
    selectedMethod: String,
    onMethodSelect: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Payment Method",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Razorpay Option
            PaymentOption(
                title = "Pay Online",
                subtitle = "UPI, Debit/Credit Card, Net Banking",
                icon = Icons.Default.CreditCard,
                isSelected = selectedMethod == "razorpay",
                onSelect = { onMethodSelect("razorpay") }
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // COD Option
            PaymentOption(
                title = "Cash on Delivery",
                subtitle = "Pay when you receive",
                icon = Icons.Default.Money,
                isSelected = selectedMethod == "cod",
                onSelect = { onMethodSelect("cod") }
            )
        }
    }
}

@Composable
private fun PaymentOption(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect() },
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = if (isSelected) Primary else Color.LightGray
        )
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = isSelected,
                onClick = onSelect,
                colors = RadioButtonDefaults.colors(selectedColor = Primary)
            )
            
            Spacer(modifier = Modifier.width(8.dp))
            
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) Primary else Color.Gray
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
        }
    }
}

@Composable
private fun OrderSummarySection(
    itemCount: Int,
    subtotal: Double,
    shippingFee: Double,
    total: Double
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Order Summary",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            SummaryRow("Items ($itemCount)", "₹${String.format("%,.0f", subtotal)}")
            SummaryRow("Shipping", if (shippingFee == 0.0) "FREE" else "₹${String.format("%,.0f", shippingFee)}")
            
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Total",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "₹${String.format("%,.0f", total)}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Primary
                )
            }
        }
    }
}

@Composable
private fun SummaryRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = if (value == "FREE") Color(0xFF4CAF50) else Color.DarkGray
        )
    }
}

@Composable
private fun TermsCheckboxSection(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    onTermsClick: () -> Unit = {},
    onRefundClick: () -> Unit = {}
) {
    val annotatedString = buildAnnotatedString {
        append("I agree to the ")
        
        pushStringAnnotation(tag = "terms", annotation = "terms")
        withStyle(style = SpanStyle(color = Primary, fontWeight = FontWeight.Medium)) {
            append("Terms & Conditions")
        }
        pop()
        
        append(" and ")
        
        pushStringAnnotation(tag = "refund", annotation = "refund")
        withStyle(style = SpanStyle(color = Primary, fontWeight = FontWeight.Medium)) {
            append("Refund Policy")
        }
        pop()
    }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = CheckboxDefaults.colors(checkedColor = Primary)
            )
            
            Spacer(modifier = Modifier.width(8.dp))
            
            ClickableText(
                text = annotatedString,
                style = MaterialTheme.typography.bodyMedium.copy(color = Color.DarkGray),
                onClick = { offset ->
                    annotatedString.getStringAnnotations(tag = "terms", start = offset, end = offset)
                        .firstOrNull()?.let {
                            onTermsClick()
                        }
                    annotatedString.getStringAnnotations(tag = "refund", start = offset, end = offset)
                        .firstOrNull()?.let {
                            onRefundClick()
                        }
                }
            )
        }
    }
}

@Composable
private fun CheckoutBottomBar(
    total: Double,
    isPlacingOrder: Boolean,
    termsAccepted: Boolean,
    onPlaceOrder: () -> Unit
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
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Total",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
                Text(
                    text = "₹${String.format("%,.0f", total)}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Primary
                )
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            Button(
                onClick = onPlaceOrder,
                modifier = Modifier.height(48.dp),
                enabled = !isPlacingOrder && termsAccepted,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Primary)
            ) {
                if (isPlacingOrder) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Place Order", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddAddressDialog(
    onDismiss: () -> Unit,
    onSave: (name: String, phone: String, addressLine1: String, addressLine2: String, city: String, pincode: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var addressLine1 by remember { mutableStateOf("") }
    var addressLine2 by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("Hingoli") }
    var pincode by remember { mutableStateOf("") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add New Address") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Full Name *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Phone *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                OutlinedTextField(
                    value = addressLine1,
                    onValueChange = { addressLine1 = it },
                    label = { Text("Address Line 1 *") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                OutlinedTextField(
                    value = addressLine2,
                    onValueChange = { addressLine2 = it },
                    label = { Text("Address Line 2") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = city,
                        onValueChange = { city = it },
                        label = { Text("City *") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    
                    OutlinedTextField(
                        value = pincode,
                        onValueChange = { pincode = it },
                        label = { Text("Pincode *") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank() && phone.isNotBlank() && addressLine1.isNotBlank() && city.isNotBlank() && pincode.isNotBlank()) {
                        onSave(name, phone, addressLine1, addressLine2, city, pincode)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Primary)
            ) {
                Text("Save Address")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
