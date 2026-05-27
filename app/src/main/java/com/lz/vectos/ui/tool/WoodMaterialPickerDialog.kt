package com.lz.vectos.ui.tool

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.lz.vectos.domain.structural.MaterialGrade
import com.lz.vectos.domain.structural.WoodGrade
import com.lz.vectos.domain.structural.WoodPropertyService
import com.lz.vectos.domain.structural.WoodSpecies

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WoodMaterialPickerDialog(
    currentGrade: MaterialGrade.Wood?,
    onDismiss: () -> Unit,
    onConfirm: (MaterialGrade.Wood) -> Unit
) {
    var selectedSpecies by remember { mutableStateOf(currentGrade?.species ?: WoodSpecies.DF_L) }
    var selectedGrade by remember { mutableStateOf(currentGrade?.grade ?: WoodGrade.NO_2) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Wood Species & Grade") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Species Selection
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Species", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                    WoodSpecies.entries.forEach { species ->
                        Row(modifier = Modifier.fillMaxWidth()) {
                            RadioButton(
                                selected = selectedSpecies == species,
                                onClick = { selectedSpecies = species }
                            )
                            Text(
                                text = species.name.replace("_", " "),
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                    }
                }

                HorizontalDivider()

                // Grade Selection
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Grade", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                    WoodGrade.entries.filter { !it.name.startsWith("G_") || selectedSpecies.isGlulam }.forEach { grade ->
                        Row(modifier = Modifier.fillMaxWidth()) {
                            RadioButton(
                                selected = selectedGrade == grade,
                                onClick = { selectedGrade = grade }
                            )
                            Text(
                                text = grade.name.replace("_", " "),
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val props = WoodPropertyService.getReferenceProperties(selectedSpecies, selectedGrade)
                val newGrade = MaterialGrade.Wood(
                    id = "WOOD_${selectedSpecies.name}_${selectedGrade.name}",
                    name = "${selectedSpecies.name.replace("_", " ")} ${selectedGrade.name.replace("_", " ")}",
                    species = selectedSpecies,
                    grade = selectedGrade,
                    referenceBending = props.bending,
                    referenceShear = props.shear,
                    referenceCompressionParallel = props.compressionParallel,
                    referenceCompressionPerp = props.compressionPerp,
                    referenceTensionParallel = props.tensionParallel,
                    modulusOfElasticity = props.modulusOfElasticity,
                    shearModulus = props.shearModulus,
                    densityPcf = props.densityPcf
                )
                onConfirm(newGrade)
            }) {
                Text("Confirm")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
