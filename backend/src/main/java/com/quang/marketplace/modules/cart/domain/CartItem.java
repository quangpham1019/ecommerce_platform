package com.quang.marketplace.modules.cart.domain;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "cart_items")
public class CartItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_id", insertable = false, updatable = false)
    private Cart cart;

    @Column(name = "product_variant_id", nullable = false)
    private Long productVariantId;
    @Column(nullable = false)
    private int quantity;

    @Column(name = "added_at", nullable = false, updatable = false)
    private Instant addedAt;

    protected CartItem() {}

    public CartItem(Long productVariantId, int quantity) {
        this.productVariantId = productVariantId;
        this.quantity = quantity;
    }

    @PrePersist
    void onCreate() { this.addedAt = Instant.now(); }

    public Long getId() { return id; }
    public Cart getCart() { return cart; }
    public Long getProductVariantId() { return productVariantId; }
    public int getQuantity() { return quantity; }

    public void setCart(Cart cart) {
        this.cart = cart;
    }

    public void updateQuantity(int quantity) {
    if (quantity <= 0) {
        throw new IllegalArgumentException("Quantity must be > 0");
    }
    this.quantity = quantity;
}
    public Instant getAddedAt() {
        return addedAt;
    }
    
}
