package com.damianryan.octopus

import org.slf4j.LoggerFactory
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.util.UriComponentsBuilder
import org.threeten.extra.Interval
import reactor.util.retry.Retry
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.util.function.Consumer

@Component
@EnableConfigurationProperties(OctopusProperties::class)
class Octopus(val client: WebClient, val config: OctopusProperties) {

    val account: Account by lazy {
        getSingle(
            UriComponentsBuilder
                .fromUriString(config.accountsUrl!!)
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

    val eletricityRegion: String by lazy {
        getSingle(
            UriComponentsBuilder
                .fromUriString(config.electricityMpanUrl!!)
                .uriVariables(mapOf(MPAN to electricityMeterPoint.mpan))
                .toUriString(),
            ElectricityMeterPoint::class.java
        ).region!!
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
                    )
                )
                .queryParam(PERIOD_FROM, movedInAt)
                .toUriString(),
            Consumption::class.java
        )
    }

    val electricityUsageByDate: Map<LocalDate, Double> by lazy {
        val readingsByDate: MultiValueMap<LocalDate, Reading> = LinkedMultiValueMap()
        electricityReadings.forEach(Consumer { result: Reading? -> readingsByDate.add(toLocalDate(result!!.from)!!, result) })
        val result = mutableMapOf<LocalDate, Double>()
        readingsByDate.forEach { (date, readings) ->
            result.put(date, readings.sumOf { it.consumption })
        }
        result
    }

    val totalElectricityStandingCharges: Double by lazy {
        var total = 0.0
        electricityUsageByDate.keys.forEach { date ->
            electricityStandingCharges.forEach { (interval, charge) ->
                if (interval.contains(toInstant(date))) {
                    total += charge
                }
            }
        }
        total
    }

    val electricityAgreements: List<Agreement> by lazy {
        electricityMeterPoint.agreements!!
    }

    val electricityStandingCharges: Map<Interval, Double> by lazy {
        val size = electricityStandingChargesMap.size
        val intervalStarts = electricityStandingChargesMap.keys.sorted()
        val result = mutableMapOf<Interval, Double>()
        var i = 1
        while (i <= size) {
            val from = intervalStarts[i - 1]
            val to = if (i == size) Instant.now() else intervalStarts[i]
            result.put(Interval.of(from, to), electricityStandingChargesMap[intervalStarts[i - 1]]!!)
            ++i
        }
        result
    }

    private val electricityStandingChargesMap: Map<Instant, Double> by lazy {
        electricityAgreements.map { agreement ->
            Pair(
                agreement.validFrom,
                UriComponentsBuilder
                    .fromUriString(config.electricityStandingChargesUrl!!)
                    .uriVariables(
                        mapOf(
                            PRODUCT_CODE to electricityProductFor(agreement.tariffCode),
                            TARIFF_CODE to agreement.tariffCode!!
                        )
                    )
                    .queryParam(PERIOD_FROM, agreement.validFrom)
                    .toUriString()
            )
        }.map { pair ->
            Pair(
                pair.first,
                getMany(pair.second, StandingCharge::class.java)[0]
            )
        }.associate { it.first!! to it.second?.valueIncVAT!! }
    }

//    private val electricityStandardUnitRateMap: Map<Instant, Double> by lazy {
//
//    }

    fun temp(): List<List<Rate?>> {
        return electricityAgreements.map { agreement ->
            UriComponentsBuilder
                .fromUriString(config.electricityStandardUnitRatesUrl!!)
                .uriVariables(
                    mapOf(
                        PRODUCT_CODE to electricityProductFor(agreement.tariffCode),
                        TARIFF_CODE to agreement.tariffCode!!
                    )
                )
                .queryParam(PERIOD_FROM, agreement.validFrom)
                .toUriString()
        }.map {
            getMany(it, StandardUnitRate::class.java)
        }
    }

    fun electricityProductFor(tariffCode: String?) = when (tariffCode) {
        FIXED_PRODUCT -> config.fixedRateProductCode!!
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
        private val log = LoggerFactory.getLogger(Octopus::class.java)

        const val ACCOUNT_NUMBER = "account_number"
        const val MPAN = "mpan"
        const val SERIAL_NUMBER = "serial_number"
        const val PERIOD_FROM = "period_from"
        const val PRODUCT_CODE = "product_code"
        const val TARIFF_CODE = "tariff_code"

        const val MAX_ATTEMPTS = 3L
        const val INITIAL_BACKOFF_SECONDS = 2L
        const val JITTER_FACTOR = 0.75

        const val FIXED_PRODUCT = "E-1R-OE-FIX-24M-21-05-29-A"
    }
}