package com.quang.marketplace.modules.catalog.api;

import com.quang.marketplace.modules.catalog.domain.ProductVariantOption;

import jakarta.validation.constraints.NotNull;

public record VariantOptionRequest(

        @NotNull
        String name,

        @NotNull
        String value,

        @NotNull
        Integer sortOrder
) {
        public static ProductVariantOption toDomain(VariantOptionRequest request) {
                return new ProductVariantOption(
                        request.name().trim(),
                        request.value().trim()
                );
        }
}