package com.damianryan.octopus

import com.damianryan.octopus.model.Reading
import java.time.LocalDate
import java.util.function.Consumer
import java.util.stream.Collectors
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.util.CollectionUtils
import org.springframework.util.LinkedMultiValueMap

@SpringBootApplication
class Application(val api: Octopus) : CommandLineRunner {

    override fun run(vararg args: String) {
        log.info("switched to Octopus: {}", api.home.movedInAt)
        logReadings(api.electricityReadings, "electricity")
        logReadings(api.gasReadings, "gas")
        val totalElectricityStandingCharge = api.totalElectricityStandingCharges
        log.info("total electricity standing charge: £{}", twoDP(totalElectricityStandingCharge / ONE_HUNDRED))
        log.info("electricity region: {}", api.electricityRegion)
        //        log.info("standard unit rates: {}", api.temp())
    }

    @Suppress("LongMethod")
    private fun logReadings(readings: List<Reading?>, type: String) {
        if (readings.isNotEmpty()) {
            val earliest = readings[0]!!.from
            val lastReading = readings[readings.size - 1]
            val latest = lastReading!!.to
            var totalUsage = readings.sumOf { it?.consumption!! }
            val readingsByDate = LinkedMultiValueMap<LocalDate, Reading>()
            readings.forEach(Consumer { result: Reading? -> readingsByDate.add(toLocalDate(result!!.from)!!, result) })
            val numberOfReadingsByDate = readingsByDate.mapValues { it.value.size }
            var numberOfReadingsOnLastDay = numberOfReadingsByDate[toLocalDate(latest)]
            if (null == numberOfReadingsOnLastDay) {
                numberOfReadingsOnLastDay = numberOfReadingsByDate[toLocalDate(latest)?.minusDays(1)]
            }
            log.info("number of readings on last day: {}", numberOfReadingsOnLastDay)
            val penultimateReading = readings[readings.size - 1 - numberOfReadingsOnLastDay!!]
            var totalDays = readingsByDate.size
            log.info(
                "total {} usage {} between {} and {} ({} days)",
                type,
                kWh(totalUsage),
                formatLocalDateTime(earliest),
                formatLocalDateTime(latest),
                totalDays,
            )
            if (numberOfReadingsOnLastDay < EXPECTED_READINGS_PER_DAY) {
                totalDays -= 1
                val lastDaysReadings = readingsByDate[toLocalDate(latest)]
                if (!CollectionUtils.isEmpty(lastDaysReadings)) {
                    val lastDaysConsumption = lastDaysReadings?.sumOf { it.consumption }!!
                    totalUsage -= lastDaysConsumption
                    log.info(
                        "ignoring last day's {} reading{}, total {} usage {} between {} and {} ({} days)",
                        numberOfReadingsOnLastDay,
                        s(numberOfReadingsOnLastDay),
                        type,
                        kWh(totalUsage),
                        formatLocalDateTime(earliest),
                        formatLocalDateTime(penultimateReading!!.to),
                        totalDays,
                    )
                }
            }
            var lowest = Double.MAX_VALUE
            var highest = Double.MIN_VALUE
            var count = 0
            val usageByDate: MutableMap<LocalDate?, Double> = LinkedHashMap()
            for ((date, usages) in readingsByDate) {
                val usage = usages.sumOf { it?.consumption!! }
                ++count
                if (usage > highest) {
                    highest = usage
                }
                if (usage < lowest && count < totalDays) {
                    lowest = usage
                }
                usageByDate[date] = usage
            }
            for ((date, usage) in usageByDate) {
                log.info(
                    "{}: {} ({} readings){}{}{}",
                    date,
                    kWh(usage),
                    numberOfReadingsByDate[date],
                    if (lowest == usage) " (lowest)" else "",
                    if (highest == usage) " (highest)" else "",
                    if (EXPECTED_READINGS_PER_DAY != numberOfReadingsByDate[date]) " *" else "",
                )
            }
            log.info("mean usage per day over {} days was: {}", totalDays, kWh(totalUsage / totalDays))
            log.info(
                "median usage per day over {} days was: {}",
                totalDays,
                kWh(median(usageByDate.values.stream().sorted().collect(Collectors.toList()))),
            )
        } else {
            log.info("no readings available")
        }
    }

    companion object {
        const val ONE_HUNDRED = 100
        const val EXPECTED_READINGS_PER_DAY = 48
        val log: Logger = LoggerFactory.getLogger(Application::class.java)
    }
}

@Suppress("SpreadOperator")
fun main(args: Array<String>) {
    SpringApplication.run(Application::class.java, *args)
}
