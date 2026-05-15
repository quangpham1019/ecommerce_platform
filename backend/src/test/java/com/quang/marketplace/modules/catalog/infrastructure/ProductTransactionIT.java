package com.quang.marketplace.modules.catalog.infrastructure;

import com.quang.marketplace.modules.catalog.api.AddVariantRequest;
import com.quang.marketplace.modules.catalog.application.ProductService;
import com.quang.marketplace.modules.catalog.domain.Product;
import com.quang.marketplace.modules.catalog.domain.ProductVariant;
import com.quang.marketplace.modules.identity.domain.User;
import com.quang.marketplace.modules.seller.domain.SellerProfile;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;

@SpringBootTest
@org.springframework.transaction.annotation.Transactional
public class ProductTransactionIT extends com.quang.marketplace.AbstractIntegrationTest {

    @Autowired
    ProductService productService;

    @Autowired
    ProductRepository productRepo;

    @Autowired
    ProductVariantRepository variantRepo;

    @Autowired
    com.quang.marketplace.modules.seller.infrastructure.SellerProfileRepository sellerRepo;

    @Autowired
    com.quang.marketplace.modules.identity.infrastructure.UserRepository userRepo;

    @MockBean
    ProductVariantInventoryRepository inventoryRepo;

    @Test
    public void addVariant_whenInventorySaveFails_rollsBackVariant() {
        User u = new User("txuser@example.com", "pw");
        userRepo.save(u);
        SellerProfile s = new SellerProfile(u.getId(), "TxShop");
        sellerRepo.save(s);
        Product p = Product.createDraft(s.getId(), "Name","D", "name-tx");
        productRepo.save(p);

        AddVariantRequest req = new AddVariantRequest("DUPTX", BigDecimal.valueOf(5.00), 5, List.of());

        doThrow(new RuntimeException("DB down")).when(inventoryRepo).save(org.mockito.ArgumentMatchers.any());

        assertThrows(RuntimeException.class, () -> {
            productService.addVariant(u.getId(), p.getId(), req);
        });

        // ensure variant not persisted
        assertThat(variantRepo.findBySku("DUPTX")).isEmpty();
    }

    @Test
    public void publishProduct_whenValidationFails_leavesProductDraft() {
        User u = new User("pubuser@example.com", "pw");
        userRepo.save(u);
        SellerProfile s = new SellerProfile(u.getId(), "PubShop");
        sellerRepo.save(s);
        Product p = Product.createDraft(s.getId(), "Name","D", "name-pub");
        productRepo.save(p);

        // attempt to publish without variants -> BusinessRuleException thrown
        assertThrows(RuntimeException.class, () -> productService.publishProduct(u.getId(), p.getId()));

        // product remains draft
        Product re = productRepo.findById(p.getId()).get();
        assertThat(re.isPublished()).isFalse();
    }
}
