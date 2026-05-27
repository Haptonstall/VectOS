package com.lz.vectos.ui.tool

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import com.lz.vectos.domain.structural.BracingMode
import com.lz.vectos.domain.structural.DiscreteBracePoint
import com.lz.vectos.domain.structural.SpanBracing
import com.lz.vectos.domain.units.UnitSystem
import com.lz.vectos.domain.units.feet
import java.util.Locale

@Composable
fun BracingPickerDialog(
    currentBracing: SpanBracing,
    unitSystem: UnitSystem,
    onDismiss: () -> Unit,
    onConfirmed: (SpanBracing) -> Unit
) {
    var topType by remember { mutableStateOf(currentBracing.topType) }
    var bottomType by remember { mutableStateOf(currentBracing.bottomType) }
    var discretePoints by remember { mutableStateOf(currentBracing.discretePoints) }
    
    var newLocationText by remember { mutableStateOf("") }

    val bgColor = Color(0xFFFDF2F0)
    val textColor = Color(0xFF4A342F)
    val subTextColor = Color(0xFF7D5248).copy(alpha = 0.8f)
    val actionColor = Color(0xFF7D5248)

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = bgColor)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    "Bracing Definition", 
                    style = MaterialTheme.typography.headlineSmall,
                    color = textColor,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "Define lateral restraints for top and bottom flanges independently.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = subTextColor
                )

                // Top Flange Section
                BracingSection(
                    label = "Top Flange",
                    selectedMode = topType,
                    onModeSelected = { topType = it }
                )

                // Bottom Flange Section
                BracingSection(
                    label = "Bottom Flange",
                    selectedMode = bottomType,
                    onModeSelected = { bottomType = it }
                )

                HorizontalDivider(color = actionColor.copy(alpha = 0.1f))

                // Discrete Points Section
                if (topType == BracingMode.DISCRETE || bottomType == BracingMode.DISCRETE) {
                    Text(
                        "Bracing Locations",
                        style = MaterialTheme.typography.titleMedium,
                        color = textColor,
                        fontWeight = FontWeight.Bold
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = newLocationText,
                            onValueChange = { newLocationText = it },
                            label = { Text("Loc (ft)") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            shape = RoundedCornerShape(12.dp)
                        )
                        
                        FloatingActionButton(
                            onClick = {
                                val loc = newLocationText.toDoubleOrNull()
                                if (loc != null) {
                                    val newPoint = DiscreteBracePoint(
                                        x = loc.feet,
                                        isTopBraced = topType == BracingMode.DISCRETE,
                                        isBottomBraced = bottomType == BracingMode.DISCRETE
                                    )
                                    discretePoints = (discretePoints + newPoint).sortedBy { it.x.inches }
                                    newLocationText = ""
                                }
                            },
                            containerColor = actionColor,
                            contentColor = Color.White,
                            shape = CircleShape,
                            modifier = Modifier.size(56.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Add Location")
                        }
                    }

                    // Table Header
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Location", style = MaterialTheme.typography.labelMedium, color = subTextColor, modifier = Modifier.weight(1f))
                        Text("Top", style = MaterialTheme.typography.labelMedium, color = subTextColor, modifier = Modifier.width(48.dp), textAlign = TextAlign.Center)
                        Text("Bot", style = MaterialTheme.typography.labelMedium, color = subTextColor, modifier = Modifier.width(48.dp), textAlign = TextAlign.Center)
                        Spacer(Modifier.width(32.dp))
                    }

                    // Table Body
                    Box(modifier = Modifier.heightIn(max = 200.dp)) {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            items(discretePoints) { point ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color.White.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                        .padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = String.format(Locale.US, "%.2f ft", point.x.inches / 12.0),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = textColor,
                                        modifier = Modifier.weight(1f)
                                    )
                                    
                                    // Top Toggle
                                    Checkbox(
                                        checked = point.isTopBraced,
                                        onCheckedChange = { checked ->
                                            discretePoints = discretePoints.map { 
                                                if (it == point) it.copy(isTopBraced = checked) else it 
                                            }
                                        },
                                        modifier = Modifier.width(48.dp)
                                    )

                                    // Bot Toggle
                                    Checkbox(
                                        checked = point.isBottomBraced,
                                        onCheckedChange = { checked ->
                                            discretePoints = discretePoints.map { 
                                                if (it == point) it.copy(isBottomBraced = checked) else it 
                                            }
                                        },
                                        modifier = Modifier.width(48.dp)
                                    )

                                    IconButton(
                                        onClick = { discretePoints = discretePoints.filter { it != point } },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red.copy(alpha = 0.6f), modifier = Modifier.size(18.dp))
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = onDismiss,
                        colors = ButtonDefaults.textButtonColors(contentColor = actionColor)
                    ) { 
                        Text("Cancel", fontWeight = FontWeight.Bold) 
                    }
                    
                    Button(
                        onClick = { 
                            onConfirmed(
                                SpanBracing(
                                    topType = topType,
                                    bottomType = bottomType,
                                    discretePoints = discretePoints
                                )
                            ) 
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = actionColor),
                        shape = RoundedCornerShape(28.dp),
                        modifier = Modifier.height(56.dp).padding(horizontal = 8.dp)
                    ) {
                        Text("Save Bracing", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun BracingSection(
    label: String,
    selectedMode: BracingMode,
    onModeSelected: (BracingMode) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            label, 
            style = MaterialTheme.typography.titleMedium, 
            color = Color(0xFF4A342F),
            fontWeight = FontWeight.Bold
        )
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val modes = listOf(
                BracingMode.CONTINUOUS to "Continuous",
                BracingMode.DISCRETE to "Discrete Points",
                BracingMode.UNBRACED to "Unbraced (Ends Only)"
            )

            modes.forEach { (mode, title) ->
                BracingOptionButton(
                    title = title,
                    isSelected = selectedMode == mode,
                    onClick = { onModeSelected(mode) },
                    modifier = Modifier.weight(1f)
                )
            }
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
        color = if (isSelected) Color(0xFFFFCCBC) else Color.Transparent,
        border = BorderStroke(1.dp, if (isSelected) Color(0xFF7D5248) else Color(0xFF7D5248).copy(alpha = 0.4f))
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(4.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = Color(0xFF4A342F),
                textAlign = TextAlign.Center,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                lineHeight = 14.sp
            )
        }
    }
}
