package com.damianryan.octopus

import com.damianryan.octopus.model.Account
import com.damianryan.octopus.model.Agreement
import com.damianryan.octopus.model.Consumption
import com.damianryan.octopus.model.ElectricityMeterPoint
import com.damianryan.octopus.model.GasMeterPoint
import com.damianryan.octopus.model.GridSupplyPoint
import com.damianryan.octopus.model.GridSupplyPoints
import com.damianryan.octopus.model.Product
import com.damianryan.octopus.model.Products
import com.damianryan.octopus.model.Reading
import com.damianryan.octopus.model.Tariff
import com.damianryan.octopus.model.dno.DistributionNetworkOperator
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.cache.annotation.Cacheable
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service
import java.util.concurrent.CompletableFuture

/**
 * Octopus REST API.
 *
 * @property restClient Octopus REST API client
 * @property properties Octopus REST API properties
 */
@Service
@Suppress("unused")
class OctopusApi(
    private val restClient: OctopusRestClient,
    private val properties: OctopusProperties,
    private val log: Logger = LoggerFactory.getLogger(OctopusApi::class.java)
) {
    val account: Account by lazy { restClient.get(properties.accountsUrl, Account::class.java) }

    val electricityMeterPoint: ElectricityMeterPoint by lazy {
        account.properties.firstOrNull()?.electricityMeterPoints?.firstOrNull()!!
    }

    val mpan: String by lazy { electricityMeterPoint.mpan }

    val dno: DistributionNetworkOperator by lazy {
        DistributionNetworkOperator.fromMpan(mpan)!!
    }

    val gspGroupId: String by lazy {
        DistributionNetworkOperator.fromMpan(mpan)?.gspGroupId!!
    }

    val electricityMeterSerialNumber: String by lazy { electricityMeterPoint.meters.firstOrNull()?.serialNumber!! }

    // https://api.octopus.energy/v1/electricity-meter-points/{mpan}/meters/{serial_number}/consumption/
    fun electricityConsumption() : List<Reading?> =
        restClient.getMany(
        "/electricity-meter-points/${mpan}/meters/${electricityMeterSerialNumber}/consumption",
        Consumption::class.java)

    @Async fun electricityConsumptionAsync(): CompletableFuture<List<Reading?>> =
        CompletableFuture.completedFuture(electricityConsumption())

    fun electricityAgreements(): List<Agreement> = electricityMeterPoint.agreements.sorted().apply {
        log.info("Electricity agreement count: ${this.size}")
    }

    val gasMeterPoint: GasMeterPoint by lazy { account.properties.firstOrNull()?.gasMeterPoints?.firstOrNull()!! }

    val mprn: String by lazy { gasMeterPoint.mprn }

    val gasMeterSerialNumber: String by lazy { gasMeterPoint.meters.firstOrNull()?.serialNumber!! }

    fun gasConsumption(): List<Reading> = restClient.getMany(
        "/gas-meter-points/${mprn}/meters/${gasMeterSerialNumber}/consumption",
        Consumption::class.java
    )

    @Async fun gasConsumptionAsync(): CompletableFuture<List<Reading?>> =
        CompletableFuture.completedFuture(gasConsumption())

    fun gasAgreements(): List<Agreement> = gasMeterPoint.agreements.sorted().apply {
        log.info("Gas agreements count: ${this.size}")
    }

    @Cacheable("products")fun product(code: String): Product = restClient.get("/products/${code}", Product::class.java)

    val electricityProduct: Product by lazy {
        restClient.get("/products/${properties.electricityProductCode}", Product::class.java)
    }

    val electricityTariff: Tariff by lazy {
        electricityProduct.singleRegisterElectricityTariffs?.get(gspGroupId)?.get(DIRECT_DEBIT)!!
    }

    val products: List<Product?> by lazy { restClient.getMany("/products", Products::class.java) }

    val gridSupplyPoints: List<GridSupplyPoint?> by lazy {
        restClient.getMany("/industry/grid-supply-points", GridSupplyPoints::class.java)
    }

    companion object {
        const val DIRECT_DEBIT = "direct_debit_monthly"
    }
}
