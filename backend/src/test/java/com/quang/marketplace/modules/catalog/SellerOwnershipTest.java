package com.quang.marketplace.modules.catalog;

import com.quang.marketplace.modules.catalog.application.ProductService;
import com.quang.marketplace.modules.identity.infrastructure.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@org.springframework.transaction.annotation.Transactional
class SellerOwnershipTest extends com.quang.marketplace.AbstractIntegrationTest {

    @Autowired
    ProductService productService;

    @Autowired
    UserRepository userRepository;

    @Test
    void cannot_create_product_without_seller_profile() {
        var user = userRepository.save(new com.quang.marketplace.modules.identity.domain.User("no-seller@example.com", "pw"));

        assertThrows(org.springframework.web.server.ResponseStatusException.class,
                () -> productService.createProduct(user.getId(), "Title", "desc"));
    }
}
