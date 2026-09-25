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
import com.lz.model.structural.ShapeType
import com.lz.model.structural.WoodGrade
import com.lz.model.structural.WoodSpecies
import com.lz.solver.material.WoodPropertyService

/**
 * Which [WoodSpecies] a glulam [ShapeType] groups together. Western Glulam
 * covers every glulam species pairing except Southern Pine (they all share
 * the same 1.5"-lamination standard sizing — see the size catalog); Southern
 * Pine Glulam is just SP/SP (1.375" laminations, its own width series).
 * Hardwoods-Glulam isn't tied to either size series yet (no data sourced),
 * so it's deliberately left out of both — picking it isn't possible via a
 * glulam ShapeType today.
 */
private fun glulamSpeciesFor(shapeType: ShapeType?): List<WoodSpecies> = when (shapeType) {
    ShapeType.GLULAM_WESTERN -> listOf(
        WoodSpecies.GLULAM_AC_AC, WoodSpecies.GLULAM_DF_DF, WoodSpecies.GLULAM_DF_HF,
        WoodSpecies.GLULAM_ES_ES, WoodSpecies.GLULAM_HF_HF, WoodSpecies.GLULAM_POC_POC,
        WoodSpecies.GLULAM_SPF_SPF
    )
    ShapeType.GLULAM_SOUTHERN_PINE -> listOf(WoodSpecies.GLULAM_SP_SP)
    else -> emptyList()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WoodMaterialPickerDialog(
    currentGrade: MaterialGrade.Wood?,
    // The currently-selected Geometry-tab shape type. Solid Sawn (or null,
    // defensively) shows the full species-then-grade picker below; a glulam
    // shape type collapses that into a single combination-symbol picker —
    // per Cody's request, once "Western Glulam" is chosen as the shape/
    // material sub-type, offering a separate Hem-Fir-vs-Douglas-Fir-style
    // species choice on top of that doesn't make sense; the user just picks
    // a combination symbol like "24F-V8", and its species pairing comes
    // along with it.
    shapeType: ShapeType? = null,
    onDismiss: () -> Unit,
    onConfirm: (MaterialGrade.Wood) -> Unit
) {
    val glulamSpecies = remember(shapeType) { glulamSpeciesFor(shapeType) }
    val isGlulamPicker = glulamSpecies.isNotEmpty()

    // (species, grade) options for the glulam flat picker: every valid
    // grade for every species in this shape type's group, in a single list.
    val glulamOptions = remember(glulamSpecies) {
        glulamSpecies.flatMap { sp -> WoodGrade.validGradesFor(sp).map { sp to it } }
    }

    var selectedSpecies by remember(shapeType) {
        mutableStateOf(
            when {
                currentGrade != null && (!isGlulamPicker || currentGrade.species in glulamSpecies) -> currentGrade.species
                isGlulamPicker -> glulamOptions.firstOrNull()?.first ?: WoodSpecies.DF_L
                else -> WoodSpecies.DF_L
            }
        )
    }
    var selectedGrade by remember(shapeType) {
        mutableStateOf(
            when {
                currentGrade != null && (!isGlulamPicker || currentGrade.species in glulamSpecies) -> currentGrade.grade
                isGlulamPicker -> glulamOptions.firstOrNull()?.second ?: WoodGrade.NO_2
                else -> WoodGrade.NO_2
            }
        )
    }

    // Recomputed on every species/grade change so the dialog can warn (and
    // block Confirm) immediately for a combination that isn't tabulated yet,
    // rather than only discovering it when the user taps Confirm. See
    // WoodPropertyService.getReferenceProperties doc comment for exactly
    // which species/grade combinations are verified so far.
    val props = remember(selectedSpecies, selectedGrade) {
        WoodPropertyService.getReferenceProperties(selectedSpecies, selectedGrade)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (isGlulamPicker) "Select Combination Symbol" else "Select Wood Species & Grade") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (isGlulamPicker) {
                    // Flat combination-symbol picker spanning every species
                    // in this shape type's group.
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            "Combination Symbol",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                        if (glulamOptions.isEmpty()) {
                            Text(
                                "No combination symbols mapped for this shape type yet.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                        glulamOptions.forEach { (species, grade) ->
                            Row(modifier = Modifier.fillMaxWidth()) {
                                RadioButton(
                                    selected = selectedSpecies == species && selectedGrade == grade,
                                    onClick = {
                                        selectedSpecies = species
                                        selectedGrade = grade
                                    }
                                )
                                Text(
                                    text = "${grade.displayName()} (${species.displayName()})",
                                    modifier = Modifier.padding(start = 8.dp)
                                )
                            }
                        }
                    }
                } else {
                    // Species Selection
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            "Species",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                        WoodSpecies.entries.filter { !it.isGlulam }.forEach { species ->
                            Row(modifier = Modifier.fillMaxWidth()) {
                                RadioButton(
                                    selected = selectedSpecies == species,
                                    onClick = {
                                        // Reset the grade whenever the new
                                        // species doesn't offer the currently
                                        // selected grade, so a grade from the
                                        // old species can't silently linger
                                        // paired with one it was never valid
                                        // for.
                                        val validGrades = WoodGrade.validGradesFor(species)
                                        if (selectedGrade !in validGrades) {
                                            validGrades.firstOrNull()?.let { selectedGrade = it }
                                        }
                                        selectedSpecies = species
                                    }
                                )
                                Text(
                                    text = species.displayName(),
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
                        val validGrades = WoodGrade.validGradesFor(selectedSpecies)
                        validGrades.forEach { grade ->
                            Row(modifier = Modifier.fillMaxWidth()) {
                                RadioButton(
                                    selected = selectedGrade == grade,
                                    onClick = { selectedGrade = grade }
                                )
                                Text(
                                    text = grade.displayName(),
                                    modifier = Modifier.padding(start = 8.dp)
                                )
                            }
                        }
                    }
                }

                if (props == null) {
                    Text(
                        "Reference design values for this combination " +
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
                        name = if (isGlulamPicker) {
                            selectedGrade.displayName()
                        } else {
                            "${selectedSpecies.displayName()} ${selectedGrade.displayName()}"
                        },
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

/**
 * Renders a [WoodSpecies] the way an engineer would recognize it: plain
 * species names as-is ("HEM FIR"), glulam species pairings in real
 * "outer/core" notation ("DF/DF") rather than the raw enum name
 * ("GLULAM DF DF"). "Hardwoods-Glulam" is its own special case since it
 * doesn't follow the outer/core pairing pattern.
 */
fun WoodSpecies.displayName(): String {
    if (!isGlulam) return name.replace("_", " ")
    if (this == WoodSpecies.GLULAM_HARDWOODS) return "Hardwoods-Glulam"
    return name.removePrefix("GLULAM_").replace("_", "/")
}

/**
 * Renders a [WoodGrade] the way an engineer would recognize it: solid-sawn
 * grades as their plain name ("No. 2"), glulam combination symbols in their
 * real NDS/AITC form ("24F-V4", "24F-1.8E") rather than the raw enum name
 * ("G 24F V4", "G 24F 1 8E").
 */
fun WoodGrade.displayName(): String {
    val raw = name
    if (!raw.startsWith("G_")) return raw.replace("_", " ")

    // A handful of ESR-1940 symbols use a "<stress-species>/<species-code><n>"
    // form (species abbreviation appears mid-symbol, joined by "/" not "-")
    // that the generic underscore-to-dash rule below can't distinguish from
    // an ordinary dash-joined symbol.
    val speciesSlashOverrides = mapOf(
        WoodGrade.G_20F_E_ES1 to "20F-E/ES1",
        WoodGrade.G_20F_E_SPF1 to "20F-E/SPF1",
        WoodGrade.G_22F_V_POC1 to "22F-V/POC1",
        WoodGrade.G_22F_V_POC2 to "22F-V/POC2"
    )
    speciesSlashOverrides[this]?.let { return it }

    val rest = raw.removePrefix("G_")
    // Legacy "<stress class>F_<MOE-integer>_<MOE-decimal>E" naming, e.g.
    // G_24F_1_8E -> "24F-1.8E" (the "." is a decimal point, not a dash).
    val legacyMoeStyle = Regex("""^(\d+F)_(\d)_(\d+E)$""")
    val match = legacyMoeStyle.matchEntire(rest)
    return if (match != null) {
        "${match.groupValues[1]}-${match.groupValues[2]}.${match.groupValues[3]}"
    } else {
        // NDS Table 5A combination-symbol naming, e.g. G_16F_E3 -> "16F-E3".
        rest.replace("_", "-")
    }
}