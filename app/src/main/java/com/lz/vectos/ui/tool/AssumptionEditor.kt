package com.lz.vectos.ui.tool

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.lz.vectos.domain.provenance.AssumptionCategory
import com.lz.vectos.domain.provenance.AssumptionSource
import com.lz.vectos.domain.provenance.CalculationAssumption

@Composable
fun AssumptionEditor(
    assumptions: List<CalculationAssumption>,
    onAdd: (String, AssumptionCategory) -> Unit,
    onUpdate: (String, String) -> Unit,
    onToggle: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showAddDialog by remember { mutableStateOf(false) }
    
    val systemAssumptions = assumptions.filter { it.source != AssumptionSource.USER }
    val userAssumptions = assumptions.filter { it.source == AssumptionSource.USER }

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Basis of Calculation", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Button(onClick = { showAddDialog = true }, contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(4.dp))
                Text("Add Assumption")
            }
        }

        Spacer(Modifier.height(16.dp))

        Text("System Assumptions", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
        systemAssumptions.forEach { assumption ->
            AssumptionItem(
                assumption = assumption,
                onToggle = { onToggle(assumption.id) },
                onEdit = null // System assumptions usually not edited, just disabled
            )
        }

        if (userAssumptions.isNotEmpty()) {
            Spacer(Modifier.height(16.dp))
            Text("Engineer Notes & Assumptions", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
            userAssumptions.forEach { assumption ->
                var editing by remember { mutableStateOf(false) }
                if (editing) {
                    EditAssumptionInline(
                        initialValue = assumption.description,
                        onConfirm = { 
                            onUpdate(assumption.id, it)
                            editing = false
                        },
                        onCancel = { editing = false }
                    )
                } else {
                    AssumptionItem(
                        assumption = assumption,
                        onToggle = { onToggle(assumption.id) },
                        onEdit = { editing = true }
                    )
                }
            }
        }
    }

    if (showAddDialog) {
        AddAssumptionDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { desc, cat ->
                onAdd(desc, cat)
                showAddDialog = false
            }
        )
    }
}

@Composable
fun AssumptionItem(
    assumption: CalculationAssumption,
    onToggle: () -> Unit,
    onEdit: (() -> Unit)?
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = assumption.isEnabled,
            onCheckedChange = { onToggle() }
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = assumption.description,
                style = MaterialTheme.typography.bodyMedium,
                color = if (assumption.isEnabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.outline
            )
            Text(
                text = "${assumption.category} | ${assumption.source}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.outline
            )
        }
        if (onEdit != null && assumption.isEnabled) {
            IconButton(onClick = onEdit) {
                Icon(Icons.Default.Edit, contentDescription = "Edit", modifier = Modifier.size(20.dp))
            }
        }
    }
}

@Composable
fun EditAssumptionInline(
    initialValue: String,
    onConfirm: (String) -> Unit,
    onCancel: () -> Unit
) {
    var text by remember { mutableStateOf(initialValue) }
    Column(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
        OutlinedTextField(
            value = text,
            onValueChange = { text = it },
            modifier = Modifier.fillMaxWidth(),
            textStyle = MaterialTheme.typography.bodyMedium
        )
        Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
            TextButton(onClick = onCancel) { Text("Cancel") }
            TextButton(onClick = { onConfirm(text) }) { Text("Save") }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddAssumptionDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, AssumptionCategory) -> Unit
) {
    var description by remember { mutableStateOf("") }
    var category by remember { mutableStateOf(AssumptionCategory.GENERAL) }
    var expanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Engineering Assumption") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = category.name,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Category") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        AssumptionCategory.entries.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat.name) },
                                onClick = {
                                    category = cat
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(description, category) },
                enabled = description.isNotBlank()
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
