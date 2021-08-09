package com.damianryan.octopus.util;

import com.damianryan.octopus.model.Price;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class StandardUnitRatePrice extends Price {

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    public static StandardUnitRatePrice of(Price price) {
        return new StandardUnitRatePrice(price.getValueExcVAT(), price.getValueIncVAT(), price.getValidFrom(), price.getValidTo());
    }

    private StandardUnitRatePrice(double valueExcVAT, double valueIncVAT, Instant validFrom, Instant validTo) {
        super(valueExcVAT, valueIncVAT, validFrom, validTo);
    }

    @Override
    public String toString() {
        return String.format("%.3fp between %s and %s", getValueIncVAT(), hourMinute(getValidFrom()), hourMinute(getValidTo()));
    }

    private String hourMinute(Instant time) {
        return TIME_FORMATTER.format(LocalTime.ofInstant(time, ZoneId.systemDefault()));
    }
}