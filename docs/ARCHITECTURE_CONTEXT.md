# 🧠 VectOS Baseline Architecture Context

This document is the authoritative architectural contract for VectOS. All future development, refactoring, and extensions must remain aligned with the laws and philosophies defined herein.

---

## 1. Project Identity
*   **Project Name:** VectOS
*   **Domain:** Structural Engineering Analysis & Design Platform
*   **Target Users:** Practicing structural engineers
*   **Core Philosophy:**
    *   **Analysis ≠ Design ≠ Judgment:** These three stages are distinct. Analysis produces raw demands; Design compares them to capacities; Judgment (Human-in-the-loop) determines the final engineering decision.

## 2. Supported Scope (Current)
*   **Structural Scope:** Beams (Simple span active; `StructuralMember` and `SpanGeometry` data structures are multi-span ready).
*   **Materials:** 
    *   **Steel:** Primary material, reference implementation quality.
    *   **Wood:** Secondary material, implemented per NDS.
*   **Analysis Capabilities:** 
    *   Linear elastic analysis.
    *   Support for Point Loads, Uniformly Distributed Loads (UDL), and Axial Loads.
    *   Internal resolution of moment, shear, and deflection envelopes.
    * Combination generation must support:
      * pattern loading,
      * mutually exclusive load cases,
      * directional load expansion,
      * and automatic envelope generation.
*   **Design Capabilities:**
    *   Yielding, Lateral-Torsional Buckling (LTB), Shear, and Axial strength checks.
    *   Flexure-Shear Interaction and Axial-Flexural Interaction hooks.
    *   Utilization-based interpretation (Low, Moderate, High, Exceeds Capacity).
    *   Automated Pass/Fail status based on code-defined limit states.
*   **Reporting & Audit:**
    *   CSV and PDF export functionality.
    *   Equation-level traceability (Traces) for all capacity evaluations.
    *   Calculation History and Provenance (Audit Trail).
    *   Revision management with automated change detection.
    

## 3. Architectural Laws (Non-Negotiable)
*   **Automated Validation:** The system provides real-time Pass/Fail feedback based on the governed utilization ratios of all active limit states.
*   **No UI-Driven Engineering Logic:** Calculators and services must reside in the domain layer. The UI is a pure projection of state and a capturer of events.
*   **No Unit Logic Inside Calculators:** Domain calculators operate exclusively on internal base units (Imperial). All unit conversion and formatting are handled at the perimeter (UI/ViewModel) via `UnitConverter` and `UnitFormattingService`.
*   **No Hardcoded Engineering Metadata:** Building codes, load factors, and serviceability limits must be retrieved from the `StructuralRepository`. Domain models (`BuildingCode`, `Standard`) are "Pure" data holders, and their instantiation is the responsibility of the persistence layer.
*   **Functional Separation:** Analysis (Demand), Design (Capacity), and Interpretation (Status) must remain strictly separate in the code.

## 4. Steel as Reference Implementation
*   Steel design (`SteelDesignStrategy`) per AISC 360 is the reference-quality implementation.
*   Any new material implementation must match or exceed the rigor, limit state granularity, and traceability of the Steel implementation.
*   Equation-level traceability (Symbolic + Substituted equations) is mandatory for transparency and auditability.

## 5. Section & Material Selection Contract
*   **Material-First Flow:** Selection begins with Material Type, followed by Shape Family, and then specific Section Profile.
  * Material-specific behavior must remain encapsulated behind strategy contracts.
  * Cross-material shared abstractions should remain minimal and engineering-agnostic.
*   **Database-Backed Sections:** Properties are retrieved from authoritative sources (Assets/JSON).
*   **Axis-Aware Properties:** The system explicitly distinguishes between Strong Axis and Weak Axis properties based on member orientation.
*   **No Free-Form Geometry Overrides:** Section properties (I, S, Z, etc.) are immutable properties of the selected profile. Manual overrides of individual properties are forbidden to preserve material/section integrity.

## 6. Load & Combination Philosophy
*   **Load Cases vs Combinations:** Loads are authored into specific Load Cases (Dead, Live, etc.). Combinations then aggregate these cases using factors.
  * User-defined combinations may exist only within explicitly designated custom engineering configurations and must be clearly identified as non-standard.
*   **Code-Driven Combinations:** Factors and combinations are derived from building codes (e.g., ASCE 7). They are retrieved dynamically from the `StructuralRepository` rather than being hardcoded in logic.
*   **Recursive Code Inheritance:** The system supports building code inheritance (e.g., FBC inherits from IBC). Merging logic resides within the repository, ensuring the domain layer sees only the final, resolved configuration.
*   **Span-Scoped Loads:** Loads are associated with specific spans within a member.

## 7. UI Ownership Model
Tab-based responsibility is strict to prevent duplicated inputs and fragmented context:

| Tab | Responsibility |
| :--- | :--- |
| **Geometry** | Span definitions + Section selection |
| **Loads** | All load authoring and case management |
| **Analysis** | Envelopes, reactions, and station-by-station demand |
| **Design** | Detailed limit state checks and serviceability evaluation |

*   **Constraint:** Duplicated inputs across tabs are strictly forbidden.

## 8. Known Gaps (Intentional)
The following are accepted states of the current development:
*   **Incomplete AISC database:** Asset files contain subsets of common sections.
*   **Dynamic Code Switching UI:** While the architecture supports it, the UI for switching building codes in an active project and handling the resulting re-validation is still in progress.
*   **Continuous Beam Solver Transition:** The mathematical solver is moving from a single-span focus to a multi-span matrix-based approach.
*   **LRFD/ASD Selector UI Polish:** The visual implementation of switching methodologies is functional but requires aesthetic refinement.

## 9. Coding Agent Rules
*   **Preserve Architecture:** Do not deviate from the layer separation (Domain/UI/Persistence).
*   **Do Not Invent New Patterns:** Follow the established `Strategy` and `Service` patterns.
*   **Ask Before Refactoring:** Seek explicit approval before changing core demand/capacity resolution flows.
*   **Extension over Replacement:** Add capabilities by extending existing interfaces rather than replacing established systems.

## 10. Forward Step Anchors
*   **32:** Project settings, methodology refinement, and global units logic.
*   **33:** Implementation of the Continuous Beam Solver.
*   **34:** Column design runtimeModule.
*   **35:** Frame analysis runtimeModule.

## 11. Edit Protocol
*   New implementation steps should be appended to the history.
*   Changes to **Architectural Laws** require explicit human approval.
*   Deprecated sections must be labeled as `[DEPRECATED]` but not deleted to preserve contextual history.

## 12. Data-Driven Structural Configuration
*   **ID-Based Persistence:** Projects must store only the `shortName` (ID) of building codes and standards. Rehydration is handled via `RoomPersistenceMapper` using the `StructuralRepository`.
*   **Strict Typing for Metadata:** Metadata lookups must use the strictly typed enums: `StandardReferenceKey`, 'CodeReferenceKey`, ServiceabilityLimitType`, and `LoadCategory`.
*   **Fail-Fast Resolution:** Repository lookups must throw explicit exceptions for missing configurations to prevent silent errors in calculations.

---
*Note: This file was initialized at Part 1 of the VectOS Baseline Initialization.*

## 13. Canonical Multi-Module Architecture

VectOS is organized as a modular Android platform using Gradle modules and Dynamic Feature Modules.

The architecture is divided into:

* Core platform modules
* Shared engineering infrastructure modules
* Installable feature modules
* Optional premium engineering modules

The system architecture must preserve strict dependency direction and maintain isolation between engineering domains.

---

## 13.1 Core Platform Modules

These modules are always installed and contain shared infrastructure required by all feature modules.

### `:app`

Application entry point and Android bootstrap layer.

Responsibilities:

* Application startup
* Authentication
* Billing and licensing
* Dynamic feature installation
* Navigation host
* Global dependency injection wiring
* Theme initialization

The `:app` runtimeModule must not contain engineering calculation logic.

---

### `:core:domain`

Pure engineering and business abstractions shared across all modules.

Responsibilities:

* Structural primitives
* Shared interfaces/contracts
* Load definitions
* Material abstractions
* Unit abstractions
* Structural graph/node models
* Solver contracts
* Traceability contracts

Rules:

* No Android imports
* No UI code
* No persistence implementations

---

### `:core:data`

Shared persistence and repository infrastructure.

Responsibilities:

* Repository implementations
* Room persistence
* Serialization
* Data mappers
* Shared asset loading

---

### `:core:ui`

Shared UI infrastructure and reusable Compose components.

Responsibilities:

* Shared design system
* Reusable composables
* Common dialogs
* Shared form controls
* Navigation helpers

---

### `:core:reporting`

Shared reporting and export infrastructure.

Responsibilities:

* PDF generation
* CSV export
* Trace rendering
* Calculation reporting
* Audit formatting

---

### `:core:licensing`

Licensing, subscriptions, entitlement verification, and runtimeModule access control.

Responsibilities:

* Google Play Billing integration
* Entitlement validation
* Subscription state management
* Module licensing contracts

---

### `:core:modules`

Dynamic runtimeModule registry and feature discovery system.

Responsibilities:

* Module registration
* Capability discovery
* Dynamic navigation integration
* Installed feature detection

---

## 13.2 Shared Engineering Infrastructure Modules

These modules provide reusable engineering services used across multiple feature modules.

### `:shared:structural`

Shared structural analysis infrastructure.

Responsibilities:

* Structural member abstractions
* Node connectivity
* Load propagation
* Combination generation
* Shared solver helpers

---

### `:shared:materials`

Material databases and shared material services.

Responsibilities:

* Steel database
* Wood database
* Coldform database
* Material metadata
* Shape repositories

---

### `:shared:codes`

Building code and standards infrastructure.

Responsibilities:

* Code repositories
* Combination definitions
* Design metadata
* Code inheritance resolution

---

## 13.3 Dynamic Feature Modules

Each engineering tool is implemented as an isolated Dynamic Feature Module.

Feature modules:

* may contain UI,
* ViewModels,
* engineering services,
* reports,
* and specialized repositories.

Feature modules must depend only on shared/core abstractions and must not directly depend on other feature modules.

---

### `:feature:beam`

Beam analysis and design runtimeModule.

Responsibilities:

* Beam analysis
* Flexural design
* Shear design
* Torsion design
* Serviceability design
* Beam reporting
* Beam-specific UI

---

### `:feature:column`

Column analysis and design runtimeModule.

Responsibilities:

* Axial design (but not limited to)
* Interaction equations
* Slenderness evaluation
* Stability checks
* Column-specific UI

---

### `:feature:pole`

Pole analysis and design runtimeModule.

Responsibilities:

* Pole geometry
* EPA loading
* ASCE loading
* Wind analysis
* Pole serviceability
* Pole-specific reporting

---

### `:feature:foundation`

Foundation analysis and design runtimeModule.

Responsibilities:

* Spread footing design
* Bearing pressure checks
* Base reactions
* Foundation-specific UI

---

## 13.4 Dependency Rules

The dependency hierarchy is strictly enforced:

```text
Feature Modules
    ↓
Shared Modules
    ↓
Core Modules
```

Rules:

* Feature modules must never depend directly on other feature modules.
* Shared modules must not depend on feature modules.
* Core modules must remain platform-agnostic wherever possible.
* Engineering calculation logic must never exist in `:app`.

---

## 13.5 Dynamic Feature Delivery

Engineering modules may be:

* installed,
* uninstalled,
* licensed,
* or disabled independently.

Licensing state must remain separate from installation state.

The UI/navigation system must dynamically discover installed modules through the runtimeModule registry rather than hardcoded navigation entries.

Unavailable modules should not appear in normal workflow navigation unless explicitly surfaced through a marketplace or upgrade interface.

---

## 13.6 Naming Conventions

Feature modules should use:

```text
:feature:<module_name>
```

Shared infrastructure modules should use:

```text
:shared:<domain_name>
```

Core platform modules should use:

```text
:core:<service_name>
```

Avoid ambiguous package names such as:

* `common`
* `misc`
* `helpers`
* `utils2`

All package names must reflect clear ownership and responsibility.

## 14. Organizational Guardrails
To preserve the baseline architecture and prevent regression:

- The `domain` layer must not import `androidx`, `android.app`, `android.content`, Room, DataStore, or UI-specific packages.
- The `domain` layer must contain no UI state, no Compose code, and no persistence adapter code.
- All repository interfaces should be domain-facing or data-facing, but not both. Prefer `com.lz.vectos.domain.repository` for domain contracts and `com.lz.vectos.data.repository` for implementations.
- Persistence classes (Room DAOs, entities, mappers, seeders) must remain under `data.persistence.room`.
- Compose screens and UI-only components must remain under `ui.*`. Shared composables may live in `ui.common` or the feature package that owns them.
- `MainActivity` should remain orchestration-only: app theme, navigation host, dependency wiring, and startup sequences.
- Input domain models may live under `domain` or `ui.input` depending on intent; avoid a generic `input` package if it mixes domain and UI concerns.
- Feature names should guide directories: `ui.project`, `ui.beam`, `ui.tool`, not broad directories like `ui/widgets` unless truly shared.
- Any new top-level package outside `app/src/main/java/com/lz/vectos/` must be intentionally added as a Gradle runtimeModule and referenced in `settings.gradle.kts`.
- Before deleting or moving files, compare duplicate content carefully and preserve the more complete version.

## 15. Duplicate-path caution
There are currently files outside the canonical app source tree, notably:

- `application/repository/SettingsRepository.kt`
- `domain/structural/ServiceabilityInterpretationService.kt`

These are legacy files outside the runtimeModule structure. They must be merged into `:core:data` and `:shared:structural` respectively, then deleted from the root.

- `application/repository/SettingsRepository.kt` currently has a larger interface surface than the app runtimeModule copy.
- `domain/structural/ServiceabilityInterpretationService.kt` is identical to the app runtimeModule copy.
- `persistence/room/entity` and `ui/beam` at the repository root are empty directories.

Do not remove these files automatically. Confirm whether the root-level files are intended to be migrated into the app runtimeModule, kept as in-progress work, or discarded after a deliberate merge.

## 16. Numerical Solver Governance

* All structural solvers must define:
  * assumptions,
  * convergence criteria,
  * tolerances,
  * and failure conditions explicitly.

* Solvers must fail-fast when:
  * stiffness matrices are singular,
  * instability is detected,
  * convergence cannot be achieved,
  * or boundary conditions are insufficient.

* Numerical tolerances must be centralized and configurable through a SolverConfiguration contract.

* Engineering warnings are distinct from computational failures:
  * computational failures invalidate results,
  * engineering warnings annotate otherwise valid solutions.

* All matrix-based solvers must support deterministic repeatability for identical inputs.

## 17. Calculation Reproducibility

* Every calculation result must retain:
  * application version,
  * design code version,
  * repository dataset version,
  * methodology (ASD/LRFD),
  * and active solver configuration.

* Re-running a historical calculation must be reproducible from stored provenance data.

## 18. Engineering Validation Semantics

The system distinguishes between:

* Errors
  * Prevent calculation execution.
  * Example: missing span length.

* Computational Failures
  * Solver could not produce valid numerical results.

* Engineering Warnings
  * Results are mathematically valid but require engineering review.

* Design Failures
  * Valid calculation exceeds allowable limits.

## 19. Immutable Calculation Snapshots

Issued calculation states must be immutable.

Editing a project after issuance creates a new revision rather than mutating historical records.

## 20. Structural Connectivity Philosophy

* Structural members may optionally participate in a connectivity graph through shared `StructuralNode` references.

* Connectivity does not inherently imply stiffness continuity, moment transfer, or global frame analysis.

* Initial connectivity behavior is limited to:

  * reaction propagation,
  * load path relationships,
  * provenance tracking,
  * and optional downstream load generation.

* Members remain independently solvable unless explicitly participating in a future global analysis model.

* Node-based connectivity is intended to provide a scalable foundation for:

  * load takedown,
  * frame analysis,
  * foundation design,
  * connection design,
  * and multi-member structural systems.
