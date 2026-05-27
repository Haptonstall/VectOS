package com.lz.vectos.ui.tool

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.lz.vectos.domain.structural.LoadCombination

@Composable
fun LoadCombinationViewer(
    selectedCombination: LoadCombination?,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            "Combination Composition",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary
        )
        
        Card(
            modifier = Modifier.padding(top = 4.dp).fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            )
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                if (selectedCombination == null) {
                    Text(
                        "Showing Service / Nominal loads (1.0 factor).",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                } else {
                    Text(
                        text = selectedCombination.name,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        selectedCombination.factors.forEach { (caseId, factor) ->
                            SuggestionChip(
                                onClick = { },
                                label = { Text("$factor $caseId") },
                                enabled = false
                            )
                        }
                    }
                }
            }
        }
    }
}
