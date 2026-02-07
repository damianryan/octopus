package com.damianryan.octopus.model

import com.damianryan.octopus.model.DualFuelSingleRate
import com.fasterxml.jackson.annotation.JsonProperty

data class SampleConsumption(
    @JsonProperty("electricity_single_rate") val electricitySingleRate: ElectricitySingleRate? = null,
    @JsonProperty("electricity_dual_rate") val electricityDualRate: ElectricityDualRate? = null,
    @JsonProperty("dual_fuel_single_rate") val dualFuelSingleRate: DualFuelSingleRate? = null,
    @JsonProperty("dual_fuel_dual_rate") val dualFuelDualRate: DualFuelDualRate? = null,
)