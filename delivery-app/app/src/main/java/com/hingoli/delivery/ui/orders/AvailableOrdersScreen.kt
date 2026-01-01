package com.hingoli.delivery.ui.orders

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
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
import com.hingoli.delivery.data.model.AvailableOrder
import com.hingoli.delivery.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AvailableOrdersScreen(
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
                    "Available Orders",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "Pull to refresh",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
            
            if (uiState.isLoading && uiState.availableOrders.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = DeliveryOrange)
                }
            } else if (uiState.availableOrders.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.Inbox,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = Color.Gray
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "No orders available",
                            fontSize = 18.sp,
                            color = Color.Gray
                        )
                        Text(
                            "Pull down to refresh",
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
                    items(uiState.availableOrders, key = { it.orderId }) { order ->
                        OrderCard(
                            order = order,
                            isLoading = uiState.loadingOrderId == order.orderId,
                            onAccept = { viewModel.acceptOrder(order.orderId) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun OrderCard(
    order: AvailableOrder,
    isLoading: Boolean,
    onAccept: () -> Unit
) {
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
                    text = "#${order.orderNumber}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Card(
                    colors = CardDefaults.cardColors(containerColor = DeliveryOrange.copy(alpha = 0.1f)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "₹${String.format("%.0f", order.deliveryEarnings)} Earnings",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        color = DeliveryOrange,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Customer Info
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Person, null, tint = Color.Gray, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(order.customer.name ?: "Customer", fontSize = 14.sp)
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Pickup Address (Store) - Minimalist white with border
            order.pickupAddress?.let { pickup ->
                val context = LocalContext.current
                Surface(
                    color = CharcoalLight,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, PremiumBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Icon in subtle circle
                        Surface(
                            color = CharcoalMedium,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                Icon(Icons.Default.Store, null, tint = PremiumTextSecondary, modifier = Modifier.size(20.dp))
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "PICKUP FROM",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium,
                                color = PremiumTextSecondary,
                                letterSpacing = 0.5.sp
                            )
                            Text(
                                pickup.label ?: "Hingoli Hub Warehouse",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                listOfNotNull(pickup.line1, pickup.city).joinToString(", "),
                                fontSize = 12.sp,
                                color = PremiumTextSecondary
                            )
                        }
                        // Call Seller Button
                        pickup.phone?.let { phone ->
                            OutlinedButton(
                                onClick = {
                                    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone"))
                                    context.startActivity(intent)
                                },
                                modifier = Modifier.height(32.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                                border = BorderStroke(1.dp, PremiumBorder),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = DeliveryOrange)
                            ) {
                                Icon(Icons.Default.Call, null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Call", fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Delivery Address (Customer) - Minimalist white with border
            val deliveryAddr = order.deliveryAddress ?: order.address
            Surface(
                color = CharcoalLight,
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, PremiumBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Icon in subtle circle
                    Surface(
                        color = CharcoalMedium,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                            Icon(Icons.Default.LocationOn, null, tint = DeliveryOrange, modifier = Modifier.size(20.dp))
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            "DELIVER TO",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            color = PremiumTextSecondary,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            order.customer.name ?: "Customer",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            listOfNotNull(
                                deliveryAddr?.line1,
                                deliveryAddr?.city,
                                deliveryAddr?.postalCode
                            ).joinToString(", "),
                            fontSize = 12.sp,
                            color = PremiumTextSecondary
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(6.dp))
            
            // Order Amount
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.ShoppingBag, null, tint = Color.Gray, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Order Total: ₹${String.format("%.2f", order.totalAmount)}",
                    fontSize = 14.sp
                )
            }
            
            Spacer(modifier = Modifier.height(6.dp))
            
            // Payment Method & Status
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Payment, null, tint = Color.Gray, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    order.paymentMethod.uppercase(),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (order.paymentMethod == "cod") Color(0xFFE65100) else Color(0xFF2E7D32)
                )
                Spacer(modifier = Modifier.width(12.dp))
                
                // Payment Status Badge
                val isPending = order.paymentStatus == "pending"
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (isPending) Color(0xFFFFF3E0) else Color(0xFFE8F5E9)
                    ),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = if (isPending) "💰 COLLECT CASH" else "✓ PAID",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isPending) Color(0xFFE65100) else Color(0xFF2E7D32)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Accept Button
            Button(
                onClick = onAccept,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp),
                enabled = !isLoading,
                colors = ButtonDefaults.buttonColors(containerColor = DeliveryOrange)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = CharcoalLight,
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(Icons.Default.CheckCircle, null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Accept Order", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
