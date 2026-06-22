package com.lz.ui.loads

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.lz.model.regulatory.LoadCombination

@Composable
fun LoadCombinationPicker(
    combinations: List<LoadCombination>,
    selectedCombination: LoadCombination?,
    onCombinationSelected: (LoadCombination?) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Load Combination",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary
            )

            TextButton(onClick = { onCombinationSelected(null) }) {
                Text("Show Governing", style = MaterialTheme.typography.labelSmall)
            }
        }

        ScrollableTabRow(
            selectedTabIndex = if (selectedCombination == null) 0 else combinations.indexOf(
                selectedCombination
            ) + 1,
            edgePadding = 0.dp,
            divider = {},
            containerColor = Color.Transparent,
            modifier = Modifier.padding(top = 4.dp)
        ) {
            Tab(
                selected = selectedCombination == null,
                onClick = { onCombinationSelected(null) },
                text = { Text("Governing", style = MaterialTheme.typography.labelLarge) }
            )
            combinations.forEach { combo ->
                Tab(
                    selected = combo == selectedCombination,
                    onClick = { onCombinationSelected(combo) },
                    text = {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(combo.name, style = MaterialTheme.typography.labelLarge)
                            Text(
                                combo.equationText,
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