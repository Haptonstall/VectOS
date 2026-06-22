# VectOS — Architecture Specification

**Version:** 1.1
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

### Gradle Module Inventory

```
VectOS/
├── app/                    Android application module — entry point, navigation, DI wiring
├── core/ui/                Shared Compose design system — theme, reusable components
├── core/domain/            Shared domain contracts — repository interfaces, core entities
├── core/model/             Pure data models — no Android deps, no logic, no DB
├── core/solver/            Calculation engines — pure Kotlin, no Android deps
├── core/data/              Room persistence — DAOs, entities, seeders, mappers, repositories
├── feature/beam/           Beam calculation module — self-contained feature
├── feature/column/         Column calculation module — planned
└── feature/pole/           Pole calculation module — planned
```

> Note: Gradle module paths use colon notation — `:core:model`, `:core:data`, `:feature:beam`, etc.

### Dependency Graph

Dependencies flow strictly inward. No module may depend on a module further out than itself.

```
app
 ├── depends on: core:ui, core:domain, core:model, core:data, core:solver
 ├── depends on: feature:beam, feature:column, feature:pole
 └── owns: MainActivity, VectosApplication, DatabaseModule (Hilt), navigation

core:ui
 ├── depends on: core:model, core:domain
 └── owns: theme, shared Compose components, cross-feature tool dialogs

feature/beam
 ├── depends on: core:ui, core:domain, core:model, core:solver, core:data
 └── owns: BeamViewModel, beam screens, BeamDatabase, beam solver wiring

core:data
 ├── depends on: core:domain, core:model
 └── owns: AppDatabase, Room entities, DAOs, seeders, mappers, repositories

core:solver
 ├── depends on: core:model
 └── owns: capacity calculators, analysis engines, load combination logic

core:domain
 ├── depends on: core:model
 └── owns: repository interfaces, core domain entities

core:model
 ├── depends on: nothing (pure Kotlin + kotlinx.serialization only)
 └── owns: all data shapes, enums, constants, standard registries
```

**Absolute rules enforced by this graph:**

- `core:model` has zero dependencies on any other project module
- `core:solver` has zero Android dependencies — it is pure Kotlin
- `core:domain` has zero Android dependencies — it is pure Kotlin
- No feature module depends on another feature module
- No feature module depends on `app`
- `core:data` does not depend on any feature module

---

## 4. Module Descriptions and File Ownership

### 4.1 `core:model` — Data Shapes and Standard Registries

**Package root:** `com.lz.model`

Pure data — enums, data classes, sealed classes, and standard reference registries. No Android dependencies, no database access, no business logic, no coroutines.

```
core/model/src/main/java/com/lz/model/
│
├── util/
│   └── UUIDSerializer.kt
│
├── units/
│   ├── UnitModels.kt                   Force, Length, Moment, Pressure, Stress,
│   │                                   ElasticModulus, ForcePerLength, MomentOfInertia,
│   │                                   Area, SectionModulus, TorsionalConstant
│   ├── UnitSystem.kt                   IMPERIAL / METRIC enum
│   └── UnitFormattingService.kt        Formats unit values for display  ← PENDING MOVE
│
├── structural/
│   ├── MaterialType.kt
│   ├── MaterialModels.kt               MaterialGrade sealed class (Steel, Wood, Generic)
│   ├── SectionModels.kt                SectionProfile, SteelProfile, axis properties
│   ├── StructuralModels.kt             StructuralMember, SpanSegment, SupportCondition
│   ├── StructuralNode.kt
│   ├── AnalysisModels.kt               AnalysisResult, StationResult, DeflectionProfile
│   ├── DesignModels.kt                 DesignResult, UtilizationRecord
│   ├── CapacityResults.kt              PointCapacityResult, ServiceabilityResult
│   ├── AxialDesignModels.kt
│   ├── InteractionModels.kt
│   ├── BracingModels.kt                BracingInput sealed class variants
│   ├── SteelStabilityModels.kt         NormalizedBraceState, lateral torsional buckling models
│   ├── BoundaryRestraint.kt
│   ├── ConstraintType.kt
│   ├── DegreeOfFreedom.kt
│   ├── DofConstraint.kt
│   ├── NodeBoundaryCondition.kt
│   ├── DesignContextModels.kt          ProjectDesignContext, DesignMethodology
│   ├── DesignEquationTrace.kt
│   ├── LimitState.kt
│   ├── LoadCase.kt
│   ├── LoadModels.kt
│   ├── ServiceabilityModels.kt
│   ├── StationDemand.kt
│   └── StructuralDemand.kt
│
├── regulatory/
│   ├── LoadCategory.kt
│   ├── LoadCombinationModels.kt
│   ├── RegulatoryEnums.kt              PrimaryBuildingCode, AiscEdition, etc.
│   ├── StandardReferenceKey.kt
│   ├── aci318/
│   │   └── Aci318Versions.kt
│   ├── aisc/
│   │   ├── AiscDesignFactors.kt
│   │   └── AiscDesignFactorRegistry.kt
│   ├── asce7/
│   │   └── Asce7Versions.kt
│   ├── codes/
│   │   ├── BuildingCode.kt
│   │   ├── CodeReferenceKey.kt
│   │   ├── ServiceabilityCriterion.kt
│   │   ├── Standard.kt
│   │   └── StandardEdition.kt
│   ├── loads/
│   │   ├── CombinationType.kt
│   │   └── DesignFactors.kt
│   └── nds/
│       ├── NdsAdjustmentFactors.kt
│       └── NdsVersions.kt
│
└── presentation/
    └── ServiceabilityLimits.kt
```

---

### 4.2 `core:domain` — Repository Interfaces and Domain Entities

**Package root:** `com.lz.domain`

Pure Kotlin. Defines the contracts that `core:data` and feature modules implement. No Android dependencies.

```
core/domain/src/main/java/com/lz/domain/
│
├── project/
│   └── Project.kt                      Root domain entity — ProjectSettings,
│                                       GeographicCoordinates, SeismicHazardData
│
├── calculation/
│   └── CalculationMetadata.kt          Shared calculation summary (id, name, timestamp)
│
├── material/
│   └── MaterialRepository.kt           Interface for material property lookup
│
├── repository/
│   ├── ProjectRepository.kt            CRUD for Project
│   ├── CalculationRepository.kt        Metadata read/write across all calculation types
│   └── SettingsRepository.kt           UnitSystem and DesignMethodology preferences
│
└── structural/
    ├── BoundaryConditionDefinition.kt
    ├── BoundaryConditionPreset.kt
    ├── BoundaryConditionValidator.kt
    ├── BoundaryConditions.kt
    ├── DeflectionLimits.kt
    └── ValidationResult.kt
```

---

### 4.3 `core:solver` — Calculation Engines

**Package root:** `com.lz.solver`

Pure Kotlin. No Android dependencies. All calculation logic lives here or in `feature/` modules that extend it.

```
core/solver/src/main/java/com/lz/solver/
│
├── analysis/
│   ├── AnalysisConfig.kt
│   ├── LimitStateService.kt
│   ├── LoadResolutionService.kt
│   ├── MemberAnalysisSolver.kt         Generic FEM analysis engine
│   └── StructuralSolver.kt
│
├── bracing/
│   ├── BracingLogic.kt                 Generic Cb bracing calculations
│   └── StabilityFactorCalculator.kt
│
├── capacity/
│   ├── CapacityCalculator.kt
│   ├── CapacityEngine.kt
│   └── StrengthDesignService.kt
│
├── envelope/
│   ├── DemandEnvelopeResolver.kt
│   ├── DesignInterpretationService.kt
│   ├── ServiceabilityEvaluationService.kt
│   └── ServiceabilityInterpretationService.kt
│
├── material/
│   ├── AiscCbCalculator.kt             AISC 360 steel-specific Cb calculation
│   ├── AiscSteelCapacityCalculator.kt
│   ├── MaterialDesignResolver.kt
│   ├── NdsClCalculator.kt
│   ├── NdsWoodCapacityCalculator.kt
│   └── WoodPropertyService.kt
│
└── regulatory/
    ├── LoadCombinationEngine.kt
    └── RegulatoryRegistry.kt
```

---

### 4.4 `core:data` — Room Persistence

**Package root:** `com.lz.data`

Owns `AppDatabase`, all shared Room entities, DAOs, seeders, mappers, and repository implementations. Must not depend on any feature module.

```
core/data/src/main/java/com/lz/data/
│
├── persistence/
│   └── room/
│       ├── AppDatabase.kt
│       ├── Migrations.kt
│       ├── StandardTypeConverters.kt
│       ├── dao/
│       │   ├── ProjectDao.kt
│       │   ├── CalculationDao.kt
│       │   ├── CodeRegistryDao.kt
│       │   ├── LoadCombinationDao.kt
│       │   ├── MaterialDao.kt
│       │   ├── SectionDaos.kt
│       │   ├── BuildingCodeDao.kt
│       │   ├── catalog/
│       │   │   ├── AiscSectionDao.kt
│       │   │   └── WoodSectionDao.kt
│       │   └── project/
│       │       └── CustomSectionDao.kt
│       ├── entity/
│       │   ├── ProjectRoomEntity.kt
│       │   ├── CalculationRoomEntity.kt
│       │   ├── BuildingCodeEntities.kt
│       │   ├── CodeRegistryEntities.kt
│       │   ├── LoadCombinationEntities.kt
│       │   ├── MaterialRoomEntity.kt
│       │   ├── SectionRoomEntities.kt
│       │   ├── catalog/
│       │   │   ├── AiscSectionRoomEntity.kt
│       │   │   └── WoodSectionRoomEntity.kt
│       │   └── project/
│       │       └── CustomSectionRoomEntity.kt
│       └── mapper/
│           ├── ProjectPersistenceMapper.kt     Project ↔ ProjectRoomEntity
│           ├── CalculationMetadataMapper.kt    CalculationRoomEntity → CalculationMetadata
│           ├── SectionMappers.kt
│           └── StructuralMappers.kt
│
└── repository/
    ├── BuildingCodeRepository.kt       IStructuralCodeRepository + StructuralCodeRepositoryImpl
    ├── CalculationWriter.kt            Interface for core metadata writes
    ├── RoomCalculationWriter.kt        Implements CalculationWriter via CalculationDao
    ├── RoomProjectRepository.kt        Implements ProjectRepository
    ├── DataStoreSettingsRepository.kt  Implements SettingsRepository via DataStore
    ├── AiscSectionRepository.kt        Interface for AISC section lookup
    ├── RoomAiscSectionRepository.kt    Implements AiscSectionRepository
    ├── NdsSectionRepository.kt         NDS section lookup from JSON asset
    └── RoomMaterialRepository.kt       Material property lookup
```

---

### 4.5 `core:ui` — Shared Compose Components

**Package root:** `com.lz.ui`

Compose UI components shared across the app and feature modules. Depends on `core:model` and `core:domain` only.

```
core/ui/src/main/java/com/lz/ui/
│
├── boundary/
│   ├── BoundaryConditionPicker.kt
│   ├── BoundaryConditionPickerConfig.kt
│   ├── BoundaryConditionVisualizer.kt
│   ├── BoundaryOptionItem.kt
│   ├── BoundaryPresetOption.kt
│   ├── ConstraintTypeDropdown.kt
│   ├── DofConstraintEditor.kt
│   ├── DofEditorConfig.kt
│   └── SpringConstraintEditor.kt
│
│   ← PENDING MOVE from app/src/main/java/com/lz/ui/
├── AnalysisChart.kt
├── SectionPicker.kt
└── UtilizationHeatMap.kt
```

---

### 4.6 `app` — Entry Point and Navigation

**Package root:** `com.lz.vectos`

Owns the Android entry point, Hilt wiring, navigation host, and app-level ViewModels. App-specific domain services that orchestrate across feature modules also live here.

```
app/src/main/java/com/lz/vectos/
│
├── app/
│   ├── VectosApplication.kt            @HiltAndroidApp entry point, seeds DB on onCreate
│   └── MainActivity.kt                 Single activity, NavHost host
│
├── di/
│   └── DatabaseModule.kt               Provides AppDatabase, repositories via Hilt
│
├── presentation/
│   ├── ProjectViewModel.kt             @HiltViewModel — project CRUD, active project state
│   ├── SettingsViewModel.kt            @HiltViewModel — app settings
│   ├── BeamDisplayModel.kt
│   └── CalculationContext.kt
│
├── domain/
│   ├── calculation/
│   │   ├── CalculationLifecycleService.kt
│   │   ├── EngineeringCalculation.kt
│   │   └── ProjectCalculationRegistry.kt
│   ├── provenance/
│   │   ├── CalculationProvenanceService.kt
│   │   └── ProvenanceModels.kt
│   ├── versioning/
│   │   ├── CalculationVersioningService.kt
│   │   └── VersioningModels.kt
│   └── structural/
│       └── DecisionCaptureService.kt
│
├── data/
│   └── export/
│       ├── CalculationExporter.kt
│       ├── CalculationFormatter.kt
│       └── ReportingService.kt
│
├── ui/
│   ├── HomeScreen.kt
│   ├── SettingsScreen.kt
│   ├── navigation/
│   │   └── NavRoutes.kt
│   ├── project/
│   │   ├── NewProjectScreen.kt
│   │   ├── ProjectLibraryScreen.kt
│   │   └── ProjectSettingsScreen.kt
│   ├── calculator/
│   │   ├── CalculatorDefinition.kt
│   │   ├── CalculatorRegistry.kt
│   │   ├── CalculatorRoute.kt
│   │   ├── BeamCalculatorDefinition.kt
│   │   └── ToolPickerScreen.kt         (currently ToolPickerScreen.kt in tool/)
│   └── tool/
│       ├── AssumptionEditor.kt
│       ├── BracingPickerDialog.kt
│       ├── LoadCasePicker.kt
│       ├── LoadCombinationPicker.kt
│       ├── LoadCombinationViewer.kt
│       ├── LoadEditor.kt
│       ├── RevisionHistory.kt
│       ├── ServiceabilityPickerDialog.kt
│       ├── SpanEditor.kt
│       ├── UnitFormatter.kt
│       └── WoodMaterialPickerDialog.kt
│
└── util/
    └── serialization/
        └── LocalDateTimeSerializer.kt
```

---

### 4.7 `feature/beam` — Beam Calculation Module

**Package root:** `com.lz.beam`

Self-contained beam calculation feature. Owns its own Room database, ViewModel, solver wiring, and UI screens.

```
feature/beam/src/main/java/com/lz/beam/
│
├── model/
│   └── BeamModels.kt                   BeamCalculation, BeamCalculationResults, Assumptions
│
├── domain/
│   └── BeamCalculationRepository.kt    Interface for beam calculation persistence
│
├── data/
│   ├── persistence/
│   │   └── room/
│   │       ├── BeamDatabase.kt
│   │       ├── BeamTypeConverters.kt
│   │       ├── dao/
│   │       │   └── BeamCalculationDao.kt
│   │       └── entity/
│   │           └── BeamCalculationRoomEntity.kt
│   └── repository/
│       ├── BeamPersistenceMapper.kt    BeamCalculation ↔ Room entities
│       └── RoomBeamCalculationRepository.kt
│
├── solver/
│   ├── BeamAnalysisConfig.kt
│   └── BeamAnalysisSolver.kt           Beam-specific analysis wiring over MemberAnalysisSolver
│
├── presentation/
│   └── BeamViewModel.kt
│
└── ui/
    ├── BeamCalculatorScreen.kt
    ├── BeamBoundaryConditionConfig.kt
    ├── BeamDiagram.kt
    └── StructuralDrawingUtils.kt
```

---

## 5. Database Architecture

### Two Databases, One File

VectOS uses two Room `@Database` classes that share a single SQLite file (`vectos.db`).

| Database | Module | Tables | Purpose |
|---|---|---|---|
| `AppDatabase` | `core:data` | projects, calculations, building_codes, standards, aisc_sections, wood_sections, custom_sections, materials, load_combinations, serviceability_criteria | All shared persistent data |
| `BeamDatabase` | `feature/beam` | beam_calculations | Beam calculation payloads |

### Cross-Database Atomicity

When a beam calculation is saved, two writes must occur in order:

1. `CalculationWriter.writeMetadata()` — writes core metadata to `AppDatabase`
2. `BeamCalculationDao.insert()` — writes payload to `BeamDatabase`

If step 2 fails, the metadata record is orphaned but non-destructive. The UI queries by joined result so incomplete records are not surfaced.

### Seeding Strategy

Reference data is seeded at application start via idempotency-checked seeders running on an IO coroutine in `VectosApplication.onCreate()`:

```
1. BuildingCodeSeeder     — building codes and standard relationships
2. StructuralDataSeeder   — additional structural reference data
3. AiscSectionSeeder      — AISC v15.0 shapes from assets TXT file
4. MaterialSeeder         — reference material grades
```

---

## 6. Dependency Injection

VectOS uses Hilt throughout. The injection graph is:

```
VectosApplication (@HiltAndroidApp)
└── DatabaseModule (@InstallIn SingletonComponent)
    ├── provides AppDatabase (singleton)
    ├── provides ProjectRepository → RoomProjectRepository
    └── provides SettingsRepository → DataStoreSettingsRepository
```

ViewModels are annotated with `@HiltViewModel` and injected with `hiltViewModel()` at the composable call site.

---

## 7. Calculation Traceability Model

Every calculation result carries a complete provenance record:

- **Metadata** — id, project, timestamp, engineer, title (`CalculationRoomEntity`)
- **Inputs** — member geometry, loads, section, material (serialized to JSON in feature entity)
- **Assumptions** — design methodology, code edition, bracing assumptions (serialized)
- **Results** — governing load combination, governing limit state, utilization ratios (serialized)
- **Design factors** — phi or omega values with their code citations (`DesignFactor.citation`)
- **Equation traces** — step-by-step intermediate values (`DesignEquationTrace`)
- **Revision history** — prior versions if modified after initial save

---

## 8. Pending Work

### Immediate (before next feature module)

| Item | Action |
|---|---|
| `app/src/main/java/com/lz/ui/` — `AnalysisChart.kt`, `SectionPicker.kt`, `UtilizationHeatMap.kt` | Move to `core/ui/src/main/java/com/lz/ui/` |
| `app/.../domain/units/UnitFormattingService.kt` | Move to `core/model/src/main/java/com/lz/model/units/` |
| `app/src/main/java/com/lz/vectos/VectosApplication.kt` (stub) | Delete — real entry point is `app/src/main/java/com/lz/vectos/app/VectosApplication.kt`. Update `AndroidManifest.xml` `android:name` to `.app.VectosApplication` |
| `app/.../data/persistence/entity/CalculationEntity.kt` and `ProjectEntity.kt` | Delete — superseded by `core:data` entities |
| `app/.../data/persistence/room/` seeders (`AiscSectionSeeder`, `MaterialSeeder`, `StructuralDataSeeder`) | Delete — superseded by seeders in `core:data` |
| `.continue/rules/LoadCase.kt` | Delete — stray AI context file, not real source |
| `fallbackToDestructiveMigration()` in `AppDatabase` | Remove before production release |

### Needs evaluation before moving or deleting

| Item | Notes |
|---|---|
| `app/domain/` cluster — `CalculationLifecycleService`, `EngineeringCalculation`, `ProjectCalculationRegistry`, `CalculationProvenanceService`, `ProvenanceModels`, `CalculationVersioningService`, `VersioningModels`, `DecisionCaptureService` | These are in `com.lz.vectos.domain.*` and reference each other. Some reference `com.lz.vectos.domain.structural.LoadCase` which no longer exists in source. Determine which are actively called by ViewModels before deciding on delete vs. port to `core:domain`. |
| `app/data/export/` — `CalculationExporter`, `CalculationFormatter`, `ReportingService` | No callers found in current codebase. Confirm whether these are intended future work or dead code before deciding to keep or delete. |
