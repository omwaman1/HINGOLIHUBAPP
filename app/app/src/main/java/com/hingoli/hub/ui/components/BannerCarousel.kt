package com.hingoli.hub.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.hingoli.hub.data.model.Banner
import com.hingoli.hub.ui.theme.Primary
import kotlinx.coroutines.delay
import kotlin.math.absoluteValue

/**
 * Auto-scrolling banner carousel component with rounded corners
 * and peek effect showing adjacent banners on sides
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun BannerCarousel(
    banners: List<Banner>,
    onBannerClick: (Banner) -> Unit,
    modifier: Modifier = Modifier,
    autoScrollDelayMs: Long = 4000
) {
    if (banners.isEmpty()) return
    
    val pagerState = rememberPagerState(pageCount = { banners.size })
    
    // Auto-scroll effect
    LaunchedEffect(pagerState) {
        while (true) {
            delay(autoScrollDelayMs)
            val nextPage = (pagerState.currentPage + 1) % banners.size
            pagerState.animateScrollToPage(nextPage)
        }
    }
    
    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        // Pager for banners with padding for peek effect
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp),
            contentPadding = PaddingValues(horizontal = 32.dp),
            pageSpacing = 16.dp,
            beyondViewportPageCount = 1
        ) { page ->
            val banner = banners[page]
            
            // Calculate offset for effects
            val pageOffset = (
                (pagerState.currentPage - page) + pagerState.currentPageOffsetFraction
            ).absoluteValue
            
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight()
                    .graphicsLayer {
                        // Scale down side banners
                        val scale = 1f - (pageOffset * 0.15f).coerceIn(0f, 0.15f)
                        scaleX = scale
                        scaleY = scale
                        // Slightly fade side banners
                        alpha = 0.6f + (1f - pageOffset.coerceIn(0f, 1f)) * 0.4f
                    }
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.LightGray)
                    .clickable { onBannerClick(banner) }
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(banner.imageUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = banner.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
        }
        
        // Page indicators
        if (banners.size > 1) {
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                repeat(banners.size) { index ->
                    val isSelected = pagerState.currentPage == index
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 3.dp)
                            .size(if (isSelected) 8.dp else 6.dp)
                            .clip(CircleShape)
                            .background(
                                if (isSelected) Primary else Color.LightGray.copy(alpha = 0.5f)
                            )
                    )
                }
            }
        }
    }
}
