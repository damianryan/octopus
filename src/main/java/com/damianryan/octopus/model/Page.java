package com.damianryan.octopus.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.lang.Nullable;

import java.util.List;

@Data
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PACKAGE)
public class Page<T> {

    long count;

    @Nullable
    String next;

    @Nullable
    String previous;

    @JsonProperty("results")
    List<T> content;
}