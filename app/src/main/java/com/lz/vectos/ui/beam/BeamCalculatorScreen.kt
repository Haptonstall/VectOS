package com.lz.vectos.ui.beam

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lz.vectos.domain.beam.LoadType
import com.lz.vectos.domain.beam.Material
import com.lz.vectos.domain.units.*
import com.lz.vectos.domain.project.Project
import com.lz.vectos.viewmodel.BeamViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BeamCalculatorScreen(
    activeProject: Project,
    viewModel: BeamViewModel
) {
    val scrollState = rememberScrollState()
    val calculationHistory by viewModel.calculationHistory.collectAsState()
    var showHistory by remember { mutableStateOf(false) }
    val displayResult = viewModel.displayResult

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Beam Calculator") },
                actions = {
                    IconButton(onClick = { 
                        viewModel.loadHistory()
                        showHistory = true 
                    }) {
                        Icon(Icons.Default.Refresh, contentDescription = "History")
                    }
                }
            )
        }
    ) { paddingValues ->
        if (showHistory) {
            HistoryOverlay(
                history = calculationHistory,
                onSelect = { id ->
                    viewModel.loadCalculation(id)
                    showHistory = false
                },
                onDismiss = { showHistory = false },
                modifier = Modifier.padding(paddingValues)
            )
        } else {
            Column(
                modifier = Modifier
                    .padding(paddingValues)
                    .padding(16.dp)
                    .verticalScroll(scrollState)
                    .fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text("Project: ${activeProject.name}", style = MaterialTheme.typography.labelLarge)
                
                // Unit Selection
                Text("Units", fontWeight = FontWeight.Bold)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    UnitSystem.entries.forEach { system ->
                        FilterChip(
                            selected = viewModel.unitSystem == system,
                            onClick = { viewModel.unitSystem = system },
                            label = { Text(system.name) }
                        )
                    }
                }

                // Material Selection
                Text("Material", fontWeight = FontWeight.Bold)
                var expandedMaterial by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = expandedMaterial,
                    onExpandedChange = { expandedMaterial = !expandedMaterial }
                ) {
                    OutlinedTextField(
                        value = viewModel.material.name,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Select Material") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedMaterial) },
                        modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = expandedMaterial,
                        onDismissRequest = { expandedMaterial = false }
                    ) {
                        Material.entries.forEach { material ->
                            DropdownMenuItem(
                                text = { Text(material.name) },
                                onClick = {
                                    viewModel.material = material
                                    expandedMaterial = false
                                }
                            )
                        }
                    }
                }

                // Load Type Selection
                Text("Load Type", fontWeight = FontWeight.Bold)
                var expandedLoadType by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = expandedLoadType,
                    onExpandedChange = { expandedLoadType = !expandedLoadType }
                ) {
                    OutlinedTextField(
                        value = viewModel.loadType.name.replace("_", " "),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Select Load Type") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedLoadType) },
                        modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = expandedLoadType,
                        onDismissRequest = { expandedLoadType = false }
                    ) {
                        LoadType.entries.forEach { loadType ->
                            DropdownMenuItem(
                                text = { Text(loadType.name.replace("_", " ")) },
                                onClick = {
                                    viewModel.loadType = loadType
                                    expandedLoadType = false
                                }
                            )
                        }
                    }
                }

                // Inputs
                val lengthLabel = if (viewModel.unitSystem == UnitSystem.METRIC) "Length (m)" else "Length (ft)"
                val loadLabel = if (viewModel.unitSystem == UnitSystem.METRIC) {
                    if (viewModel.loadType == LoadType.POINT_LOAD_MIDSPAN) "Load (N)" else "Load (N/m)"
                } else {
                    if (viewModel.loadType == LoadType.POINT_LOAD_MIDSPAN) "Load (lb)" else "Load (lb/ft)"
                }
                val iLabel = if (viewModel.unitSystem == UnitSystem.METRIC) "Moment of Inertia (m⁴)" else "Moment of Inertia (in⁴)"

                OutlinedTextField(
                    value = viewModel.length,
                    onValueChange = { viewModel.length = it },
                    label = { Text(lengthLabel) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = viewModel.loadValue,
                    onValueChange = { viewModel.loadValue = it },
                    label = { Text(loadLabel) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = viewModel.momentOfInertia,
                    onValueChange = { viewModel.momentOfInertia = it },
                    label = { Text(iLabel) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )

                Button(
                    onClick = { viewModel.calculate() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Calculate")
                }

                // Results (Read-only pre-formatted Strings from Display Model)
                displayResult?.let { result ->
                    HorizontalDivider()
                    Text("Results", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                    
                    ResultRow("Max Bending Moment", result.maxBendingMoment)
                    ResultRow("Max Shear", result.maxShear)
                    ResultRow("Max Deflection", result.maxDeflection)
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Calculation ID: ${result.calculationId}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
        }
    }
}

@Composable
fun HistoryOverlay(
    history: List<com.lz.vectos.domain.calculation.CalculationMetadata>,
    onSelect: (java.util.UUID) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Keep internal UI helpers simple
    Column(modifier = modifier.fillMaxSize().padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Calculation History", style = MaterialTheme.typography.headlineSmall)
            TextButton(onClick = onDismiss) { Text("Close") }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        if (history.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No calculations saved yet.", color = MaterialTheme.colorScheme.outline)
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(history) { item ->
                    Card(
                        modifier = Modifier.fillMaxWidth().clickable { onSelect(item.id) },
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(item.name, fontWeight = FontWeight.Bold)
                            Text(
                                text = "ID: ${item.id}",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ResultRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = MaterialTheme.colorScheme.secondary)
        Text(value, fontWeight = FontWeight.Bold)
    }
}
