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
public class Reading implements Comparable<Reading> {

    double consumption;

    @JsonProperty("interval_start")
    Instant from;

    @JsonProperty("interval_end")
    Instant to;

    @Override
    public int compareTo(Reading other) {
        return from.compareTo(other.getFrom());
    }
}