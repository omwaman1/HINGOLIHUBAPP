package com.hingoli.delivery.ui.orders

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.hingoli.delivery.data.model.MyDelivery
import com.hingoli.delivery.ui.call.VoiceCallActivity
import com.hingoli.delivery.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyDeliveriesScreen(
    viewModel: OrdersViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val pullToRefreshState = rememberPullToRefreshState()
    
    PullToRefreshBox(
        isRefreshing = uiState.isRefreshing,
        onRefresh = { viewModel.refresh() },
        state = pullToRefreshState,
        modifier = Modifier.fillMaxSize()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "My Deliveries",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "Pull to refresh",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
            
            if (uiState.isLoading && uiState.myDeliveries.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = DeliveryOrange)
                }
            } else if (uiState.myDeliveries.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.DeliveryDining,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = Color.Gray
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "No active deliveries",
                            fontSize = 18.sp,
                            color = Color.Gray
                        )
                        Text(
                            "Accept orders to see them here",
                            fontSize = 14.sp,
                            color = Color.Gray.copy(alpha = 0.7f)
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(uiState.myDeliveries, key = { it.orderId }) { delivery ->
                        DeliveryCard(
                            delivery = delivery,
                            isLoading = uiState.loadingOrderId == delivery.orderId,
                            onUpdateStatus = { status -> viewModel.updateStatus(delivery.orderId, status) },
                            onCancel = { viewModel.cancelOrder(delivery.orderId) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DeliveryCard(
    delivery: MyDelivery,
    isLoading: Boolean,
    onUpdateStatus: (String) -> Unit,
    onCancel: () -> Unit
) {
    val context = LocalContext.current
    
    val statusColor = when (delivery.orderStatus) {
        "accepted" -> Color(0xFF1976D2)
        "out_for_delivery" -> Color(0xFFFF9800)
        "delivered" -> Color(0xFF4CAF50)
        else -> Color.Gray
    }
    
    val statusText = when (delivery.orderStatus) {
        "accepted" -> "Accepted"
        "out_for_delivery" -> "Out for Delivery"
        "delivered" -> "Delivered"
        else -> delivery.orderStatus.replace("_", " ").uppercase()
    }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "#${delivery.orderNumber}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Card(
                    colors = CardDefaults.cardColors(containerColor = statusColor.copy(alpha = 0.1f)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = statusText,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        color = statusColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Customer Info
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Person, null, tint = Color.Gray, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(delivery.customer.name ?: "Customer", fontSize = 14.sp)
                Spacer(modifier = Modifier.width(16.dp))
                Icon(Icons.Default.Phone, null, tint = Color.Gray, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(delivery.customer.phone ?: "", fontSize = 14.sp, color = DeliveryOrange)
            }
            
            Spacer(modifier = Modifier.height(6.dp))
            
            // Address
            val addr = delivery.deliveryAddress ?: delivery.address
            Row(verticalAlignment = Alignment.Top) {
                Icon(Icons.Default.LocationOn, null, tint = Color.Gray, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = listOfNotNull(
                        addr?.line1,
                        addr?.line2,
                        addr?.city,
                        addr?.postalCode
                    ).joinToString(", "),
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }
            
            Spacer(modifier = Modifier.height(6.dp))
            
            // Earnings
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.CurrencyRupee, null, tint = DeliveryOrange, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Earnings: ₹${String.format("%.0f", delivery.deliveryEarnings)}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = DeliveryOrange
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Action Buttons
            when (delivery.orderStatus) {
                "accepted" -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = onCancel,
                            modifier = Modifier.weight(1f),
                            enabled = !isLoading,
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red)
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text("Cancel")
                            }
                        }
                        Button(
                            onClick = { onUpdateStatus("out_for_delivery") },
                            modifier = Modifier.weight(1f),
                            enabled = !isLoading,
                            colors = ButtonDefaults.buttonColors(containerColor = DeliveryOrange)
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text("Picked Up")
                            }
                        }
                    }
                }
                "out_for_delivery" -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                VoiceCallActivity.start(
                                    context = context,
                                    orderId = delivery.orderId,
                                    deliveryUserId = 1,
                                    deliveryUserName = "Delivery Partner",
                                    customerName = delivery.customer.name ?: "Customer",
                                    customerUserId = delivery.customer.userId ?: 0
                                )
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = DeliveryOrange)
                        ) {
                            Icon(Icons.Default.Phone, null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Call Customer")
                        }
                        Button(
                            onClick = { onUpdateStatus("delivered") },
                            modifier = Modifier.weight(1f),
                            enabled = !isLoading,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1976D2))  // Blue instead of green
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(Icons.Default.CheckCircle, null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Mark Delivered")
                            }
                        }
                    }
                }
                "delivered" -> {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF4CAF50).copy(alpha = 0.1f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.CheckCircle, null, tint = Color(0xFF4CAF50))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Delivery Completed",
                                color = Color(0xFF4CAF50),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}
