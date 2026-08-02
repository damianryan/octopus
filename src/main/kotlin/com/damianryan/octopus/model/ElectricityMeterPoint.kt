package com.damianryan.octopus.model

import com.fasterxml.jackson.annotation.JsonProperty

@JvmRecord data class ElectricityMeterPoint(
    @JsonProperty("gsp") val region: String? = null,
    val mpan: String,
    @JsonProperty("profile_class") val profileClass: Int = 0,
    @JsonProperty("consumption_standard") val consumptionStandard: Int? = 0,
    @JsonProperty("consumption_day") val consumptionDay: Int? = 0,
    @JsonProperty("consumption_night") val consumptionNight: Int? = 0,
    val meters: List<ElectricityMeter>,
    val agreements: List<Agreement>,
)
