package com.damianryan.octopus.util;

import com.damianryan.octopus.model.Price;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@EqualsAndHashCode
@ToString()
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Prices implements Comparable<Prices>{

    public static Prices of(String name, Price standingCharge, List<Price> standardUnitRates) {
        return new Prices(name, standingCharge, standardUnitRates.get(0), standardUnitRates.get(1));
    }
    @ToString.Include(rank = 1)
    String name;

    Price standingCharge;

    Price standardUnitRate1;

    Price standardUnitRate2;

    private double amountValue() {
        return standingCharge.getValueIncVAT() + amount(standardUnitRate1) + amount(standardUnitRate2);
    }

    @ToString.Include(rank = 2)
    public String amount() {
        return String.format("%.2f", amountValue());
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
}