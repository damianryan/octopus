package com.damianryan.octopus.model

import com.fasterxml.jackson.annotation.JsonProperty
import java.time.Instant

data class Rate(
    @JsonProperty("value_exc_vat") val valueExcVAT: Double? = 0.0,
    @JsonProperty("value_inc_vat") val valueIncVAT: Double? = 0.0,
    @JsonProperty("valid_from") val validFrom: Instant? = null,
    @JsonProperty("valid_to") val validTo: Instant? = null,
) {
    override fun toString() = "Rate(${valueIncVAT}p inc VAT between $validFrom and $validTo)"
}