package com.lz.model.regulatory.aisc

import com.lz.model.regulatory.AiscEdition
import com.lz.model.structural.DesignMethodology

object AiscDesignFactorRegistry {

    fun get(edition: AiscEdition, methodology: DesignMethodology): AiscDesignFactors {
        return when (edition) {
            AiscEdition.AISC_360_16 -> forAisc360(edition, methodology, "AISC 360-16")
            AiscEdition.AISC_360_22 -> forAisc360(edition, methodology, "AISC 360-22")
        }
    }

    /**
     * AISC 360-16 and 360-22 share identical phi/omega values.
     * If a future edition changes a factor, add a separate branch above
     * and override only the changed factor here.
     */
    private fun forAisc360(
        edition: AiscEdition,
        methodology: DesignMethodology,
        prefix: String
    ): AiscDesignFactors {
        return when (methodology) {
            DesignMethodology.LRFD -> AiscDesignFactors(
                edition     = edition,
                methodology = methodology,
                flexure        = DesignFactor(0.90, "$prefix Section F1"),
                shear          = DesignFactor(0.90, "$prefix Section G2.1"),
                compression    = DesignFactor(0.90, "$prefix Section E1"),
                tensionYield   = DesignFactor(0.90, "$prefix Section D2(a)"),
                tensionRupture = DesignFactor(0.75, "$prefix Section D2(b)"),
                torsion        = DesignFactor(0.90, "$prefix Section H3.1")
            )
            DesignMethodology.ASD -> AiscDesignFactors(
                edition     = edition,
                methodology = methodology,
                flexure        = DesignFactor(1.67, "$prefix Section F1"),
                shear          = DesignFactor(1.67, "$prefix Section G2.1"),
                compression    = DesignFactor(1.67, "$prefix Section E1"),
                tensionYield   = DesignFactor(1.67, "$prefix Section D2(a)"),
                tensionRupture = DesignFactor(2.00, "$prefix Section D2(b)"),
                torsion        = DesignFactor(1.67, "$prefix Section H3.1")
            )
        }
    }
}