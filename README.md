
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
│  ├─ markdown.xml
│  ├─ misc.xml
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
│  │  └─ errors-1779990756611.log
│  └─ sessions
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
│  │  │  │           ├─ .artifacts
│  │  │  │           │  └─ 20260504-075725-23094890-8cf0-4a00-b3f9-a365877a2a21
│  │  │  │           │     └─ implementation_plan.artifact.md
│  │  │  │           ├─ app
│  │  │  │           │  ├─ MainActivity.kt
│  │  │  │           │  └─ VectosApplication.kt
│  │  │  │           ├─ data
│  │  │  │           │  ├─ export
│  │  │  │           │  │  ├─ CalculationExporter.kt
│  │  │  │           │  │  ├─ CalculationFormatter.kt
│  │  │  │           │  │  └─ ReportingService.kt
│  │  │  │           │  └─ persistence
│  │  │  │           │     ├─ entity
│  │  │  │           │     │  ├─ CalculationEntity.kt
│  │  │  │           │     │  └─ ProjectEntity.kt
│  │  │  │           │     ├─ mapper
│  │  │  │           │     │  └─ RoomPersistenceMapper.kt
│  │  │  │           │     ├─ repository
│  │  │  │           │     │  ├─ AiscSectionRepository.kt
│  │  │  │           │     │  ├─ CompositeSectionRepository.kt
│  │  │  │           │     │  ├─ DataStoreSettingsRepository.kt
│  │  │  │           │     │  ├─ NdsSectionRepository.kt
│  │  │  │           │     │  ├─ RoomAiscSectionRepository.kt
│  │  │  │           │     │  ├─ RoomCalculationRepository.kt
│  │  │  │           │     │  └─ RoomProjectRepository.kt
│  │  │  │           │     └─ room
│  │  │  │           │        ├─ AiscSectionSeeder.kt
│  │  │  │           │        ├─ dao
│  │  │  │           │        │  └─ SectionDaos.kt
│  │  │  │           │        ├─ entity
│  │  │  │           │        │  └─ SectionRoomEntities.kt
│  │  │  │           │        ├─ mapper
│  │  │  │           │        │  └─ SectionMappers.kt
│  │  │  │           │        ├─ MaterialSeeder.kt
│  │  │  │           │        └─ StructuralDataSeeder.kt
│  │  │  │           ├─ di
│  │  │  │           │  └─ DatabaseModule.kt
│  │  │  │           ├─ domain
│  │  │  │           │  ├─ beam
│  │  │  │           │  │  └─ BeamModels.kt
│  │  │  │           │  ├─ calculation
│  │  │  │           │  │  ├─ CalculationLifecycleService.kt
│  │  │  │           │  │  ├─ EngineeringCalculation.kt
│  │  │  │           │  │  └─ ProjectCalculationRegistry.kt
│  │  │  │           │  ├─ provenance
│  │  │  │           │  │  ├─ CalculationProvenanceService.kt
│  │  │  │           │  │  └─ ProvenanceModels.kt
│  │  │  │           │  ├─ structural
│  │  │  │           │  │  ├─ aisc
│  │  │  │           │  │  │  └─ AiscSteelCapacityCalculator.kt
│  │  │  │           │  │  ├─ analysis
│  │  │  │           │  │  │  ├─ BeamAnalysisConfig.kt
│  │  │  │           │  │  │  ├─ BeamAnalysisSolver.kt
│  │  │  │           │  │  │  ├─ core
│  │  │  │           │  │  │  │  └─ StructuralSolver.kt
│  │  │  │           │  │  │  └─ NextSteps.md
│  │  │  │           │  │  ├─ BracingLogic.kt
│  │  │  │           │  │  ├─ CapacityCalculator.kt
│  │  │  │           │  │  ├─ CapacityEngine.kt
│  │  │  │           │  │  ├─ DecisionCaptureService.kt
│  │  │  │           │  │  ├─ DesignInterpretationService.kt
│  │  │  │           │  │  ├─ LimitStateService.kt
│  │  │  │           │  │  ├─ LoadCaseModels.kt
│  │  │  │           │  │  ├─ LoadCombinationEngine.kt
│  │  │  │           │  │  ├─ LoadModels.kt
│  │  │  │           │  │  ├─ LoadResolutionService.kt
│  │  │  │           │  │  ├─ nds
│  │  │  │           │  │  │  └─ NdsWoodCapacityCalculator.kt
│  │  │  │           │  │  ├─ ServiceabilityEvaluationService.kt
│  │  │  │           │  │  ├─ ServiceabilityInterpretationService.kt
│  │  │  │           │  │  ├─ ServiceabilityLimits.kt
│  │  │  │           │  │  ├─ StrengthDesignService.kt
│  │  │  │           │  │  └─ WoodPropertyService.kt
│  │  │  │           │  ├─ units
│  │  │  │           │  │  └─ UnitFormattingService.kt
│  │  │  │           │  └─ versioning
│  │  │  │           │     ├─ CalculationVersioningService.kt
│  │  │  │           │     └─ VersioningModels.kt
│  │  │  │           ├─ presentation
│  │  │  │           │  ├─ BeamDisplayModel.kt
│  │  │  │           │  ├─ BeamViewModel.kt
│  │  │  │           │  ├─ CalculationContext.kt
│  │  │  │           │  ├─ ProjectViewModel.kt
│  │  │  │           │  └─ SettingsViewModel.kt
│  │  │  │           ├─ ui
│  │  │  │           │  ├─ beam
│  │  │  │           │  │  ├─ AnalysisChart.kt
│  │  │  │           │  │  ├─ BeamCalculatorScreen.kt
│  │  │  │           │  │  ├─ BeamDiagram.kt
│  │  │  │           │  │  ├─ SectionPicker.kt
│  │  │  │           │  │  ├─ StructuralDrawingUtils.kt
│  │  │  │           │  │  ├─ SupportConditionPicker.kt
│  │  │  │           │  │  └─ UtilizationHeatMap.kt
│  │  │  │           │  ├─ calculator
│  │  │  │           │  │  ├─ BeamCalculatorDefinition.kt
│  │  │  │           │  │  ├─ CalculatorDefinition.kt
│  │  │  │           │  │  ├─ CalculatorRegistry.kt
│  │  │  │           │  │  └─ CalculatorRoute.kt
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
│  │  │  │           │     ├─ BracingPickerDialog.kt
│  │  │  │           │     ├─ LoadCasePicker.kt
│  │  │  │           │     ├─ LoadCombinationPicker.kt
│  │  │  │           │     ├─ LoadCombinationViewer.kt
│  │  │  │           │     ├─ LoadEditor.kt
│  │  │  │           │     ├─ RevisionHistory.kt
│  │  │  │           │     ├─ ServiceabilityPickerDialog.kt
│  │  │  │           │     ├─ SpanEditor.kt
│  │  │  │           │     ├─ ToolPickerScreen.kt
│  │  │  │           │     ├─ UnitFormatter.kt
│  │  │  │           │     └─ WoodMaterialPickerDialog.kt
│  │  │  │           ├─ util
│  │  │  │           │  └─ serialization
│  │  │  │           │     └─ LocalDateTimeSerializer.kt
│  │  │  │           └─ VectosApplication.kt
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
│  │     │              │     │  └─ ProjectDao.kt
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
│  │     │              │     │  └─ ProjectRoomEntity.kt
│  │     │              │     ├─ mapper
│  │     │              │     │  └─ StructuralMappers.kt
│  │     │              │     ├─ Migrations.kt
│  │     │              │     ├─ repository
│  │     │              │     │  └─ RoomMaterialRepository.kt
│  │     │              │     └─ StandardTypeConverters.kt
│  │     │              └─ repository
│  │     │                 ├─ BuildingCodeRepository.kt
│  │     │                 ├─ CalculationWriter.kt
│  │     │                 └─ RoomCalculationWriter.kt
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
│  │     │              │  └─ CalculationMetadata.kt
│  │     │              ├─ material
│  │     │              │  └─ MaterialRepository.kt
│  │     │              ├─ project
│  │     │              │  └─ Project.kt
│  │     │              └─ repository
│  │     │                 ├─ CalculationRepository.kt
│  │     │                 ├─ ProjectRepository.kt
│  │     │                 └─ SettingsRepository.kt
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
│  │     │              │  ├─ DesignContextModels.kt
│  │     │              │  ├─ DesignEquationTrace.kt
│  │     │              │  ├─ DesignModels.kt
│  │     │              │  ├─ InteractionModels.kt
│  │     │              │  ├─ LimitState.kt
│  │     │              │  ├─ MaterialModels.kt
│  │     │              │  ├─ MaterialType.kt
│  │     │              │  ├─ SectionModels.kt
│  │     │              │  ├─ ServiceabilityModels.kt
│  │     │              │  ├─ StationDemand.kt
│  │     │              │  ├─ SteelStabilityModels.kt
│  │     │              │  ├─ StructuralDemand.kt
│  │     │              │  └─ StructuralModels.kt
│  │     │              ├─ units
│  │     │              │  ├─ UnitModels.kt
│  │     │              │  └─ UnitSystem.kt
│  │     │              └─ util
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
│  │     │              ├─ envelope
│  │     │              ├─ material
│  │     │              │  └─ MaterialDesignResolver.kt
│  │     │              └─ regulatory
│  │     │                 ├─ LoadCombinationEngine.kt
│  │     │                 └─ RegulatoryRegistry.kt
│  │     └─ test
│  │        └─ java
│  │           └─ com
│  │              └─ lz
│  │                 └─ solver
│  │                    └─ ExampleUnitTest.kt
│  └─ ui
│     ├─ consumer-rules.pro
│     ├─ proguard-rules.pro
│     └─ src
│        ├─ androidTest
│        │  └─ java
│        │     └─ com
│        │        └─ lz
│        │           └─ ui
│        │              └─ ExampleInstrumentedTest.kt
│        ├─ main
│        │  ├─ AndroidManifest.xml
│        │  └─ java
│        │     └─ com
│        │        └─ lz
│        │           └─ ui
│        └─ test
│           └─ java
│              └─ com
│                 └─ lz
│                    └─ ui
│                       └─ ExampleUnitTest.kt
├─ docs
│  ├─ ARCHITECTURE_CONTEXT.md
│  └─ test example.pdf
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
│  │     │              │     └─ RoomBeamCalculationRepository.kt
│  │     │              ├─ domain
│  │     │              │  ├─ BeamCalculationRepository.kt
│  │     │              │  └─ repository
│  │     │              ├─ solver
│  │     │              └─ ui
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