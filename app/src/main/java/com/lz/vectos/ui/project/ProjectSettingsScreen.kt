package com.lz.vectos.ui.project

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.lz.model.regulatory.codes.BuildingCode
import com.lz.model.regulatory.codes.Standard
import com.lz.model.structural.DesignMethodology
import com.lz.model.units.UnitSystem
import com.lz.vectos.presentation.ProjectViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectSettingsScreen(
    viewModel: ProjectViewModel,
    onBack: () -> Unit,
) {
    val projectState by viewModel.activeProject.collectAsState()
    val buildingCodes by viewModel.buildingCodes.collectAsState()
    val scrollState = rememberScrollState()

    projectState.let { activeProject ->
        var units by remember { mutableStateOf(activeProject.settings.unitSystem) }
        var methodology by remember { mutableStateOf(activeProject.settings.designMethodology) }
        // Note: activeProject.settings.buildingCode is an enum, we need to find the full object
        var selectedCodeId by remember { mutableStateOf(activeProject.settings.buildingCode.name) }
        var buildingCode by remember { mutableStateOf<BuildingCode?>(null) }

        LaunchedEffect(buildingCodes, selectedCodeId) {
            buildingCode = buildingCodes.find { it.id == selectedCodeId }
        }

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
                            buildingCode?.let {
                                viewModel.updateProjectSettings(units, methodology, it)
                                onBack()
                            }
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
                                    label = { Text(method.name) }
                                )
                            }
                        }
                    }
                }

                HorizontalDivider()

                // 3. Codes & Standards
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text("Governing Codes", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    
                    buildingCode?.let { currentCode ->
                        ObjectSelector("Building Code", currentCode, buildingCodes) { 
                            selectedCodeId = it.id
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Display governing standards for information
                    buildingCode?.let { code ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    "Governing Standards (Auto-Resolved)",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                code.standards.forEach { standard ->
                                    Text(
                                        standard.shortName,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }

                HorizontalDivider()

                // 4. Validation Issues (Intelligence)
                // Note: Validation logic will be updated when the domain validation service is ready
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun <T : Any> ObjectSelector(
    label: String,
    selected: T,
    options: List<T>,
    onSelect: (T) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    
    val displayName = when (selected) {
        is BuildingCode -> selected.shortName
        is Standard -> selected.shortName
        else -> selected.toString()
    }

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(label, style = MaterialTheme.typography.labelMedium)
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = it }
        ) {
            OutlinedTextField(
                value = displayName,
                onValueChange = {},
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable, true).fillMaxWidth()
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                options.forEach { option ->
                    val optionName = when (option) {
                        is BuildingCode -> option.shortName
                        is Standard -> option.shortName
                        else -> option.toString()
                    }
                    DropdownMenuItem(
                        text = { Text(optionName) },
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
