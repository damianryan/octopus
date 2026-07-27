package com.damianryan.octopus

import com.damianryan.octopus.model.Page
import com.damianryan.octopus.utils.LoggingClientHttpRequestInterceptor
import java.net.http.HttpClient
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.client.JdkClientHttpRequestFactory
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient
import org.springframework.web.client.RestClientException

/**
 * Octopus REST API client.
 *
 * @property properties Octopus REST API properties
 */
@Component
class OctopusRestClient(
    private val properties: OctopusProperties,
    private val requestInterceptor: LoggingClientHttpRequestInterceptor
) {
    private val delegate: RestClient

    init {
        val httpClient = HttpClient.newBuilder().followRedirects(HttpClient.Redirect.NORMAL).build()
        delegate =
            RestClient.builder()
                .requestFactory(JdkClientHttpRequestFactory(httpClient))
                .requestInterceptor(requestInterceptor)
                .baseUrl(properties.baseUrl)
                .build()
    }

    /**
     * Get a single entity of a specific type from a relative URL path.
     *
     * @param path URL path
     * @param type entity class
     * @param T entity type
     * @return the matching entity.
     * @throws RestClientException if the get operation is unsuccessful.
     */
    fun <T : Any> get(path: String, type: Class<T>): T =
        delegate
            .get()
            .uri(path)
            .accept(MediaType.APPLICATION_JSON)
            .headers { header: HttpHeaders -> header.setBasicAuth(properties.apiKey, NONE) }
            .retrieve()
            .body(type)!!

    /**
     * Get a collection of entities by requesting all available pages of them from a URL path.
     *
     * @param path URL path
     * @param type page class
     * @param T entity type
     * @param P page type
     * @return a collection of matching entities.
     * @throws RestClientException if the get operation is unsuccessful.
     */
    fun <T, P : Page<T>> getMany(path: String, type: Class<P>): List<T> {
        val result: MutableList<T> = ArrayList()
        var uri: String? = "$path?page=$FIRST_PAGE&page_size=$PAGE_SIZE"
        while (null != uri) {
            val page = get(uri, type)
            val elements = page.content!!
            result.addAll(elements)
            uri = page.next
        }
        return result
    }

    companion object {
        const val FIRST_PAGE = 1
        const val PAGE_SIZE = 25000
        const val NONE = ""
    }
}
