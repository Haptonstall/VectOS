package com.lz.ui.boundary

import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier

import com.lz.model.structural.ConstraintType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConstraintTypeDropdown(
    selectedType: ConstraintType,
    allowedTypes: Set<ConstraintType>,
    onSelected: (ConstraintType) -> Unit,
) {
    var expanded by remember {
        mutableStateOf(value = false)
    }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = {
            expanded = !expanded
        },
        modifier = Modifier
    ) {

        OutlinedTextField(
            value = selectedType.name,
            onValueChange = {},
            readOnly = true,
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(
                    expanded = expanded
                )
            },
            modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryEditable, true)
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = {
                expanded = false
            }
        ) {
            allowedTypes.forEach { type ->
                DropdownMenuItem(
                    text = {
                        Text(type.name)
                    },
                    onClick = {
                        onSelected(type)
                        expanded = false
                    }
                )
            }
        }
    }
}