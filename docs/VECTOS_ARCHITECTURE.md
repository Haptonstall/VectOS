# VectOS — Architecture Specification

**Version:** 1.0  
**Last Updated:** June 2026  
**Platform:** Android (Kotlin / Jetpack Compose)

---

## 1. Application Purpose and Vision

VectOS is a professional structural engineering design platform for Android. It provides licensed structural engineers and engineering technologists with a mobile-native environment for performing code-compliant structural member design calculations, managing project documentation, and producing traceable engineering records.

The application is organized around a **core project management system** and an **expandable suite of calculation modules** delivered as optional subscription add-ons. A user who purchases the base application can manage projects, configure site parameters, and set building code references. They can then subscribe to specific calculation modules — beam design, column design, pole design, and others — that activate within the same application install without requiring a separate app download.

Every calculation produced by VectOS is designed to be fully traceable. Each result records the governing code edition, the design factors applied, the load combination that governed, and the assumptions in effect at the time of calculation. This traceability is a first-class requirement, not an afterthought.

---

## 2. Subscription Module Model

The application separates **core infrastructure** from **calculation features** at the module boundary level. This separation is intentional and structural — it is enforced by the Gradle module graph, not just by convention.

### Core (always installed)

The core application provides:

- Project creation and management
- Site location and geocoordinate entry
- Building code and standard selection (IBC, CBC, and others)
- Seismic and wind hazard data retrieval
- AISC section catalog browser
- NDS lumber section catalog
- Material property library
- Calculation history and project record management
- Report export infrastructure

A user without any paid module can use all of these features. They can configure a project fully but cannot perform structural member capacity calculations.

### Calculation Modules (subscription gated)

Each calculation module is a self-contained Android library module under `feature/`. A module provides:

- Its own Room database for calculation payload storage
- Its own ViewModel and UI screens
- Its own domain contracts (repository interfaces)
- Its own solver logic, or references to shared solver components

The currently defined modules are:

| Module | Description | Status |
|---|---|---|
| `feature/beam` | Multi-span beam design — steel (AISC 360), wood (NDS), aluminum (ADM) | In development |
| `feature/column` | Column and post design — axial + combined loading | Planned |
| `feature/pole` | Embedded pole design — lateral and axial | Planned |

Additional modules (connection design, wall design, foundation design) follow the same pattern when introduced.

### The Subscription Boundary

The subscription check occurs at the UI navigation layer in the `app` module. When a user taps a tool in the Tool Picker, the app checks whether the corresponding module is licensed before navigating to it. The feature modules themselves contain no licensing logic — they assume they are authorized to run if they are invoked. This keeps the feature modules clean and testable in isolation.

---

## 3. Module Structure and Dependency Graph

### Module Inventory

```
VectOS/
├── app/                    Android application module — entry point, navigation, DI wiring
├── ui/                     Shared Compose design system — theme, reusable components, tool dialogs
├── domain/                 Shared domain contracts — repository interfaces, core entities
├── model/                  Pure data models — no Android deps, no logic, no DB
├── solver/                 Calculation engines — pure Kotlin, no Android deps
├── core.data/              Room persistence implementation — DAOs, entities, seeders, mappers
├── feature/beam/           Beam calculation module — self-contained feature
├── feature/column/         Column calculation module — self-contained feature (planned)
├── feature/pole/           Pole calculation module — self-contained feature (planned)
└── shared/                 Reserved — currently empty, available for future cross-feature content
```

### Dependency Graph

Dependencies flow strictly inward. No module may depend on a module further out than itself. The `app` module is the only module that may depend on everything.

```
app
 ├── depends on: ui, domain, model, core.data, solver
 ├── depends on: feature/beam, feature/column, feature/pole
 └── owns: MainActivity, VectosApplication, DatabaseModule (Hilt), navigation

ui
 ├── depends on: model, domain
 └── owns: theme, shared Compose components, cross-feature tool dialogs

feature/beam
 ├── depends on: ui, domain, model, solver, core.data
 └── owns: BeamViewModel, beam screens, BeamDatabase, beam solver wiring

feature/column
 ├── depends on: ui, domain, model, solver, core.data
 └── owns: ColumnViewModel, column screens, ColumnDatabase, column solver wiring

feature/pole
 ├── depends on: ui, domain, model, solver, core.data
 └── owns: PoleViewModel, pole screens, PoleDatabase, pole solver wiring

core.data
 ├── depends on: domain, model
 └── owns: AppDatabase, Room entities, DAOs, seeders, mappers, repositories

solver
 ├── depends on: model
 └── owns: capacity calculators, analysis engines, load combination logic

domain
 ├── depends on: model
 └── owns: repository interfaces, core domain entities

model
 ├── depends on: nothing (pure Kotlin + kotlinx.serialization only)
 └── owns: all data shapes, enums, constants, standard registries
```

**Absolute rules enforced by this graph:**

- `model` has zero dependencies on any other project module
- `solver` has zero Android dependencies — it is pure Kotlin
- `domain` has zero Android dependencies — it is pure Kotlin
- No feature module depends on another feature module
- No feature module depends on `app`
- `core.data` does not depend on any feature module

---

## 4. Module Descriptions and File Ownership

### 4.1 `model` — Data Shapes and Standard Registries

**Package root:** `com.lz.model`

This module is the foundation of the entire project. It contains only pure data — enums, data classes, sealed classes, and standard reference registries. It has no Android dependencies, no database access, no business logic, and no coroutines. Every other module depends on it.

```
model/src/main/java/com/lz/model/
│
├── util/
│   └── UUIDSerializer.kt               Kotlinx serialization for java.util.UUID
│
├── units/
│   ├── UnitModels.kt                   Force, Length, Moment, Pressure, Stress, ElasticModulus,
    │                                   ForcePerLength, MomentOfInertia, Area, SectionModulus, TorsionalConstant
│   └── UnitSystem.kt                   IMPERIAL / METRIC enum
│
├── structural/
│   ├── MaterialType.kt                 STEEL, CONCRETE, WOOD, COLD_FORMED_STEEL,
│   │                                   MASONRY, ALUMINUM
│   ├── MaterialModels.kt               MaterialGrade sealed class (Steel, Wood, Generic)
│   ├── SectionModels.kt                SectionProfile, SteelProfile, axis properties
│   ├── StructuralModels.kt             SpanGeometry, StructuralMember, SupportCondition,
│   │                                   SectionCapacity, CapacityEvaluationStatus
│   ├── BracingModels.kt                BracingInput sealed class (Steel, Aluminum, Wood,
│   │                                   Masonry), BracingMode, DiscreteBracePoint,
│   │                                   UnbracedSegment, BracingResolver, NormalizedBraceState
│   ├── LimitState.kt                   LimitState, DetailedLimitState, DeflectionCriteria
│   ├── DesignModels.kt                 StrengthDesignResult, StrengthCheckResult
│   ├── DesignContextModels.kt          ProjectDesignContext, DesignContextIssue,
│   │                                   IssueSeverity
│   ├── DesignEquationTrace.kt          Traceable calculation step records
│   ├── ServiceabilityModels.kt         ServiceabilityResult
│   ├── AxialDesignModels.kt            Axial capacity and interaction models
│   └── CapacityResults.kt              CodeCheck, PointCapacityResult
│
├── regulatory/
│   ├── RegulatoryEnums.kt              DesignMethodology (ASD / LRFD)
│   ├── LoadCategory.kt                 D, L, S, W, E, Lr, etc.
│   ├── LoadCombinationModels.kt        LoadCombination, LoadCombinationSet,
│   │                                   CombinationSource
│   ├── StandardReferenceKey.kt         Typed keys for standard section citations
│   │
│   ├── codes/
│   │   ├── BuildingCode.kt             BuildingCode domain object
│   │   ├── Standard.kt                 Standard domain object
│   │   ├── StandardEdition.kt          Sealed class: Asce7, Aisc360, Nds, Aci318,
│   │   │                               Unknown
│   │   ├── CodeReferenceKey.kt         Typed keys for building code citations
│   │   └── ServiceabilityCriterion.kt  ServiceabilityLimitType, ServiceabilityCriterion
│   │
│   ├── loads/
│   │   ├── CombinationType.kt          STRENGTH, SERVICEABILITY, STABILITY,
│   │   │                               EXTRAORDINARY
│   │   └── DesignFactors.kt            DesignFactor (value + citation)
│   │
│   ├── asce7/
│   │   └── Asce7Versions.kt            Asce7Edition, RiskCategory,
│   │                                   SeismicDesignParameters,
│   │                                   Asce7SpecificationConstants,
│   │                                   Asce7VersionRegistry
│   │
│   ├── aisc/
│   │   ├── AiscDesignFactors.kt        AiscDesignFactors data class
│   │   └── AiscDesignFactorRegistry.kt Registry: get(edition, methodology)
│   │
│   ├── nds/
│   │   ├── NdsVersions.kt              NdsEdition, ServiceCondition, TemperatureRange,
│   │   │                               LoadDurationClass, LumberSizeClass,
│   │   │                               WetServiceFactors, TemperatureFactors,
│   │   │                               NdsSpecificationConstants, NdsVersionRegistry
│   │   └── NdsAdjustmentFactors.kt     CD, CM, Ct, CL, CF chain — adjusted F' values
│   │
│   └── aci318/
│       └── Aci318Versions.kt           Aci318Edition, ExposureCategory,
│                                       ReinforcementType, LightweightConcreteClass,
│                                       Aci318StrengthReductionFactors,
│                                       Aci318SpecificationConstants,
│                                       Aci318VersionRegistry
```

**What does not belong here:** Any Android import, any Room annotation, any coroutine, any repository, any ViewModel, any calculation logic beyond constant lookup tables.

---

### 4.2 `domain` — Contracts and Core Entities

**Package root:** `com.lz.domain`

This module defines the contracts that the application depends on — repository interfaces and core domain entities that cross module boundaries. It has no Android dependencies and no implementation details. It depends only on `model`.

```
domain/src/main/java/com/lz/domain/
│
├── project/
│   └── Project.kt                      Root project entity — id, name, settings,
│                                       coordinates, seismic data, design context
│
├── calculation/
│   └── CalculationMetadata.kt          Shared metadata for all calculation types —
│                                       id, projectId, type, timestamp, title
│
├── material/
│   └── MaterialRepository.kt           Interface: getMaterialsByType(), getMaterialById(),
│                                       saveMaterials()
│
└── repository/
    ├── ProjectRepository.kt            Interface: getProject(), saveProject(),
    │                                   getAllProjects(), deleteProject()
    ├── CalculationRepository.kt        Interface: getCalculation(), saveCalculation(),
    │                                   getCalculationsForProject(), deleteCalculation()
    └── SettingsRepository.kt           Interface: getSettings(), saveSettings()
```

**What belongs here:** Repository interfaces, core entity definitions that multiple modules share, domain-level enums that are not model-layer constants.

**What does not belong here:** Any Room entity, any DAO, any ViewModel, any Compose, any implementation class, any calculation logic.

---

### 4.3 `solver` — Calculation Engines

**Package root:** `com.lz.solver`

This module contains all structural calculation logic. It is pure Kotlin — no Android dependencies, no database access, no UI. It depends on `model` only. Feature modules call into this module for their calculation work; they do not re-implement physics.

```
solver/src/main/java/com/lz/solver/
│
├── analysis/
│   ├── AnalysisConfig.kt           Generic analysis input contract — member, loads,
│   │                               combinations, section properties (unit-typed)
│   ├── LoadResolutionService.kt    Public API for load envelope resolution —
│   │                               assembles AnalysisConfig, delegates to
│   │                               MemberAnalysisSolver
│   ├── MemberAnalysisSolver.kt     FEM / direct stiffness analysis engine —
│   │                               generic, material-agnostic
│   ├── LimitStateService.kt        Limit state resolution utilities
│   └── StructuralSolver.kt         Core solver interface / orchestration
│
├── material/
│   ├── MaterialDesignResolver.kt   Resolves AiscDesignFactors / NdsAdjustmentFactors
│   ├── AiscSteelCapacityCalculator.kt  AISC 360 flexure, shear, axial, torsion,
│   │                                   LTB, FLB — full chapter F/G/D/E/H
│   ├── NdsWoodCapacityCalculator.kt    NDS ASD/LRFD capacity with adjustment chain
│   ├── AluminumCapacityCalculator.kt   ADM Part I Section F (planned)
│   └── MasonryCapacityCalculator.kt    TMS 402 Chapters 8/9 (planned)
│
├── bracing/
│   └── BracingLogic.kt             Enriches StationDemand with Lb, Cb,
│                                   compressionFlange from UnbracedSegments
│
├── capacity/
│   ├── CapacityEngine.kt           Applies phi/omega factors, computes utilization
│   │                               ratios across all limit states at every station
│   └── RawCapacityResult.kt        Internal handoff type between material calculators
│                                   and CapacityEngine — solver-internal only
│
├── regulatory/
│   ├── DemandEnvelopeResolver.kt    Generates LoadCombinationSet for a given
│   │                               BuildingCode + DesignMethodology
│   └── RegulatoryRegistry.kt       Resolves standard editions from BuildingCode
│
└── envelope/
    └── DemandEnvelopeResolver.kt   Resolves governing demand envelope across all stations
                                    and combinations — produces MemberEnvelopeResult with
                                    strength and serviceability envelopes
```

**What belongs here:** Any class that takes structural inputs and produces structural outputs through mathematical operations. Capacity calculators, analysis solvers, load combination assemblers, stability evaluators.

**What does not belong here:** Any UI, any ViewModel, any Room entity, any repository, any Android import.

---

### 4.4 `core.data` — Persistence Implementation

**Package root:** `com.lz.data`

This module provides the Room database implementation for all shared persistent data. It implements the repository interfaces defined in `domain` and owns the core `AppDatabase`. It depends on `domain` and `model`. Feature modules depend on `core.data` to access shared data (sections, materials, building codes) but `core.data` does not depend on any feature module.

```
core.data/src/main/java/com/lz/data/
│
├── persistence/
│   └── room/
│       ├── AppDatabase.kt              @Database registering all core entities,
│       │                               version 1 baseline
│       ├── Migrations.kt               Migration registry — empty at v1 baseline
│       ├── StandardTypeConverters.kt   Room TypeConverters for enums and UUID
│       │
│       ├── dao/
│       │   ├── catalog/
│       │   │   ├── AiscSectionDao.kt   getAllSections(), getSectionById(),
│       │   │   │                       getSectionsByType(), getCount()
│       │   │   └── WoodSectionDao.kt   getAllSections(), getSectionById(),
│       │   │                           getSectionsByWidth()
│       │   ├── project/
│       │   │   ├── ProjectDao.kt       CRUD for projects
│       │   │   ├── CalculationDao.kt   CRUD for calculation metadata
│       │   │   └── CustomSectionDao.kt CRUD for user-defined sections
│       │   ├── MaterialDao.kt          getMaterialsByType(), getMaterialById(),
│       │   │                           insertAll()
│       │   ├── CodeRegistryDao.kt      getBuildingCodeById(), getAllBuildingCodes(),
│       │   │                           getAllStandards(), insertBuildingCode(),
│       │   │                           insertStandard(), insertCrossRef()
│       │   │                           BuildingCodeWithDetails composite result
│       │   └── LoadCombinationDao.kt   getCombinationSetsBySources(),
│       │                               insertCombinationSet(), insertCombination(),
│       │                               insertLoadFactors()
│       │                               CombinationSetWithDetails composite result
│       │
│       ├── entity/
│       │   ├── catalog/
│       │   │   ├── AiscSectionRoomEntity.kt
│       │   │   └── WoodSectionRoomEntity.kt
│       │   ├── project/
│       │   │   ├── ProjectRoomEntity.kt
│       │   │   ├── CalculationRoomEntity.kt
│       │   │   └── CustomSectionRoomEntity.kt
│       │   ├── CodeRegistryEntities.kt  BuildingCodeEntity, StandardEntity,
│       │   │                            BuildingCodeStandardCrossRef
│       │   ├── LoadCombinationEntities.kt LoadCombinationSetEntity,
│       │   │                              LoadCombinationEntity, LoadFactorEntity
│       │   ├── MaterialRoomEntity.kt
│       │   └── BuildingCodeEntities.kt  DefaultMaterialStandardEntity,
│       │                                ServiceabilityCriterionRoomEntity,
│       │                                DefaultLoadCaseRoomEntity
│       │
│       ├── mapper/
│       │   ├── StructuralMappers.kt    toDomainModel() extensions for building code
│       │   │                           and combination entities
│       │   └── SectionMappers.kt       toDomainModel() extensions for section entities
│       │
│       └── seeder/
│           ├── BuildingCodeSeeder.kt   Seeds IBC 2021, IBC 2024, CBC 2025 and all
│           │                           standard cross-references
│           ├── AiscSectionSeeder.kt    Seeds AISC v15.0 shapes from assets TXT file
│           ├── MaterialSeeder.kt       Seeds reference material grades
│           └── StructuralDataSeeder.kt Seeds any remaining structural reference data
│
└── repository/
    ├── BuildingCodeRepository.kt       IBuildingCodeRepository interface
    ├── BuildingCodeRepositoryImpl.kt   Implements getBuildingCode(), getAllBuildingCodes(),
    │                                   getAllStandards(), getDefaultBuildingCode()
    │                                   using CodeRegistryDao
    └── RoomCalculationWriter.kt        Implements CalculationWriter — writes core
                                        CalculationRoomEntity for feature modules
                                        to call atomically with their own payload save
```

**What belongs here:** All Room entities, DAOs, database class, migrations, type converters, seeders, and repository implementations for shared data. The `CalculationWriter` bridge that feature modules call to write core metadata without depending on `AppDatabase` directly.

**What does not belong here:** Any ViewModel, any Compose, any feature-specific entity (beam calculations live in `feature/beam`), any calculation logic.

---

### 4.5 `ui` — Shared Design System

**Package root:** `com.lz.ui`

This module provides the visual design system and shared Compose components that are used across two or more feature modules or by the app shell. It depends on `model` and `domain`. Feature modules depend on `ui` for shared dialogs and design tokens.

```
ui/src/main/java/com/lz/ui/
│
├── theme/
│   ├── Color.kt                        VectOS color palette and semantic color tokens
│   ├── Theme.kt                        MaterialTheme configuration, VectOSTheme wrapper
│   └── Type.kt                         Typography scale
│
├── components/
│   ├── VectosCard.kt                   Styled card with consistent rounding and shadow
│   ├── VectosButton.kt                 Primary, secondary, destructive button variants
│   ├── SectionBadge.kt                 Compact display for section designation
│   ├── UtilizationBar.kt               Color-coded utilization ratio indicator
│   └── CodeReferenceChip.kt            Inline citation display (e.g. "AISC 360-22 F1")
│
└── tool/
    ├── BracingPickerDialog.kt          Material-aware bracing configuration dialog
    │                                   (Steel/Aluminum/Wood/Masonry/Concrete)
    ├── LoadCasePicker.kt               Load case selection UI
    ├── LoadCombinationPicker.kt        Load combination selection and review UI
    ├── LoadCombinationViewer.kt        Read-only combination display
    ├── LoadEditor.kt                   Load magnitude and type entry
    ├── ServiceabilityPickerDialog.kt   Serviceability limit selection
    ├── SpanEditor.kt                   Span geometry and support condition entry
    ├── AssumptionEditor.kt             Engineering assumption capture and display
    └── WoodMaterialPickerDialog.kt     Wood species and grade selection
```

**What belongs here:** Any Compose component used by more than one feature module, all theme definitions, all shared tool dialogs that feature modules open. The rule of thumb is: if two different feature screens need it, it lives in `ui`.

**What does not belong here:** Any ViewModel, any repository, any Room entity, any feature-specific screen, any navigation logic.

---

### 4.6 `feature/beam` — Beam Design Module

**Package root:** `com.lz.beam`

This is the first and most developed calculation module. It provides multi-span beam design for steel (AISC 360), wood (NDS), and aluminum (ADM). It is entirely self-contained — a user without this module sees no beam-related UI.

```
feature/beam/src/main/java/com/lz/beam/
│
├── data/
│   ├── persistence/
│   │   └── room/
│   │       ├── BeamDatabase.kt         @Database for beam_calculations table,
│   │       │                           version 1, shares vectos.db file with AppDatabase
│   │       ├── BeamTypeConverters.kt   UUID TypeConverter for BeamDatabase
│   │       ├── dao/
│   │       │   └── BeamCalculationDao.kt  insert(), update(), getByCalculationId(),
│   │       │                              deleteByCalculationId(), getAll()
│   │       └── entity/
│   │           └── BeamCalculationRoomEntity.kt  calculationId (FK to core),
│   │                                              memberJson, resultsJson,
│   │                                              assumptionsJson, summary fields
│   └── repository/
│       └── RoomBeamCalculationRepository.kt  Implements BeamCalculationRepository —
│                                              calls CalculationWriter for metadata,
│                                              BeamCalculationDao for payload
│
├── domain/
│   ├── BeamCalculationRepository.kt    Interface: getBeamCalculation(), saveBeamCalculation()
│   ├── BeamCalculation.kt              Full beam calculation domain object
│   └── BeamPersistenceMapper.kt        Maps BeamCalculation ↔ Room entities
│
├── model/
│   └── BeamModels.kt               BeamCalculation, BeamCalculationResults,
│                                   beam-specific domain types
├── solver/
│   ├── BeamAnalysisConfig.kt       Beam-specific analysis config — wraps AnalysisConfig
│   │                               with section, material, methodology, deflection limits
│   └── BeamAnalysisSolver.kt       Beam orchestrator — calls MemberAnalysisSolver,
│                                   runs BracingLogic, CapacityEngine, assembles
│                                   BeamAnalysisResult│
└── ui/
    ├── BeamCalculatorScreen.kt         Main beam calculator screen
    ├── BeamViewModel.kt                @HiltViewModel — owns beam calculation state
    ├── BeamDisplayModel.kt             UI-layer display representation
    ├── AnalysisChart.kt                Moment, shear, deflection diagram rendering
    ├── BeamDiagram.kt                  Member geometry visualization
    ├── SectionPicker.kt                Section selection from AISC/wood catalogs
    ├── SupportConditionPicker.kt       End condition selection UI
    └── UtilizationHeatMap.kt           Color-mapped utilization along member length
```

**What belongs here:** Everything specific to beam calculations that no other module needs. The BeamDatabase, beam-specific entities, beam ViewModel, beam screens, beam solver orchestration.

**What does not belong here:** Any capacity calculator logic (that lives in `solver/material/`), any shared dialog (that lives in `ui/tool/`), any building code data (that lives in `core.data/`).

---

### 4.7 `feature/column` and `feature/pole` (Planned)

These modules follow the identical structural pattern as `feature/beam`. Each will have:

```
feature/column/
└── com/lz/column/
    ├── data/persistence/room/          ColumnDatabase — column_calculations table
    ├── domain/                         ColumnCalculationRepository interface
    ├── solver/                         Column solver orchestration
    └── ui/                             ColumnCalculatorScreen, ColumnViewModel
```

Column design covers axial compression (AISC 360 Chapter E), combined axial + flexure (Chapter H interaction equations), and NDS post/column design (Section 3.7).

Pole design covers embedded pole lateral resistance, moment at grade, and passive pressure distribution per IBC Section 1807.

---

### 4.8 `app` — Application Shell

**Package root:** `com.lz.vectos`

The app module is intentionally thin. Its only responsibilities are application entry point, Hilt dependency injection wiring, and navigation hosting. It contains no business logic, no calculation engines, and no database implementation.

```
app/src/main/java/com/lz/vectos/
│
├── app/
│   ├── MainActivity.kt                 @AndroidEntryPoint — hosts NavHost, calls
│   │                                   hiltViewModel() for each ViewModel, no manual
│   │                                   factory construction
│   └── VectosApplication.kt            @HiltAndroidApp — triggers Hilt, runs seeders
│                                       via applicationScope on IO dispatcher
│
├── di/
│   └── DatabaseModule.kt               @Module @InstallIn(SingletonComponent) —
│                                       provides AppDatabase, BeamDatabase, all DAOs,
│                                       CalculationWriter, repositories
│
├── ui/
│   ├── HomeScreen.kt                   Project list and quick-calc entry point
│   ├── SettingsScreen.kt               App-level settings
│   ├── navigation/
│   │   └── NavRoutes.kt                Screen sealed class, route strings
│   ├── project/
│   │   ├── NewProjectScreen.kt         Project creation and editing
│   │   ├── ProjectLibraryScreen.kt     Calculation list within a project
│   │   └── ProjectSettingsScreen.kt    Building code, methodology, risk category
│   └── calculator/
│       ├── CalculatorDefinition.kt     Interface for tool registration
│       ├── CalculatorRegistry.kt       Maps module IDs to CalculatorDefinition
│       ├── CalculatorRoute.kt          Sealed navigation destinations for calculators
│       ├── BeamCalculatorDefinition.kt Registers beam module with the registry
│       └── ToolPickerScreen.kt         Displays available and locked modules
│
├── presentation/
│   ├── ProjectViewModel.kt             @HiltViewModel — project CRUD, active project state
│   └── SettingsViewModel.kt            @HiltViewModel — app settings
│
├── domain/
│   ├── calculation/
│   │   ├── CalculationLifecycleService.kt  Orchestrates save/load/delete across
│   │   │                                   core metadata + feature payload
│   │   ├── EngineeringCalculation.kt       Union type wrapping any calculation variant
│   │   └── ProjectCalculationRegistry.kt  Maps calculation type strings to handlers
│   ├── provenance/
│   │   ├── CalculationProvenanceService.kt Records design decision audit trail
│   │   └── ProvenanceModels.kt             ProvenanceEntry, DecisionRecord
│   ├── versioning/
│   │   ├── CalculationVersioningService.kt Manages calculation revision history
│   │   └── VersioningModels.kt             VersionSnapshot, RevisionRecord
│   └── units/
│       └── UnitFormattingService.kt        Formats unit values for display
│
├── data/
│   ├── export/
│   │   ├── CalculationExporter.kt          Exports calculation to PDF/text
│   │   ├── CalculationFormatter.kt         Formats calculation results for reports
│   │   └── ReportingService.kt             Orchestrates report generation
│   └── persistence/
│       ├── mapper/
│       │   └── RoomPersistenceMapper.kt    Maps project/calculation entities to domain
│       └── repository/
│           ├── RoomProjectRepository.kt    Implements ProjectRepository
│           ├── RoomCalculationRepository.kt Implements CalculationRepository
│           ├── DataStoreSettingsRepository.kt Implements SettingsRepository via DataStore
│           ├── CompositeSectionRepository.kt Aggregates AISC + NDS section sources
│           ├── RoomAiscSectionRepository.kt  Implements section lookup from Room
│           └── NdsSectionRepository.kt       Implements NDS section lookup from JSON asset
│
└── util/
    └── serialization/
        └── LocalDateTimeSerializer.kt      Kotlinx serialization for LocalDateTime
```

**Files still to migrate out of `app/domain/structural/`:**

These files are currently in `app` but belong in `solver` or `model`. Moving them unblocks `feature/column` and `feature/pole` from depending on any part of `app`.

| Current Location | Target Location | Reason |
|---|---|---|
| `domain/structural/analysis/BeamAnalysisSolver.kt` | `solver/analysis/` | Analysis engine |
| `domain/structural/analysis/BeamAnalysisConfig.kt` | `solver/analysis/` | Analysis config |
| `domain/structural/DemandEnvelopeResolver.kt` | `solver/regulatory/` | Already present |
| `domain/structural/DecisionCaptureService.kt` | `app/domain/` | App-level service |

---

## 5. Database Architecture

### Two Databases, One File

VectOS uses two Room `@Database` classes that share a single SQLite file (`vectos.db`). This pattern allows feature modules to own their schema independently while permitting cross-database operations at the SQLite layer.

| Database | Module | Tables | Purpose |
|---|---|---|---|
| `AppDatabase` | `core.data` | projects, calculations, building_codes, standards, aisc_sections, wood_sections, custom_sections, materials, load_combinations, serviceability_criteria, etc. | All shared persistent data |
| `BeamDatabase` | `feature/beam` | beam_calculations | Beam calculation payloads |
| `ColumnDatabase` | `feature/column` (planned) | column_calculations | Column calculation payloads |

### Cross-Database Atomicity

When a beam calculation is saved, two writes must occur atomically: the `CalculationRoomEntity` in `AppDatabase` and the `BeamCalculationRoomEntity` in `BeamDatabase`. Because Room cannot span transactions across two `@Database` instances, VectOS uses a **metadata-first ordering convention**:

1. `CalculationWriter.writeMetadata()` — writes core metadata to `AppDatabase`
2. `BeamCalculationDao.insert()` — writes payload to `BeamDatabase`

If step 2 fails, the metadata record is orphaned but non-destructive. The project calculation list will not surface incomplete records because the UI queries by joined result. True atomic cross-database writes are available at the SQLite level if needed in the future.

### Seeding Strategy

All reference data (building codes, standards, AISC sections, materials) is seeded at application start via idempotency-checked seeders. Each seeder checks its own record count before writing. Seeders run on an IO coroutine in `VectosApplication.onCreate()`. The order is:

```
1. BuildingCodeSeeder     — building codes and standard relationships
2. StructuralDataSeeder   — additional structural reference data
3. AiscSectionSeeder      — AISC v15.0 shapes from assets TXT file
4. MaterialSeeder         — reference material grades
```

---

## 6. Dependency Injection

VectOS uses Hilt for dependency injection throughout. The injection graph is:

```
VectosApplication (@HiltAndroidApp)
└── DatabaseModule (@InstallIn SingletonComponent)
    ├── provides AppDatabase (singleton)
    ├── provides BeamDatabase (singleton)
    ├── provides all DAOs (from their respective databases)
    ├── provides CalculationWriter (bridges core metadata writes for feature modules)
    └── provides repository implementations
```

ViewModels are annotated with `@HiltViewModel` and injected with `hiltViewModel()` at the composable call site. No manual `ViewModelProvider.Factory` construction exists anywhere in the project.

---

## 7. Calculation Traceability Model

Every calculation result in VectOS carries a complete provenance record. This is a product requirement, not just an implementation detail.

A complete calculation record includes:

- **Metadata** — id, project, timestamp, engineer, title (stored in `CalculationRoomEntity`)
- **Inputs** — member geometry, loads, section, material (serialized to JSON in feature entity)
- **Assumptions** — design methodology, code edition, bracing assumptions (serialized)
- **Results** — governing load combination, governing limit state, utilization ratios (serialized)
- **Design factors** — phi or omega values with their code citations (`DesignFactor.citation`)
- **Equation traces** — step-by-step intermediate values (`DesignEquationTrace`)
- **Revision history** — prior versions of the calculation if modified after initial save

This model ensures that a calculation saved today can be fully reconstructed and reviewed years later, even if code editions or default assumptions have changed.

---

## 8. Known Cleanup Items

These are architectural debts identified during the module migration that should be resolved before the next feature module is started:

| Item | Location | Action |
|---|---|---|
| Duplicate UUIDSerializer | `model/Uuidserializer.kt` | Delete — canonical version is `model/util/UUIDSerializer.kt` |
| Empty `shared/` directories | `shared/codes/`, `shared/materials/`, etc. | Delete — purpose fulfilled by `model` module |
| Empty `domain/repository/` folder | `domain/repository/` (outside src) | Delete — Android Studio artifact |
| `app/domain/structural/` cluster | 25+ files | Migrate to `solver/` and `model/structural/` per table in Section 4.8 |
| `fallbackToDestructiveMigration()` | `AppDatabase.create()` | Remove before production release |
| Schema JSON files in `app/schemas/` | Both schema subfolders | Delete old files — `core.data/schemas/1.json` is the new baseline |
| `BeamViewModel` injecting `ProjectViewModel` | `app/presentation/` | Refactor to shared StateFlow or SavedStateHandle pattern |

