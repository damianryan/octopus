package com.damianryan.octopus.model

import com.fasterxml.jackson.annotation.JsonProperty

data class GasMeter(@JsonProperty("serial_number") val serialNumber: String? = null)