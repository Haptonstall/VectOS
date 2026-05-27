package com.lz.vectos.ui.beam

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.lz.vectos.domain.beam.SectionProfile

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SectionPicker(
    sections: List<SectionProfile>,
    selectedSection: SectionProfile?,
    onSectionSelected: (SectionProfile?) -> Unit,
    modifier: Modifier = Modifier
) {
    var showDialog by remember { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            "Section Profile",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary
        )
        
        OutlinedCard(
            onClick = { showDialog = true },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp)
        ) {
            Row(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = selectedSection?.name ?: "Manual Entry / Custom Section",
                    style = MaterialTheme.typography.bodyLarge
                )
                if (selectedSection != null) {
                    Text(
                        text = "Standardized",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }
        }
    }

    if (showDialog) {
        SectionSelectionDialog(
            sections = sections,
            onDismiss = { showDialog = false },
            onSelect = {
                onSectionSelected(it)
                showDialog = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SectionSelectionDialog(
    sections: List<SectionProfile>,
    onDismiss: () -> Unit,
    onSelect: (SectionProfile?) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val filteredSections = remember(searchQuery) {
        sections.filter { it.name.contains(searchQuery, ignoreCase = true) }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.8f)
                .padding(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "Select Section",
                    style = MaterialTheme.typography.headlineSmall
                )
                
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text("Search sections") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                )

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        ListItem(
                            headlineContent = { Text("Manual Entry") },
                            supportingContent = { Text("Override with custom properties") },
                            modifier = Modifier.clickable { onSelect(null) }
                        )
                        HorizontalDivider()
                    }
                    
                    items(filteredSections) { section ->
                        ListItem(
                            headlineContent = { Text(section.name) },
                            supportingContent = { 
                                Text("${section.material} | Ix: ${String.format("%.2e", section.momentOfInertia.metersToFourth)} m⁴") 
                            },
                            modifier = Modifier.clickable { onSelect(section) }
                        )
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                }
            }
        }
    }
}
