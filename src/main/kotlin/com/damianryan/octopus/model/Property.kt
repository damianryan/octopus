package com.damianryan.octopus.model

import com.fasterxml.jackson.annotation.JsonProperty
import java.time.Instant

data class Property(
    val id: Int = 0,
    @JsonProperty("moved_in_at") val movedInAt: Instant? = null,
    @JsonProperty("moved_out_at") val movedOutAt: Instant? = null,
    @JsonProperty("address_line_1") val addressLine1: String? = null,
    @JsonProperty("address_line_2") val addressLine2: String? = null,
    @JsonProperty("address_line_3") val addressLine3: String? = null,
    val town: String? = null,
    val county: String? = null,
    val postcode: String? = null,
    @JsonProperty("electricity_meter_points") val electricityMeterPoints: List<ElectricityMeterPoint>? = null,
    @JsonProperty("gas_meter_points") val gasMeterPoints: List<GasMeterPoint>? = null,
)