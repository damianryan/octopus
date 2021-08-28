package com.damianryan.octopus.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DualFuelSingleRate {

    @JsonProperty("electricity_standard")
    int electricityStandard;

    @JsonProperty("gas_standard")
    int gasStandard;
}