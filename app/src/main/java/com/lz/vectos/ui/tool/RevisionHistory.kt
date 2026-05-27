package com.lz.vectos.ui.tool

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.lz.vectos.domain.versioning.CalculationVersion
import com.lz.vectos.domain.versioning.DiffCategory

@Composable
fun RevisionHistory(
    history: List<CalculationVersion>,
    onCreateRevision: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showCreateDialog by remember { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Calculation Revisions", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Button(onClick = { showCreateDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(4.dp))
                Text("New Revision")
            }
        }

        if (history.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
                Text("No revisions recorded yet.", color = MaterialTheme.colorScheme.outline)
            }
        } else {
            Spacer(Modifier.height(16.dp))
            history.asReversed().forEach { version ->
                RevisionCard(version)
                Spacer(Modifier.height(12.dp))
            }
        }
    }

    if (showCreateDialog) {
        CreateRevisionDialog(
            onDismiss = { showCreateDialog = false },
            onConfirm = { note ->
                onCreateRevision(note)
                showCreateDialog = false
            }
        )
    }
}

@Composable
fun RevisionCard(version: CalculationVersion) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Revision V${version.versionNumber}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Text(version.createdAt, style = MaterialTheme.typography.labelSmall)
            }
            
            Text(version.summaryNote, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(vertical = 8.dp))
            
            if (version.diffs.isNotEmpty()) {
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                Text("Changes detected:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                version.diffs.forEach { diff ->
                    Row(modifier = Modifier.fillMaxWidth().padding(top = 4.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.Info, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.secondary)
                        Text("${diff.description}: ${diff.beforeValue} → ${diff.afterValue}", style = MaterialTheme.typography.bodySmall)
                    }
                }
            } else if (version.versionNumber > 1) {
                Text("No technical changes from previous revision.", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
            }
        }
    }
}

@Composable
fun CreateRevisionDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var note by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Record Calculation Revision") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("This snapshots the current state of the calculation for project records.", style = MaterialTheme.typography.bodySmall)
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Revision summary (e.g. 'Updated for load change')") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(note) }, enabled = note.isNotBlank()) {
                Text("Create V-Next")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
