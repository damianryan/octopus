package com.damianryan.octopus;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "octopus")
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
class OctopusProperties {

    String accountUrl;

    String productsUrl;

    String electricityConsumptionUrl;

    String electricityTariffsUrl;

    String gasConsumptionUrl;

    String gasTariffsUrl;

    String greenFixedRateProductsUrl;

    String apiKey;

    String region;

    String tariffType;

    String standingChargesPath;

    String standardUnitRatesPath;

    String fixedRateProductCode;

    String goProductCode;
}