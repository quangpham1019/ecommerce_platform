package com.quang.marketplace.modules.order.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "order_items")
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "seller_order_id", nullable = false)
    private SellerOrder sellerOrder;

    @Column(name = "product_variant_id", nullable = false)
    private Long productVariantId;

    @Column(nullable = false)
    private int quantity;

    @Column(name = "unit_price_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal unitPriceAmount;

    @Column(name = "currency_code", nullable = false, length = 3, columnDefinition = "CHAR(3)")
    private String currencyCode = "USD";

    @Column(name = "product_name_snapshot", nullable = false)
    private String productNameSnapshot;

    @Column(name = "variant_name_snapshot", nullable = false)
    private String variantNameSnapshot;

    @Column(name = "sku_snapshot", nullable = false)
    private String skuSnapshot;

    @Column(name = "image_url_snapshot")
    private String imageUrlSnapshot;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected OrderItem() {}

    public OrderItem(
            Long productVariantId,
            int quantity,
            BigDecimal unitPriceAmount,
            String productNameSnapshot,
            String variantNameSnapshot,
            String skuSnapshot,
            String imageUrlSnapshot
    ) {
        if (productVariantId == null) {
            throw new IllegalArgumentException("Product variant is required");
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero");
        }
        if (unitPriceAmount == null || unitPriceAmount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Unit price cannot be negative");
        }
        if (productNameSnapshot == null || productNameSnapshot.isBlank()) {
            throw new IllegalArgumentException("Product name snapshot is required");
        }
        if (variantNameSnapshot == null || variantNameSnapshot.isBlank()) {
            throw new IllegalArgumentException("Variant name snapshot is required");
        }
        if (skuSnapshot == null || skuSnapshot.isBlank()) {
            throw new IllegalArgumentException("SKU snapshot is required");
        }

        this.productVariantId = productVariantId;
        this.quantity = quantity;
        this.unitPriceAmount = unitPriceAmount;
        this.productNameSnapshot = productNameSnapshot;
        this.variantNameSnapshot = variantNameSnapshot;
        this.skuSnapshot = skuSnapshot;
        this.imageUrlSnapshot = imageUrlSnapshot;
    }

    public BigDecimal getLineTotalAmount() {
        return unitPriceAmount.multiply(BigDecimal.valueOf(quantity));
    }

    void setSellerOrder(SellerOrder sellerOrder) {
        this.sellerOrder = sellerOrder;
    }

    @PrePersist
    void onCreate() {
        this.createdAt = Instant.now();
    }

    public Long getId() { return id; }

    public SellerOrder getSellerOrder() { return sellerOrder; }

    public Long getProductVariantId() { return productVariantId; }

    public int getQuantity() { return quantity; }

    public BigDecimal getUnitPriceAmount() { return unitPriceAmount; }

    public String getCurrencyCode() { return currencyCode; }

    public String getProductNameSnapshot() { return productNameSnapshot; }

    public String getVariantNameSnapshot() { return variantNameSnapshot; }

    public String getSkuSnapshot() { return skuSnapshot; }

    public String getImageUrlSnapshot() { return imageUrlSnapshot; }

    public Instant getCreatedAt() { return createdAt; }
}