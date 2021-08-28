package com.damianryan.octopus.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AnnualCost {

    @JsonProperty("annual_cost_inc_vat")
    int incVAT;

    @JsonProperty("annual_cost_exc_vat")
    int excVAT;
}