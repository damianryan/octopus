package com.damianryan.octopus

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "octopus")
data class OctopusProperties(
    var accountUrl: String? = null,
    var productsUrl: String? = null,
    var electricityConsumptionUrl: String? = null,
    var electricityTariffsUrl: String? = null,
    var gasConsumptionUrl: String? = null,
    var gasTariffsUrl: String? = null,
    var greenFixedRateProductsUrl: String? = null,
    var apiKey: String? = null,
    var region: String? = null,
    var tariffType: String? = null,
    var standingChargesPath: String? = null,
    var standardUnitRatesPath: String? = null,
    var fixedRateProductCode: String? = null,
    var goProductCode: String? = null
)