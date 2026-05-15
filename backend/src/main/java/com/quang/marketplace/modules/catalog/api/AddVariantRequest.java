package com.quang.marketplace.modules.catalog.api;

import java.math.BigDecimal;
import java.util.List;

public record AddVariantRequest(
        String sku,
        BigDecimal price,
        Integer quantity,
        List<VariantOptionRequest> options
) {}
