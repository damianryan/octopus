package com.damianryan.octopus

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties

@SpringBootApplication(scanBasePackages = ["com.damianryan.octopus"])
@EnableConfigurationProperties(OctopusProperties::class)
class NewApplication(
    private val octopus: OctopusApi,
    private val log: Logger = LoggerFactory.getLogger(NewApplication::class.java)
) : CommandLineRunner {

    override fun run(vararg args: String) {
        log.info("New octopus application")
        val account = octopus.account
        log.info("Account: {}", account)
        val electricityConsumption = octopus.electricityConsumption
        log.info("Electricity consumption: {} readings", electricityConsumption.size)
        val gasConsumption = octopus.gasConsumption
        log.info("Gas consumption: {} readings", gasConsumption.size)
    }
}

@Suppress("SpreadOperator")
fun main(args: Array<String>) {
    SpringApplication.run(NewApplication::class.java, *args)
}
