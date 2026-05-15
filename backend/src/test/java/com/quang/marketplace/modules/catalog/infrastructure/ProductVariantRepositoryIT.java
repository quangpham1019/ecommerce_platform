package com.quang.marketplace.modules.catalog.infrastructure;

import com.quang.marketplace.AbstractIntegrationTest;
import com.quang.marketplace.modules.catalog.domain.Product;
import com.quang.marketplace.modules.catalog.domain.ProductVariant;
import com.quang.marketplace.modules.catalog.domain.ProductVariantOption;
import com.quang.marketplace.modules.catalog.domain.ProductVariantStatus;
import com.quang.marketplace.modules.catalog.domain.VariantImage;
import com.quang.marketplace.modules.seller.domain.SellerProfile;
import com.quang.marketplace.modules.seller.infrastructure.SellerProfileRepository;
import com.quang.marketplace.modules.identity.domain.User;
import com.quang.marketplace.modules.identity.infrastructure.UserRepository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Transactional
public class ProductVariantRepositoryIT extends AbstractIntegrationTest {

    @Autowired
    ProductVariantRepository variantRepo;

    @Autowired
    ProductRepository productRepo;

    @Autowired
    SellerProfileRepository sellerRepo;

    @Autowired
    UserRepository userRepo;

    @Test
    void saveVariant_requiresProduct() {
        ProductVariant variant = new ProductVariant(
            "SKU-1",
            new BigDecimal("10.00")
        );

        assertThrows(
            DataIntegrityViolationException.class,
            () -> variantRepo.saveAndFlush(variant)
        );

        // This test may not be needed if constructor already requires product.
        // DB-level test is mainly useful if product_id constraint is involved.
    }

    @Test
    void saveVariant_persistsStatusAndPrice() {
        Product product = Product.createDraft(
            1L,
            "Product",
            "Description",
            "variant-product"
        );

        ProductVariant variant = new ProductVariant(
            "SKU-1",
            new BigDecimal("10.00")
        );

        product.addVariant(variant);

        Product savedProduct = productRepo.saveAndFlush(product);

        ProductVariant reloaded = savedProduct.getVariants().get(0);

        assertEquals("SKU-1", reloaded.getSku());
        assertEquals(new BigDecimal("10.00"), reloaded.getPrice());
        assertEquals(ProductVariantStatus.ACTIVE, reloaded.getStatus());
        assertEquals("USD", reloaded.getCurrencyCode());
    }

    @Test
    void saveVariant_withOptions_cascadesOptions() {
        Product product = Product.createDraft(
            1L,
            "Product",
            "Description",
            "variant-options-product"
        );

        ProductVariant variant = new ProductVariant(
            "SKU-OPTIONS",
            new BigDecimal("10.00")
        );

        variant.addOption(new ProductVariantOption("Size", "Large"));
        variant.addOption(new ProductVariantOption("Color", "Black"));

        product.addVariant(variant);

        Product saved = productRepo.saveAndFlush(product);
        productRepo.flush();

        Product reloaded = productRepo.findById(saved.getId()).orElseThrow();
        ProductVariant reloadedVariant = reloaded.getVariants().get(0);

        assertEquals(2, reloadedVariant.getOptions().size());
        assertEquals("Black / Large", reloadedVariant.getVariantName());
    }

    @Test
    void saveVariant_withImages_cascadesImages() {
        Product product = Product.createDraft(
            1L,
            "Product",
            "Description",
            "variant-images-product"
        );

        ProductVariant variant = new ProductVariant(
            "SKU-IMAGE",
            new BigDecimal("10.00")
        );

        variant.addImage(new VariantImage(
            "https://example.com/image.jpg",
            "Front image",
            1
        ));

        product.addVariant(variant);

        Product saved = productRepo.saveAndFlush(product);

        Product reloaded = productRepo.findById(saved.getId()).orElseThrow();
        ProductVariant reloadedVariant = reloaded.getVariants().get(0);

        assertEquals(1, reloadedVariant.getImages().size());
    }

    @Test
    void existsByProductSellerProfileIdAndSkuIgnoreCase_returnsTrueForExistingSellerSku() {
        User user = new User(
            "variant-query@example.com",
            "password123"
        );

        userRepo.save(user);

        SellerProfile seller = new SellerProfile(
            user.getId(),
            "Variant Query Shop"
        );

        sellerRepo.save(seller);

        Product product = Product.createDraft(
            seller.getId(),
            "Product",
            "Description",
            "product-query"
        );

        ProductVariant variant = new ProductVariant(
            "SKU-ABC",
            BigDecimal.valueOf(9.99)
        );

        product.addVariant(variant);
        productRepo.save(product);

        boolean exists = variantRepo
            .existsByProductSellerProfileIdAndSkuIgnoreCase(
                seller.getId(),
                "sku-abc"
            );

        assertThat(exists).isTrue();
    }

    @Test
    void duplicateSkuWithinSameSeller_isRejectedByDatabase() {
        User user = new User(
            "duplicate-sku@example.com",
            "password123"
        );

        userRepo.save(user);

        SellerProfile seller = new SellerProfile(
            user.getId(),
            "Duplicate SKU Shop"
        );

        sellerRepo.save(seller);

        Product productOne = Product.createDraft(
            seller.getId(),
            "Product One",
            "Description",
            "product-one"
        );

        Product productTwo = Product.createDraft(
            seller.getId(),
            "Product Two",
            "Description",
            "product-two"
        );

        ProductVariant variantOne = new ProductVariant(
            "DUP-SKU",
            BigDecimal.valueOf(5.00)
        );
        productOne.addVariant(variantOne);
        productRepo.save(productOne);
        productRepo.save(productTwo);


        ProductVariant variantTwo = new ProductVariant(
            "DUP-SKU",
            BigDecimal.valueOf(6.00)
        );
        productTwo.addVariant(variantTwo);

        assertThrows(
            DataIntegrityViolationException.class,
            () -> productRepo.saveAndFlush(productTwo)
        );
    }
}
