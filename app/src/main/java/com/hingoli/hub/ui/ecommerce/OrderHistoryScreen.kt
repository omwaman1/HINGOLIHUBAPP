package com.hingoli.hub.ui.ecommerce

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.hingoli.hub.data.model.Order
import com.hingoli.hub.ui.components.EmptyView
import com.hingoli.hub.ui.components.LoadingView
import com.hingoli.hub.ui.components.ShimmerListScreen
import com.hingoli.hub.ui.theme.Primary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderHistoryScreen(
    onBack: () -> Unit,
    onOrderClick: (Long) -> Unit,
    viewModel: OrderHistoryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Orders") },
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
        ) {
            when {
                uiState.isLoading -> ShimmerListScreen()
                uiState.error != null -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(uiState.error!!, color = Color.Red)
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = viewModel::loadOrders) {
                            Text("Retry")
                        }
                    }
                }
                uiState.orders.isEmpty() -> {
                    EmptyView(
                        message = "No orders yet"
                    )
                }
                else -> {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(uiState.orders, key = { it.orderId }) { order ->
                            OrderCard(
                                order = order,
                                onClick = { onOrderClick(order.orderId) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun OrderCard(
    order: Order,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Order Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Order #${order.orderNumber}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = order.createdAt.take(10), // Date only
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
                
                OrderStatusChip(status = order.orderStatus)
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Delivery Time Row - Use API-provided message if available
            val deliveryText = order.deliveryMessage ?: getDeliveryText(order.orderStatus)
            Surface(
                color = Color(0xFFF5F5F5),
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.LocalShipping,
                        contentDescription = null,
                        tint = when (order.orderStatus.lowercase()) {
                            "delivered" -> Color(0xFF4CAF50)
                            "out_for_delivery" -> Color(0xFF2196F3)
                            "cancelled" -> Color(0xFFF44336)
                            else -> Color(0xFFFF9800)
                        },
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = deliveryText,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = Color.DarkGray
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(12.dp))
            
            // Order Details
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "${order.itemCount} item${if (order.itemCount > 1) "s" else ""}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = order.paymentMethod.uppercase(),
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
                
                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = "₹${String.format("%,.0f", order.totalAmount)}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Primary
                    )
                    PaymentStatusChip(status = order.paymentStatus)
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // View Details
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onClick) {
                    Text("View Details")
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        Icons.Default.ChevronRight,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

private fun getDeliveryText(status: String): String {
    return when (status.lowercase()) {
        "delivered" -> "Delivered"
        "out_for_delivery" -> "Arriving today"
        "shipped", "dispatched", "accepted" -> "Arriving in 1-2 days"
        "processing", "waiting_to_dispatch" -> "Being prepared for shipping"
        "confirmed" -> "Order confirmed, preparing soon"
        "pending" -> "Waiting for confirmation"
        "cancelled" -> "Order Cancelled"
        else -> "Arriving in 2-4 days"
    }
}

@Composable
private fun OrderStatusChip(status: String) {
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
private fun PaymentStatusChip(status: String) {
    val (icon, color) = when (status) {
        "paid" -> Pair(Icons.Default.CheckCircle, Color(0xFF4CAF50))
        "pending" -> Pair(Icons.Default.Schedule, Color(0xFFFFC107))
        "failed" -> Pair(Icons.Default.Error, Color(0xFFF44336))
        else -> Pair(Icons.Default.Help, Color.Gray)
    }
    
    Row(
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
            text = status.replaceFirstChar { it.uppercase() },
            style = MaterialTheme.typography.labelSmall,
            color = color
        )
    }
}
