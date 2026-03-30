package com.tinyprocessing.spanishrussian.data

import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

data class MultitranResult(
    val word: String,
    val entries: List<MultitranEntry>,
)

data class MultitranEntry(
    val category: String, // e.g. "общ.", "гл.", "авиа."
    val translations: List<String>,
)

object MultitranFetcher {

    private const val BASE_URL = "https://www.multitran.com/m.exe?ll1=2&ll2=5&l2=5&s="

    fun fetch(word: String): MultitranResult {
        val encoded = URLEncoder.encode(word, "UTF-8")
        val url = URL(BASE_URL + encoded)
        val conn = url.openConnection() as HttpURLConnection
        conn.setRequestProperty("User-Agent", "Mozilla/5.0")
        conn.connectTimeout = 10000
        conn.readTimeout = 10000

        val html = try {
            conn.inputStream.bufferedReader().readText()
        } finally {
            conn.disconnect()
        }

        return parseHtml(word, html)
    }

    private fun parseHtml(word: String, html: String): MultitranResult {
        val entries = mutableListOf<MultitranEntry>()

        // Find translation rows: <tr> containing <td class="subj"> and <td class="trans">
        val rowPattern = Regex(
            """<tr>\s*<td\s+class="subj"[^>]*>(.*?)</td>\s*<td\s+class="trans[^"]*"[^>]*>(.*?)</td>\s*</tr>""",
            RegexOption.DOT_MATCHES_ALL
        )

        for (match in rowPattern.findAll(html)) {
            val categoryHtml = match.groupValues[1]
            val transHtml = match.groupValues[2]

            // Extract category text (strip HTML tags)
            val category = stripTags(categoryHtml).trim()

            // Extract translation texts from <a> tags
            val linkPattern = Regex("""<a[^>]*>([^<]+)</a>""")
            val translations = linkPattern.findAll(transHtml)
                .map { it.groupValues[1].trim() }
                .filter { it.isNotBlank() }
                .toList()

            if (translations.isNotEmpty()) {
                entries.add(MultitranEntry(category = category, translations = translations))
            }
        }

        return MultitranResult(word = word, entries = entries)
    }

    private fun stripTags(html: String): String {
        return html.replace(Regex("<[^>]+>"), "").replace("&nbsp;", " ").trim()
    }
}
