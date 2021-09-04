package com.damianryan.octopus

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "octopus")
data class OctopusProperties(
    var accountNumber: String? = null,
    var apiKey: String? = null,

    var accountsUrl: String? = null,

    var electricityMpanUrl: String? = null,
    var electricityConsumptionUrl: String? = null,
    var electricityStandingChargesUrl: String? = null,
    var electricityStandardUnitRatesUrl: String? = null,

    var tariffType: String? = null,

    var fixedRateProductCode: String? = null,
    var goProductCode: String? = null,

    var gasConsumptionUrl: String? = null
)