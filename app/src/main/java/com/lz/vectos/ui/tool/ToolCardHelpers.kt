package com.lz.vectos.ui.tool

import com.lz.domain.module.ToolPickerItem

fun buildStatusText(
    item: ToolPickerItem
): String {

    return when {
        item.installed && item.licensed ->
            "Installed • Licensed"

        item.licensed ->
            "Licensed • Not Installed"

        else ->
            "Not Purchased"
    }
}