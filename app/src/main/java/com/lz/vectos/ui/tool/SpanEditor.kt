package com.lz.vectos.ui.tool

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.lz.vectos.domain.structural.SpanGeometry
import com.lz.vectos.domain.units.Length
import java.util.UUID

@Composable
fun SpanEditor(
    spans: List<SpanGeometry>,
    activeSpanId: UUID?,
    onAddSpan: () -> Unit,
    onRemoveSpan: (UUID) -> Unit,
    onUpdateSpanLength: (UUID, Length) -> Unit,
    onSelectSpan: (UUID) -> Unit,
    modifier: Modifier = Modifier
) {
    var spanToRemove by remember { mutableStateOf<UUID?>(null) }

    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Beam Layout", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Button(
                onClick = onAddSpan,
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                modifier = Modifier.height(32.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text("Add Span", style = MaterialTheme.typography.labelMedium)
            }
        }

        spans.forEachIndexed { index, span ->
            SpanItem(
                index = index,
                span = span,
                isActive = span.id == activeSpanId,
                canRemove = spans.size > 1,
                onRemove = { spanToRemove = span.id },
                onUpdateLength = { onUpdateSpanLength(span.id, it) },
                onSelect = { onSelectSpan(span.id) }
            )
        }
    }

    if (spanToRemove != null) {
        AlertDialog(
            onDismissRequest = { spanToRemove = null },
            title = { Text("Remove Span?") },
            text = { Text("Removing this span will also delete all loads associated with it. This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        spanToRemove?.let { onRemoveSpan(it) }
                        spanToRemove = null
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Remove")
                }
            },
            dismissButton = {
                TextButton(onClick = { spanToRemove = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun SpanItem(
    index: Int,
    span: SpanGeometry,
    isActive: Boolean,
    canRemove: Boolean,
    onRemove: () -> Unit,
    onUpdateLength: (Length) -> Unit,
    onSelect: () -> Unit
) {
    var lengthText by remember(span.length) { mutableStateOf(span.length.meters.toString()) }

    Card(
        onClick = onSelect,
        colors = CardDefaults.cardColors(
            containerColor = if (isActive) 
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f) 
            else 
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        ),
        border = if (isActive) androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Span ${index + 1}", 
                    style = MaterialTheme.typography.labelLarge, 
                    fontWeight = FontWeight.Bold,
                    color = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                )
                
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = lengthText,
                        onValueChange = { 
                            lengthText = it
                            it.toDoubleOrNull()?.let { m -> onUpdateLength(Length(m)) }
                        },
                        label = { Text("Length") },
                        suffix = { Text("m") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        textStyle = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.width(120.dp).height(56.dp)
                    )
                    
                    Text(
                        "Simply Supported", 
                        style = MaterialTheme.typography.labelSmall, 
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }

            if (canRemove) {
                IconButton(onClick = onRemove) {
                    Icon(Icons.Default.Delete, contentDescription = "Remove", tint = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}
