package com.damianryan.octopus.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.lang.Nullable;

import java.time.Instant;
import java.util.List;

@Data
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Property {

    int id;

    @JsonProperty("moved_in_at")
    Instant movedInAt;

    @JsonProperty("moved_out_at")
    @Nullable
    Instant movedOutAt;

    @JsonProperty("address_line_1")
    String addressLine1;

    @JsonProperty("address_line_2")
    String addressLine2;

    @JsonProperty("address_line_3")
    String addressLine3;

    String town;

    String county;

    String postcode;

    @JsonProperty("electricity_meter_points")
    List<ElectricityMeterPoint> electricityMeterPoints;

    @JsonProperty("gas_meter_points")
    List<GasMeterPoint> gasMeterPoints;
}