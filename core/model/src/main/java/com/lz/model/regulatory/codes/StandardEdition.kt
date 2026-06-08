package com.lz.model.regulatory.codes

import com.lz.model.regulatory.AiscEdition
import com.lz.model.regulatory.asce7.Asce7Edition
import com.lz.model.regulatory.aci318.Aci318Edition
import com.lz.model.regulatory.nds.NdsEdition


sealed class StandardEdition {
    data class Asce7(val edition: Asce7Edition) : StandardEdition()
    data class Aisc360(val edition: AiscEdition) : StandardEdition()
    data class Nds(val edition: NdsEdition) : StandardEdition()
    data class Aci318(val edition: Aci318Edition) : StandardEdition()
    object Unknown : StandardEdition()
}