package com.damianryan.octopus;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "octopus")
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
class OctopusProperties {

    String baseUrl;

    String electricityUrl;

    String gasUrl;

    String accountUrl;

    String productsUrl;

    String apiKey;

    String region;

    String tariffType;

    String getElectricityUrl() {
        return baseUrl + electricityUrl;
    }

    String getGasUrl() {
        return baseUrl + gasUrl;
    }

    String getAccountUrl() {
        return baseUrl + accountUrl;
    }

    String getProductsUrl() {
        return baseUrl + productsUrl;
    }
}