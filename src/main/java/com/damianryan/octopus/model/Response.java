package com.damianryan.octopus.model;

import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.lang.Nullable;

import java.util.List;

@Data
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PACKAGE)
public class Response<T> {

    long count;

    @Nullable
    String next;

    @Nullable
    String previous;

    List<T> results;
}