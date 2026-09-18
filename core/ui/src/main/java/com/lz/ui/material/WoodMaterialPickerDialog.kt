package com.lz.ui.material

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.lz.model.structural.MaterialGrade
import com.lz.model.structural.WoodGrade
import com.lz.model.structural.WoodSpecies
import com.lz.solver.material.WoodPropertyService

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WoodMaterialPickerDialog(
    currentGrade: MaterialGrade.Wood?,
    onDismiss: () -> Unit,
    onConfirm: (MaterialGrade.Wood) -> Unit
) {
    var selectedSpecies by remember { mutableStateOf(currentGrade?.species ?: WoodSpecies.DF_L) }
    var selectedGrade by remember { mutableStateOf(currentGrade?.grade ?: WoodGrade.NO_2) }

    // Recomputed on every species/grade change so the dialog can warn (and
    // block Confirm) immediately for a combination that isn't tabulated yet,
    // rather than only discovering it when the user taps Confirm. See
    // WoodPropertyService.getReferenceProperties doc comment — this is
    // deliberately null instead of a fabricated fallback for anything not
    // yet in the reference table (currently: every glulam combination).
    val props = remember(selectedSpecies, selectedGrade) {
        WoodPropertyService.getReferenceProperties(selectedSpecies, selectedGrade)
    }

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
                    Text(
                        "Species",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
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
                    Text(
                        "Grade",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                    WoodGrade.entries.filter { !it.name.startsWith("G_") || selectedSpecies.isGlulam }
                        .forEach { grade ->
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

                if (props == null) {
                    Text(
                        "Reference design values for this species/grade combination " +
                            "aren't available yet — Confirm is disabled until real NDS " +
                            "Supplement values are added for it.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = props != null,
                onClick = {
                    val resolvedProps = props ?: return@TextButton
                    val newGrade = MaterialGrade.Wood(
                        id = "WOOD_${selectedSpecies.name}_${selectedGrade.name}",
                        name = "${
                            selectedSpecies.name.replace(
                                "_",
                                " "
                            )
                        } ${selectedGrade.name.replace("_", " ")}",
                        species = selectedSpecies,
                        grade = selectedGrade,
                        referenceBending = resolvedProps.bending,
                        referenceShear = resolvedProps.shear,
                        referenceCompressionParallel = resolvedProps.compressionParallel,
                        referenceCompressionPerp = resolvedProps.compressionPerp,
                        referenceTensionParallel = resolvedProps.tensionParallel,
                        modulusOfElasticity = resolvedProps.modulusOfElasticity,
                        shearModulus = resolvedProps.shearModulus,
                        densityPcf = resolvedProps.densityPcf
                    )
                    onConfirm(newGrade)
                }
            ) {
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