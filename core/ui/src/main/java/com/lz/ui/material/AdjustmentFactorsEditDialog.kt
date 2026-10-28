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
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.lz.model.regulatory.nds.LiveLoadKind
import com.lz.model.regulatory.nds.MoistureCondition
import com.lz.model.regulatory.nds.NdsAdjustmentFactors
import com.lz.model.regulatory.nds.NdsLoadDurationAssumptions

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
    var autoCd by remember(currentFactors) { mutableStateOf(currentFactors.usesAutoLoadDuration) }
    var moisture by remember(currentFactors) {
        mutableStateOf(if (currentFactors.isWet) MoistureCondition.WET else MoistureCondition.DRY)
    }
    var ct by remember(currentFactors) { mutableStateOf(currentFactors.ct.toString()) }
    var cl by remember(currentFactors) { mutableStateOf(currentFactors.cl.toString()) }
    var cf by remember(currentFactors) { mutableStateOf(currentFactors.cf.toString()) }
    var cfu by remember(currentFactors) { mutableStateOf(currentFactors.cfu.toString()) }
    var ci by remember(currentFactors) { mutableStateOf(currentFactors.ci.toString()) }
    var cr by remember(currentFactors) { mutableStateOf(currentFactors.cr.toString()) }
    var cp by remember(currentFactors) { mutableStateOf(currentFactors.cp.toString()) }
    var cb by remember(currentFactors) { mutableStateOf(currentFactors.cb.toString()) }
    val da = currentFactors.loadDurationAssumptions
    var liveCd by remember(currentFactors) { mutableStateOf(da.liveCd.toString()) }
    var roofLiveCd by remember(currentFactors) { mutableStateOf(da.roofLiveCd.toString()) }
    var snowCd by remember(currentFactors) { mutableStateOf(da.snowCd.toString()) }
    var rainCd by remember(currentFactors) { mutableStateOf(da.rainCd.toString()) }
    var liveKind by remember(currentFactors) { mutableStateOf(da.liveKind) }
    var treated by remember(currentFactors) { mutableStateOf(currentFactors.treatedMember) }
    fun validCd(t: String): Boolean = t.toDoubleOrNull()?.let { it > 0.0 && it <= 2.0 } == true
    val assumptionsValid = validCd(liveCd) && validCd(roofLiveCd) && validCd(snowCd) && validCd(rainCd)

    val fields = listOf(
        FactorField(
            label = "Load Duration Factor",
            symbol = "CD",
            description = "NDS Section 2.3.2. Adjusts for how long the governing load is " +
                "sustained — shorter-duration loads permit a higher design value. " +
                "Typical: 0.9 (permanent/dead), 1.0 (10-year/normal occupancy live), " +
                "1.15 (2-month/snow), 1.25 (7-day/construction), 1.6 (10-min/wind or " +
                "seismic), 2.0 (impact). Does not apply to modulus of elasticity. " +
                "Under LRFD the time effect factor λ takes its place and is also set per " +
                "combination.",
            onValueChange = { cd = it }
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
                            "Ct" -> ct
                            "CL" -> cl
                            "CF" -> cf
                            "Cfu" -> cfu
                            "Ci" -> ci
                            "Cr" -> cr
                            "CP" -> cp
                            else -> cb
                        }
                        if (field.symbol == "CD") {
                            // CD belongs to a load combination (the shortest-duration load
                            // in it sets it), not to the member — so by default it is derived
                            // for each combination. A typed-in value overrides that for all.
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    "Set automatically from each load combination",
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.weight(1f)
                                )
                                Switch(checked = autoCd, onCheckedChange = { autoCd = it })
                            }
                        }
                        OutlinedTextField(
                            value = if (field.symbol == "CD" && autoCd) "Automatic" else rawText,
                            enabled = !(field.symbol == "CD" && autoCd),
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
                HorizontalDivider()
                Text(
                    "Moisture condition (wet service factor CM)",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "NDS Supplement Tables 4A / 4B / 5A / 5C. Choose wet service when the in-service moisture " +
                        "content will exceed 19% (sawn lumber) or reach 16% or more (glulam) for an extended " +
                        "time. CM is then applied separately to each design value (it differs by property, e.g. " +
                        "glulam: Fb 0.8, Ft 0.8, Fv 0.875, Fc 0.73, Fc\u22a5 0.53, E 0.833; sawn lumber: Fb 0.85, " +
                        "Fv 0.97, Fc 0.8, Fc\u22a5 0.67, E 0.9, with the Fb\u00b7CF \u2264 1150 psi and " +
                        "Fc\u00b7CF \u2264 750 psi exceptions) and to E in deflection.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    MoistureCondition.values().forEach { m ->
                        FilterChip(
                            selected = moisture == m,
                            onClick = { moisture = m },
                            label = { Text(m.label) }
                        )
                    }
                }
                HorizontalDivider()
                Text(
                    "Load duration assumptions",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "Used when CD is set automatically from each load combination. NDS Table 2.3.2 " +
                        "gives dead 0.9, wind / earthquake 1.6 (fixed) but no duration for roof live " +
                        "or rain, and leaves snow and live load duration to the designer. Change " +
                        "these if your project differs (e.g. snow that stays on the roof for months, " +
                        "storage loading). Allowed range 0 < CD <= 2.0.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                listOf(
                    Triple<String, String, (String) -> Unit>("Occupancy live load CD (ten years)", liveCd, { v: String -> liveCd = v }),
                    Triple<String, String, (String) -> Unit>("Roof live load CD (seven days)", roofLiveCd, { v: String -> roofLiveCd = v }),
                    Triple<String, String, (String) -> Unit>("Snow load CD (two months)", snowCd, { v: String -> snowCd = v }),
                    Triple<String, String, (String) -> Unit>("Rain load CD (no tabulated duration)", rainCd, { v: String -> rainCd = v })
                ).forEach { (label, value, onChange) ->
                    OutlinedTextField(
                        value = value,
                        onValueChange = onChange,
                        label = { Text(label) },
                        isError = !validCd(value),
                        singleLine = true,
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                            keyboardType = KeyboardType.Decimal
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                Text(
                    "LRFD live load type (time effect factor, Table N3 row 1.2D + 1.6L + 0.5(Lr or S or R)): " +
                        "storage 0.7, occupancy 0.8, impact 1.25.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    LiveLoadKind.values().forEach { kind ->
                        FilterChip(
                            selected = liveKind == kind,
                            onClick = { liveKind = kind },
                            label = { Text("${kind.label} (${kind.lambda})") }
                        )
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "Pressure-treated with water-borne preservatives or fire-retardant chemicals " +
                            "(limits CD to 1.6 and lambda to 1.0)",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f)
                    )
                    Switch(checked = treated, onCheckedChange = { treated = it })
                }
            }
        },
        confirmButton = {
            TextButton(enabled = assumptionsValid, onClick = {
                onConfirm(
                    NdsAdjustmentFactors(
                        cd = if (autoCd) 1.0 else (cd.toDoubleOrNull() ?: 1.0),
                        loadDurationFromCombination = autoCd,
                        moisture = moisture,
                        cm = 1.0,
                        ct = ct.toDoubleOrNull() ?: 1.0,
                        cl = cl.toDoubleOrNull() ?: 1.0,
                        cf = cf.toDoubleOrNull() ?: 1.0,
                        cfu = cfu.toDoubleOrNull() ?: 1.0,
                        ci = ci.toDoubleOrNull() ?: 1.0,
                        cr = cr.toDoubleOrNull() ?: 1.0,
                        cp = cp.toDoubleOrNull() ?: 1.0,
                        cb = cb.toDoubleOrNull() ?: 1.0,
                        loadDurationAssumptions = NdsLoadDurationAssumptions(
                            liveCd = liveCd.toDouble(),
                            roofLiveCd = roofLiveCd.toDouble(),
                            snowCd = snowCd.toDouble(),
                            rainCd = rainCd.toDouble(),
                            liveKind = liveKind
                        ),
                        treatedMember = treated
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
