package com.chtmed.restapidebugger.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.chtmed.restapidebugger.model.ApiCallRecord
import com.chtmed.restapidebugger.store.ApiCallHistoryStore
import com.chtmed.restapidebugger.util.DebuggerColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun HistoryScreen(onCallClick: (String) -> Unit) {
    val history by ApiCallHistoryStore.history.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("REST API Debugger") },
                actions = {
                    IconButton(onClick = { ApiCallHistoryStore.clear() }) {
                        Icon(Icons.Filled.DeleteSweep, contentDescription = "Clear history")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        if (history.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No API calls captured yet.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 4.dp)
            ) {
                items(items = history, key = { it.id }) { record ->
                    HistoryRow(record = record, onClick = { onCallClick(record.id) })
                }
            }
        }
    }
}

@Composable
private fun HistoryRow(record: ApiCallRecord, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        MethodBadge(method = record.method)

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = record.path,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1
            )
            Text(
                text = record.url,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
        }

        Column(horizontalAlignment = Alignment.End) {
            val statusText = record.statusCode?.toString() ?: "ERR"
            Text(
                text = statusText,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = Color(DebuggerColors.forStatus(record.statusCode))
            )
            Text(
                text = "${record.durationMs ?: 0} ms",
                style = MaterialTheme.typography.labelSmall,
                color = Color(DebuggerColors.DURATION)
            )
        }
    }
}

@Composable
private fun MethodBadge(method: String) {
    Box(
        modifier = Modifier
            .size(width = 56.dp, height = 24.dp)
            .background(
                color = Color(DebuggerColors.forMethod(method)).copy(alpha = 0.18f),
                shape = RoundedCornerShape(6.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = method,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = Color(DebuggerColors.forMethod(method))
        )
    }
}
