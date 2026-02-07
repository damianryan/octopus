package com.damianryan.octopus.model

import com.fasterxml.jackson.annotation.JsonProperty
import java.time.Instant

data class Agreement(
    @JsonProperty("tariff_code") val tariffCode: String? = null,
    @JsonProperty("valid_from") val validFrom: Instant? = null,
    @JsonProperty("valid_to") val validTo: Instant? = null,
)