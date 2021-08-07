package com.damianryan.octopus.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.hateoas.Link;

import java.util.List;

@Data
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Tariff {

    String code;

    @JsonProperty("standing_charge_exc_vat")
    double standingChargeExcVAT;

    @JsonProperty("standing_charge_inc_vat")
    double standingChargeIncVAT;

    @JsonProperty("online_discount_exc_vat")
    double onlineDiscountExcVAT;

    @JsonProperty("online_discount_inc_vat")
    double onlineDiscountIncVAT;

    @JsonProperty("dual_fuel_discount_exc_vat")
    double dualFuelDiscountExcVAT;

    @JsonProperty("dual_fuel_discount_inc_vat")
    double dualFuelDiscountIncVAT;

    @JsonProperty("exit_fees_exc_vat")
    double exitFeesExcVAT;

    @JsonProperty("exit_fees_inc_vat")
    double exitFeesIncVAT;

    List<Link> links;

    @JsonProperty("standing_unit_rate_exc_vat")
    double standardUnitRateExcVAT;

    @JsonProperty("standing_unit_rate_inc_vat")
    double standardUnitRateIncVAT;
}