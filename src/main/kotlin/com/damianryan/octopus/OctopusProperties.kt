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
 * @property electricityProductCode electricity product code
 * @property electricityRegion electricity region (also known as distribution area, DNO region, or GSP group)
 */
@ConfigurationProperties("octopus")
data class OctopusProperties
@ConstructorBinding
constructor(
    val accountNumber: String,
    val apiKey: String,
    val arrangementDate: ZonedDateTime,
    val periodFrom: ZonedDateTime,
    val baseUrl: String,
    val electricityProductCode: String,
    val electricityRegion: String
) {
    val accountsUrl = "/accounts/$accountNumber"
}
