package com.damianryan.octopus.model

import com.fasterxml.jackson.annotation.JsonProperty

@Suppress("unused") data class GridSupplyPoint(@JsonProperty("group_id") val groupId: String? = null)
