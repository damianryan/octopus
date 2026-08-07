package com.damianryan.octopus

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import java.util.concurrent.CompletableFuture

@SpringBootApplication(scanBasePackages = ["com.damianryan.octopus"])
class OctopusApplication(
    private val octopus: OctopusApi,
    @Suppress("unused") private val tariffService: TariffService,
    private val log: Logger = LoggerFactory.getLogger(OctopusApplication::class.java)
) : CommandLineRunner {

    override fun run(vararg args: String) {
        val electricityConsumption = octopus.electricityConsumptionAsync()
        val gasConsumption = octopus.gasConsumptionAsync()
        CompletableFuture.allOf(electricityConsumption, gasConsumption).join()

        log.info("Electricity consumption: {} readings", electricityConsumption.get().size)
        log.info("Gas consumption: {} readings", gasConsumption.get().size)
    }
}

@Suppress("SpreadOperator")
fun main(args: Array<String>) {
    SpringApplication.run(OctopusApplication::class.java, *args)
}
