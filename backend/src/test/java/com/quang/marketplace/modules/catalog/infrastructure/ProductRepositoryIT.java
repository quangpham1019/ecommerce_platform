package com.quang.marketplace.modules.catalog.infrastructure;

import com.quang.marketplace.AbstractIntegrationTest;
import com.quang.marketplace.modules.catalog.domain.Product;
import com.quang.marketplace.modules.catalog.domain.ProductStatus;
import com.quang.marketplace.modules.catalog.domain.ProductVariant;
import com.quang.marketplace.modules.catalog.domain.ProductVariantInventory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class ProductRepositoryIT extends AbstractIntegrationTest {

    @Autowired ProductRepository productRepo;

    @Test
    void saveProduct_persistsDraftStatus() {
        Product product = Product.createDraft(
            1L,
            "Product",
            "Description",
            "product-slug"
        );

        Product saved = productRepo.saveAndFlush(product);

        Product reloaded = productRepo.findById(saved.getId()).orElseThrow();

        assertEquals(ProductStatus.DRAFT, reloaded.getStatus());
        assertFalse(reloaded.isPublished());
    }

    @Test
    void saveProduct_persistsSellerProfileId() {
        Product product = Product.createDraft(
            123L,
            "Product",
            "Description",
            "product-seller-id"
        );

        Product saved = productRepo.saveAndFlush(product);

        Product reloaded = productRepo.findById(saved.getId()).orElseThrow();

        assertEquals(123L, reloaded.getSellerProfileId());
    }

    @Test
    void saveProduct_requiresUniqueSlug() {
        productRepo.saveAndFlush(Product.createDraft(
            1L,
            "Product A",
            "Description",
            "same-slug"
        ));

        Product duplicate = Product.createDraft(
            1L,
            "Product B",
            "Description",
            "same-slug"
        );

        assertThrows(
            DataIntegrityViolationException.class,
            () -> productRepo.saveAndFlush(duplicate)
        );
    }

    @Test
    void saveProduct_withVariant_cascadesVariant() {
        Product product = Product.createDraft(
            1L,
            "Product",
            "Description",
            "product-with-variant"
        );

        ProductVariant variant = new ProductVariant(
            "SKU-1",
            new BigDecimal("10.00")
        );

        product.addVariant(variant);

        Product saved = productRepo.saveAndFlush(product);

        Product reloaded = productRepo.findById(saved.getId()).orElseThrow();

        assertEquals(1, reloaded.getVariants().size());
        assertEquals("SKU-1", reloaded.getVariants().get(0).getSku());
    }

    @Test
    void publish_persistsPublishedStatus() {
        Product product = Product.createDraft(
            1L,
            "Published Product",
            "Description",
            "published-product"
        );

        ProductVariant variant = new ProductVariant(
            "SKU-PUBLISHED",
            new BigDecimal("10.00")
        );

        product.addVariant(variant);
        product.publish();

        Product saved = productRepo.saveAndFlush(product);

        Product reloaded = productRepo.findById(saved.getId()).orElseThrow();

        assertEquals(ProductStatus.PUBLISHED, reloaded.getStatus());
        assertTrue(reloaded.isPublished());
    }

    @Test
    void productWithVariantsAndInventoryCanBeReloadedForPublishValidation() {
        Product product = Product.createDraft(
            1L,
            "Inventory Product",
            "Description",
            "inventory-product"
        );

        ProductVariant variant = new ProductVariant(
            "SKU-INVENTORY",
            new BigDecimal("10.00")
        );

        variant.assignInventory(new ProductVariantInventory(5));
        product.addVariant(variant);

        Product saved = productRepo.saveAndFlush(product);

        Product reloaded = productRepo.findById(saved.getId()).orElseThrow();

        assertEquals(1, reloaded.getVariants().size());

        ProductVariant reloadedVariant = reloaded.getVariants().get(0);

        assertNotNull(reloadedVariant.getInventory());
        assertEquals(5, reloadedVariant.getInventory().getOnHandQuantity());
        assertEquals(5, reloadedVariant.getInventory().getAvailableQuantity());
    }

    @Test
    void removeVariant_fromProduct_deletesVariantBecauseOrphanRemoval() {
        Product product = Product.createDraft(
            1L,
            "Product",
            "Description",
            "product-orphan"
        );

        ProductVariant variant = new ProductVariant(
            "SKU-ORPHAN",
            new BigDecimal("10.00")
        );

        product.addVariant(variant);

        Product saved = productRepo.saveAndFlush(product);
        productRepo.flush();

        saved.getVariants().clear();

        productRepo.saveAndFlush(saved);

        Product reloaded = productRepo.findById(saved.getId()).orElseThrow();

        assertEquals(0, reloaded.getVariants().size());
    }
}