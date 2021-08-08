package com.damianryan.octopus.model;

import lombok.*;
import lombok.experimental.FieldDefaults;

@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProductStubWrapper extends Response<ProductStub>{
}