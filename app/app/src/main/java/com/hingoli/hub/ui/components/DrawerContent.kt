package com.hingoli.hub.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.hingoli.hub.data.settings.AppLanguage
import com.hingoli.hub.ui.theme.*

@Composable
fun FullScreenDrawerContent(
    userName: String?,
    userPhone: String?,
    userAvatarUrl: String? = null,
    selectedLanguage: AppLanguage,
    // Removed unreadNotifications as Orders doesn't need badge
    unreadChats: Int = 0,
    onCloseClick: () -> Unit,
    onEditProfileClick: () -> Unit,
    onHomeClick: () -> Unit,
    onChatsClick: () -> Unit,
    onMyOrdersClick: () -> Unit,
    onMyListingsClick: () -> Unit,
    onJobsClick: () -> Unit = {},
    onShopClick: () -> Unit = {},
    // Registration & Selling callbacks
    onServiceRegistrationClick: () -> Unit = {},
    onBusinessRegistrationClick: () -> Unit = {},
    onSellNewProductClick: () -> Unit = {},
    onSellOldProductClick: () -> Unit = {},
    onHelpClick: () -> Unit,
    onRateUsClick: () -> Unit,
    onShareClick: () -> Unit,
    onPrivacyClick: () -> Unit,
    onTermsClick: () -> Unit,
    onAboutClick: () -> Unit = {},
    onRefundClick: () -> Unit = {},
    onLogoutClick: () -> Unit,
    onLanguageChange: (AppLanguage) -> Unit,
    modifier: Modifier = Modifier
) {
    // Light theme colors (always white theme)
    val backgroundColor = Color.White
    val textColor = Color(0xFF1E293B)
    val subtitleColor = Color(0xFF64748B)
    val dividerColor = Color(0xFFE2E8F0)
    val cardBorderColor = Color(0xFFE2E8F0)
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundColor)
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {
        // Close Button
        IconButton(
            onClick = onCloseClick,
            modifier = Modifier.size(40.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Close",
                tint = textColor,
                modifier = Modifier.size(24.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Profile Section
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar
            Box(
                modifier = Modifier.size(60.dp),
                contentAlignment = Alignment.Center
            ) {
                if (userAvatarUrl != null) {
                    AsyncImage(
                        model = userAvatarUrl,
                        contentDescription = "Profile",
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                            .background(Color(0xFFE0E7FF)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = PrimaryBlue,
                            modifier = Modifier.size(30.dp)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = userName ?: "User",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = textColor
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit",
                        tint = subtitleColor,
                        modifier = Modifier
                            .size(16.dp)
                            .clickable { onEditProfileClick() }
                    )
                }
                Text(
                    text = userPhone ?: "",
                    style = MaterialTheme.typography.bodyMedium,
                    color = subtitleColor
                )
                Text(
                    text = if (selectedLanguage == AppLanguage.MARATHI) "प्रोफाइल संपादित करा" else "Edit Profile",
                    style = MaterialTheme.typography.bodySmall,
                    color = PrimaryBlue,
                    modifier = Modifier.clickable { onEditProfileClick() }
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Quick Action Buttons (2x2 Grid)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            QuickActionButton(
                icon = Icons.Default.Home,
                label = if (selectedLanguage == AppLanguage.MARATHI) "होम" else "Home",
                onClick = onHomeClick,
                borderColor = cardBorderColor,
                textColor = textColor,
                modifier = Modifier.weight(1f)
            )
            QuickActionButton(
                icon = Icons.AutoMirrored.Filled.Chat,
                label = if (selectedLanguage == AppLanguage.MARATHI) "चॅट्स" else "Chats",
                onClick = onChatsClick,
                borderColor = cardBorderColor,
                textColor = textColor,
                modifier = Modifier.weight(1f)
            )
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            QuickActionButton(
                icon = Icons.Default.ShoppingCart,
                label = if (selectedLanguage == AppLanguage.MARATHI) "शॉप" else "Shop",
                onClick = onShopClick,
                borderColor = cardBorderColor,
                textColor = textColor,
                modifier = Modifier.weight(1f)
            )
            QuickActionButton(
                icon = Icons.Default.BusinessCenter,
                label = if (selectedLanguage == AppLanguage.MARATHI) "नोकरी" else "Jobs",
                onClick = onJobsClick,
                borderColor = cardBorderColor,
                textColor = textColor,
                modifier = Modifier.weight(1f)
            )
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            QuickActionButton(
                icon = Icons.Default.ShoppingBag,
                label = if (selectedLanguage == AppLanguage.MARATHI) "माझ्या ऑर्डर्स" else "My Orders",
                onClick = onMyOrdersClick,
                borderColor = cardBorderColor,
                textColor = textColor,
                modifier = Modifier.weight(1f)
            )
        }
        
        Spacer(modifier = Modifier.height(20.dp))
        
        // Registration & Selling Section Header
        Text(
            text = if (selectedLanguage == AppLanguage.MARATHI) "नोंदणी आणि विक्री" else "Register & Sell",
            style = MaterialTheme.typography.labelMedium,
            color = subtitleColor,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        // Service Registration
        DrawerMenuRow(
            icon = Icons.Default.Build,
            title = if (selectedLanguage == AppLanguage.MARATHI) "सेवा नोंदणी" else "Service Registration",
            badge = if (selectedLanguage == AppLanguage.MARATHI) "मोफत" else "Free",
            onClick = onServiceRegistrationClick,
            textColor = textColor,
            subtitleColor = subtitleColor
        )
        
        // Business Registration
        DrawerMenuRow(
            icon = Icons.Default.Storefront,
            title = if (selectedLanguage == AppLanguage.MARATHI) "व्यवसाय नोंदणी" else "Business Registration",
            badge = if (selectedLanguage == AppLanguage.MARATHI) "मोफत" else "Free",
            onClick = onBusinessRegistrationClick,
            textColor = textColor,
            subtitleColor = subtitleColor
        )
        
        // Sell New Product
        DrawerMenuRow(
            icon = Icons.Default.ShoppingCart,
            title = if (selectedLanguage == AppLanguage.MARATHI) "नवीन प्रोडक्ट विका" else "Sell New Product",
            badge = if (selectedLanguage == AppLanguage.MARATHI) "मोफत" else "Free",
            onClick = onSellNewProductClick,
            textColor = textColor,
            subtitleColor = subtitleColor
        )
        
        // Sell Old Product
        DrawerMenuRow(
            icon = Icons.Default.Recycling,
            title = if (selectedLanguage == AppLanguage.MARATHI) "जुने प्रोडक्ट विका" else "Sell Old Product",
            badge = if (selectedLanguage == AppLanguage.MARATHI) "मोफत" else "Free",
            onClick = onSellOldProductClick,
            textColor = textColor,
            subtitleColor = subtitleColor
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        HorizontalDivider(color = dividerColor)
        Spacer(modifier = Modifier.height(8.dp))
        
        // My Listings (to see existing listings) - styled differently
        DrawerMenuRow(
            icon = Icons.Default.Inventory2,
            title = if (selectedLanguage == AppLanguage.MARATHI) "माझ्या जाहिराती पहा" else "View My Listings",
            onClick = onMyListingsClick,
            textColor = textColor,
            subtitleColor = subtitleColor
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Language Selection
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            LanguageChip(
                text = "ENGLISH",
                isSelected = selectedLanguage == AppLanguage.ENGLISH,
                onClick = { onLanguageChange(AppLanguage.ENGLISH) },
                modifier = Modifier.weight(1f)
            )
            LanguageChip(
                text = "मराठी",
                isSelected = selectedLanguage == AppLanguage.MARATHI,
                onClick = { onLanguageChange(AppLanguage.MARATHI) },
                modifier = Modifier.weight(1f)
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        HorizontalDivider(color = dividerColor, modifier = Modifier.padding(vertical = 8.dp))
        
        // Menu Items
        DrawerMenuRow(
            icon = Icons.AutoMirrored.Filled.Help,
            title = if (selectedLanguage == AppLanguage.MARATHI) "मदत आणि समर्थन" else "Help & Support",
            onClick = onHelpClick,
            textColor = textColor,
            subtitleColor = subtitleColor
        )
        
        DrawerMenuRow(
            icon = Icons.Default.Star,
            title = if (selectedLanguage == AppLanguage.MARATHI) "आम्हाला रेट करा" else "Rate Us",
            onClick = onRateUsClick,
            textColor = textColor,
            subtitleColor = subtitleColor
        )
        
        DrawerMenuRow(
            icon = Icons.Default.Share,
            title = if (selectedLanguage == AppLanguage.MARATHI) "अ‍ॅप शेअर करा" else "Share App",
            onClick = onShareClick,
            textColor = textColor,
            subtitleColor = subtitleColor
        )
        
        DrawerMenuRow(
            icon = Icons.Default.PrivacyTip,
            title = if (selectedLanguage == AppLanguage.MARATHI) "गोपनीयता धोरण" else "Privacy Policy",
            onClick = onPrivacyClick,
            textColor = textColor,
            subtitleColor = subtitleColor
        )
        
        DrawerMenuRow(
            icon = Icons.Default.Description,
            title = if (selectedLanguage == AppLanguage.MARATHI) "सेवा अटी" else "Terms of Service",
            onClick = onTermsClick,
            textColor = textColor,
            subtitleColor = subtitleColor
        )
        
        DrawerMenuRow(
            icon = Icons.Default.Info,
            title = if (selectedLanguage == AppLanguage.MARATHI) "अ‍ॅप माहिती" else "About App",
            onClick = onAboutClick,
            textColor = textColor,
            subtitleColor = subtitleColor
        )
        
        DrawerMenuRow(
            icon = Icons.Default.CurrencyRupee,
            title = if (selectedLanguage == AppLanguage.MARATHI) "परतावा धोरण" else "Refund Policy",
            onClick = onRefundClick,
            textColor = textColor,
            subtitleColor = subtitleColor
        )
        
        Spacer(modifier = Modifier.weight(1f))
        
        // Logout
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onLogoutClick() }
                .padding(vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Logout,
                contentDescription = "Logout",
                tint = AccentRed,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = if (selectedLanguage == AppLanguage.MARATHI) "लॉगआउट" else "Logout",
                style = MaterialTheme.typography.bodyLarge,
                color = AccentRed,
                fontWeight = FontWeight.Medium
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // App Version Info
        val context = androidx.compose.ui.platform.LocalContext.current
        val packageInfo = try {
            context.packageManager.getPackageInfo(context.packageName, 0)
        } catch (e: Exception) {
            null
        }
        val appName = context.applicationInfo.loadLabel(context.packageManager).toString()
        val versionName = packageInfo?.versionName ?: "1.0.0"
        
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = appName,
                style = MaterialTheme.typography.bodySmall,
                color = OnSurfaceVariant,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "Version $versionName",
                style = MaterialTheme.typography.labelSmall,
                color = OnSurfaceVariant.copy(alpha = 0.7f)
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun QuickActionButton(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    borderColor: Color,
    textColor: Color,
    badgeCount: Int = 0,
    modifier: Modifier = Modifier
) {
    OutlinedCard(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, borderColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = textColor,
                    modifier = Modifier.size(20.dp)
                )
                if (badgeCount > 0) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .offset(x = 6.dp, y = (-4).dp)
                            .size(16.dp)
                            .clip(CircleShape)
                            .background(AccentRed),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (badgeCount > 9) "9+" else badgeCount.toString(),
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White,
                            fontSize = 9.sp
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = textColor
            )
        }
    }
}

@Composable
private fun LanguageChip(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor = if (isSelected) PrimaryBlue else Color.Transparent
    val textColor = if (isSelected) Color.White else Color(0xFF64748B)
    val borderColor = if (isSelected) PrimaryBlue else Color(0xFFE2E8F0)
    
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(backgroundColor)
            .border(1.dp, borderColor, RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = textColor
        )
    }
}

@Composable
private fun DrawerMenuRow(
    icon: ImageVector,
    title: String,
    badge: String? = null,
    onClick: () -> Unit,
    textColor: Color,
    subtitleColor: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f, fill = false)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = subtitleColor,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = textColor,
                maxLines = 2,
                modifier = Modifier.weight(1f, fill = false)
            )
            if (badge != null) {
                Spacer(modifier = Modifier.width(8.dp))
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = AccentGreen
                ) {
                    Text(
                        text = badge,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
        }
        Spacer(modifier = Modifier.width(8.dp))
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = subtitleColor
        )
    }
}
