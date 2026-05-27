package com.lz.vectos.ui.project

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.lz.vectos.domain.structural.*
import com.lz.vectos.domain.units.UnitSystem
import com.lz.vectos.viewmodel.ProjectViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectSettingsScreen(
    viewModel: ProjectViewModel,
    onBack: () -> Unit
) {
    val project by viewModel.activeProject.collectAsState()
    val scrollState = rememberScrollState()

    project?.let { activeProject ->
        var units by remember { mutableStateOf(activeProject.designContext.units) }
        var methodology by remember { mutableStateOf(activeProject.designContext.methodology) }
        var ibc by remember { mutableStateOf(activeProject.designContext.ibcEdition) }
        var asce by remember { mutableStateOf(activeProject.designContext.asceEdition) }
        var aisc by remember { mutableStateOf(activeProject.designContext.aiscEdition) }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Project Settings") },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    },
                    actions = {
                        TextButton(onClick = {
                            viewModel.updateProjectSettings(units, methodology, ibc, asce, aisc)
                            onBack()
                        }) {
                            Text("SAVE")
                        }
                    }
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .padding(padding)
                    .padding(16.dp)
                    .verticalScroll(scrollState)
                    .fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // 1. General Info
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Project Info", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    OutlinedTextField(
                        value = activeProject.name,
                        onValueChange = {},
                        label = { Text("Project Name") },
                        readOnly = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                HorizontalDivider()

                // 2. Units & Methodology
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text("Design Context", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    
                    Column {
                        Text("Unit System", style = MaterialTheme.typography.labelMedium)
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            UnitSystem.entries.forEach { system ->
                                FilterChip(
                                    selected = units == system,
                                    onClick = { units = system },
                                    label = { Text(system.name) }
                                )
                            }
                        }
                    }

                    Column {
                        Text("Design Methodology", style = MaterialTheme.typography.labelMedium)
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            DesignMethodology.entries.forEach { method ->
                                FilterChip(
                                    selected = methodology == method,
                                    onClick = { methodology = method },
                                    label = { Text(method.label) }
                                )
                            }
                        }
                    }
                }

                HorizontalDivider()

                // 3. Codes & Standards
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text("Governing Codes", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    
                    EditionSelector("International Building Code (IBC)", ibc, IbcEdition.entries) { ibc = it }
                    EditionSelector("ASCE 7 (Minimum Design Loads)", asce, AsceEdition.entries) { asce = it }
                    EditionSelector("AISC 360 (Steel Specification)", aisc, AiscEdition.entries) { aisc = it }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun <T : Enum<T>> EditionSelector(
    label: String,
    selected: T,
    options: List<T>,
    onSelect: (T) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(label, style = MaterialTheme.typography.labelMedium)
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = it }
        ) {
            OutlinedTextField(
                value = selected.toString().replace("_", " "),
                onValueChange = {},
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier.menuAnchor().fillMaxWidth()
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option.toString().replace("_", " ")) },
                        onClick = {
                            onSelect(option)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}
