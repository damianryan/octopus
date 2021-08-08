package com.damianryan.octopus.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;
import java.util.Map;

@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Product extends ProductStub {

    @JsonProperty("tariffs_active_at")
    Instant tariffsActiveAt;

    @JsonProperty("single_register_electricity_tariffs")
    Map<String, Map<String, Tariff>> singleRegisterElectricityTariffs;
}