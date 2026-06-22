package com.lz.vectos.ui.tool

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lz.domain.calculation.CalculationContext
import com.lz.domain.plugin.ModuleAction
import com.lz.domain.plugin.ModuleInstaller
import com.lz.domain.plugin.ModuleLauncher
import com.lz.domain.plugin.ModuleRegistry
import com.lz.domain.plugin.PurchaseManager
import com.lz.domain.plugin.PurchaseResult
import com.lz.domain.plugin.SubscriptionRepository
import com.lz.domain.plugin.ToolPickerItem
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ToolPickerViewModel(
    private val registry: ModuleRegistry,
    private val installer: ModuleInstaller,
    private val subscriptions: SubscriptionRepository,
    private val launcher: ModuleLauncher,
    private val purchaseManager: PurchaseManager
) : ViewModel() {

    private val _tools =
        MutableStateFlow<List<ToolPickerItem>>(emptyList())

    val tools: StateFlow<List<ToolPickerItem>>
        get() = _tools

    fun onModuleAction(
        action: ModuleAction
    ) {

        when (action) {

            is ModuleAction.Open ->
                openModule(action.moduleId)

            is ModuleAction.Install ->
                installModule(action.moduleId)

            is ModuleAction.Purchase ->
                purchaseModule(action.moduleId)
        }
    }

    fun loadTools(
        context: CalculationContext
    ) {

        currentContext = context

        viewModelScope.launch {

            val items = buildToolList(
                context
            )

            _tools.value = items
        }
    }

    private suspend fun refreshTools() {
        _tools.value =
            buildToolList(
                currentContext
            )
    }

    private suspend fun buildToolList(
        context: CalculationContext
    ): List<ToolPickerItem> {
        return registry
            .getModules()
            .filter { descriptor ->
                when (context) {
                    is CalculationContext.ProjectContext ->
                        descriptor.supportsProjectMode
                    is CalculationContext.QuickCalcContext ->
                        descriptor.supportsQuickCalcMode
                }
            }
            .map { descriptor ->
                ToolPickerItem(
                    descriptor = descriptor,
                    installed =
                        installer.isInstalled(
                            descriptor.id
                        ),
                    licensed =
                        subscriptions.isLicensed(
                        descriptor.id
                        )
                )
            }
    }

    private fun openModule(
        moduleId: String
    ) {

        viewModelScope.launch {
            launcher.open(
                moduleId = moduleId
            )
        }
    }

    private fun installModule(
        moduleId: String
    ) {
        viewModelScope.launch {
            installer.install(
                moduleId = moduleId
            )

            refreshTools()
        }
    }

    private fun purchaseModule(
        moduleId: String
    ) {
        viewModelScope.launch {
            when (
                purchaseManager.purchase(
                    moduleId
                )
            ) {
                PurchaseResult.Success -> {
                    refreshTools()
                }

                PurchaseResult.Cancelled -> {
                    // no-op
                }

                is PurchaseResult.Error -> {
                    // TODO
                    // surface to UI
                }
            }
        }
    }

}