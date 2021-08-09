package com.damianryan.octopus.util;

import com.damianryan.octopus.model.Price;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

import java.time.Duration;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@EqualsAndHashCode
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Prices implements Comparable<Prices> {

    public static Prices of(String name, Price standingCharge, List<Price> standardUnitRates) {
        standardUnitRates.sort(Comparator.comparing(Price::getValueIncVAT));
        return new Prices(name,
                          StandardUnitRatePrice.of(standardUnitRates.get(0)),
                          StandardUnitRatePrice.of(standardUnitRates.get(1)),
                          StandingChargePrice.of(standingCharge));
    }

    String name;

    StandardUnitRatePrice lowStandardUnitRate;

    StandardUnitRatePrice highStandardUnitRate;

    StandingChargePrice standingCharge;

    public double amountValue() {
        return standingCharge.getValueIncVAT() + amount(lowStandardUnitRate) + amount(highStandardUnitRate);
    }

    @SuppressWarnings("unused")
    private String amount() {
        return String.format("%.2fp", amountValue());
    }

    private double amount(Price price) {
        Instant validFrom = price.getValidFrom();
        Instant validTo = price.getValidTo();
        return Duration.between(validFrom, validTo).toHours() * price.getValueIncVAT();
    }

    @Override
    public int compareTo(Prices other) {
        return Double.compare(amountValue(), other.amountValue());
    }

    @Override
    public String toString() {
        return name + " (1kW/h for 24h = " + amount() + "), low rate = " + lowStandardUnitRate +
               ", high rate = " + highStandardUnitRate + ", standing charge = " + standingCharge;
    }
}