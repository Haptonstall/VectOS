# Multi-Module Architecture Rules

This document defines the architectural rules and conventions for the VectOS project. Continue must follow these rules when generating, reviewing, or refactoring code.

## 1. Module Architecture Overview & Module Heirarchy

The application follows a modularized feature-by-layer Android/Kotlin application following Clean Architecture principles:
1. ':app' → Application entry point, DI composition, navigation
feature/ → Feature modules (beam, column, pole, etc.)
├─ beam → Beam calculation feature
├─ column → Column calculation feature
└─ pole → Pole calculation feature
core/ → Core capabilities
├─ ui → Reusable UI components, themes, base classes
├─ domain → Business logic, use cases, repository interfaces
├─ data → Data layer implementations (Room, Retrofit, DataStore)
├─ model → Pure data models, regulatory enums, DTOs
└─ solver → Engineering solvers (load combinations, capacity)
shared/ → Cross-feature domain models
├─ codes → Building codes (ACI, AISC, NDS, ASCE)
├─ materials → Material property models
├─ sections → Section property models
└─ structural → Shared structural analysis models
docs/ → Documentation only

## 2. Dependency Rules

### 2.1 Strict Layer Boundaries Within Modules and Layer Dependency Direction
-Every feature module (`:feature:beam`, `:feature:column`, etc.) MUST strictly segregate code into three independent packages. No cross-layer imports are allowed:
- **UI** → **Domain** → **Data** (dependencies point inward)
- No layer may depend on an outer layer (e.g., Domain cannot depend on UI)
- Data layer implements interfaces defined in Domain

### 2.2 Module Dependency Rules
| Module | Can Depend On | Cannot Depend On |
|--------|---------------|------------------|
| `app` | `feature/*`, `core/ui`, `core/domain`, `core/data`, `core/model`, `shared/*` | Anything else |
| `feature/*` | `core/ui`, `core/domain`, `core/model`, `shared/*`, `core/solver` | `app`, other `feature/*`, `core/data` (use repository interfaces only) |
| `core/ui` | `core/domain`, `core/model` | `core/data`, `feature/*`, `app` |
| `core/domain` | `core/model`, `shared/*` | Android SDK (except java/util), `core/data`, `feature/*` |
| `core/data` | `core/domain`, `core/model`, `shared/*` | `feature/*`, `core/ui` |
| `core/model` | Nothing (pure Kotlin) | Any Android or other module |
| `core/solver` | `core/model`, `shared/*` | Android, `core/domain` (may use interfaces) |
| `shared/*` | `core/model` | Any other module |

### 2.3 Package Naming Conventions
- Base package: `com.lz.vectos`
- Module-specific subpackages follow the module path
    - `com.lz.vectos.feature.beam`
    - `com.lz.vectos.core.domain`
    - `com.lz.vectos.core.data.persistence.room`
- Inside each module, use standard Clean Architecture subpackages:
    - `di` – Dependency injection modules
    - `domain` – Use cases, repository interfaces, business models
    - `data` – Repository implementations, mappers, data sources
    - `presentation` – ViewModels, UI state, composables (in `core/ui` or `feature`)
    - `util` – Module-specific utilities

## 4. Code Generation Rules

When Continue generates code, it MUST adhere to these rules:

### 4.1 Repository Pattern
  - Always define an interface in `core/domain` or `feature/.../domain`
  - Implementations go in `core/data` or `feature/.../data`
  - Name interfaces: `XyzRepository`
  - Name implementations: `RoomXyzRepository`, `MemoryXyzRepository`, `RetrofitXyzRepository`

    ```kotlin
    // Good
    interface BeamCalculationRepository {
        suspend fun save(calculation: BeamCalculation)
    }
    
    class RoomBeamCalculationRepository(
        private val dao: BeamCalculationDao
    ) : BeamCalculationRepository
    ## Dependency Injection Rules
    - All dependencies must be scoped and provided via Hilt/Koin modules located inside a localized `di` package inside each respective module.
    
    ## Agent Code Generation and Refactoring Constraints
    - BEFORE writing or updating any file, explicitly state which module (`:app`, `:feature:beam`, etc.) and layer (`data`, `domain`, `ui`) the target file belongs to.
    - If creating a calculation engine for concrete or steel, it MUST be placed inside the `domain/model/` or `domain/usecase/` path of the respective feature module.
    - Reject any user requests or autocomplete suggestions that attempt to add a feature-specific UI component into the core `:app` module.

### 4.2 Use Cases / Interactors
  - Encapsulate business logic in use case classes (optional but recommended)
  - Name: GetXyzUseCase, CalculateXyzUseCase
  - Live in core/domain or feature/.../domain

### 4.3 ViewModels
  - Live in presentation package of feature module or core/ui for shared UI
  - Do not directly reference Android framework classes (avoid Context, Activity). Use AndroidViewModel only when absolutely necessary.
  - Expose StateFlow or Compose State

### 4.4 Dependency Injection
  - Use Dagger Hilt modules in each module's di package
  - Module names: NetworkModule, DatabaseModule, RepositoryModule
  - Use @Module, @InstallIn with appropriate components

### 4.5 Data Persistence
  - Room entities go in core/data/persistence/room/entity
  - DAOs go in core/data/persistence/room/dao
  - Use type converters in StandardTypeConverters
  - Database class in core/data/persistence/room/AppDatabase

### 4.6 Concurrency
  - Use suspend functions for Room and network operations
  - Use Dispatchers.IO for database/network in repository implementations
  - ViewModel uses viewModelScope

### 4.7 Unit Testing
  - Put tests in src/test (unit) or src/androidTest (instrumented)
  - Domain layer tests should be pure JVM
  - Data layer tests can use Robolectric or Room in-memory

## 5. Naming Conventions
   Type	                            Convention	                            Example
   Repository interface	            XyzRepository	                        BeamRepository
   Repository implementation	    RoomXyzRepository, ApiXyzRepository	    RoomBeamRepository
   Use case	                        VerbNounUseCase	                        CalculateBeamCapacityUseCase
   ViewModel	                    XyzViewModel	                        BeamCalculatorViewModel
   Composable	                    XyzScreen or XyzComponent	            BeamCalculatorScreen
   Entity (Room)	                XyzEntity	                            BeamCalculationEntity
   DTO (network)	                XyzDto	                                AiscSectionDto
   Mapper	                        XyzMapper (object)	                    BeamMapper
   DI module	                    XyzModule	                            DatabaseModule

## 6. Prohibited Patterns
    ❌ Direct Room DAO access from ViewModel (use Repository)
    ❌ Android Context in domain or model modules
    ❌ import android.* in core/domain (except androidx.lifecycle only in ViewModel)
    ❌ Circular dependencies between modules
    ❌ Hardcoded strings in UI – use strings.xml
    ❌ Business logic inside UI composables – delegate to ViewModel

## 7. Continue-Specific Workflow Rules
    When Continue is asked to:
        - Refactor a file: Always check if the file belongs in app and suggest moving it to the correct module first.
        - Generate a new feature: Create a new module under feature/ with proper build.gradle.kts dependencies (see template below).
        - Fix a compilation error: Verify module dependencies before suggesting code changes.
        - Add a repository: Create interface in core/domain and implementation in core/data.
        - Add a database table: Create entity in core/data/persistence/room/entity, DAO in core/data/persistence/room/dao, and update AppDatabase and migrations.

### 7.1 New Feature Module Template (feature/xyz/build.gradle.kts)
    plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("dagger.hilt.android.plugin")
    id("kotlin-kapt")
    }
    
    android { /* standard config */ }
    
    dependencies {
    implementation(project(":core:ui"))
    implementation(project(":core:domain"))
    implementation(project(":core:model"))
    implementation(project(":shared:structural"))
    // No direct implementation of :core:data – use domain interfaces
    }

## 8. Enforcement with Continue
    - When writing or reviewing code, Continue MUST flag violations of these rules. If uncertain, ask the user for clarification.
    - This rule file is located at '.continue/rules/'. Continue should read this file on every session and apply its contents as system instructions for code generation and refactoring tasks.
    - This file shall be used in conjunction with @ARCHITECTURE_CONTEXT.md.  
    - If there are any conflicts between these two files, the conflict should be flagged for updating.
    - If there are any conflicts between these two files, this file 'multi-module-rules.md' shall govern.
