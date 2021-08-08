package com.damianryan.octopus.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.hateoas.Link;

import java.time.Instant;
import java.util.List;

@Data
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PACKAGE)
public class ProductStub implements Comparable<ProductStub> {

    String code;

    String direction;

    @JsonProperty("full_name")
    String fullName;

    @JsonProperty("display_name")
    String displayName;

    @JsonProperty("is_variable")
    boolean variable;

    @JsonProperty("is_green")
    boolean green;

    @JsonProperty("is_tracker")
    boolean tracker;

    @JsonProperty("is_prepay")
    boolean prepay;

    @JsonProperty("is_business")
    boolean business;

    @JsonProperty("is_restricted")
    boolean restricted;

    int term;

    @JsonProperty("available_from")
    Instant availableFrom;

    @JsonProperty("available_to")
    Instant availableTo;

    List<Link> links;

    String brand;

    @Override
    public int compareTo(ProductStub other) {
        return fullName.compareTo(other.getFullName());
    }
}