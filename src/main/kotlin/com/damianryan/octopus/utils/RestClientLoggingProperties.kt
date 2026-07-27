package com.damianryan.octopus.utils

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.bind.ConstructorBinding

/**
 * Rest client logging configuration.
 *
 * @property enabled whether requests and responses should be logged at all
 * @property headers whether request and response headers should be logged
 */
@ConfigurationProperties("rest-client.logging")
data class RestClientLoggingProperties
@ConstructorBinding
constructor(val enabled: Boolean = true, val headers: Boolean = false) {
    @Suppress("unused") constructor() : this(true, false)
}
