package com.damianryan.octopus

import com.damianryan.octopus.utils.RestClientLoggingProperties
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties

@SpringBootApplication(scanBasePackages = ["com.damianryan.octopus"])
@EnableConfigurationProperties(OctopusProperties::class, RestClientLoggingProperties::class)
class OctopusApplication(
    private val octopus: OctopusApi,
    private val properties: OctopusProperties,
    private val log: Logger = LoggerFactory.getLogger(OctopusApplication::class.java)
) : CommandLineRunner {

    override fun run(vararg args: String) {
        val account = octopus.account
        log.info("Account: {}", account.number)
        log.info("Electricity distribution network operator: {}", octopus.dno)
        log.info("Current electricity product: {}", properties.electricityProductCode)

        val electricityAgreements = octopus.electricityAgreements
        log.info("Electricity agreements: {}", electricityAgreements.size)
        electricityAgreements.forEach { log.info(TARIFF_PATTERN, it.tariffCode, it.validFrom, it.validTo) }
        val tariff = octopus.electricityTariff
        log.info("Current electricity tariff: {}", tariff)
        val electricityConsumption = octopus.electricityConsumption
        log.info("Electricity consumption: {} readings", electricityConsumption.size)

        val gasAgreements = octopus.gasAgreements
        log.info("Gas agreements: {}", gasAgreements.size)
        gasAgreements.forEach { log.info(TARIFF_PATTERN, it.tariffCode, it.validFrom, it.validTo) }
        val gasConsumption = octopus.gasConsumption
        log.info("Gas consumption: total of {} readings", gasConsumption.size)
    }

    companion object {
        const val TARIFF_PATTERN = "  {} between {} and {}"
    }
}

@Suppress("SpreadOperator")
fun main(args: Array<String>) {
    SpringApplication.run(OctopusApplication::class.java, *args)
}
