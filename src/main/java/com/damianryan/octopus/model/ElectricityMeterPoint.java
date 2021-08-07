package com.damianryan.octopus.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ElectricityMeterPoint {

    String mpan;

    @JsonProperty("profile_class")
    int profileClass;

    @JsonProperty("consumption_standard")
    int consumptionStandard;

    List<ElectricityMeter> meters;

    List<Agreement> agreements;
}