package com.damianryan.octopus.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SampleConsumption {

    @JsonProperty("electricity_single_rate")
    ElectricitySingleRate electricitySingleRate;

    @JsonProperty("electricity_dual_rate")
    ElectricityDualRate electricityDualRate;

    @JsonProperty("dual_fuel_single_rate")
    DualFuelSingleRate dualFuelSingleRate;

    @JsonProperty("dual_fuel_dual_rate")
    DualFuelDualRate dualFuelDualRate;
}