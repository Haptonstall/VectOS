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
 *  - CD (Table 2.3.2, §2.3.2), KF (Table 2.3.5) and phi (Table 2.3.6):
 *    checked against the NDS 2018 text published by the AWC; not checked for
 *    2015 or 2024.
 *  - Lambda (Appendix N, Table N3): implemented from published summaries of
 *    the table; Appendix N has not been checked against any edition's text.
 *  - Stability / combined-stress provisions and Table 3.3.3: implemented from
 *    page images of an unspecified edition — not checked per edition.
 */
object NdsCodeData {

    /** Editions the Supplement design values and factors have actually been sourced for. */
    private val designValueEditions = setOf(NdsEdition.NDS_2018)

    /** Editions whose Chapter 2 (CD, KF, phi) has been checked against the published text. */
    private val chapter2Editions = setOf(NdsEdition.NDS_2018)

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

        val ch2Checked = edition in chapter2Editions
        if (methodology == DesignMethodology.LRFD) {
            if (!ch2Checked) {
                out += CodeDataNotice(
                    dataset = "Format conversion KF and resistance factor phi (NDS 2.3.5, 2.3.6)",
                    status = CodeDataStatus.UNVERIFIED,
                    message = "The values (KF 2.54 / 2.70 / 2.88 / 2.40 / 1.67 / 1.76; phi 0.85 / 0.80 / 0.75 / 0.90 / 0.85) " +
                        "were checked against the NDS 2018 text; they have not been checked against the $label text."
                )
            }
            out += CodeDataNotice(
                dataset = "Time effect factor \u03bb (NDS Appendix N, Table N3)",
                status = CodeDataStatus.UNVERIFIED,
                message = "\u03bb is derived per load combination from Table N3 as published in design references " +
                    "(0.6 dead only, 0.8 gravity loads, 1.0 wind or earthquake at a strength-level factor); " +
                    "Appendix N has not been checked against the $label text. Occupancy live load is assumed " +
                    "(storage 0.7 and impact 1.25 are not distinguished)."
            )
        } else {
            if (!ch2Checked) {
                out += CodeDataNotice(
                    dataset = "Load duration factor CD (NDS 2.3.2, Table 2.3.2)",
                    status = CodeDataStatus.UNVERIFIED,
                    message = "The CD values and the shortest-duration-load rule were checked against the NDS 2018 text; " +
                        "they have not been checked against the $label text."
                )
            }
            out += CodeDataNotice(
                dataset = "Load type to duration (CD) assignment",
                status = CodeDataStatus.UNVERIFIED,
                message = "Table 2.3.2 lists dead, occupancy live, snow, construction, wind / earthquake and impact loads. " +
                    "Roof live load is treated here as seven-day (construction) duration, rain load has no tabulated duration " +
                    "(CD = 1.0 assumed), and snow is taken at two months. CD above 1.0 is not restricted for members treated " +
                    "with water-borne preservatives or fire retardant (CD must not exceed 1.6 there; Table 2.3.2 note 2)."
            )
        }
        return out
    }
}
