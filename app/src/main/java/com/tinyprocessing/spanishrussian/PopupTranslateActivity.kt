package com.tinyprocessing.spanishrussian

import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.tinyprocessing.spanishrussian.data.AppSettings
import com.tinyprocessing.spanishrussian.data.DictDatabase
import com.tinyprocessing.spanishrussian.data.DictEntry
import com.tinyprocessing.spanishrussian.data.MultitranFetcher
import com.tinyprocessing.spanishrussian.data.MultitranResult
import com.tinyprocessing.spanishrussian.ui.theme.Gray400
import com.tinyprocessing.spanishrussian.ui.theme.Gray500
import com.tinyprocessing.spanishrussian.ui.theme.Gray600
import com.tinyprocessing.spanishrussian.ui.theme.Gray700
import com.tinyprocessing.spanishrussian.ui.theme.Gray800
import com.tinyprocessing.spanishrussian.ui.theme.Gray900
import com.tinyprocessing.spanishrussian.ui.theme.SpanishRussianTheme
import com.tinyprocessing.spanishrussian.ui.theme.White
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PopupTranslateActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        // Force window to top of screen
        window.apply {
            setGravity(Gravity.TOP)
            setLayout(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
            )
        }

        val query = extractQuery(intent)
        if (query.isNullOrBlank()) {
            finish()
            return
        }

        // Pre-load results immediately on the IO thread before setContent
        val db = DictDatabase.getInstance(applicationContext)

        setContent {
            SpanishRussianTheme {
                PopupTranslateScreen(
                    query = query.trim(),
                    db = db,
                    onDismiss = { finish() },
                    onOpenInApp = { openMainApp(query.trim()) },
                )
            }
        }
    }

    private fun extractQuery(intent: Intent): String? {
        return when (intent.action) {
            Intent.ACTION_PROCESS_TEXT ->
                intent.getCharSequenceExtra(Intent.EXTRA_PROCESS_TEXT)?.toString()
            Intent.ACTION_SEND ->
                intent.getStringExtra(Intent.EXTRA_TEXT)
            else -> null
        }
    }

    private fun openMainApp(query: String) {
        val mainIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("query", query)
        }
        startActivity(mainIntent)
        finish()
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PopupTranslateScreen(
    query: String,
    db: DictDatabase,
    onDismiss: () -> Unit,
    onOpenInApp: () -> Unit,
) {
    val context = LocalContext.current
    var results by remember { mutableStateOf<List<DictEntry>>(emptyList()) }
    var onlineResult by remember { mutableStateOf<MultitranResult?>(null) }
    var loaded by remember { mutableStateOf(false) }
    var onlineLoading by remember { mutableStateOf(false) }

    LaunchedEffect(query) {
        results = withContext(Dispatchers.IO) {
            db.search(query.trimEnd(), limit = 5)
        }
        loaded = true

        if (AppSettings.shouldFetchOnline(context, results.isNotEmpty())) {
            onlineLoading = true
            try {
                val result = withContext(Dispatchers.IO) {
                    MultitranFetcher.fetch(query.trimEnd())
                }
                onlineResult = if (result.entries.isNotEmpty()) result else null
            } catch (_: Exception) {
                onlineResult = null
            }
            onlineLoading = false
        }
    }

    // Transparent backdrop — tap to dismiss
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() },
            ) { onDismiss() },
        contentAlignment = Alignment.TopCenter,
    ) {
        // Card — full width, top-aligned
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 12.dp, end = 12.dp, top = 8.dp)
                .clip(RoundedCornerShape(28.dp))
                .background(Gray900)
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() },
                ) { /* block backdrop click */ },
        ) {
            Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 20.dp)) {
                // Header row: word + actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = query,
                            style = MaterialTheme.typography.headlineMedium,
                            color = White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        if (loaded && results.isNotEmpty()) {
                            val entry = results.first()
                            Text(
                                "${entry.langLabel} → ${entry.translationLangLabel}",
                                style = MaterialTheme.typography.labelMedium,
                                color = Gray500,
                            )
                        }
                    }

                    if (loaded && results.isNotEmpty()) {
                        val entry = results.first()
                        var isFav by remember { mutableStateOf(entry.isFav) }
                        val scope = rememberCoroutineScope()

                        IconButton(
                            onClick = {
                                isFav = !isFav
                                scope.launch(Dispatchers.IO) {
                                    db.toggleFavorite(entry.id, isFav)
                                }
                            },
                            modifier = Modifier.size(40.dp),
                        ) {
                            Icon(
                                painter = painterResource(
                                    if (isFav) R.drawable.ic_heart_filled else R.drawable.ic_heart_outline
                                ),
                                contentDescription = "Favorite",
                                tint = if (isFav) White else Gray600,
                                modifier = Modifier.size(22.dp),
                            )
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                if (!loaded) {
                    Text(
                        "...",
                        color = Gray500,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                } else if (results.isEmpty() && onlineResult == null && !onlineLoading) {
                    Text(
                        "No translations found",
                        color = Gray500,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                } else {
                    // Local results
                    if (results.isNotEmpty()) {
                        val entry = results.first()
                        val translations = entry.translationList.take(8)

                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                            maxItemsInEachRow = 2,
                        ) {
                            translations.forEach { translation ->
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(Gray800)
                                        .padding(horizontal = 14.dp, vertical = 11.dp),
                                ) {
                                    Text(
                                        translation.trim(),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = White,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                }
                            }
                        }

                        if (entry.translationList.size > 8) {
                            Spacer(Modifier.height(6.dp))
                            Text(
                                "+${entry.translationList.size - 8} more",
                                style = MaterialTheme.typography.labelMedium,
                                color = Gray600,
                            )
                        }
                    }

                    // Online results
                    if (onlineLoading) {
                        Spacer(Modifier.height(14.dp))
                        Text(
                            "Searching Multitran...",
                            color = Gray500,
                            style = MaterialTheme.typography.labelMedium,
                        )
                    }
                    if (onlineResult != null) {
                        Spacer(Modifier.height(14.dp))
                        Text(
                            "Multitran.com",
                            style = MaterialTheme.typography.labelMedium,
                            color = Gray500,
                        )
                        Spacer(Modifier.height(8.dp))
                        onlineResult!!.entries.take(3).forEach { onlineEntry ->
                            if (onlineEntry.category.isNotBlank()) {
                                Text(
                                    onlineEntry.category,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = Gray600,
                                )
                                Spacer(Modifier.height(2.dp))
                            }
                            Text(
                                onlineEntry.translations.take(4).joinToString(", "),
                                style = MaterialTheme.typography.bodyMedium,
                                color = White,
                            )
                            Spacer(Modifier.height(4.dp))
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))

                // Bottom action
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    TextButton(onClick = onOpenInApp) {
                        Text(
                            "Open in app",
                            color = Gray400,
                            style = MaterialTheme.typography.labelMedium,
                        )
                        Spacer(Modifier.width(4.dp))
                        Icon(
                            painter = painterResource(R.drawable.ic_open_web),
                            contentDescription = null,
                            tint = Gray400,
                            modifier = Modifier.size(16.dp),
                        )
                    }
                }
            }
        }
    }
}
