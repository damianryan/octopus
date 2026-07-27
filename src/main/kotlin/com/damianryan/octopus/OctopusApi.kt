package com.damianryan.octopus

import com.damianryan.octopus.model.Account
import com.damianryan.octopus.model.Consumption
import com.damianryan.octopus.model.ElectricityMeterPoint
import com.damianryan.octopus.model.GasMeterPoint
import com.damianryan.octopus.model.Reading
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class OctopusApi(
    private val restClient: OctopusRestClient,
    private val properties: OctopusProperties,
    private val log: Logger = LoggerFactory.getLogger(OctopusApi::class.java)
) {
    init {
        log.info("Account number: ${properties.accountNumber}")
    }

    val account: Account by lazy {
        restClient.get(
            properties.accountsUrl,
            Account::class.java
        )
    }

    val electricityMeterPoint: ElectricityMeterPoint by lazy {
        account.properties?.firstOrNull()?.electricityMeterPoints?.firstOrNull()!!
    }

    val mpan: String by lazy {
        electricityMeterPoint.mpan!!
    }

    val electricityMeterSerialNumber: String by lazy {
        electricityMeterPoint.meters?.firstOrNull()?.serialNumber!!
    }

    open val electricityConsumption: List<Reading?> by lazy {
        // https://api.octopus.energy/v1/electricity-meter-points/{mpan}/meters/{serial_number}/consumption/
        restClient.getMany(
            "/electricity-meter-points/${mpan}/meters/${electricityMeterSerialNumber}/consumption",
            Consumption::class.java
        )
    }

    val gasMeterPoint: GasMeterPoint by lazy {
        account.properties?.firstOrNull()?.gasMeterPoints?.firstOrNull()!!
    }

    val mprn: String by lazy {
        gasMeterPoint.mprn!!
    }

    val gasMeterSerialNumber: String by lazy {
        gasMeterPoint.meters?.firstOrNull()?.serialNumber!!
    }

    val gasConsumption: List<Reading?> by lazy {
        restClient.getMany(
            "/gas-meter-points/${mprn}/meters/${gasMeterSerialNumber}/consumption",
            Consumption::class.java
        )
    }
}
