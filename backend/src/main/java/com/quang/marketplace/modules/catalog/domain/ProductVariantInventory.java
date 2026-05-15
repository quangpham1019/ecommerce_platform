package com.quang.marketplace.modules.catalog.domain;

import jakarta.persistence.*;
import java.time.Instant;

import com.quang.marketplace.shared.error.BusinessRuleException;
import com.quang.marketplace.shared.error.ValidationException;

@Entity
@Table(name = "product_variant_inventories")
public class ProductVariantInventory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_variant_id", nullable = false)
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

    public ProductVariantInventory(int onHandQuantity) {

        if (onHandQuantity < 0) {
            throw new ValidationException("On-hand quantity cannot be negative");
        }

        this.onHandQuantity = onHandQuantity;
    }

    @PrePersist
    void onCreate() {
        this.updatedAt = Instant.now();
    }

    @PreUpdate
    void onUpdate() {
        this.updatedAt = Instant.now();
    }

    void assignToVariant(ProductVariant variant) {
        if (variant == null) {
            throw new ValidationException("Product variant is required");
        }

        this.productVariant = variant;
    }

    public int getAvailableQuantity() {
        return onHandQuantity - reservedQuantity;
    }

    public boolean hasAvailableQuantity(int quantity) {
        return quantity > 0 && getAvailableQuantity() >= quantity;
    }

    public void adjustOnHandQuantity(int delta) {
        int newOnHandQuantity = this.onHandQuantity + delta;

        if (newOnHandQuantity < 0) {
            throw new BusinessRuleException("On-hand quantity cannot drop below zero");
        }

        if (newOnHandQuantity < this.reservedQuantity) {
            throw new BusinessRuleException("On-hand quantity cannot be less than reserved quantity");
        }

        this.onHandQuantity = newOnHandQuantity;
    }

    public void reserve(int quantity) {
        requirePositiveQuantity(quantity);

        if (getAvailableQuantity() < quantity) {
            throw new BusinessRuleException("Insufficient available inventory");
        }

        this.reservedQuantity += quantity;
    }

    public void releaseReservation(int quantity) {
        requirePositiveQuantity(quantity);

        if (quantity > this.reservedQuantity) {
            throw new BusinessRuleException("Cannot release more inventory than reserved");
        }

        this.reservedQuantity -= quantity;
    }

    public void commitReservation(int quantity) {
        requirePositiveQuantity(quantity);

        if (quantity > this.reservedQuantity) {
            throw new BusinessRuleException("Cannot commit more inventory than reserved");
        }

        this.reservedQuantity -= quantity;
        this.onHandQuantity -= quantity;
    }

    public void updateReorderThreshold(int reorderThreshold) {
        if (reorderThreshold < 0) {
            throw new ValidationException("Reorder threshold cannot be negative");
        }

        this.reorderThreshold = reorderThreshold;
    }

    private void requirePositiveQuantity(int quantity) {
        if (quantity <= 0) {
            throw new ValidationException("Quantity must be greater than zero");
        }
    }

    public Long getId() {
        return id;
    }

    public Long getProductVariantId() {
        return productVariant == null ? null : productVariant.getId();
    }

    public ProductVariant getProductVariant() {
        return productVariant;
    }

    public int getOnHandQuantity() {
        return onHandQuantity;
    }

    public int getReservedQuantity() {
        return reservedQuantity;
    }

    public int getReorderThreshold() {
        return reorderThreshold;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}