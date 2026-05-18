package com.quang.marketplace.modules.cart.api;

import java.net.URI;
import java.time.Duration;
import java.util.UUID;

import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.HttpHeaders;
import com.quang.marketplace.modules.cart.application.CartService;
import com.quang.marketplace.modules.cart.domain.CartItem;
import com.quang.marketplace.shared.security.CurrentUserProvider;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/carts")
public class CartController {
    
    private final CartService cartService;
    private final CurrentUserProvider currentUserProvider;

    public CartController(CartService cartService, CurrentUserProvider currentUserProvider) {
        this.cartService = cartService;
        this.currentUserProvider = currentUserProvider;
    }

    @GetMapping("/active")
    public ResponseEntity<?> getActiveCart(
            @CookieValue(name = "guest_cart_token", required = false) String guestToken,
            HttpServletResponse response
    ) {
        Long userId = currentUserProvider.getCurrentUserId();

        if (userId == null) {
            guestToken = createGuestTokenIfNotExists(guestToken, response);
        }

        CartView cartView = cartService.getActiveCart(userId, guestToken);
        return ResponseEntity.ok(cartView);
    }

    @PostMapping("/items")
    public ResponseEntity<?> addItemToCart(
        @CookieValue(name = "guest_cart_token", required = false) String guestToken,
        HttpServletResponse response,
        @Valid @RequestBody AddItemToCartRequest request
    ) {
        Long userId = currentUserProvider.getCurrentUserId();

        if (userId == null) {
            guestToken = createGuestTokenIfNotExists(guestToken, response);
        }

        CartItem cartItem = cartService.addItemToCart(userId, guestToken, request);

        return ResponseEntity.created(URI.create("/api/carts/items/" + cartItem.getId())).body(cartItem.getId());
    }

    @PatchMapping("/items/{itemId}")
    public ResponseEntity<?> updateCartItem(
            @CookieValue(name = "guest_cart_token", required = false) String guestToken,
            HttpServletResponse response,
            @PathVariable Long itemId,
            @Valid @RequestBody UpdateCartItemRequest request
    ) {
        Long userId = currentUserProvider.getCurrentUserId();

        if (userId == null) {
            guestToken = createGuestTokenIfNotExists(guestToken, response);
        }

        CartItem cartItem = cartService.updateCartItem(userId, guestToken, itemId, request);

        return ResponseEntity.ok().body(cartItem.getId());
    }

    @DeleteMapping("/items/{itemId}")
    public ResponseEntity<?> removeCartItem(
            @CookieValue(name = "guest_cart_token", required = false) String guestToken,
            HttpServletResponse response,
            @PathVariable Long itemId
    ) {
        Long userId = currentUserProvider.getCurrentUserId();

        if (userId == null) {
            guestToken = createGuestTokenIfNotExists(guestToken, response);
        }

        cartService.removeCartItem(userId, guestToken, itemId);

        return ResponseEntity.ok().build();
    }

    @PostMapping("/clear")
    public ResponseEntity<?> clearCart(
            @CookieValue(name = "guest_cart_token", required = false) String guestToken,
            HttpServletResponse response
    ) {
        Long userId = currentUserProvider.getCurrentUserId();

        if (userId == null) {
            guestToken = createGuestTokenIfNotExists(guestToken, response);
        }

        cartService.clearCart(userId, guestToken);

        return ResponseEntity.ok().build();
    }

    private String createGuestTokenIfNotExists(String guestToken, HttpServletResponse response) {
        // Implementation for creating guest token if not exists

        if (guestToken != null && !guestToken.isBlank()) {
            return guestToken; // Guest token already exists, no need to create
        }

        guestToken = UUID.randomUUID().toString();

        ResponseCookie cookie = ResponseCookie.from("guest_cart_token", guestToken)
            .httpOnly(true)
            .secure(false) // Set to true in production with HTTPS
            .sameSite("Lax")
            .path("/")
            .maxAge(Duration.ofDays(30))
            .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        return guestToken;
    }
}
