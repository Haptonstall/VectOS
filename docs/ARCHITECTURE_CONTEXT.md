# 🧠 VectOS Baseline Architecture Context

This document is the authoritative architectural contract for VectOS. All future development, refactoring, and extensions must remain aligned with the laws and philosophies defined herein.

---

## 1. Project Identity
*   **Project Name:** VectOS
*   **Domain:** Structural Engineering Analysis & Design Platform
*   **Target Users:** Practicing structural engineers
*   **Core Philosophy:**
    *   **Analysis ≠ Design ≠ Judgment:** These three stages are distinct. Analysis produces raw demands; Design compares them to capacities; Judgment (Human-in-the-loop) determines the final engineering decision.
    *   **Human-in-the-loop is mandatory:** The system provides data and interpretation, but the engineer provides the acknowledgment and final approval.

## 2. Supported Scope (Current)
*   **Structural Scope:** Beams (Simple span active; `StructuralMember` and `SpanGeometry` data structures are multi-span ready).
*   **Materials:** 
    *   **Steel:** Primary material, reference implementation quality.
    *   **Wood:** Secondary material, implemented per NDS.
*   **Analysis Capabilities:** 
    *   Linear elastic analysis.
    *   Support for Point Loads, Uniformly Distributed Loads (UDL), and Axial Loads.
    *   Internal resolution of moment, shear, and deflection envelopes.
*   **Design Capabilities:**
    *   Yielding, Lateral-Torsional Buckling (LTB), Shear, and Axial strength checks.
    *   Flexure-Shear Interaction and Axial-Flexural Interaction hooks.
    *   Utilization-based interpretation (Low, Moderate, High, Exceeds Capacity).
*   **Reporting & Audit:**
    *   CSV and PDF export functionality.
    *   Equation-level traceability (Traces) for all capacity evaluations.
    *   Calculation History and Provenance (Audit Trail).
    *   Revision management with automated change detection.

## 3. Architectural Laws (Non-Negotiable)
*   **No Auto Pass/Fail:** The system shall not autonomously declare a calculation "Finished" or "Safe". Engineering acknowledgment is required.
*   **No UI-Driven Engineering Logic:** Calculators and services must reside in the domain layer. The UI is a pure projection of state and a capturer of events.
*   **No Unit Logic Inside Calculators:** Domain calculators operate exclusively on internal base units (SI: Meters, Newtons, Pascals). All unit conversion and formatting are handled at the perimeter (UI/ViewModel) via `UnitConverter` and `UnitFormattingService`.
*   **Functional Separation:** Analysis (Demand), Design (Capacity), Interpretation (Status), and Decision (Acknowledgment) must remain strictly separate in the code.

## 4. Steel as Reference Implementation
*   Steel design (`SteelDesignStrategy`) per AISC 360 is the reference-quality implementation.
*   Any new material implementation must match or exceed the rigor, limit state granularity, and traceability of the Steel implementation.
*   Equation-level traceability (Symbolic + Substituted equations) is mandatory for transparency and auditability.

## 5. Section & Material Selection Contract
*   **Material-First Flow:** Selection begins with Material Type, followed by Shape Family, and then specific Section Profile.
*   **Database-Backed Sections:** Properties are retrieved from authoritative sources (Assets/JSON).
*   **Axis-Aware Properties:** The system explicitly distinguishes between Strong Axis and Weak Axis properties based on member orientation.
*   **No Free-Form Geometry Overrides:** Section properties (I, S, Z, etc.) are immutable properties of the selected profile. Manual overrides of individual properties are forbidden to preserve material/section integrity.

## 6. Load & Combination Philosophy
*   **Load Cases vs Combinations:** Loads are authored into specific Load Cases (Dead, Live, etc.). Combinations then aggregate these cases using factors.
*   **Code-Driven Combinations:** Factors and combinations are derived from building codes (e.g., ASCE 7).
*   **Span-Scoped Loads:** Loads are associated with specific spans within a member.
*   **No User-Defined Factors:** To ensure code compliance, users select established code combinations rather than defining arbitrary factors for safety-critical checks.

## 7. UI Ownership Model
Tab-based responsibility is strict to prevent duplicated inputs and fragmented context:

| Tab | Responsibility |
| :--- | :--- |
| **Geometry** | Span definitions + Section selection |
| **Loads** | All load authoring and case management |
| **Design** | Evaluation results, utilization, and acknowledgment |
| **Basis** | Engineering assumptions and design methodology |
| **Revisions** | Version history and audit provenance |

*   **Constraint:** Duplicated inputs across tabs are strictly forbidden.

## 8. Known Gaps (Intentional)
The following are accepted states of the current development:
*   **Incomplete AISC database:** Asset files contain subsets of common sections.
*   **Project Settings UI Gaps:** Global settings vs. Project-specific overrides are in transition.
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
*   **34:** Column design module.
*   **35:** Frame analysis module.

## 11. Edit Protocol
*   New implementation steps should be appended to the history.
*   Changes to **Architectural Laws** require explicit human approval.
*   Deprecated sections must be labeled as `[DEPRECATED]` but not deleted to preserve contextual history.

---
*Note: This file was initialized at Part 1 of the VectOS Baseline Initialization.*
