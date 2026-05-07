package com.quang.marketplace.modules.cart.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "carts")
public class Cart {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "guest_token")
    private String guestToken;

    @Column(name = "status", nullable = false)
    private String status = "ACTIVE";

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CartItem> items = new ArrayList<>();

    protected Cart() {}

    public Cart(Long userId, String guestToken) {
        this.userId = userId;
        this.guestToken = guestToken;
    }

    @PrePersist
    void onCreate() { Instant now = Instant.now(); this.createdAt = now; this.updatedAt = now; }

    @PreUpdate
    void onUpdate() { this.updatedAt = Instant.now(); }

    public Long getId() { return id; }
    public Long getUserId() { return userId; }
    public String getGuestToken() { return guestToken; }
    public String getStatus() { return status; }
    public List<CartItem> getItems() { return items; }

    public void addItem(CartItem item) {
        item.setCart(this);
        items.add(item);
    }

    public void removeItem(CartItem item) {
        items.remove(item);
        item.setCart(null);
    }

    public void updateItemQuantity(Long variantId, int quantity) {
        CartItem item = findItem(variantId);
        item.updateQuantity(quantity);
    }

    private CartItem findItem(Long productVariantId) {
        return items.stream()
            .filter(item -> item.getProductVariantId().equals(productVariantId))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Item not found in cart"));
    }
}
