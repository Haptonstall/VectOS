# NDS 2024 Supplement: adjustment-factor inventory for wiring

Source: *NDS 2024 Supplement*, the "Adjustment Factors" page that precedes each table, plus the footnotes under each table. Page numbers are PDF pages (printed page in brackets). Values are exactly as printed; nothing here was derived or filled from another edition.

Code status is as read from origin `6a68bfe` (`NdsWetService.kt`, `NdsBeamStability.kt`, `NdsWoodCapacityCalculator.kt`, `NdsVersions.kt`, `NdsAdjustmentFactors.kt`). "Present" means a resolver exists, not that it was compared line by line against the 2024 page. Status key: **Present**, **Partial**, **Absent**, **Not located** (I did not find it with a search; confirm).

Data in the repo: `Nds2024Table4A` to `4F`, `5A`, `5B`, `5C`, `5D`. None of these objects carries an adjustment factor. Since the code status below was read, the 2024 design values for Douglas Fir-Larch, Hem-Fir, Spruce-Pine-Fir (Table 4A) and Southern Pine (Table 4B, by width) have been connected through `Nds2024SawnLumber`; the adjustment factors below are unchanged by that. Everything else below still has to be wired.

Decisions recorded for this inventory (Nov 18): use Ex min 1.09 for 28F and 30F SP for now (5A-13); leave Dense Structural 86 / 72 / 65 off for NDS 2024 because the 2024 Supplement has no dimension-lumber data for them (4B-5); do not apply the Fbx- increase (5A-5) until the "specific combinations" are known; apply the volume-factor width cap b <= 10.75 in. for the 5A elements for now (5A-1).

---

## 0. Factors that are not in the Supplement but that the printed rules point to

| Item | Where referenced | Note |
|---|---|---|
| Shear reduction factor Cvr | 5A fn 4 / 5A-Expanded fn 2 / 5B fn 3 (NDS 5.3.10) | Multiplies Fvx and Fvy. Definition is in the main body, not in the Supplement. Code status: **Not located**. |
| Beam stability factor CL vs volume factor CV | 5A to 5D (NDS 5.3.6) | The lesser applies, never both. Code status: **Present** (`min(CL, CV)`). |
| NDS 4.1.3 / Table 1B (Beams and Stringers) | 4D page | Defines which timber rows get Cfu. |
| NDS 5.2.7 | 5A fn 12 | Definition of apparent E. |

---

## 1. Table 4A (dimension lumber, 2" to 4" thick): page 40 [32]; footnotes page 46

| # | Factor | Applies to | Printed rule | Code today |
|---|---|---|---|---|
| 4A-1 | Repetitive member Cr | Fb | 1.15 for members of 2" to 4" thick used as joists, truss chords, rafters, studs, planks, decking or similar, in contact or spaced at most 24" o.c., at least 3 in number, joined by a load-distributing element. | **Partial**: `cr` input exists and is used in the sawn Fb path. I did not check whether eligibility is checked or the value is defaulted. |
| 4A-2 | Flat use Cfu | Fb | By width (rows) and thickness (columns 2"&3" / 4"): 2"&3" wide: 1.0 / not printed; 4": 1.1 / 1.0; 5": 1.1 / 1.05; 6": 1.15 / 1.05; 8": 1.15 / 1.05; 10" & wider: 1.2 / 1.1. | **Present**: `resolveFlatUseCfu`. |
| 4A-3 | Wet service CM | all | Above 19% moisture for an extended time. Fb 0.85*, Ft 1.0, Fv 0.97, Fc⊥ 0.67, Fc 0.8**, E and Emin 0.9. * CM = 1.0 when (Fb)(CF) ≤ 1,150 psi. ** CM = 1.0 when (Fc)(CF) ≤ 750 psi. | **Present**: `NdsWetService`. |
| 4A-4 | Size factor CF | Fb, Ft, Fc | Select Structural / No.1 & Btr / No.1 / No.2 / No.3 by width, columns Fb (2"&3" thick), Fb (4" thick), Ft, Fc: 2"/3"/4": 1.5, 1.5, 1.5, 1.15; 5": 1.4, 1.4, 1.4, 1.1; 6": 1.3, 1.3, 1.3, 1.1; 8": 1.2, 1.3, 1.2, 1.05; 10": 1.1, 1.2, 1.1, 1.0; 12": 1.0, 1.1, 1.0, 1.0; 14" & wider: 0.9, 1.0, 0.9, 0.9. Stud: 2"/3"/4": 1.1, 1.1, 1.1, 1.05; 5" & 6": 1.0 all; 8" & wider: use No.3 values and factors. Construction and Standard: 1.0 all. Utility: 4": 1.0 all; 2"&3": Fb 0.4, Ft 0.4, Fc 0.6 (Fb 4" column printed as a dash). | **Present**: `ndsTable4ASizeFactor` / `resolveSawnCf`. The table values I compared by eye match, but a line-by-line test against the page is not done. |
| 4A-5 | Shading note | CF | Printed values are based on a 12" nominal width (Select Structural to No.3), 6" (Stud) or 4" (Construction, Standard, Utility). | Informational: confirm CF is applied relative to these bases. |
| 4A-6 | fn 3: combined species | all | Use the lowest value of each property. | **Absent** (design rule for multi-species members). |

---

## 2. Table 4B (Southern Pine and Mixed Southern Pine dimension lumber): page 47 [39]; footnotes page 49

| # | Factor | Applies to | Printed rule | Code today |
|---|---|---|---|---|
| 4B-1 | Size factor CF | Fb (and Ft, Fc above 12") | Size is already in the tabulated values for most thicknesses. For 4" thick, 8" and wider: Fb may be multiplied by CF = 1.1. For lumber wider than 12": Fb, Ft and Fc of 12" wide lumber multiplied by CF = 0.9. | **Partial**: the 0.9 case is present in `resolveSawnCf`. The optional 1.1 is not applied automatically (override exists). |
| 4B-2 | Repetitive member Cr | Fb | 1.15, same conditions as 4A. | **Partial**, same as 4A-1. |
| 4B-3 | Flat use Cfu | Fb | Same table as 4A-2. | **Present**. |
| 4B-4 | Wet service CM | all | Fb 0.85*, Ft 1.0, Fv 0.97, Fc⊥ 0.67, Fc 0.8**, E and Emin 0.9. * CM = 1.0 when (Fb)(CF) ≤ 1,150 psi. ** CM = 1.0 when (Fc) ≤ 750 psi. The ** test has no CF (it does in 4A). | **Present** (the code uses Fc ≤ 750 for Southern Pine). |
| 4B-5 | Dense Structural 86 / 72 / 65, wet | Fb etc. | The page says surfaced-dry Dense Structural 86, 72 and 65 use tabulated surfaced-green values in wet service, with no further adjustment. | **Blocked** in code for wet service. **Decided: leave off for 2024.** The 2024 Table 4B as imported has no Dense Structural 86/72/65 rows (they appear only as "Dense Select Structural 86/72/65" in Table 4D, which is timbers). So 2024 has no dimension-lumber row for these grades. |
| 4B-6 | fn 3: Spruce Pine | all | Derive from Mixed Southern Pine with factors Fb 0.78, Ft 0.78, Fv 0.98, Fc⊥ 0.73, Fc 0.78, E and Emin 0.82. Round: E to the nearest 100,000 psi; Emin to the nearest 10,000 psi (printed as "for E" twice; the second is presumably Emin, confirm); Fv and Fc⊥ down to a multiple of 5 psi; Fb, Ft, Fc down to a multiple of 50 psi if 1,000 psi or greater, otherwise 25 psi. | **Absent**. Factors are in `Nds2024Table4B.sprucePineConversion`; the rounding is not encoded. |
| 4B-7 | fn 4: combined species | all | Lowest value of each property. | **Absent**. |
| 4B-8 | Width dependence | Fb, Ft, Fc, E, Emin | Tabulated values change with member width (2"-4", 4", 5"-6", 8", 10", 12"). | **Present** for the 2015/2018 path (`getSouthernPineDimensionValues`); needs a width input for 2024. |

---

## 3. Table 4C (MSR and MEL lumber): page 50 [42]; footnotes page 53

| # | Factor | Applies to | Printed rule | Code today |
|---|---|---|---|---|
| 4C-1 | Flat use Cfu | Fb | Thickness 2" only: width 2"&3": 1.0; 4": 1.1; 5": 1.1; 6": 1.15; 8": 1.15; 10" & wider: 1.2. | **Partial**: same resolver as 4A; there is no 4" thickness column here. |
| 4C-2 | Repetitive member Cr | Fb | 1.15, same conditions as 4A. | **Partial**. |
| 4C-3 | Wet service CM | all | Fb 0.85*, Ft 1.0, Fv 0.97, Fc⊥ 0.67, Fc 0.8**, E and Emin 0.9. * CM = 1.0 when Fb ≤ 1,150 psi. ** CM = 1.0 when Fc ≤ 750 psi (no CF in either test). | **Partial**: the sawn path multiplies by CF in the Fb test, so applying it to MSR/MEL as is would test the wrong quantity. |
| 4C-4 | Size factor CF | none | No size factor is printed for 4C. | The 4A CF resolver must not be applied to MSR/MEL. |
| 4C-5 | fn 2: G, Fv, Fc⊥ | Fv, Fc⊥, G | Fv and Fc⊥ come from the species table (data present). Higher G may be claimed if assigned by the rules agency or qualified by test and on the grade stamp; then higher Fv and Fc⊥ may be calculated per the grading rules. For species not listed use the visually graded values. | **Absent**. |
| 4C-6 | fn 3: E and Ft on the grade stamp | E, Ft, Fc | The E and Ft in each "Fb-E" designation are those usually associated with the Fb level. Where the E or Ft on a grade stamp differs from the Table 4C values for that Fb, the grade-stamp values are used in design, and the Fc associated with that Fb in Table 4C is used. | **Absent**. Needs inputs for stamped E and Ft. |
| 4C-7 | fn 2 asterisk | Fv, Fc⊥ | 1.8E Southern Pine marked with G = 0.55 on the grade stamp has Fv 175 psi and Fc⊥ 565 psi (stored in `Nds2024Table4C.SOUTHERN_PINE_1_8E_G055`). | **Absent**. |

---

## 4. Table 4D (visually graded timbers, 5" x 5" and larger): page 54 [46]; footnotes page 60

| # | Factor | Applies to | Printed rule | Code today |
|---|---|---|---|---|
| 4D-1 | Size factor CF | Fb, Ft, Fc | For loads on the narrow face: d > 12": Fb (12/d)^(1/9), Ft 1.0, Fc 1.0; d ≤ 12": 1.0 all. | **Partial**: the Fb formula exists only as the Dense Structural branch in `resolveSawnCf`; there is no timber species path. |
| 4D-2 | Flat use Cfu | Fb, E, Emin | Beams and Stringers only (NDS 4.1.3). Select Structural: Fb 0.86, E and Emin 1.00, other 1.00. No.1: Fb 0.74, E and Emin 0.90, other 1.00. No.2: all 1.00. For flatwise bending or buckling. | **Absent**. |
| 4D-3 | Wet service CM | all | Fb 1.00, Ft 1.00, Fv 1.00, Fc⊥ 0.67, Fc 0.91, E and Emin 1.00. For Southern Pine and Mixed Southern Pine use the tabulated values with no further adjustment (the data rows carry `wetServiceValues = true`). | **Absent**. |
| 4D-4 | fn 2: Spruce Pine | all | Same factors and rounding as 4B-6. | **Absent**. |
| 4D-5 | fn 3: combined species | all | Lowest value of each property. | **Absent**. |
| 4D-6 | fn 1 | size | Green dressed sizes may be used for 5" and thicker. | Informational. |

---

## 5. Table 4E (decking): page 61 [53]; footnotes page 64

| # | Factor | Applies to | Printed rule | Code today |
|---|---|---|---|---|
| 4E-1 | Size factor CF | Fb | All species except Redwood; values are for 4" thick decking. 2" thick: 1.10; 3" thick: 1.04. | **Absent**. |
| 4E-2 | Wet service CM | Fb, Fc⊥, E, Emin | Fb 0.85*, Fc⊥ 0.67, E and Emin 0.9. * CM = 1.0 when (Fb)(CF) ≤ 1,150 psi. For surfaced dry Southern Pine decking use the tabulated surfaced green values with no further adjustment (data carries `surfacing`). | **Absent**. |
| 4E-3 | Repetitive member Cr | Fb | Already included in the printed (Fb)(Cr) column: do not apply again. | **Absent**; must not default to 1.15. |
| 4E-4 | Flat use Cfu | Fb | Already included in the tabulated Fb: do not apply again. | **Absent**; the 4A resolver must not be applied. |
| 4E-5 | Dash entries | Fb single, Fc⊥ | The table prints no value for some species. | Data is null; design must block or skip. |
| 4E-6 | fn 2: combined species | all | Lowest value of each property. | **Absent**. |

---

## 6. Table 4F (non-North American dimension lumber): page 65 [57]; footnotes page 69

| # | Factor | Applies to | Printed rule | Code today |
|---|---|---|---|---|
| 4F-1 | Repetitive member Cr | Fb | 1.15, same conditions as 4A. | **Partial**. |
| 4F-2 | Flat use Cfu | Fb | Same table as 4A-2. | **Present** (resolver); not connected to 4F data. |
| 4F-3 | Wet service CM | all | Same values as 4A. * (Fb)(CF) ≤ 1,150 psi, ** (Fc)(CF) ≤ 750 psi. | **Present** (resolver). |
| 4F-4 | Size factor CF | Fb, Ft, Fc | Same schedule as 4A-4 (including the shading note). | **Present** (resolver). |
| 4F-5 | fn 2: Douglas Fir/European Larch | all | Values apply only to 2x4 dimension lumber and are not multiplied by the size factor. | **Absent**; CF must be disabled for that block. |
| 4F-6 | fn 3: combined species | all | Lowest value of each property. | **Absent**. |
| 4F-7 | fn 4: Scots Pine, Germany | all | Does not include Baden-Wurttemberg and Saarland. | Informational (data carries the footnote number). |

---

## 7. Table 5A and 5A-Expanded (glulam softwood, bending): page 87 [79]; footnotes pages 88 and 90

| # | Factor | Applies to | Printed rule | Code today |
|---|---|---|---|---|
| 5A-1 | Volume factor CV | Fbx | CV = (21/L)^(1/x) (12/d)^(1/x) (5.125/b)^(1/x) ≤ 1.0. L is the length between points of zero moment (ft), d the depth (in.), b the width (in.; for multiple piece width, the width of the widest piece, so b ≤ 10.75 in.). x = 20 for Southern Pine, 10 for all other species. Not applied together with CL: the lesser governs. | **Present**: `computeCV`. **Decided:** cap b at 10.75 in. for the 5A elements for now; the code currently uses the dressed width with no cap. |
| 5A-2 | Flat use Cfu | Fby | When the member dimension parallel to the wide faces is under 12": 10-3/4" or 10-1/2": 1.01; 8-3/4" or 8-1/2": 1.04; 6-3/4": 1.07; 5-1/8" or 5": 1.10; 3-1/8" or 3": 1.16; 2-1/2": 1.19. | **Present** (`resolveFlatUseCfu`, next larger tabulated size). |
| 5A-3 | Wet service CM | all | At 16% moisture or greater: Fb 0.8, Ft 0.8, Fv 0.875, Fc⊥ 0.53, Fc 0.73, E and Emin 0.833. | **Present**. |
| 5A-4 | fn 1: balanced layups | Fbx- | For balanced layups Fbx- equals Fbx+ (the "stress class" value). Designer must specify a balanced layup. | **Absent**. |
| 5A-5 | fn 2: Fbx- increase | Fbx- | May be increased to 1,850 psi (Douglas Fir) or 1,950 psi (Southern Pine) for specific combinations; designer must specify. The table does not say which combinations. | **Absent**; **decided:** not applied until the specific combinations are known. |
| 5A-6 | fn 3: Southern Pine shear | Fvx, Fvy | May be increased to 300 psi and 260 psi. | **Absent**. |
| 5A-7 | fn 4 / Exp. fn 2: Cvr | Fvx, Fvy | Multiply by Cvr per NDS 5.3.10. | **Not located**. |
| 5A-8 | fn 5 / Exp. fn 3: multi-piece laminations | all | Single-piece or edge-bonded laminations: as tabulated. Multiple piece laminations not edge bonded: multiply by 0.4 for 5, 7 or 9 laminations, 0.5 for other members; cumulative with Cvr. | **Absent**. |
| 5A-9 | fn 6 / Exp. fn 4: wane | Fvx | Certain Southern Pine combinations may contain wane. If wane is allowed on both sides multiply Fvx by 0.67; if limited to one side, 0.83; cumulative with Cvr. Markers are stored on 20F-1.5E, 24F-1.7E and 24F-V4 SP/SP. | **Absent**. |
| 5A-10 | fn 9 / Exp. fn 7: more than 15 laminations | 28F, 30F | Ex true 2.1, Ex 2.0, Ex min 1.06 (the Expanded footnote gives only Ex 2.0 and Ex min 1.06). | **Absent**. |
| 5A-11 | fn 10: fastener G | fasteners | Southern Pine specific gravity for fastener design may be 0.55. | **Absent**. |
| 5A-12 | fn 7, 8: availability and 30F width | 26F, 28F, 30F | Availability limited; 30F limited to 6 in. nominal width unless the manufacturer has qualified wider. | **Absent** (informational or hard-block decision for you). |
| 5A-13 | Printed Ex min discrepancy | 28F, 30F | 1.09 in the basic table, 1.11 in the Expanded table. | **Decided:** use 1.09 for now. |
| 5A-14 | Shear-free modulus | Ex true | For deflection with shear considered separately. | **Present** (shear deflection item). |

Reference design values are for 4 or more laminations; for 2 and 3 lamination members the Supplement points to Table 5B.

---

## 8. Table 5B (glulam softwood, axial / parallel-to-wide-face and 2 or 3 laminations): page 91 [83]; footnotes page 92

| # | Factor | Applies to | Printed rule | Code today |
|---|---|---|---|---|
| 5B-1 | Volume factor CV | Fbx | Same formula as 5A-1 (x = 20 Southern Pine, 10 other species). | **Present**. |
| 5B-2 | Flat use Cfu | Fby | Same table as 5A-2. | **Present**. |
| 5B-3 | Wet service CM | all | Same as 5A-3. | **Present**. |
| 5B-4 | fn 1: 2 and 3 laminations | Fvy | Multiply by 0.84 (2 laminations) or 0.95 (3 laminations). | **Absent**. |
| 5B-5 | fn 2: multi-piece laminations | Fvy | 0.4 for 5, 7 or 9 laminations of multiple piece laminations not edge bonded; 0.5 for other multiple piece laminations with unbonded edge joints; cumulative with fn 1 and fn 3. | **Absent**. |
| 5B-6 | fn 3: Cvr | Fvx, Fvy | Per NDS 5.3.10. | **Not located**. |
| 5B-7 | fn 4: deep members | Fbx | For members deeper than 15 in., multiply Fbx by 0.88. | **Absent**. |
| 5B-8 | fn 5: SW combinations | E, Emin, Fvx, Fvy | In Softwood Species (SW) combinations (stored marker on combination 22): when Western Cedars, Western Cedars (North), Western Woods or Redwood (open grain) are used, reduce E by 100,000 psi and Emin by 52,800 psi; when Coast Sitka Spruce, Coast Species, Western White Pine or Eastern White Pine are used, reduce Fvx and Fvy by 10 psi, before any other adjustment. | **Absent**. |

---

## 9. Table 5C (glulam hardwood, bending): page 93 [85]; footnotes page 94

| # | Factor | Applies to | Printed rule | Code today |
|---|---|---|---|---|
| 5C-1 | Volume factor CV | Fbx | Same formula with x = 10 for every combination. | **Present** (x = 10 for hardwood species). |
| 5C-2 | Flat use Cfu | Fby | Same table as 5A-2. | **Present**. |
| 5C-3 | Wet service CM | all | Same as 5A-3. | **Present**. |
| 5C-4 | fn 2: multi-piece laminations | all | Multiply by 0.4 for 5, 7 or 9 laminations, 0.5 for other members, for timbers of multiple piece laminations not edge bonded. | **Absent**. |
| 5C-5 | fn 3: fastener species | fastener G | Fastener values are for the group of hardwood species permitted in the combination; the actual species' value may be used if known. | **Absent**. |
| 5C-6 | fn 1: scope | all | Applies to 4 or more laminations; for 2 or 3 laminations and axial or parallel-to-wide-face loading see Table 5D. | Informational. |

---

## 10. Table 5D (glulam hardwood, axial / parallel-to-wide-face and 2 or 3 laminations): page 95 [87]; footnotes page 97

| # | Factor | Applies to | Printed rule | Code today |
|---|---|---|---|---|
| 5D-1 | Volume factor CV | Fbx | Same formula as 5C-1 (x = 10). | **Present**. |
| 5D-2 | Flat use Cfu | Fby | Same table as 5A-2. | **Present**. |
| 5D-3 | Wet service CM | all | Same as 5A-3. | **Present**. |
| 5D-4 | fn 1: 2 and 3 laminations | Fvy | Multiply by 0.84 (2) or 0.95 (3). | **Absent**. |
| 5D-5 | fn 2: multi-piece laminations | Fvy | 0.4 for 5, 7 or 9 laminations not edge bonded; 0.5 for other; cumulative with fn 1. | **Absent**. |
| 5D-6 | fn 3: deep members | Fbx | For members deeper than 15 in., multiply Fbx by 0.88. | **Absent**. |

---

## 11. Differences between tables that are easy to get wrong when wiring

1. **CM thresholds differ.** 4A and 4F: Fb and Fc tests include CF. 4B: Fb includes CF, Fc does not. 4C: neither includes CF (4C has no CF). 4E: Fb includes CF, no Fc test. 4D: no thresholds.
2. **Moisture trigger differs.** Sawn lumber, timbers and decking: above 19% for an extended time. Glulam: 16% or greater.
3. **Already-included factors.** 4E Cr and Cfu are inside the printed values. 4B size factors are inside the printed values for most thicknesses. Southern Pine in 4D and Southern Pine surfaced-green decking in 4E need no further wet adjustment.
4. **CF absent or disabled.** None printed for 4C; 4F Douglas Fir/European Larch must not take it; 4D is Fb only above 12" deep; 4E is Fb only, 2" and 3" thick, not Redwood.
5. **Cfu is not one table.** 4A/4B/4C/4F share the width-by-thickness table (4C has only the 2" thickness column). 4D uses a grade table for Beams and Stringers. 4E has none. 5A to 5D share the member-dimension table (under 12").
6. **CV exponent.** 5A and 5B: 20 for Southern Pine, 10 otherwise. 5C and 5D: 10 always. The widest-piece cap b ≤ 10.75 in. appears on every glulam page.
7. **Glulam shear and modulus footnotes are table-specific** (the basic and Expanded 5A tables number their footnotes differently; see the stored markers).
8. **Nothing here is applied by the data objects**; each factor above needs a resolver or an input that reads the data and a test with independently hand-calculated expected values.
