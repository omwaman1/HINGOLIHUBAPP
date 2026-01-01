package com.hingoli.hub.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.hingoli.hub.ui.theme.Strings

/**
 * Generic state handler composable that handles loading, error, and empty states.
 * Eliminates duplicate when { isLoading -> ... error != null -> ... } patterns.
 */
@Composable
fun <T> StateHandler(
    isLoading: Boolean,
    error: String?,
    data: T?,
    isEmpty: Boolean = false,
    isMarathi: Boolean = false,
    emptyMessage: String = Strings.get("No items found", "कोणतेही आयटम नाहीत", isMarathi),
    onRetry: () -> Unit = {},
    modifier: Modifier = Modifier,
    loadingContent: @Composable () -> Unit = { LoadingView() },
    errorContent: @Composable (String, () -> Unit) -> Unit = { msg, retry -> 
        ErrorView(message = msg, onRetry = retry) 
    },
    emptyContent: @Composable () -> Unit = { EmptyView(message = emptyMessage) },
    content: @Composable (T) -> Unit
) {
    Box(modifier = modifier.fillMaxSize()) {
        when {
            isLoading && data == null -> loadingContent()
            error != null && data == null -> errorContent(error, onRetry)
            data == null || isEmpty -> emptyContent()
            else -> content(data)
        }
    }
}

/**
 * Simplified state handler for lists
 */
@Composable
fun <T> ListStateHandler(
    isLoading: Boolean,
    error: String?,
    items: List<T>,
    isMarathi: Boolean = false,
    emptyMessage: String = Strings.get("No items found", "कोणतेही आयटम नाहीत", isMarathi),
    onRetry: () -> Unit = {},
    modifier: Modifier = Modifier,
    content: @Composable (List<T>) -> Unit
) {
    StateHandler(
        isLoading = isLoading,
        error = error,
        data = items,
        isEmpty = items.isEmpty(),
        isMarathi = isMarathi,
        emptyMessage = emptyMessage,
        onRetry = onRetry,
        modifier = modifier,
        content = content
    )
}
