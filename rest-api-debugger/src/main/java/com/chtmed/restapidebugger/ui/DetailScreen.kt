package com.chtmed.restapidebugger.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.chtmed.restapidebugger.model.ApiCallRecord
import com.chtmed.restapidebugger.store.ApiCallHistoryStore
import com.chtmed.restapidebugger.util.DebuggerColors
import com.chtmed.restapidebugger.util.JsonFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun DetailScreen(callId: String, onBackClick: () -> Unit) {
    val record = ApiCallHistoryStore.get(callId)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Call details") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
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
        if (record == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text("This call is no longer in history.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            SummaryHeader(record)

            if (record.queryParams.isNotEmpty()) {
                SectionCard(title = "Query parameters") {
                    record.queryParams.forEach { (key, value) -> KeyValueLine(key, value) }
                }
            }

            SectionCard(title = "Request") {
                Text(
                    text = "Headers",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                record.requestHeaders.forEach { (key, value) -> KeyValueLine(key, value) }

                if (!record.requestBody.isNullOrBlank()) {
                    Text(
                        text = "Body",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 10.dp)
                    )
                    CodeBlock(JsonFormatter.prettyPrint(record.requestBody))
                }
            }

            SectionCard(title = "Response") {
                KeyValueLine("Status", "${record.statusCode ?: "—"} ${record.statusMessage.orEmpty()}".trim())
                KeyValueLine("Duration", "${record.durationMs ?: 0} ms")
                if (record.errorMessage != null) {
                    KeyValueLine("Error", record.errorMessage)
                }

                if (record.responseHeaders.isNotEmpty()) {
                    Text(
                        text = "Headers",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 10.dp)
                    )
                    record.responseHeaders.forEach { (key, value) -> KeyValueLine(key, value) }
                }

                if (!record.responseBody.isNullOrBlank()) {
                    Text(
                        text = "Body",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 10.dp)
                    )
                    CodeBlock(JsonFormatter.prettyPrint(record.responseBody))
                }
            }
        }
    }
}

@Composable
private fun SummaryHeader(record: ApiCallRecord) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = record.method,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color(DebuggerColors.forMethod(record.method))
            )
            Text(
                text = record.url,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        Row(
            modifier = Modifier.padding(top = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val statusText = record.statusCode?.let { "$it ${record.statusMessage.orEmpty()}".trim() } ?: "ERROR"
            Text(
                text = statusText,
                style = MaterialTheme.typography.labelMedium,
                color = Color(DebuggerColors.forStatus(record.statusCode))
            )
            Text(
                text = "• ${record.durationMs ?: 0} ms",
                style = MaterialTheme.typography.labelMedium,
                color = Color(DebuggerColors.DURATION)
            )
        }
    }
}

@Composable
private fun SectionCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp)
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
            .padding(14.dp)
    ) {
        Text(
            text = title.uppercase(),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Box(modifier = Modifier.padding(top = 8.dp)) {
            Column { content() }
        }
    }
}

@Composable
private fun KeyValueLine(key: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)) {
        Text(
            text = "$key: ",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun CodeBlock(text: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background, RoundedCornerShape(8.dp))
            .padding(10.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
