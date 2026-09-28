package com.lz.ui.material

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.lz.model.regulatory.nds.NdsAdjustmentFactors

/**
 * One editable NDS adjustment factor. Description text is always visible
 * (not a tap/hover tooltip) — more reliable on mobile, and lets the user
 * see the NDS section reference and typical values without an extra tap.
 */
private data class FactorField(
    val label: String,
    val symbol: String,
    val description: String,
    val onValueChange: (String) -> Unit
)

/**
 * Editable form for all 10 [NdsAdjustmentFactors]. Each factor defaults to
 * 1.0 (no adjustment); CL, CF, and CP are flagged as auto-calculated by the
 * solver when left at 1.0, since those three are only ever manually
 * overridden when the user wants to bypass the automatic NDS 3.3.3 / Table
 * 4A / 3.7.1 calculation with a known value of their own.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdjustmentFactorsEditDialog(
    currentFactors: NdsAdjustmentFactors,
    onDismiss: () -> Unit,
    onConfirm: (NdsAdjustmentFactors) -> Unit
) {
    var cd by remember(currentFactors) { mutableStateOf(currentFactors.cd.toString()) }
    var cm by remember(currentFactors) { mutableStateOf(currentFactors.cm.toString()) }
    var ct by remember(currentFactors) { mutableStateOf(currentFactors.ct.toString()) }
    var cl by remember(currentFactors) { mutableStateOf(currentFactors.cl.toString()) }
    var cf by remember(currentFactors) { mutableStateOf(currentFactors.cf.toString()) }
    var cfu by remember(currentFactors) { mutableStateOf(currentFactors.cfu.toString()) }
    var ci by remember(currentFactors) { mutableStateOf(currentFactors.ci.toString()) }
    var cr by remember(currentFactors) { mutableStateOf(currentFactors.cr.toString()) }
    var cp by remember(currentFactors) { mutableStateOf(currentFactors.cp.toString()) }
    var cb by remember(currentFactors) { mutableStateOf(currentFactors.cb.toString()) }

    val fields = listOf(
        FactorField(
            label = "Load Duration Factor",
            symbol = "CD",
            description = "NDS Section 2.3.2. Adjusts for how long the governing load is " +
                "sustained — shorter-duration loads permit a higher design value. " +
                "Typical: 0.9 (permanent/dead), 1.0 (10-year/normal occupancy live), " +
                "1.15 (2-month/snow), 1.25 (7-day/construction), 1.6 (10-min/wind or " +
                "seismic), 2.0 (impact). Does not apply to modulus of elasticity.",
            onValueChange = { cd = it }
        ),
        FactorField(
            label = "Wet Service Factor",
            symbol = "CM",
            description = "NDS Table 4A/4B/5A. Reduces design values when in-service " +
                "moisture content exceeds 19% (sawn lumber) or 16% (glulam) for an " +
                "extended period. Typical: 1.0 (dry service), 0.7–0.85 (wet service, " +
                "varies by property and species group).",
            onValueChange = { cm = it }
        ),
        FactorField(
            label = "Temperature Factor",
            symbol = "Ct",
            description = "NDS Table 2.3.3. Reduces design values for sustained exposure " +
                "above 100°F. Typical: 1.0 (T ≤ 100°F, the overwhelming majority of " +
                "cases), 0.8–0.9 (100°F < T ≤ 125°F), 0.5–0.7 (125°F < T ≤ 150°F).",
            onValueChange = { ct = it }
        ),
        FactorField(
            label = "Beam Stability Factor",
            symbol = "CL",
            description = "NDS Section 3.3.3. Accounts for lateral-torsional buckling of " +
                "the compression edge between points of lateral support. Auto-calculated " +
                "by the solver from unbraced length and section geometry when left at " +
                "1.0 — only override this if you want to force a known value instead of " +
                "the automatic calculation. Range: 0 < CL ≤ 1.0.",
            onValueChange = { cl = it }
        ),
        FactorField(
            label = "Size Factor",
            symbol = "CF",
            description = "NDS Table 4A (sawn lumber only — glulam uses CV/volume factor " +
                "instead, not user-editable here). Adjusts bending, tension, and " +
                "compression values for member size per NDS 4.3.6. Auto-calculated by " +
                "the solver from nominal width and grade group when left at 1.0. " +
                "Typical range: 0.9–1.5 depending on width and grade.",
            onValueChange = { cf = it }
        ),
        FactorField(
            label = "Flat Use Factor",
            symbol = "Cfu",
            description = "NDS Table 4A. Applies when a rectangular sawn member is loaded " +
                "on its wide face (flatwise) rather than the standard edgewise " +
                "orientation. Typical: 1.0 (edgewise, standard orientation), 1.0–1.2 " +
                "(flatwise, varies by nominal thickness).",
            onValueChange = { cfu = it }
        ),
        FactorField(
            label = "Incising Factor",
            symbol = "Ci",
            description = "NDS Section 4.3.8. Reduces design values for lumber that has " +
                "been incised for preservative treatment. Typical: 1.0 (not incised), " +
                "0.8–0.95 (incised, varies by property — E is reduced least, shear-related " +
                "properties most).",
            onValueChange = { ci = it }
        ),
        FactorField(
            label = "Repetitive Member Factor",
            symbol = "Cr",
            description = "NDS Section 4.3.9. Applies a bending-value increase for sawn " +
                "dimension lumber (2\"–4\" thick) in a repetitive-member system — 3 or " +
                "more parallel members, ≤ 24\" spacing, joined by sheathing or similar " +
                "load-distributing elements. Typical: 1.0 (not repetitive), 1.15 " +
                "(qualifying repetitive-member system).",
            onValueChange = { cr = it }
        ),
        FactorField(
            label = "Column Stability Factor",
            symbol = "CP",
            description = "NDS Section 3.7.1. Accounts for buckling of compression " +
                "members about their weak axis, computed from the member's slenderness " +
                "ratio (Le/d). Auto-calculated by the solver from unbraced length and " +
                "section geometry when left at 1.0 — only override this if you want to " +
                "force a known value instead of the automatic calculation. Range: " +
                "0 < CP ≤ 1.0.",
            onValueChange = { cp = it }
        ),
        FactorField(
            label = "Bearing Area Factor",
            symbol = "Cb",
            description = "NDS Section 3.10.4. Increases the perpendicular-to-grain " +
                "compression value for a bearing length less than 6\", away from the " +
                "member end. Typical: 1.0 (bearing length ≥ 6\", or at the member end), " +
                "up to 1.75 (bearing length as short as 0.5\", away from the end).",
            onValueChange = { cb = it }
        )
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Adjustment Factors") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    "NDS Table 4.3.1 adjustment factors applied to reference design " +
                        "values. All factors default to 1.0 (no adjustment).",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )
                fields.forEachIndexed { index, field ->
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            "${field.label} (${field.symbol})",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            field.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        // Reflects the raw text-field state (which may be mid-edit
                        // / not yet a valid Double) rather than the parsed value,
                        // so the user can type "1." or clear the field without it
                        // snapping back.
                        val rawText = when (field.symbol) {
                            "CD" -> cd
                            "CM" -> cm
                            "Ct" -> ct
                            "CL" -> cl
                            "CF" -> cf
                            "Cfu" -> cfu
                            "Ci" -> ci
                            "Cr" -> cr
                            "CP" -> cp
                            else -> cb
                        }
                        OutlinedTextField(
                            value = rawText,
                            onValueChange = field.onValueChange,
                            label = { Text(field.symbol) },
                            singleLine = true,
                            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                                keyboardType = KeyboardType.Decimal
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    if (index != fields.lastIndex) {
                        HorizontalDivider(modifier = Modifier.padding(top = 8.dp))
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onConfirm(
                    NdsAdjustmentFactors(
                        cd = cd.toDoubleOrNull() ?: 1.0,
                        cm = cm.toDoubleOrNull() ?: 1.0,
                        ct = ct.toDoubleOrNull() ?: 1.0,
                        cl = cl.toDoubleOrNull() ?: 1.0,
                        cf = cf.toDoubleOrNull() ?: 1.0,
                        cfu = cfu.toDoubleOrNull() ?: 1.0,
                        ci = ci.toDoubleOrNull() ?: 1.0,
                        cr = cr.toDoubleOrNull() ?: 1.0,
                        cp = cp.toDoubleOrNull() ?: 1.0,
                        cb = cb.toDoubleOrNull() ?: 1.0
                    )
                )
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
