package com.damianryan.octopus.model

import com.fasterxml.jackson.annotation.JsonProperty

@JvmRecord data class Register(
    val identifier: String,
    val rate: String? = null,
    @JsonProperty("is_settlement_register") val settlementRegister: Boolean = false,
)
