package com.damianryan.octopus.model

import com.fasterxml.jackson.annotation.JsonProperty
import java.time.Instant

data class Agreement(
    @JsonProperty("tariff_code") val tariffCode: String,
    @JsonProperty("valid_from") val validFrom: Instant,
    @JsonProperty("valid_to") val validTo: Instant? = null,
) : Comparable<Agreement> {
    override fun compareTo(other: Agreement): Int =
        compareValuesBy(other, this, Agreement::validFrom, Agreement::validTo)

    val electricityRegion: String
        get() = "_${tariffCode.substringAfterLast("-")}"

    val fuelType: String
        get() = tariffCode.substring(0, 1)

    val registerType: String
        get() = tariffCode.substring(2, 4)

    val productCode: String
        get() = tariffCode.substringBeforeLast("-").substring(5)
}
