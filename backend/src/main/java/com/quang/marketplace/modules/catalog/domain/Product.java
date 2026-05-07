package com.quang.marketplace.modules.catalog.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

//TODO: add ProductStatus enum and use it for status field
@Entity
@Table(name = "products")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "seller_profile_id", nullable = false)
    private Long sellerProfileId;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true, length = 280)
    private String slug;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProductStatus status = ProductStatus.DRAFT;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductVariant> variants = new ArrayList<>();

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductImage> images = new ArrayList<>();

    protected Product() {}

    public Product(Long sellerProfileId, String name, String description) {
        this.sellerProfileId = sellerProfileId;
        this.name = name;
        this.description = description;
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

    public void addImage(ProductImage image) {
        image.assignToProduct(this);
        images.add(image);
    }

    public Long getId() { return id; }
    public Long getSellerProfileId() { return sellerProfileId; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public boolean isPublished() { return "PUBLISHED".equalsIgnoreCase(status.name()); }
    public List<ProductVariant> getVariants() { return variants; }
    public List<ProductImage> getProductImages() { return images; }
    public String getStatus() { return status.name(); }

    public void publish() {
        if (name == null || name.isBlank()) {
            throw new IllegalStateException("Published product must have a name");
        }

        if (description == null || description.isBlank()) {
            throw new IllegalStateException("Published product must have a description");
        }

        if (variants.isEmpty()) {
            throw new IllegalStateException("Published product must have at least one variant");
        }

        // TODO: add inventory check in application service to ensure at least one variant has available inventory before allowing publish
        // boolean hasAvailableVariant = variants.stream()
        //     .anyMatch(ProductVariant::hasAvailableInventory);

        // if (!hasAvailableVariant) {
        //     throw new IllegalStateException("Published product must have at least one variant with available inventory");
        // }

        this.status = ProductStatus.PUBLISHED;
    }

    public void unpublish() {
        this.status = ProductStatus.DRAFT;
    }

    public void updateDetails(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public void addVariant(ProductVariant variant) {
        variants.add(variant);
        variant.assignToProduct(this);
    }
}
