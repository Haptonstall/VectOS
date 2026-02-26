package com.lz.vectos.domain.units

import org.junit.Assert.assertEquals
import org.junit.Test

class UnitConverterTest {

    private val delta = 1e-6

    @Test
    fun length_conversions_toBase() {
        assertEquals(1.0, UnitConverter.toInternalBase(1.0, LengthUnit.METERS).meters, delta)
        assertEquals(0.001, UnitConverter.toInternalBase(1.0, LengthUnit.MILLIMETERS).meters, delta)
        assertEquals(0.3048, UnitConverter.toInternalBase(1.0, LengthUnit.FEET).meters, delta)
        assertEquals(0.0254, UnitConverter.toInternalBase(1.0, LengthUnit.INCHES).meters, delta)
    }

    @Test
    fun length_conversions_fromBase() {
        val meter = 1.0.meters
        assertEquals(1.0, UnitConverter.toDisplayValue(meter, LengthUnit.METERS), delta)
        assertEquals(1000.0, UnitConverter.toDisplayValue(meter, LengthUnit.MILLIMETERS), delta)
        assertEquals(3.28084, UnitConverter.toDisplayValue(meter, LengthUnit.FEET), delta)
        assertEquals(39.3701, UnitConverter.toDisplayValue(meter, LengthUnit.INCHES), delta)
    }

    @Test
    fun force_conversions() {
        assertEquals(1.0, UnitConverter.toInternalBase(1.0, ForceUnit.NEWTONS).newtons, delta)
        assertEquals(1000.0, UnitConverter.toInternalBase(1.0, ForceUnit.KILONEWTONS).newtons, delta)
        assertEquals(4.44822, UnitConverter.toInternalBase(1.0, ForceUnit.POUNDS_FORCE).newtons, delta)
    }

    @Test
    fun pressure_conversions() {
        assertEquals(1.0, UnitConverter.toInternalBase(1.0, PressureUnit.PASCALS).pascals, delta)
        assertEquals(1e6, UnitConverter.toInternalBase(1.0, PressureUnit.MEGA_PASCALS).pascals, delta)
        assertEquals(1e9, UnitConverter.toInternalBase(1.0, PressureUnit.GIGA_PASCALS).pascals, delta)
        assertEquals(6894.757, UnitConverter.toInternalBase(1.0, PressureUnit.PSI).pascals, delta)
    }
}
