# ES-RU Dictionary

Offline Spanish-Russian dictionary for Android. Built for fast word lookup while reading books.

204,000+ words with bidirectional search. No internet required for translations.

## Features

- **Instant search** — results appear as you type each letter, showing word + translation inline
- **Bidirectional** — search in Spanish or Russian, get translations in the other language
- **Favorites** — save words with a tap, most recently added first
- **Search history** — recent lookups saved automatically
- **Word detail** — tap any result to see all translations
- **Web lookup** — in-app browser opens [ingles.com](https://www.ingles.com/traductor/) for verb conjugation, grammar, and usage
- **Clipboard paste** — paste a word directly from your book reader
- **Smart back navigation** — back gesture clears search, then keyboard, then exits

## Screenshots

<!-- Add screenshots here -->

## Architecture

```
app/
├── data/
│   ├── DictDatabase.kt      # SQLite helper, search, favorites, history
│   └── DictViewModel.kt     # State management with coroutines
├── ui/
│   ├── components/
│   │   └── ConfirmDialog.kt
│   ├── screens/
│   │   ├── SearchScreen.kt   # Main search UI with FAB
│   │   ├── DetailScreen.kt   # Word detail + web lookup
│   │   ├── ListScreen.kt     # All recents / all favorites
│   │   └── WebScreen.kt      # In-app WebView browser
│   └── theme/                # Black & white Material 3 theme
└── MainActivity.kt           # Single Activity, navigation
```

- Kotlin + Jetpack Compose
- Single Activity, no fragments
- Pre-built SQLite with denormalized search table and composite indexes
- Paginated favorites (50 per page, auto-loads on scroll)
- 100ms debounced search for responsive typing

## Database

The dictionary ships as a pre-built SQLite database (`rues.db`, ~36MB) in app assets. On first launch it copies to internal storage.

The `dict` table is a denormalized view of the original word/translation tables, optimized for prefix search:

| Column | Type | Description |
|--------|------|-------------|
| word | TEXT | The word to search |
| lang | INT | 0 = Russian, 1 = Spanish |
| translations | TEXT | Comma-separated translations |
| isFav | INT | Favorite flag |
| favTimestamp | INT | When favorited (for ordering) |

Indexes: `word COLLATE NOCASE`, `(isFav, favTimestamp DESC)`

## Build

```bash
git clone https://github.com/user/SpanishRussian.git
cd SpanishRussian
./gradlew assembleDebug
```

> Requires Git LFS — the database file is tracked with LFS.

APK output: `app/build/outputs/apk/debug/`

**Requirements:** Android SDK 26+ (Android 8.0), Kotlin 2.2, AGP 9.1

## License

[MIT](LICENSE)
