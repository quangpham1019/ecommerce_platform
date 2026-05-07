package com.quang.marketplace.modules.catalog.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "product_variants")
public class ProductVariant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false)
    private String sku;

    @Column(name = "variant_name", nullable = false)
    private String variantName = "";

    @Column(nullable = false)
    private java.math.BigDecimal price = java.math.BigDecimal.ZERO;

    @Column(name = "currency_code", nullable = false, length = 3, columnDefinition = "CHAR(3)")
    private String currencyCode = "USD";

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProductVariantStatus status = ProductVariantStatus.ACTIVE;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @OneToMany(mappedBy = "productVariant", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<VariantImage> images = new ArrayList<>();

    @OneToOne(mappedBy = "productVariant", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private ProductVariantInventory inventory;

    protected ProductVariant() {}

    public ProductVariant(Product product, String sku, java.math.BigDecimal price) {
        this.product = product;
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
    public Product getProduct() { return product; }
    public String getSku() { return sku; }
    public java.math.BigDecimal getPrice() { return price; }
    public List<VariantImage> getImages() { return images; }
    public ProductVariantInventory getInventory() { return inventory; }

    void assignToProduct(Product product) {
        this.product = product;
    }

    public void addImage(VariantImage image) {
        image.assignToVariant(this);
        images.add(image);
    }
}
