package com.lz.ui.boundary

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier

@Composable
fun SpringConstraintEditor(
    stiffness: Double,
    onStiffnessChanged: (Double) -> Unit
) {
    var value by remember {
        mutableStateOf(stiffness.toString())
    }

    OutlinedTextField(
        modifier = Modifier.fillMaxWidth(),
        value = value,
        onValueChange = {
            value = it

            it.toDoubleOrNull()?.let {
                onStiffnessChanged(it)
            }
        },
        label = {
            Text("Spring Stiffness")
        }
    )
}
