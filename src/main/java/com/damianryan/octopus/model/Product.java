package com.damianryan.octopus.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.hateoas.Link;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Product implements Comparable<Product> {

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

    @JsonProperty("tariffs_active_at")
    Instant tariffsActiveAt;

    @JsonProperty("single_register_electricity_tariffs")
    Map<String, Map<String, Tariff>> singleRegisterElectricityTariffs;

    @Override
    public int compareTo(Product other) {
        return fullName.compareTo(other.getFullName());
    }
}