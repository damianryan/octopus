package com.damianryan.octopus

import com.damianryan.octopus.concurrency.awaitAllSuccessfulOrThrow
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication

@SpringBootApplication(scanBasePackages = ["com.damianryan.octopus"])
class OctopusApplication(
    private val octopus: OctopusApi,
    @Suppress("unused") private val tariffService: TariffService,
    private val log: Logger = LoggerFactory.getLogger(OctopusApplication::class.java)
) : CommandLineRunner {

    override fun run(vararg args: String) {
        val consumptions = awaitAllSuccessfulOrThrow {
            val electricityConsumption = fork { octopus.electricityConsumption() }
            val gasConsumption = fork { octopus.gasConsumption() }
            join()
            mapOf(ELECTRICITY to electricityConsumption.get(), GAS to gasConsumption.get())
        }
        val electricityConsumption = consumptions[ELECTRICITY] ?: emptyList()
        val gasConsumption = consumptions[GAS] ?: emptyList()
        log.info("${electricityConsumption.size} electricity readings, ${gasConsumption.size} gas readings")
    }

    companion object {
        const val GAS = "gas"
        const val ELECTRICITY = "electricity"
    }
}

@Suppress("SpreadOperator")
fun main(args: Array<String>) {
    SpringApplication.run(OctopusApplication::class.java, *args)
}
