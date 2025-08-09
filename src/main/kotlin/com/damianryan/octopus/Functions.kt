package com.damianryan.octopus

import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

fun s(count: Int): String = if (count == 1) "" else "s"

fun toInstant(date: LocalDate): Instant = date.atStartOfDay(ZoneId.systemDefault()).toInstant()

fun toLocalDate(time: Instant?): LocalDate? = LocalDate.ofInstant(time, ZoneId.systemDefault())

fun toLocalDateTime(time: Instant?): LocalDateTime? = LocalDateTime.ofInstant(time, ZoneId.systemDefault())

fun formatLocalDateTime(time: Instant?): String? = DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(toLocalDateTime(time))

fun twoDP(number: Double): String = String.format("%.2f", number)

fun kWh(kWh: Double): String = String.format("%.2fkWh", kWh)

fun median(numbers: List<Double>?): Double {
    val scratch: MutableList<Double> = ArrayList(numbers!!)
    scratch.sort()
    scratch.removeAt(0) // remove the small reading for the final day that only includes one 30-minute period
    val count = scratch.size
    val isOdd = 0 != count % 2
    val midPoint = count / 2
    return if (isOdd) {
        scratch[midPoint]
    } else {
        (scratch[midPoint - 1] + scratch[midPoint]) / 2
    }
}
