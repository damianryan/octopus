package com.damianryan.octopus.utils

import java.nio.charset.StandardCharsets
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpRequest
import org.springframework.http.client.ClientHttpRequestExecution
import org.springframework.http.client.ClientHttpRequestInterceptor
import org.springframework.http.client.ClientHttpResponse
import org.springframework.stereotype.Component

/**
 * A [ClientHttpRequestInterceptor] that can log HTTP requests and responses.
 *
 * @property logging logging configuration properties
 * @property log logger
 */
@Component
class LoggingClientHttpRequestInterceptor(
    private val logging: RestClientLoggingProperties,
    private val log: Logger = LoggerFactory.getLogger(LoggingClientHttpRequestInterceptor::class.java)
) : ClientHttpRequestInterceptor {

    override fun intercept(
        request: HttpRequest,
        body: ByteArray,
        execution: ClientHttpRequestExecution
    ): ClientHttpResponse {
        logRequest(request, body)
        val response = execution.execute(request, body)
        return logResponse(response)
    }

    private fun logRequest(request: HttpRequest, body: ByteArray) {
        if (logging.enabled) {
            logHeaders(request.headers, "Request")
            log.info("Request URI: {} {}", request.method, request.uri)
            if (body.isNotEmpty()) {
                log.info("Request body: {}", String(body, StandardCharsets.UTF_8))
            }
        }
    }

    private fun logResponse(response: ClientHttpResponse): ClientHttpResponse {
        val bytes = response.body.readAllBytes()
        if (logging.enabled) {
            log.info("Response status: {}", response.statusCode)
            logHeaders(response.headers, "Response")
            if (bytes.isNotEmpty()) {
                log.info("Response body: {}", String(bytes, StandardCharsets.UTF_8))
            }
        }
        return ClientHttpResponseWrapper(response, bytes)
    }

    private fun logHeaders(headers: HttpHeaders, type: String) {
        if (logging.headers) {
            log.info("$type headers:")
            headers.forEach { name, values -> values.forEach { log.info("{} = {}", name, it) } }
        }
    }
}
