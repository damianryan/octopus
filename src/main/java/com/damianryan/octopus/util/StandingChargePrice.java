package com.damianryan.octopus.util;

import com.damianryan.octopus.model.Price;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

@RequiredArgsConstructor(staticName = "of")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class StandingChargePrice extends Price {

    public static StandingChargePrice of(Price price) {
        return new StandingChargePrice(price.getValueExcVAT(), price.getValueIncVAT(), price.getValidFrom(), price.getValidTo());
    }

    private StandingChargePrice(double valueExcVAT, double valueIncVAT, Instant validFrom, Instant validTo) {
        super(valueExcVAT, valueIncVAT, validFrom, validTo);
    }

    @Override
    public String toString() {
        return String.format("%.3fp as of %s", getValueIncVAT(), LocalDate.ofInstant(getValidFrom(), ZoneId.systemDefault()));
    }
}