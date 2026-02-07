package com.damianryan.octopus.model

import com.fasterxml.jackson.annotation.JsonProperty

data class DualFuelDualRate(
    @JsonProperty("electricity_day") val electricityDay: Int = 0,
    @JsonProperty("electricity_night") val electricityNight: Int = 0,
    @JsonProperty("gas_standard") val gasStandard: Int = 0,
)