package com.quang.marketplace.modules.catalog;

import com.quang.marketplace.modules.catalog.application.ProductService;
import com.quang.marketplace.modules.catalog.infrastructure.InventoryRepository;
import com.quang.marketplace.modules.catalog.infrastructure.ProductRepository;
import com.quang.marketplace.modules.catalog.infrastructure.ProductVariantRepository;
import com.quang.marketplace.modules.identity.infrastructure.UserRepository;
import com.quang.marketplace.modules.seller.infrastructure.SellerProfileRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@org.springframework.transaction.annotation.Transactional
public class ProductPublishTest extends com.quang.marketplace.AbstractIntegrationTest {

    @Autowired
    ProductService productService;

    @Autowired
    UserRepository userRepository;

    @Autowired
    SellerProfileRepository sellerProfileRepository;

    @Autowired
    ProductRepository productRepository;

    @Autowired
    ProductVariantRepository variantRepository;

    @Autowired
    InventoryRepository inventoryRepository;

    @Test
    @Transactional
    void publish_requires_instock_variant() {
        // create a user
        var user = userRepository.save(new com.quang.marketplace.modules.identity.domain.User("pubtest@example.com", "pw"));

        // create seller profile
        var seller = sellerProfileRepository.save(new com.quang.marketplace.modules.seller.domain.SellerProfile(user.getId(), "Seller Pub"));

        var product = productService.createProduct(user.getId(), "P1", "desc");

        // add variant with zero inventory
        var v = productService.addVariant(user.getId(), product.getId(), "SKU-1", BigDecimal.valueOf(10), 0);

        // publish should fail
        assertThrows(org.springframework.web.server.ResponseStatusException.class, () -> productService.publishProduct(user.getId(), product.getId()));

        // increase inventory by adding another variant with qty
        var v2 = productService.addVariant(user.getId(), product.getId(), "SKU-2", BigDecimal.valueOf(5), 3);

        // now publish succeeds
        productService.publishProduct(user.getId(), product.getId());

        var p = productRepository.findById(product.getId()).get();
        assertThat(p.isPublished()).isTrue();
    }
}
