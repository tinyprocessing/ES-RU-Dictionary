package com.tinyprocessing.spanishrussian.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.tinyprocessing.spanishrussian.R
import com.tinyprocessing.spanishrussian.data.DictEntry
import com.tinyprocessing.spanishrussian.ui.theme.Gray500
import com.tinyprocessing.spanishrussian.ui.theme.Gray600
import com.tinyprocessing.spanishrussian.ui.theme.Gray800

@Composable
fun DetailScreen(
    entry: DictEntry,
    onBack: () -> Unit,
    onToggleFavorite: () -> Unit,
    onOpenWeb: ((String) -> Unit)? = null,
) {
    // For Spanish words, look up the word itself; for Russian words, look up the first Spanish translation
    val webWord: String? = if (entry.lang == 1) {
        entry.word
    } else {
        entry.translationList.firstOrNull()?.trim()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .windowInsetsPadding(WindowInsets.statusBars)
    ) {
        // Top bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    painter = painterResource(R.drawable.ic_back),
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(Modifier.weight(1f))
            if (webWord != null && onOpenWeb != null) {
                IconButton(onClick = { onOpenWeb(webWord) }) {
                    Icon(
                        painter = painterResource(R.drawable.ic_open_web),
                        contentDescription = "Look up on web",
                        tint = Gray500,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            IconButton(onClick = onToggleFavorite) {
                Icon(
                    painter = painterResource(
                        if (entry.isFav) R.drawable.ic_heart_filled else R.drawable.ic_heart_outline
                    ),
                    contentDescription = if (entry.isFav) "Remove favorite" else "Add favorite",
                    tint = if (entry.isFav) MaterialTheme.colorScheme.primary else Gray600,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
        ) {
            item {
                Spacer(Modifier.height(8.dp))
                Text(
                    entry.word,
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    if (entry.lang == 1) "Spanish" else "Russian",
                    style = MaterialTheme.typography.labelMedium,
                    color = Gray500,
                )
                Spacer(Modifier.height(24.dp))

                Text(
                    "Translations (${entry.translationLangLabel})",
                    style = MaterialTheme.typography.labelMedium,
                    color = Gray500,
                )
                Spacer(Modifier.height(12.dp))
            }

            val translations = entry.translationList
            itemsIndexed(translations) { index, translation ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Gray800)
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        "${index + 1}",
                        style = MaterialTheme.typography.labelMedium,
                        color = Gray600,
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(
                        translation.trim(),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }

            item {
                Spacer(Modifier.height(40.dp))
            }
        }
    }
}
