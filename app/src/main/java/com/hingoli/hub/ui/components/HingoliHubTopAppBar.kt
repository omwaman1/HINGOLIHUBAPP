package com.hingoli.hub.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.hingoli.hub.ui.theme.Primary

/**
 * Shared TopAppBar component used across all screens
 * Supports: Menu icon, Back icon, City selector, Marathi localization
 * 
 * Usage:
 * ```
 * HingoliHubTopAppBar(
 *     onMenuClick = { /* open drawer */ },
 *     onCityClick = { showCityPicker = true },
 *     cityName = "Hingoli",
 *     isMarathi = true
 * )
 * ```
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HingoliHubTopAppBar(
    title: String? = null, // If null, uses default "HINGOLI HUB" / "हिंगोली हब"
    onMenuClick: (() -> Unit)? = null,
    onBackClick: (() -> Unit)? = null,
    showCitySelector: Boolean = true,
    onCityClick: (() -> Unit)? = null,
    cityName: String = "Hingoli",
    cityNameMr: String? = null, // Marathi city name
    isMarathi: Boolean = false,
    actions: @Composable RowScope.() -> Unit = {}
) {
    // Determine the title to display
    val displayTitle = title ?: if (isMarathi) "हिंगोली हब" else "HINGOLI HUB"
    // Determine city name to display
    val displayCityName = if (isMarathi && !cityNameMr.isNullOrBlank()) cityNameMr else cityName
    
    TopAppBar(
        title = {
            Text(
                text = displayTitle,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        navigationIcon = {
            when {
                onBackClick != null -> {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
                onMenuClick != null -> {
                    IconButton(onClick = onMenuClick) {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = "Menu",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        },
        actions = {
            // City selector
            if (showCitySelector && onCityClick != null) {
                CitySelector(
                    cityName = displayCityName,
                    onClick = onCityClick
                )
            }
            // Additional custom actions
            actions()
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface
        )
    )
}

/**
 * City selector component for TopAppBar
 */
@Composable
private fun CitySelector(
    cityName: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.LocationOn,
            contentDescription = "Location",
            tint = Primary,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = cityName,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = " ▼",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

