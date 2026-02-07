package com.damianryan.octopus.model

import com.fasterxml.jackson.annotation.JsonProperty

data class SampleQuote(
    @JsonProperty("electricity_single_rate") val electricitySingleRate: AnnualCost? = null,
    @JsonProperty("electricity_dual_rate") val electricityDualRate: AnnualCost? = null,
    @JsonProperty("dual_fuel_single_rate") val dualFuelSingleRate: AnnualCost? = null,
    @JsonProperty("dual_fuel_dual_rate") val dualFuelDualRate: AnnualCost? = null,
)