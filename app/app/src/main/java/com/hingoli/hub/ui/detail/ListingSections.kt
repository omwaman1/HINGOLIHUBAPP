package com.hingoli.hub.ui.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.WorkOutline
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.hingoli.hub.data.model.Listing
import com.hingoli.hub.ui.theme.*

/**
 * Common sections used in ListingDetailScreen extracted for reusability.
 */

// ==================== TITLE SECTION ====================

@Composable
fun TitleSection(
    listing: Listing,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.padding(horizontal = 16.dp)) {
        Text(
            text = listing.title,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = OnSurface
        )
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Text(
            text = listing.listingType.replaceFirstChar { 
                if (it.isLowerCase()) it.titlecase() else it.toString() 
            },
            style = MaterialTheme.typography.bodyMedium,
            color = OnSurfaceVariant
        )
    }
}

// ==================== STATS SECTION ====================

@Composable
fun StatsSection(
    listing: Listing,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        StatItem(
            icon = Icons.Default.Star,
            value = String.format("%.1f", listing.avgRating),
            label = "${listing.reviewCount} reviews",
            iconColor = Color(0xFFFFC107)
        )
        StatItem(
            icon = Icons.Default.Visibility,
            value = "${listing.viewCount}",
            label = "Views"
        )
        StatItem(
            icon = Icons.Default.ThumbUp,
            value = "${listing.viewCount}",
            label = "Engagement"
        )
    }
}

@Composable
private fun StatItem(
    icon: ImageVector,
    value: String,
    label: String,
    iconColor: Color = OnSurfaceVariant
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = OnSurface
            )
        }
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = OnSurfaceVariant
        )
    }
}

// ==================== DESCRIPTION SECTION ====================

@Composable
fun DescriptionSection(
    listing: Listing,
    modifier: Modifier = Modifier
) {
    if (listing.description.isNullOrBlank()) return
    
    Column(modifier = modifier.padding(horizontal = 16.dp)) {
        Text(
            text = "Description",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = OnSurface
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = listing.description,
            style = MaterialTheme.typography.bodyMedium,
            color = OnSurfaceVariant
        )
    }
}

// ==================== ADDRESS SECTION ====================

@Composable
fun AddressSection(
    listing: Listing,
    modifier: Modifier = Modifier,
    onMapClick: () -> Unit = {}
) {
    val address = buildString {
        listing.location?.let { append(it) }
        listing.city?.let { 
            if (isNotEmpty()) append(", ")
            append(it) 
        }
    }
    
    if (address.isBlank()) return
    
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(CardBorder.copy(alpha = 0.3f))
            .clickable { onMapClick() }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.LocationOn,
            contentDescription = null,
            tint = Primary,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Address",
                style = MaterialTheme.typography.labelMedium,
                color = OnSurfaceVariant
            )
            Text(
                text = address,
                style = MaterialTheme.typography.bodyMedium,
                color = OnSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = OnSurfaceVariant
        )
    }
}

// ==================== EXPERIENCE SECTION ====================

@Composable
fun ExperienceSection(
    listing: Listing,
    modifier: Modifier = Modifier
) {
    val experienceStr = listing.experienceYears?.let { "$it years" } ?: listing.serviceDetails?.experienceYears?.let { "$it years" }
    if (experienceStr.isNullOrBlank()) return
    
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Outlined.WorkOutline,
            contentDescription = null,
            tint = Primary,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "$experienceStr experience",
            style = MaterialTheme.typography.bodyMedium,
            color = OnSurfaceVariant
        )
    }
}

// ==================== JOB DETAIL ROW ====================

@Composable
fun JobDetailRow(
    icon: ImageVector,
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(PrimaryBlue.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = PrimaryBlue,
                modifier = Modifier.size(18.dp)
            )
        }
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = OnSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = OnSurface
            )
        }
    }
}

// ==================== SECTION DIVIDER ====================

@Composable
fun SectionDivider(modifier: Modifier = Modifier) {
    HorizontalDivider(
        modifier = modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        color = CardBorder.copy(alpha = 0.5f)
    )
}
