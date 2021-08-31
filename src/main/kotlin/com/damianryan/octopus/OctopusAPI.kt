package com.damianryan.octopus

import com.damianryan.octopus.model.*
import org.slf4j.LoggerFactory
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.util.UriComponentsBuilder
import reactor.util.retry.Retry
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import java.time.Duration
import java.time.Instant

@Component
@EnableConfigurationProperties(OctopusProperties::class)
class OctopusAPI(val client: WebClient, val config: OctopusProperties) {

    val account: Account by lazy {
        getSingle(
            UriComponentsBuilder.fromUriString(config.accountsUrl!!)
                .uriVariables(mapOf(ACCOUNT_NUMBER to config.accountNumber!!))
                .toUriString(),
            Account::class.java
        )
    }

    val home: Property by lazy {
        account.properties?.get(0)!!
    }

    val movedInAt: Instant by lazy {
        home.movedInAt!!
    }

    val electricityMeterPoint: ElectricityMeterPoint by lazy {
        home.electricityMeterPoints?.get(0)!!
    }

    val electricityMeter: ElectricityMeter by lazy {
        electricityMeterPoint.meters?.get(0)!!
    }

    val electricityReadings: List<Reading?> by lazy {
        log.info("fetching electricity consumption")
        getMany(
            UriComponentsBuilder.fromUriString(config.electricityConsumptionUrl!!)
                .uriVariables(
                    mapOf(
                        MPAN to electricityMeterPoint.mpan,
                        SERIAL_NUMBER to electricityMeter.serialNumber
                    ))
                .queryParam(PERIOD_FROM, movedInAt)
                .toUriString(),
            Consumption::class.java
        )
    }

    val electricityAgreements: List<Agreement> by lazy {
        electricityMeterPoint.agreements!!
    }

    /**
     * Electricity standing charges as daily prices inclusive of from the associated date.
     */
    val electricityStandingCharges: Map<Instant, Double> by lazy {
        electricityAgreements.map { agreement ->
            Pair(
                UriComponentsBuilder
                    .fromUriString(config.electricityStandingChargesUrl!!)
                    .uriVariables(
                        mapOf(
                            PRODUCT_CODE to electricityProductFor(agreement.tariffCode),
                            TARIFF_CODE to agreement.tariffCode!!
                        )
                    )
                    .toUriString(),
                agreement.validFrom
            )
        }.map { pair ->
            Pair(
                getMany(pair.first, StandingCharge::class.java)[0],
                pair.second
            )
        }.associate { it.second!! to it.first?.valueIncVAT!! }
    }

    fun electricityProductFor(tariffCode: String?) = when (tariffCode) {
        "E-1R-OE-FIX-24M-21-05-29-A" -> config.fixedRateProductCode!!
        else -> config.goProductCode!!
    }

    private fun <T> getSingle(uri: String, type: Class<T>): T =
        client
            .get()
            .uri(uri)
            .accept(MediaType.APPLICATION_JSON)
            .headers { header: HttpHeaders -> header.setBasicAuth(config.apiKey!!, "") }
            .retrieve()
            .bodyToMono(type)
            .doOnSuccess { log.debug("got {}", it) }
            .retryWhen(Retry.backoff(MAX_ATTEMPTS, Duration.ofSeconds(INITIAL_BACKOFF_SECONDS)).jitter(JITTER_FACTOR))
            .block()!!

    private fun <T, P : Page<T>> getMany(initialUri: String, type: Class<P>): List<T> {
        val result: MutableList<T> = ArrayList()
        var uri: String? = initialUri
        var pageIndex = 1
        var runningCount: Long = 0
        while (null != uri) {
            log.debug("getting page {} of {} from {}...", pageIndex++, type.simpleName, uri)
            val page = getSingle(uri, type)
            val elements = page.content!!
            result.addAll(elements)
            val thisCount = elements.size.toLong()
            runningCount += thisCount
            log.debug(
                "got {}/{} {} results, running total {}",
                thisCount,
                page.count,
                type.simpleName,
                runningCount
            )
            uri = if (null != page.next) URLDecoder.decode(page.next, StandardCharsets.UTF_8) else null
        }
        return result
    }

    companion object {
        private val log = LoggerFactory.getLogger(OctopusAPI::class.java)

        const val ACCOUNT_NUMBER = "accountNumber"
        const val MPAN = "mpan"
        const val SERIAL_NUMBER = "serialNumber"
        const val PERIOD_FROM = "period_from"
        const val PRODUCT_CODE = "productCode"
        const val TARIFF_CODE = "tariffCode"

        const val MAX_ATTEMPTS = 3L
        const val INITIAL_BACKOFF_SECONDS = 2L
        const val JITTER_FACTOR = 0.75
    }
}