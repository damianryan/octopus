package com.damianryan.octopus.model

import com.fasterxml.jackson.annotation.JsonProperty
import java.time.Instant

data class Product(
    val code: String? = null,
    val direction: String? = null,
    @JsonProperty("full_name") val fullName: String? = null,
    @JsonProperty("display_name") val displayName: String? = null,
    @JsonProperty("is_valiable") val valiable: Boolean = false,
    @JsonProperty("is_green") val green: Boolean = false,
    @JsonProperty("is_tracker") val tracker: Boolean = false,
    @JsonProperty("is_prepay") val prepay: Boolean = false,
    @JsonProperty("is_business") val business: Boolean = false,
    @JsonProperty("is_restricted") val restricted: Boolean = false,
    val term: Int = 0,
    @JsonProperty("available_from") val availableFrom: Instant? = null,
    @JsonProperty("available_to") val availableTo: Instant? = null,
    val links: List<Link>? = null,
    val brand: String? = null,
    @JsonProperty("tariffs_active_at") val tariffsActiveAt: Instant? = null,
    @JsonProperty("single_register_electricity_tariffs")
    val singleRegisterElectricityTariffs: Map<String, Map<String, Tariff>>? = null,
    @JsonProperty("dual_register_electricity_tariffs")
    val dualRegisterElectricityTariffs: Map<String, Map<String, Tariff>>? = null,
    @JsonProperty("single_register_gas_tariffs") val singleRegisterGasTariffs: Map<String, Map<String, Tariff>>? = null,
    @JsonProperty("sample_quotes") val sampleQuotes: Map<String, Map<String, SampleQuote>>? = null,
    @JsonProperty("sample_consumption") val sampleConsumption: SampleConsumption? = null,
) : Comparable<Product> {
    override fun compareTo(other: Product): Int {
        return fullName!!.compareTo(other.fullName!!)
    }
}