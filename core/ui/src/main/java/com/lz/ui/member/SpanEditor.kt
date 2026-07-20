package com.lz.ui.member

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.lz.model.structural.BracingInput
import com.lz.model.structural.SpanGeometry
import com.lz.model.units.Length
import com.lz.model.units.feet
import com.lz.model.units.inFeet
import java.util.Locale
import java.util.UUID

@Composable
fun SpanEditor(
    spans: List<SpanGeometry>,
    activeSpanId: UUID?,
    spanBracing: Map<UUID, BracingInput> = emptyMap(),
    onAddSpan: () -> Unit,
    onRemoveSpan: (UUID) -> Unit,
    onUpdateSpanLength: (UUID, Length) -> Unit,
    onSelectSpan: (UUID) -> Unit,
    onEditBracing: (UUID) -> Unit,
    modifier: Modifier = Modifier
) {
    var spanToRemove by remember { mutableStateOf<UUID?>(null) }

    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Beam Layout",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Button(
                onClick = onAddSpan,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                shape = RoundedCornerShape(20.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                modifier = Modifier.height(40.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(4.dp))
                Text("Add Span", style = MaterialTheme.typography.labelLarge)
            }
        }

        spans.forEachIndexed { index, span ->
            SpanItem(
                index = index,
                span = span,
                bracing = spanBracing[span.id],
                isActive = span.id == activeSpanId,
                canRemove = spans.size > 1,
                onRemove = { spanToRemove = span.id },
                onUpdateLength = { onUpdateSpanLength(span.id, it) },
                onSelect = { onSelectSpan(span.id) },
                onEditBracing = { onEditBracing(span.id) }
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
    bracing: BracingInput?,
    isActive: Boolean,
    canRemove: Boolean,
    onRemove: () -> Unit,
    onUpdateLength: (Length) -> Unit,
    onSelect: () -> Unit,
    onEditBracing: () -> Unit
) {
    var lengthText by remember(span.id) { mutableStateOf(String.format(Locale.US, "%.2f", span.length.inFeet)) }

    Card(
        onClick = onSelect,
        colors = CardDefaults.cardColors(
            containerColor = if (isActive)
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)
            else
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.1f)
        ),
        shape = RoundedCornerShape(16.dp),
        border = if (isActive) BorderStroke(
            2.dp,
            MaterialTheme.colorScheme.primary
        ) else BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    "Span ${index + 1}",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Length Field
                    OutlinedTextField(
                        value = lengthText,
                        onValueChange = {
                            lengthText = it
                        },
                        label = { Text("Length", style = MaterialTheme.typography.labelSmall) },
                        suffix = {
                            Text(
                                "ft",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }, // Changed to ft as per image
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        textStyle = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier
                            .width(100.dp)
                            .onFocusChanged { focusState ->
                                if (!focusState.isFocused) {
                                    lengthText.toDoubleOrNull()?.let { m ->
                                        onUpdateLength(m.feet)
                                        // Format it back nicely after validation
                                        lengthText = String.format(Locale.US, "%.2f", m)
                                    } ?: run {
                                        // Reset to original value if invalid
                                        lengthText =
                                            String.format(Locale.US, "%.2f", span.length.inFeet)
                                    }
                                }
                            },
                        shape = RoundedCornerShape(8.dp),
                        singleLine = true
                    )

                    // Bracing Button
                    Surface(
                        onClick = onEditBracing,
                        color = Color(0xFFFFEBEE), // Light Pink/Red
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f).height(56.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 8.dp),
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                "Bracing",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF7D5248).copy(alpha = 0.6f)
                            )
                            Text(
                                if (bracing != null) "${bracing.topType.label} / ${bracing.bottomType.label}" else "Ends Only",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF7D5248)
                            )
                        }
                    }

                    // Deflection Button (Placeholder for now)
                    Surface(
                        onClick = { /* TODO: Deflection Dialog */ },
                        color = Color(0xFFFFF9C4), // Light Yellow
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f).height(56.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 8.dp),
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                "Deflect",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF7D5248).copy(alpha = 0.6f)
                            )
                            Text(
                                "floor",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF7D5248)
                            )
                        }
                    }
                }
            }

            if (canRemove) {
                IconButton(onClick = onRemove) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Remove",
                        tint = Color(0xFFD32F2F),
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }
    }
}