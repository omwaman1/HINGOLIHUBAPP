package com.hingoli.hub.ui.reels

import android.content.Intent
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.annotation.OptIn
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import com.hingoli.hub.data.model.Reel
import com.hingoli.hub.data.settings.SettingsManager
import com.hingoli.hub.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun ReelsScreen(
    onMenuClick: () -> Unit,
    onBackClick: () -> Unit = {},
    viewModel: ReelsViewModel = hiltViewModel(),
    settingsManager: SettingsManager
) {
    val uiState by viewModel.uiState.collectAsState()
    var isMuted by remember { mutableStateOf(false) }
    val view = LocalView.current
    
    // Set status bar icons to white (light icons on dark background) for Reels
    DisposableEffect(Unit) {
        val window = (view.context as android.app.Activity).window
        val insetsController = androidx.core.view.WindowCompat.getInsetsController(window, view)
        insetsController.isAppearanceLightStatusBars = false // White icons on black
        
        onDispose {
            // Restore to dark icons when leaving Reels
            insetsController.isAppearanceLightStatusBars = true
        }
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .statusBarsPadding() // Don't overlap status bar
            .navigationBarsPadding() // Don't overlap bottom navigation
    ) {
        when {
            uiState.isLoading -> {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = Primary
                )
            }
            uiState.error != null -> {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = uiState.error ?: "Error loading reels",
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { viewModel.loadReels() }) {
                        Text("Retry")
                    }
                }
            }
            uiState.reels.isEmpty() -> {
                Text(
                    text = "No reels available",
                    color = Color.White,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
            else -> {
                val pagerState = rememberPagerState(pageCount = { uiState.reels.size })
                
                // Pre-cache is handled by VerticalPager's built-in lazy loading
                // The ExoPlayer in each ReelVideoItem will buffer when prepared
                
                VerticalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize()
                ) { page ->
                    ReelVideoItem(
                        reel = uiState.reels[page],
                        isCurrentPage = pagerState.currentPage == page,
                        isMuted = isMuted,
                        onMuteToggle = { isMuted = !isMuted },
                        onLikeClick = { viewModel.toggleLike(uiState.reels[page].reelId) },
                        onWatched = { viewModel.markWatched(uiState.reels[page].reelId) }
                    )
                }
            }
        }
        
        // Back button (top-left)
        IconButton(
            onClick = onBackClick,
            modifier = Modifier
                .align(Alignment.TopStart)
                .statusBarsPadding()
                .padding(8.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = Color.White,
                modifier = Modifier.size(28.dp)
            )
        }
    }
}

@OptIn(UnstableApi::class)
@Composable
fun ReelVideoItem(
    reel: Reel,
    isCurrentPage: Boolean,
    isMuted: Boolean,
    onMuteToggle: () -> Unit,
    onLikeClick: () -> Unit,
    onWatched: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    
    var isPlaying by remember { mutableStateOf(true) }
    var isFastForward by remember { mutableStateOf(false) }
    var progress by remember { mutableFloatStateOf(0f) }
    var hasMarkedWatched by remember { mutableStateOf(false) }
    
    // Create ExoPlayer instance
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            repeatMode = Player.REPEAT_MODE_ONE
            playWhenReady = true
        }
    }
    
    // Set media source when reel changes
    LaunchedEffect(reel.videoUrl) {
        reel.videoUrl?.let { url ->
            val mediaItem = MediaItem.fromUri(url)
            exoPlayer.setMediaItem(mediaItem)
            exoPlayer.prepare()
        }
    }
    
    // Play/pause based on page visibility and isPlaying state
    LaunchedEffect(isCurrentPage, isPlaying) {
        exoPlayer.playWhenReady = isCurrentPage && isPlaying
    }
    
    // Handle mute state
    LaunchedEffect(isMuted) {
        exoPlayer.volume = if (isMuted) 0f else 1f
    }
    
    // Handle 2x speed
    LaunchedEffect(isFastForward) {
        exoPlayer.setPlaybackSpeed(if (isFastForward) 2f else 1f)
    }
    
    // Update progress bar
    LaunchedEffect(isCurrentPage) {
        while (isCurrentPage) {
            val duration = exoPlayer.duration.coerceAtLeast(1L)
            val position = exoPlayer.currentPosition
            progress = (position.toFloat() / duration.toFloat()).coerceIn(0f, 1f)
            
            // Mark as watched when 80% complete
            if (progress > 0.8f && !hasMarkedWatched) {
                hasMarkedWatched = true
                onWatched()
            }
            delay(100)
        }
    }
    
    // Lifecycle handling - pause when app goes to background
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE -> exoPlayer.playWhenReady = false
                Lifecycle.Event.ON_RESUME -> exoPlayer.playWhenReady = isCurrentPage && isPlaying
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            exoPlayer.release()
        }
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = {
                        // Tap to pause/play
                        isPlaying = !isPlaying
                    },
                    onLongPress = { offset ->
                        // Long press near edges for 2x speed (handled in drag)
                    }
                )
            }
            .pointerInput(Unit) {
                // Detect press on edges for 2x speed
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent()
                        val position = event.changes.firstOrNull()?.position
                        val width = size.width
                        
                        if (position != null) {
                            val isNearEdge = position.x < width * 0.15f || position.x > width * 0.85f
                            val isPressed = event.changes.any { it.pressed }
                            isFastForward = isNearEdge && isPressed
                        }
                    }
                }
            }
    ) {
        // Video Player
        if (reel.videoUrl != null) {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { ctx ->
                    PlayerView(ctx).apply {
                        player = exoPlayer
                        useController = false
                        resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                        layoutParams = FrameLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                    }
                }
            )
        } else {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("Video not available", color = Color.White, fontSize = 16.sp)
            }
        }
        
        // 2x Speed Indicator
        AnimatedVisibility(
            visible = isFastForward,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.Center)
        ) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color.Black.copy(alpha = 0.7f)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("⏩", fontSize = 20.sp)
                    Spacer(Modifier.width(8.dp))
                    Text("2x", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }
            }
        }
        
        // Title overlay at bottom-left
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(16.dp)
                .padding(bottom = 40.dp, end = 80.dp)
        ) {
            reel.title?.let { title ->
                Text(
                    text = title,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    lineHeight = 20.sp
                )
            }
        }
        
        // Right side action buttons (vertical)
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 12.dp, bottom = 60.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Like button with count
            ReelActionButton(
                icon = if (reel.isLiked) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                label = formatCount(reel.likesCount),
                tint = if (reel.isLiked) Color.Red else Color.White,
                onClick = onLikeClick
            )
            
            // Share button
            ReelActionButton(
                icon = Icons.Filled.Share,
                label = "Share",
                tint = Color.White,
                onClick = {
                    val shareText = "बघा आपल्या हिंगोली च्या reels आता Hingoli Hub App वर 🎬\nhttps://hellohingoli.com/apiv5/reel/${reel.reelId}"
                    val intent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, shareText)
                    }
                    context.startActivity(Intent.createChooser(intent, "Share Reel"))
                }
            )
            
            // Mute/Unmute button
            ReelActionButton(
                icon = if (isMuted) Icons.Filled.VolumeOff else Icons.Filled.VolumeUp,
                label = if (isMuted) "Unmute" else "Mute",
                tint = Color.White,
                onClick = onMuteToggle
            )
        }
        
        // Progress bar at very bottom edge (just above nav bar)
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(16.dp)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.3f))
                    )
                )
                .padding(horizontal = 0.dp, vertical = 0.dp)
        ) {
            // Background track
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color.White.copy(alpha = 0.3f))
                    .align(Alignment.Center)
            )
            // Progress
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction = progress)
                    .height(3.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(if (isFastForward) Color(0xFFFF6B6B) else Color.White)
                    .align(Alignment.CenterStart)
            )
            // 2x indicator on progress bar
            if (isFastForward) {
                Row(
                    modifier = Modifier.align(Alignment.CenterEnd),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("⏩", fontSize = 12.sp)
                    Spacer(Modifier.width(4.dp))
                    Text("2x", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun ReelActionButton(
    icon: ImageVector,
    label: String,
    tint: Color = Color.White,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null,
            onClick = onClick
        )
    ) {
        Box(
            modifier = Modifier.size(48.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = tint,
                modifier = Modifier.size(28.dp)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            color = Color.White,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

private fun formatCount(count: Int): String {
    return when {
        count >= 1000000 -> String.format("%.1fM", count / 1000000.0)
        count >= 1000 -> String.format("%.1fK", count / 1000.0)
        else -> count.toString()
    }
}
