package com.lz.solver.material

import com.lz.model.regulatory.nds.MoistureCondition
import com.lz.model.regulatory.nds.NdsAdjustmentFactors
import com.lz.model.regulatory.nds.NdsVersionRegistry
import com.lz.model.regulatory.nds.TemperatureRange
import com.lz.model.structural.MaterialGrade
import com.lz.model.structural.SectionProfile
import com.lz.model.structural.WoodGrade
import com.lz.model.units.inPsi

/**
 * Wet service factors CM, per design value (NDS Supplement Tables 4A / 4B for dimension
 * lumber, 5A / 5C for glulam; identical in the NDS 2015 and 2018 Supplements).
 *
 *   Dimension lumber (in service above 19 % moisture content):
 *     Fb 0.85 (1.0 when Fb x CF <= 1150 psi)   Ft 1.0   Fv 0.97   Fc-perp 0.67
 *     Fc 0.8 (1.0 when Fc x CF <= 750 psi; Southern Pine: when Fc <= 750 psi)   E, Emin 0.9
 *   Glulam, hardwood glulam (16 % or greater):
 *     Fb 0.8   Ft 0.8   Fv 0.875   Fc-perp 0.53   Fc 0.73   E, Emin 0.833
 *
 * Dry service: every CM is 1.0. The old single CM entry (applied to every property alike,
 * which is wrong because the factors differ by property) is read as "wet service".
 */
internal object NdsWetService {

    private const val SAWN_FB_LIMIT_PSI = 1150.0
    private const val SAWN_FC_LIMIT_PSI = 750.0

    private val DENSE = setOf(
        WoodGrade.DENSE_STRUCTURAL_86, WoodGrade.DENSE_STRUCTURAL_72, WoodGrade.DENSE_STRUCTURAL_65
    )

    /** Table 2.3.3 stops at 150 F. */
    const val MAX_TEMPERATURE_F = 150.0

    /** Why the temperature input cannot be designed for, or null. */
    fun temperatureBlockReason(f: NdsAdjustmentFactors): String? =
        if (f.ct == 1.0 && (!f.temperatureF.isFinite() || f.temperatureF > MAX_TEMPERATURE_F))
            "Design temperature ${f.temperatureF} F is above 150 F: NDS Table 2.3.3 (temperature factor Ct) stops " +
                "at 150 F, so wood design is blocked above it."
        else null

    /** Why wet service cannot be designed for this material, or null. */
    fun blockReason(material: MaterialGrade.Wood): String? =
        if (!material.species.isGlulam && material.grade in DENSE)
            "Wet service is blocked for Dense Structural Southern Pine: the Supplement says wet service uses the " +
                "tabulated surfaced-green design values for those grades, which are not incorporated here."
        else null

    /**
     * [f] with the six per-property CM values filled in for the member. Idempotent.
     * [material] must be the design material (values already resolved for the edition).
     */
    fun resolve(
        f: NdsAdjustmentFactors,
        material: MaterialGrade.Wood,
        profile: SectionProfile,
        isGlulam: Boolean
    ): NdsAdjustmentFactors = withTemperature(resolveMoisture(f, material, profile, isGlulam))

    private fun resolveMoisture(
        f: NdsAdjustmentFactors,
        material: MaterialGrade.Wood,
        profile: SectionProfile,
        isGlulam: Boolean
    ): NdsAdjustmentFactors {
        if (!f.isWet) {
            return f.copy(moisture = MoistureCondition.DRY, cm = 1.0,
                cmFb = 1.0, cmFt = 1.0, cmFv = 1.0, cmFcPerp = 1.0, cmFc = 1.0, cmE = 1.0)
        }
        if (isGlulam) {
            return f.copy(moisture = MoistureCondition.WET, cm = 1.0,
                cmFb = 0.8, cmFt = 0.8, cmFv = 0.875, cmFcPerp = 0.53, cmFc = 0.73, cmE = 0.833)
        }
        blockReason(material)?.let { throw CodeDataBlockedException(it) }
        val cfB = resolveSawnCf(f, material, profile, NdsCfProperty.BENDING)
        val southernPine = material.species == com.lz.model.structural.WoodSpecies.SOUTHERN_PINE
        val cfC = if (southernPine) 1.0 else resolveSawnCf(f, material, profile, NdsCfProperty.COMPRESSION)
        val fb = material.referenceBending.inPsi
        val fc = material.referenceCompressionParallel.inPsi
        return f.copy(moisture = MoistureCondition.WET, cm = 1.0,
            cmFb = if (fb * cfB <= SAWN_FB_LIMIT_PSI) 1.0 else 0.85,
            cmFt = 1.0, cmFv = 0.97, cmFcPerp = 0.67,
            cmFc = if (fc * cfC <= SAWN_FC_LIMIT_PSI) 1.0 else 0.8,
            cmE = 0.9)
    }
}

/**
 * [f] with the per-property temperature factors Ct filled in (NDS Table 2.3.3, identical in the
 * 2015 and 2018 texts): T <= 100 F: 1.0; 100 < T <= 125 F: Ft, E, Emin 0.9, and Fb, Fv, Fc, Fc-perp
 * 0.8 dry / 0.7 wet; 125 < T <= 150 F: Ft, E, Emin 0.9, and Fb, Fv, Fc, Fc-perp 0.7 dry / 0.5 wet.
 * A legacy single Ct other than 1.0 is applied to every property, as before.
 */
private fun withTemperature(f: NdsAdjustmentFactors): NdsAdjustmentFactors {
    if (f.ct != 1.0) return f.copy(ctFb = f.ct, ctFt = f.ct, ctFv = f.ct, ctFcPerp = f.ct, ctFc = f.ct, ctE = f.ct)
    NdsWetService.temperatureBlockReason(f)?.let { throw CodeDataBlockedException(it) }
    f.netSection.blockReason()?.let { throw CodeDataBlockedException(it) }
    f.shearNearSupport.blockReason()?.let { throw CodeDataBlockedException(it) }
    val range = when {
        f.temperatureF <= 100.0 -> TemperatureRange.T_100_OR_LESS
        f.temperatureF <= 125.0 -> TemperatureRange.T_101_TO_125
        else -> TemperatureRange.T_126_TO_150
    }
    val t = NdsVersionRegistry.getTemperatureFactors(range, f.isWet)
    return f.copy(ctFb = t.ctBending, ctFt = t.ctTension, ctFv = t.ctBending, ctFcPerp = t.ctCompPerp,
        ctFc = t.ctBending, ctE = t.ctModulus)
}

/**
 * The modulus of elasticity for deflection: the edition's tabulated E for this member
 * x CM x Ct x Ci (NDS E' = E CM Ct Ci; wet service lowers E by 0.9 / 0.833). CD does not
 * apply to E.
 */
fun ndsDeflectionModulusPsi(
    material: MaterialGrade.Wood,
    profile: SectionProfile,
    factors: NdsAdjustmentFactors,
    edition: com.lz.model.regulatory.nds.NdsEdition
): Double {
    val dm = resolveDesignMaterial(material, profile, edition)
    val f = NdsWetService.resolve(factors, dm, profile, material.species.isGlulam)
    return dm.modulusOfElasticity.inPsi * f.cmE * f.ctE * f.ci
}
