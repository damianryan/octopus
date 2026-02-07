package com.damianryan.octopus.model

import com.fasterxml.jackson.annotation.JsonProperty

data class Tariff(
    val code: String? = null,
    @JsonProperty("standing_charge_exc_vat") val standingChargeExcVAT: Double = 0.0,
    @JsonProperty("standing_charge_inc_vat") val standingChargeIncVAT: Double = 0.0,
    @JsonProperty("online_discount_exc_vat") val onlineDiscountExcVAT: Double = 0.0,
    @JsonProperty("online_discount_inc_vat") val onlineDiscountIncVAT: Double = 0.0,
    @JsonProperty("dual_fuel_discount_exc_vat") val dualFuelDiscountExcVAT: Double = 0.0,
    @JsonProperty("dual_fuel_discount_inc_vat") val dualFuelDiscountIncVAT: Double = 0.0,
    @JsonProperty("exit_fees_exc_vat") val exitFeesExcVAT: Double = 0.0,
    @JsonProperty("exit_fees_inc_vat") val exitFeesIncVAT: Double = 0.0,
    val links: List<Link>? = null,
    @JsonProperty("standard_unit_rate_exc_vat") val standardUnitRateExcVAT: Double = 0.0,
    @JsonProperty("standard_unit_rate_inc_vat") val standardUnitRateIncVAT: Double = 0.0,
) {
    override fun toString() =
        "Tariff(standing charge=${standingChargeIncVAT}p inc VAT per day, " +
            "online discount=${onlineDiscountIncVAT}p inc VAT, " +
            "dual fuel discount=${dualFuelDiscountIncVAT}p inc VAT, exit fees=${exitFeesExcVAT}p inc VAT, " +
            "standard unit rate=${standardUnitRateIncVAT}p inc VAT)"
}