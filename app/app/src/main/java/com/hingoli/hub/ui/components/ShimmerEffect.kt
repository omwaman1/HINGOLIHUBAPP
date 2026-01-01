package com.hingoli.hub.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Shimmer colors for the loading animation
 */
private val ShimmerColorShades = listOf(
    Color(0xFFE0E0E0),
    Color(0xFFF5F5F5),
    Color(0xFFE0E0E0)
)

/**
 * Creates an animated shimmer brush effect
 */
@Composable
fun shimmerBrush(): Brush {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateAnimation by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 1200,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_translate"
    )
    
    return Brush.linearGradient(
        colors = ShimmerColorShades,
        start = Offset(translateAnimation - 500f, translateAnimation - 500f),
        end = Offset(translateAnimation, translateAnimation)
    )
}

/**
 * A basic shimmer box - reusable placeholder element
 */
@Composable
fun ShimmerBox(
    modifier: Modifier = Modifier,
    width: Dp = 100.dp,
    height: Dp = 20.dp,
    shape: RoundedCornerShape = RoundedCornerShape(4.dp)
) {
    val brush = shimmerBrush()
    Box(
        modifier = modifier
            .width(width)
            .height(height)
            .clip(shape)
            .background(brush)
    )
}

/**
 * Shimmer placeholder for banner carousel
 */
@Composable
fun ShimmerBanner(
    modifier: Modifier = Modifier
) {
    val brush = shimmerBrush()
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .height(160.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(brush)
    )
}

/**
 * Shimmer placeholder for a listing card
 */
@Composable
fun ShimmerListingCard(
    modifier: Modifier = Modifier
) {
    val brush = shimmerBrush()
    Column(
        modifier = modifier.width(140.dp)
    ) {
        // Image placeholder
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
                .background(brush)
        )
        
        Column(
            modifier = Modifier.padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Title placeholder
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(14.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(brush)
            )
            
            // Second line title
            Box(
                modifier = Modifier
                    .width(80.dp)
                    .height(14.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(brush)
            )
            
            // Rating placeholder
            Row(
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                repeat(5) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(brush)
                    )
                }
            }
            
            // Price placeholder
            Box(
                modifier = Modifier
                    .width(60.dp)
                    .height(14.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(brush)
            )
        }
    }
}

/**
 * Shimmer placeholder for a section header (with pill button)
 */
@Composable
fun ShimmerSectionHeader(
    modifier: Modifier = Modifier
) {
    val brush = shimmerBrush()
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
    ) {
        // Title placeholder
        Box(
            modifier = Modifier
                .width(120.dp)
                .height(20.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(brush)
        )
        // Pill button placeholder
        Box(
            modifier = Modifier
                .width(70.dp)
                .height(28.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(brush)
        )
    }
}

/**
 * Shimmer placeholder for a horizontal row of listing cards
 */
@Composable
fun ShimmerListingRow(
    modifier: Modifier = Modifier,
    itemCount: Int = 4
) {
    LazyRow(
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        userScrollEnabled = false
    ) {
        items(itemCount) {
            ShimmerListingCard()
        }
    }
}

/**
 * Complete shimmer skeleton for the home screen
 * Matches the home screen layout: banner + 3-column grid sections
 */
@Composable
fun ShimmerHomeScreen(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))
        
        // Banner shimmer
        ShimmerBanner()
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Section 1 - Services
        ShimmerSectionHeader()
        ShimmerGridSection()
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Section 2 - Shop
        ShimmerSectionHeader()
        ShimmerGridSection()
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Section 3 - Buy & Sell
        ShimmerSectionHeader()
        ShimmerGridSection()
    }
}

/**
 * Shimmer for a 3-column grid section (2 rows x 3 columns)
 */
@Composable
fun ShimmerGridSection(
    modifier: Modifier = Modifier,
    columns: Int = 3,
    rows: Int = 2
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        repeat(rows) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                repeat(columns) {
                    ShimmerGridCard(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

/**
 * Shimmer for a compact grid card (matching GridListingCard)
 */
@Composable
fun ShimmerGridCard(modifier: Modifier = Modifier) {
    val brush = shimmerBrush()
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White)
            .padding(8.dp),
        horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
    ) {
        // Square image placeholder
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(RoundedCornerShape(8.dp))
                .background(brush)
        )
        Spacer(modifier = Modifier.height(6.dp))
        // Title placeholder
        Box(
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .height(12.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(brush)
        )
        Spacer(modifier = Modifier.height(4.dp))
        // Rating placeholder
        Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
            repeat(5) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(brush)
                )
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        // Price placeholder
        Box(
            modifier = Modifier
                .width(40.dp)
                .height(12.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(brush)
        )
    }
}

/**
 * Shimmer for grid layout (e.g., Shop screen with 3-column grid)
 * Only shows product grid skeleton - search and categories load instantly
 */
@Composable
fun ShimmerGridScreen(
    modifier: Modifier = Modifier,
    columns: Int = 3,
    rows: Int = 4
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Color(0xFFF8F8F8))
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Grid items shimmer only (search bar and categories load faster)
        repeat(rows) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                repeat(columns) {
                    ShimmerProductCard(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

/**
 * Shimmer for a product card in grid (matching Shop product cards)
 */
@Composable
fun ShimmerProductCard(modifier: Modifier = Modifier) {
    val brush = shimmerBrush()
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .background(Color.White)
            .padding(1.dp) // Border effect
    ) {
        // Product image (square)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .background(brush)
        )
        
        // Product info
        Column(
            modifier = Modifier.padding(6.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Product name
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(12.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(brush)
            )
            // Price
            Box(
                modifier = Modifier
                    .width(50.dp)
                    .height(14.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(brush)
            )
            // Add to cart button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(28.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(brush)
            )
        }
    }
}

/**
 * Shimmer for list layout (e.g., Jobs, Listings screens)
 */
@Composable
fun ShimmerListScreen(
    modifier: Modifier = Modifier,
    itemCount: Int = 6
) {
    val brush = shimmerBrush()
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        repeat(itemCount) {
            ShimmerListCard()
        }
    }
}

/**
 * Shimmer for a list card (horizontal card with image and text)
 */
@Composable
fun ShimmerListCard(modifier: Modifier = Modifier) {
    val brush = shimmerBrush()
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(100.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White)
            .padding(8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Image placeholder
        Box(
            modifier = Modifier
                .size(84.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(brush)
        )
        
        // Text content
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(16.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(brush)
            )
            Box(
                modifier = Modifier
                    .width(120.dp)
                    .height(14.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(brush)
            )
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                repeat(5) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(brush)
                    )
                }
            }
            Box(
                modifier = Modifier
                    .width(80.dp)
                    .height(16.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(brush)
            )
        }
    }
}

/**
 * Shimmer for detail screen (e.g., ListingDetailScreen)
 */
@Composable
fun ShimmerDetailScreen(modifier: Modifier = Modifier) {
    val brush = shimmerBrush()
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Image carousel placeholder
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(brush)
        )
        
        // Title
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(24.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(brush)
        )
        
        // Price
        Box(
            modifier = Modifier
                .width(120.dp)
                .height(28.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(brush)
        )
        
        // Rating row
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            repeat(5) {
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .clip(CircleShape)
                        .background(brush)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .width(40.dp)
                    .height(16.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(brush)
            )
        }
        
        // Description lines
        repeat(4) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(if (it == 3) 0.7f else 1f)
                    .height(14.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(brush)
            )
        }
        
        // Action buttons placeholder
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(brush)
            )
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(brush)
            )
        }
    }
}
