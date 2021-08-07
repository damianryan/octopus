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
public class GasMeterPoint {

    String mprn;

    @JsonProperty("consumption_standard")
    int consumptionStandard;

    List<GasMeter> meters;

    List<Agreement> agreements;
}