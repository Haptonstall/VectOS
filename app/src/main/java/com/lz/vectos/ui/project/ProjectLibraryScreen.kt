package com.lz.vectos.ui.project

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.lz.domain.calculation.CalculationMetadata
import com.lz.domain.project.Project
import com.lz.vectos.presentation.ProjectViewModel
import java.util.UUID

/**
 * Maps a calculation's raw toolId (the runtime module's registered
 * navigation route, e.g. "beam.calculator") to a human-readable label for
 * display. :app can't reference feature module constants directly (dynamic
 * feature modules build after :app), so this is a plain string lookup —
 * add an entry here whenever a new tool module is registered.
 */
private fun toolDisplayName(toolId: String): String = when (toolId) {
    "beam.calculator" -> "Beam"
    else -> toolId
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectLibraryScreen(
    viewModel: ProjectViewModel,
    onOpenCalculation: (CalculationMetadata) -> Unit,
    onDeleteCalculation: (CalculationMetadata) -> Unit,
    onAddCalculation: () -> Unit,
    onProjectSettings: () -> Unit,
    onEditProject: () -> Unit,
    onBack: () -> Unit,
) {
    val project by viewModel.activeProject.collectAsState()
    val calculations by viewModel.calculations.collectAsState()
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner, project.id) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.refreshCalculations()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text(
                            project.name.split("\n").first(), 
                            style = MaterialTheme.typography.titleLarge,
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        )
                        if (!project.projectNumber.isNullOrBlank()) {
                            Text(
                                project.projectNumber!!, 
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = onProjectSettings) {
                        Icon(Icons.Default.Info, contentDescription = "Project Info")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddCalculation) {
                Icon(Icons.Default.Add, contentDescription = "New Calculation")
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
                ProjectSummaryCard(
                    project = project,
                    onEdit = onEditProject
                )
            }

            item {
                Text(
                    "Engineering Calculations",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            if (calculations.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "No calculations in this project yet.",
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            } else {
                val grouped = calculations.groupBy { it.toolId }
                grouped.forEach { (toolId, toolCalcs) ->
                    item {
                        Text(
                            text = toolDisplayName(toolId),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }
                    items(toolCalcs, key = { it.id }) { calculation ->
                        CalculationLibraryItem(
                            calculation = calculation,
                            onClick = { onOpenCalculation(calculation) },
                            onDelete = { onDeleteCalculation(calculation) }
                        )
                    }
                }
            }
            
            item {
                Spacer(modifier = Modifier.height(80.dp)) // Space for FAB
            }
        }
    }
}

@Composable
fun ProjectSummaryCard(
    project: Project,
    onEdit: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Box(modifier = Modifier.padding(16.dp)) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    "Project Summary", 
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    val client = project.clientName ?: "None"
                    val city = project.coordinates.city
                    val state = project.coordinates.state
                    val location = when {
                        city.isNotBlank() && state.isNotBlank() -> "$city, $state"
                        city.isNotBlank() -> city
                        state.isNotBlank() -> state
                        else -> "None"
                    }
                    
                    Text(
                        text = "Client: $client",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Site: $location",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TagItem(text = project.settings.buildingCodeId)
                    TagItem(text = project.settings.designMethodology.name)
                }
            }
            
            IconButton(
                onClick = onEdit,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 12.dp, y = (-12).dp)
            ) {
                Icon(
                    Icons.Default.Edit, 
                    contentDescription = "Edit Project",
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun TagItem(text: String) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
        color = Color.Transparent
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
        )
    }
}

// Removed private fun border

@Composable
fun CalculationLibraryItem(
    calculation: CalculationMetadata,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = when (calculation.toolId) {
                    "beam.calculator" -> Icons.Default.LinearScale
                    else -> Icons.Default.Description
                },
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = calculation.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "Created: ${calculation.createdAt.toLocalDate()}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
                Icon(
                    Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.outline
                )
            }
        }
    }
}
