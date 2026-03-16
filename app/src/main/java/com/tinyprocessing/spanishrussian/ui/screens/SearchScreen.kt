package com.tinyprocessing.spanishrussian.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.tinyprocessing.spanishrussian.R
import com.tinyprocessing.spanishrussian.data.DictEntry
import com.tinyprocessing.spanishrussian.ui.components.ConfirmDialog
import com.tinyprocessing.spanishrussian.ui.theme.Gray400
import com.tinyprocessing.spanishrussian.ui.theme.Gray500
import com.tinyprocessing.spanishrussian.ui.theme.Gray600
import com.tinyprocessing.spanishrussian.ui.theme.Gray700
import com.tinyprocessing.spanishrussian.ui.theme.Gray800
import com.tinyprocessing.spanishrussian.ui.theme.White
import com.tinyprocessing.spanishrussian.ui.theme.Black

@Composable
fun SearchScreen(
    textFieldValue: TextFieldValue,
    results: List<DictEntry>,
    favorites: List<DictEntry>,
    recentSearches: List<String>,
    onTextFieldValueChange: (TextFieldValue) -> Unit,
    onEntryClick: (DictEntry) -> Unit,
    onRecentClick: (String) -> Unit,
    onClearHistory: () -> Unit,
    onToggleFavorite: (DictEntry) -> Unit,
    onFabClick: () -> Unit,
    onShowAllRecents: () -> Unit,
    onShowAllFavorites: () -> Unit,
    onClearFavorites: () -> Unit,
    focusRequester: FocusRequester,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .windowInsetsPadding(WindowInsets.statusBars)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            SearchBar(
                textFieldValue = textFieldValue,
                onTextFieldValueChange = onTextFieldValueChange,
                focusRequester = focusRequester,
            )

            if (textFieldValue.text.isBlank()) {
                IdleContent(
                    favorites = favorites,
                    recentSearches = recentSearches,
                    onEntryClick = onEntryClick,
                    onRecentClick = onRecentClick,
                    onClearHistory = onClearHistory,
                    onToggleFavorite = onToggleFavorite,
                    onShowAllRecents = onShowAllRecents,
                    onShowAllFavorites = onShowAllFavorites,
                    onClearFavorites = onClearFavorites,
                )
            } else {
                SearchResults(
                    results = results,
                    onEntryClick = onEntryClick,
                    onToggleFavorite = onToggleFavorite,
                )
            }
        }

        // FAB - sits above keyboard
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .imePadding()
                .windowInsetsPadding(WindowInsets.navigationBars)
                .padding(end = 20.dp, bottom = 20.dp)
                .size(56.dp)
                .clip(CircleShape)
                .background(White)
                .clickable { onFabClick() },
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_search),
                contentDescription = "Search",
                tint = Black,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
private fun SearchBar(
    textFieldValue: TextFieldValue,
    onTextFieldValueChange: (TextFieldValue) -> Unit,
    focusRequester: FocusRequester,
) {
    val clipboardManager = LocalClipboardManager.current

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Gray800)
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_search),
                contentDescription = null,
                tint = Gray500,
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.width(12.dp))
            Box(modifier = Modifier.weight(1f)) {
                if (textFieldValue.text.isEmpty()) {
                    Text(
                        "Search Spanish or Russian...",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Gray600,
                    )
                }
                BasicTextField(
                    value = textFieldValue,
                    onValueChange = onTextFieldValueChange,
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodyLarge.copy(
                        color = MaterialTheme.colorScheme.onSurface
                    ),
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester),
                )
            }
            // Paste button
            AnimatedVisibility(visible = textFieldValue.text.isEmpty(), enter = fadeIn(), exit = fadeOut()) {
                IconButton(
                    onClick = {
                        val clip = clipboardManager.getText()
                        if (clip != null) {
                            val text = clip.text
                            onTextFieldValueChange(TextFieldValue(text, selection = TextRange(text.length)))
                            focusRequester.requestFocus()
                        }
                    },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_paste),
                        contentDescription = "Paste",
                        tint = Gray500,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            // Clear button
            AnimatedVisibility(visible = textFieldValue.text.isNotEmpty(), enter = fadeIn(), exit = fadeOut()) {
                IconButton(
                    onClick = { onTextFieldValueChange(TextFieldValue("")) },
                    modifier = Modifier.size(20.dp)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_close),
                        contentDescription = "Clear",
                        tint = Gray500,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun SearchResults(
    results: List<DictEntry>,
    onEntryClick: (DictEntry) -> Unit,
    onToggleFavorite: (DictEntry) -> Unit,
) {
    if (results.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "No results",
                style = MaterialTheme.typography.bodyLarge,
                color = Gray600,
            )
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 96.dp)
        ) {
            items(results, key = { it.id }) { entry ->
                DictEntryRow(
                    entry = entry,
                    onClick = { onEntryClick(entry) },
                    onFavClick = { onToggleFavorite(entry) },
                )
            }
        }
    }
}

@Composable
private fun IdleContent(
    favorites: List<DictEntry>,
    recentSearches: List<String>,
    onEntryClick: (DictEntry) -> Unit,
    onRecentClick: (String) -> Unit,
    onClearHistory: () -> Unit,
    onToggleFavorite: (DictEntry) -> Unit,
    onShowAllRecents: () -> Unit,
    onShowAllFavorites: () -> Unit,
    onClearFavorites: () -> Unit,
) {
    var showClearFavDialog by remember { mutableStateOf(false) }

    if (showClearFavDialog) {
        ConfirmDialog(
            title = "Clear all favorites?",
            message = "This will remove all ${favorites.size} saved favorites. This action cannot be undone.",
            confirmText = "Clear all",
            onConfirm = onClearFavorites,
            onDismiss = { showClearFavDialog = false },
        )
    }

    val maxVisible = 7
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 96.dp)
    ) {
        if (recentSearches.isNotEmpty()) {
            item {
                SectionHeader(
                    title = "Recent",
                    action = "Clear",
                    onAction = onClearHistory,
                )
            }
            val visibleRecents = recentSearches.take(maxVisible)
            items(visibleRecents) { query ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onRecentClick(query) }
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_history),
                        contentDescription = null,
                        tint = Gray600,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(
                        query,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
            if (recentSearches.size > maxVisible) {
                item {
                    Text(
                        "See all (${recentSearches.size})",
                        style = MaterialTheme.typography.labelMedium,
                        color = Gray500,
                        modifier = Modifier
                            .clickable { onShowAllRecents() }
                            .padding(horizontal = 20.dp, vertical = 12.dp)
                    )
                }
            }
        }

        if (favorites.isNotEmpty()) {
            item {
                SectionHeader(
                    title = "Favorites",
                    action = "Clear",
                    onAction = { showClearFavDialog = true },
                )
            }
            val visibleFavs = favorites.take(maxVisible)
            items(visibleFavs, key = { it.id }) { entry ->
                DictEntryRow(
                    entry = entry,
                    onClick = { onEntryClick(entry) },
                    onFavClick = { onToggleFavorite(entry) },
                )
            }
            if (favorites.size > maxVisible) {
                item {
                    Text(
                        "See all (${favorites.size})",
                        style = MaterialTheme.typography.labelMedium,
                        color = Gray500,
                        modifier = Modifier
                            .clickable { onShowAllFavorites() }
                            .padding(horizontal = 20.dp, vertical = 12.dp)
                    )
                }
            }
        }

        if (recentSearches.isEmpty() && favorites.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 120.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            "ES \u2194 RU",
                            style = MaterialTheme.typography.headlineLarge,
                            color = Gray700,
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "Start typing to search",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Gray600,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(
    title: String,
    action: String? = null,
    onAction: (() -> Unit)? = null,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            title,
            style = MaterialTheme.typography.labelMedium,
            color = Gray500,
        )
        if (action != null && onAction != null) {
            Text(
                action,
                style = MaterialTheme.typography.labelMedium,
                color = Gray600,
                modifier = Modifier.clickable { onAction() }
            )
        }
    }
}

@Composable
fun DictEntryRow(
    entry: DictEntry,
    onClick: () -> Unit,
    onFavClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    entry.word,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    entry.langLabel,
                    style = MaterialTheme.typography.labelMedium,
                    color = Gray500,
                )
            }
            Spacer(Modifier.height(2.dp))
            Text(
                entry.translations,
                style = MaterialTheme.typography.bodyMedium,
                color = Gray400,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Spacer(Modifier.width(8.dp))
        IconButton(
            onClick = onFavClick,
            modifier = Modifier.size(36.dp)
        ) {
            Icon(
                painter = painterResource(
                    if (entry.isFav) R.drawable.ic_heart_filled else R.drawable.ic_heart_outline
                ),
                contentDescription = if (entry.isFav) "Remove favorite" else "Add favorite",
                tint = if (entry.isFav) MaterialTheme.colorScheme.primary else Gray600,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
