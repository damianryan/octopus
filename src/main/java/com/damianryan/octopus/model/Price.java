package com.damianryan.octopus.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.time.Instant;

@Data
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Price {

    @JsonProperty("value_exc_vat")
    double valueExcVAT;

    @JsonProperty("value_inc_vat")
    double valueIncVAT;

    @JsonProperty("valid_from")
    Instant validFrom;

    @JsonProperty("valid_to")
    Instant validTo;
}