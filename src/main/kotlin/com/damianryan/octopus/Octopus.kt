package com.damianryan.octopus

import com.damianryan.octopus.model.Reading
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.util.CollectionUtils
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import java.time.LocalDate
import java.util.function.Consumer
import java.util.stream.Collectors

@SpringBootApplication
class Octopus(val api: OctopusAPI) : CommandLineRunner {

    override fun run(vararg args: String) {
        logReadings(api.electricityReadings, "electricity")
        log.info("electricity standing charges: {}", api.electricityStandingCharges)
    }

    private fun logReadings(readings: List<Reading?>, type: String) {
        if (readings.isNotEmpty()) {
            val earliest = readings[0]!!.from
            val lastReading = readings[readings.size - 1]
            val latest = lastReading!!.to
            var totalUsage = readings.sumOf { it?.consumption!! }
            val readingsByDate: MultiValueMap<LocalDate?, Reading?> = LinkedMultiValueMap()
            readings.forEach(Consumer { result: Reading? -> readingsByDate.add(toLocalDate(result!!.from)!!, result) })
            val numberOfReadingsByDate = readingsByDate.mapValues { it.value.size }
            val numberOfReadingsOnLastDay = numberOfReadingsByDate[toLocalDate(latest)]!!
            log.info("number of readings on last day: {}", numberOfReadingsOnLastDay)
            val penultimateReading = readings[readings.size - 1 - numberOfReadingsOnLastDay]
            var totalDays = readingsByDate.size
            log.info("total {} usage {} between {} and {} ({} days)", type, kWh(totalUsage), formatLocalDateTime(earliest), formatLocalDateTime(latest), totalDays)
            if (numberOfReadingsOnLastDay < 48) {
                totalDays -= 1
                val lastDaysReadings = readingsByDate[toLocalDate(latest)]
                if (!CollectionUtils.isEmpty(lastDaysReadings)) {
                    val lastDaysConsumption = lastDaysReadings?.sumOf { it?.consumption!! }!!
                    totalUsage -= lastDaysConsumption
                    log.info(
                        "ignoring last day's {} reading{}, total {} usage {} between {} and {} ({} days)",
                        numberOfReadingsOnLastDay,
                        s(numberOfReadingsOnLastDay),
                        type,
                        kWh(totalUsage),
                        formatLocalDateTime(earliest),
                        formatLocalDateTime(penultimateReading!!.to),
                        totalDays
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
                    if (48 != numberOfReadingsByDate[date]) " *" else ""
                )
            }
            log.info("mean usage per day over {} days was: {}", totalDays, kWh(totalUsage / totalDays))
            log.info(
                "median usage per day over {} days was: {}",
                totalDays,
                kWh(median(usageByDate.values.stream().sorted().collect(Collectors.toList())))
            )
        } else {
            log.info("no readings available")
        }
    }

    companion object {
        val log: Logger = LoggerFactory.getLogger(Octopus::class.java)
    }
}

fun main(args: Array<String>) {
    SpringApplication.run(Octopus::class.java, *args)
}