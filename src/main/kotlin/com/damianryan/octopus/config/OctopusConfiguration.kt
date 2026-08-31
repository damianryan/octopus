package com.damianryan.octopus.config

import com.damianryan.octopus.OctopusProperties
import com.damianryan.octopus.utils.LoggingInterceptor
import com.damianryan.octopus.utils.RestClientLoggingProperties
import java.net.http.HttpClient
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.client.JdkClientHttpRequestFactory
import org.springframework.web.client.RestClient

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(OctopusProperties::class, RestClientLoggingProperties::class)
class OctopusConfiguration {

    @Bean
    fun restClientBuilder(requestInterceptor: LoggingInterceptor): RestClient.Builder {
        val httpClient = HttpClient.newBuilder().followRedirects(HttpClient.Redirect.NORMAL).build()
        return RestClient.builder()
            .requestFactory(JdkClientHttpRequestFactory(httpClient))
            .requestInterceptor(requestInterceptor)
    }
}
