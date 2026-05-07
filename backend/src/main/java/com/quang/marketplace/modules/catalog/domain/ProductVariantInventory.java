package com.quang.marketplace.modules.catalog.domain;

import jakarta.persistence.*;
import java.time.Instant;

// TODO: consider moving to Inventory domain since Inventory has its own rules and logic that may grow more complex over time, and we may want to have separate services/controllers for inventory management in the future
@Entity
@Table(name = "product_variant_inventories")
public class ProductVariantInventory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "product_variant_id", nullable = false)
    private Long productVariantId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_variant_id", insertable = false, updatable = false)
    private ProductVariant productVariant;

    @Column(name = "on_hand_quantity", nullable = false)
    private int onHandQuantity = 0;

    @Column(name = "reserved_quantity", nullable = false)
    private int reservedQuantity = 0;

    @Column(name = "reorder_threshold", nullable = false)
    private int reorderThreshold = 0;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected ProductVariantInventory() {}

    public ProductVariantInventory(Long productVariantId, int onHandQuantity) {
        this.productVariantId = productVariantId;
        this.onHandQuantity = onHandQuantity;
    }

    @PrePersist
    void onCreate() { this.updatedAt = Instant.now(); }

    @PreUpdate
    void onUpdate() { this.updatedAt = Instant.now(); }

    public Long getId() { return id; }
    public Long getProductVariantId() { return productVariantId; }
    public ProductVariant getProductVariant() { return productVariant; }
    public int getOnHandQuantity() { return onHandQuantity; }
    public void setOnHandQuantity(int onHandQuantity) { this.onHandQuantity = onHandQuantity; }
    public int getReservedQuantity() { return reservedQuantity; }
    public void setReservedQuantity(int reservedQuantity) { this.reservedQuantity = reservedQuantity; }
    public int getReorderThreshold() { return reorderThreshold; }
    public void setReorderThreshold(int reorderThreshold) { this.reorderThreshold = reorderThreshold; }
}
