package com.lz.model.regulatory.loads

import com.lz.model.regulatory.LoadCategory
import com.lz.model.structural.DesignMethodology

/** An unfactored (service-level) load combination used for deflection: factors on the load categories. */
data class ServiceCombination(val name: String, val factors: Map<LoadCategory, Double>)

/**
 * The service combinations behind the "Total Load" deflection check.
 *
 * Deflection is a service-level response, so it is evaluated under unfactored combinations, not under the
 * factored strength combination that governs the member's strength. For an ASCE 7 combination set the service
 * combinations are the allowable-stress gravity combinations of the same edition (no wind or seismic): ASCE 7-10
 * and 7-16 §2.4.1 Eq. 1 to 4 (D; D + L; D + (Lr or S or R); D + 0.75L + 0.75(Lr or S or R)) and ASCE 7-22
 * §2.4.1 Eq. 1a to 4a, where snow is 0.7S. Wind has its own single-category check and is not included here.
 * Without a recognised ASCE set, D alone and D plus each of L, Lr, S and R at 1.0 are used, and the basis says so.
 *
 * Creep (NDS 3.5.2 / the IBC Table 1604.3 long-term footnote) is not applied: these are immediate deflections.
 */
object ServiceabilityCombinations {

    data class Result(val combinations: List<ServiceCombination>, val basis: String)

    private val genericCategories = listOf(LoadCategory.LIVE, LoadCategory.ROOF_LIVE, LoadCategory.SNOW, LoadCategory.RAIN)

    fun forCombinationSet(setId: String?): Result {
        for (e in AsceEdition.values()) {
            if (setId == null) break
            val matches = DesignMethodology.values().any { setId == AsceLoadCombinationCatalog.setId(e, it) }
            if (!matches) continue
            val asd = AsceLoadCombinationCatalog.combinations(e, DesignMethodology.ASD)
                .filter { LoadCategory.WIND !in it.factors && LoadCategory.SEISMIC !in it.factors }
            return Result(
                asd.map { ServiceCombination(it.name, it.factors) },
                "Unfactored service combinations of ${e.sourceName} (${e.asdSection} gravity combinations, no wind or " +
                    "seismic); creep not applied (immediate deflection)."
            )
        }
        val generic = listOf(ServiceCombination("D", mapOf(LoadCategory.DEAD to 1.0))) +
            genericCategories.map { ServiceCombination("D + ${it.shortLabel}", mapOf(LoadCategory.DEAD to 1.0, it to 1.0)) }
        return Result(
            generic,
            "ASSUMED: no ASCE 7 combination set recognised, so D alone and D plus each of L, Lr, S and R at 1.0 are " +
                "used as the unfactored service combinations; creep not applied (immediate deflection)."
        )
    }
}
