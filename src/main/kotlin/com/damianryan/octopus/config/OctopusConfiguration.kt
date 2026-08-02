package com.damianryan.octopus.config

import com.damianryan.octopus.OctopusProperties
import com.damianryan.octopus.utils.RestClientLoggingProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.task.VirtualThreadTaskExecutor
import org.springframework.scheduling.annotation.EnableAsync


@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(OctopusProperties::class, RestClientLoggingProperties::class)
@EnableAsync
class OctopusConfiguration {

    @Bean("applicationTaskExecutor")
    fun applicationTaskExecutor() = VirtualThreadTaskExecutor("async-")
}
