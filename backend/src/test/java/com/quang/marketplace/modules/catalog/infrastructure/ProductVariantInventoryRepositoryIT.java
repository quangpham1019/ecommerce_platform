package com.quang.marketplace.modules.catalog.infrastructure;

import com.quang.marketplace.AbstractIntegrationTest;
import com.quang.marketplace.modules.catalog.domain.Product;
import com.quang.marketplace.modules.catalog.domain.ProductVariant;
import com.quang.marketplace.modules.catalog.domain.ProductVariantInventory;
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
public class ProductVariantInventoryRepositoryIT extends AbstractIntegrationTest {

    @Autowired ProductRepository productRepo;
    @Autowired ProductVariantRepository variantRepo;
    @Autowired ProductVariantInventoryRepository inventoryRepo;
    @Autowired SellerProfileRepository sellerRepo;
    @Autowired UserRepository userRepo;

    @Test
    void saveVariantWithInventory_cascadesInventory() {
        ProductVariant variant = savedVariantWithInventory(10);

        ProductVariant reloaded = variantRepo.findById(variant.getId()).orElseThrow();

        assertThat(reloaded.getInventory()).isNotNull();
        assertThat(reloaded.getInventory().getOnHandQuantity()).isEqualTo(10);
        assertThat(reloaded.getInventory().getReservedQuantity()).isEqualTo(0);
        assertThat(reloaded.getInventory().getAvailableQuantity()).isEqualTo(10);
    }

    @Test
    void inventoryReservation_persistsAfterReload() {
        ProductVariant variant = savedVariantWithInventory(10);

        ProductVariant reloaded = variantRepo.findById(variant.getId()).orElseThrow();
        reloaded.getInventory().reserve(4);

        variantRepo.saveAndFlush(reloaded);

        ProductVariant again = variantRepo.findById(variant.getId()).orElseThrow();

        assertThat(again.getInventory().getOnHandQuantity()).isEqualTo(10);
        assertThat(again.getInventory().getReservedQuantity()).isEqualTo(4);
        assertThat(again.getInventory().getAvailableQuantity()).isEqualTo(6);
    }

    @Test
    void inventoryCommit_persistsAfterReload() {
        ProductVariant variant = savedVariantWithInventory(10);

        ProductVariant reloaded = variantRepo.findById(variant.getId()).orElseThrow();
        reloaded.getInventory().reserve(5);
        reloaded.getInventory().commitReservation(3);

        variantRepo.saveAndFlush(reloaded);

        ProductVariant again = variantRepo.findById(variant.getId()).orElseThrow();

        assertThat(again.getInventory().getOnHandQuantity()).isEqualTo(7);
        assertThat(again.getInventory().getReservedQuantity()).isEqualTo(2);
        assertThat(again.getInventory().getAvailableQuantity()).isEqualTo(5);
    }

    @Test
    void inventoryHasSameVariantAfterReload() {
        ProductVariant variant = savedVariantWithInventory(10);

        ProductVariant reloaded = variantRepo.findById(variant.getId()).orElseThrow();

        assertThat(reloaded.getInventory().getProductVariant()).isNotNull();
        assertThat(reloaded.getInventory().getProductVariant().getId()).isEqualTo(variant.getId());
    }

    private ProductVariant savedVariantWithInventory(int quantity) {
        User user = userRepo.save(new User(
            "inventory-" + System.nanoTime() + "@example.com",
            "pw"
        ));

        SellerProfile seller = sellerRepo.save(new SellerProfile(
            user.getId(),
            "Inventory Shop " + System.nanoTime()
        ));

        Product product = Product.createDraft(
            seller.getId(),
            "Inventory Product",
            "Description",
            "inventory-product-" + System.nanoTime()
        );

        ProductVariant variant = new ProductVariant(
            "SKU-INV-" + System.nanoTime(),
            BigDecimal.valueOf(9.99)
        );

        product.addVariant(variant);

        ProductVariantInventory inventory = new ProductVariantInventory(quantity);
        variant.assignInventory(inventory);

        productRepo.saveAndFlush(product);

        return variant;
    }
}