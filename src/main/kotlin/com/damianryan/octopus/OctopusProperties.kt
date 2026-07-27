package com.damianryan.octopus

import java.time.ZonedDateTime
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.bind.ConstructorBinding

/**
 * Octopus application properties.
 *
 * @property accountNumber account number
 * @property apiKey API key
 * @property arrangementDate date when arrangement was made with Octopus
 * @property periodFrom date when switch to Octopus actually started
 * @property baseUrl Octopus API base URL
 */
@ConfigurationProperties(prefix = "octopus")
data class OctopusProperties
@ConstructorBinding
constructor(
    val accountNumber: String = "TBA",
    val apiKey: String = "TBA",
    val arrangementDate: ZonedDateTime,
    val periodFrom: ZonedDateTime,
    val baseUrl: String,
) {
    val accountsUrl = "/accounts/$accountNumber"
}
