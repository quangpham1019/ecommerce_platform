package com.quang.marketplace.modules.catalog.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

import com.quang.marketplace.shared.error.ValidationException;

public class ProductTest {
    
    @Test
    void newProduct_defaultsToDraftStatus() {
        Product p = Product.createDraft(
            10L,
            "My product",
            "Description",
            "my-product"
        );

        assertEquals(ProductStatus.DRAFT, p.getStatus());
        assertFalse(p.isPublished());
    }

    @Test
    void newProduct_requiresTitle() {
        assertThrows(
            ValidationException.class,
            () -> Product.createDraft(
                10L,
                null,
                "Description",
                "my-product"
            )
        );
    }

    @Test
    void newProduct_requiresDescription() {
        assertThrows(
            ValidationException.class,
            () -> Product.createDraft(
                10L,
                "My product",
                null,
                "my-product"
            )
        );
    }

    @Test
    void newProduct_requiresSellerProfile() {
        assertThrows(
            ValidationException.class,
            () -> Product.createDraft(
                null,
                "My product",
                "Description",
                "my-product"
            )
        );
    }
    @Test
    void newProduct_requiresSlug() {
        assertThrows(
            ValidationException.class,
            () -> Product.createDraft(
                10L,
                "My product",
                "Description",
                null
            )
        );
    }

    @Test
    void publish_failsWithoutVariant() {
        Product p = Product.createDraft(10L, "Name", "Desc", "name-10");
        org.junit.jupiter.api.Assertions.assertThrows(ValidationException.class, p::publish);
    }

    @Test
    void publish_withAtLeastOneVariant_setsStatusToPublished() {
        Product p = Product.createDraft(10L, "Name", "Desc", "name-10");
        ProductVariant v = new ProductVariant("SKU1", new BigDecimal("1.00"));

        p.addVariant(v);
        p.publish();

        assertEquals(ProductStatus.PUBLISHED, p.getStatus());
        assertTrue(p.isPublished());
    }

    @Test
    void unpublish_setsStatusBackToDraft() {
        Product p = Product.createDraft(10L, "Name", "Desc", "name-10");
        ProductVariant v = new ProductVariant("SKU1", new BigDecimal("1.00"));
        p.addVariant(v);
        p.publish();

        p.unpublish();

        assertEquals(ProductStatus.DRAFT, p.getStatus());
        assertFalse(p.isPublished());
    }

    @Test
    void updateDetails_rejectsBlankName() {
        Product p = Product.createDraft(10L, "Name", "Desc", "name-10");

        assertThrows(
            ValidationException.class,
            () -> p.updateDetails("", "Desc")
        );
    }

    @Test
    void updateDetails_rejectsBlankDescription() {
        Product p = Product.createDraft(10L, "Name", "Desc", "name-10");

        assertThrows(
            ValidationException.class,
            () -> p.updateDetails("Name", "")
        );
    }

    @Test
    void addVariant_addsVariantToProduct() {
        Product p = Product.createDraft(10L, "Name", "Desc", "name-10");
        ProductVariant v = new ProductVariant("SKU1", new BigDecimal("1.00"));

        p.addVariant(v);

        assertEquals(1, p.getVariants().size());
        assertEquals(v, p.getVariants().get(0));
    }

    @Test
    void addVariant_rejectsNullVariant() {
        Product p = Product.createDraft(10L, "Name", "Desc", "name-10");

        assertThrows(
            ValidationException.class,
            () -> p.addVariant(null)
        );
    }

}
