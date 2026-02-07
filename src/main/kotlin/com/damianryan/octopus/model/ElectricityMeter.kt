package com.damianryan.octopus.model

import com.fasterxml.jackson.annotation.JsonProperty

data class ElectricityMeter(
    @JsonProperty("serial_number") val serialNumber: String? = null,
    val registers: List<Register>? = null,
)