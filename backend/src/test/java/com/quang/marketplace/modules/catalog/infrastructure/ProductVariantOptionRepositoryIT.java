package com.quang.marketplace.modules.catalog.infrastructure;

import com.quang.marketplace.AbstractIntegrationTest;
import com.quang.marketplace.modules.catalog.domain.Product;
import com.quang.marketplace.modules.catalog.domain.ProductVariant;
import com.quang.marketplace.modules.catalog.domain.ProductVariantOption;
import com.quang.marketplace.modules.identity.domain.User;
import com.quang.marketplace.modules.identity.infrastructure.UserRepository;
import com.quang.marketplace.modules.seller.domain.SellerProfile;
import com.quang.marketplace.modules.seller.infrastructure.SellerProfileRepository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
public class ProductVariantOptionRepositoryIT extends AbstractIntegrationTest {

    @Autowired ProductRepository productRepo;
    @Autowired ProductVariantRepository variantRepo;
    @Autowired SellerProfileRepository sellerRepo;
    @Autowired UserRepository userRepo;

    @Test
    void saveVariantWithOptions_cascadesOptions() {
        ProductVariant variant = savedVariantWithOptions();

        ProductVariant reloaded = variantRepo.findById(variant.getId()).orElseThrow();

        assertThat(reloaded.getOptions()).hasSize(2);
        assertThat(reloaded.getOptions())
            .extracting(ProductVariantOption::getOptionName)
            .containsExactlyInAnyOrder("Size", "Color");

        assertThat(reloaded.getOptions())
            .extracting(ProductVariantOption::getOptionValue)
            .containsExactlyInAnyOrder("Large", "Black");
    }

    @Test
    void saveVariantWithOptions_persistsAlphabeticalVariantName() {
        ProductVariant variant = savedVariantWithOptions();

        ProductVariant reloaded = variantRepo.findById(variant.getId()).orElseThrow();

        assertThat(reloaded.getVariantName()).isEqualTo("Black / Large");
    }

    @Test
    void optionHasSameVariantAfterReload() {
        ProductVariant variant = savedVariantWithOptions();

        ProductVariant reloaded = variantRepo.findById(variant.getId()).orElseThrow();

        ProductVariantOption option = reloaded.getOptions().get(0);

        assertThat(option.getOptionName()).isNotBlank();
        assertThat(option.getOptionValue()).isNotBlank();
    }

    private ProductVariant savedVariantWithOptions() {
        User user = userRepo.save(new User(
            "option-" + System.nanoTime() + "@example.com",
            "pw"
        ));

        SellerProfile seller = sellerRepo.save(new SellerProfile(
            user.getId(),
            "Option Shop " + System.nanoTime()
        ));

        Product product = Product.createDraft(
            seller.getId(),
            "Option Product",
            "Description",
            "option-product-" + System.nanoTime()
        );

        ProductVariant variant = new ProductVariant(
            "SKU-OPT-" + System.nanoTime(),
            BigDecimal.valueOf(9.99)
        );

        product.addVariant(variant);

        variant.addOption(new ProductVariantOption("Size", "Large"));
        variant.addOption(new ProductVariantOption("Color", "Black"));

        productRepo.saveAndFlush(product);

        return variant;
    }
}