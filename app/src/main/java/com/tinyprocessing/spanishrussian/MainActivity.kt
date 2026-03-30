package com.tinyprocessing.spanishrussian

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.OnBackPressedCallback
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tinyprocessing.spanishrussian.data.DictViewModel
import com.tinyprocessing.spanishrussian.ui.screens.AllFavoritesScreen
import com.tinyprocessing.spanishrussian.ui.screens.AllRecentsScreen
import com.tinyprocessing.spanishrussian.ui.screens.DetailScreen
import com.tinyprocessing.spanishrussian.ui.screens.SearchScreen
import com.tinyprocessing.spanishrussian.ui.screens.WebScreen
import com.tinyprocessing.spanishrussian.ui.theme.SpanishRussianTheme
import java.net.URLEncoder

private enum class Screen { Search, Detail, AllRecents, AllFavorites, Web }

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            SpanishRussianTheme {
                val vm: DictViewModel = viewModel()

                // Accept query from PopupTranslateActivity "Open in app"
                val incomingQuery = remember { intent.getStringExtra("query") }
                val openedFromPopup = remember { incomingQuery != null }

                val textFieldValue by vm.textFieldValue.collectAsState()
                val results by vm.results.collectAsState()
                val favorites by vm.favorites.collectAsState()
                val recentSearches by vm.recentSearches.collectAsState()
                val selectedEntry by vm.selectedEntry.collectAsState()
                val favoritesCount by vm.favoritesCount.collectAsState()
                val hasMoreFavorites by vm.hasMoreFavorites.collectAsState()
                val focusRequester = remember { FocusRequester() }
                val focusManager = LocalFocusManager.current
                val keyboardController = LocalSoftwareKeyboardController.current
                var subScreen by remember { mutableStateOf<Screen?>(null) }
                var webUrl by remember { mutableStateOf("") }
                var webTitle by remember { mutableStateOf("") }

                // Pre-fill search + open keyboard when launched from popup
                androidx.compose.runtime.LaunchedEffect(incomingQuery) {
                    if (!incomingQuery.isNullOrBlank()) {
                        vm.selectRecentSearch(incomingQuery)
                        focusRequester.requestFocus()
                        keyboardController?.show()
                    }
                }

                val currentScreen = when {
                    subScreen == Screen.Web -> Screen.Web
                    selectedEntry != null -> Screen.Detail
                    subScreen != null -> subScreen!!
                    else -> Screen.Search
                }

                val backCallback = remember {
                    object : OnBackPressedCallback(true) {
                        override fun handleOnBackPressed() {
                            when {
                                subScreen == Screen.Web -> subScreen = null
                                selectedEntry != null -> vm.clearSelection()
                                subScreen != null -> subScreen = null
                                vm.queryText.isNotEmpty() -> {
                                    vm.clearQuery()
                                    focusManager.clearFocus()
                                }
                                else -> {
                                    isEnabled = false
                                    onBackPressedDispatcher.onBackPressed()
                                }
                            }
                        }
                    }
                }

                androidx.compose.runtime.DisposableEffect(Unit) {
                    onBackPressedDispatcher.addCallback(backCallback)
                    onDispose { backCallback.remove() }
                }

                backCallback.isEnabled = true

                AnimatedContent(
                    targetState = currentScreen,
                    modifier = Modifier.fillMaxSize(),
                    transitionSpec = {
                        if (targetState == Screen.Search ||
                            (targetState == Screen.Detail && initialState == Screen.Web)
                        ) {
                            slideInHorizontally { -it / 3 } togetherWith slideOutHorizontally { it }
                        } else {
                            slideInHorizontally { it } togetherWith slideOutHorizontally { -it / 3 }
                        }
                    },
                    label = "nav"
                ) { screen ->
                    when (screen) {
                        Screen.Web -> {
                            WebScreen(
                                url = webUrl,
                                title = webTitle,
                                onBack = { subScreen = null },
                            )
                        }
                        Screen.Detail -> {
                            val entry = selectedEntry
                            if (entry != null) {
                                DetailScreen(
                                    entry = entry,
                                    onBack = { vm.clearSelection() },
                                    onToggleFavorite = { vm.toggleFavorite(entry) },
                                    onOpenWeb = { word ->
                                        val encoded = URLEncoder.encode(word, "UTF-8")
                                        webUrl = "https://www.ingles.com/traductor/$encoded"
                                        webTitle = word
                                        subScreen = Screen.Web
                                    },
                                )
                            }
                        }
                        Screen.AllRecents -> {
                            AllRecentsScreen(
                                recentSearches = recentSearches,
                                onRecentClick = {
                                    subScreen = null
                                    vm.selectRecentSearch(it)
                                },
                                onClear = {
                                    vm.clearHistory()
                                    subScreen = null
                                },
                                onBack = { subScreen = null },
                            )
                        }
                        Screen.AllFavorites -> {
                            AllFavoritesScreen(
                                favorites = favorites,
                                totalCount = favoritesCount,
                                hasMore = hasMoreFavorites,
                                onEntryClick = {
                                    vm.selectEntry(it)
                                },
                                onToggleFavorite = { vm.toggleFavorite(it) },
                                onLoadMore = { vm.loadMoreFavorites() },
                                onClear = {
                                    vm.clearFavorites()
                                    subScreen = null
                                },
                                onBack = { subScreen = null },
                            )
                        }
                        Screen.Search -> {
                            SearchScreen(
                                textFieldValue = textFieldValue,
                                results = results,
                                favorites = favorites,
                                recentSearches = recentSearches,
                                onTextFieldValueChange = { vm.onTextFieldValueChange(it) },
                                onEntryClick = { vm.selectEntry(it) },
                                onRecentClick = { vm.selectRecentSearch(it) },
                                onClearHistory = { vm.clearHistory() },
                                onToggleFavorite = { vm.toggleFavorite(it) },
                                onFabClick = {
                                    if (vm.queryText.isNotEmpty()) {
                                        vm.selectAllText()
                                    }
                                    focusRequester.requestFocus()
                                    keyboardController?.show()
                                },
                                onShowAllRecents = { subScreen = Screen.AllRecents },
                                onShowAllFavorites = { subScreen = Screen.AllFavorites },
                                onClearFavorites = { vm.clearFavorites() },
                                focusRequester = focusRequester,
                            )
                        }
                    }
                }
            }
        }
    }
}
