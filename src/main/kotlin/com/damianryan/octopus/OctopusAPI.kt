package com.damianryan.octopus

import com.damianryan.octopus.model.Consumption
import com.damianryan.octopus.model.Page
import com.damianryan.octopus.model.Reading
import org.slf4j.LoggerFactory
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import reactor.util.retry.Retry
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import java.time.Duration

@Component
@EnableConfigurationProperties(OctopusProperties::class)
class OctopusAPI(val client: WebClient, val config: OctopusProperties) {

//    val account: Account
//        get() = getSingle(config.accountUrl!!, Account::class.java)

    val gasReadings: List<Reading?>
        get() {
            log.info("fetching gas consumption")
            return getMany(config.gasConsumptionUrl!!, Consumption::class.java)
        }

    val electricityReadings: List<Reading?>
        get() {
            log.info("fetching electricity consumption")
            return getMany(config.electricityConsumptionUrl!!, Consumption::class.java)
        }

//    val allProducts: List<Product?>
//        get() {
//            log.info("fetching products...")
//            val timer = StopWatch("products")
//            timer.start()
//            val products = getMany(config.productsUrl!!, Products::class.java).map { product: Product? -> populateTariffsFor(product) }
//            timer.stop()
//            log.info("{}", timer)
//            return products
//        }

//    private fun populateTariffsFor(product: Product?): Product {
//        return getSingle(product?.links!!.first().href, Product::class.java)
//    }

    private fun <T> getSingle(uri: String, type: Class<T>): T {
        return client
            .get()
            .uri(uri)
            .accept(MediaType.APPLICATION_JSON)
            .headers { header: HttpHeaders -> header.setBasicAuth(config.apiKey!!, "") }
            .retrieve()
            .bodyToMono(type)
            .doOnSuccess { log.info("got {}", it) }
            .retryWhen(Retry.backoff(3, Duration.ofSeconds(2)).jitter(0.75))
            .block()!!
    }

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

//    private fun getProductCode(tariffCode: String?): String? {
//        if (tariffCode!!.contains("GO")) {
//            return config.goProductCode
//        } else if (tariffCode.contains("FIX")) {
//            return config.fixedRateProductCode
//        }
//        throw IllegalArgumentException("Unhandled tariff code: $tariffCode")
//    }

//    private fun getElectricityTariffUrlBase(agreement: Agreement): String {
//        return config.electricityTariffsUrl
//            ?.replace("@product-code".toRegex(), getProductCode(agreement.tariffCode)!!)
//            ?.replace("@tariff-code".toRegex(), agreement.tariffCode!!) + "/"
//    }

    companion object {

        @JvmStatic
        private val log = LoggerFactory.getLogger(OctopusAPI::class.java)

//        private val ISO_LOCAL_DATE_TIME_HH_MM = DateTimeFormatterBuilder()
//            .parseCaseInsensitive()
//            .append(DateTimeFormatter.ISO_LOCAL_DATE)
//            .appendLiteral('T')
//            .append(DateTimeFormatter.ofPattern("HH:mm"))
//            .toFormatter()

//        private fun dateTime(instant: Instant?): String {
//            return ISO_LOCAL_DATE_TIME_HH_MM.format(LocalDateTime.ofInstant(instant, ZoneId.systemDefault()))
//        }

//        private fun getAgreementPeriodParameters(agreement: Agreement): String {
//            return "/?period_from=" + dateTime(agreement.validFrom) +
//                    "&period_to=" + dateTime(agreement.validTo)
//        }
    }
}