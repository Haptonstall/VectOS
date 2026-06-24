# VectOS Dependency Rule Charter

**Version:** 1.0
**Status:** Active
**Last Updated:** 2026-06-23

---

# Purpose

This document defines the mandatory dependency rules governing all VectOS development.

These rules exist to preserve:

* Platform independence
* Module isolation
* Engine replaceability
* AI compatibility
* Long-term maintainability
* Marketplace extensibility

All contributors shall follow these rules.

Architectural exceptions require an approved ADR.

---

# Core Dependency Principle

Dependencies shall always point toward abstraction.

```text
UI
↓
Application
↓
Modules
↓
Contracts

Engines
↑
Contracts

Infrastructure
↑
Contracts
```

No dependency shall point outward toward implementation details.

---

# Rule 1 - Modules Depend Only on Contracts

Modules shall depend only on contract definitions.

## Allowed

```text
Beam Module
↓
BeamAnalysisContract
```

## Forbidden

```text
Beam Module
↓
KotlinBeamEngine
```

```text
Beam Module
↓
CppBeamEngine
```

---

# Rule 2 - Contracts Must Remain Stable

Contracts define platform capabilities.

Contracts shall expose:

* Domain models
* Requests
* Responses
* Events

Contracts shall not expose:

* UI concepts
* Database models
* Framework types
* Engine implementations

---

# Rule 3 - UI Layers Are Consumers Only

UI layers shall:

* Display data
* Collect user input
* Trigger workflows

UI layers shall not:

* Execute engineering calculations
* Perform design checks
* Implement business rules

---

# Rule 4 - Engines Are Replaceable

Every engine implementation shall be replaceable without modifying:

* UI
* Modules
* Workflows
* AI tools

Engine replacement must be achievable through dependency injection or service registration.

---

# Rule 5 - Core Code Must Be Platform Independent

Core platform code shall not depend on:

* Android SDK
* Compose UI
* Browser APIs
* Desktop Frameworks

Platform-specific code belongs in adapters.

---

# Rule 6 - AI Is A First-Class Consumer

Every engineering capability shall be callable without a user interface.

Capabilities shall be exposed through tool contracts.

Example:

```kotlin
interface Tool {
    fun execute(
        request: ToolRequest
    ): ToolResult
}
```

---

# Rule 7 - Repository Pattern Required

Storage implementations shall be hidden behind contracts.

## Allowed

```text
Module
↓
MaterialRepository
```

```text
MaterialRepository
↑
SQLiteRepository
```

```text
MaterialRepository
↑
PostgresRepository
```

## Forbidden

```text
Module
↓
SQLite
```

---

# Rule 8 - Marketplace Modules Must Be Isolated

Modules may communicate through:

* Contracts
* Events
* Tool APIs

Modules shall not access:

* Internal classes
* Internal storage
* Internal implementation details

of other modules.

---

# Rule 9 - Service Registry Required

All engine implementations shall be resolved through a registry.

Example:

```kotlin
val engine =
    registry.get<BeamAnalysisEngine>()
```

Modules shall not instantiate engine implementations directly.

---

# Rule 10 - Architectural Violations Require ADR Approval

Any exception to this charter requires:

1. ADR creation
2. Technical review
3. Approval

No exceptions.
