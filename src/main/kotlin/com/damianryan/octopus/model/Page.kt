package com.damianryan.octopus.model

import com.fasterxml.jackson.annotation.JsonProperty

open class Page<T> {
    val count: Long = 0
    val next: String? = null
    val previous: String? = null
    @JsonProperty("results") val content: List<T>? = null

    override fun toString(): String {
        return "${javaClass.simpleName}(previous: $previous, next: $next, content: ${content?.size}/$count)"
    }
}