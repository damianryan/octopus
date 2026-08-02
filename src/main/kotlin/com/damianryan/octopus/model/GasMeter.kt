package com.damianryan.octopus.model

import com.fasterxml.jackson.annotation.JsonProperty

@JvmRecord data class GasMeter(@JsonProperty("serial_number") val serialNumber: String)
