package com.damianryan.octopus

import com.damianryan.octopus.concurrency.awaitAllSuccessfulOrThrow
import com.damianryan.octopus.model.Product
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class TariffService(
    private val octopus: OctopusApi,
    private val log: Logger = LoggerFactory.getLogger(TariffService::class.java)
) {
    private val products: List<Product>

    init {
        val productCodes = awaitAllSuccessfulOrThrow {
            val electricityAgreements = fork { octopus.electricityAgreements() }
            val gasAgreements = fork { octopus.gasAgreements() }
            join()
            val codes = electricityAgreements.get().map { it.productCode }.toMutableSet()
            codes.addAll(gasAgreements.get().map { it.productCode })
            codes.toSet()
        }

        products = awaitAllSuccessfulOrThrow {
            val productFutures = productCodes.map { code ->
                fork { octopus.product(code).apply { log.info("Product: {} - {}", code, description) } }
            }
            join()
            productFutures.map { it.get() }
        }
    }
}
