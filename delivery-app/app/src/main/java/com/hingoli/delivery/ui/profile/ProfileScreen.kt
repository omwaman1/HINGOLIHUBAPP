package com.hingoli.delivery.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.hingoli.delivery.ui.theme.DeliveryOrange
import com.hingoli.delivery.ui.theme.DeliveryOrangeDark

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel = hiltViewModel(),
    onLogout: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val pullToRefreshState = rememberPullToRefreshState()
    
    // Show snackbar for success/error messages
    LaunchedEffect(uiState.successMessage, uiState.error) {
        uiState.successMessage?.let { message ->
            snackbarHostState.showSnackbar(
                message = message,
                duration = SnackbarDuration.Short
            )
            viewModel.clearMessages()
        }
        uiState.error?.let { error ->
            snackbarHostState.showSnackbar(
                message = error,
                duration = SnackbarDuration.Short
            )
            viewModel.clearMessages()
        }
    }
    
    Scaffold(
        snackbarHost = {
            SnackbarHost(snackbarHostState) { data ->
                Snackbar(
                    snackbarData = data,
                    containerColor = if (uiState.successMessage != null) Color(0xFF4CAF50) else Color(0xFFE53935),
                    contentColor = Color.White
                )
            }
        }
    ) { paddingValues ->
        PullToRefreshBox(
            isRefreshing = uiState.isLoading,
            onRefresh = { viewModel.loadProfile() },
            state = pullToRefreshState,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {
                // Profile Header
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                brush = Brush.verticalGradient(
                                    colors = listOf(DeliveryOrange, DeliveryOrangeDark)
                                )
                            )
                            .padding(24.dp)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // Profile Photo with Pencil Icon
                            Box {
                                Box(
                                    modifier = Modifier
                                        .size(100.dp)
                                        .clip(CircleShape)
                                        .background(Color.White),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.Person,
                                        contentDescription = null,
                                        modifier = Modifier.size(60.dp),
                                        tint = DeliveryOrange
                                    )
                                }
                                
                                // Pencil edit icon overlay
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.BottomEnd)
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(DeliveryOrangeDark)
                                        .clickable { 
                                            // TODO: Open image picker
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.Edit,
                                        contentDescription = "Edit Photo",
                                        modifier = Modifier.size(18.dp),
                                        tint = Color.White
                                    )
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            Text(
                                text = uiState.user?.name ?: "Delivery Partner",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            
                            Text(
                                text = uiState.user?.phone ?: "",
                                fontSize = 16.sp,
                                color = Color.White.copy(alpha = 0.9f)
                            )
                            
                            Text(
                                text = "Pull down to refresh",
                                fontSize = 12.sp,
                                color = Color.White.copy(alpha = 0.6f)
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            // Stats Row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                StatItem("Deliveries", "${uiState.user?.totalDeliveries ?: 0}")
                                StatItem("Earnings", "₹${uiState.user?.totalEarnings?.toInt() ?: 0}")
                            }
                        }
                    }
                }
                
                // Edit Profile Form
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            "Edit Profile",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                        
                        // Name
                        ProfileTextField(
                            value = uiState.name,
                            onValueChange = { viewModel.updateName(it) },
                            label = "Full Name",
                            icon = Icons.Default.Person
                        )
                        
                        // Email
                        ProfileTextField(
                            value = uiState.email,
                            onValueChange = { viewModel.updateEmail(it) },
                            label = "Email Address",
                            icon = Icons.Default.Email
                        )
                        
                        // Address
                        ProfileTextField(
                            value = uiState.address,
                            onValueChange = { viewModel.updateAddress(it) },
                            label = "Home Address",
                            icon = Icons.Default.Home,
                            singleLine = false,
                            maxLines = 3
                        )
                        
                        HorizontalDivider()
                        
                        Text(
                            "Vehicle Details",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                        
                        // Vehicle Type
                        Text("Vehicle Type", fontSize = 14.sp, color = Color.Gray)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf("bike" to "🏍️", "scooter" to "🛵", "bicycle" to "🚲", "auto" to "🛺").forEach { (type, emoji) ->
                                FilterChip(
                                    onClick = { viewModel.updateVehicleType(type) },
                                    label = { Text("$emoji ${type.replaceFirstChar { it.uppercase() }}") },
                                    selected = uiState.vehicleType == type
                                )
                            }
                        }
                        
                        // Vehicle Number
                        ProfileTextField(
                            value = uiState.vehicleNumber,
                            onValueChange = { viewModel.updateVehicleNumber(it) },
                            label = "Vehicle Number",
                            icon = Icons.Default.DirectionsBike,
                            placeholder = "MH 26 AB 1234"
                        )
                        
                        HorizontalDivider()
                        
                        Text(
                            "Payment Details",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                        
                        // UPI ID
                        ProfileTextField(
                            value = uiState.upiId,
                            onValueChange = { viewModel.updateUpiId(it) },
                            label = "UPI ID for Payouts",
                            icon = Icons.Default.AccountBalance,
                            placeholder = "yourname@upi"
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // Save Button
                        Button(
                            onClick = { viewModel.saveProfile() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            shape = RoundedCornerShape(12.dp),
                            enabled = !uiState.isSaving,
                            colors = ButtonDefaults.buttonColors(containerColor = DeliveryOrange)
                        ) {
                            if (uiState.isSaving) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = Color.White
                                )
                            } else {
                                Icon(Icons.Default.Save, null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Save Changes", fontWeight = FontWeight.Bold)
                            }
                        }
                        
                        // Logout Button
                        OutlinedButton(
                            onClick = { viewModel.logout(onLogout) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red)
                        ) {
                            Icon(Icons.Default.Logout, null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Logout", fontWeight = FontWeight.Bold)
                        }
                        
                        Spacer(modifier = Modifier.height(32.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun StatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Text(
            text = label,
            fontSize = 12.sp,
            color = Color.White.copy(alpha = 0.8f)
        )
    }
}

@Composable
fun ProfileTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: ImageVector,
    placeholder: String = "",
    singleLine: Boolean = true,
    maxLines: Int = 1,
    isPassword: Boolean = false
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        leadingIcon = { Icon(icon, null, tint = DeliveryOrange) },
        placeholder = if (placeholder.isNotEmpty()) {{ Text(placeholder) }} else null,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        singleLine = singleLine,
        maxLines = maxLines,
        visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None
    )
}
