package com.quang.marketplace.modules.cart.api;

import java.math.BigDecimal;
import java.util.List;

import com.quang.marketplace.modules.cart.domain.Cart;

public record CartView(
    List<CartItemView> items,
    BigDecimal totalPrice
) {
    
    // Will be implemented later and invoked in CartService.getActiveCart method
    public static CartView fromCart(Cart cart) {
        // List<CartItemView> itemViews = cart.getItems().stream()
        //     .map(item -> new CartItemView(
        //         item.getId(),
        //         item.getProductVariantId(),
        //         item.get(),
        //         item.getQuantity(),
        //         item.getUnitPrice(),
        //         item.getTotalPrice()
        //     ))
        //     .toList();

        // BigDecimal totalPrice = itemViews.stream()
        //     .map(CartItemView::totalPrice)
        //     .reduce(BigDecimal.ZERO, BigDecimal::add);

        // return new CartView(itemViews, totalPrice);

        return null;
    }
}
