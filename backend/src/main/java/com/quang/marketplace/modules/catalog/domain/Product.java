package com.quang.marketplace.modules.catalog.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import com.quang.marketplace.shared.error.ValidationException;

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

    private Product(Long sellerProfileId, String name, String description, String slug) {

        if (sellerProfileId == null) {
            throw new ValidationException("Seller profile ID is required");
        }
        if (name == null || name.isBlank()) {
            throw new ValidationException("Product name is required");
        }
        if (description == null || description.isBlank()) {
            throw new ValidationException("Product description is required");
        }
        if (slug == null || slug.isBlank()) {
            throw new ValidationException("Product slug is required");
        }

        this.sellerProfileId = sellerProfileId;
        this.name = name;
        this.description = description;
        this.slug = slug;
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

    public static Product createDraft(Long sellerProfileId, String name, String description, String slug) {
        return new Product(sellerProfileId, name, description, slug);
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
    public ProductStatus getStatus() { return status; }

    public void publish() {
        if (name == null || name.isBlank()) {
            throw new ValidationException("Published product must have a name");
        }

        if (description == null || description.isBlank()) {
            throw new ValidationException("Published product must have a description");
        }

        if (variants.isEmpty()) {
            throw new ValidationException("Published product must have at least one variant");
        }

        this.status = ProductStatus.PUBLISHED;
    }

    public void unpublish() {
        this.status = ProductStatus.DRAFT;
    }

    public void updateDetails(String name, String description) {
        if (name == null || name.isBlank()) {
            throw new ValidationException("Product name is required");
        }

        if (description == null || description.isBlank()) {
            throw new ValidationException("Product description is required");
        }

        this.name = name;
        this.description = description;
    }

    public void addVariant(ProductVariant variant) {
        if (variant == null) {
            throw new ValidationException("Product variant is required");
        }

        variants.add(variant);
        variant.assignToProduct(this);
    }

}
