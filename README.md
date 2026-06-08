# VectOS
Engineering calculation tool

```
VectOS
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
│  │  │  └─ 18.json
│  │  └─ com.lz.vectos.persistence.room.AppDatabase
│  │     ├─ 1.json
│  │     ├─ 10.json
│  │     ├─ 12.json
│  │     ├─ 14.json
│  │     ├─ 15.json
│  │     ├─ 16.json
│  │     ├─ 17.json
│  │     ├─ 18.json
│  │     ├─ 2.json
│  │     ├─ 3.json
│  │     ├─ 4.json
│  │     ├─ 5.json
│  │     ├─ 7.json
│  │     └─ 9.json
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
│  │  │  │           ├─ application
│  │  │  │           │  └─ repository
│  │  │  │           │     ├─ CalculationRepository.kt
│  │  │  │           │     ├─ ProjectRepository.kt
│  │  │  │           │     └─ SettingsRepository.kt
│  │  │  │           ├─ data
│  │  │  │           │  ├─ export
│  │  │  │           │  │  ├─ CalculationExporter.kt
│  │  │  │           │  │  ├─ CalculationFormatter.kt
│  │  │  │           │  │  └─ ReportingService.kt
│  │  │  │           │  ├─ persistence
│  │  │  │           │  │  ├─ entity
│  │  │  │           │  │  │  ├─ BeamCalculationEntity.kt
│  │  │  │           │  │  │  ├─ CalculationEntity.kt
│  │  │  │           │  │  │  └─ ProjectEntity.kt
│  │  │  │           │  │  ├─ mapper
│  │  │  │           │  │  │  └─ RoomPersistenceMapper.kt
│  │  │  │           │  │  ├─ repository
│  │  │  │           │  │  │  ├─ AiscSectionRepository.kt
│  │  │  │           │  │  │  ├─ CompositeSectionRepository.kt
│  │  │  │           │  │  │  ├─ DataStoreSettingsRepository.kt
│  │  │  │           │  │  │  ├─ NdsSectionRepository.kt
│  │  │  │           │  │  │  ├─ RoomAiscSectionRepository.kt
│  │  │  │           │  │  │  ├─ RoomCalculationRepository.kt
│  │  │  │           │  │  │  ├─ RoomMaterialRepository.kt
│  │  │  │           │  │  │  ├─ RoomProjectRepository.kt
│  │  │  │           │  │  │  └─ StructuralRepository.kt
│  │  │  │           │  │  └─ room
│  │  │  │           │  │     ├─ AiscSectionSeeder.kt
│  │  │  │           │  │     ├─ AppDatabase.kt
│  │  │  │           │  │     ├─ dao
│  │  │  │           │  │     │  ├─ BeamCalculationDao.kt
│  │  │  │           │  │     │  ├─ BuildingCodeDao.kt
│  │  │  │           │  │     │  ├─ CalculationDao.kt
│  │  │  │           │  │     │  ├─ MaterialDao.kt
│  │  │  │           │  │     │  ├─ ProjectDao.kt
│  │  │  │           │  │     │  ├─ SectionDaos.kt
│  │  │  │           │  │     │  └─ StructuralDataDao.kt
│  │  │  │           │  │     ├─ entity
│  │  │  │           │  │     │  ├─ BeamCalculationRoomEntity.kt
│  │  │  │           │  │     │  ├─ BuildingCodeEntities.kt
│  │  │  │           │  │     │  ├─ CalculationRoomEntity.kt
│  │  │  │           │  │     │  ├─ MaterialRoomEntity.kt
│  │  │  │           │  │     │  ├─ ProjectRoomEntity.kt
│  │  │  │           │  │     │  ├─ SectionRoomEntities.kt
│  │  │  │           │  │     │  └─ StructuralDataEntities.kt
│  │  │  │           │  │     ├─ mapper
│  │  │  │           │  │     │  ├─ SectionMappers.kt
│  │  │  │           │  │     │  └─ StructuralMappers.kt
│  │  │  │           │  │     ├─ MaterialSeeder.kt
│  │  │  │           │  │     ├─ Migrations.kt
│  │  │  │           │  │     ├─ StandardTypeConverters.kt
│  │  │  │           │  │     └─ StructuralDataSeeder.kt
│  │  │  │           │  └─ repository
│  │  │  │           │     ├─ CalculationRepository.kt
│  │  │  │           │     ├─ ProjectRepository.kt
│  │  │  │           │     └─ StructuralCodeRepository.kt
│  │  │  │           ├─ domain
│  │  │  │           │  ├─ beam
│  │  │  │           │  │  ├─ BeamModels.kt
│  │  │  │           │  │  ├─ MaterialRepository.kt
│  │  │  │           │  │  ├─ MaterialType.kt
│  │  │  │           │  │  └─ SectionModels.kt
│  │  │  │           │  ├─ calculation
│  │  │  │           │  │  ├─ CalculationLifecycleService.kt
│  │  │  │           │  │  ├─ CalculationMetadata.kt
│  │  │  │           │  │  ├─ CalculationRepository.kt
│  │  │  │           │  │  ├─ EngineeringCalculation.kt
│  │  │  │           │  │  └─ ProjectCalculationRegistry.kt
│  │  │  │           │  ├─ project
│  │  │  │           │  │  ├─ Project.kt
│  │  │  │           │  │  ├─ ProjectRepository.kt
│  │  │  │           │  │  └─ ProjectSettings.kt
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
│  │  │  │           │  │  ├─ AnalysisModels.kt
│  │  │  │           │  │  ├─ AsceEdition.kt
│  │  │  │           │  │  ├─ AxialDesignModels.kt
│  │  │  │           │  │  ├─ BoundaryRestraint.kt
│  │  │  │           │  │  ├─ BracingLogic.kt
│  │  │  │           │  │  ├─ BracingModels.kt
│  │  │  │           │  │  ├─ BuildingCode.kt
│  │  │  │           │  │  ├─ CapacityCalculator.kt
│  │  │  │           │  │  ├─ CapacityEngine.kt
│  │  │  │           │  │  ├─ CombinationType.kt
│  │  │  │           │  │  ├─ DecisionCaptureService.kt
│  │  │  │           │  │  ├─ DesignContextModels.kt
│  │  │  │           │  │  ├─ DesignEquationTrace.kt
│  │  │  │           │  │  ├─ DesignInterpretationService.kt
│  │  │  │           │  │  ├─ DesignModels.kt
│  │  │  │           │  │  ├─ InteractionModels.kt
│  │  │  │           │  │  ├─ LimitState.kt
│  │  │  │           │  │  ├─ LimitStateService.kt
│  │  │  │           │  │  ├─ LoadCaseModels.kt
│  │  │  │           │  │  ├─ LoadCategory.kt
│  │  │  │           │  │  ├─ LoadCombinationEngine.kt
│  │  │  │           │  │  ├─ LoadCombinationModels.kt
│  │  │  │           │  │  ├─ LoadModels.kt
│  │  │  │           │  │  ├─ LoadResolutionService.kt
│  │  │  │           │  │  ├─ MaterialDesignResolver.kt
│  │  │  │           │  │  ├─ MaterialDesignStrategy.kt
│  │  │  │           │  │  ├─ MaterialModels.kt
│  │  │  │           │  │  ├─ nds
│  │  │  │           │  │  │  └─ NdsWoodCapacityCalculator.kt
│  │  │  │           │  │  ├─ ServiceabilityEvaluationService.kt
│  │  │  │           │  │  ├─ ServiceabilityInterpretationService.kt
│  │  │  │           │  │  ├─ ServiceabilityLimits.kt
│  │  │  │           │  │  ├─ ServiceabilityModels.kt
│  │  │  │           │  │  ├─ StationDemand.kt
│  │  │  │           │  │  ├─ SteelStabilityModels.kt
│  │  │  │           │  │  ├─ StrengthDesignService.kt
│  │  │  │           │  │  ├─ StructuralDemand.kt
│  │  │  │           │  │  ├─ StructuralModels.kt
│  │  │  │           │  │  ├─ StructuralReferenceKey.kt
│  │  │  │           │  │  └─ WoodPropertyService.kt
│  │  │  │           │  ├─ units
│  │  │  │           │  │  ├─ UnitFormattingService.kt
│  │  │  │           │  │  ├─ UnitModels.kt
│  │  │  │           │  │  └─ UnitSystem.kt
│  │  │  │           │  └─ versioning
│  │  │  │           │     ├─ CalculationVersioningService.kt
│  │  │  │           │     └─ VersioningModels.kt
│  │  │  │           ├─ MainActivity.kt
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
│  │  │  │           └─ util
│  │  │  │              └─ serialization
│  │  │  │                 ├─ LocalDateTimeSerializer.kt
│  │  │  │                 └─ UUIDSerializer.kt
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
├─ docs
│  ├─ ARCHITECTURE_CONTEXT.md
│  └─ test example.pdf
├─ emulator_screen.png
├─ emulator_screen_after.png
├─ emulator_screen_after2.png
├─ emulator_verify.png
├─ gradle
│  ├─ gradle-daemon-jvm.properties
│  ├─ libs.versions.toml
│  └─ wrapper
│     ├─ gradle-wrapper.jar
│     └─ gradle-wrapper.properties
├─ gradle.properties
├─ gradlew
├─ gradlew.bat
├─ home_screen.png
├─ README.md
├─ reorg_script.py
├─ screenshot.png
├─ temp
├─ test_errors.txt
├─ test_errors_2.txt
├─ test_errors_utf8.txt
├─ test_errors_utf8_2.txt
├─ test_output.txt
├─ test_output_utf8.txt
└─ view.xml

```
```
VectOS
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
│  │  │  └─ 18.json
│  │  └─ com.lz.vectos.persistence.room.AppDatabase
│  │     ├─ 1.json
│  │     ├─ 10.json
│  │     ├─ 12.json
│  │     ├─ 14.json
│  │     ├─ 15.json
│  │     ├─ 16.json
│  │     ├─ 17.json
│  │     ├─ 18.json
│  │     ├─ 2.json
│  │     ├─ 3.json
│  │     ├─ 4.json
│  │     ├─ 5.json
│  │     ├─ 7.json
│  │     └─ 9.json
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
│  │  │  │           ├─ application
│  │  │  │           │  └─ repository
│  │  │  │           │     ├─ CalculationRepository.kt
│  │  │  │           │     ├─ ProjectRepository.kt
│  │  │  │           │     └─ SettingsRepository.kt
│  │  │  │           ├─ data
│  │  │  │           │  ├─ export
│  │  │  │           │  │  ├─ CalculationExporter.kt
│  │  │  │           │  │  ├─ CalculationFormatter.kt
│  │  │  │           │  │  └─ ReportingService.kt
│  │  │  │           │  ├─ persistence
│  │  │  │           │  │  ├─ entity
│  │  │  │           │  │  │  ├─ BeamCalculationEntity.kt
│  │  │  │           │  │  │  ├─ CalculationEntity.kt
│  │  │  │           │  │  │  └─ ProjectEntity.kt
│  │  │  │           │  │  ├─ mapper
│  │  │  │           │  │  │  └─ RoomPersistenceMapper.kt
│  │  │  │           │  │  ├─ repository
│  │  │  │           │  │  │  ├─ AiscSectionRepository.kt
│  │  │  │           │  │  │  ├─ CompositeSectionRepository.kt
│  │  │  │           │  │  │  ├─ DataStoreSettingsRepository.kt
│  │  │  │           │  │  │  ├─ NdsSectionRepository.kt
│  │  │  │           │  │  │  ├─ RoomAiscSectionRepository.kt
│  │  │  │           │  │  │  ├─ RoomCalculationRepository.kt
│  │  │  │           │  │  │  ├─ RoomMaterialRepository.kt
│  │  │  │           │  │  │  ├─ RoomProjectRepository.kt
│  │  │  │           │  │  │  └─ StructuralRepository.kt
│  │  │  │           │  │  └─ room
│  │  │  │           │  │     ├─ AiscSectionSeeder.kt
│  │  │  │           │  │     ├─ AppDatabase.kt
│  │  │  │           │  │     ├─ dao
│  │  │  │           │  │     │  ├─ BeamCalculationDao.kt
│  │  │  │           │  │     │  ├─ BuildingCodeDao.kt
│  │  │  │           │  │     │  ├─ CalculationDao.kt
│  │  │  │           │  │     │  ├─ MaterialDao.kt
│  │  │  │           │  │     │  ├─ ProjectDao.kt
│  │  │  │           │  │     │  ├─ SectionDaos.kt
│  │  │  │           │  │     │  └─ StructuralDataDao.kt
│  │  │  │           │  │     ├─ entity
│  │  │  │           │  │     │  ├─ BeamCalculationRoomEntity.kt
│  │  │  │           │  │     │  ├─ BuildingCodeEntities.kt
│  │  │  │           │  │     │  ├─ CalculationRoomEntity.kt
│  │  │  │           │  │     │  ├─ MaterialRoomEntity.kt
│  │  │  │           │  │     │  ├─ ProjectRoomEntity.kt
│  │  │  │           │  │     │  ├─ SectionRoomEntities.kt
│  │  │  │           │  │     │  └─ StructuralDataEntities.kt
│  │  │  │           │  │     ├─ mapper
│  │  │  │           │  │     │  ├─ SectionMappers.kt
│  │  │  │           │  │     │  └─ StructuralMappers.kt
│  │  │  │           │  │     ├─ MaterialSeeder.kt
│  │  │  │           │  │     ├─ Migrations.kt
│  │  │  │           │  │     ├─ StandardTypeConverters.kt
│  │  │  │           │  │     └─ StructuralDataSeeder.kt
│  │  │  │           │  └─ repository
│  │  │  │           │     ├─ CalculationRepository.kt
│  │  │  │           │     ├─ ProjectRepository.kt
│  │  │  │           │     └─ StructuralCodeRepository.kt
│  │  │  │           ├─ domain
│  │  │  │           │  ├─ beam
│  │  │  │           │  │  ├─ BeamModels.kt
│  │  │  │           │  │  ├─ MaterialRepository.kt
│  │  │  │           │  │  ├─ MaterialType.kt
│  │  │  │           │  │  └─ SectionModels.kt
│  │  │  │           │  ├─ calculation
│  │  │  │           │  │  ├─ CalculationLifecycleService.kt
│  │  │  │           │  │  ├─ CalculationMetadata.kt
│  │  │  │           │  │  ├─ CalculationRepository.kt
│  │  │  │           │  │  ├─ EngineeringCalculation.kt
│  │  │  │           │  │  └─ ProjectCalculationRegistry.kt
│  │  │  │           │  ├─ project
│  │  │  │           │  │  ├─ Project.kt
│  │  │  │           │  │  ├─ ProjectRepository.kt
│  │  │  │           │  │  └─ ProjectSettings.kt
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
│  │  │  │           │  │  ├─ AnalysisModels.kt
│  │  │  │           │  │  ├─ AsceEdition.kt
│  │  │  │           │  │  ├─ AxialDesignModels.kt
│  │  │  │           │  │  ├─ BoundaryRestraint.kt
│  │  │  │           │  │  ├─ BracingLogic.kt
│  │  │  │           │  │  ├─ BracingModels.kt
│  │  │  │           │  │  ├─ BuildingCode.kt
│  │  │  │           │  │  ├─ CapacityCalculator.kt
│  │  │  │           │  │  ├─ CapacityEngine.kt
│  │  │  │           │  │  ├─ CombinationType.kt
│  │  │  │           │  │  ├─ DecisionCaptureService.kt
│  │  │  │           │  │  ├─ DesignContextModels.kt
│  │  │  │           │  │  ├─ DesignEquationTrace.kt
│  │  │  │           │  │  ├─ DesignInterpretationService.kt
│  │  │  │           │  │  ├─ DesignModels.kt
│  │  │  │           │  │  ├─ InteractionModels.kt
│  │  │  │           │  │  ├─ LimitState.kt
│  │  │  │           │  │  ├─ LimitStateService.kt
│  │  │  │           │  │  ├─ LoadCaseModels.kt
│  │  │  │           │  │  ├─ LoadCategory.kt
│  │  │  │           │  │  ├─ LoadCombinationEngine.kt
│  │  │  │           │  │  ├─ LoadCombinationModels.kt
│  │  │  │           │  │  ├─ LoadModels.kt
│  │  │  │           │  │  ├─ LoadResolutionService.kt
│  │  │  │           │  │  ├─ MaterialDesignResolver.kt
│  │  │  │           │  │  ├─ MaterialDesignStrategy.kt
│  │  │  │           │  │  ├─ MaterialModels.kt
│  │  │  │           │  │  ├─ nds
│  │  │  │           │  │  │  └─ NdsWoodCapacityCalculator.kt
│  │  │  │           │  │  ├─ ServiceabilityEvaluationService.kt
│  │  │  │           │  │  ├─ ServiceabilityInterpretationService.kt
│  │  │  │           │  │  ├─ ServiceabilityLimits.kt
│  │  │  │           │  │  ├─ ServiceabilityModels.kt
│  │  │  │           │  │  ├─ StationDemand.kt
│  │  │  │           │  │  ├─ SteelStabilityModels.kt
│  │  │  │           │  │  ├─ StrengthDesignService.kt
│  │  │  │           │  │  ├─ StructuralDemand.kt
│  │  │  │           │  │  ├─ StructuralModels.kt
│  │  │  │           │  │  ├─ StructuralReferenceKey.kt
│  │  │  │           │  │  └─ WoodPropertyService.kt
│  │  │  │           │  ├─ units
│  │  │  │           │  │  ├─ UnitFormattingService.kt
│  │  │  │           │  │  ├─ UnitModels.kt
│  │  │  │           │  │  └─ UnitSystem.kt
│  │  │  │           │  └─ versioning
│  │  │  │           │     ├─ CalculationVersioningService.kt
│  │  │  │           │     └─ VersioningModels.kt
│  │  │  │           ├─ MainActivity.kt
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
│  │  │  │           └─ util
│  │  │  │              └─ serialization
│  │  │  │                 ├─ LocalDateTimeSerializer.kt
│  │  │  │                 └─ UUIDSerializer.kt
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
├─ docs
│  ├─ ARCHITECTURE_CONTEXT.md
│  └─ test example.pdf
├─ emulator_screen.png
├─ emulator_screen_after.png
├─ emulator_screen_after2.png
├─ emulator_verify.png
├─ gradle
│  ├─ gradle-daemon-jvm.properties
│  ├─ libs.versions.toml
│  └─ wrapper
│     ├─ gradle-wrapper.jar
│     └─ gradle-wrapper.properties
├─ gradle.properties
├─ gradlew
├─ gradlew.bat
├─ home_screen.png
├─ README.md
├─ reorg_script.py
├─ screenshot.png
├─ temp
├─ test_errors.txt
├─ test_errors_2.txt
├─ test_errors_utf8.txt
├─ test_errors_utf8_2.txt
├─ test_output.txt
├─ test_output_utf8.txt
└─ view.xml

```
```
VectOS
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
│  │  │  └─ 18.json
│  │  └─ com.lz.vectos.persistence.room.AppDatabase
│  │     ├─ 1.json
│  │     ├─ 10.json
│  │     ├─ 12.json
│  │     ├─ 14.json
│  │     ├─ 15.json
│  │     ├─ 16.json
│  │     ├─ 17.json
│  │     ├─ 18.json
│  │     ├─ 2.json
│  │     ├─ 3.json
│  │     ├─ 4.json
│  │     ├─ 5.json
│  │     ├─ 7.json
│  │     └─ 9.json
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
│  │  │  │           │  └─ MainActivity.kt
│  │  │  │           ├─ application
│  │  │  │           │  └─ repository
│  │  │  │           │     ├─ CalculationRepository.kt
│  │  │  │           │     ├─ ProjectRepository.kt
│  │  │  │           │     └─ SettingsRepository.kt
│  │  │  │           ├─ data
│  │  │  │           │  ├─ export
│  │  │  │           │  │  ├─ CalculationExporter.kt
│  │  │  │           │  │  ├─ CalculationFormatter.kt
│  │  │  │           │  │  └─ ReportingService.kt
│  │  │  │           │  ├─ persistence
│  │  │  │           │  │  ├─ entity
│  │  │  │           │  │  │  ├─ BeamCalculationEntity.kt
│  │  │  │           │  │  │  ├─ CalculationEntity.kt
│  │  │  │           │  │  │  └─ ProjectEntity.kt
│  │  │  │           │  │  ├─ mapper
│  │  │  │           │  │  │  └─ RoomPersistenceMapper.kt
│  │  │  │           │  │  ├─ repository
│  │  │  │           │  │  │  ├─ AiscSectionRepository.kt
│  │  │  │           │  │  │  ├─ CompositeSectionRepository.kt
│  │  │  │           │  │  │  ├─ DataStoreSettingsRepository.kt
│  │  │  │           │  │  │  ├─ NdsSectionRepository.kt
│  │  │  │           │  │  │  ├─ RoomAiscSectionRepository.kt
│  │  │  │           │  │  │  ├─ RoomCalculationRepository.kt
│  │  │  │           │  │  │  ├─ RoomMaterialRepository.kt
│  │  │  │           │  │  │  ├─ RoomProjectRepository.kt
│  │  │  │           │  │  │  └─ StructuralRepository.kt
│  │  │  │           │  │  └─ room
│  │  │  │           │  │     ├─ AiscSectionSeeder.kt
│  │  │  │           │  │     ├─ AppDatabase.kt
│  │  │  │           │  │     ├─ dao
│  │  │  │           │  │     │  ├─ BeamCalculationDao.kt
│  │  │  │           │  │     │  ├─ BuildingCodeDao.kt
│  │  │  │           │  │     │  ├─ CalculationDao.kt
│  │  │  │           │  │     │  ├─ MaterialDao.kt
│  │  │  │           │  │     │  ├─ ProjectDao.kt
│  │  │  │           │  │     │  ├─ SectionDaos.kt
│  │  │  │           │  │     │  └─ StructuralDataDao.kt
│  │  │  │           │  │     ├─ entity
│  │  │  │           │  │     │  ├─ BeamCalculationRoomEntity.kt
│  │  │  │           │  │     │  ├─ BuildingCodeEntities.kt
│  │  │  │           │  │     │  ├─ CalculationRoomEntity.kt
│  │  │  │           │  │     │  ├─ MaterialRoomEntity.kt
│  │  │  │           │  │     │  ├─ ProjectRoomEntity.kt
│  │  │  │           │  │     │  ├─ SectionRoomEntities.kt
│  │  │  │           │  │     │  └─ StructuralDataEntities.kt
│  │  │  │           │  │     ├─ mapper
│  │  │  │           │  │     │  ├─ SectionMappers.kt
│  │  │  │           │  │     │  └─ StructuralMappers.kt
│  │  │  │           │  │     ├─ MaterialSeeder.kt
│  │  │  │           │  │     ├─ Migrations.kt
│  │  │  │           │  │     ├─ StandardTypeConverters.kt
│  │  │  │           │  │     └─ StructuralDataSeeder.kt
│  │  │  │           │  └─ repository
│  │  │  │           │     ├─ CalculationRepository.kt
│  │  │  │           │     ├─ ProjectRepository.kt
│  │  │  │           │     └─ StructuralCodeRepository.kt
│  │  │  │           ├─ domain
│  │  │  │           │  ├─ beam
│  │  │  │           │  │  ├─ BeamModels.kt
│  │  │  │           │  │  ├─ MaterialRepository.kt
│  │  │  │           │  │  ├─ MaterialType.kt
│  │  │  │           │  │  └─ SectionModels.kt
│  │  │  │           │  ├─ calculation
│  │  │  │           │  │  ├─ CalculationLifecycleService.kt
│  │  │  │           │  │  ├─ CalculationMetadata.kt
│  │  │  │           │  │  ├─ CalculationRepository.kt
│  │  │  │           │  │  ├─ EngineeringCalculation.kt
│  │  │  │           │  │  └─ ProjectCalculationRegistry.kt
│  │  │  │           │  ├─ project
│  │  │  │           │  │  ├─ Project.kt
│  │  │  │           │  │  ├─ ProjectRepository.kt
│  │  │  │           │  │  └─ ProjectSettings.kt
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
│  │  │  │           │  │  ├─ AnalysisModels.kt
│  │  │  │           │  │  ├─ AsceEdition.kt
│  │  │  │           │  │  ├─ AxialDesignModels.kt
│  │  │  │           │  │  ├─ BoundaryRestraint.kt
│  │  │  │           │  │  ├─ BracingLogic.kt
│  │  │  │           │  │  ├─ BracingModels.kt
│  │  │  │           │  │  ├─ BuildingCode.kt
│  │  │  │           │  │  ├─ CapacityCalculator.kt
│  │  │  │           │  │  ├─ CapacityEngine.kt
│  │  │  │           │  │  ├─ CombinationType.kt
│  │  │  │           │  │  ├─ DecisionCaptureService.kt
│  │  │  │           │  │  ├─ DesignContextModels.kt
│  │  │  │           │  │  ├─ DesignInterpretationService.kt
│  │  │  │           │  │  ├─ InteractionModels.kt
│  │  │  │           │  │  ├─ LimitState.kt
│  │  │  │           │  │  ├─ LimitStateService.kt
│  │  │  │           │  │  ├─ LoadCaseModels.kt
│  │  │  │           │  │  ├─ LoadCategory.kt
│  │  │  │           │  │  ├─ LoadCombinationEngine.kt
│  │  │  │           │  │  ├─ LoadCombinationModels.kt
│  │  │  │           │  │  ├─ LoadModels.kt
│  │  │  │           │  │  ├─ LoadResolutionService.kt
│  │  │  │           │  │  ├─ MaterialDesignResolver.kt
│  │  │  │           │  │  ├─ MaterialDesignStrategy.kt
│  │  │  │           │  │  ├─ MaterialModels.kt
│  │  │  │           │  │  ├─ nds
│  │  │  │           │  │  │  └─ NdsWoodCapacityCalculator.kt
│  │  │  │           │  │  ├─ ServiceabilityEvaluationService.kt
│  │  │  │           │  │  ├─ ServiceabilityInterpretationService.kt
│  │  │  │           │  │  ├─ ServiceabilityLimits.kt
│  │  │  │           │  │  ├─ ServiceabilityModels.kt
│  │  │  │           │  │  ├─ StationDemand.kt
│  │  │  │           │  │  ├─ SteelStabilityModels.kt
│  │  │  │           │  │  ├─ StrengthDesignService.kt
│  │  │  │           │  │  ├─ StructuralDemand.kt
│  │  │  │           │  │  ├─ StructuralModels.kt
│  │  │  │           │  │  ├─ StructuralReferenceKey.kt
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
│  │  │  │           └─ util
│  │  │  │              └─ serialization
│  │  │  │                 ├─ LocalDateTimeSerializer.kt
│  │  │  │                 └─ UUIDSerializer.kt
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
├─ assets
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
│  │     │              ├─ structural
│  │     │              │  ├─ DesignEquationTrace.kt
│  │     │              │  └─ DesignModels.kt
│  │     │              └─ units
│  │     │                 ├─ UnitModels.kt
│  │     │                 └─ UnitSystem.kt
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
│  │     └─ test
│  │        └─ java
│  │           └─ com
│  │              └─ lz
│  │                 └─ solver
│  │                    └─ ExampleUnitTest.kt
│  ├─ src
│  │  ├─ androidTest
│  │  │  └─ java
│  │  │     └─ com
│  │  │        └─ lz
│  │  │           └─ vectos
│  │  │              └─ core
│  │  │                 └─ ExampleInstrumentedTest.kt
│  │  ├─ main
│  │  │  ├─ AndroidManifest.xml
│  │  │  └─ java
│  │  │     └─ com
│  │  │        └─ lz
│  │  │           └─ vectos
│  │  │              └─ core
│  │  │                 └─ domain
│  │  │                    └─ repository
│  │  └─ test
│  │     └─ java
│  │        └─ com
│  │           └─ lz
│  │              └─ vectos
│  │                 └─ core
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
├─ emulator_screen.png
├─ emulator_screen_after.png
├─ emulator_screen_after2.png
├─ emulator_verify.png
├─ feature
│  ├─ beam
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
├─ home_screen.png
├─ README.md
├─ reorg_script.py
├─ screenshot.png
├─ shared
│  ├─ codes
│  ├─ materials
│  ├─ sections
│  └─ structural
├─ temp
├─ test_errors.txt
├─ test_errors_2.txt
├─ test_errors_utf8.txt
├─ test_errors_utf8_2.txt
├─ test_output.txt
├─ test_output_utf8.txt
└─ view.xml

```
```
VectOS
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
│  │  │  └─ 18.json
│  │  └─ com.lz.vectos.persistence.room.AppDatabase
│  │     ├─ 1.json
│  │     ├─ 10.json
│  │     ├─ 12.json
│  │     ├─ 14.json
│  │     ├─ 15.json
│  │     ├─ 16.json
│  │     ├─ 17.json
│  │     ├─ 18.json
│  │     ├─ 2.json
│  │     ├─ 3.json
│  │     ├─ 4.json
│  │     ├─ 5.json
│  │     ├─ 7.json
│  │     └─ 9.json
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
│  │  │  │           │  └─ MainActivity.kt
│  │  │  │           ├─ data
│  │  │  │           │  ├─ export
│  │  │  │           │  │  ├─ CalculationExporter.kt
│  │  │  │           │  │  ├─ CalculationFormatter.kt
│  │  │  │           │  │  └─ ReportingService.kt
│  │  │  │           │  ├─ persistence
│  │  │  │           │  │  ├─ entity
│  │  │  │           │  │  │  ├─ BeamCalculationEntity.kt
│  │  │  │           │  │  │  ├─ CalculationEntity.kt
│  │  │  │           │  │  │  └─ ProjectEntity.kt
│  │  │  │           │  │  ├─ mapper
│  │  │  │           │  │  │  └─ RoomPersistenceMapper.kt
│  │  │  │           │  │  ├─ repository
│  │  │  │           │  │  │  ├─ AiscSectionRepository.kt
│  │  │  │           │  │  │  ├─ CompositeSectionRepository.kt
│  │  │  │           │  │  │  ├─ DataStoreSettingsRepository.kt
│  │  │  │           │  │  │  ├─ NdsSectionRepository.kt
│  │  │  │           │  │  │  ├─ RoomAiscSectionRepository.kt
│  │  │  │           │  │  │  ├─ RoomCalculationRepository.kt
│  │  │  │           │  │  │  ├─ RoomMaterialRepository.kt
│  │  │  │           │  │  │  ├─ RoomProjectRepository.kt
│  │  │  │           │  │  │  └─ StructuralRepository.kt
│  │  │  │           │  │  └─ room
│  │  │  │           │  │     ├─ AiscSectionSeeder.kt
│  │  │  │           │  │     ├─ AppDatabase.kt
│  │  │  │           │  │     ├─ dao
│  │  │  │           │  │     │  ├─ BuildingCodeDao.kt
│  │  │  │           │  │     │  ├─ MaterialDao.kt
│  │  │  │           │  │     │  ├─ SectionDaos.kt
│  │  │  │           │  │     │  └─ StructuralDataDao.kt
│  │  │  │           │  │     ├─ entity
│  │  │  │           │  │     │  ├─ BuildingCodeEntities.kt
│  │  │  │           │  │     │  ├─ MaterialRoomEntity.kt
│  │  │  │           │  │     │  ├─ SectionRoomEntities.kt
│  │  │  │           │  │     │  └─ StructuralDataEntities.kt
│  │  │  │           │  │     ├─ mapper
│  │  │  │           │  │     │  ├─ SectionMappers.kt
│  │  │  │           │  │     │  └─ StructuralMappers.kt
│  │  │  │           │  │     ├─ MaterialSeeder.kt
│  │  │  │           │  │     ├─ Migrations.kt
│  │  │  │           │  │     ├─ StandardTypeConverters.kt
│  │  │  │           │  │     └─ StructuralDataSeeder.kt
│  │  │  │           │  └─ repository
│  │  │  │           │     └─ StructuralCodeRepository.kt
│  │  │  │           ├─ domain
│  │  │  │           │  ├─ beam
│  │  │  │           │  │  ├─ BeamModels.kt
│  │  │  │           │  │  ├─ MaterialRepository.kt
│  │  │  │           │  │  └─ SectionModels.kt
│  │  │  │           │  ├─ calculation
│  │  │  │           │  │  ├─ CalculationLifecycleService.kt
│  │  │  │           │  │  ├─ CalculationMetadata.kt
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
│  │  │  │           │  │  ├─ AnalysisModels.kt
│  │  │  │           │  │  ├─ AxialDesignModels.kt
│  │  │  │           │  │  ├─ BoundaryRestraint.kt
│  │  │  │           │  │  ├─ BracingLogic.kt
│  │  │  │           │  │  ├─ BuildingCode.kt
│  │  │  │           │  │  ├─ CapacityCalculator.kt
│  │  │  │           │  │  ├─ CapacityEngine.kt
│  │  │  │           │  │  ├─ CombinationType.kt
│  │  │  │           │  │  ├─ DecisionCaptureService.kt
│  │  │  │           │  │  ├─ DesignInterpretationService.kt
│  │  │  │           │  │  ├─ InteractionModels.kt
│  │  │  │           │  │  ├─ LimitStateService.kt
│  │  │  │           │  │  ├─ LoadCaseModels.kt
│  │  │  │           │  │  ├─ LoadCombinationEngine.kt
│  │  │  │           │  │  ├─ LoadModels.kt
│  │  │  │           │  │  ├─ LoadResolutionService.kt
│  │  │  │           │  │  ├─ MaterialDesignResolver.kt
│  │  │  │           │  │  ├─ MaterialDesignStrategy.kt
│  │  │  │           │  │  ├─ nds
│  │  │  │           │  │  │  └─ NdsWoodCapacityCalculator.kt
│  │  │  │           │  │  ├─ ServiceabilityEvaluationService.kt
│  │  │  │           │  │  ├─ ServiceabilityInterpretationService.kt
│  │  │  │           │  │  ├─ ServiceabilityLimits.kt
│  │  │  │           │  │  ├─ ServiceabilityModels.kt
│  │  │  │           │  │  ├─ StationDemand.kt
│  │  │  │           │  │  ├─ SteelStabilityModels.kt
│  │  │  │           │  │  ├─ StrengthDesignService.kt
│  │  │  │           │  │  ├─ StructuralDemand.kt
│  │  │  │           │  │  ├─ StructuralReferenceKey.kt
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
│  │  │  │           └─ util
│  │  │  │              └─ serialization
│  │  │  │                 └─ LocalDateTimeSerializer.kt
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
├─ assets
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
│  │     │              │     ├─ dao
│  │     │              │     │  ├─ CalculationDao.kt
│  │     │              │     │  └─ ProjectDao.kt
│  │     │              │     └─ entity
│  │     │              │        ├─ CalculationRoomEntity.kt
│  │     │              │        ├─ catalog
│  │     │              │        │  ├─ AiscSectionRoomEntity.kt
│  │     │              │        │  ├─ CustomSectionRoomEntity.kt
│  │     │              │        │  └─ WoodSectionRoomEntity.kt
│  │     │              │        └─ ProjectRoomEntity.kt
│  │     │              └─ repository
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
│  │     │              │  ├─ aisc
│  │     │              │  ├─ asce7
│  │     │              │  │  ├─ Asce7Versions.kt
│  │     │              │  │  └─ AsceEdition.kt
│  │     │              │  ├─ LoadCategory.kt
│  │     │              │  ├─ LoadCombinationModels.kt
│  │     │              │  └─ RegulatoryEnums.kt
│  │     │              ├─ structural
│  │     │              │  ├─ BracingModels.kt
│  │     │              │  ├─ DesignContextModels.kt
│  │     │              │  ├─ DesignEquationTrace.kt
│  │     │              │  ├─ DesignModels.kt
│  │     │              │  ├─ LimitState.kt
│  │     │              │  ├─ MaterialModels.kt
│  │     │              │  ├─ MaterialType.kt
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
├─ emulator_screen.png
├─ emulator_screen_after.png
├─ emulator_screen_after2.png
├─ emulator_verify.png
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
├─ home_screen.png
├─ README.md
├─ reorg_script.py
├─ screenshot.png
├─ shared
│  ├─ codes
│  ├─ materials
│  ├─ sections
│  └─ structural
├─ temp
├─ test_errors.txt
├─ test_errors_2.txt
├─ test_errors_utf8.txt
├─ test_errors_utf8_2.txt
├─ test_output.txt
├─ test_output_utf8.txt
└─ view.xml

```
```
VectOS
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
│  │  │  └─ 18.json
│  │  └─ com.lz.vectos.persistence.room.AppDatabase
│  │     ├─ 1.json
│  │     ├─ 10.json
│  │     ├─ 12.json
│  │     ├─ 14.json
│  │     ├─ 15.json
│  │     ├─ 16.json
│  │     ├─ 17.json
│  │     ├─ 18.json
│  │     ├─ 2.json
│  │     ├─ 3.json
│  │     ├─ 4.json
│  │     ├─ 5.json
│  │     ├─ 7.json
│  │     └─ 9.json
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
│  │  │  │           │  └─ MainActivity.kt
│  │  │  │           ├─ data
│  │  │  │           │  ├─ export
│  │  │  │           │  │  ├─ CalculationExporter.kt
│  │  │  │           │  │  ├─ CalculationFormatter.kt
│  │  │  │           │  │  └─ ReportingService.kt
│  │  │  │           │  └─ persistence
│  │  │  │           │     ├─ entity
│  │  │  │           │     │  ├─ BeamCalculationEntity.kt
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
│  │  │  │           │        ├─ AppDatabase.kt
│  │  │  │           │        ├─ dao
│  │  │  │           │        │  └─ SectionDaos.kt
│  │  │  │           │        ├─ entity
│  │  │  │           │        │  └─ SectionRoomEntities.kt
│  │  │  │           │        ├─ mapper
│  │  │  │           │        │  ├─ SectionMappers.kt
│  │  │  │           │        │  └─ StructuralMappers.kt
│  │  │  │           │        ├─ MaterialSeeder.kt
│  │  │  │           │        ├─ Migrations.kt
│  │  │  │           │        ├─ StandardTypeConverters.kt
│  │  │  │           │        └─ StructuralDataSeeder.kt
│  │  │  │           ├─ domain
│  │  │  │           │  ├─ beam
│  │  │  │           │  │  └─ BeamModels.kt
│  │  │  │           │  ├─ calculation
│  │  │  │           │  │  ├─ CalculationLifecycleService.kt
│  │  │  │           │  │  ├─ CalculationMetadata.kt
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
│  │  │  │           │  │  ├─ AnalysisModels.kt
│  │  │  │           │  │  ├─ AxialDesignModels.kt
│  │  │  │           │  │  ├─ BoundaryRestraint.kt
│  │  │  │           │  │  ├─ BracingLogic.kt
│  │  │  │           │  │  ├─ CapacityCalculator.kt
│  │  │  │           │  │  ├─ CapacityEngine.kt
│  │  │  │           │  │  ├─ DecisionCaptureService.kt
│  │  │  │           │  │  ├─ DesignInterpretationService.kt
│  │  │  │           │  │  ├─ InteractionModels.kt
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
│  │  │  │           │  │  ├─ StationDemand.kt
│  │  │  │           │  │  ├─ SteelStabilityModels.kt
│  │  │  │           │  │  ├─ StrengthDesignService.kt
│  │  │  │           │  │  ├─ StructuralDemand.kt
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
│  │  │  │           └─ util
│  │  │  │              └─ serialization
│  │  │  │                 └─ LocalDateTimeSerializer.kt
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
├─ assets
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
│  │     │              │     └─ repository
│  │     │              │        └─ RoomMaterialRepository.kt
│  │     │              └─ repository
│  │     │                 └─ BuildingCodeRepository.kt
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
│  │     │              │  │  └─ NdsAdjustmentFactors.kt
│  │     │              │  ├─ RegulatoryEnums.kt
│  │     │              │  └─ StandardReferenceKey.kt
│  │     │              ├─ structural
│  │     │              │  ├─ BracingModels.kt
│  │     │              │  ├─ DesignContextModels.kt
│  │     │              │  ├─ DesignEquationTrace.kt
│  │     │              │  ├─ DesignModels.kt
│  │     │              │  ├─ LimitState.kt
│  │     │              │  ├─ MaterialModels.kt
│  │     │              │  ├─ MaterialType.kt
│  │     │              │  ├─ SectionModels.kt
│  │     │              │  ├─ ServiceabilityModels.kt
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
├─ emulator_screen.png
├─ emulator_screen_after.png
├─ emulator_screen_after2.png
├─ emulator_verify.png
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
├─ home_screen.png
├─ README.md
├─ reorg_script.py
├─ screenshot.png
├─ shared
│  ├─ codes
│  ├─ materials
│  ├─ sections
│  └─ structural
├─ temp
├─ test_errors.txt
├─ test_errors_2.txt
├─ test_errors_utf8.txt
├─ test_errors_utf8_2.txt
├─ test_output.txt
├─ test_output_utf8.txt
└─ view.xml

```