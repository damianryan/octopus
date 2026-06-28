package com.damianryan.octopus.model

import com.damianryan.octopus.twoDP
import com.fasterxml.jackson.annotation.JsonProperty

data class AnnualCost(
    @JsonProperty("annual_cost_inc_vat") val incVAT: Int = 0,
    @JsonProperty("annual_cost_exc_vat") val excVAT: Int = 0,
) {
    override fun toString() = "annual cost of £${twoDP(incVAT.toDouble() / HUNDRED)}p"

    companion object {
        const val HUNDRED = 100.0
    }
}
