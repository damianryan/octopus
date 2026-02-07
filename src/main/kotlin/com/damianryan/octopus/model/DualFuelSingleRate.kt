package com.damianryan.octopus.model

import com.fasterxml.jackson.annotation.JsonProperty

data class DualFuelSingleRate(
    @JsonProperty("electricity_standard") val electricityStandard: Int = 0,
    @JsonProperty("gas_standard") val gasStandard: Int = 0,
)