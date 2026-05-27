# Implementation Plan - Structural App Improvements

This plan addresses UI issues in the beam graphic and span input, logic errors in load combinations, and data persistence issues in the project library.

## Proposed Changes

### UI Improvements (Graphics & Input)

#### [StructuralDrawingUtils.kt](file:///C:/Users/hapto/AndroidStudioProjects/VectOS/app/src/main/java/com/lz/vectos/ui/beam/StructuralDrawingUtils.kt)
- Increase label text size from `10.sp` to `14.sp` to improve readability on the beam graphic.

#### [SpanEditor.kt](file:///C:/Users/hapto/AndroidStudioProjects/VectOS/app/src/main/java/com/lz/vectos/ui/tool/SpanEditor.kt)
- Refactor `SpanItem` to use a local `mutableStateOf` for the text field.
- Update the underlying model (`onUpdateLength`) only when the input is valid and the text field loses focus (using `onFocusChanged`) or when the user explicitly finishes entry.
- This prevents the "jumping" behavior caused by immediate reformatting of the input.

---

### Load Combination Logic

#### [LoadCategory.kt](file:///C:/Users/hapto/AndroidStudioProjects/VectOS/app/src/main/java/com/lz/vectos/domain/structural/LoadCategory.kt)
- Add `RAIN("Rain Load", "R")` to the `LoadCategory` enum to support full ASCE 7 combinations.

#### [StructuralDataSeeder.kt](file:///C:/Users/hapto/AndroidStudioProjects/VectOS/app/src/main/java/com/lz/vectos/persistence/room/StructuralDataSeeder.kt)
- Expand the simplified load combinations to the full ASCE 7-16 and ASCE 7-22 sets for both ASD and LRFD.
- Ensure all categories (L, Lr, S, R, W, E) are handled correctly.

#### [BeamViewModel.kt](file:///C:/Users/hapto/AndroidStudioProjects/VectOS/app/src/main/java/com/lz/vectos/viewmodel/BeamViewModel.kt)
- Sync `methodology`, `unitSystem`, and `activeBuildingCode` from the `ProjectViewModel`'s `activeProject` state.
- Implement a collector to refresh `selectedCombinationSet` and `enabledCombinations` whenever the project's design methodology or building code changes.

---

### Data Persistence & Library

#### [ProjectViewModel.kt](file:///C:/Users/hapto/AndroidStudioProjects/VectOS/app/src/main/java/com/lz/vectos/viewmodel/ProjectViewModel.kt)
- Implement `loadCalculationsForActiveProject()` which fetches calculations from `CalculationRepository` for the active project.
- Map the retrieved `BeamCalculation` objects to `EngineeringCalculation` domain models and populate the `ProjectCalculationRegistry`.
- Add `addCalculationToRegistry(BeamCalculation)` to allow `BeamViewModel` to notify the registry of new/updated saves.

#### [BeamViewModel.kt](file:///C:/Users/hapto/AndroidStudioProjects/VectOS/app/src/main/java/com/lz/vectos/viewmodel/BeamViewModel.kt)
- After calling `calculationRepository.saveBeamCalculation(result)`, notify the `ProjectViewModel` via the new `addCalculationToRegistry` method to ensure the library view is updated immediately.

## Verification Plan

### Automated Tests
- Run existing unit tests (if any) to ensure no regressions in calculation logic.
- Add a test case in `LoadCombinationEngineTest` (if it exists) to verify new combinations are applied correctly.

### Manual Verification
1.  **Beam Graphic:** Open the beam calculator and verify joint labels (0, 1, 2...) are clearly readable.
2.  **Span Input:** Edit a span length and verify that typing "15.5" doesn't immediately force it to "15.00" while typing. Verify validation occurs on focus loss.
3.  **Load Combinations:** Change project settings to ASD and verify the 'LCs' tab shows ASD combinations (e.g., "D + L" instead of "1.2D + 1.6L").
4.  **Full LCs:** Verify that combinations including Snow, Wind, etc., appear in the list when those load cases have loads.
5.  **Project Library:** Save a beam calculation, return to the Project Library, and verify it appears under "Engineering Calculations" for that project.
