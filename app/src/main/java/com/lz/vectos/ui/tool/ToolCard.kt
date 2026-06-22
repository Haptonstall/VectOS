package com.lz.vectos.ui.tool

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.lz.domain.plugin.ModuleAction
import com.lz.domain.plugin.ToolPickerItem

@Composable
fun ToolCard(
    item: ToolPickerItem,
    onAction: (ModuleAction) -> Unit
) {

    val action =
        when {
            !item.licensed ->
                ModuleAction.Purchase(
                    item.descriptor.id
                )

            !item.installed ->
                ModuleAction.Install(
                    item.descriptor.id
                )

            else ->
                ModuleAction.Open(
                    item.descriptor.id
                )
        }

    val buttonText =
        when (action) {

            is ModuleAction.Open ->
                "Open"

            is ModuleAction.Install ->
                "Download"

            is ModuleAction.Purchase ->
                "Purchase"
        }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor =
                MaterialTheme.colorScheme.surfaceVariant
        )
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement =
                Arrangement.SpaceBetween
        ) {

            Column(
                modifier = Modifier.weight(1f)
            ) {

                Text(
                    text = item.descriptor.displayName,
                    style =
                        MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = item.descriptor.description,
                    style =
                        MaterialTheme.typography.bodySmall
                )

                Text(
                    text = buildStatusText(item),
                    style =
                        MaterialTheme.typography.labelSmall
                )
            }

            Button(
                onClick = {
                    onAction(action)
                }
            ) {
                Text(buttonText)
            }
        }
    }
}