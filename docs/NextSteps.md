# Structural Solver Roadmap

This document tracks the planned and proposed improvements for the structural analysis domain, specifically the `BeamAnalysisSolver`.

## Item 1: Span-Specific Stiffness Properties (Incorporating Transitions & Tapers)
- **Status**: Selected for implementation.
- **Problem**: Current solver uses a single $E$ and $I$ for the entire multi-span member.
- **Solution**:
    - Introduce a `SectionPropertyProvider` interface.
    - Initially, it will return constant $E$ and $I$ per span.
    - Future-proof for tapered beams by allowing $I(x)$ as a function of position.
    - Update the stiffness matrix assembly to handle these per-span properties. For constant properties, use closed-form matrices; for variable properties, use high-order Gaussian quadrature.

## Item 2: Analytical (Closed-Form) Integration
- **Status**: Selected for implementation.
- **Problem**: Numerical integration (20-segment approximation) introduces small errors and is computationally less efficient.
- **Solution**:
    - Implement exact formulas for Fixed-End Actions (FEAs) for Point, UDL, and Trapezoidal loads (e.g., $M = \frac{wL^2}{12}$).
    - Implement exact displacement functions $v(x)$ based on beam bending equations for each load type.
    - Ensures 100% precision and higher performance.

## Item 3: Timoshenko Beam Theory (Shear Deformation)
- **Status**: Selected for implementation.
- **Problem**: Euler-Bernoulli theory only considers bending deformation, ignoring shear which is significant for "deep" beams (low L/D ratio).
- **Solution**:
    - Update the stiffness matrix to include the shear deformation parameter $\phi = \frac{12EI}{L^2 GA_v}$.
    - Incorporate Shear Modulus ($G$) and Shear Area ($A_v$) into the analysis.
    - Default $\phi = 0$ maintains backwards compatibility with bending-only behavior.

## Item 4: Influence Line Generation
- **Status**: Proposed.
- **Strategy**: Use the **Mueller-Breslau Principle**.
- **Implementation**:
    - Add `generateInfluenceLine(member, x, type: InfluenceType)` API.
    - Internally "insert a hinge" at $x$, apply a unit moment/force, and calculate the deflected shape.
    - Significantly faster and more elegant than running multiple load-stepping simulations.

## Item 5: Support Settlement & Thermal Loading
- **Status**: Proposed.
- **Strategy**: Treat as "Fixed-End Actions".
- **Implementation**:
    - Add `SupportSettlement(nodeIndex, displacement)` and `ThermalLoad(deltaT, alpha)` to the Load hierarchy.
    - Calculate equivalent internal moments and forces (e.g., $6EI\Delta/L^2$ for settlement).
    - Add these to the global force vector so the indeterminate system reacts correctly to environmental changes.

## Item 6: P-Delta Analysis (Secondary Effects)
- **Status**: Proposed.
- **Strategy**: Implement **Geometric Stiffness Matrix ($K_g$)**.
- **Implementation**:
    - Add `AnalysisType` (First-Order Linear vs Second-Order P-Delta) to `BeamAnalysisConfig`.
    - First pass determines axial forces ($P$); second pass updates span stiffness based on $P$.
    - Captures moment amplification and secondary stability risks in slender members.

## Item 7: On-Demand Result Evaluators
- **Status**: Proposed.
- **Strategy**: Return analytical evaluators instead of discrete point lists.
- **Implementation**:
    - Solver returns a `BeamEvaluator` object.
    - Provide `momentAt(x)`, `shearAt(x)`, and `deflectionAt(x)` methods.
    - Calculates values instantly using exact equations and node displacements for any arbitrary $x$, eliminating interpolation errors.

## App Fixes Priority
The following tasks are prioritized for the current app workstream. They are ordered by minimal-impact changes first, consistent with the architecture guardrails in `ARCHITECTURE_CONTEXT.md`.

1. Fix shear and moment diagrams not visible in the `Analysis` tab.
2. Add interactive scrolling/scrubbing along the beam diagram in the `Analysis` tab to show exact shear, moment, or deflection values at the selected cursor location.
3. Resolve the shear and moment diagrams not matching correct output.
4. Update the loading dialog boxes to show tributary width options.
5. Update the loading dialog boxes to apply load to individual span or entire beam.
6. Update the bracing visualization in the beam view.
7. Update the colors across the app, including dialogs (notably the loading dialog).
8. Improve the steel shape selection dialog with quick jump buttons for each beam depth (e.g. `W12`, `HSS8`).
9. Fix the steel shape type list so it is complete and scrollable on the emulator.
10. Resolve the missing associated standards for a selected building code in the Project information form.
11. Move the edit pencil icon on the Project information card from the `Project Summary` label to the right side of the card.
12. Enhance the design cards for shear, flexure, torsion, deflection, etc. so they are expandable and show full capacity development with code references, variables, equations, and selectable load combinations.

## Single-Step Implementation Plan
We will proceed one step at a time to minimize risk and remain aligned with the baseline architecture.

- Next Step: Investigate and restore visibility of shear and moment diagrams in the `Analysis` tab.
- Goal: Ensure diagrams are rendered and accessible before modifying analysis results.
- Scope: UI/visibility fix only, no domain solver changes or architecture impacts.
- Approval: Do not implement the next step until explicit consent is given.
