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
import com.lz.model.regulatory.codes.BuildingCode
import com.lz.model.structural.DesignMethodology
import com.lz.model.structural.IssueSeverity
import com.lz.model.structural.MaterialType
import com.lz.vectos.domain.structural.*
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
    val standards by viewModel.standards.collectAsState()
    val scrollState = rememberScrollState()

    projectState.let { activeProject ->
        var units by remember { mutableStateOf(activeProject.designContext.units) }
        var methodology by remember { mutableStateOf(activeProject.designContext.methodology) }
        var buildingCode by remember { mutableStateOf(activeProject.designContext.buildingCode) }
        var standard by remember { mutableStateOf(activeProject.designContext.loadingStandard) }
        var materialStandards by remember { mutableStateOf(activeProject.designContext.materialStandards) }

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
                            viewModel.updateProjectSettings(units, methodology, buildingCode, standard, materialStandards)
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
                    
                    ObjectSelector("Building Code", buildingCode, buildingCodes) { 
                        buildingCode = it 
                        // Note: In Task 6, we would ideally offer to reset standards here.
                    }
                    ObjectSelector("Loading Standard", standard, standards.filter { it.shortName.contains("ASCE") }) { standard = it }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Material Design Standards", style = MaterialTheme.typography.titleSmall)

                    // Dynamically render selectors for materials supported by the building code
                    MaterialType.entries.forEach { type ->
                        val currentStd = materialStandards[type] ?: buildingCode.defaultMaterialStandards[type]
                        if (currentStd != null) {
                            val options = standards.filter { 
                                when(type) {
                                    MaterialType.STEEL -> it.shortName.contains("AISC")
                                    MaterialType.WOOD -> it.shortName.contains("NDS")
                                    else -> true
                                }
                            }
                            
                            ObjectSelector(
                                label = "${type.name.lowercase().replaceFirstChar { it.uppercase() }} Standard",
                                selected = currentStd,
                                options = options
                            ) { newStd ->
                                materialStandards = materialStandards + (type to newStd)
                            }
                        }
                    }
                }

                HorizontalDivider()

                // 4. Validation Issues (Intelligence)
                val currentContext = activeProject.designContext.copy(
                    units = units,
                    methodology = methodology,
                    buildingCode = buildingCode,
                    loadingStandard = standard,
                    materialStandards = materialStandards
                )
                val issues = currentContext.validate()

                if (issues.isNotEmpty()) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Compatibility Notes", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.secondary)
                        issues.forEach { issue ->
                            val color = when(issue.severity) {
                                IssueSeverity.ERROR -> MaterialTheme.colorScheme.error
                                IssueSeverity.WARNING -> MaterialTheme.colorScheme.primary
                                IssueSeverity.INFO -> MaterialTheme.colorScheme.outline
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text("•", color = color, style = MaterialTheme.typography.bodySmall)
                                Text(issue.message, style = MaterialTheme.typography.bodySmall, color = color)
                            }
                        }
                    }
                }
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
