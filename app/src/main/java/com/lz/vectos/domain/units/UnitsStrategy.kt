package com.lz.vectos.domain.units

/**
 * INTERNAL BASE UNITS STRATEGY
 * 
 * To ensure consistency and prevent unit-related calculation errors, all internal 
 * domain logic and calculations MUST operate on a single, unified base unit system.
 * 
 * The chosen internal system is SI (Metric).
 * 
 * Base Units:
 * - Length: Meters (m)
 * - Force: Newtons (N)
 * - Mass: Kilograms (kg)
 * - Time: Seconds (s)
 * 
 * Derived Base Units:
 * - Stress / Modulus: Pascals (Pa) -> (N/m²)
 * - Moment / Torque: Newton-meters (N·m)
 * - Area: Square meters (m²)
 * - Moment of Inertia: Meters to the fourth power (m⁴)
 * - Distributed Load: Newtons per meter (N/m)
 * 
 * Conversion Policy:
 * 1. All inputs (Metric or Imperial) are converted to these Base Units immediately 
 *    upon entering the domain layer.
 * 2. All calculations are performed using these Base Units.
 * 3. Results are stored in Base Units and converted to the requested display units 
 *    only at the UI/Presentation layer.
 */
object UnitsStrategy {
    const val LENGTH_BASE = "Meters (m)"
    const val FORCE_BASE = "Newtons (N)"
    const val STRESS_BASE = "Pascals (Pa)"
    const val MODULUS_BASE = "Pascals (Pa)"
    const val MOMENT_BASE = "Newton-meters (Nm)"
}
