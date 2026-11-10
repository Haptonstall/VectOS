package com.lz.solver.capacity

import com.lz.model.structural.DesignPropertyGroup
import com.lz.model.structural.DesignPropertyRow
import com.lz.model.structural.MaterialGrade
import com.lz.model.structural.SectionProfile
import com.lz.model.structural.SteelProfile
import com.lz.model.units.inIn2
import com.lz.model.units.inIn3
import com.lz.model.units.inIn4
import com.lz.model.units.inInches
import com.lz.model.units.inPsi
import java.util.Locale

/**
 * "Selected Properties" groups (material and section) for any material, so the properties the
 * code checks use are at hand whether or not a material-specific design calculator exists.
 * Values are the profile's and the grade's own; nothing is looked up or filled in here.
 */
object SelectedPropertiesBuilder {

    private fun f(v: Double, d: Int) = String.format(Locale.US, "%.${d}f", v)

    private fun row(symbol: String, description: String, value: String) =
        DesignPropertyRow(symbol, description, value)

    fun forSection(profile: SectionProfile?, material: MaterialGrade?): List<DesignPropertyGroup> {
        val groups = mutableListOf<DesignPropertyGroup>()
        if (material != null) groups += materialGroup(material)
        if (profile != null) groups += sectionGroup(profile, material)
        return groups
    }

    private fun materialGroup(m: MaterialGrade): DesignPropertyGroup {
        val rows = mutableListOf(
            row("Grade", "Material grade", m.name),
            row("Type", "Material type", m.type.name)
        )
        when (m) {
            is MaterialGrade.Steel -> {
                rows += row("Fy", "Yield strength", "${f(m.yieldStrength.inPsi / 1000.0, 1)} ksi")
                rows += row("Fu", "Tensile strength", "${f(m.ultimateStrength.inPsi / 1000.0, 1)} ksi")
            }
            is MaterialGrade.Aluminum -> {
                rows += row("Fy", "Yield strength", "${f(m.yieldStrength.inPsi / 1000.0, 1)} ksi")
                rows += row("Fu", "Tensile strength", "${f(m.ultimateStrength.inPsi / 1000.0, 1)} ksi")
            }
            is MaterialGrade.Wood -> {
                rows += row("Species", "Species / combination", m.species.name)
            }
            else -> Unit
        }
        rows += row("E", "Modulus of elasticity", "${f(m.modulusOfElasticity.inPsi / 1000.0, 0)} ksi")
        rows += row("G", "Shear modulus", "${f(m.shearModulus.inPsi / 1000.0, 0)} ksi")
        rows += row("\u03C1", "Density", "${f(m.densityPcf, 1)} pcf")
        return DesignPropertyGroup("Material", rows)
    }

    private fun sectionGroup(p: SectionProfile, m: MaterialGrade?): DesignPropertyGroup {
        val s = p.propertiesStrongAxis
        val w = p.propertiesWeakAxis
        val rows = mutableListOf(
            row("Size", "Section", p.designation),
            row("d", "Overall depth", "${f(p.depth.inInches, 3)} in"),
            row("A", "Gross area", "${f(p.area.inIn2, 3)} in\u00B2")
        )
        if (p is SteelProfile) {
            rows += row("bf", "Flange width", "${f(p.flangeWidth.inInches, 3)} in")
            rows += row("tf", "Flange thickness", "${f(p.flangeThickness.inInches, 3)} in")
            rows += row("tw", "Web thickness", "${f(p.webThickness.inInches, 3)} in")
        }
        rows += row("Ix", "Moment of inertia, strong axis", "${f(s.i.inIn4, 2)} in\u2074")
        rows += row("Sx", "Elastic section modulus, strong axis", "${f(s.s.inIn3, 2)} in\u00B3")
        rows += row("Zx", "Plastic section modulus, strong axis", "${f(s.z.inIn3, 2)} in\u00B3")
        rows += row("rx", "Radius of gyration, strong axis", "${f(s.r.inInches, 3)} in")
        rows += row("Iy", "Moment of inertia, weak axis", "${f(w.i.inIn4, 2)} in\u2074")
        rows += row("Sy", "Elastic section modulus, weak axis", "${f(w.s.inIn3, 2)} in\u00B3")
        rows += row("Zy", "Plastic section modulus, weak axis", "${f(w.z.inIn3, 2)} in\u00B3")
        rows += row("ry", "Radius of gyration, weak axis", "${f(w.r.inInches, 3)} in")
        if (p is SteelProfile) {
            rows += row("J", "Torsional constant", "${f(p.torsionalConstantJ, 3)} in\u2074")
            rows += row("Cw", "Warping constant", "${f(p.warpingConstantCw, 0)} in\u2076")
        }
        rows += row("w", "Weight per foot", "${f(p.getWeightLbFt(m?.densityPcf ?: p.materialType.defaultDensityPcf), 2)} lb/ft")
        return DesignPropertyGroup("Section", rows)
    }
}
