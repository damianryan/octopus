package com.damianryan.octopus

import com.damianryan.octopus.model.Page
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.http.client.JdkClientHttpRequestFactory
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient
import org.springframework.web.client.RestClientException
import java.net.http.HttpClient

@Component
class OctopusRestClient(
    private val properties: OctopusProperties
) {
    private val delegate: RestClient

    init {
        val httpClient = HttpClient
            .newBuilder()
            .followRedirects(HttpClient.Redirect.NORMAL).build()
        delegate = RestClient
            .builder()
            .requestFactory(JdkClientHttpRequestFactory(httpClient))
            .baseUrl(properties.baseUrl)
            .build()
    }

    fun <T: Any> get(path: String, type: Class<T>): T {
        val entity = getForEntity(path, type)
        if (!entity.statusCode.is2xxSuccessful) {
            throw RestClientException("Failed to get resource from $path, status code: ${entity.statusCode}")
        }
        return entity.body!!
    }

    private fun <T : Any> getForEntity(path: String, type: Class<T>): ResponseEntity<T> =
        delegate
            .get()
            .uri(path)
            .accept(MediaType.APPLICATION_JSON)
            .headers { header: HttpHeaders -> header.setBasicAuth(properties.apiKey, "") }
            .retrieve()
            .toEntity(type)

    fun <T, P : Page<T>> getMany(path: String, type: Class<P>): List<T> {
        val result: MutableList<T> = ArrayList()
        var uri: String? = "$path?page=1&page_size=25000"
        while (null != uri) {
            val page = getForEntity(uri, type).body!!
            val elements = page.content!!
            result.addAll(elements)
            uri = page.next
        }
        return result
    }
}
