package com.lz.vectos.ui.tool

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.lz.model.structural.BracingInput
import com.lz.model.structural.BracingMode
import com.lz.model.structural.DiscreteBracePoint
import com.lz.model.structural.MaterialType
import com.lz.model.units.UnitSystem
import com.lz.model.units.feet
import com.lz.model.units.inches
import java.util.Locale

/**
 * Material-aware bracing configuration dialog.
 *
 * Renders the appropriate bracing controls based on [materialType]:
 *
 *   STEEL / COLD_FORMED_STEEL → BracingInput.Steel
 *     Modes: CONTINUOUS, DISCRETE, UNBRACED
 *     AISC 360 Appendix 6 — top and bottom flange restraint
 *
 *   ALUMINUM → BracingInput.Aluminum
 *     Modes: CONTINUOUS, DISCRETE, UNBRACED
 *     ADM Part I Section F — same flange concepts as steel, different solver
 *
 *   WOOD → BracingInput.Wood
 *     Modes: CONTINUOUS, REPETITIVE_SPACING (o.c. bridging/blocking), DISCRETE, UNBRACED
 *     NDS Section 4.4 — top (compression) and bottom flange restraint
 *
 *   MASONRY → BracingInput.Masonry
 *     Modes: CONTINUOUS, DISCRETE, UNBRACED
 *     TMS 402 Sections 8.3 / 9.3 — compression face and tension face restraint
 *     Includes reinforced/unreinforced toggle (different TMS 402 chapter provisions)
 *
 *   CONCRETE / other → Not applicable panel (construction phase note shown)
 */
@Composable
fun BracingPickerDialog(
    currentBracing: BracingInput,
    materialType: MaterialType,
    unitSystem: UnitSystem,
    onDismiss: () -> Unit,
    onConfirmed: (BracingInput) -> Unit
) {
    val bgColor      = Color(0xFFFDF2F0)
    val textColor    = Color(0xFF4A342F)
    val subTextColor = Color(0xFF7D5248).copy(alpha = 0.8f)
    val actionColor  = Color(0xFF7D5248)

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            shape    = RoundedCornerShape(24.dp),
            colors   = CardDefaults.cardColors(containerColor = bgColor)
        ) {
            Column(
                modifier            = Modifier.padding(24.dp).fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    "Bracing Definition",
                    style      = MaterialTheme.typography.headlineSmall,
                    color      = textColor,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    bracingSubtitle(materialType),
                    style = MaterialTheme.typography.bodyMedium,
                    color = subTextColor
                )
                HorizontalDivider(color = actionColor.copy(alpha = 0.1f))

                when (materialType) {
                    MaterialType.STEEL,
                    MaterialType.COLDFORM -> SteelBracingContent(
                        currentBracing = currentBracing as? BracingInput.Steel
                            ?: BracingInput.Steel(),
                        textColor    = textColor,
                        subTextColor = subTextColor,
                        actionColor  = actionColor,
                        onConfirmed  = onConfirmed,
                        onDismiss    = onDismiss
                    )

                    MaterialType.ALUMINUM -> AluminumBracingContent(
                        currentBracing = currentBracing as? BracingInput.Aluminum
                            ?: BracingInput.Aluminum(),
                        textColor    = textColor,
                        subTextColor = subTextColor,
                        actionColor  = actionColor,
                        onConfirmed  = onConfirmed,
                        onDismiss    = onDismiss
                    )

                    MaterialType.WOOD -> WoodBracingContent(
                        currentBracing = currentBracing as? BracingInput.Wood
                            ?: BracingInput.Wood(),
                        textColor    = textColor,
                        subTextColor = subTextColor,
                        actionColor  = actionColor,
                        onConfirmed  = onConfirmed,
                        onDismiss    = onDismiss
                    )

                    MaterialType.MASONRY -> MasonryBracingContent(
                        currentBracing = currentBracing as? BracingInput.Masonry
                            ?: BracingInput.Masonry(),
                        textColor    = textColor,
                        subTextColor = subTextColor,
                        actionColor  = actionColor,
                        onConfirmed  = onConfirmed,
                        onDismiss    = onDismiss
                    )

                    else -> NotApplicableContent(
                        materialType = materialType,
                        subTextColor = subTextColor,
                        actionColor  = actionColor,
                        onDismiss    = onDismiss
                    )
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Steel content — AISC 360 Appendix 6
// ---------------------------------------------------------------------------

@Composable
private fun SteelBracingContent(
    currentBracing: BracingInput.Steel,
    textColor: Color,
    subTextColor: Color,
    actionColor: Color,
    onConfirmed: (BracingInput) -> Unit,
    onDismiss: () -> Unit
) {
    var topMode       by remember { mutableStateOf(currentBracing.topMode) }
    var bottomMode    by remember { mutableStateOf(currentBracing.bottomMode) }
    var discreteTable by remember { mutableStateOf(currentBracing.discreteTable) }
    var newLocationText by remember { mutableStateOf("") }

    val steelModes = steelAluminumModes()

    BracingSection("Top Flange",    topMode,    steelModes) { topMode    = it }
    BracingSection("Bottom Flange", bottomMode, steelModes) { bottomMode = it }

    if (topMode == BracingMode.DISCRETE || bottomMode == BracingMode.DISCRETE) {
        HorizontalDivider(color = actionColor.copy(alpha = 0.1f))
        DiscretePointsTable(
            discreteTable        = discreteTable,
            newLocationText      = newLocationText,
            topLabel             = "Top",
            bottomLabel          = "Bot",
            textColor            = textColor,
            subTextColor         = subTextColor,
            actionColor          = actionColor,
            onLocationTextChange = { newLocationText = it },
            onAddPoint = { loc ->
                val p = DiscreteBracePoint(
                    x              = loc.feet,
                    isTopBraced    = topMode == BracingMode.DISCRETE,
                    isBottomBraced = bottomMode == BracingMode.DISCRETE
                )
                discreteTable   = (discreteTable + p).sortedBy { it.x.inches }
                newLocationText = ""
            },
            onUpdatePoint = { updated ->
                discreteTable = discreteTable.map { if (it == updated) updated else it }
            },
            onDeletePoint = { point ->
                discreteTable = discreteTable.filter { it != point }
            }
        )
    }

    DialogActions(actionColor, onDismiss) {
        onConfirmed(
            BracingInput.Steel(
                topMode       = topMode,
                bottomMode    = bottomMode,
                discreteTable = discreteTable
            )
        )
    }
}

// ---------------------------------------------------------------------------
// Aluminum content — ADM Part I Section F
// ---------------------------------------------------------------------------

@Composable
private fun AluminumBracingContent(
    currentBracing: BracingInput.Aluminum,
    textColor: Color,
    subTextColor: Color,
    actionColor: Color,
    onConfirmed: (BracingInput) -> Unit,
    onDismiss: () -> Unit
) {
    var topMode       by remember { mutableStateOf(currentBracing.topMode) }
    var bottomMode    by remember { mutableStateOf(currentBracing.bottomMode) }
    var discreteTable by remember { mutableStateOf(currentBracing.discreteTable) }
    var newLocationText by remember { mutableStateOf("") }

    val aluminumModes = steelAluminumModes()

    BracingSection("Top Flange",    topMode,    aluminumModes) { topMode    = it }
    BracingSection("Bottom Flange", bottomMode, aluminumModes) { bottomMode = it }

    // Alloy note — ADM slenderness limits vary by alloy series
    Text(
        "Note: Aluminum solver applies ADM alloy-specific slenderness limits. " +
                "Ensure the correct alloy is selected in material properties.",
        style = MaterialTheme.typography.bodySmall,
        color = subTextColor
    )

    if (topMode == BracingMode.DISCRETE || bottomMode == BracingMode.DISCRETE) {
        HorizontalDivider(color = actionColor.copy(alpha = 0.1f))
        DiscretePointsTable(
            discreteTable        = discreteTable,
            newLocationText      = newLocationText,
            topLabel             = "Top",
            bottomLabel          = "Bot",
            textColor            = textColor,
            subTextColor         = subTextColor,
            actionColor          = actionColor,
            onLocationTextChange = { newLocationText = it },
            onAddPoint = { loc ->
                val p = DiscreteBracePoint(
                    x              = loc.feet,
                    isTopBraced    = topMode == BracingMode.DISCRETE,
                    isBottomBraced = bottomMode == BracingMode.DISCRETE
                )
                discreteTable   = (discreteTable + p).sortedBy { it.x.inches }
                newLocationText = ""
            },
            onUpdatePoint = { updated ->
                discreteTable = discreteTable.map { if (it == updated) updated else it }
            },
            onDeletePoint = { point ->
                discreteTable = discreteTable.filter { it != point }
            }
        )
    }

    DialogActions(actionColor, onDismiss) {
        onConfirmed(
            BracingInput.Aluminum(
                topMode       = topMode,
                bottomMode    = bottomMode,
                discreteTable = discreteTable
            )
        )
    }
}

// ---------------------------------------------------------------------------
// Wood content — NDS Section 4.4
// ---------------------------------------------------------------------------

@Composable
private fun WoodBracingContent(
    currentBracing: BracingInput.Wood,
    textColor: Color,
    subTextColor: Color,
    actionColor: Color,
    onConfirmed: (BracingInput) -> Unit,
    onDismiss: () -> Unit
) {
    var topMode    by remember { mutableStateOf(currentBracing.topMode) }
    var bottomMode by remember { mutableStateOf(currentBracing.bottomMode) }
    var luTopText  by remember {
        mutableStateOf(
            String.format(Locale.US, "%.2f", currentBracing.luTopSpacing.inches / 12.0)
        )
    }
    var luBottomText by remember {
        mutableStateOf(
            String.format(Locale.US, "%.2f", currentBracing.luBottomSpacing.inches / 12.0)
        )
    }
    var discreteTable   by remember { mutableStateOf(currentBracing.discreteTable) }
    var newLocationText by remember { mutableStateOf("") }

    val woodModes = listOf(
        BracingMode.CONTINUOUS         to "Continuous",
        BracingMode.REPETITIVE_SPACING to "O.C. Spacing",
        BracingMode.DISCRETE           to "Discrete\nPoints",
        BracingMode.UNBRACED           to "Unbraced\n(Ends Only)"
    )

    BracingSection("Top (Compression) Flange", topMode,    woodModes) { topMode    = it }
    if (topMode == BracingMode.REPETITIVE_SPACING) {
        SpacingInputField("Top Flange Spacing (ft)", luTopText, subTextColor, actionColor) {
            luTopText = it
        }
    }

    BracingSection("Bottom Flange", bottomMode, woodModes) { bottomMode = it }
    if (bottomMode == BracingMode.REPETITIVE_SPACING) {
        SpacingInputField("Bottom Flange Spacing (ft)", luBottomText, subTextColor, actionColor) {
            luBottomText = it
        }
    }

    if (topMode == BracingMode.DISCRETE || bottomMode == BracingMode.DISCRETE) {
        HorizontalDivider(color = actionColor.copy(alpha = 0.1f))
        DiscretePointsTable(
            discreteTable        = discreteTable,
            newLocationText      = newLocationText,
            topLabel             = "Top",
            bottomLabel          = "Bot",
            textColor            = textColor,
            subTextColor         = subTextColor,
            actionColor          = actionColor,
            onLocationTextChange = { newLocationText = it },
            onAddPoint = { loc ->
                val p = DiscreteBracePoint(
                    x              = loc.feet,
                    isTopBraced    = topMode == BracingMode.DISCRETE,
                    isBottomBraced = bottomMode == BracingMode.DISCRETE
                )
                discreteTable   = (discreteTable + p).sortedBy { it.x.inches }
                newLocationText = ""
            },
            onUpdatePoint = { updated ->
                discreteTable = discreteTable.map { if (it == updated) updated else it }
            },
            onDeletePoint = { point ->
                discreteTable = discreteTable.filter { it != point }
            }
        )
    }

    DialogActions(actionColor, onDismiss) {
        onConfirmed(
            BracingInput.Wood(
                topMode         = topMode,
                bottomMode      = bottomMode,
                luTopSpacing    = luTopText.toDoubleOrNull()?.feet
                    ?: currentBracing.luTopSpacing,
                luBottomSpacing = luBottomText.toDoubleOrNull()?.feet
                    ?: currentBracing.luBottomSpacing,
                discreteTable   = discreteTable
            )
        )
    }
}

// ---------------------------------------------------------------------------
// Masonry content — TMS 402 Sections 8.3 / 9.3
// ---------------------------------------------------------------------------

@Composable
private fun MasonryBracingContent(
    currentBracing: BracingInput.Masonry,
    textColor: Color,
    subTextColor: Color,
    actionColor: Color,
    onConfirmed: (BracingInput) -> Unit,
    onDismiss: () -> Unit
) {
    var compressionFaceMode by remember { mutableStateOf(currentBracing.compressionFaceMode) }
    var tensionFaceMode     by remember { mutableStateOf(currentBracing.tensionFaceMode) }
    var isReinforced        by remember { mutableStateOf(currentBracing.isReinforced) }
    var discreteTable       by remember { mutableStateOf(currentBracing.discreteTable) }
    var newLocationText     by remember { mutableStateOf("") }

    val masonryModes = listOf(
        BracingMode.CONTINUOUS to "Continuous",
        BracingMode.DISCRETE   to "Discrete\nPoints",
        BracingMode.UNBRACED   to "Unbraced\n(Ends Only)"
    )

    // Reinforced / Unreinforced toggle
    // This drives which TMS 402 chapter provisions the solver applies
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            "Member Type",
            style      = MaterialTheme.typography.titleMedium,
            color      = textColor,
            fontWeight = FontWeight.Bold
        )
        Text(
            "TMS 402 Chapter 8 (unreinforced) imposes stricter lateral bracing limits " +
                    "than Chapter 9 (reinforced). Select the governing condition for this member.",
            style = MaterialTheme.typography.bodySmall,
            color = subTextColor
        )
        Row(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf(true to "Reinforced", false to "Unreinforced").forEach { (value, label) ->
                BracingOptionButton(
                    title      = label,
                    isSelected = isReinforced == value,
                    onClick    = { isReinforced = value },
                    modifier   = Modifier.weight(1f)
                )
            }
        }
    }

    // Masonry uses "face" terminology rather than "flange"
    BracingSection(
        label          = "Compression Face",
        selectedMode   = compressionFaceMode,
        modes          = masonryModes,
        onModeSelected = { compressionFaceMode = it }
    )
    BracingSection(
        label          = "Tension Face",
        selectedMode   = tensionFaceMode,
        modes          = masonryModes,
        onModeSelected = { tensionFaceMode = it }
    )

    if (compressionFaceMode == BracingMode.DISCRETE || tensionFaceMode == BracingMode.DISCRETE) {
        HorizontalDivider(color = actionColor.copy(alpha = 0.1f))
        DiscretePointsTable(
            discreteTable        = discreteTable,
            newLocationText      = newLocationText,
            // Column headers reflect masonry terminology
            topLabel             = "Comp",
            bottomLabel          = "Tens",
            textColor            = textColor,
            subTextColor         = subTextColor,
            actionColor          = actionColor,
            onLocationTextChange = { newLocationText = it },
            onAddPoint = { loc ->
                val p = DiscreteBracePoint(
                    x              = loc.feet,
                    isTopBraced    = compressionFaceMode == BracingMode.DISCRETE,
                    isBottomBraced = tensionFaceMode == BracingMode.DISCRETE
                )
                discreteTable   = (discreteTable + p).sortedBy { it.x.inches }
                newLocationText = ""
            },
            onUpdatePoint = { updated ->
                discreteTable = discreteTable.map { if (it == updated) updated else it }
            },
            onDeletePoint = { point ->
                discreteTable = discreteTable.filter { it != point }
            }
        )
    }

    DialogActions(actionColor, onDismiss) {
        onConfirmed(
            BracingInput.Masonry(
                compressionFaceMode = compressionFaceMode,
                tensionFaceMode     = tensionFaceMode,
                isReinforced        = isReinforced,
                discreteTable       = discreteTable
            )
        )
    }
}

// ---------------------------------------------------------------------------
// Not applicable panel
// ---------------------------------------------------------------------------

@Composable
private fun NotApplicableContent(
    materialType: MaterialType,
    subTextColor: Color,
    actionColor: Color,
    onDismiss: () -> Unit
) {
    val materialName = materialType.name
        .lowercase()
        .replace('_', ' ')
        .replaceFirstChar { it.uppercase() }

    Box(
        modifier         = Modifier.fillMaxWidth().padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                "Lateral bracing is not applicable for $materialName members in the " +
                        "final design state.",
                style     = MaterialTheme.typography.bodyMedium,
                color     = subTextColor,
                textAlign = TextAlign.Center
            )
            if (materialType == MaterialType.CONCRETE) {
                Text(
                    "Note: Temporary lateral bracing during construction (pre-composite) " +
                            "is a contractor responsibility and is not evaluated here.",
                    style     = MaterialTheme.typography.bodySmall,
                    color     = subTextColor,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
    Spacer(Modifier.height(8.dp))
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
        TextButton(
            onClick = onDismiss,
            colors  = ButtonDefaults.textButtonColors(contentColor = actionColor)
        ) {
            Text("Close", fontWeight = FontWeight.Bold)
        }
    }
}

// ---------------------------------------------------------------------------
// Shared composables
// ---------------------------------------------------------------------------

@Composable
private fun BracingSection(
    label: String,
    selectedMode: BracingMode,
    modes: List<Pair<BracingMode, String>>,
    onModeSelected: (BracingMode) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            label,
            style      = MaterialTheme.typography.titleMedium,
            color      = Color(0xFF4A342F),
            fontWeight = FontWeight.Bold
        )
        Row(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            modes.forEach { (mode, title) ->
                BracingOptionButton(
                    title      = title,
                    isSelected = selectedMode == mode,
                    onClick    = { onModeSelected(mode) },
                    modifier   = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun SpacingInputField(
    label: String,
    spacingText: String,
    subTextColor: Color,
    actionColor: Color,
    onValueChange: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = subTextColor)
        OutlinedTextField(
            value           = spacingText,
            onValueChange   = onValueChange,
            label           = { Text("Spacing (ft)") },
            modifier        = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            shape           = RoundedCornerShape(12.dp),
            colors          = OutlinedTextFieldDefaults.colors(focusedBorderColor = actionColor)
        )
    }
}

@Composable
private fun DiscretePointsTable(
    discreteTable: List<DiscreteBracePoint>,
    newLocationText: String,
    topLabel: String,
    bottomLabel: String,
    textColor: Color,
    subTextColor: Color,
    actionColor: Color,
    onLocationTextChange: (String) -> Unit,
    onAddPoint: (Double) -> Unit,
    onUpdatePoint: (DiscreteBracePoint) -> Unit,
    onDeletePoint: (DiscreteBracePoint) -> Unit
) {
    Text(
        "Bracing Locations",
        style      = MaterialTheme.typography.titleMedium,
        color      = textColor,
        fontWeight = FontWeight.Bold
    )

    Row(
        modifier              = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment     = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value           = newLocationText,
            onValueChange   = onLocationTextChange,
            label           = { Text("Location (ft)") },
            modifier        = Modifier.weight(1f),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            shape           = RoundedCornerShape(12.dp)
        )
        FloatingActionButton(
            onClick        = { newLocationText.toDoubleOrNull()?.let { onAddPoint(it) } },
            containerColor = actionColor,
            contentColor   = Color.White,
            shape          = CircleShape,
            modifier       = Modifier.size(56.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add Location")
        }
    }

    Row(
        modifier              = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text("Location",  style = MaterialTheme.typography.labelMedium, color = subTextColor, modifier = Modifier.weight(1f))
        Text(topLabel,    style = MaterialTheme.typography.labelMedium, color = subTextColor, modifier = Modifier.width(48.dp), textAlign = TextAlign.Center)
        Text(bottomLabel, style = MaterialTheme.typography.labelMedium, color = subTextColor, modifier = Modifier.width(48.dp), textAlign = TextAlign.Center)
        Spacer(Modifier.width(32.dp))
    }

    Box(modifier = Modifier.heightIn(max = 200.dp)) {
        LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            items(discreteTable) { point ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text     = String.format(Locale.US, "%.2f ft", point.x.inches / 12.0),
                        style    = MaterialTheme.typography.bodyMedium,
                        color    = textColor,
                        modifier = Modifier.weight(1f)
                    )
                    Checkbox(
                        checked         = point.isTopBraced,
                        onCheckedChange = { onUpdatePoint(point.copy(isTopBraced = it)) },
                        modifier        = Modifier.width(48.dp)
                    )
                    Checkbox(
                        checked         = point.isBottomBraced,
                        onCheckedChange = { onUpdatePoint(point.copy(isBottomBraced = it)) },
                        modifier        = Modifier.width(48.dp)
                    )
                    IconButton(
                        onClick  = { onDeletePoint(point) },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint     = Color.Red.copy(alpha = 0.6f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DialogActions(
    actionColor: Color,
    onDismiss: () -> Unit,
    onConfirmed: () -> Unit
) {
    Spacer(Modifier.height(8.dp))
    Row(
        modifier              = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment     = Alignment.CenterVertically
    ) {
        TextButton(
            onClick = onDismiss,
            colors  = ButtonDefaults.textButtonColors(contentColor = actionColor)
        ) {
            Text("Cancel", fontWeight = FontWeight.Bold)
        }
        Button(
            onClick  = onConfirmed,
            colors   = ButtonDefaults.buttonColors(containerColor = actionColor),
            shape    = RoundedCornerShape(28.dp),
            modifier = Modifier.height(56.dp).padding(horizontal = 8.dp)
        ) {
            Text("Save Bracing", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun BracingOptionButton(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick  = onClick,
        modifier = modifier.height(64.dp),
        shape    = RoundedCornerShape(12.dp),
        color    = if (isSelected) Color(0xFFFFCCBC) else Color.Transparent,
        border   = BorderStroke(
            1.dp,
            if (isSelected) Color(0xFF7D5248) else Color(0xFF7D5248).copy(alpha = 0.4f)
        )
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(4.dp)) {
            Text(
                text       = title,
                style      = MaterialTheme.typography.labelMedium,
                color      = Color(0xFF4A342F),
                textAlign  = TextAlign.Center,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                lineHeight = 14.sp
            )
        }
    }
}

// ---------------------------------------------------------------------------
// Helpers
// ---------------------------------------------------------------------------

/** Mode list shared by steel and aluminum — REPETITIVE_SPACING excluded. */
private fun steelAluminumModes() = listOf(
    BracingMode.CONTINUOUS to "Continuous",
    BracingMode.DISCRETE   to "Discrete\nPoints",
    BracingMode.UNBRACED   to "Unbraced\n(Ends Only)"
)

/** Subtitle text describing bracing concepts for the active material. */
private fun bracingSubtitle(materialType: MaterialType): String = when (materialType) {
    MaterialType.STEEL,
    MaterialType.COLDFORM ->
        "Define top and bottom flange restraints. " +
                "Reference: AISC 360 Appendix 6."
    MaterialType.ALUMINUM ->
        "Define top and bottom flange restraints. " +
                "Reference: ADM Part I Section F."
    MaterialType.WOOD ->
        "Define lateral support for top (compression) and bottom flanges. " +
                "Reference: NDS Section 4.4."
    MaterialType.MASONRY ->
        "Define lateral restraints for compression and tension faces. " +
                "Reference: TMS 402 Sections 8.3 / 9.3."
    else ->
        "Lateral bracing is not applicable for this material in the final design state."
}