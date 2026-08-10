package com.lz.vectos.ui.tool

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lz.domain.calculation.CalculationContext
import com.lz.domain.module.InstallResult
import com.lz.domain.module.ModuleAction
import com.lz.domain.module.ModuleInstaller
import com.lz.domain.module.ModuleLauncher
import com.lz.domain.module.ModuleRegistry
import com.lz.domain.module.PurchaseManager
import com.lz.domain.module.PurchaseResult
import com.lz.domain.module.SubscriptionRepository
import com.lz.domain.module.ToolPickerEvent
import com.lz.domain.module.ToolPickerItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ToolPickerViewModel @Inject constructor(
    private val registry: ModuleRegistry,
    private val installer: ModuleInstaller,
    private val subscriptions: SubscriptionRepository,
    private val launcher: ModuleLauncher,
    private val purchaseManager: PurchaseManager
) : ViewModel() {

    private val _context = MutableStateFlow<CalculationContext?>(null)

    private val _tools = MutableStateFlow<List<ToolPickerItem>>(emptyList())
    val tools: StateFlow<List<ToolPickerItem>> get() = _tools

    private val _events = MutableSharedFlow<ToolPickerEvent>()
    val events = _events.asSharedFlow()

    fun onModuleAction(action: ModuleAction) {
        when (action) {
            is ModuleAction.Open -> openModule(action.moduleId)
            is ModuleAction.Install -> installModule(action.moduleId)
            is ModuleAction.Purchase -> purchaseModule(action.moduleId)
        }
    }

    fun loadTools(context: CalculationContext) {
        _context.value = context
        viewModelScope.launch { _tools.value = createToolList(context) }
    }

    private suspend fun refreshTools() {
        val context = _context.value ?: return
        val modules = registry.getModules()
        println("ToolPicker found ${modules.size} modules")
        _tools.value = createToolList(context)
    }

    private suspend fun createToolList(context: CalculationContext): List<ToolPickerItem> {
        return registry.getModules()
            .filter { descriptor ->
                when (context) {
                    is CalculationContext.ProjectContext -> descriptor.supportsProjectMode
                    is CalculationContext.QuickCalcContext -> descriptor.supportsQuickCalcMode
                }
            }
            .map { descriptor ->
                ToolPickerItem(
                    descriptor = descriptor,
                    installed = installer.isInstalled(descriptor.id),
                    licensed = subscriptions.isLicensed(descriptor.id)
                )
            }
    }

    private fun openModule(moduleId: String) {
        viewModelScope.launch {
            val route = launcher.open(moduleId)
            _events.emit(ToolPickerEvent.Navigate(route))
        }
    }

    private fun installModule(moduleId: String) {
        viewModelScope.launch {
            when (installer.install(moduleId)) {
                InstallResult.Success -> {
                    _events.emit(ToolPickerEvent.Snackbar("Module installed."))
                    refreshTools()
                }
                InstallResult.Cancelled ->
                    _events.emit(ToolPickerEvent.Snackbar("Installation cancelled."))
                is InstallResult.Error ->
                    _events.emit(ToolPickerEvent.Snackbar("Installation failed."))
            }
        }
    }

    private fun purchaseModule(moduleId: String) {
        viewModelScope.launch {
            when (purchaseManager.purchase(moduleId)) {
                PurchaseResult.Success -> {
                    _events.emit(ToolPickerEvent.Snackbar("Purchase successful."))
                    refreshTools()  // was: loadTools() with missing arg
                }
                PurchaseResult.Cancelled ->
                    _events.emit(ToolPickerEvent.Snackbar("Purchase cancelled."))
                is PurchaseResult.Error ->
                    _events.emit(ToolPickerEvent.Snackbar("Purchase failed."))
            }
        }
    }
}