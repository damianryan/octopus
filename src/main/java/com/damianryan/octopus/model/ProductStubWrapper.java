package com.damianryan.octopus.model;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProductStubWrapper extends Response<ProductStub> {
}