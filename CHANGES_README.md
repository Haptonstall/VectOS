# VectOS session patch — apply instructions

## Applying

From your repo root, on `working-progress`, with a clean working tree:

```
git apply --check vectos-code-consolidation.patch   # dry run, verify no conflicts
git apply vectos-code-consolidation.patch
```

If `--check` reports conflicts (likely if you've made local edits since the
`working-progress` HEAD this was cloned from — commit `53f1f61`), let me know
and I'll regenerate against your current HEAD instead of guessing at a merge.

31 files changed: 27 modified, 2 new source files, 2 new test files, 2 deleted.

**After applying:** this changes the Room schema (v1 -> v2) and deletes two
files that had zero callers before this session (confirmed via repo-wide
grep each time, not assumed) — worth a full `./gradlew build` and running
the test suites below before trusting it.

---

## What's in it, grouped by the three things fixed this session

### 1. NDS 3.3.3 `CL` stability factor (was stubbed to 1.0)
- New: `core/solver/material/NdsBeamStability.kt` — shared `computeNdsCL()`
- `NdsWoodCapacityCalculator.kt` — delegates to the shared function
- `NdsClCalculator.kt` — rewritten, now takes `(profile, material)` and
  returns a real value instead of `1.0`
- `BeamAnalysisSolver.kt` — updated construction site
- New test: `NdsClCalculatorTest.kt`

### 2. `CapacityEngine` placeholder factors (φ=0.9/Ω=1.67 flat, every material)
This was the most severe finding — it's what actually computes every
utilization ratio shown to users. For wood ASD it silently double-divided
capacity (NDS's nominal already *is* the adjusted allowable value); for
wood LRFD it used a flat 0.9 instead of NDS's real per-limit-state φ.
- New: `core/solver/capacity/DesignFactorSet.kt`
- `CapacityCalculator.kt` — new `designFactors()` contract method
- `AiscSteelCapacityCalculator.kt` — implements it via `AiscDesignFactors`
- `NdsWoodCapacityCalculator.kt` — implements it, ASD=1.0 (no re-division),
  LRFD=λ·φ per limit state (0.85 bending / 0.75 shear / 0.80 tension / 0.90
  compression), replicating the logic already proven correct in its own
  `evaluateDetailed()`
- `CapacityEngine.kt` — takes `factors: DesignFactorSet`, all hardcoded
  locals removed
- `BeamAnalysisSolver.kt` — updated call site
- New test: `CapacityEngineTest.kt` — proves wood ASD isn't double-divided,
  wood LRFD uses real NDS φ, steel matches `AiscDesignFactors` exactly

### 3. Building-code selection + three competing edition-resolution systems
- **Live bug fix:** `BeamViewModel.kt` now reads the active project's
  `buildingCodeId` instead of hardcoding `"IBC_2024"` regardless of what
  the user picked.
- **UI/domain migration:** `PrimaryBuildingCode` (3-value enum, one value
  — `CBC_2025` — didn't even exist in the seeded DB catalog) replaced with
  `ProjectSettings.buildingCodeId: String`, matching the real 6-code DB
  catalog directly. Touches `Project.kt`, `ProjectRoomEntity.kt`,
  `ProjectPersistenceMapper.kt`, `ProjectViewModel.kt`, `NewProjectScreen.kt`
  (fixes a `PrimaryBuildingCode.valueOf(code.id)` that would throw for 4 of
  the 6 real codes), `ProjectLibraryScreen.kt`, `ProjectSettingsScreen.kt`.
- **Real Room migration** `MIGRATION_1_2` in `Migrations.kt` (version bump
  in `AppDatabase.kt`): adds `material_type`/`edition_family`/`edition_key`
  to `standards`; rebuilds `projects` to rename `buildingCode` ->
  `buildingCodeId` (remapping orphaned `CBC_2025` rows to `CBC_2022`).
  Uses create/copy/drop/rename rather than `ALTER TABLE...DROP/RENAME
  COLUMN` — those need SQLite 3.35+, not guaranteed on your `minSdk 28`.
- **Deleted (confirmed zero callers before removal, each verified via
  repo-wide grep):**
  - `RegulatoryRegistry.kt` (hardcoded `PrimaryBuildingCode`-keyed system,
    fully superseded by the DB-backed `BuildingCode`)
  - `BuildingCodeDao.kt` (fully-scaffolded but never-queried duplicate of
    the live `CodeRegistryDao`)
  - Duplicate `Asce7Edition`/`NdsEdition` in `RegulatoryEnums.kt` (a second,
    incompatible definition of each existed in `regulatory.asce7`/`.nds` —
    those are now the sole definitions)
  - `Standard.fromId()` / `BuildingCode.fromId()` — dead stubs, zero callers
- **Kept and wired up:** `AiscDesignFactorRegistry`/`AiscDesignFactors` (the
  one genuinely complete piece of the old system B) — now actually driven
  by a real resolved edition instead of sitting uncalled.
- `CodeRegistryDao.kt` — added the `default_material_standards` relation +
  insert method (this table existed in the schema since v1 but was never
  used — the "System C" scaffolding).
- `StructuralMappers.kt` — resolves `Standard.edition`/`materialType` from
  the new columns; builds `BuildingCode.defaultMaterialStandards`.
- `BuildingCodeSeeder.kt` — populates the new columns, cross-references
  AISC/NDS standards to all 6 building codes, seeds
  `default_material_standards`. **The code-year pairings here (e.g. IBC
  2021 -> AISC 360-16, not 360-22) are my best-effort domain mapping, not
  derived from anything in the repo — please sanity-check before trusting.**
- `AiscSteelCapacityCalculator.kt` / `NdsWoodCapacityCalculator.kt` — both
  now take an optional resolved edition (default preserves prior hardcoded
  behavior for any caller not yet updated).
- `AiscDesignFactorRegistry.kt` — added `AISC_360_10` (needed since IBC 2015
  maps to it); same φ/Ω as 360-16/360-22, consistent with the registry's
  existing documented basis for treating these as edition-invariant.
- `BeamAnalysisConfig.kt` / `BeamAnalysisSolver.kt` — config now carries the
  active `BuildingCode`; solver resolves the real AISC/NDS edition from it
  and threads it into both capacity calculators, closing the loop from
  "user's building code selection" to "equations actually applied."

---

## Suggested verification order

```
./gradlew build   # full compile check first — largest single-pass change this repo has seen
./gradlew :core:solver:testDebugUnitTest --tests "com.lz.solver.capacity.CapacityEngineTest"
./gradlew :core:solver:testDebugUnitTest --tests "com.lz.solver.material.NdsClCalculatorTest"
./gradlew :feature:beam:testDebugUnitTest --tests "com.lz.beam.solver.BeamAnalysisSolverVerificationTest"
```

Then a manual pass: create a project on each of the 6 building codes,
confirm the New Project / Project Settings pickers work for all of them
(previously 4 of 6 would crash), and spot-check that a wood beam's ASD
utilization ratio changed (it should — this was the double-division bug).

## Known follow-ups not done in this pass
- `NdsClCalculator`'s CL formula doesn't distinguish cantilever load cases
  per NDS Table 3.3.3 multipliers (pre-existing simplification, carried
  over unchanged from `NdsWoodCapacityCalculator`'s original logic).
- Stale pre-migration test files `app/src/test/.../CapacityEngineTest.kt`
  and `.../AiscSteelCapacityCalculatorTest.kt` reference a package
  (`com.lz.vectos.domain.structural...`) that no longer exists as real
  source — already broken before this session, untouched here, still
  Priority-3 cleanup debt.
- `GAP`/`TENSION_ONLY`/`NONLINEAR_SPRING` boundary conditions still
  unimplemented no-ops (original handoff doc, item 5).
