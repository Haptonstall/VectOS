package com.lz.ui.loads

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import com.lz.model.structural.Load
import com.lz.model.structural.LoadCase
import com.lz.model.structural.StandardLoadCases
import com.lz.model.units.Force
import com.lz.model.units.Length

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoadCasePicker(
    loadCases: List<LoadCase>,
    activeCaseId: String,
    onCaseSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
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
            containerColor = Color.Transparent,
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

@Preview(showBackground = true)
@Composable
private fun LoadCasePickerPreview() {
    val cases = listOf(
        LoadCase(StandardLoadCases.DEAD, "Dead Load"),
        LoadCase(StandardLoadCases.LIVE, "Live Load", listOf(
            Load.PointLoad(
                value = Force(1000.0),
                spanId = java.util.UUID.randomUUID(),
                locationStart = Length(24.0)
            )
        )),
        LoadCase(StandardLoadCases.SNOW, "Snow Load")
    )
    MaterialTheme {
        LoadCasePicker(
            loadCases = cases,
            activeCaseId = StandardLoadCases.LIVE,
            onCaseSelected = {},
            modifier = Modifier.padding(16.dp)
        )
    }
}
