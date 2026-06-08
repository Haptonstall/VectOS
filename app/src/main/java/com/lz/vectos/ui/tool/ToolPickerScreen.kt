package com.lz.vectos.ui.tool

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.lz.vectos.ui.calculator.CalculatorDefinition
import com.lz.vectos.ui.calculator.CalculatorRegistry
import com.lz.vectos.ui.calculator.CalculatorRoute
import com.lz.vectos.presentation.CalculationContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ToolPickerScreen(
    context: CalculationContext,
    onToolSelected: (CalculatorRoute) -> Unit,
    onBack: () -> Unit
) {
    val isQuickCalc = context is CalculationContext.QuickCalcContext
    val tools = CalculatorRegistry.calculators

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Select Tool") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            val contextLabel = when (context) {
                is CalculationContext.ProjectContext -> "Project: ${context.project.name}"
                is CalculationContext.QuickCalcContext -> "Quick Calc Mode"
            }

            Text(
                text = contextLabel,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(16.dp)
            )

            LazyVerticalGrid(
                columns = GridCells.Fixed(1),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(tools) { tool ->
                    val isEnabled = !isQuickCalc || tool.supportsQuickCalc
                    ToolCard(
                        tool = tool,
                        isEnabled = isEnabled,
                        onClick = {
                            if (isEnabled) {
                                onToolSelected(tool.createRoute(context))
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun ToolCard(
    tool: CalculatorDefinition,
    isEnabled: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = isEnabled) { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (isEnabled) 
                MaterialTheme.colorScheme.surfaceVariant 
            else 
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = tool.icon,
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = if (isEnabled) 
                    MaterialTheme.colorScheme.primary 
                else 
                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = tool.displayName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isEnabled) 
                        MaterialTheme.colorScheme.onSurfaceVariant 
                    else 
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
                )
                Text(
                    text = tool.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isEnabled) 
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    else 
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
                )
            }
            if (!isEnabled) {
                Text(
                    "Project Required",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}
