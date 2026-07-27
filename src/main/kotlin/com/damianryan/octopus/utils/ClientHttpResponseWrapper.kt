package com.damianryan.octopus.utils

import java.io.ByteArrayInputStream
import java.io.InputStream
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatusCode
import org.springframework.http.client.ClientHttpResponse

/**
 * A wrapper for a [ClientHttpResponse] that allows the response body to be read again after it's been consumed by a
 * [LoggingClientHttpRequestInterceptor].
 *
 * @property response a client HTTP response
 * @property body a copy of the response body from the client HTTP response
 */
class ClientHttpResponseWrapper(val response: ClientHttpResponse, val body: ByteArray) : ClientHttpResponse {

    override fun getStatusCode(): HttpStatusCode = response.statusCode

    override fun getStatusText(): String = response.statusText

    override fun close() {
        response.close()
    }

    override fun getBody(): InputStream = ByteArrayInputStream(body)

    override fun getHeaders(): HttpHeaders = response.headers
}
