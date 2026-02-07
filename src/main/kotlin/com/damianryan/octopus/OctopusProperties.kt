package com.damianryan.octopus

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.bind.ConstructorBinding
import java.time.ZonedDateTime

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
data class OctopusProperties @ConstructorBinding constructor(
    val accountNumber: String,
    val apiKey: String,
    val arrangementDate: ZonedDateTime,
    val periodFrom: ZonedDateTime,
    val accountsUrl: String? = null,
    val electricityMpanUrl: String? = null,
    val electricityConsumptionUrl: String? = null,
    val electricityStandingChargesUrl: String? = null,
    val electricityStandardUnitRatesUrl: String? = null,
    val tariffType: String? = null,
    val fixedRateProductCode: String? = null,
    val goProductCode: String? = null,
    val gasConsumptionUrl: String? = null,
)
