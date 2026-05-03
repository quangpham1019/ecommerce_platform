package com.quang.marketplace.modules.catalog.domain;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "product_variants")
public class ProductVariant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(nullable = false)
    private String sku;

    @Column(nullable = false)
    private java.math.BigDecimal price = java.math.BigDecimal.ZERO;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected ProductVariant() {}

    public ProductVariant(Long productId, String sku, java.math.BigDecimal price) {
        this.productId = productId;
        this.sku = sku;
        this.price = price;
    }

    @PrePersist
    void onCreate() {
        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        this.updatedAt = Instant.now();
    }

    public Long getId() { return id; }
    public Long getProductId() { return productId; }
    public String getSku() { return sku; }
    public java.math.BigDecimal getPrice() { return price; }
}
