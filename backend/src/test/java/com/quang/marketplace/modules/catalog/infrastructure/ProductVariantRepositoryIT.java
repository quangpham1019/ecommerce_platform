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
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
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
    void saveVariant_persistsProductId() {
        Product product = Product.createDraft(
            1L,
            "Product " + System.nanoTime(),
            "Description",
            "variant-product-id" + "-" + System.nanoTime()
        );

        ProductVariant variant = new ProductVariant(
            "SKU-PRODUCT-ID",
            new BigDecimal("10.00")
        );

        product.addVariant(variant);

        productRepo.saveAndFlush(product);

        ProductVariant reloaded = variantRepo.findById(variant.getId()).orElseThrow();

        assertEquals(product.getId(), reloaded.getProduct().getId());
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
    void existsByProductIdAndSkuIgnoreCase_returnsTrueForExistingProductSku() {
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
        productRepo.saveAndFlush(product);

        boolean exists = variantRepo
            .existsByProductIdAndSkuIgnoreCase(
                product.getId(),
                "sku-abc"
            );

        assertThat(exists).isTrue();
    }

    @Test
    void duplicateSkuWithinSameProduct_isRejectedByDatabase() {
        User user = new User(
            "duplicate-sku" + System.nanoTime() + "@example.com",
            "password123"
        );

        userRepo.save(user);

        SellerProfile seller = new SellerProfile(
            user.getId(),
            "Seller Shop" + System.nanoTime()
        );

        sellerRepo.save(seller);

        Product productOne = Product.createDraft(
            seller.getId(),
            "Product One",
            "Description",
            "product-one"
        );
        
        ProductVariant variantOne = new ProductVariant(
            "DUP-SKU",
            BigDecimal.valueOf(5.00)
        );
        productOne.addVariant(variantOne);
        productRepo.save(productOne);

        ProductVariant variantTwo = new ProductVariant(
            "DUP-SKU",
            BigDecimal.valueOf(6.00)
        );
        productOne.addVariant(variantTwo);

        assertThrows(
            DataIntegrityViolationException.class,
            () -> productRepo.saveAndFlush(productOne)
        );
    }

    @Test
    void sameSkuAllowedAcrossDifferentProducts() {
        User userOne = userRepo.save(new User(
            "seller-one-" + System.nanoTime() + "@example.com",
            "pw"
        ));

        SellerProfile sellerOne = sellerRepo.save(new SellerProfile(
            userOne.getId(),
            "Seller One " + System.nanoTime()
        ));

        Product productOne = Product.createDraft(
            sellerOne.getId(),
            "Product One",
            "Description",
            "product-one-" + System.nanoTime()
        );

        ProductVariant variantOne = new ProductVariant(
            "SHARED-SKU",
            new BigDecimal("10.00")
        );

        productOne.addVariant(variantOne);
        productRepo.saveAndFlush(productOne);

        Product productTwo = Product.createDraft(
            sellerOne.getId(),
            "Product Two",
            "Description",
            "product-two-" + System.nanoTime()
        );

        ProductVariant variantTwo = new ProductVariant(
            "SHARED-SKU",
            new BigDecimal("12.00")
        );

        productTwo.addVariant(variantTwo);

        assertDoesNotThrow(() -> productRepo.saveAndFlush(productTwo));
    }
}
