package com.quang.marketplace.modules.catalog.domain;

import com.quang.marketplace.shared.error.ValidationException;

import jakarta.persistence.*;


@Entity
@Table(name = "product_variant_options")
public class ProductVariantOption {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_variant_id", nullable = false)
    private ProductVariant productVariant;

    @Column(name = "option_name", nullable = false)
    private String optionName;

    @Column(name = "option_value", nullable = false)
    private String optionValue;

    protected ProductVariantOption() {}

    public ProductVariantOption(String optionName, String optionValue) {
        
        if (optionName == null || optionName.isBlank()) {
            throw new ValidationException("Option name is required");
        }

        if (optionValue == null || optionValue.isBlank()) {
            throw new ValidationException("Option value is required");
        }

        this.optionName = optionName;
        this.optionValue = optionValue;
    }

    void assignToVariant(ProductVariant variant) {

        if (variant == null) {
            throw new ValidationException("Product variant is required");
        }

        this.productVariant = variant;
    }

    public String getOptionName() { return optionName; }
    public String getOptionValue() { return optionValue; }
}