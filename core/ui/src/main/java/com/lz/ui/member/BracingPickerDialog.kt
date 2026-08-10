package com.lz.ui.member

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.lz.model.units.inFeet
import com.lz.ui.formatting.EngineeringUnitFormatter
import java.util.Locale

@Composable
fun BracingPickerDialog(
    currentBracing: BracingInput,
    materialType: MaterialType,
    unitSystem: UnitSystem,
    onDismiss: () -> Unit,
    onConfirmed: (BracingInput) -> Unit
) {
    val formatter = remember(unitSystem) { EngineeringUnitFormatter(unitSystem) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.padding(24.dp).fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    "Bracing Definition",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    bracingSubtitle(materialType),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                when (materialType) {
                    MaterialType.STEEL,
                    MaterialType.COLDFORM -> SteelBracingContent(
                        currentBracing = currentBracing as? BracingInput.Steel
                            ?: BracingInput.Steel(),
                        formatter = formatter,
                        onConfirmed = onConfirmed,
                        onDismiss = onDismiss
                    )

                    MaterialType.ALUMINUM -> AluminumBracingContent(
                        currentBracing = currentBracing as? BracingInput.Aluminum
                            ?: BracingInput.Aluminum(),
                        formatter = formatter,
                        onConfirmed = onConfirmed,
                        onDismiss = onDismiss
                    )

                    MaterialType.WOOD -> WoodBracingContent(
                        currentBracing = currentBracing as? BracingInput.Wood
                            ?: BracingInput.Wood(),
                        formatter = formatter,
                        onConfirmed = onConfirmed,
                        onDismiss = onDismiss
                    )

                    MaterialType.MASONRY -> MasonryBracingContent(
                        currentBracing = currentBracing as? BracingInput.Masonry
                            ?: BracingInput.Masonry(),
                        formatter = formatter,
                        onConfirmed = onConfirmed,
                        onDismiss = onDismiss
                    )

                    else -> NotApplicableContent(
                        materialType = materialType,
                        onDismiss = onDismiss
                    )
                }
            }
        }
    }
}

@Composable
private fun SteelBracingContent(
    currentBracing: BracingInput.Steel,
    formatter: com.lz.ui.formatting.UnitFormatter,
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
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        DiscretePointsTable(
            discreteTable        = discreteTable,
            newLocationText      = newLocationText,
            topLabel             = "Top",
            bottomLabel          = "Bot",
            formatter            = formatter,
            onLocationTextChange = { newLocationText = it },
            onAddPoint = { loc ->
                val p = DiscreteBracePoint(
                    x = loc.feet,
                    isTopBraced = topMode == BracingMode.DISCRETE,
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

    DialogActions(onDismiss) {
        onConfirmed(
            BracingInput.Steel(
                topMode       = topMode,
                bottomMode    = bottomMode,
                discreteTable = discreteTable
            )
        )
    }
}

@Composable
private fun AluminumBracingContent(
    currentBracing: BracingInput.Aluminum,
    formatter: com.lz.ui.formatting.UnitFormatter,
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

    Text(
        "Note: Aluminum solver applies ADM alloy-specific slenderness limits. " +
                "Ensure the correct alloy is selected in material properties.",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )

    if (topMode == BracingMode.DISCRETE || bottomMode == BracingMode.DISCRETE) {
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        DiscretePointsTable(
            discreteTable        = discreteTable,
            newLocationText      = newLocationText,
            topLabel             = "Top",
            bottomLabel          = "Bot",
            formatter            = formatter,
            onLocationTextChange = { newLocationText = it },
            onAddPoint = { loc ->
                val p = DiscreteBracePoint(
                    x = loc.feet,
                    isTopBraced = topMode == BracingMode.DISCRETE,
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

    DialogActions(onDismiss) {
        onConfirmed(
            BracingInput.Aluminum(
                topMode       = topMode,
                bottomMode    = bottomMode,
                discreteTable = discreteTable
            )
        )
    }
}

@Composable
private fun WoodBracingContent(
    currentBracing: BracingInput.Wood,
    formatter: com.lz.ui.formatting.UnitFormatter,
    onConfirmed: (BracingInput) -> Unit,
    onDismiss: () -> Unit
) {
    var topMode    by remember { mutableStateOf(currentBracing.topMode) }
    var bottomMode by remember { mutableStateOf(currentBracing.bottomMode) }
    var luTopText  by remember {
        mutableStateOf(
            String.format(Locale.US, "%.2f", currentBracing.luTopSpacing.inFeet)
        )
    }
    var luBottomText by remember {
        mutableStateOf(
            String.format(Locale.US, "%.2f", currentBracing.luBottomSpacing.inFeet)
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
        SpacingInputField("Top Flange Spacing (ft)", luTopText) {
            luTopText = it
        }
    }

    BracingSection("Bottom Flange", bottomMode, woodModes) { bottomMode = it }
    if (bottomMode == BracingMode.REPETITIVE_SPACING) {
        SpacingInputField("Bottom Flange Spacing (ft)", luBottomText) {
            luBottomText = it
        }
    }

    if (topMode == BracingMode.DISCRETE || bottomMode == BracingMode.DISCRETE) {
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        DiscretePointsTable(
            discreteTable        = discreteTable,
            newLocationText      = newLocationText,
            topLabel             = "Top",
            bottomLabel          = "Bot",
            formatter            = formatter,
            onLocationTextChange = { newLocationText = it },
            onAddPoint = { loc ->
                val p = DiscreteBracePoint(
                    x = loc.feet,
                    isTopBraced = topMode == BracingMode.DISCRETE,
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

    DialogActions(onDismiss) {
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

@Composable
private fun MasonryBracingContent(
    currentBracing: BracingInput.Masonry,
    formatter: com.lz.ui.formatting.UnitFormatter,
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

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            "Member Type",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Bold
        )
        Text(
            "TMS 402 Chapter 8 (unreinforced) imposes stricter lateral bracing limits " +
                    "than Chapter 9 (reinforced). Select the governing condition for this member.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
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
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        DiscretePointsTable(
            discreteTable        = discreteTable,
            newLocationText      = newLocationText,
            topLabel             = "Comp",
            bottomLabel          = "Tens",
            formatter            = formatter,
            onLocationTextChange = { newLocationText = it },
            onAddPoint = { loc ->
                val p = DiscreteBracePoint(
                    x = loc.feet,
                    isTopBraced = compressionFaceMode == BracingMode.DISCRETE,
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

    DialogActions(onDismiss) {
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

@Composable
private fun NotApplicableContent(
    materialType: MaterialType,
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
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            if (materialType == MaterialType.CONCRETE) {
                Text(
                    "Note: Temporary lateral bracing during construction (pre-composite) " +
                            "is a contractor responsibility and is not evaluated here.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
    Spacer(Modifier.height(8.dp))
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
        TextButton(
            onClick = onDismiss,
            colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.primary)
        ) {
            Text("Close", fontWeight = FontWeight.Bold)
        }
    }
}

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
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
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
    onValueChange: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        OutlinedTextField(
            value = spacingText,
            onValueChange = onValueChange,
            label = { Text("Spacing (ft)") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            shape = RoundedCornerShape(12.dp)
        )
    }
}

@Composable
private fun DiscretePointsTable(
    discreteTable: List<DiscreteBracePoint>,
    newLocationText: String,
    topLabel: String,
    bottomLabel: String,
    formatter: com.lz.ui.formatting.UnitFormatter,
    onLocationTextChange: (String) -> Unit,
    onAddPoint: (Double) -> Unit,
    onUpdatePoint: (DiscreteBracePoint) -> Unit,
    onDeletePoint: (DiscreteBracePoint) -> Unit
) {
    Text(
        "Bracing Locations",
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onSurface,
        fontWeight = FontWeight.Bold
    )

    Row(
        modifier              = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment     = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = newLocationText,
            onValueChange = onLocationTextChange,
            label = { Text("Location (ft)") },
            modifier = Modifier.weight(1f),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            shape = RoundedCornerShape(12.dp)
        )
        FloatingActionButton(
            onClick = { newLocationText.toDoubleOrNull()?.let { onAddPoint(it) } },
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            shape = CircleShape,
            modifier = Modifier.size(56.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add Location")
        }
    }

    Row(
        modifier              = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            "Location",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f)
        )
        Text(
            topLabel,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(48.dp),
            textAlign = TextAlign.Center
        )
        Text(
            bottomLabel,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(48.dp),
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.width(32.dp))
    }

    Box(modifier = Modifier.heightIn(max = 200.dp)) {
        LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            items(discreteTable) { point ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = formatter.length(point.x.inches),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )
                    Checkbox(
                        checked = point.isTopBraced,
                        onCheckedChange = { onUpdatePoint(point.copy(isTopBraced = it)) },
                        modifier = Modifier.width(48.dp)
                    )
                    Checkbox(
                        checked = point.isBottomBraced,
                        onCheckedChange = { onUpdatePoint(point.copy(isBottomBraced = it)) },
                        modifier = Modifier.width(48.dp)
                    )
                    IconButton(
                        onClick = { onDeletePoint(point) },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = MaterialTheme.colorScheme.error.copy(alpha = 0.6f),
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
            colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.primary)
        ) {
            Text("Cancel", fontWeight = FontWeight.Bold)
        }
        Button(
            onClick = onConfirmed,
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
            shape = RoundedCornerShape(28.dp),
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
        onClick = onClick,
        modifier = modifier.height(64.dp),
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
        border = BorderStroke(
            1.dp,
            if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
        )
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(4.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                lineHeight = 14.sp
            )
        }
    }
}

private fun steelAluminumModes() = listOf(
    BracingMode.CONTINUOUS to "Continuous",
    BracingMode.DISCRETE   to "Discrete\nPoints",
    BracingMode.UNBRACED   to "Unbraced\n(Ends Only)"
)

private fun bracingSubtitle(materialType: MaterialType): String = when (materialType) {
    MaterialType.STEEL,
    MaterialType.COLDFORM ->
        "Define top and bottom flange restraints. Reference: AISC 360 Appendix 6."
    MaterialType.ALUMINUM ->
        "Define top and bottom flange restraints. Reference: ADM Part I Section F."
    MaterialType.WOOD ->
        "Define lateral support for top (compression) and bottom flanges. Reference: NDS Section 4.4."
    MaterialType.MASONRY ->
        "Define lateral restraints for compression and tension faces. Reference: TMS 402 Sections 8.3 / 9.3."
    else ->
        "Lateral bracing is not applicable for this material in the final design state."
}
