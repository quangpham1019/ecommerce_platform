package com.quang.marketplace.modules.cart.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.quang.marketplace.modules.cart.application.CartService;
import com.quang.marketplace.shared.security.CurrentUserProvider;

@RestController
@RequestMapping("/api/checkout")
public class CheckoutController {
    
    private final CartService cartService;
    private final CurrentUserProvider currentUserProvider;

    public CheckoutController(CartService cartService, CurrentUserProvider currentUserProvider) {
        this.cartService = cartService;
        this.currentUserProvider = currentUserProvider;
    }

    // ** Should be moved to OrderController when we implement order management **
    // @PostMapping("/checkout")
    // public ResponseEntity<?> checkoutCart() {
    //     Long userId = currentUserProvider.getCurrentUserId();
    //     cartService.checkoutCart(userId);

    //    return ResponseEntity.ok().build();
    // }
}
