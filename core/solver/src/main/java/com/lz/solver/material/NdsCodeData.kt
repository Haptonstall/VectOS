package com.lz.solver.material

import com.lz.model.regulatory.nds.NdsEdition
import com.lz.model.structural.CodeDataNotice
import com.lz.model.structural.CodeDataStatus
import com.lz.model.structural.DesignMethodology
import com.lz.model.structural.WoodGrade
import com.lz.model.structural.WoodSpecies

/**
 * What the wood calculator can and can't stand behind for a given NDS edition.
 *
 * The values and rules in the calculator come from specific editions of the
 * NDS and its Supplement. Projects select a building code (IBC 2015 / 2018 /
 * 2021 / 2024 -> NDS 2015 / 2018 / 2018 / 2024), so the calculator must know
 * when it is being run against an edition whose data hasn't been incorporated,
 * rather than quietly applying another edition's numbers. This registry is
 * where that knowledge lives: each dataset records which editions it is
 * verified for. As an edition's tables are entered, flip its status here and
 * the notices disappear for it.
 *
 * Statuses today (honest as of this writing):
 *  - Reference design values and size / flat-use factors: sourced from the
 *    NDS 2018 and NDS 2015 Supplements. Other editions are HARD-BLOCKED
 *    ([blockReason]), and so is any species / grade missing from the edition's tables.
 *    2015 was read from the published Supplement: DF-L / Hem-Fir / SPF (4A), Southern
 *    Pine (4B) and hardwood glulam (5C) are identical to 2018; softwood glulam (5A) has
 *    its own 2015 values (22 combinations).
 *  - Glulam tables: mixed provenance (see the notice text), 2018-era.
 *  - CD (Table 2.3.2, §2.3.2), Ct (Table 2.3.3), KF (Table 2.3.5) and phi
 *    (Table 2.3.6): checked against the NDS 2018 text published by the AWC and
 *    against the NDS 2015 page images supplied by the project owner (the
 *    values are identical in the two); not checked for 2024.
 *  - KF / phi / lambda (Appendix N, Tables N1 - N3): checked against the NDS
 *    2015 page images. Lambda is not checked for 2018 or 2024.
 *  - Stability / combined-stress provisions (3.3.3, Table 3.3.3, 3.7.1, 3.9):
 *    checked line by line against the NDS 2015 page images supplied by the
 *    project owner. No 2018 or 2024 text of Chapter 3 has been seen, so those
 *    editions are not checked.
 */
/** Thrown when wood design is requested for an edition whose design values have not been incorporated. */
class CodeDataBlockedException(message: String) : IllegalStateException(message)

object NdsCodeData {

    /**
     * Why wood design is blocked for [edition], or null when it may run. An
     * edition whose Supplement design values and size / flat-use factors have
     * not been sourced is blocked outright - no other edition's numbers are
     * substituted (project decision, Oct 2026).
     */
    fun blockReason(edition: NdsEdition): String? =
        if (edition in designValueEditions) null
        else "Wood design is blocked for ${standardLabel(edition)}: its Supplement design values " +
            "(Tables 4A / 4B / 4D / 4E / 5A / 5C) and the size and flat-use factors have not been " +
            "incorporated, and values from another edition are not substituted. Use a building code " +
            "that maps to a sourced edition (${designValueEditions.joinToString { standardLabel(it) }}) " +
            "or add the ${standardLabel(edition)} Supplement data."

    /**
     * As [blockReason], and also blocked when the edition's tables do not contain this
     * species / grade (e.g. a glulam combination that is not in the NDS 2015 Table 5A).
     */
    fun blockReason(edition: NdsEdition, species: WoodSpecies, grade: WoodGrade): String? =
        blockReason(edition)
            ?: if (WoodPropertyService.isSourced(species, grade, edition)) null
            else "Wood design is blocked: ${species.name} / ${grade.name} has no tabulated design values in " +
                "the ${standardLabel(edition)} Supplement as incorporated here, and values from another " +
                "edition are not substituted. Choose a combination that is in the ${standardLabel(edition)} tables."

    /** @throws CodeDataBlockedException when [blockReason] is non-null. */
    fun requireUsable(edition: NdsEdition) {
        blockReason(edition)?.let { throw CodeDataBlockedException(it) }
    }

    fun requireUsable(edition: NdsEdition, species: WoodSpecies, grade: WoodGrade) {
        blockReason(edition, species, grade)?.let { throw CodeDataBlockedException(it) }
    }

    /**
     * Editions the Supplement design values and factors have actually been sourced for.
     * NDS 2015: Tables 4A, 4B, 5A and 5C and their adjustment factors, checked against the
     * 2015 Supplement PDF (individual rows may still be missing - see the per-material block).
     */
    private val designValueEditions = setOf(NdsEdition.NDS_2018, NdsEdition.NDS_2015)

    /** Editions whose Chapter 2 (CD, Ct, KF, phi) has been checked against the published text. */
    private val chapter2Editions = setOf(NdsEdition.NDS_2018, NdsEdition.NDS_2015)

    /** Editions whose Chapter 3 stability / combined-stress provisions have been checked against the text. */
    private val chapter3Editions = setOf(NdsEdition.NDS_2015)

    /** Editions whose Appendix N Table N3 (lambda) has been checked against the text. */
    private val lambdaEditions = setOf(NdsEdition.NDS_2015)

    fun standardLabel(edition: NdsEdition) = "NDS ${edition.year}"

    fun noticesFor(
        edition: NdsEdition,
        methodology: DesignMethodology,
        isGlulam: Boolean
    ): List<CodeDataNotice> {
        val label = standardLabel(edition)
        val out = mutableListOf<CodeDataNotice>()

        if (edition !in designValueEditions) {
            out += CodeDataNotice(
                dataset = if (isGlulam) "Reference design values \u2014 glulam (Supplement Tables 5A / 5C)"
                else "Reference design values and size factors (Supplement Tables 4A / 4B)",
                status = CodeDataStatus.NOT_SOURCED,
                message = "The $label Supplement values have not been incorporated, so wood design is blocked " +
                    "for this edition; no other edition's values are substituted."
            )
        } else if (isGlulam && edition == NdsEdition.NDS_2018) {
            out += CodeDataNotice(
                dataset = "Reference design values \u2014 glulam (Supplement Tables 5A / 5C)",
                status = CodeDataStatus.UNVERIFIED,
                message = "Mixed provenance: some combinations come from ICC-ES ESR-1940 (2018-era), some from the " +
                    "NDS 2018 Supplement, with a few DF/DF rows corrected against a 2024 Supplement page. Four " +
                    "combinations are known to disagree with the 2018 table and are pending reconciliation."
            )
        }

        if (edition !in chapter3Editions) {
            out += CodeDataNotice(
                dataset = "Stability and combined-stress provisions (NDS 3.3.3, 3.7, 3.9, Table 3.3.3)",
                status = CodeDataStatus.UNVERIFIED,
                message = "Checked against the NDS 2015 text and found to match it; the $label text has not been " +
                    "seen, so any change in $label is not reflected."
            )
        }

        val ch2Checked = edition in chapter2Editions
        if (methodology == DesignMethodology.LRFD) {
            if (!ch2Checked) {
                out += CodeDataNotice(
                    dataset = "Format conversion KF and resistance factor phi (NDS 2.3.5, 2.3.6)",
                    status = CodeDataStatus.UNVERIFIED,
                    message = "The values (KF 2.54 / 2.70 / 2.88 / 2.40 / 1.67 / 1.76; phi 0.85 / 0.80 / 0.75 / 0.90 / 0.85) " +
                        "were checked against the NDS 2018 and 2015 text; they have not been checked against the $label text."
                )
            }
            if (edition !in lambdaEditions) {
                out += CodeDataNotice(
                    dataset = "Time effect factor \u03bb (NDS Appendix N, Table N3)",
                    status = CodeDataStatus.UNVERIFIED,
                    message = "\u03bb follows Table N3 as checked against the NDS 2015 page image; the $label " +
                        "Appendix N text has not been seen."
                )
            }
            out += CodeDataNotice(
                dataset = "Live load type for \u03bb",
                status = CodeDataStatus.UNVERIFIED,
                message = "Occupancy, storage (0.7) or impact (1.25) live load is chosen in the load duration " +
                    "assumptions; the default is occupancy (0.8)."
            )
        } else {
            if (!ch2Checked) {
                out += CodeDataNotice(
                    dataset = "Load duration factor CD (NDS 2.3.2, Table 2.3.2)",
                    status = CodeDataStatus.UNVERIFIED,
                    message = "The CD values and the shortest-duration-load rule were checked against the NDS 2018 and 2015 text; " +
                        "they have not been checked against the $label text."
                )
            }
            out += CodeDataNotice(
                dataset = "Load type to duration (CD) assignment",
                status = CodeDataStatus.UNVERIFIED,
                message = "Table 2.3.2 lists dead, occupancy live, snow, construction, wind / earthquake and impact loads. " +
                    "Unless changed in the load duration assumptions, roof live load is taken as seven-day (CD 1.25), " +
                    "snow as two months (1.15), occupancy live as ten years (1.0) and rain (no tabulated duration) as 1.0. " +
                    "CD is limited to 1.6 only when the member is marked pressure-treated / fire-retardant-treated."
            )
        }
        return out
    }
}
