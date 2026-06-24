# VectOS Architecture Validator Design

**Version:** 1.0
**Status:** Draft
**Last Updated:** 2026-06-23

---

# 1. Purpose

This document defines the design of the VectOS Architecture Validator.

The validator enforces architectural rules defined in:

* Dependency Rule Charter
* Platform Architecture Specification
* Module SDK Specification
* Engine Contract Specification

It ensures that architecture is enforced in:

* Local development (IDE feedback)
* Build system (CI enforcement)
* Runtime validation (optional sandbox mode)

---

# 2. Core Principle

Architecture is not documented.

Architecture is **validated automatically**.

---

# 3. System Overview

```text id="av1"
Source Code
    ↓
Gradle Module Graph
    ↓
Architecture Graph Builder
    ↓
Rule Engine
    ↓
Violation Report
    ↓
CI / IDE Feedback
```

---

# 4. Validator Responsibilities

The Architecture Validator must:

* Parse module dependencies
* Build a directed dependency graph
* Apply rule set from architecture definition
* Detect violations
* Fail build if violations exist
* Provide human-readable diagnostics

---

# 5. Architecture Graph Model

```kotlin id="av2"
data class ModuleNode(
    val name: String,
    val type: ModuleType,
    val dependencies: List<String>
)
```

Graph:

```text id="av3"
ModuleA → ModuleB → ModuleC
```

---

# 6. Rule System

Rules are declarative.

```kotlin id="av4"
interface ArchitectureRule {

    val id: String

    fun validate(graph: ArchitectureGraph): List<Violation>
}
```

---

# 7. Example Rules

## 7.1 No Module → Engine Dependency

```kotlin id="av5"
class NoModuleToEngineRule : ArchitectureRule {

    override val id = "NO_MODULE_TO_ENGINE"

    override fun validate(graph: ArchitectureGraph): List<Violation> {

        return graph.edges.filter { edge ->
            edge.from.type == MODULE &&
            edge.to.type == ENGINE
        }.map {
            Violation(
                ruleId = id,
                message = "Modules cannot depend on Engines",
                edge = it
            )
        }
    }
}
```

---

## 7.2 No UI → Domain Violation

```text id="av6"
UI → Domain ❌
UI → Application → Domain ✅
```

---

# 8. Rule Registry

```kotlin id="av7"
class RuleRegistry {

    fun loadRules(): List<ArchitectureRule> = listOf(
        NoModuleToEngineRule(),
        NoUiToDomainRule(),
        NoEngineToUiRule()
    )
}
```

---

# 9. Validator Engine

```kotlin id="av8"
class ArchitectureValidator(
    private val ruleRegistry: RuleRegistry
) {

    fun validate(graph: ArchitectureGraph): ValidationResult {

        val violations = ruleRegistry.loadRules()
            .flatMap { it.validate(graph) }

        return ValidationResult(violations)
    }
}
```

---

# 10. Gradle Integration

Validator runs as a Gradle task:

```text id="av9"
./gradlew validateArchitecture
```

Failure behavior:

```text id="av10"
BUILD FAILED: Architecture violations detected
```

---

# 11. CI Integration

Pipeline:

```text id="av11"
1. Build
2. Unit Tests
3. Architecture Validation ← HARD GATE
4. Package
```

---

# 12. IDE Integration (Optional but recommended)

Real-time feedback:

* IntelliJ plugin OR
* Gradle sync hook

Highlights:

* red underline on forbidden imports
* warnings on dependency violations

---

# 13. Output Format

Example violation report:

```text id="av12"
[NO_MODULE_TO_ENGINE]
BeamModule → CppBeamEngine

Location:
modules/beam/src/BeamModule.kt:42

Fix:
Move logic into BeamAnalysisEngine contract layer
```

---

# 14. Module Classification System

Each module must be classified:

```kotlin id="av13"
enum class ModuleType {
    UI,
    MODULE,
    ENGINE,
    CONTRACT,
    INFRASTRUCTURE,
    APPLICATION
}
```

---

# 15. Graph Builder

Built from Gradle:

```text id="av14"
settings.gradle → includes modules
build.gradle → dependency graph
```

Converted into:

```text id="av15"
ArchitectureGraph
```

---

# 16. Enforcement Levels

## Level 1 — Warning

* IDE only

## Level 2 — CI Failure

* Build blocked

## Level 3 — Hard Runtime Guard (optional)

* sandbox enforcement

---

# 17. AI Safety Integration

AI-generated modules MUST:

* pass architecture validation
* pass tool schema validation
* pass dependency rules

Otherwise they are rejected.

---

# 18. Extension Points

Future extensions:

* distributed graph validation
* cloud-based architecture enforcement
* marketplace module validation
* AI-assisted rule generation

---

# 19. Summary

The Architecture Validator ensures:

* Architecture is enforceable
* Violations are impossible to ignore
* Modules remain decoupled
* Engines remain replaceable
* AI integration remains safe

It is the enforcement layer that turns VectOS architecture from documentation into a **governed system**.

