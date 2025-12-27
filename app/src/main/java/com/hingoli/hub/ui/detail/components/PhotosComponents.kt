package com.hingoli.hub.ui.detail.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.PhotoLibrary
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.hingoli.hub.data.model.Listing
import com.hingoli.hub.data.model.ListingImage
import com.hingoli.hub.ui.theme.*

/**
 * Photos tab showing gallery of listing images with owner management
 */
@Composable
fun PhotosTab(
    listing: Listing,
    isOwnListing: Boolean = false,
    onDeleteImage: (Long) -> Unit = {},
    onAddImage: () -> Unit = {}
) {
    val images = listing.images ?: listOf(
        ListingImage(0, listing.mainImageUrl ?: "", null, 0)
    )
    
    Column {
        // Add Photo button for owner
        if (isOwnListing) {
            Button(
                onClick = onAddImage,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Add Photos")
            }
            Spacer(modifier = Modifier.height(12.dp))
        }
        
        if (images.isEmpty() || (images.size == 1 && images.first().imageUrl.isEmpty())) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Outlined.PhotoLibrary,
                        contentDescription = null,
                        tint = OnSurfaceVariant,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (isOwnListing) "No photos yet. Tap 'Add Photos' to upload." else "No photos available",
                        style = MaterialTheme.typography.bodyMedium,
                        color = OnSurfaceVariant
                    )
                }
            }
        } else {
            Text(
                text = "${images.size} Photos",
                style = MaterialTheme.typography.titleMedium,
                color = OnSurface,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(12.dp))
            
            // Photo grid
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(images) { image ->
                    Box(modifier = Modifier.size(120.dp)) {
                        Card(
                            modifier = Modifier.fillMaxSize(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            AsyncImage(
                                model = image.imageUrl,
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }
                        
                        // Delete button for owner (only for gallery images, not main)
                        if (isOwnListing && image.imageId > 0) {
                            IconButton(
                                onClick = { onDeleteImage(image.imageId) },
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .size(28.dp)
                                    .background(
                                        color = Color.Black.copy(alpha = 0.5f),
                                        shape = CircleShape
                                    )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Delete",
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
