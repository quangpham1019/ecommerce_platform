package com.quang.marketplace.modules.catalog.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

import com.quang.marketplace.shared.error.ValidationException;

public class ProductVariantOptionTest {

    private ProductVariant variant() {
        Product product = Product.createDraft(
            10L,
            "Product",
            "Description",
            "product-10"
        );

        ProductVariant variant = new ProductVariant(
            "SKU-1",
            new BigDecimal("10.00"));

        product.addVariant(variant);

        return variant;
    }

    @Test
    void newOption_requiresOptionName() {
        assertThrows(
            ValidationException.class,
            () -> new ProductVariantOption(null, "Large")
        );
    }

    @Test
    void newOption_requiresOptionValue() {
        assertThrows(
            ValidationException.class,
            () -> new ProductVariantOption("Size", null)
        );
    }

    @Test
    void newOption_withValidInputs_storesNameAndValue() {
        ProductVariantOption option = new ProductVariantOption(
            "Size",
            "Large"
        );

        assertEquals("Size", option.getOptionName());
        assertEquals("Large", option.getOptionValue());
    }

    @Test
    void assignToVariant_rejectsNullVariant() {
        ProductVariantOption option = new ProductVariantOption(
            "Size",
            "Large"
        );

        assertThrows(
            ValidationException.class,
            () -> option.assignToVariant(null)
        );
    }

    @Test
    void assignToVariant_acceptsValidVariant() {
        ProductVariantOption option = new ProductVariantOption(
            "Size",
            "Large"
        );

        option.assignToVariant(variant());

        // No public getter for productVariant, so this test mainly proves no exception.
    }
}