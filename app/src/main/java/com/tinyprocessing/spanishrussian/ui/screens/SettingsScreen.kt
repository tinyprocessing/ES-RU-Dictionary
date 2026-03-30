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
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.tinyprocessing.spanishrussian.R
import com.tinyprocessing.spanishrussian.data.AppSettings
import com.tinyprocessing.spanishrussian.ui.theme.Gray400
import com.tinyprocessing.spanishrussian.ui.theme.Gray500
import com.tinyprocessing.spanishrussian.ui.theme.Gray800
import com.tinyprocessing.spanishrussian.ui.theme.White

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    var alwaysEnabled by remember { mutableStateOf(AppSettings.isMultitranAlwaysEnabled(context)) }
    var fallbackEnabled by remember { mutableStateOf(AppSettings.isMultitranFallbackEnabled(context)) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .windowInsetsPadding(WindowInsets.statusBars)
    ) {
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
            Text(
                "Settings",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(Modifier.weight(1f))
            Spacer(Modifier.size(48.dp))
        }

        Spacer(Modifier.height(12.dp))

        Text(
            "ONLINE TRANSLATIONS",
            style = MaterialTheme.typography.labelMedium,
            color = Gray500,
            modifier = Modifier.padding(start = 20.dp, end = 20.dp, bottom = 8.dp),
        )

        // Always show online results
        SettingsToggle(
            title = "Always show Multitran",
            description = "Show online translations alongside offline results for every search. Useful to compare and see verb forms, usage examples.",
            checked = alwaysEnabled,
            onCheckedChange = {
                alwaysEnabled = it
                AppSettings.setMultitranAlwaysEnabled(context, it)
                // If "always" is on, fallback is redundant — disable it
                if (it) {
                    fallbackEnabled = false
                    AppSettings.setMultitranFallbackEnabled(context, false)
                }
            },
        )

        HorizontalDivider(color = Gray800, modifier = Modifier.padding(horizontal = 20.dp))

        // Fallback only when no results
        SettingsToggle(
            title = "Online fallback",
            description = "Search Multitran only when no offline results found. Good for conjugated verb forms and slang.",
            checked = fallbackEnabled,
            enabled = !alwaysEnabled,
            onCheckedChange = {
                fallbackEnabled = it
                AppSettings.setMultitranFallbackEnabled(context, it)
            },
        )

        Spacer(Modifier.height(12.dp))

        Text(
            "Requires internet. Data from multitran.com (ES-RU)",
            style = MaterialTheme.typography.labelMedium,
            color = Gray500,
            modifier = Modifier.padding(horizontal = 20.dp),
        )
    }
}

@Composable
private fun SettingsToggle(
    title: String,
    description: String,
    checked: Boolean,
    enabled: Boolean = true,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f).padding(end = 16.dp)) {
            Text(
                title,
                style = MaterialTheme.typography.titleMedium,
                color = if (enabled) White else Gray500,
            )
            Spacer(Modifier.height(6.dp))
            Text(
                description,
                style = MaterialTheme.typography.bodyMedium,
                color = if (enabled) Gray400 else Gray500,
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            enabled = enabled,
            colors = SwitchDefaults.colors(
                checkedThumbColor = White,
                checkedTrackColor = Gray500,
                uncheckedThumbColor = Gray500,
                uncheckedTrackColor = Gray800,
            ),
        )
    }
}
