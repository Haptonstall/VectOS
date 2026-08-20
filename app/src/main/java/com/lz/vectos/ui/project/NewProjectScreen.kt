package com.lz.vectos.ui.project

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lz.domain.project.Project
import com.lz.model.regulatory.codes.BuildingCode
import com.lz.model.structural.DesignMethodology
import com.lz.model.units.UnitSystem
import com.lz.vectos.presentation.ProjectViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewProjectScreen(
    viewModel: ProjectViewModel,
    projectToEdit: Project? = null,
    onProjectCreated: () -> Unit,
    onCancel: () -> Unit
) {
    val buildingCodes by viewModel.buildingCodes.collectAsState(initial = emptyList())
    
    var name by remember { mutableStateOf(projectToEdit?.name ?: "") }
    var projectNumber by remember { mutableStateOf(projectToEdit?.projectNumber ?: "") }
    var description by remember { mutableStateOf(projectToEdit?.description ?: "") }
    var client by remember { mutableStateOf(projectToEdit?.clientName ?: "") }
    var engineer by remember { mutableStateOf(projectToEdit?.engineerName ?: "") }
    var firmName by remember { mutableStateOf(projectToEdit?.firmName ?: "") }
    
    var streetAddress by remember { mutableStateOf(projectToEdit?.coordinates?.streetAddress ?: "") }
    var city by remember { mutableStateOf(projectToEdit?.coordinates?.city ?: "") }
    var state by remember { mutableStateOf(projectToEdit?.coordinates?.state ?: "") }
    var zipCode by remember { mutableStateOf(projectToEdit?.coordinates?.zipCode ?: "") }
    
    var selectedCode by remember { mutableStateOf<BuildingCode?>(null) }
    var unitSystem by remember { mutableStateOf(projectToEdit?.settings?.unitSystem ?: UnitSystem.IMPERIAL) }
    var methodology by remember { mutableStateOf(projectToEdit?.settings?.designMethodology ?: DesignMethodology.ASD) }
    
    var showCodeDropdown by remember { mutableStateOf(false) }

    // Initialize selectedCode when buildingCodes are loaded
    LaunchedEffect(buildingCodes, projectToEdit) {
        if (selectedCode == null && buildingCodes.isNotEmpty()) {
            selectedCode = if (projectToEdit != null) {
                buildingCodes.find { it.id == projectToEdit.settings.buildingCodeId }
            } else {
                buildingCodes.find { it.id == "IBC_2021" } ?: buildingCodes.firstOrNull()
            }
        }
    }

    val saveEnabled = name.isNotBlank() && selectedCode != null

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (projectToEdit == null) "New Project" else "Edit Project") },
                navigationIcon = {
                    IconButton(onClick = onCancel) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            selectedCode?.let { code ->
                                if (projectToEdit == null) {
                                    viewModel.createProject(
                                        name = name,
                                        projectNumber = projectNumber,
                                        description = description,
                                        client = client,
                                        engineer = engineer,
                                        firmName = firmName,
                                        streetAddress = streetAddress,
                                        city = city,
                                        state = state,
                                        zipCode = zipCode,
                                        units = unitSystem,
                                        methodology = methodology,
                                        buildingCode = code
                                    )
                                } else {
                                    val updatedProject = projectToEdit.copy(
                                        name = name,
                                        projectNumber = projectNumber,
                                        description = description,
                                        clientName = client,
                                        engineerName = engineer,
                                        settings = projectToEdit.settings.copy(
                                            unitSystem = unitSystem,
                                            designMethodology = methodology,
                                            buildingCodeId = code.id
                                        ),
                                        coordinates = projectToEdit.coordinates.copy(
                                            streetAddress = streetAddress,
                                            city = city,
                                            state = state,
                                            zipCode = zipCode
                                        )
                                    )
                                    viewModel.updateProject(updatedProject)
                                }
                                onProjectCreated()
                            }
                        },
                        enabled = saveEnabled
                    ) {
                        Text(if (projectToEdit == null) "CREATE" else "SAVE")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Project Name") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            OutlinedTextField(
                value = projectNumber,
                onValueChange = { projectNumber = it },
                label = { Text("Project Number") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            OutlinedTextField(
                value = streetAddress,
                onValueChange = { streetAddress = it },
                label = { Text("Street Address") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = city,
                    onValueChange = { city = it },
                    label = { Text("City") },
                    modifier = Modifier.weight(1.5f),
                    shape = RoundedCornerShape(12.dp)
                )
                OutlinedTextField(
                    value = state,
                    onValueChange = { state = it },
                    label = { Text("State") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                )
                OutlinedTextField(
                    value = zipCode,
                    onValueChange = { zipCode = it },
                    label = { Text("Zip") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                )
            }

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                shape = RoundedCornerShape(12.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = client,
                    onValueChange = { client = it },
                    label = { Text("Client") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                )
                OutlinedTextField(
                    value = engineer,
                    onValueChange = { engineer = it },
                    label = { Text("Engineer") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                )
            }

            // Building Code Selector
            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = selectedCode?.longName ?: "Select Code",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Primary Building Code") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    trailingIcon = {
                        IconButton(onClick = { showCodeDropdown = true }) {
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                        }
                    }
                )
                DropdownMenu(
                    expanded = showCodeDropdown,
                    onDismissRequest = { showCodeDropdown = false },
                    modifier = Modifier.fillMaxWidth(0.85f)
                ) {
                    buildingCodes.forEach { code ->
                        DropdownMenuItem(
                            text = { Text(code.longName) },
                            onClick = {
                                selectedCode = code
                                showCodeDropdown = false
                            }
                        )
                    }
                }
            }

            // Governing Standards Summary
            selectedCode?.let { code ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            "Governing Standards (Reference)",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        code.standards.forEach { standard ->
                            Text(
                                "${standard.shortName}: ${standard.shortName}", // Adjust based on desired display
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        if (code.standards.isEmpty()) {
                            Text("No associated standards", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }

            // Unit System Toggle
            Text("Unit System", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
            CustomToggle(
                options = listOf("Metric" to UnitSystem.METRIC, "Imperial" to UnitSystem.IMPERIAL),
                selected = unitSystem,
                onSelected = { unitSystem = it }
            )

            // Design Methodology Toggle
            Text("Design Methodology", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
            CustomToggle(
                options = listOf("ASD" to DesignMethodology.ASD, "LRFD" to DesignMethodology.LRFD),
                selected = methodology,
                onSelected = { methodology = it }
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun <T> CustomToggle(
    options: List<Pair<String, T>>,
    selected: T,
    onSelected: (T) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(24.dp))
            .background(Color.White, RoundedCornerShape(24.dp)),
        verticalAlignment = Alignment.CenterVertically
    ) {
        options.forEachIndexed { index, option ->
            val isSelected = option.second == selected
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .background(
                        if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                        RoundedCornerShape(24.dp)
                    )
                    .clickable { onSelected(option.second) },
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (isSelected) {
                        Icon(
                            Icons.Default.Check, 
                            contentDescription = null, 
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                    }
                    Text(
                        text = option.first,
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        fontSize = 14.sp
                    )
                }
            }
            if (index < options.size - 1) {
                VerticalDivider(color = MaterialTheme.colorScheme.outline, modifier = Modifier.fillMaxHeight(0.6f))
            }
        }
    }
}
