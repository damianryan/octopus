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
import tools.jackson.databind.DeserializationFeature
import tools.jackson.databind.SerializationFeature
import tools.jackson.databind.json.JsonMapper

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

    private val jsonMapper: JsonMapper = JsonMapper
        .builder()
        .enable(SerializationFeature.INDENT_OUTPUT)
        .enable(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT)
        .build()

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
                log.debug("Request body: {}", String(body, StandardCharsets.UTF_8))
            }
        }
    }

    private fun logResponse(response: ClientHttpResponse): ClientHttpResponse {
        val bytes = response.body.readAllBytes()
        if (logging.enabled) {
            log.info("Response status: {}", response.statusCode)
            logHeaders(response.headers, "Response")
            if (bytes.isNotEmpty()) {
                val node = jsonMapper.readTree(bytes)
                val indented = jsonMapper.writeValueAsString(node)
                log.debug("Response body:\n{}", indented)
            }
        }
        return ClientHttpResponseWrapper(response, bytes)
    }

    private fun logHeaders(headers: HttpHeaders, type: String) {
        if (logging.headers) {
            log.debug("$type headers:")
            headers.forEach { name, values -> values.forEach { log.info("{} = {}", name, it) } }
        }
    }
}
