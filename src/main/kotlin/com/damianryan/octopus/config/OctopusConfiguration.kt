package com.damianryan.octopus.config

import com.damianryan.octopus.OctopusProperties
import com.damianryan.octopus.utils.LoggingClientHttpRequestInterceptor
import com.damianryan.octopus.utils.RestClientLoggingProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.task.VirtualThreadTaskExecutor
import org.springframework.http.client.JdkClientHttpRequestFactory
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.web.client.RestClient
import java.net.http.HttpClient


@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(OctopusProperties::class, RestClientLoggingProperties::class)
@EnableAsync
class OctopusConfiguration {

    @Bean("applicationTaskExecutor")
    fun applicationTaskExecutor() = VirtualThreadTaskExecutor("async-")

    @Bean
    fun restClientBuilder(requestInterceptor: LoggingClientHttpRequestInterceptor): RestClient.Builder {
        val httpClient = HttpClient.newBuilder().followRedirects(HttpClient.Redirect.NORMAL).build()
        return RestClient.builder()
            .requestFactory(JdkClientHttpRequestFactory(httpClient))
            .requestInterceptor(requestInterceptor)
    }
}
