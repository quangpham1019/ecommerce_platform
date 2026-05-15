package com.quang.marketplace.modules.catalog.api;

import com.quang.marketplace.modules.catalog.domain.ProductVariantOption;

public record VariantOptionRequest(
        String name,
        String value,
        Integer sortOrder
) {
        public static ProductVariantOption toDomain(VariantOptionRequest request) {
                return new ProductVariantOption(
                        request.name().trim(),
                        request.value().trim()
                );
        }
}