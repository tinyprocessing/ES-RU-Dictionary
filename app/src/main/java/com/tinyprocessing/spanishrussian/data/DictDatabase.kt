package com.tinyprocessing.spanishrussian.data

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.io.File
import java.io.FileOutputStream

class DictDatabase(private val context: Context) :
    SQLiteOpenHelper(context, DB_NAME, null, DB_VERSION) {

    companion object {
        private const val DB_NAME = "rues.db"
        private const val DB_VERSION = 1

        @Volatile
        private var instance: DictDatabase? = null

        fun getInstance(context: Context): DictDatabase {
            return instance ?: synchronized(this) {
                instance ?: DictDatabase(context.applicationContext).also { instance = it }
            }
        }
    }

    init {
        copyDatabaseIfNeeded()
    }

    private fun copyDatabaseIfNeeded() {
        val dbFile = context.getDatabasePath(DB_NAME)
        if (!dbFile.exists()) {
            dbFile.parentFile?.mkdirs()
            context.assets.open("databases/$DB_NAME").use { input ->
                FileOutputStream(dbFile).use { output ->
                    input.copyTo(output)
                }
            }
        }
    }

    override fun onCreate(db: SQLiteDatabase?) {}
    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {}

    fun search(query: String, limit: Int = 50): List<DictEntry> {
        if (query.isBlank()) return emptyList()
        val db = readableDatabase
        val sanitized = query.replace("'", "''")
        val cursor = db.rawQuery(
            """
            SELECT _id, word_id, word, lang, translations, isFav
            FROM dict
            WHERE word LIKE ? || '%'
            ORDER BY length(word), word COLLATE NOCASE
            LIMIT ?
            """.trimIndent(),
            arrayOf(sanitized, limit.toString())
        )
        val results = mutableListOf<DictEntry>()
        cursor.use {
            while (it.moveToNext()) {
                results.add(
                    DictEntry(
                        id = it.getLong(0),
                        wordId = it.getLong(1),
                        word = it.getString(2),
                        lang = it.getInt(3),
                        translations = it.getString(4),
                        isFav = it.getInt(5) == 1
                    )
                )
            }
        }
        return results
    }

    fun getEntry(id: Long): DictEntry? {
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT _id, word_id, word, lang, translations, isFav FROM dict WHERE _id = ?",
            arrayOf(id.toString())
        )
        cursor.use {
            if (it.moveToFirst()) {
                return DictEntry(
                    id = it.getLong(0),
                    wordId = it.getLong(1),
                    word = it.getString(2),
                    lang = it.getInt(3),
                    translations = it.getString(4),
                    isFav = it.getInt(5) == 1
                )
            }
        }
        return null
    }

    fun toggleFavorite(id: Long, isFav: Boolean) {
        val db = writableDatabase
        if (isFav) {
            ensureFavTimestampColumn(db)
            db.execSQL(
                "UPDATE dict SET isFav = 1, favTimestamp = ? WHERE _id = ?",
                arrayOf<Any>(System.currentTimeMillis(), id)
            )
        } else {
            db.execSQL("UPDATE dict SET isFav = 0 WHERE _id = ?", arrayOf(id))
        }
    }

    private var favColumnEnsured = false

    private fun ensureFavTimestampColumn(db: SQLiteDatabase) {
        if (favColumnEnsured) return
        try {
            db.rawQuery("SELECT favTimestamp FROM dict LIMIT 1", null).use {}
        } catch (e: Exception) {
            db.execSQL("ALTER TABLE dict ADD COLUMN favTimestamp INTEGER DEFAULT 0")
        }
        // Create composite index for fast favorite queries
        try {
            db.execSQL("CREATE INDEX IF NOT EXISTS idx_dict_fav_ts ON dict(isFav, favTimestamp DESC)")
        } catch (_: Exception) {}
        favColumnEnsured = true
    }

    fun getFavorites(limit: Int = 0, offset: Int = 0): List<DictEntry> {
        val db = readableDatabase
        ensureFavTimestampColumn(db)
        val sql = buildString {
            append("SELECT _id, word_id, word, lang, translations, isFav FROM dict WHERE isFav = 1 ORDER BY favTimestamp DESC")
            if (limit > 0) append(" LIMIT $limit OFFSET $offset")
        }
        val cursor = db.rawQuery(sql, null)
        val results = mutableListOf<DictEntry>()
        cursor.use {
            while (it.moveToNext()) {
                results.add(
                    DictEntry(
                        id = it.getLong(0),
                        wordId = it.getLong(1),
                        word = it.getString(2),
                        lang = it.getInt(3),
                        translations = it.getString(4),
                        isFav = it.getInt(5) == 1
                    )
                )
            }
        }
        return results
    }

    fun getFavoritesCount(): Int {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT COUNT(*) FROM dict WHERE isFav = 1", null)
        cursor.use {
            if (it.moveToFirst()) return it.getInt(0)
        }
        return 0
    }

    fun getRecentSearches(): List<String> {
        val db = readableDatabase
        return try {
            val cursor = db.rawQuery(
                "SELECT query FROM search_history ORDER BY timestamp DESC LIMIT 20",
                null
            )
            val results = mutableListOf<String>()
            cursor.use {
                while (it.moveToNext()) {
                    results.add(it.getString(0))
                }
            }
            results
        } catch (e: Exception) {
            db.execSQL("CREATE TABLE IF NOT EXISTS search_history (_id INTEGER PRIMARY KEY AUTOINCREMENT, query TEXT NOT NULL, timestamp INTEGER NOT NULL)")
            emptyList()
        }
    }

    fun saveSearch(query: String) {
        if (query.isBlank()) return
        val db = writableDatabase
        db.execSQL("CREATE TABLE IF NOT EXISTS search_history (_id INTEGER PRIMARY KEY AUTOINCREMENT, query TEXT NOT NULL, timestamp INTEGER NOT NULL)")
        db.execSQL("DELETE FROM search_history WHERE query = ?", arrayOf(query))
        db.execSQL(
            "INSERT INTO search_history (query, timestamp) VALUES (?, ?)",
            arrayOf<Any>(query, System.currentTimeMillis())
        )
        db.execSQL("DELETE FROM search_history WHERE _id NOT IN (SELECT _id FROM search_history ORDER BY timestamp DESC LIMIT 20)")
    }

    fun clearHistory() {
        val db = writableDatabase
        db.execSQL("DELETE FROM search_history")
    }

    fun clearFavorites() {
        val db = writableDatabase
        db.execSQL("UPDATE dict SET isFav = 0")
    }
}

data class DictEntry(
    val id: Long,
    val wordId: Long,
    val word: String,
    val lang: Int, // 0=Russian, 1=Spanish
    val translations: String,
    val isFav: Boolean
) {
    val langLabel: String get() = if (lang == 1) "ES" else "RU"
    val translationLangLabel: String get() = if (lang == 1) "RU" else "ES"
    val translationList: List<String> get() = translations.split(", ")
}
