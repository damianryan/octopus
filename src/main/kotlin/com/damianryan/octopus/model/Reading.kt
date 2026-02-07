package com.damianryan.octopus.model

import com.fasterxml.jackson.annotation.JsonProperty
import java.time.Instant

data class Reading(
    val consumption: Double = 0.0,
    @JsonProperty("interval_start") val from: Instant? = null,
    @JsonProperty("interval_end") val to: Instant? = null,
) : Comparable<Reading> {
    override fun compareTo(other: Reading): Int {
        return from!!.compareTo(other.from!!)
    }
}