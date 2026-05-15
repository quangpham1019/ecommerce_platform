package com.quang.marketplace.modules.catalog.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.quang.marketplace.modules.catalog.api.AddVariantRequest;
import com.quang.marketplace.modules.catalog.api.CreateProductRequest;
import com.quang.marketplace.modules.catalog.api.VariantOptionRequest;
import com.quang.marketplace.modules.catalog.domain.Product;
import com.quang.marketplace.modules.catalog.domain.ProductVariant;
import com.quang.marketplace.modules.catalog.domain.ProductVariantInventory;
import com.quang.marketplace.modules.catalog.infrastructure.ProductRepository;
import com.quang.marketplace.modules.catalog.infrastructure.ProductVariantInventoryRepository;
import com.quang.marketplace.modules.catalog.infrastructure.ProductVariantRepository;
import com.quang.marketplace.modules.seller.domain.SellerProfile;
import com.quang.marketplace.modules.seller.infrastructure.SellerProfileRepository;
import com.quang.marketplace.shared.error.BusinessRuleException;
import com.quang.marketplace.shared.error.ConflictException;
import com.quang.marketplace.shared.error.ForbiddenOperationException;
import com.quang.marketplace.shared.error.ResourceNotFoundException;

public class ProductServiceTest {

    @Test
    @DisplayName("createProduct_requiresActiveSellerProfile")
    void createProduct_requiresActiveSellerProfile() {
        var productRepo = mock(ProductRepository.class);
        var variantRepo = mock(ProductVariantRepository.class);
        var inventoryRepo = mock(ProductVariantInventoryRepository.class);
        var sellerRepo = mock(SellerProfileRepository.class);

        var service = new ProductService(productRepo, variantRepo, inventoryRepo, sellerRepo);

        when(sellerRepo.findByUserIdAndStatus(anyLong(), any()))
            .thenReturn(Optional.empty());

        assertThrows(ForbiddenOperationException.class,
            () -> service.createProduct(1L, new CreateProductRequest("T","D")));
    }

    @Test
    @DisplayName("createProduct_createsDraftProductForActiveSeller")
    void createProduct_createsDraftProductForActiveSeller() {
        var productRepo = mock(ProductRepository.class);
        var variantRepo = mock(ProductVariantRepository.class);
        var inventoryRepo = mock(ProductVariantInventoryRepository.class);
        var sellerRepo = mock(SellerProfileRepository.class);

        var seller = mock(SellerProfile.class);
        when(seller.getId()).thenReturn(99L);
        when(sellerRepo.findByUserIdAndStatus(anyLong(), any()))
            .thenReturn(Optional.of(seller));

        when(productRepo.save(any()))
            .thenAnswer(i -> i.getArgument(0));

        var service = new ProductService(productRepo, variantRepo, inventoryRepo, sellerRepo);
        var saved = service.createProduct(1L, new CreateProductRequest("T","D"));
        assertEquals("DRAFT", saved.getStatus());
        assertEquals(99L, saved.getSellerProfileId());
    }

    @Test
    @DisplayName("addVariant_failsWhenProductNotFound")
    void addVariant_failsWhenProductNotFound() {
        var productRepo = mock(ProductRepository.class);
        var variantRepo = mock(ProductVariantRepository.class);
        var inventoryRepo = mock(ProductVariantInventoryRepository.class);

        var sellerRepo = mock(SellerProfileRepository.class);

        var service = new ProductService(productRepo, variantRepo, inventoryRepo, sellerRepo);
        when(productRepo.findById(anyLong())).thenReturn(Optional.empty());

        var req = new AddVariantRequest("SKU", new java.math.BigDecimal("1.00"), 0, List.of());
        assertThrows(ResourceNotFoundException.class,
                () -> service.addVariant(1L, 1L, req));
    }

    @Test
    @DisplayName("addVariant_failsWhenSellerProfileInactiveOrMissing")
    void addVariant_failsWhenSellerProfileInactiveOrMissing() {
        var productRepo = mock(ProductRepository.class);
        var variantRepo = mock(ProductVariantRepository.class);
        var inventoryRepo = mock(ProductVariantInventoryRepository.class);

        var sellerRepo = mock(SellerProfileRepository.class);

        var product = Product.createDraft(10L, "Name", "Desc", "name-2");
        when(productRepo.findById(anyLong()))
            .thenReturn(Optional.of(product));

        when(sellerRepo.findByUserIdAndStatus(anyLong(), any()))
            .thenReturn(Optional.empty());

        var service = new ProductService(productRepo, variantRepo, inventoryRepo, sellerRepo);
        var req = new AddVariantRequest("SKU", new java.math.BigDecimal("1.00"), 0, List.of());

        assertThrows(ForbiddenOperationException.class,
            () -> service.addVariant(1L, 1L, req));
    }

    @Test
    @DisplayName("addVariant_failsWhenUserDoesNotOwnProduct")
    void addVariant_failsWhenUserDoesNotOwnProduct() {
        var productRepo = mock(ProductRepository.class);
        var variantRepo = mock(ProductVariantRepository.class);
        var inventoryRepo = mock(ProductVariantInventoryRepository.class);

        var sellerRepo = mock(SellerProfileRepository.class);

        var product = Product.createDraft(10L, "Name", "Desc", "name-2");
        when(productRepo.findById(anyLong()))
            .thenReturn(Optional.of(product));

        var seller = mock(SellerProfile.class);
        when(seller.getId()).thenReturn(999L);
        when(sellerRepo.findByUserIdAndStatus(anyLong(), any()))
            .thenReturn(Optional.of(seller));

        var service = new ProductService(productRepo, variantRepo, inventoryRepo, sellerRepo);
        var req = new AddVariantRequest("SKU", new java.math.BigDecimal("1.00"), 0, List.of());

        assertThrows(ForbiddenOperationException.class,
            () -> service.addVariant(1L, 1L, req));
    }

    @Test
    @DisplayName("addVariant_generatesSkuWhenSkuMissing")
    void addVariant_generatesSkuWhenSkuMissing() {
        var productRepo = mock(ProductRepository.class);
        var variantRepo = mock(ProductVariantRepository.class);
        var inventoryRepo = mock(ProductVariantInventoryRepository.class);

        var sellerRepo = mock(SellerProfileRepository.class);

        var product = Product.createDraft(10L, "Name", "Desc", "name-2");
        when(productRepo.findById(anyLong()))
            .thenReturn(Optional.of(product));

        var seller = mock(SellerProfile.class);
        when(seller.getId()).thenReturn(10L);
        when(seller.getCode()).thenReturn("SHP");
        when(sellerRepo.findByUserIdAndStatus(anyLong(), any()))
            .thenReturn(Optional.of(seller));

        when(variantRepo.existsByProductSellerProfileIdAndSkuIgnoreCase(anyLong(), anyString()))
            .thenReturn(false);

        when(variantRepo.save(any()))
            .thenAnswer(i -> {
                var arg = (ProductVariant) i.getArgument(0);
                org.springframework.test.util.ReflectionTestUtils.setField(arg, "id", 123L);
                return arg;
            });

        when(inventoryRepo.save(any()))
            .thenAnswer(i -> i.getArgument(0));

        var service = new ProductService(productRepo, variantRepo, inventoryRepo, sellerRepo);

        var options = List.of(new VariantOptionRequest("Color", "Blue", 1));
        var req = new AddVariantRequest(null, new java.math.BigDecimal("5.00"), 3, options);

        var saved = service.addVariant(1L, 1L, req);
        // sku should be generated and contain seller code and product id
        assertTrue(saved.getSku().startsWith("SHP-"));
    }

    @Test
    @DisplayName("addVariant_rejectsDuplicateSkuWithinSellerCatalog")
    void addVariant_rejectsDuplicateSkuWithinSellerCatalog() {
        var productRepo = mock(ProductRepository.class);
        var variantRepo = mock(ProductVariantRepository.class);
        var inventoryRepo = mock(ProductVariantInventoryRepository.class);

        var sellerRepo = mock(SellerProfileRepository.class);

        var product = Product.createDraft(10L, "Name", "Desc", "name-2");
        when(productRepo.findById(anyLong()))
            .thenReturn(Optional.of(product));

        var seller = mock(SellerProfile.class);
        when(seller.getId()).thenReturn(10L);
        when(sellerRepo.findByUserIdAndStatus(anyLong(), any()))
            .thenReturn(Optional.of(seller));

        when(variantRepo.existsByProductSellerProfileIdAndSkuIgnoreCase(anyLong(), anyString()))
            .thenReturn(true);

        var service = new ProductService(productRepo, variantRepo, inventoryRepo, sellerRepo);
        var req = new AddVariantRequest("SKU123", new java.math.BigDecimal("5.00"), 1, List.of());

        assertThrows(ConflictException.class,
            () -> service.addVariant(1L, 1L, req));
    }

    @Test
    @DisplayName("addVariant_persistsVariantOptions")
    void addVariant_persistsVariantOptions() {
        var productRepo = mock(ProductRepository.class);
        var variantRepo = mock(ProductVariantRepository.class);
        var inventoryRepo = mock(ProductVariantInventoryRepository.class);

        var sellerRepo = mock(SellerProfileRepository.class);

        var product = Product.createDraft(10L, "Name", "Desc", "name-2");
        when(productRepo.findById(anyLong()))
            .thenReturn(Optional.of(product));

        var seller = mock(SellerProfile.class);
        when(seller.getId()).thenReturn(10L);
        when(sellerRepo.findByUserIdAndStatus(anyLong(), any()))
            .thenReturn(Optional.of(seller));

        when(variantRepo.existsByProductSellerProfileIdAndSkuIgnoreCase(anyLong(), anyString()))
            .thenReturn(false);

        when(variantRepo.save(any()))
            .thenAnswer(i -> {
                var arg = (ProductVariant) i.getArgument(0);
                org.springframework.test.util.ReflectionTestUtils.setField(arg, "id", 321L);
                return arg;
            });
        when(inventoryRepo.save(any())).thenAnswer(i -> i.getArgument(0));

        var service = new ProductService(productRepo, variantRepo, inventoryRepo, sellerRepo);
        var options = List.of(new VariantOptionRequest("Color", "Green", 1));
        var req = new AddVariantRequest("SK", new java.math.BigDecimal("5.00"), 2, options);

        var saved = service.addVariant(1L, 1L, req);
        assertEquals(1, saved.getOptions().size());
    }

    @Test
    @DisplayName("addVariant_createsInventoryForVariant")
    void addVariant_createsInventoryForVariant() {
        var productRepo = mock(ProductRepository.class);
        var variantRepo = mock(ProductVariantRepository.class);
        var inventoryRepo = mock(ProductVariantInventoryRepository.class);

        var sellerRepo = mock(SellerProfileRepository.class);

        var product = Product.createDraft(10L, "Name", "Desc", "name-2");
        when(productRepo.findById(anyLong()))
            .thenReturn(Optional.of(product));

        var seller = mock(SellerProfile.class);
        when(seller.getId()).thenReturn(10L);
        when(sellerRepo.findByUserIdAndStatus(anyLong(), any()))
            .thenReturn(Optional.of(seller));

        when(variantRepo.existsByProductSellerProfileIdAndSkuIgnoreCase(anyLong(), anyString()))
            .thenReturn(false);

        when(variantRepo.save(any()))
            .thenAnswer(i -> {
                var arg = (ProductVariant) i.getArgument(0);
                org.springframework.test.util.ReflectionTestUtils.setField(arg, "id", 777L);
                return arg;
            });

        final AtomicReference<ProductVariantInventory> captured = new AtomicReference<>();
        when(inventoryRepo.save(any()))
            .thenAnswer(i -> {
                var inv = (ProductVariantInventory) i.getArgument(0);
                captured.set(inv);
                return inv;
            });

        var service = new ProductService(productRepo, variantRepo, inventoryRepo, sellerRepo);
        var req = new AddVariantRequest("SK", new BigDecimal("5.00"), 42, List.of());
        var saved = service.addVariant(1L, 1L, req);

        assertNotNull(captured.get());
        assertEquals(777L, captured.get().getProductVariantId());
        assertEquals(42, captured.get().getOnHandQuantity());
    }

    @Test
    @DisplayName("publishProduct_failsWhenProductNotFound")
    void publishProduct_failsWhenProductNotFound() {
        var productRepo = mock(ProductRepository.class);
        var variantRepo = mock(ProductVariantRepository.class);
        var inventoryRepo = mock(ProductVariantInventoryRepository.class);

        var sellerRepo = mock(SellerProfileRepository.class);

        var service = new ProductService(productRepo, variantRepo, inventoryRepo, sellerRepo);
        when(productRepo.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> service.publishProduct(1L, 1L));
    }

    @Test
    @DisplayName("publishProduct_failsWhenSellerProfileInactiveOrMissing")
    void publishProduct_failsWhenSellerProfileInactiveOrMissing() {
        var productRepo = mock(ProductRepository.class);
        var variantRepo = mock(ProductVariantRepository.class);
        var inventoryRepo = mock(ProductVariantInventoryRepository.class);

        var sellerRepo = mock(SellerProfileRepository.class);

        var product = Product.createDraft(10L, "Name", "Desc", "name-2");
        when(productRepo.findById(anyLong())).thenReturn(Optional.of(product));
        when(sellerRepo.findByUserIdAndStatus(anyLong(), any()))
            .thenReturn(Optional.empty());

        var service = new ProductService(productRepo, variantRepo, inventoryRepo, sellerRepo);
        assertThrows(ForbiddenOperationException.class,
            () -> service.publishProduct(1L, 1L));
    }

    @Test
    @DisplayName("publishProduct_failsWhenUserDoesNotOwnProduct")
    void publishProduct_failsWhenUserDoesNotOwnProduct() {
        var productRepo = mock(ProductRepository.class);
        var variantRepo = mock(ProductVariantRepository.class);
        var inventoryRepo = mock(ProductVariantInventoryRepository.class);

        var sellerRepo = mock(SellerProfileRepository.class);

        var product = Product.createDraft(10L, "Name", "Desc", "name-2");
        when(productRepo.findById(anyLong())).thenReturn(Optional.of(product));

        var seller = mock(SellerProfile.class);
        when(seller.getId()).thenReturn(999L);
        when(sellerRepo.findByUserIdAndStatus(anyLong(), any()))
            .thenReturn(Optional.of(seller));

        var service = new ProductService(productRepo, variantRepo, inventoryRepo, sellerRepo);
        assertThrows(ForbiddenOperationException.class,
            () -> service.publishProduct(1L, 1L));
    }

    @Test
    @DisplayName("publishProduct_failsWhenProductHasNoVariants")
    void publishProduct_failsWhenProductHasNoVariants() {
        var productRepo = mock(ProductRepository.class);
        var variantRepo = mock(ProductVariantRepository.class);
        var inventoryRepo = mock(ProductVariantInventoryRepository.class);

        var sellerRepo = mock(SellerProfileRepository.class);

        var product = Product.createDraft(10L, "Name", "Desc", "name-2");
        when(productRepo.findById(anyLong())).thenReturn(Optional.of(product));

        var seller = mock(SellerProfile.class);
        when(seller.getId()).thenReturn(10L);
        when(sellerRepo.findByUserIdAndStatus(anyLong(), any()))
            .thenReturn(Optional.of(seller));

        when(variantRepo.findByProductId(anyLong())).thenReturn(List.of());

        var service = new ProductService(productRepo, variantRepo, inventoryRepo, sellerRepo);
        assertThrows(BusinessRuleException.class,
            () -> service.publishProduct(1L, 1L));
    }

    @Test
    @DisplayName("publishProduct_failsWhenNoVariantHasPositiveInventory")
    void publishProduct_failsWhenNoVariantHasPositiveInventory() {
        var productRepo = mock(ProductRepository.class);
        var variantRepo = mock(ProductVariantRepository.class);
        var inventoryRepo = mock(ProductVariantInventoryRepository.class);

        var sellerRepo = mock(SellerProfileRepository.class);

        var product = Product.createDraft(10L, "Name", "Desc", "name-2");
        when(productRepo.findById(anyLong())).thenReturn(Optional.of(product));

        var seller = mock(SellerProfile.class);
        when(seller.getId()).thenReturn(10L);
        when(sellerRepo.findByUserIdAndStatus(anyLong(), any()))
            .thenReturn(Optional.of(seller));

        var variant = new ProductVariant("S1", new java.math.BigDecimal("1"));
        org.springframework.test.util.ReflectionTestUtils.setField(variant, "id", 5L);
        product.addVariant(variant);
        when(variantRepo.findByProductId(anyLong())).thenReturn(List.of(variant));

        when(inventoryRepo.findByProductVariant_Id(5L))
            .thenReturn(Optional.of(new ProductVariantInventory(0)));

        var service = new ProductService(productRepo, variantRepo, inventoryRepo, sellerRepo);

        assertThrows(BusinessRuleException.class,
            () -> service.publishProduct(1L, 1L));
    }

    @Test
    @DisplayName("publishProduct_publishesWhenAtLeastOneVariantHasPositiveInventory")
    void publishProduct_publishesWhenAtLeastOneVariantHasPositiveInventory() {
        var productRepo = mock(ProductRepository.class);
        var variantRepo = mock(ProductVariantRepository.class);
        var inventoryRepo = mock(ProductVariantInventoryRepository.class);

        var sellerRepo = mock(SellerProfileRepository.class);

        var product = Product.createDraft(10L, "Name", "Desc", "name-2");
        when(productRepo.findById(anyLong())).thenReturn(Optional.of(product));

        var seller = mock(SellerProfile.class);
        when(seller.getId()).thenReturn(10L);
        when(sellerRepo.findByUserIdAndStatus(anyLong(), any()))
            .thenReturn(Optional.of(seller));

        var variant = new ProductVariant("S1", new java.math.BigDecimal("1"));
        org.springframework.test.util.ReflectionTestUtils.setField(variant, "id", 5L);
        product.addVariant(variant);
        when(variantRepo.findByProductId(anyLong())).thenReturn(List.of(variant));

        when(inventoryRepo.findByProductVariant_Id(5L))
            .thenReturn(Optional.of(new ProductVariantInventory(3)));

        when(productRepo.save(any())).thenAnswer(i -> i.getArgument(0));

        var service = new ProductService(productRepo, variantRepo, inventoryRepo, sellerRepo);
        service.publishProduct(1L, 1L);
        assertEquals("PUBLISHED", product.getStatus());
    }

    @Test
    @DisplayName("publishProduct_isSafeWhenCalledTwice")
    void publishProduct_isSafeWhenCalledTwice() {
        var productRepo = mock(ProductRepository.class);
        var variantRepo = mock(ProductVariantRepository.class);
        var inventoryRepo = mock(ProductVariantInventoryRepository.class);

        var sellerRepo = mock(SellerProfileRepository.class);

        var product = Product.createDraft(10L, "Name", "Desc", "name-2");
        when(productRepo.findById(anyLong())).thenReturn(Optional.of(product));

        var seller = mock(SellerProfile.class);
        when(seller.getId()).thenReturn(10L);
        when(sellerRepo.findByUserIdAndStatus(anyLong(), any()))
            .thenReturn(Optional.of(seller));

        var variant = new ProductVariant("S1", new java.math.BigDecimal("1"));
        org.springframework.test.util.ReflectionTestUtils.setField(variant, "id", 5L);
        product.addVariant(variant);
        when(variantRepo.findByProductId(anyLong())).thenReturn(List.of(variant));

        when(inventoryRepo.findByProductVariant_Id(5L))
            .thenReturn(Optional.of(new ProductVariantInventory(3)));

        when(productRepo.save(any())).thenAnswer(i -> i.getArgument(0));

        var service = new ProductService(productRepo, variantRepo, inventoryRepo, sellerRepo);
        service.publishProduct(1L, 1L);
        // call again - should be idempotent and not throw
        service.publishProduct(1L, 1L);
        assertEquals("PUBLISHED", product.getStatus());
    }
}
