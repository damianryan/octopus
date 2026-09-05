package com.damianryan.octopus.utils

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.bind.DefaultValue

/**
 * Rest client logging configuration.
 *
 * @property enabled whether requests and responses should be logged at all
 * @property headers whether request and response headers should be logged
 */
@ConfigurationProperties("rest-client.logging")
data class RestClientLoggingProperties(
    @DefaultValue("true") val enabled: Boolean,
    @DefaultValue("false") val headers: Boolean
)
