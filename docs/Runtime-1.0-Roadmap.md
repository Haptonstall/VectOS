# VectOS Runtime 1.0 Architecture & Implementation Roadmap

**Status:** Living Architecture Specification

**Purpose**

This document defines the target Runtime architecture for VectOS. It serves as the authoritative implementation guide during development. Any architectural changes should be reflected here before implementation proceeds.

---

# Guiding Principles

The VectOS application is **not** an engineering application.

It is a Runtime platform capable of hosting engineering modules.

Every engineering discipline (Beam, Wind, Seismic, Concrete, Wood, Steel Connections, etc.) exists as an independent Runtime module.

The Runtime must never require modification when a new engineering module is introduced.

---

# Core Architectural Goals

The architecture shall satisfy the following principles.

## 1. Runtime owns execution

The Runtime is responsible for:

* Discovering installed modules
* Loading modules
* Starting modules
* Registering capabilities
* Exposing capabilities to the UI
* Managing module lifecycle

The Runtime is **not** responsible for installation.

---

## 2. Marketplace owns installation

The Marketplace subsystem is responsible for:

* Purchasing modules
* Downloading modules
* Installing modules
* Updating modules
* Removing modules
* Licensing
* Enabling/disabling modules

The Runtime simply consumes the installed module repository.

**This is the selected architecture (Option B).**

```
Marketplace
        │
        ▼
InstalledModuleRepository
        │
        ▼
Runtime
        │
        ▼
Loaded Modules
```

The Runtime must never know *how* a module became installed.

---

## 3. The App knows nothing about engineering modules

The application layer shall never reference:

* Beam
* Wind
* Seismic
* Concrete
* Wood
* Foundation
* Flagpole

or any future engineering module.

The application interacts only with:

* Runtime
* Tool Picker
* Project Library
* Settings
* Marketplace

---

## 4. Modules are peers

Every engineering module follows exactly the same contract.

```
Beam

Wind

Seismic

Concrete

Wood

Retaining Walls

Connection Design
```

None receive special treatment.

---

## 5. Runtime discovers modules

Modules register themselves through Runtime.

The Runtime never contains module-specific logic.

---

# Runtime Layers

```
Application
        │
        ▼
Runtime
        │
 ┌───────────────┐
 │ Module Loader │
 │ Module Registry │
 │ Capability Registry │
 │ Event Bus │
 │ Lifecycle │
 └───────────────┘
        │
        ▼
Engineering Modules
```

---

# Runtime Responsibilities

Runtime owns:

* RuntimeEnvironment
* RuntimeContext
* RuntimeBootstrapper
* RuntimeStartupPipeline
* RuntimeModuleLoader
* RuntimeModuleRegistry
* CapabilityRegistry
* EventBus
* Runtime lifecycle

Runtime does **not** own:

* Installation
* Licensing
* Marketplace
* Purchasing
* Downloads

---

# Marketplace Responsibilities

Marketplace owns:

* Installed modules
* Download state
* Licensing
* Purchases
* Updates
* Enable/Disable
* Installation

Marketplace exposes:

```
InstalledModuleRepository
```

The Runtime consumes this repository.

---

# Installed Module Repository

Current implementation:

* Hardcoded bootstrap list

Future implementation:

* Room
* Google Play Dynamic Features
* Enterprise deployment
* Local package installation

The Runtime must never require modification when the repository implementation changes.

---

# Runtime Primary Abstraction

## Previous Bootstrap Abstraction

```
InstalledModuleManifest

↓

providerClass : String

↓

Reflection

↓

RuntimeModuleProvider
```

This design tightly couples Runtime to Java reflection and assumes all providers are already present on the application classpath.

It is acceptable only as a temporary bootstrap implementation.

---

## Runtime 1.0 Abstraction

Runtime uses a strongly typed installed module model.

```
InstalledModule

moduleId

displayName

version

installState

enabled

source

featureName

entryPoint

signature
```

The ProviderFactory should consume this object rather than a raw class name.

Reflection becomes an implementation detail behind the loading pipeline rather than the Runtime's public abstraction.

This allows future support for:

* Google Play Dynamic Features
* Marketplace downloads
* Enterprise deployment
* Local packages
* Testing harnesses

without redesigning Runtime.

---

# Module Loading Pipeline

```
Marketplace

↓

InstalledModuleRepository

↓

RuntimeStartupPipeline

↓

RuntimeModuleLoader

↓

ProviderFactory

↓

RuntimeModuleProvider

↓

RuntimeModule

↓

Capability Registration

↓

Runtime Ready
```

---

# Tool Discovery

The Tool Picker must be generated entirely from Runtime capabilities.

No engineering module may be referenced directly.

---

# Engineering Modules

Each module shall provide:

```
RuntimeModuleProvider

↓

RuntimeModule

↓

Capabilities

↓

Tools

↓

Screens
```

Nothing else in the application should know the module exists.

---

# Dependency Rules

Application

* Depends on Runtime
* Depends on Marketplace

Application does **not** depend on Beam or any engineering module.

Runtime

* Depends only on Runtime contracts.

Runtime does **not** depend on Beam.

Engineering Modules

* Depend on Runtime contracts.

Engineering Modules do **not** depend on the App.

---

# Runtime 1.0 Completion Checklist

## Phase 1 – Freeze Runtime Architecture

* [x] Review Runtime API
* [x] Review Runtime Boot
* [x] Review Runtime Registry
* [x] Review Runtime Discovery
* [x] Review Runtime Compose
* [x] Freeze Runtime contracts

### Phase 1 Review Notes

Runtime 1.0 Phase 1 review fixes completed:

* [x] Tool Picker entries must be derived from Runtime capabilities, not module descriptors.
* [x] Calculator capabilities must declare Project and Quick Calc mode support.
* [x] Runtime startup must continue when an individual module provider or module install fails.
* [x] RuntimeEnvironment construction must have a valid default startup pipeline and must not require app-only wiring to avoid startup crashes.
* [x] Android module discovery must converge on the InstalledModuleRepository-driven path before the discovery contract is frozen.

---

## Phase 2 – Replace Runtime Primary Abstraction

* [x] Introduce InstalledModule model
* [x] Replace providerClass string abstraction
* [x] Update ProviderFactory
* [x] Move reflection behind RuntimeModuleLoader
* [x] Remove reflection from Runtime public API

---

## Phase 3 – Marketplace Integration

* [ ] Define Marketplace architecture
* [ ] Expand InstalledModuleRepository
* [ ] Add enable/disable support
* [ ] Add installation state
* [ ] Prepare for licensing
* [ ] Stub installer implementation

### Phase 3 Review Notes

Marketplace integration should proceed against the Phase 2 `InstalledModule` model:

* `InstalledModuleRepository` is the Runtime-facing boundary and should be backed by Marketplace-owned state.
* Enable/disable behavior should update installed module metadata rather than unloading modules directly from UI code.
* Licensing should remain Marketplace-owned; Runtime should consume only installed/enabled state and registered capabilities.
* The current hardcoded app repository is a bootstrap repository and should become a replaceable Marketplace adapter.
* The app-side `ModuleInstaller`, `PurchaseManager`, and subscription stubs still represent Marketplace behavior and should converge under the Phase 3 Marketplace architecture.

---

## Phase 4 – Runtime Startup

* [ ] Enumerate installed modules
* [ ] Load enabled modules
* [ ] Register capabilities
* [ ] Continue startup on module failure
* [ ] Publish Runtime Ready event

---

## Phase 5 – Tool System

* [ ] Generate Tool Picker from Runtime
* [ ] Remove remaining Beam references
* [ ] Remove remaining static tool registration
* [ ] Verify capability discovery

---

## Phase 6 – Engineering Module Cleanup

* [ ] Update Beam to Runtime 1.0 contracts
* [ ] Remove obsolete providers
* [ ] Remove obsolete adapters
* [ ] Verify module isolation
* [ ] Verify no App dependencies remain

---

## Phase 7 – Marketplace Readiness

* [ ] Validate Runtime without Beam installed
* [ ] Validate Runtime with Beam installed
* [ ] Validate multiple installed modules
* [ ] Validate enable/disable behavior
* [ ] Validate future Dynamic Feature compatibility

---

# Development Rule

Implementation work shall proceed by completing one checklist item at a time.

Architectural changes shall first be reflected in this document before code is modified.

This document is the authoritative roadmap for completing Runtime 1.0 and establishing the long-term VectOS platform architecture.
