package com.quang.marketplace.modules.cart.api;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record AddItemToCartRequest(
    @NotNull Long productVariantId,
    @NotNull @Min(1) Integer quantity
) {
    
}
