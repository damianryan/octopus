package com.damianryan.octopus

import com.damianryan.octopus.model.Agreement
import com.damianryan.octopus.model.Product
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.util.concurrent.CompletableFuture

@Component
class TariffService(
    private val octopus: OctopusApi,
    private val log: Logger = LoggerFactory.getLogger(TariffService::class.java)
) {
    private val electricityAgreements: List<Agreement>
    private val gasAgreements: List<Agreement>
    private val products: List<Product>

    init {
        val account = octopus.account
        log.info("Account: {}", account.number)
        log.info("Electricity distribution network operator: {}", octopus.dno)
        val electricityAgreementsFuture = octopus.electricityAgreements()
        val gasAgreementsFuture = octopus.gasAgreements()
        CompletableFuture.allOf(electricityAgreementsFuture, gasAgreementsFuture).join()
        electricityAgreements = electricityAgreementsFuture.get()
        gasAgreements = gasAgreementsFuture.get()
        val productCodes = electricityAgreements.map { it.productCode }.toMutableSet()
        val gasProductCodes = gasAgreements.map { it.productCode }.toSet()
        productCodes.addAll(gasProductCodes)
        val productFutures = productCodes.map { octopus.product(it) }
        CompletableFuture.allOf(*productFutures.toTypedArray()).join()
        products = productFutures.map { it.get().apply {
            log.info("Product: {} - {}", code, description)
        } }
    }
}
