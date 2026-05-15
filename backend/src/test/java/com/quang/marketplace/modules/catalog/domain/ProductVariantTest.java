package com.quang.marketplace.modules.catalog.domain;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

import com.quang.marketplace.shared.error.ValidationException;

public class ProductVariantTest {

    private Product product() {
        return Product.createDraft(
            10L,
            "Product",
            "Description",
            "product-10"
        );
    }

    @Test
    void newVariant_requiresSku() {
        assertThrows(
            ValidationException.class,
            () -> new ProductVariant(null, new BigDecimal("10.00"))
        );
    }

    @Test
    void newVariant_requiresPositivePrice() {
        assertThrows(
            ValidationException.class,
            () -> new ProductVariant("SKU-1", BigDecimal.ZERO)
        );
    }

    @Test
    void newVariant_withValidInputs_defaultsToActiveStatus() {
        ProductVariant variant = new ProductVariant(
            "SKU-1",
            new BigDecimal("10.00")
        );

        assertEquals(ProductVariantStatus.ACTIVE, variant.getStatus());
        assertTrue(variant.isActive());
    }

    @Test
    void newVariant_withValidInputs_storesSkuAndPrice() {
        ProductVariant variant = new ProductVariant(
            "SKU-1",
            new BigDecimal("10.00")
        );

        assertEquals("SKU-1", variant.getSku());
        assertEquals(new BigDecimal("10.00"), variant.getPrice());
        assertEquals("USD", variant.getCurrencyCode());
    }

    @Test
    void updatePrice_rejectsNullPrice() {
        ProductVariant variant = new ProductVariant(
            "SKU-1",
            new BigDecimal("10.00")
        );

        assertThrows(
            ValidationException.class,
            () -> variant.updatePrice(null)
        );
    }

    @Test
    void updatePrice_rejectsZeroPrice() {
        ProductVariant variant = new ProductVariant(
            "SKU-1",
            new BigDecimal("10.00")
        );

        assertThrows(
            ValidationException.class,
            () -> variant.updatePrice(BigDecimal.ZERO)
        );
    }

    @Test
    void updatePrice_rejectsNegativePrice() {
        ProductVariant variant = new ProductVariant(
            "SKU-1",
            new BigDecimal("10.00")
        );

        assertThrows(
            ValidationException.class,
            () -> variant.updatePrice(new BigDecimal("-1.00"))
        );
    }

    @Test
    void updatePrice_withPositivePrice_updatesPrice() {
        ProductVariant variant = new ProductVariant(
            "SKU-1",
            new BigDecimal("10.00")
        );

        variant.updatePrice(new BigDecimal("15.00"));

        assertEquals(new BigDecimal("15.00"), variant.getPrice());
    }

    @Test
    void deactivate_setsStatusToInactive() {
        ProductVariant variant = new ProductVariant(
            "SKU-1",
            new BigDecimal("10.00")
        );

        variant.deactivate();

        assertEquals(ProductVariantStatus.INACTIVE, variant.getStatus());
        assertFalse(variant.isActive());
    }

    @Test
    void activate_setsStatusToActive() {
        ProductVariant variant = new ProductVariant(
            "SKU-1",
            new BigDecimal("10.00")
        );

        variant.deactivate();
        variant.activate();

        assertEquals(ProductVariantStatus.ACTIVE, variant.getStatus());
        assertTrue(variant.isActive());
    }

    @Test
    void addImage_rejectsNullImage() {
        ProductVariant variant = new ProductVariant(
            "SKU-1",
            new BigDecimal("10.00")
        );

        assertThrows(
            ValidationException.class,
            () -> variant.addImage(null)
        );
    }

    @Test
    void addImage_addsImageToVariant() {
        ProductVariant variant = new ProductVariant(
            "SKU-1",
            new BigDecimal("10.00")
        );

        VariantImage image = new VariantImage(
            "https://example.com/image.jpg",
            "Front view",
            1
        );

        variant.addImage(image);

        assertEquals(1, variant.getImages().size());
        assertEquals(image, variant.getImages().get(0));
    }

    @Test
    void addOption_rejectsNullOption() {
        ProductVariant variant = new ProductVariant(
            "SKU-1",
            new BigDecimal("10.00")
        );

        assertThrows(
            ValidationException.class,
            () -> variant.addOption(null)
        );
    }
@Test
void addOption_addsOptionToVariant() {
    ProductVariant variant = new ProductVariant(
        "SKU-1",
        new BigDecimal("10.00")
    );

    ProductVariantOption option = new ProductVariantOption(
        "Size",
        "Large"
    );

    variant.addOption(option);

    assertEquals(1, variant.getOptions().size());
    assertTrue(variant.getOptions().contains(option));
}

    @Test
    void addOption_rebuildsVariantNameAlphabeticallyByOptionName() {
        ProductVariant variant = new ProductVariant(
            "SKU-1",
            new BigDecimal("10.00")
        );

        ProductVariantOption size = new ProductVariantOption(
            "Size",
            "Large"
        );

        ProductVariantOption color = new ProductVariantOption(
            "Color",
            "Black"
        );

        variant.addOption(size);
        variant.addOption(color);

        assertEquals("Black / Large", variant.getVariantName());
    }
}