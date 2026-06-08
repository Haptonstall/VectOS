package com.lz.vectos.ui.beam

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.lz.model.units.UnitSystem
import com.lz.model.structural.SectionProfile
import com.lz.model.structural.SteelProfile
import com.lz.model.units.*
import com.lz.model.units.UnitFormattingService
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SectionPicker(
    sections: List<SectionProfile>,
    selectedSection: SectionProfile?,
    onSectionSelected: (SectionProfile?) -> Unit,
    modifier: Modifier = Modifier,
) {
    var showDialog by remember { mutableStateOf(value = false) }

    OutlinedCard(
        onClick = { showDialog = true },
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.outlinedCardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 14.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = selectedSection?.designation ?: "Select Section",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = if (selectedSection != null) Color(0xFF4A342F) else MaterialTheme.colorScheme.outline
            )
            Icon(
                imageVector = Icons.Default.ArrowDropDown,
                contentDescription = null,
                tint = Color(0xFF7D5248)
            )
        }
    }

    if (showDialog) {
        SectionSelectionDialog(
            sections = sections,
            onDismiss = { showDialog = false },
            onSelect = { section ->
                onSectionSelected(section)
                showDialog = false
            },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SectionSelectionDialog(
    sections: List<SectionProfile>,
    onDismiss: () -> Unit,
    onSelect: (SectionProfile?) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val filteredSections = remember(searchQuery) {
        sections.filter { it.designation.contains(searchQuery, ignoreCase = true) }
    }

    var expandedSectionId by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.9f)
                .padding(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "Select Section",
                    style = MaterialTheme.typography.headlineSmall
                )
                
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text("Search sections") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                )

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        ListItem(
                            headlineContent = { Text("Manual Entry") },
                            supportingContent = { Text("Override with custom properties") },
                            modifier = Modifier.clickable { onSelect(null) }
                        )
                        HorizontalDivider()
                    }
                    
                    items(filteredSections) { section ->
                        val isExpanded = expandedSectionId == section.id
                        
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .animateContentSize()
                                .clickable { onSelect(section) },
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                        ) {
                            Column {
                                ListItem(
                                    headlineContent = { Text(section.designation, fontWeight = FontWeight.Bold) },
                                    trailingContent = {
                                        IconButton(onClick = {
                                            expandedSectionId = if (isExpanded) null else section.id
                                        }) {
                                            Icon(
                                                Icons.Default.Info,
                                                contentDescription = "Show Properties",
                                                tint = if (isExpanded) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                                            )
                                        }
                                    },
                                    colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                                )
                                
                                if (isExpanded) {
                                    SectionPropertiesGrid(section)
                                }
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                }
            }
        }
    }
}

@Composable
fun SectionPropertiesGrid(section: SectionProfile) {
    val system = UnitSystem.IMPERIAL // Fixed for AISC/NDS for now, or pull from VM if needed
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            PropertyItem("Area", UnitFormattingService.formatArea(section.area, system), Modifier.weight(1f))
            PropertyItem("Depth (d)", UnitFormattingService.formatSmallLength(section.depth, system), Modifier.weight(1f))
        }
        Row(modifier = Modifier.fillMaxWidth()) {
            PropertyItem("Ix", UnitFormattingService.formatInertia(section.propertiesStrongAxis.i, system), Modifier.weight(1f))
            PropertyItem("Sx", UnitFormattingService.formatSectionModulus(section.propertiesStrongAxis.s, system), Modifier.weight(1f))
        }
        Row(modifier = Modifier.fillMaxWidth()) {
            PropertyItem("Iy", UnitFormattingService.formatInertia(section.propertiesWeakAxis.i, system), Modifier.weight(1f))
            PropertyItem("Sy", UnitFormattingService.formatSectionModulus(section.propertiesWeakAxis.s, system), Modifier.weight(1f))
        }
        
        if (section is SteelProfile) {
            Row(modifier = Modifier.fillMaxWidth()) {
                PropertyItem("bf", UnitFormattingService.formatSmallLength(section.flangeWidth, system), Modifier.weight(1f))
                PropertyItem("tf", UnitFormattingService.formatSmallLength(section.flangeThickness, system), Modifier.weight(1f))
            }
            Row(modifier = Modifier.fillMaxWidth()) {
                PropertyItem("tw", UnitFormattingService.formatSmallLength(section.webThickness, system), Modifier.weight(1f))
                PropertyItem("J", String.format(Locale.US, "%.2f in⁴", section.torsionalConstantJ), Modifier.weight(1f))
            }
        }
    }
}

@Composable
fun PropertyItem(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
    }
}
