package com.damianryan.octopus.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SampleQuote {

    @JsonProperty("electricity_single_rate")
    AnnualCost electricitySingleRate;

    @JsonProperty("electricity_dual_rate")
    AnnualCost electricityDualRate;

    @JsonProperty("dual_fuel_single_rate")
    AnnualCost dualFuelSingleRate;

    @JsonProperty("dual_fuel_dual_rate")
    AnnualCost dualFuelDualRate;
}