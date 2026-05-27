package com.lz.vectos.domain.beam

/**
 * Categorizes materials for standard selection and design logic.
 * This is used to map building code requirements to specific design standards.
 */
enum class MaterialType(
    val defaultModulusOfElasticityPsi: Double,
    val defaultDensityPcf: Double
) {
    STEEL(29000000.0, 490.0),
    WOOD(1600000.0, 35.0),
    CONCRETE(3600000.0, 150.0),
    COLDFORM(29000000.0, 490.0),
    MASONRY(1500000.0, 120.0),
    ALUMINUM(10000000.0, 165.0)
}
