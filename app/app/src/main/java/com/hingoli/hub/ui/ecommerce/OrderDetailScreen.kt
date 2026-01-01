package com.hingoli.hub.ui.ecommerce

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.hingoli.hub.data.model.OrderDetail
import com.hingoli.hub.data.model.OrderItem
import com.hingoli.hub.ui.components.LoadingView
import com.hingoli.hub.ui.components.ShimmerDetailScreen
import com.hingoli.hub.ui.theme.Primary
import com.hingoli.hub.ui.theme.PrimaryBlue
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderDetailScreen(
    orderId: Long,
    onBack: () -> Unit,
    viewModel: OrderDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    LaunchedEffect(orderId) {
        viewModel.loadOrderDetail(orderId)
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Order Details") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFFF5F5F5))
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
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { viewModel.loadOrderDetail(orderId) }) {
                            Text("Retry")
                        }
                    }
                }
                uiState.orderDetail != null -> {
                    OrderDetailContent(orderDetail = uiState.orderDetail!!)
                }
            }
        }
    }
}

@Composable
private fun OrderDetailContent(orderDetail: OrderDetail) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Order Status Card
        item {
            OrderStatusCard(orderDetail = orderDetail)
        }
        
        // Delivery Time Card
        item {
            DeliveryTimeCard(orderDetail = orderDetail)
        }
        
        // Items Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Order Items (${orderDetail.items.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }
        
        // Order Items
        items(orderDetail.items) { item ->
            OrderItemCard(item = item)
        }
        
        // Price Summary Card
        item {
            PriceSummaryCard(orderDetail = orderDetail)
        }
        
        // Delivery Address Card
        item {
            DeliveryAddressCard(orderDetail = orderDetail)
        }
        
        // Payment Info Card
        item {
            PaymentInfoCard(orderDetail = orderDetail)
        }
        
        // Bottom Spacing
        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun OrderStatusCard(orderDetail: OrderDetail) {
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
                Column {
                    Text(
                        text = "Order #${orderDetail.orderNumber}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = formatDate(orderDetail.createdAt),
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
                EnhancedOrderStatusChip(status = orderDetail.orderStatus)
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(16.dp))
            
            // Order Progress Tracker
            OrderProgressTracker(status = orderDetail.orderStatus)
        }
    }
}

@Composable
private fun OrderProgressTracker(status: String) {
    // Updated to match admin panel statuses
    val steps = listOf(
        "Pending" to "pending",
        "Confirmed" to "confirmed", 
        "Processing" to "processing",
        "Shipped" to "shipped",
        "Out for Delivery" to "out_for_delivery",
        "Delivered" to "delivered"
    )
    
    val currentStepIndex = steps.indexOfFirst { it.second == status.lowercase() }
        .takeIf { it >= 0 } ?: when (status.lowercase()) {
            "dispatched" -> 3  // dispatched = shipped
            "accepted" -> 3    // delivery boy accepted = shipped
            "waiting_to_dispatch" -> 2  // waiting = processing
            "cancelled" -> -1
            else -> 0
        }
    
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        steps.forEachIndexed { index, (label, _) ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f)
            ) {
                val isCompleted = index <= currentStepIndex
                val isCurrent = index == currentStepIndex
                
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .background(
                            color = when {
                                isCompleted -> Color(0xFF4CAF50)
                                else -> Color(0xFFE0E0E0)
                            },
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (isCompleted) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = label.split(" ").take(2).joinToString("\n"),
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isCompleted) Color(0xFF4CAF50) else Color.Gray,
                    fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun DeliveryTimeCard(orderDetail: OrderDetail) {
    // Use API-provided message if available, otherwise calculate based on status
    val estimatedDelivery = if (orderDetail.deliveryMessage != null) {
        orderDetail.deliveryMessage to getDeliverySubtext(orderDetail.orderStatus)
    } else {
        getEstimatedDeliveryText(orderDetail.orderStatus)
    }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Outlined.LocalShipping,
                contentDescription = null,
                tint = Color(0xFF2E7D32),
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = estimatedDelivery.first,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2E7D32)
                )
                Text(
                    text = estimatedDelivery.second,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF4CAF50)
                )
            }
        }
    }
}

private fun getDeliverySubtext(status: String): String {
    return when (status.lowercase()) {
        "delivered" -> "Your order has been delivered"
        "out_for_delivery" -> "Your order is out for delivery"
        "shipped", "dispatched" -> "Your order has been shipped"
        "accepted" -> "A delivery partner has been assigned"
        "processing", "waiting_to_dispatch" -> "Your order is being prepared"
        "confirmed" -> "Your order has been confirmed"
        "pending" -> "Waiting for order confirmation"
        "cancelled" -> "This order has been cancelled"
        else -> "Your order is being processed"
    }
}

private fun getEstimatedDeliveryText(status: String): Pair<String, String> {
    return when (status.lowercase()) {
        "delivered" -> "Delivered" to "Your order has been delivered"
        "out_for_delivery" -> "Arriving today" to "Your order is out for delivery"
        "shipped", "dispatched" -> "Arriving in 1-2 days" to "Your order has been shipped"
        "accepted" -> "Arriving today" to "Delivery partner has been assigned"
        "processing", "waiting_to_dispatch" -> "Arriving in 2-3 days" to "Your order is being prepared"
        "confirmed" -> "Arriving in 3-4 days" to "Your order has been confirmed"
        "pending" -> "Arriving in 4-5 days" to "Waiting for order confirmation"
        "cancelled" -> "Order Cancelled" to "This order has been cancelled"
        else -> "Arriving in 2-4 days" to "Your order is being processed"
    }
}

@Composable
private fun OrderItemCard(item: OrderItem) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Product Image
            AsyncImage(
                model = item.mainImageUrl,
                contentDescription = item.title,
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Seller: ${item.sellerName}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "₹${String.format("%,.0f", item.price)} × ${item.quantity}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                    Text(
                        text = "₹${String.format("%,.0f", item.price * item.quantity)}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Primary
                    )
                }
            }
        }
    }
}

@Composable
private fun PriceSummaryCard(orderDetail: OrderDetail) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Price Details",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            PriceRow("Subtotal", orderDetail.subtotal)
            PriceRow("Shipping Fee", orderDetail.shippingFee)
            
            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Total Amount",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "₹${String.format("%,.0f", orderDetail.totalAmount)}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Primary
                )
            }
        }
    }
}

@Composable
private fun PriceRow(label: String, amount: Double) {
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
            text = if (amount > 0) "₹${String.format("%,.0f", amount)}" else "FREE",
            style = MaterialTheme.typography.bodyMedium,
            color = if (amount > 0) Color.Black else Color(0xFF4CAF50)
        )
    }
}

@Composable
private fun DeliveryAddressCard(orderDetail: OrderDetail) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.LocationOn,
                    contentDescription = null,
                    tint = PrimaryBlue
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Delivery Address",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = orderDetail.address.name,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = orderDetail.address.phone,
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = buildString {
                    append(orderDetail.address.addressLine1)
                    orderDetail.address.addressLine2?.let { append(", $it") }
                    append("\n${orderDetail.address.city}, ${orderDetail.address.state} - ${orderDetail.address.pincode}")
                },
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )
        }
    }
}

@Composable
private fun PaymentInfoCard(orderDetail: OrderDetail) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.Payment,
                    contentDescription = null,
                    tint = PrimaryBlue
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Payment Information",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Payment Method",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
                Text(
                    text = formatPaymentMethod(orderDetail.paymentMethod),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Payment Status",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
                EnhancedPaymentStatusChip(status = orderDetail.paymentStatus)
            }
        }
    }
}

@Composable
fun EnhancedOrderStatusChip(status: String) {
    val (backgroundColor, textColor, label) = when (status.lowercase()) {
        "delivered" -> Triple(Color(0xFFE8F5E9), Color(0xFF2E7D32), "Delivered")
        "out_for_delivery" -> Triple(Color(0xFFE3F2FD), Color(0xFF1565C0), "Out for Delivery")
        "shipped", "dispatched" -> Triple(Color(0xFFE3F2FD), Color(0xFF1565C0), "Shipped")
        "accepted" -> Triple(Color(0xFFE3F2FD), Color(0xFF1565C0), "Delivery Partner Assigned")
        "processing", "waiting_to_dispatch" -> Triple(Color(0xFFFFF3E0), Color(0xFFEF6C00), "Processing")
        "confirmed" -> Triple(Color(0xFFFFF3E0), Color(0xFFEF6C00), "Confirmed")
        "pending" -> Triple(Color(0xFFFCE4EC), Color(0xFFAD1457), "Pending")
        "cancelled" -> Triple(Color(0xFFFFEBEE), Color(0xFFC62828), "Cancelled")
        else -> Triple(Color(0xFFF5F5F5), Color.Gray, status.replaceFirstChar { it.uppercase() })
    }
    
    Surface(
        color = backgroundColor,
        shape = RoundedCornerShape(16.dp)
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelMedium,
            color = textColor,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun EnhancedPaymentStatusChip(status: String) {
    val (icon, color, label) = when (status.lowercase()) {
        "paid" -> Triple(Icons.Default.CheckCircle, Color(0xFF4CAF50), "Paid")
        "pending" -> Triple(Icons.Default.Schedule, Color(0xFFFFC107), "Pending")
        "failed" -> Triple(Icons.Default.Error, Color(0xFFF44336), "Failed")
        "refunded" -> Triple(Icons.Default.Refresh, Color(0xFF2196F3), "Refunded")
        else -> Triple(Icons.Default.Help, Color.Gray, status.replaceFirstChar { it.uppercase() })
    }
    
    Surface(
        color = color.copy(alpha = 0.1f),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(14.dp),
                tint = color
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = color,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

private fun formatPaymentMethod(method: String): String {
    return when (method.lowercase()) {
        "razorpay" -> "Online Payment"
        "cod" -> "Cash on Delivery"
        else -> method.replaceFirstChar { it.uppercase() }
    }
}

private fun formatDate(dateString: String): String {
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        val outputFormat = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
        val date = inputFormat.parse(dateString)
        date?.let { outputFormat.format(it) } ?: dateString
    } catch (e: Exception) {
        dateString.take(10)
    }
}
