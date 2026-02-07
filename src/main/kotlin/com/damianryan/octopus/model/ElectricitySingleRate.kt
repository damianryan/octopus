package com.damianryan.octopus.model

import com.fasterxml.jackson.annotation.JsonProperty

data class ElectricitySingleRate(@JsonProperty("electricity_standard") val electricityStandard: Int = 0)