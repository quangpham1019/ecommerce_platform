package com.quang.marketplace.modules.cart.api;

import java.math.BigDecimal;

public record CartItemView(
    Long id,
    Long productVariantId,
    String productName,
    String variantName,
    int quantity,
    BigDecimal unitPrice,
    BigDecimal totalPrice
) {
    
}
