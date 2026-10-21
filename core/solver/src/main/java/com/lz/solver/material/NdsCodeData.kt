package com.lz.solver.material

import com.lz.model.regulatory.nds.NdsEdition
import com.lz.model.structural.CodeDataNotice
import com.lz.model.structural.CodeDataStatus
import com.lz.model.structural.DesignMethodology

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
 *    NDS 2018 Supplement only.
 *  - Glulam tables: mixed provenance (see the notice text), 2018-era.
 *  - Stability / combined-stress provisions and Table 3.3.3: implemented from
 *    page images of an unspecified edition — not checked per edition.
 *  - CD (ASD) and lambda (LRFD): implemented, unchecked against the pages.
 *  - LRFD format conversion factor KF: not applied.
 */
object NdsCodeData {

    /** Editions the Supplement design values and factors have actually been sourced for. */
    private val designValueEditions = setOf(NdsEdition.NDS_2018)

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
                message = "The $label Supplement values have not been incorporated; the NDS 2018 Supplement " +
                    "values are standing in and may differ. Results are provisional until $label data is added."
            )
        } else if (isGlulam) {
            out += CodeDataNotice(
                dataset = "Reference design values \u2014 glulam (Supplement Tables 5A / 5C)",
                status = CodeDataStatus.UNVERIFIED,
                message = "Mixed provenance: some combinations come from ICC-ES ESR-1940 (2018-era), some from the " +
                    "NDS 2018 Supplement, with a few DF/DF rows corrected against a 2024 Supplement page. Four " +
                    "combinations are known to disagree with the 2018 table and are pending reconciliation."
            )
        }

        out += CodeDataNotice(
            dataset = "Stability and combined-stress provisions (NDS 3.3.3, 3.7, 3.9, Table 3.3.3)",
            status = CodeDataStatus.UNVERIFIED,
            message = "Implemented from page images of an unspecified edition; not yet checked against the $label text."
        )

        if (methodology == DesignMethodology.LRFD) {
            out += CodeDataNotice(
                dataset = "Time effect factor \u03bb (NDS Appendix N, Table N3)",
                status = CodeDataStatus.UNVERIFIED,
                message = "\u03bb is derived per load combination from Table N3 as commonly published; not yet checked " +
                    "against the $label pages."
            )
            out += CodeDataNotice(
                dataset = "LRFD format conversion factor KF (NDS Appendix N)",
                status = CodeDataStatus.NOT_IMPLEMENTED,
                message = "KF is not applied, so LRFD wood capacities are conservative \u2014 roughly 2.5 times too low " +
                    "(KF \u2248 2.4\u20132.9 on the reference values). Use ASD, or treat LRFD wood results as a lower bound."
            )
        } else {
            out += CodeDataNotice(
                dataset = "Load duration factor CD (NDS 2.3.2, Appendix B)",
                status = CodeDataStatus.UNVERIFIED,
                message = "CD is derived per load combination from the NDS duration classes; not yet checked against " +
                    "the $label pages. Rain load has no tabulated duration (CD = 1.0 assumed)."
            )
        }
        return out
    }
}
