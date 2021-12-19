package com.damianryan.octopus

import com.fasterxml.jackson.annotation.JsonProperty
import java.time.Instant

@Suppress("unused")
data class Account(
    var number: String? = null,
    var properties: List<Property>? = null
)

data class Property(
    var id: Int = 0,
    @JsonProperty("moved_in_at") var movedInAt: Instant? = null,
    @JsonProperty("moved_out_at") var movedOutAt: Instant? = null,
    @JsonProperty("address_line_1") var addressLine1: String? = null,
    @JsonProperty("address_line_2") var addressLine2: String? = null,
    @JsonProperty("address_line_3") var addressLine3: String? = null,
    var town: String? = null,
    var county: String? = null,
    var postcode: String? = null,
    @JsonProperty("electricity_meter_points") var electricityMeterPoints: List<ElectricityMeterPoint>? = null,
    @JsonProperty("gas_meter_points") var gasMeterPoints: List<GasMeterPoint>? = null
)

data class ElectricityMeterPoint(
    @JsonProperty("gsp") var region: String? = null,
    var mpan: String? = null,
    @JsonProperty("profile_class") var profileClass: Int = 0,
    @JsonProperty("consumption_standard") var consumptionStandard: Int = 0,
    @JsonProperty("consumption_day") var consumptionDay: Int = 0,
    @JsonProperty("consumption_night") var consumptionNight: Int = 0,
    var meters: List<ElectricityMeter>? = null,
    var agreements: List<Agreement>? = null
)

data class ElectricityMeter(
    @JsonProperty("serial_number") var serialNumber: String? = null,
    var registers: List<Register>? = null
)

data class Register(
    var identifier: String? = null,
    var rate: String? = null,
    @JsonProperty("is_settlement_register") var settlementRegister: Boolean = false
)

data class GasMeterPoint(
    var mprn: String? = null,
    @JsonProperty("consumption_standard") var consumptionStandard: Int = 0,
    var meters: List<GasMeter>? = null,
    var agreements: List<Agreement>? = null
)

data class GasMeter(
    @JsonProperty("serial_number") var serialNumber: String? = null
)

data class Agreement(
    @JsonProperty("tariff_code") var tariffCode: String? = null,
    @JsonProperty("valid_from") var validFrom: Instant? = null,
    @JsonProperty("valid_to") var validTo: Instant? = null
)

class Consumption : Page<Reading?>()

data class Reading(
    var consumption: Double = 0.0,
    @JsonProperty("interval_start") var from: Instant? = null,
    @JsonProperty("interval_end") var to: Instant? = null
) : Comparable<Reading> {
    override fun compareTo(other: Reading): Int {
        return from!!.compareTo(other.from!!)
    }
}

open class Page<T> {
    var count: Long = 0
    var next: String? = null
    var previous: String? = null
    @JsonProperty("results") var content: List<T>? = null

    override fun toString(): String {
        return "Consumption(previous: $previous, next: $next, content: ${content?.size}/$count)"
    }
}

data class Link(val href: String)

@Suppress("unused")
class Products : Page<Product?>()

data class Product(
    var code: String? = null,
    var direction: String? = null,
    @JsonProperty("full_name") var fullName: String? = null,
    @JsonProperty("display_name") var displayName: String? = null,
    @JsonProperty("is_variable") var variable: Boolean = false,
    @JsonProperty("is_green") var green: Boolean = false,
    @JsonProperty("is_tracker") var tracker: Boolean = false,
    @JsonProperty("is_prepay") var prepay: Boolean = false,
    @JsonProperty("is_business") var business: Boolean = false,
    @JsonProperty("is_restricted") var restricted: Boolean = false,
    var term: Int = 0,
    @JsonProperty("available_from") var availableFrom: Instant? = null,
    @JsonProperty("available_to") var availableTo: Instant? = null,
    var links: List<Link>? = null,
    var brand: String? = null,
    @JsonProperty("tariffs_active_at") var tariffsActiveAt: Instant? = null,
    @JsonProperty("single_register_electricity_tariffs") var singleRegisterElectricityTariffs: Map<String, Map<String, Tariff>>? = null,
    @JsonProperty("dual_register_electricity_tariffs") var dualRegisterElectricityTariffs: Map<String, Map<String, Tariff>>? = null,
    @JsonProperty("single_register_gas_tariffs") var singleRegisterGasTariffs: Map<String, Map<String, Tariff>>? = null,
    @JsonProperty("sample_quotes") var sampleQuotes: Map<String, Map<String, SampleQuote>>? = null,
    @JsonProperty("sample_consumption") var sampleConsumption: SampleConsumption? = null
) : Comparable<Product> {
    override fun compareTo(other: Product): Int {
        return fullName!!.compareTo(other.fullName!!)
    }
}

@Suppress("unused")
class StandingCharge : Page<Rate?>()

@Suppress("unused")
class StandardUnitRate : Page<Rate?>()

data class Rate(
    @JsonProperty("value_exc_vat") var valueExcVAT: Double? = 0.0,
    @JsonProperty("value_inc_vat") var valueIncVAT: Double? = 0.0,
    @JsonProperty("valid_from") var validFrom: Instant? = null,
    @JsonProperty("valid_to") var validTo: Instant? = null
) {
    override fun toString()= "Rate(${valueIncVAT}p inc VAT between $validFrom and $validTo)"
}

data class Tariff(
    var code: String? = null,
    @JsonProperty("standing_charge_exc_vat") var standingChargeExcVAT: Double = 0.0,
    @JsonProperty("standing_charge_inc_vat") var standingChargeIncVAT: Double = 0.0,
    @JsonProperty("online_discount_exc_vat") var onlineDiscountExcVAT: Double = 0.0,
    @JsonProperty("online_discount_inc_vat") var onlineDiscountIncVAT: Double = 0.0,
    @JsonProperty("dual_fuel_discount_exc_vat") var dualFuelDiscountExcVAT: Double = 0.0,
    @JsonProperty("dual_fuel_discount_inc_vat") var dualFuelDiscountIncVAT: Double = 0.0,
    @JsonProperty("exit_fees_exc_vat") var exitFeesExcVAT: Double = 0.0,
    @JsonProperty("exit_fees_inc_vat") var exitFeesIncVAT: Double = 0.0,
    var links: List<Link>? = null,
    @JsonProperty("standard_unit_rate_exc_vat") var standardUnitRateExcVAT: Double = 0.0,
    @JsonProperty("standard_unit_rate_inc_vat") var standardUnitRateIncVAT: Double = 0.0
) {
    override fun toString() = "Tariff(standing charge=${standingChargeIncVAT}p inc VAT per day, online discount=${onlineDiscountIncVAT}p inc VAT, " +
            "dual fuel discount=${dualFuelDiscountIncVAT}p inc VAT, exit fees=${exitFeesExcVAT}p inc VAT, " +
            "standard unit rate=${standardUnitRateIncVAT}p inc VAT)"
}

data class SampleQuote(
    @JsonProperty("electricity_single_rate") var electricitySingleRate: AnnualCost? = null,
    @JsonProperty("electricity_dual_rate") var electricityDualRate: AnnualCost? = null,
    @JsonProperty("dual_fuel_single_rate") var dualFuelSingleRate: AnnualCost? = null,
    @JsonProperty("dual_fuel_dual_rate") var dualFuelDualRate: AnnualCost? = null
)

data class AnnualCost(
    @JsonProperty("annual_cost_inc_vat") var incVAT: Int = 0,
    @JsonProperty("annual_cost_exc_vat") var excVAT: Int = 0
) {
    override fun toString() = "annual cost of £${twoDP(incVAT.toDouble() / 100)}p"
}

data class SampleConsumption(
    @JsonProperty("electricity_single_rate") var electricitySingleRate: ElectricitySingleRate? = null,
    @JsonProperty("electricity_dual_rate") var electricityDualRate: ElectricityDualRate? = null,
    @JsonProperty("dual_fuel_single_rate") var dualFuelSingleRate: DualFuelSingleRate? = null,
    @JsonProperty("dual_fuel_dual_rate") var dualFuelDualRate: DualFuelDualRate? = null
)

data class ElectricityDualRate(
    @JsonProperty("electricity_day") var electricityDay: Int = 0,
    @JsonProperty("electricity_night") var electricityNight: Int = 0
)

data class ElectricitySingleRate(
    @JsonProperty("electricity_standard") var electricityStandard: Int = 0
)

data class DualFuelDualRate(
    @JsonProperty("electricity_day") var electricityDay: Int = 0,
    @JsonProperty("electricity_night") var electricityNight: Int = 0,
    @JsonProperty("gas_standard") var gasStandard: Int = 0
)

data class DualFuelSingleRate(
    @JsonProperty("electricity_standard") var electricityStandard: Int = 0,
    @JsonProperty("gas_standard") var gasStandard: Int = 0
)