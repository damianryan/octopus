package com.damianryan.octopus.model

import com.fasterxml.jackson.annotation.JsonProperty

data class GasMeterPoint(
    val mprn: String? = null,
    @JsonProperty("consumption_standard") val consumptionStandard: Int = 0,
    val meters: List<GasMeter>? = null,
    val agreements: List<Agreement>? = null,
)