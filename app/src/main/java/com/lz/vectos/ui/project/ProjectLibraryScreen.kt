package com.lz.vectos.ui.project

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.lz.vectos.domain.calculation.EngineeringCalculation
import com.lz.vectos.domain.project.Project
import com.lz.vectos.viewmodel.ProjectViewModel
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectLibraryScreen(
    viewModel: ProjectViewModel,
    onOpenCalculation: (EngineeringCalculation) -> Unit,
    onBack: () -> Unit
) {
    val project by viewModel.activeProject.collectAsState()
    val calculationsMap by viewModel.calculations.collectAsState()
    val calculations = calculationsMap.values.toList()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(project?.name ?: "Project Library") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text(
                "Engineering Calculations",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            if (calculations.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "No calculations in this project yet.",
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    val grouped = calculations.groupBy { it.toolId }
                    grouped.forEach { (toolId, toolCalcs) ->
                        item {
                            Text(
                                text = toolId,
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                        }
                        items(toolCalcs) { calculation ->
                            CalculationLibraryItem(
                                calculation = calculation,
                                onClick = { onOpenCalculation(calculation) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CalculationLibraryItem(
    calculation: EngineeringCalculation,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = when (calculation.toolId) {
                    "BEAM" -> Icons.Default.LinearScale
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
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    Text(
                        "V${calculation.latestVersion.versionNumber}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Text(
                        calculation.latestVersion.provenance.sectionDesignation,
                        style = MaterialTheme.typography.labelSmall
                    )
                    Text(
                        calculation.latestVersion.provenance.buildingCode,
                        style = MaterialTheme.typography.labelSmall
                    )
                }
                Text(
                    "Modified: ${calculation.updatedAt.toLocalDate()}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.padding(top = 2.dp)
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
