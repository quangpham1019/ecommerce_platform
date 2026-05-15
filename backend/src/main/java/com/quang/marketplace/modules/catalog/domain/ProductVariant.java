package com.quang.marketplace.modules.catalog.domain;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import com.quang.marketplace.shared.error.ValidationException;

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

    @OneToMany(mappedBy = "productVariant", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sortOrder ASC")
    private List<ProductVariantOption> options = new ArrayList<>();

    @Column(nullable = false)
    private BigDecimal price = BigDecimal.ZERO;

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

    public ProductVariant(String sku, BigDecimal price) {
        requireSku(sku);
        requirePositivePrice(price);

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

    public void updatePrice(BigDecimal price) {
        requirePositivePrice(price);
        this.price = price;
    }

    public void activate() {
        this.status = ProductVariantStatus.ACTIVE;
    }

    public void deactivate() {
        this.status = ProductVariantStatus.INACTIVE;
    }

    public boolean isActive() {
        return this.status == ProductVariantStatus.ACTIVE;
    }

    void assignToProduct(Product product) {
        requireProduct(product);
        this.product = product;
    }

    public void assignInventory(ProductVariantInventory inventory) {
        if (inventory == null) {
            throw new ValidationException("Inventory is required");
        }

        inventory.assignToVariant(this);
        this.inventory = inventory;
    }

    public void addImage(VariantImage image) {
        if (image == null) {
            throw new ValidationException("Variant image is required");
        }

        image.assignToVariant(this);
        images.add(image);
    }

    public void addOption(ProductVariantOption option) {
        if (option == null) {
            throw new ValidationException("Variant option is required");
        }

        option.assignToVariant(this);
        options.add(option);
        rebuildVariantName();
    }

    private void rebuildVariantName() {
        this.variantName = options.stream()
            .sorted(Comparator.comparing(
                o -> o.getOptionName().toLowerCase()
            ))
            .map(ProductVariantOption::getOptionValue)
            .collect(Collectors.joining(" / "));
    }

    private void requireProduct(Product product) {
        if (product == null) {
            throw new ValidationException("Product is required");
        }
    }

    private void requireSku(String sku) {
        if (sku == null || sku.isBlank()) {
            throw new ValidationException("SKU is required");
        }
    }

    private void requirePositivePrice(BigDecimal price) {
        if (price == null || price.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidationException("Price must be larger than zero");
        }
    }

    public Long getId() {
        return id;
    }

    public Product getProduct() {
        return product;
    }

    public String getSku() {
        return sku;
    }

    public String getVariantName() {
        return variantName;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }

    public ProductVariantStatus getStatus() {
        return status;
    }

    public List<ProductVariantOption> getOptions() {
        return options;
    }

    public List<VariantImage> getImages() {
        return images;
    }

    public ProductVariantInventory getInventory() {
        return inventory;
    }
}