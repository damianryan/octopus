package com.damianryan.octopus

import org.springframework.boot.context.properties.ConfigurationProperties

/**
 * Octopus application properties.
 *
 * @property accountNumber account number
 * @property apiKey API key
 * @property accountsUrl Octopus API accounts endpoint
 * @property electricityMpanUrl Octopus API electricity MPAN endpoint
 * @property electricityConsumptionUrl Octopus API electricity consumption endpoint
 * @property electricityStandingChargesUrl Octopus API electricity standing charges endpoint
 * @property electricityConsumptionUrl Octopus API electricity standard unit rates endpoint
 * @property tariffType tariff type
 * @property fixedRateProductCode fixed rate product code
 * @property goProductCode go product code
 * @property gasConsumptionUrl Octopus API gas consumption endpoint
 */
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
    var gasConsumptionUrl: String? = null,
)
