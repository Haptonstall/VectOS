
```
VectOS
├─ .continue
│  └─ rules
│     └─ multi-runtimeModule-rules.md
├─ .idea
│  ├─ AndroidProjectSystem.xml
│  ├─ compiler.xml
│  ├─ deploymentTargetSelector.xml
│  ├─ deviceManager.xml
│  ├─ gradle.xml
│  ├─ inspectionProfiles
│  │  └─ Project_Default.xml
│  ├─ markdown.xml
│  ├─ misc.xml
│  ├─ modules
│  │  └─ core
│  │     └─ ui
│  ├─ planningMode.xml
│  ├─ runConfigurations.xml
│  └─ vcs.xml
├─ .kotlin
│  ├─ errors
│  │  ├─ errors-1776859940176.log
│  │  ├─ errors-1776950328489.log
│  │  ├─ errors-1777038377613.log
│  │  ├─ errors-1778238784233.log
│  │  ├─ errors-1779823569391.log
│  │  ├─ errors-1779990756611.log
│  │  └─ errors-1781267191864.log
│  └─ sessions
├─ AGENTS.md
├─ app
│  ├─ proguard-rules.pro
│  ├─ schemas
│  │  ├─ com.lz.vectos.data.persistence.room.AppDatabase
│  │  └─ com.lz.vectos.persistence.room.AppDatabase
│  ├─ src
│  │  ├─ androidTest
│  │  │  └─ java
│  │  │     └─ com
│  │  │        └─ lz
│  │  │           └─ vectos
│  │  │              └─ ExampleInstrumentedTest.kt
│  │  ├─ main
│  │  │  ├─ AndroidManifest.xml
│  │  │  ├─ assets
│  │  │  │  ├─ AISC Shapes Database v15.0.txt
│  │  │  │  └─ nds_sections.json
│  │  │  ├─ java
│  │  │  │  └─ com
│  │  │  │     └─ lz
│  │  │  │        └─ vectos
│  │  │  │           ├─ app
│  │  │  │           │  ├─ MainActivity.kt
│  │  │  │           │  └─ VectosApplication.kt
│  │  │  │           ├─ di
│  │  │  │           │  ├─ DatabaseModule.kt
│  │  │  │           │  └─ ModuleBindings.kt
│  │  │  │           ├─ domain
│  │  │  │           │  ├─ calculation
│  │  │  │           │  │  ├─ CalculationLifecycleService.kt
│  │  │  │           │  │  ├─ EngineeringCalculation.kt
│  │  │  │           │  │  └─ ProjectCalculationRegistry.kt
│  │  │  │           │  ├─ provenance
│  │  │  │           │  │  ├─ CalculationProvenanceService.kt
│  │  │  │           │  │  └─ ProvenanceModels.kt
│  │  │  │           │  ├─ structural
│  │  │  │           │  │  └─ DecisionCaptureService.kt
│  │  │  │           │  └─ versioning
│  │  │  │           │     ├─ CalculationVersioningService.kt
│  │  │  │           │     └─ VersioningModels.kt
│  │  │  │           ├─ plugin
│  │  │  │           │  ├─ DefaultRuntimeModuleRegistry.kt
│  │  │  │           │  ├─ DefaultModuleBootstrapper.kt
│  │  │  │           │  ├─ DefaultModuleProvider.kt
│  │  │  │           │  ├─ RuntimeModuleInstaller.kt
│  │  │  │           │  ├─ GooglePlayPurchaseManager.kt
│  │  │  │           │  ├─ LocalModuleCatalogRepository.kt
│  │  │  │           │  ├─ LocalRegisteredModuleRepository.kt
│  │  │  │           │  ├─ LocalSubscriptionRepository.kt
│  │  │  │           │  └─ ProductionModuleLauncher.kt
│  │  │  │           ├─ presentation
│  │  │  │           │  ├─ ProjectViewModel.kt
│  │  │  │           │  └─ SettingsViewModel.kt
│  │  │  │           ├─ ui
│  │  │  │           │  ├─ HomeScreen.kt
│  │  │  │           │  ├─ input
│  │  │  │           │  │  ├─ model
│  │  │  │           │  │  │  └─ InputModels.kt
│  │  │  │           │  │  ├─ state
│  │  │  │           │  │  │  └─ CalculationInputState.kt
│  │  │  │           │  │  └─ ui
│  │  │  │           │  │     └─ InputRenderer.kt
│  │  │  │           │  ├─ navigation
│  │  │  │           │  │  └─ NavRoutes.kt
│  │  │  │           │  ├─ project
│  │  │  │           │  │  ├─ NewProjectScreen.kt
│  │  │  │           │  │  ├─ ProjectLibraryScreen.kt
│  │  │  │           │  │  └─ ProjectSettingsScreen.kt
│  │  │  │           │  ├─ SettingsScreen.kt
│  │  │  │           │  ├─ theme
│  │  │  │           │  │  ├─ Color.kt
│  │  │  │           │  │  ├─ Theme.kt
│  │  │  │           │  │  └─ Type.kt
│  │  │  │           │  └─ tool
│  │  │  │           │     ├─ AssumptionEditor.kt
│  │  │  │           │     ├─ RevisionHistory.kt
│  │  │  │           │     ├─ ToolCard.kt
│  │  │  │           │     ├─ ToolCardHelpers.kt
│  │  │  │           │     ├─ ToolPickerScreen.kt
│  │  │  │           │     └─ ToolPickerViewModel.kt
│  │  │  │           └─ util
│  │  │  │              └─ serialization
│  │  │  └─ res
│  │  │     ├─ drawable
│  │  │     │  ├─ ic_launcher_background.xml
│  │  │     │  └─ ic_launcher_foreground.xml
│  │  │     ├─ mipmap-anydpi-v26
│  │  │     │  ├─ ic_launcher.xml
│  │  │     │  └─ ic_launcher_round.xml
│  │  │     ├─ mipmap-hdpi
│  │  │     │  ├─ ic_launcher.webp
│  │  │     │  └─ ic_launcher_round.webp
│  │  │     ├─ mipmap-mdpi
│  │  │     │  ├─ ic_launcher.webp
│  │  │     │  └─ ic_launcher_round.webp
│  │  │     ├─ mipmap-xhdpi
│  │  │     │  ├─ ic_launcher.webp
│  │  │     │  └─ ic_launcher_round.webp
│  │  │     ├─ mipmap-xxhdpi
│  │  │     │  ├─ ic_launcher.webp
│  │  │     │  └─ ic_launcher_round.webp
│  │  │     ├─ mipmap-xxxhdpi
│  │  │     │  ├─ ic_launcher.webp
│  │  │     │  └─ ic_launcher_round.webp
│  │  │     ├─ values
│  │  │     │  ├─ colors.xml
│  │  │     │  ├─ strings.xml
│  │  │     │  └─ themes.xml
│  │  │     └─ xml
│  │  │        ├─ backup_rules.xml
│  │  │        └─ data_extraction_rules.xml
│  │  └─ test
│  │     └─ java
│  │        └─ com
│  │           └─ lz
│  │              └─ vectos
│  │                 ├─ domain
│  │                 │  ├─ beam
│  │                 │  ├─ structural
│  │                 │  │  ├─ aisc
│  │                 │  │  │  └─ AiscSteelCapacityCalculatorTest.kt
│  │                 │  │  ├─ analysis
│  │                 │  │  │  └─ BeamAnalysisSolverVerificationTest.kt
│  │                 │  │  └─ CapacityEngineTest.kt
│  │                 │  └─ units
│  │                 └─ ExampleUnitTest.kt
│  └─ test.puml
├─ core
│  ├─ consumer-rules.pro
│  ├─ data
│  │  ├─ consumer-rules.pro
│  │  ├─ proguard-rules.pro
│  │  ├─ schemas
│  │  │  └─ com.lz.data.persistence.room.AppDatabase
│  │  │     └─ 1.json
│  │  └─ src
│  │     ├─ androidTest
│  │     │  └─ java
│  │     │     └─ com
│  │     │        └─ lz
│  │     │           └─ data
│  │     │              └─ ExampleInstrumentedTest.kt
│  │     ├─ main
│  │     │  ├─ AndroidManifest.xml
│  │     │  └─ java
│  │     │     └─ com
│  │     │        └─ lz
│  │     │           └─ data
│  │     │              ├─ persistence
│  │     │              │  └─ room
│  │     │              │     ├─ AppDatabase.kt
│  │     │              │     ├─ dao
│  │     │              │     │  ├─ BuildingCodeDao.kt
│  │     │              │     │  ├─ CalculationDao.kt
│  │     │              │     │  ├─ catalog
│  │     │              │     │  │  ├─ AiscSectionDao.kt
│  │     │              │     │  │  └─ WoodSectionDao.kt
│  │     │              │     │  ├─ CodeRegistryDao.kt
│  │     │              │     │  ├─ LoadCombinationDao.kt
│  │     │              │     │  ├─ MaterialDao.kt
│  │     │              │     │  ├─ project
│  │     │              │     │  │  └─ CustomSectionDao.kt
│  │     │              │     │  ├─ ProjectDao.kt
│  │     │              │     │  └─ SectionDaos.kt
│  │     │              │     ├─ entity
│  │     │              │     │  ├─ BuildingCodeEntities.kt
│  │     │              │     │  ├─ CalculationRoomEntity.kt
│  │     │              │     │  ├─ catalog
│  │     │              │     │  │  ├─ AiscSectionRoomEntity.kt
│  │     │              │     │  │  └─ WoodSectionRoomEntity.kt
│  │     │              │     │  ├─ CodeRegistryEntities.kt
│  │     │              │     │  ├─ LoadCombinationEntities.kt
│  │     │              │     │  ├─ MaterialRoomEntity.kt
│  │     │              │     │  ├─ project
│  │     │              │     │  │  └─ CustomSectionRoomEntity.kt
│  │     │              │     │  ├─ ProjectRoomEntity.kt
│  │     │              │     │  └─ SectionRoomEntities.kt
│  │     │              │     ├─ mapper
│  │     │              │     │  ├─ CalculationMetadataMapper.kt
│  │     │              │     │  ├─ ProjectPersistenceMapper.kt
│  │     │              │     │  ├─ SectionMappers.kt
│  │     │              │     │  └─ StructuralMappers.kt
│  │     │              │     ├─ Migrations.kt
│  │     │              │     ├─ repository
│  │     │              │     │  └─ RoomMaterialRepository.kt
│  │     │              │     ├─ seeder
│  │     │              │     │  ├─ AiscSectionSeeder.kt
│  │     │              │     │  ├─ BuildingCodeSeeder.kt
│  │     │              │     │  ├─ MaterialSeeder.kt
│  │     │              │     │  └─ StructuralDataSeeder.kt
│  │     │              │     └─ StandardTypeConverters.kt
│  │     │              └─ repository
│  │     │                 ├─ AiscSectionRepository.kt
│  │     │                 ├─ BuildingCodeRepository.kt
│  │     │                 ├─ CalculationWriter.kt
│  │     │                 ├─ DataStoreSettingsRepository.kt
│  │     │                 ├─ NdsSectionRepository.kt
│  │     │                 ├─ RoomAiscSectionRepository.kt
│  │     │                 ├─ RoomCalculationWriter.kt
│  │     │                 └─ RoomProjectRepository.kt
│  │     └─ test
│  │        └─ java
│  │           └─ com
│  │              └─ lz
│  │                 └─ data
│  │                    └─ ExampleUnitTest.kt
│  ├─ domain
│  │  ├─ consumer-rules.pro
│  │  ├─ proguard-rules.pro
│  │  ├─ repository
│  │  └─ src
│  │     ├─ androidTest
│  │     │  └─ java
│  │     │     └─ com
│  │     │        └─ lz
│  │     │           └─ domain
│  │     │              └─ ExampleInstrumentedTest.kt
│  │     ├─ main
│  │     │  ├─ AndroidManifest.xml
│  │     │  └─ java
│  │     │     └─ com
│  │     │        └─ lz
│  │     │           └─ domain
│  │     │              ├─ calculation
│  │     │              │  ├─ CalculationContext.kt
│  │     │              │  └─ CalculationMetadata.kt
│  │     │              ├─ material
│  │     │              │  └─ MaterialRepository.kt
│  │     │              ├─ plugin
│  │     │              │  ├─ CalculatorDestination.kt
│  │     │              │  ├─ CalculatorPlugin.kt
│  │     │              │  ├─ InstalledModule.kt
│  │     │              │  ├─ InstallResult.kt
│  │     │              │  ├─ ModuleAction.kt
│  │     │              │  ├─ ModuleCatalogRepository.kt
│  │     │              │  ├─ RuntimeModuleDescriptor.kt
│  │     │              │  ├─ ModuleInstaller.kt
│  │     │              │  ├─ ModuleLauncher.kt
│  │     │              │  ├─ RuntimeModuleRegistry.kt
│  │     │              │  ├─ ModuleType.kt
│  │     │              │  ├─ ModuleEntryPoint.kt
│  │     │              │  ├─ ModuleBootstrapper.kt
│  │     │              │  ├─ RuntimeModuleProvider.kt
│  │     │              │  ├─ PurchaseManager.kt
│  │     │              │  ├─ PurchaseResult.kt
│  │     │              │  ├─ RegisteredModule.kt
│  │     │              │  ├─ RegisteredModuleRepository.kt
│  │     │              │  ├─ SubscriptionRepository.kt
│  │     │              │  ├─ ToolPickerEvent.kt
│  │     │              │  └─ ToolPickerItem.kt
│  │     │              ├─ project
│  │     │              │  └─ Project.kt
│  │     │              ├─ repository
│  │     │              │  ├─ CalculationRepository.kt
│  │     │              │  ├─ ProjectRepository.kt
│  │     │              │  └─ SettingsRepository.kt
│  │     │              └─ structural
│  │     │                 ├─ BoundaryConditionDefinition.kt
│  │     │                 ├─ BoundaryConditionPreset.kt
│  │     │                 ├─ BoundaryConditions.kt
│  │     │                 ├─ BoundaryConditionValidator.kt
│  │     │                 ├─ DeflectionLimits.kt
│  │     │                 └─ ValidationResult.kt
│  │     └─ test
│  │        └─ java
│  │           └─ com
│  │              └─ lz
│  │                 └─ domain
│  │                    └─ ExampleUnitTest.kt
│  ├─ model
│  │  ├─ consumer-rules.pro
│  │  ├─ proguard-rules.pro
│  │  └─ src
│  │     ├─ androidTest
│  │     │  └─ java
│  │     │     └─ com
│  │     │        └─ lz
│  │     │           └─ model
│  │     │              └─ ExampleInstrumentedTest.kt
│  │     ├─ main
│  │     │  ├─ AndroidManifest.xml
│  │     │  └─ java
│  │     │     └─ com
│  │     │        └─ lz
│  │     │           └─ model
│  │     │              ├─ presentation
│  │     │              │  └─ ServiceabilityLimits.kt
│  │     │              ├─ regulatory
│  │     │              │  ├─ aci318
│  │     │              │  │  └─ Aci318Versions.kt
│  │     │              │  ├─ aisc
│  │     │              │  │  ├─ AiscDesignFactorRegistry.kt
│  │     │              │  │  └─ AiscDesignFactors.kt
│  │     │              │  ├─ asce7
│  │     │              │  │  └─ Asce7Versions.kt
│  │     │              │  ├─ codes
│  │     │              │  │  ├─ BuildingCode.kt
│  │     │              │  │  ├─ CodeReferenceKey.kt
│  │     │              │  │  ├─ ServiceabilityCriterion.kt
│  │     │              │  │  ├─ Standard.kt
│  │     │              │  │  └─ StandardEdition.kt
│  │     │              │  ├─ LoadCategory.kt
│  │     │              │  ├─ LoadCombinationModels.kt
│  │     │              │  ├─ loads
│  │     │              │  │  ├─ CombinationType.kt
│  │     │              │  │  └─ DesignFactors.kt
│  │     │              │  ├─ nds
│  │     │              │  │  ├─ NdsAdjustmentFactors.kt
│  │     │              │  │  └─ NdsVersions.kt
│  │     │              │  ├─ RegulatoryEnums.kt
│  │     │              │  └─ StandardReferenceKey.kt
│  │     │              ├─ structural
│  │     │              │  ├─ AnalysisModels.kt
│  │     │              │  ├─ AxialDesignModels.kt
│  │     │              │  ├─ BoundaryRestraint.kt
│  │     │              │  ├─ BracingModels.kt
│  │     │              │  ├─ CapacityResults.kt
│  │     │              │  ├─ ConstraintType.kt
│  │     │              │  ├─ DegreeOfFreedom.kt
│  │     │              │  ├─ DesignContextModels.kt
│  │     │              │  ├─ DesignEquationTrace.kt
│  │     │              │  ├─ DesignModels.kt
│  │     │              │  ├─ DofConstraint.kt
│  │     │              │  ├─ InteractionModels.kt
│  │     │              │  ├─ LimitState.kt
│  │     │              │  ├─ LoadCase.kt
│  │     │              │  ├─ LoadModels.kt
│  │     │              │  ├─ MaterialModels.kt
│  │     │              │  ├─ MaterialType.kt
│  │     │              │  ├─ NodeBoundaryCondition.kt
│  │     │              │  ├─ SectionModels.kt
│  │     │              │  ├─ ServiceabilityModels.kt
│  │     │              │  ├─ StationDemand.kt
│  │     │              │  ├─ SteelStabilityModels.kt
│  │     │              │  ├─ StructuralDemand.kt
│  │     │              │  ├─ StructuralModels.kt
│  │     │              │  └─ StructuralNode.kt
│  │     │              ├─ units
│  │     │              │  ├─ UnitFormattingService.kt
│  │     │              │  ├─ UnitModels.kt
│  │     │              │  └─ UnitSystem.kt
│  │     │              └─ util
│  │     │                 ├─ LocalDateTimeSerializer.kt
│  │     │                 └─ UUIDSerializer.kt
│  │     └─ test
│  │        └─ java
│  │           └─ com
│  │              └─ lz
│  │                 └─ model
│  │                    └─ ExampleUnitTest.kt
│  ├─ proguard-rules.pro
│  ├─ solver
│  │  ├─ consumer-rules.pro
│  │  ├─ proguard-rules.pro
│  │  └─ src
│  │     ├─ androidTest
│  │     │  └─ java
│  │     │     └─ com
│  │     │        └─ lz
│  │     │           └─ solver
│  │     │              └─ ExampleInstrumentedTest.kt
│  │     ├─ main
│  │     │  ├─ AndroidManifest.xml
│  │     │  └─ java
│  │     │     └─ com
│  │     │        └─ lz
│  │     │           └─ solver
│  │     │              ├─ analysis
│  │     │              │  ├─ AnalysisConfig.kt
│  │     │              │  ├─ LimitStateService.kt
│  │     │              │  ├─ LoadResolutionService.kt
│  │     │              │  ├─ MemberAnalysisSolver.kt
│  │     │              │  └─ StructuralSolver.kt
│  │     │              ├─ bracing
│  │     │              │  ├─ BracingLogic.kt
│  │     │              │  └─ StabilityFactorCalculator.kt
│  │     │              ├─ capacity
│  │     │              │  ├─ CapacityCalculator.kt
│  │     │              │  ├─ CapacityEngine.kt
│  │     │              │  └─ StrengthDesignService.kt
│  │     │              ├─ envelope
│  │     │              │  ├─ DemandEnvelopeResolver.kt
│  │     │              │  ├─ DesignInterpretationService.kt
│  │     │              │  ├─ ServiceabilityEvaluationService.kt
│  │     │              │  └─ ServiceabilityInterpretationService.kt
│  │     │              ├─ material
│  │     │              │  ├─ AiscCbCalculator.kt
│  │     │              │  ├─ AiscSteelCapacityCalculator.kt
│  │     │              │  ├─ MaterialDesignResolver.kt
│  │     │              │  ├─ NdsClCalculator.kt
│  │     │              │  ├─ NdsWoodCapacityCalculator.kt
│  │     │              │  └─ WoodPropertyService.kt
│  │     │              └─ regulatory
│  │     │                 ├─ LoadCombinationEngine.kt
│  │     │                 └─ RegulatoryRegistry.kt
│  │     └─ test
│  │        └─ java
│  │           └─ com
│  │              └─ lz
│  │                 └─ solver
│  │                    ├─ analysis
│  │                    │  └─ LimitStateServiceTest.kt
│  │                    └─ ExampleUnitTest.kt
│  └─ ui
│     ├─ consumer-rules.pro
│     ├─ proguard-rules.pro
│     ├─ src
│     │  ├─ androidTest
│     │  │  └─ java
│     │  │     └─ com
│     │  │        └─ lz
│     │  │           └─ ui
│     │  │              └─ ExampleInstrumentedTest.kt
│     │  ├─ main
│     │  │  ├─ AndroidManifest.xml
│     │  │  └─ java
│     │  │     └─ com
│     │  │        └─ lz
│     │  │           └─ ui
│     │  │              ├─ AnalysisChart.kt
│     │  │              ├─ boundary
│     │  │              │  ├─ BoundaryConditionPicker.kt
│     │  │              │  ├─ BoundaryConditionPickerConfig.kt
│     │  │              │  ├─ BoundaryConditionVisualizer.kt
│     │  │              │  ├─ BoundaryOptionItem.kt
│     │  │              │  ├─ BoundaryPresetOption.kt
│     │  │              │  ├─ ConstraintTypeDropdown.kt
│     │  │              │  ├─ DofConstraintEditor.kt
│     │  │              │  ├─ DofEditorConfig.kt
│     │  │              │  └─ SpringConstraintEditor.kt
│     │  │              ├─ formatting
│     │  │              │  └─ UnitFormatter.kt
│     │  │              ├─ loads
│     │  │              │  ├─ LoadCasePicker.kt
│     │  │              │  ├─ LoadCombinationPicker.kt
│     │  │              │  ├─ LoadCombinationViewer.kt
│     │  │              │  └─ LoadEditor.kt
│     │  │              ├─ material
│     │  │              │  └─ WoodMaterialPickerDialog.kt
│     │  │              ├─ member
│     │  │              │  ├─ BracingPickerDialog.kt
│     │  │              │  └─ SpanEditor.kt
│     │  │              ├─ SectionPicker.kt
│     │  │              ├─ serviceability
│     │  │              │  └─ ServiceabilityPickerDialog.kt
│     │  │              └─ UtilizationHeatMap.kt
│     │  └─ test
│     │     └─ java
│     │        └─ com
│     │           └─ lz
│     │              └─ ui
│     │                 └─ ExampleUnitTest.kt
│     └─ ui
├─ docs
│  ├─ 01-platform-architecture.md
│  ├─ 02-dependency-rule-charter.md
│  ├─ 03-adr-process.md
│  ├─ 04-runtimeModule-sdk-specification.md
│  ├─ 05-engine-contract-specification.md
│  ├─ 06-tooling-runtimeEnvironment-specification.md
│  ├─ 07-ai-agent-orchestration-spec.md
│  ├─ 08-marketplace-runtimeEnvironment-spec.md
│  ├─ 09-distributed-compute-spec.md
│  ├─ 10-versioning-and-migration-strategy.md
│  ├─ architecture-validator-design.md
│  ├─ ARCHITECTURE_CONTEXT.md
│  ├─ NextSteps.md
│  ├─ rules.md
│  ├─ test example.pdf
│  └─ VECTOS_ARCHITECTURE.md
├─ feature
│  ├─ beam
│  │  ├─ consumer-rules.pro
│  │  ├─ proguard-rules.pro
│  │  └─ src
│  │     ├─ androidTest
│  │     │  └─ java
│  │     │     └─ com
│  │     │        └─ lz
│  │     │           └─ beam
│  │     │              └─ ExampleInstrumentedTest.kt
│  │     ├─ main
│  │     │  ├─ AndroidManifest.xml
│  │     │  └─ java
│  │     │     └─ com
│  │     │        └─ lz
│  │     │           └─ beam
│  │     │              ├─ data
│  │     │              │  ├─ persistence
│  │     │              │  │  └─ room
│  │     │              │  │     ├─ BeamDatabase.kt
│  │     │              │  │     ├─ BeamTypeConverters.kt
│  │     │              │  │     ├─ dao
│  │     │              │  │     │  └─ BeamCalculationDao.kt
│  │     │              │  │     └─ entity
│  │     │              │  │        └─ BeamCalculationRoomEntity.kt
│  │     │              │  └─ repository
│  │     │              │     ├─ BeamPersistenceMapper.kt
│  │     │              │     └─ RoomBeamCalculationRepository.kt
│  │     │              ├─ domain
│  │     │              │  ├─ BeamCalculationRepository.kt
│  │     │              │  └─ repository
│  │     │              ├─ model
│  │     │              │  └─ BeamModels.kt
│  │     │              ├─ plugin
│  │     │              │  ├─ BeamEntryPoint.kt
│  │     │              │  └─ BeamPlugin.kt
│  │     │              ├─ presentation
│  │     │              │  ├─ BeamDisplayModel.kt
│  │     │              │  └─ BeamViewModel.kt
│  │     │              ├─ solver
│  │     │              │  ├─ BeamAnalysisConfig.kt
│  │     │              │  └─ BeamAnalysisSolver.kt
│  │     │              └─ ui
│  │     │                 ├─ BeamBoundaryConditionConfig.kt
│  │     │                 ├─ BeamCalculatorScreen.kt
│  │     │                 ├─ BeamDiagram.kt
│  │     │                 └─ StructuralDrawingUtils.kt
│  │     └─ test
│  │        └─ java
│  │           └─ com
│  │              └─ lz
│  │                 └─ beam
│  │                    └─ ExampleUnitTest.kt
│  ├─ column
│  └─ pole
├─ gradle
│  ├─ gradle-daemon-jvm.properties
│  ├─ libs.versions.toml
│  └─ wrapper
│     ├─ gradle-wrapper.jar
│     └─ gradle-wrapper.properties
├─ gradle.properties
├─ gradlew
├─ gradlew.bat
└─ shared
   ├─ codes
   ├─ materials
   ├─ sections
   └─ structural

```
```
VectOS
├─ .continue
│  └─ rules
│     └─ multi-module-rules.md
├─ .idea
│  ├─ AndroidProjectSystem.xml
│  ├─ compiler.xml
│  ├─ deploymentTargetSelector.xml
│  ├─ deviceManager.xml
│  ├─ gradle.xml
│  ├─ inspectionProfiles
│  │  └─ Project_Default.xml
│  ├─ kotlinc.xml
│  ├─ markdown.xml
│  ├─ misc.xml
│  ├─ modules
│  │  └─ core
│  │     └─ ui
│  ├─ planningMode.xml
│  ├─ runConfigurations.xml
│  └─ vcs.xml
├─ .kotlin
│  ├─ errors
│  │  ├─ errors-1776859940176.log
│  │  ├─ errors-1776950328489.log
│  │  ├─ errors-1777038377613.log
│  │  ├─ errors-1778238784233.log
│  │  ├─ errors-1779823569391.log
│  │  ├─ errors-1779990756611.log
│  │  └─ errors-1781267191864.log
│  └─ sessions
├─ AGENTS.md
├─ app
│  ├─ proguard-rules.pro
│  ├─ schemas
│  │  ├─ com.lz.vectos.data.persistence.room.AppDatabase
│  │  └─ com.lz.vectos.persistence.room.AppDatabase
│  ├─ src
│  │  ├─ androidTest
│  │  │  └─ java
│  │  │     └─ com
│  │  │        └─ lz
│  │  │           └─ vectos
│  │  │              └─ ExampleInstrumentedTest.kt
│  │  ├─ main
│  │  │  ├─ AndroidManifest.xml
│  │  │  ├─ assets
│  │  │  │  ├─ AISC Shapes Database v15.0.txt
│  │  │  │  └─ nds_sections.json
│  │  │  ├─ java
│  │  │  │  └─ com
│  │  │  │     └─ lz
│  │  │  │        └─ vectos
│  │  │  │           ├─ app
│  │  │  │           │  ├─ MainActivity.kt
│  │  │  │           │  └─ VectosApplication.kt
│  │  │  │           ├─ di
│  │  │  │           │  ├─ DatabaseModule.kt
│  │  │  │           │  └─ ModuleBindings.kt
│  │  │  │           ├─ domain
│  │  │  │           │  ├─ calculation
│  │  │  │           │  │  ├─ CalculationLifecycleService.kt
│  │  │  │           │  │  ├─ EngineeringCalculation.kt
│  │  │  │           │  │  └─ ProjectCalculationRegistry.kt
│  │  │  │           │  ├─ provenance
│  │  │  │           │  │  ├─ CalculationProvenanceService.kt
│  │  │  │           │  │  └─ ProvenanceModels.kt
│  │  │  │           │  ├─ structural
│  │  │  │           │  │  └─ DecisionCaptureService.kt
│  │  │  │           │  └─ versioning
│  │  │  │           │     ├─ CalculationVersioningService.kt
│  │  │  │           │     └─ VersioningModels.kt
│  │  │  │           ├─ plugin
│  │  │  │           │  ├─ DefaultModuleBootstrapper.kt
│  │  │  │           │  ├─ DefaultModuleProvider.kt
│  │  │  │           │  ├─ DefaultModuleRegistry.kt
│  │  │  │           │  ├─ RuntimeModuleInstaller.kt
│  │  │  │           │  ├─ GooglePlayPurchaseManager.kt
│  │  │  │           │  ├─ LocalModuleCatalogRepository.kt
│  │  │  │           │  ├─ LocalRegisteredModuleRepository.kt
│  │  │  │           │  ├─ LocalSubscriptionRepository.kt
│  │  │  │           │  └─ ProductionModuleLauncher.kt
│  │  │  │           ├─ presentation
│  │  │  │           │  ├─ ProjectViewModel.kt
│  │  │  │           │  └─ SettingsViewModel.kt
│  │  │  │           ├─ ui
│  │  │  │           │  ├─ HomeScreen.kt
│  │  │  │           │  ├─ input
│  │  │  │           │  │  ├─ model
│  │  │  │           │  │  │  └─ InputModels.kt
│  │  │  │           │  │  ├─ state
│  │  │  │           │  │  │  └─ CalculationInputState.kt
│  │  │  │           │  │  └─ ui
│  │  │  │           │  │     └─ InputRenderer.kt
│  │  │  │           │  ├─ navigation
│  │  │  │           │  │  └─ NavRoutes.kt
│  │  │  │           │  ├─ project
│  │  │  │           │  │  ├─ NewProjectScreen.kt
│  │  │  │           │  │  ├─ ProjectLibraryScreen.kt
│  │  │  │           │  │  └─ ProjectSettingsScreen.kt
│  │  │  │           │  ├─ SettingsScreen.kt
│  │  │  │           │  ├─ theme
│  │  │  │           │  │  ├─ Color.kt
│  │  │  │           │  │  ├─ Theme.kt
│  │  │  │           │  │  └─ Type.kt
│  │  │  │           │  └─ tool
│  │  │  │           │     ├─ AssumptionEditor.kt
│  │  │  │           │     ├─ RevisionHistory.kt
│  │  │  │           │     ├─ ToolCard.kt
│  │  │  │           │     ├─ ToolCardHelpers.kt
│  │  │  │           │     ├─ ToolPickerScreen.kt
│  │  │  │           │     └─ ToolPickerViewModel.kt
│  │  │  │           └─ util
│  │  │  │              └─ serialization
│  │  │  └─ res
│  │  │     ├─ drawable
│  │  │     │  ├─ ic_launcher_background.xml
│  │  │     │  └─ ic_launcher_foreground.xml
│  │  │     ├─ mipmap-anydpi-v26
│  │  │     │  ├─ ic_launcher.xml
│  │  │     │  └─ ic_launcher_round.xml
│  │  │     ├─ mipmap-hdpi
│  │  │     │  ├─ ic_launcher.webp
│  │  │     │  └─ ic_launcher_round.webp
│  │  │     ├─ mipmap-mdpi
│  │  │     │  ├─ ic_launcher.webp
│  │  │     │  └─ ic_launcher_round.webp
│  │  │     ├─ mipmap-xhdpi
│  │  │     │  ├─ ic_launcher.webp
│  │  │     │  └─ ic_launcher_round.webp
│  │  │     ├─ mipmap-xxhdpi
│  │  │     │  ├─ ic_launcher.webp
│  │  │     │  └─ ic_launcher_round.webp
│  │  │     ├─ mipmap-xxxhdpi
│  │  │     │  ├─ ic_launcher.webp
│  │  │     │  └─ ic_launcher_round.webp
│  │  │     ├─ values
│  │  │     │  ├─ colors.xml
│  │  │     │  ├─ strings.xml
│  │  │     │  └─ themes.xml
│  │  │     └─ xml
│  │  │        ├─ backup_rules.xml
│  │  │        └─ data_extraction_rules.xml
│  │  └─ test
│  │     └─ java
│  │        └─ com
│  │           └─ lz
│  │              └─ vectos
│  │                 ├─ domain
│  │                 │  ├─ beam
│  │                 │  ├─ structural
│  │                 │  │  ├─ aisc
│  │                 │  │  │  └─ AiscSteelCapacityCalculatorTest.kt
│  │                 │  │  ├─ analysis
│  │                 │  │  │  └─ BeamAnalysisSolverVerificationTest.kt
│  │                 │  │  └─ CapacityEngineTest.kt
│  │                 │  └─ units
│  │                 └─ ExampleUnitTest.kt
│  └─ test.puml
├─ core
│  ├─ consumer-rules.pro
│  ├─ data
│  │  ├─ consumer-rules.pro
│  │  ├─ proguard-rules.pro
│  │  ├─ schemas
│  │  │  └─ com.lz.data.persistence.room.AppDatabase
│  │  │     └─ 1.json
│  │  └─ src
│  │     ├─ androidTest
│  │     │  └─ java
│  │     │     └─ com
│  │     │        └─ lz
│  │     │           └─ data
│  │     │              └─ ExampleInstrumentedTest.kt
│  │     ├─ main
│  │     │  ├─ AndroidManifest.xml
│  │     │  └─ java
│  │     │     └─ com
│  │     │        └─ lz
│  │     │           └─ data
│  │     │              ├─ persistence
│  │     │              │  └─ room
│  │     │              │     ├─ AppDatabase.kt
│  │     │              │     ├─ dao
│  │     │              │     │  ├─ BuildingCodeDao.kt
│  │     │              │     │  ├─ CalculationDao.kt
│  │     │              │     │  ├─ catalog
│  │     │              │     │  │  ├─ AiscSectionDao.kt
│  │     │              │     │  │  └─ WoodSectionDao.kt
│  │     │              │     │  ├─ CodeRegistryDao.kt
│  │     │              │     │  ├─ LoadCombinationDao.kt
│  │     │              │     │  ├─ MaterialDao.kt
│  │     │              │     │  ├─ project
│  │     │              │     │  │  └─ CustomSectionDao.kt
│  │     │              │     │  ├─ ProjectDao.kt
│  │     │              │     │  └─ SectionDaos.kt
│  │     │              │     ├─ entity
│  │     │              │     │  ├─ BuildingCodeEntities.kt
│  │     │              │     │  ├─ CalculationRoomEntity.kt
│  │     │              │     │  ├─ catalog
│  │     │              │     │  │  ├─ AiscSectionRoomEntity.kt
│  │     │              │     │  │  └─ WoodSectionRoomEntity.kt
│  │     │              │     │  ├─ CodeRegistryEntities.kt
│  │     │              │     │  ├─ LoadCombinationEntities.kt
│  │     │              │     │  ├─ MaterialRoomEntity.kt
│  │     │              │     │  ├─ project
│  │     │              │     │  │  └─ CustomSectionRoomEntity.kt
│  │     │              │     │  ├─ ProjectRoomEntity.kt
│  │     │              │     │  └─ SectionRoomEntities.kt
│  │     │              │     ├─ mapper
│  │     │              │     │  ├─ CalculationMetadataMapper.kt
│  │     │              │     │  ├─ ProjectPersistenceMapper.kt
│  │     │              │     │  ├─ SectionMappers.kt
│  │     │              │     │  └─ StructuralMappers.kt
│  │     │              │     ├─ Migrations.kt
│  │     │              │     ├─ repository
│  │     │              │     │  └─ RoomMaterialRepository.kt
│  │     │              │     ├─ seeder
│  │     │              │     │  ├─ AiscSectionSeeder.kt
│  │     │              │     │  ├─ BuildingCodeSeeder.kt
│  │     │              │     │  ├─ MaterialSeeder.kt
│  │     │              │     │  └─ StructuralDataSeeder.kt
│  │     │              │     └─ StandardTypeConverters.kt
│  │     │              └─ repository
│  │     │                 ├─ AiscSectionRepository.kt
│  │     │                 ├─ BuildingCodeRepository.kt
│  │     │                 ├─ CalculationWriter.kt
│  │     │                 ├─ DataStoreSettingsRepository.kt
│  │     │                 ├─ NdsSectionRepository.kt
│  │     │                 ├─ RoomAiscSectionRepository.kt
│  │     │                 ├─ RoomCalculationWriter.kt
│  │     │                 └─ RoomProjectRepository.kt
│  │     └─ test
│  │        └─ java
│  │           └─ com
│  │              └─ lz
│  │                 └─ data
│  │                    └─ ExampleUnitTest.kt
│  ├─ domain
│  │  ├─ consumer-rules.pro
│  │  ├─ proguard-rules.pro
│  │  ├─ repository
│  │  └─ src
│  │     ├─ androidTest
│  │     │  └─ java
│  │     │     └─ com
│  │     │        └─ lz
│  │     │           └─ domain
│  │     │              └─ ExampleInstrumentedTest.kt
│  │     ├─ main
│  │     │  ├─ AndroidManifest.xml
│  │     │  └─ java
│  │     │     └─ com
│  │     │        └─ lz
│  │     │           └─ domain
│  │     │              ├─ calculation
│  │     │              │  ├─ CalculationContext.kt
│  │     │              │  └─ CalculationMetadata.kt
│  │     │              ├─ material
│  │     │              │  └─ MaterialRepository.kt
│  │     │              ├─ module
│  │     │              │  ├─ CalculatorDestination.kt
│  │     │              │  ├─ CalculatorPlugin.kt
│  │     │              │  ├─ InstalledModule.kt
│  │     │              │  ├─ InstallResult.kt
│  │     │              │  ├─ ModuleAction.kt
│  │     │              │  ├─ ModuleBootstrapper.kt
│  │     │              │  ├─ ModuleCatalogRepository.kt
│  │     │              │  ├─ ModuleDescriptor.kt
│  │     │              │  ├─ ModuleEntryPoint.kt
│  │     │              │  ├─ ModuleInstaller.kt
│  │     │              │  ├─ ModuleLauncher.kt
│  │     │              │  ├─ ModuleProvider.kt
│  │     │              │  ├─ ModuleRegistry.kt
│  │     │              │  ├─ ModuleType.kt
│  │     │              │  ├─ NavigationContributor.kt
│  │     │              │  ├─ PurchaseManager.kt
│  │     │              │  ├─ PurchaseResult.kt
│  │     │              │  ├─ RegisteredModule.kt
│  │     │              │  ├─ RegisteredModuleRepository.kt
│  │     │              │  ├─ SubscriptionRepository.kt
│  │     │              │  ├─ ToolPickerEvent.kt
│  │     │              │  └─ ToolPickerItem.kt
│  │     │              ├─ project
│  │     │              │  └─ Project.kt
│  │     │              ├─ repository
│  │     │              │  ├─ CalculationRepository.kt
│  │     │              │  ├─ ProjectRepository.kt
│  │     │              │  └─ SettingsRepository.kt
│  │     │              └─ structural
│  │     │                 ├─ BoundaryConditionDefinition.kt
│  │     │                 ├─ BoundaryConditionPreset.kt
│  │     │                 ├─ BoundaryConditions.kt
│  │     │                 ├─ BoundaryConditionValidator.kt
│  │     │                 ├─ DeflectionLimits.kt
│  │     │                 └─ ValidationResult.kt
│  │     └─ test
│  │        └─ java
│  │           └─ com
│  │              └─ lz
│  │                 └─ domain
│  │                    └─ ExampleUnitTest.kt
│  ├─ model
│  │  ├─ consumer-rules.pro
│  │  ├─ proguard-rules.pro
│  │  └─ src
│  │     ├─ androidTest
│  │     │  └─ java
│  │     │     └─ com
│  │     │        └─ lz
│  │     │           └─ model
│  │     │              └─ ExampleInstrumentedTest.kt
│  │     ├─ main
│  │     │  ├─ AndroidManifest.xml
│  │     │  └─ java
│  │     │     └─ com
│  │     │        └─ lz
│  │     │           └─ model
│  │     │              ├─ presentation
│  │     │              │  └─ ServiceabilityLimits.kt
│  │     │              ├─ regulatory
│  │     │              │  ├─ aci318
│  │     │              │  │  └─ Aci318Versions.kt
│  │     │              │  ├─ aisc
│  │     │              │  │  ├─ AiscDesignFactorRegistry.kt
│  │     │              │  │  └─ AiscDesignFactors.kt
│  │     │              │  ├─ asce7
│  │     │              │  │  └─ Asce7Versions.kt
│  │     │              │  ├─ codes
│  │     │              │  │  ├─ BuildingCode.kt
│  │     │              │  │  ├─ CodeReferenceKey.kt
│  │     │              │  │  ├─ ServiceabilityCriterion.kt
│  │     │              │  │  ├─ Standard.kt
│  │     │              │  │  └─ StandardEdition.kt
│  │     │              │  ├─ LoadCategory.kt
│  │     │              │  ├─ LoadCombinationModels.kt
│  │     │              │  ├─ loads
│  │     │              │  │  ├─ CombinationType.kt
│  │     │              │  │  └─ DesignFactors.kt
│  │     │              │  ├─ nds
│  │     │              │  │  ├─ NdsAdjustmentFactors.kt
│  │     │              │  │  └─ NdsVersions.kt
│  │     │              │  ├─ RegulatoryEnums.kt
│  │     │              │  └─ StandardReferenceKey.kt
│  │     │              ├─ structural
│  │     │              │  ├─ AnalysisModels.kt
│  │     │              │  ├─ AxialDesignModels.kt
│  │     │              │  ├─ BoundaryRestraint.kt
│  │     │              │  ├─ BracingModels.kt
│  │     │              │  ├─ CapacityResults.kt
│  │     │              │  ├─ ConstraintType.kt
│  │     │              │  ├─ DegreeOfFreedom.kt
│  │     │              │  ├─ DesignContextModels.kt
│  │     │              │  ├─ DesignEquationTrace.kt
│  │     │              │  ├─ DesignModels.kt
│  │     │              │  ├─ DofConstraint.kt
│  │     │              │  ├─ InteractionModels.kt
│  │     │              │  ├─ LimitState.kt
│  │     │              │  ├─ LoadCase.kt
│  │     │              │  ├─ LoadModels.kt
│  │     │              │  ├─ MaterialModels.kt
│  │     │              │  ├─ MaterialType.kt
│  │     │              │  ├─ NodeBoundaryCondition.kt
│  │     │              │  ├─ SectionModels.kt
│  │     │              │  ├─ ServiceabilityModels.kt
│  │     │              │  ├─ StationDemand.kt
│  │     │              │  ├─ SteelStabilityModels.kt
│  │     │              │  ├─ StructuralDemand.kt
│  │     │              │  ├─ StructuralModels.kt
│  │     │              │  └─ StructuralNode.kt
│  │     │              ├─ units
│  │     │              │  ├─ UnitFormattingService.kt
│  │     │              │  ├─ UnitModels.kt
│  │     │              │  └─ UnitSystem.kt
│  │     │              └─ util
│  │     │                 ├─ LocalDateTimeSerializer.kt
│  │     │                 └─ UUIDSerializer.kt
│  │     └─ test
│  │        └─ java
│  │           └─ com
│  │              └─ lz
│  │                 └─ model
│  │                    └─ ExampleUnitTest.kt
│  ├─ proguard-rules.pro
│  ├─ solver
│  │  ├─ consumer-rules.pro
│  │  ├─ proguard-rules.pro
│  │  └─ src
│  │     ├─ androidTest
│  │     │  └─ java
│  │     │     └─ com
│  │     │        └─ lz
│  │     │           └─ solver
│  │     │              └─ ExampleInstrumentedTest.kt
│  │     ├─ main
│  │     │  ├─ AndroidManifest.xml
│  │     │  └─ java
│  │     │     └─ com
│  │     │        └─ lz
│  │     │           └─ solver
│  │     │              ├─ analysis
│  │     │              │  ├─ AnalysisConfig.kt
│  │     │              │  ├─ LimitStateService.kt
│  │     │              │  ├─ LoadResolutionService.kt
│  │     │              │  ├─ MemberAnalysisSolver.kt
│  │     │              │  └─ StructuralSolver.kt
│  │     │              ├─ bracing
│  │     │              │  ├─ BracingLogic.kt
│  │     │              │  └─ StabilityFactorCalculator.kt
│  │     │              ├─ capacity
│  │     │              │  ├─ CapacityCalculator.kt
│  │     │              │  ├─ CapacityEngine.kt
│  │     │              │  └─ StrengthDesignService.kt
│  │     │              ├─ envelope
│  │     │              │  ├─ DemandEnvelopeResolver.kt
│  │     │              │  ├─ DesignInterpretationService.kt
│  │     │              │  ├─ ServiceabilityEvaluationService.kt
│  │     │              │  └─ ServiceabilityInterpretationService.kt
│  │     │              ├─ material
│  │     │              │  ├─ AiscCbCalculator.kt
│  │     │              │  ├─ AiscSteelCapacityCalculator.kt
│  │     │              │  ├─ MaterialDesignResolver.kt
│  │     │              │  ├─ NdsClCalculator.kt
│  │     │              │  ├─ NdsWoodCapacityCalculator.kt
│  │     │              │  └─ WoodPropertyService.kt
│  │     │              └─ regulatory
│  │     │                 ├─ LoadCombinationEngine.kt
│  │     │                 └─ RegulatoryRegistry.kt
│  │     └─ test
│  │        └─ java
│  │           └─ com
│  │              └─ lz
│  │                 └─ solver
│  │                    ├─ analysis
│  │                    │  └─ LimitStateServiceTest.kt
│  │                    └─ ExampleUnitTest.kt
│  └─ ui
│     ├─ consumer-rules.pro
│     ├─ proguard-rules.pro
│     ├─ src
│     │  ├─ androidTest
│     │  │  └─ java
│     │  │     └─ com
│     │  │        └─ lz
│     │  │           └─ ui
│     │  │              └─ ExampleInstrumentedTest.kt
│     │  ├─ main
│     │  │  ├─ AndroidManifest.xml
│     │  │  └─ java
│     │  │     └─ com
│     │  │        └─ lz
│     │  │           └─ ui
│     │  │              ├─ AnalysisChart.kt
│     │  │              ├─ boundary
│     │  │              │  ├─ BoundaryConditionPicker.kt
│     │  │              │  ├─ BoundaryConditionPickerConfig.kt
│     │  │              │  ├─ BoundaryConditionVisualizer.kt
│     │  │              │  ├─ BoundaryOptionItem.kt
│     │  │              │  ├─ BoundaryPresetOption.kt
│     │  │              │  ├─ ConstraintTypeDropdown.kt
│     │  │              │  ├─ DofConstraintEditor.kt
│     │  │              │  ├─ DofEditorConfig.kt
│     │  │              │  └─ SpringConstraintEditor.kt
│     │  │              ├─ formatting
│     │  │              │  └─ UnitFormatter.kt
│     │  │              ├─ loads
│     │  │              │  ├─ LoadCasePicker.kt
│     │  │              │  ├─ LoadCombinationPicker.kt
│     │  │              │  ├─ LoadCombinationViewer.kt
│     │  │              │  └─ LoadEditor.kt
│     │  │              ├─ material
│     │  │              │  └─ WoodMaterialPickerDialog.kt
│     │  │              ├─ member
│     │  │              │  ├─ BracingPickerDialog.kt
│     │  │              │  └─ SpanEditor.kt
│     │  │              ├─ SectionPicker.kt
│     │  │              ├─ serviceability
│     │  │              │  └─ ServiceabilityPickerDialog.kt
│     │  │              └─ UtilizationHeatMap.kt
│     │  └─ test
│     │     └─ java
│     │        └─ com
│     │           └─ lz
│     │              └─ ui
│     │                 └─ ExampleUnitTest.kt
│     └─ ui
├─ docs
│  ├─ 01-platform-architecture.md
│  ├─ 02-dependency-rule-charter.md
│  ├─ 03-adr-process.md
│  ├─ 04-module-sdk-specification.md
│  ├─ 05-engine-contract-specification.md
│  ├─ 06-tooling-runtime-specification.md
│  ├─ 07-ai-agent-orchestration-spec.md
│  ├─ 08-marketplace-runtime-spec.md
│  ├─ 09-distributed-compute-spec.md
│  ├─ 10-versioning-and-migration-strategy.md
│  ├─ architecture-validator-design.md
│  ├─ ARCHITECTURE_CONTEXT.md
│  ├─ NextSteps.md
│  ├─ rules.md
│  ├─ test example.pdf
│  └─ VECTOS_ARCHITECTURE.md
├─ feature
│  ├─ beam
│  │  ├─ consumer-rules.pro
│  │  ├─ proguard-rules.pro
│  │  └─ src
│  │     ├─ androidTest
│  │     │  └─ java
│  │     │     └─ com
│  │     │        └─ lz
│  │     │           └─ beam
│  │     │              └─ ExampleInstrumentedTest.kt
│  │     ├─ main
│  │     │  ├─ AndroidManifest.xml
│  │     │  └─ java
│  │     │     └─ com
│  │     │        └─ lz
│  │     │           └─ beam
│  │     │              ├─ data
│  │     │              │  ├─ persistence
│  │     │              │  │  └─ room
│  │     │              │  │     ├─ BeamDatabase.kt
│  │     │              │  │     ├─ BeamTypeConverters.kt
│  │     │              │  │     ├─ dao
│  │     │              │  │     │  └─ BeamCalculationDao.kt
│  │     │              │  │     └─ entity
│  │     │              │  │        └─ BeamCalculationRoomEntity.kt
│  │     │              │  └─ repository
│  │     │              │     ├─ BeamPersistenceMapper.kt
│  │     │              │     └─ RoomBeamCalculationRepository.kt
│  │     │              ├─ domain
│  │     │              │  ├─ BeamCalculationRepository.kt
│  │     │              │  └─ repository
│  │     │              ├─ model
│  │     │              │  └─ BeamModels.kt
│  │     │              ├─ plugin
│  │     │              │  ├─ BeamEntryPoint.kt
│  │     │              │  └─ BeamPlugin.kt
│  │     │              ├─ presentation
│  │     │              │  ├─ BeamDisplayModel.kt
│  │     │              │  └─ BeamViewModel.kt
│  │     │              ├─ solver
│  │     │              │  ├─ BeamAnalysisConfig.kt
│  │     │              │  └─ BeamAnalysisSolver.kt
│  │     │              └─ ui
│  │     │                 ├─ BeamBoundaryConditionConfig.kt
│  │     │                 ├─ BeamCalculatorScreen.kt
│  │     │                 ├─ BeamDiagram.kt
│  │     │                 └─ StructuralDrawingUtils.kt
│  │     └─ test
│  │        └─ java
│  │           └─ com
│  │              └─ lz
│  │                 └─ beam
│  │                    └─ ExampleUnitTest.kt
│  ├─ column
│  └─ pole
├─ gradle
│  ├─ gradle-daemon-jvm.properties
│  ├─ libs.versions.toml
│  └─ wrapper
│     ├─ gradle-wrapper.jar
│     └─ gradle-wrapper.properties
├─ gradle.properties
├─ gradlew
├─ gradlew.bat
├─ README.md
├─ runtime
│  └─ src
│     └─ main
│        └─ java
│           └─ com
│              └─ lz
│                 └─ runtime
│                    ├─ api
│                    │  ├─ CapabilityRegistry.kt
│                    │  ├─ CapabilityType.kt
│                    │  ├─ EventBus.kt
│                    │  ├─ ModuleCapability.kt
│                    │  ├─ NavigationDestination.kt
│                    │  ├─ NavigationRegistry.kt
│                    │  ├─ RuntimeConfiguration.kt
│                    │  ├─ RuntimeContext.kt
│                    │  ├─ RuntimeEnvironment.kt
│                    │  ├─ RuntimeEvent.kt
│                    │  ├─ RuntimeModule.kt
│                    │  ├─ RuntimeModuleDescriptor.kt
│                    │  ├─ RuntimeModuleLoader.kt
│                    │  ├─ RuntimeModuleProvider.kt
│                    │  ├─ RuntimeModuleRegistry.kt
│                    │  ├─ RuntimeService.kt
│                    │  ├─ RuntimeState.kt
│                    │  └─ ServiceRegistry.kt
│                    ├─ boot
│                    │  ├─ RuntimeBootstrapper.kt
│                    │  ├─ RuntimeHolder.kt
│                    │  └─ RuntimeInitializer.kt
│                    ├─ core
│                    │  ├─ AbstractRuntimeService.kt
│                    │  ├─ DefaultRuntimeContext.kt
│                    │  └─ DefaultRuntimeEnvironment.kt
│                    ├─ discovery
│                    │  └─ PlatformModuleDiscovery.kt
│                    ├─ events
│                    │  └─ DefaultEventBus.kt
│                    ├─ loader
│                    │  ├─ DefaultRuntimeModuleLoader.kt
│                    │  └─ RuntimeModuleInstaller.kt
│                    ├─ MyClass.kt
│                    ├─ registry
│                    │  ├─ DefaultCapabilityRegistry.kt
│                    │  ├─ DefaultNavigationRegistry.kt
│                    │  ├─ DefaultRuntimeModuleRegistry.kt
│                    │  └─ RegistryStore.kt
│                    └─ services
│                       └─ DefaultServiceRegistry.kt
└─ shared
   ├─ codes
   ├─ materials
   ├─ sections
   └─ structural

```