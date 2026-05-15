package com.quang.marketplace.modules.catalog.infrastructure;

import com.quang.marketplace.AbstractIntegrationTest;
import com.quang.marketplace.modules.catalog.domain.Product;
import com.quang.marketplace.modules.catalog.domain.ProductStatus;
import com.quang.marketplace.modules.catalog.domain.ProductVariant;
import com.quang.marketplace.modules.identity.domain.User;
import com.quang.marketplace.modules.seller.domain.SellerProfile;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

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