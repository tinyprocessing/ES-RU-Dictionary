package com.tinyprocessing.spanishrussian.data

import android.app.Application
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DictViewModel(app: Application) : AndroidViewModel(app) {

    private val db = DictDatabase.getInstance(app)

    companion object {
        private const val FAV_PAGE_SIZE = 50
    }

    private val _textFieldValue = MutableStateFlow(TextFieldValue(""))
    val textFieldValue: StateFlow<TextFieldValue> = _textFieldValue

    private val _results = MutableStateFlow<List<DictEntry>>(emptyList())
    val results: StateFlow<List<DictEntry>> = _results

    private val _favorites = MutableStateFlow<List<DictEntry>>(emptyList())
    val favorites: StateFlow<List<DictEntry>> = _favorites

    private val _favoritesCount = MutableStateFlow(0)
    val favoritesCount: StateFlow<Int> = _favoritesCount

    private val _hasMoreFavorites = MutableStateFlow(false)
    val hasMoreFavorites: StateFlow<Boolean> = _hasMoreFavorites

    private val _recentSearches = MutableStateFlow<List<String>>(emptyList())
    val recentSearches: StateFlow<List<String>> = _recentSearches

    private val _selectedEntry = MutableStateFlow<DictEntry?>(null)
    val selectedEntry: StateFlow<DictEntry?> = _selectedEntry

    private var searchJob: Job? = null

    init {
        loadFavorites()
        loadRecentSearches()
    }

    fun onTextFieldValueChange(newValue: TextFieldValue) {
        val oldText = _textFieldValue.value.text
        _textFieldValue.value = newValue
        if (newValue.text != oldText) {
            searchJob?.cancel()
            val searchText = newValue.text.trimEnd()
            if (searchText.length < 2) {
                _results.value = emptyList()
                return
            }
            searchJob = viewModelScope.launch {
                delay(100)
                val searchResults = withContext(Dispatchers.IO) {
                    db.search(searchText)
                }
                _results.value = searchResults
            }
        }
    }

    val queryText: String get() = _textFieldValue.value.text

    fun clearQuery() {
        _textFieldValue.value = TextFieldValue("")
        _results.value = emptyList()
    }

    fun selectAllText() {
        val current = _textFieldValue.value
        if (current.text.isNotEmpty()) {
            _textFieldValue.value = current.copy(
                selection = TextRange(0, current.text.length)
            )
        }
    }

    fun selectEntry(entry: DictEntry) {
        _selectedEntry.value = entry
        viewModelScope.launch(Dispatchers.IO) {
            db.saveSearch(entry.word)
            loadRecentSearches()
        }
    }

    fun clearSelection() {
        _selectedEntry.value = null
    }

    fun toggleFavorite(entry: DictEntry) {
        val newFav = !entry.isFav
        viewModelScope.launch(Dispatchers.IO) {
            db.toggleFavorite(entry.id, newFav)
            val updated = entry.copy(isFav = newFav)
            withContext(Dispatchers.Main) {
                _selectedEntry.value = _selectedEntry.value?.let {
                    if (it.id == entry.id) updated else it
                }
                _results.value = _results.value.map {
                    if (it.id == entry.id) updated else it
                }
            }
            loadFavorites()
        }
    }

    fun loadMoreFavorites() {
        val currentSize = _favorites.value.size
        if (!_hasMoreFavorites.value) return
        viewModelScope.launch(Dispatchers.IO) {
            val more = db.getFavorites(limit = FAV_PAGE_SIZE, offset = currentSize)
            if (more.isNotEmpty()) {
                withContext(Dispatchers.Main) {
                    _favorites.value = _favorites.value + more
                    _hasMoreFavorites.value = _favorites.value.size < _favoritesCount.value
                }
            }
        }
    }

    fun selectRecentSearch(query: String) {
        _textFieldValue.value = TextFieldValue(query, selection = TextRange(query.length))
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            val searchResults = withContext(Dispatchers.IO) {
                db.search(query)
            }
            _results.value = searchResults
        }
    }

    fun clearHistory() {
        viewModelScope.launch(Dispatchers.IO) {
            db.clearHistory()
            loadRecentSearches()
        }
    }

    fun clearFavorites() {
        viewModelScope.launch(Dispatchers.IO) {
            db.clearFavorites()
            loadFavorites()
            withContext(Dispatchers.Main) {
                _selectedEntry.value = _selectedEntry.value?.copy(isFav = false)
                _results.value = _results.value.map { it.copy(isFav = false) }
            }
        }
    }

    private fun loadFavorites() {
        viewModelScope.launch(Dispatchers.IO) {
            val count = db.getFavoritesCount()
            val favs = db.getFavorites(limit = FAV_PAGE_SIZE, offset = 0)
            withContext(Dispatchers.Main) {
                _favoritesCount.value = count
                _favorites.value = favs
                _hasMoreFavorites.value = favs.size < count
            }
        }
    }

    private fun loadRecentSearches() {
        viewModelScope.launch(Dispatchers.IO) {
            val recent = db.getRecentSearches()
            _recentSearches.value = recent
        }
    }
}
