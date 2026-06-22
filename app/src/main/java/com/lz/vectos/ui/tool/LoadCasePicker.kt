package com.lz.vectos.ui.tool

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.lz.model.regulatory.LoadCategory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoadCasePicker(
    loadCases: List<LoadCategory>,
    activeCaseId: String,
    onCaseSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            "Load Case",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary
        )
        
        ScrollableTabRow(
            selectedTabIndex = loadCases.indexOfFirst { it.id == activeCaseId }.coerceAtLeast(0),
            edgePadding = 0.dp,
            divider = {},
            containerColor = androidx.compose.ui.graphics.Color.Transparent,
            modifier = Modifier.padding(top = 4.dp)
        ) {
            loadCases.forEach { case ->
                Tab(
                    selected = case.id == activeCaseId,
                    onClick = { onCaseSelected(case.id) },
                    text = {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(case.id, style = MaterialTheme.typography.labelLarge)
                            Text(
                                "${case.loads.size} loads", 
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    }
                )
            }
        }
    }
}
