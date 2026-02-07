package com.damianryan.octopus.model

import com.fasterxml.jackson.annotation.JsonProperty

data class ElectricityMeterPoint(
    @JsonProperty("gsp") val region: String? = null,
    val mpan: String? = null,
    @JsonProperty("profile_class") val profileClass: Int = 0,
    @JsonProperty("consumption_standard") val consumptionStandard: Int? = 0,
    @JsonProperty("consumption_day") val consumptionDay: Int? = 0,
    @JsonProperty("consumption_night") val consumptionNight: Int? = 0,
    val meters: List<ElectricityMeter>? = null,
    val agreements: List<Agreement>? = null,
)