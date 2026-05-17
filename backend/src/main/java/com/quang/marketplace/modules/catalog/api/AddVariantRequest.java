package com.quang.marketplace.modules.catalog.api;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;
import java.util.List;

public record AddVariantRequest(
    String sku,

    @NotNull
    @Positive
    BigDecimal price,

    @NotNull
    @PositiveOrZero
    Integer quantity,

    List<@Valid VariantOptionRequest> options
) {}
