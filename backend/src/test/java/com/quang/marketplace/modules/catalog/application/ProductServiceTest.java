package com.quang.marketplace.modules.catalog.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.quang.marketplace.modules.catalog.api.AddVariantRequest;
import com.quang.marketplace.modules.catalog.api.CreateProductRequest;
import com.quang.marketplace.modules.catalog.api.VariantOptionRequest;
import com.quang.marketplace.modules.catalog.domain.Product;
import com.quang.marketplace.modules.catalog.domain.ProductStatus;
import com.quang.marketplace.modules.catalog.domain.ProductVariant;
import com.quang.marketplace.modules.catalog.domain.ProductVariantInventory;
import com.quang.marketplace.modules.catalog.infrastructure.ProductRepository;
import com.quang.marketplace.modules.catalog.infrastructure.ProductVariantInventoryRepository;
import com.quang.marketplace.modules.catalog.infrastructure.ProductVariantRepository;
import com.quang.marketplace.modules.seller.domain.SellerProfile;
import com.quang.marketplace.modules.seller.domain.SellerProfileStatus;
import com.quang.marketplace.modules.seller.infrastructure.SellerProfileRepository;
import com.quang.marketplace.shared.error.BusinessRuleException;
import com.quang.marketplace.shared.error.ConflictException;
import com.quang.marketplace.shared.error.ForbiddenOperationException;
import com.quang.marketplace.shared.error.ResourceNotFoundException;

public class ProductServiceTest {

    // createProduct tests

    @Test
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
    void createProduct_generatesUniqueSlugWhenSlugAlreadyExists() {
        var productRepo = mock(ProductRepository.class);
        var variantRepo = mock(ProductVariantRepository.class);
        var inventoryRepo = mock(ProductVariantInventoryRepository.class);
        var sellerRepo = mock(SellerProfileRepository.class);

        var seller = mock(SellerProfile.class);
        when(seller.getId()).thenReturn(99L);
        when(seller.getCode()).thenReturn("SHOP");

        when(sellerRepo.findByUserIdAndStatus(anyLong(), any()))
            .thenReturn(Optional.of(seller));

        when(productRepo.existsBySellerProfileIdAndSlug(99L, "my-product"))
            .thenReturn(true);

        when(productRepo.existsBySellerProfileIdAndSlug(99L, "my-product-2"))
            .thenReturn(false);

        when(productRepo.save(any(Product.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        var service = new ProductService(
            productRepo,
            variantRepo,
            inventoryRepo,
            sellerRepo
        );

        Product saved = service.createProduct(
            1L,
            new CreateProductRequest("My Product", "Description")
        );

        assertEquals("my-product-2", saved.getSlug());

        verify(productRepo).existsBySellerProfileIdAndSlug(99L, "my-product");
        verify(productRepo).existsBySellerProfileIdAndSlug(99L, "my-product-2");
        verify(productRepo).save(any(Product.class));
    }

    @Test
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
        assertEquals(ProductStatus.DRAFT, saved.getStatus());
        assertEquals(99L, saved.getSellerProfileId());
    }

    // addVariant tests

    @Test
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
    void addVariant_generatesSkuWhenSkuMissing() {
        ProductRepository productRepo = mock(ProductRepository.class);
        ProductVariantRepository variantRepo = mock(ProductVariantRepository.class);
        ProductVariantInventoryRepository inventoryRepo = mock(ProductVariantInventoryRepository.class);
        SellerProfileRepository sellerRepo = mock(SellerProfileRepository.class);

        Product product = Product.createDraft(10L, "Name", "Desc", "name-2");

        when(productRepo.findById(1L))
            .thenReturn(Optional.of(product));

        SellerProfile seller = mock(SellerProfile.class);
        when(seller.getId()).thenReturn(10L);
        when(seller.getCode()).thenReturn("SHP");

        when(sellerRepo.findByUserIdAndStatus(1L, SellerProfileStatus.ACTIVE))
            .thenReturn(Optional.of(seller));

        when(variantRepo.existsByProductIdAndSkuIgnoreCase(eq(1L), anyString()))
            .thenReturn(false);

        when(productRepo.save(any(Product.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        ProductService service = new ProductService(
            productRepo,
            variantRepo,
            inventoryRepo,
            sellerRepo
        );

        List<VariantOptionRequest> options = List.of(
            new VariantOptionRequest("Color", "Blue", 1)
        );

        AddVariantRequest request = new AddVariantRequest(
            null,
            new BigDecimal("5.00"),
            3,
            options
        );

        ProductVariant saved = service.addVariant(1L, 1L, request);

        assertTrue(saved.getSku().startsWith("SHP-"));
        assertEquals(1, product.getVariants().size());
        assertEquals(saved, product.getVariants().get(0));

        verify(variantRepo).existsByProductIdAndSkuIgnoreCase(eq(1L), anyString());
        verify(productRepo).save(product);
        verify(variantRepo, never()).save(any());
        verify(inventoryRepo, never()).save(any());
    }

    @Test
    void addVariant_usesTrimmedProvidedSku() {
        ProductRepository productRepo = mock(ProductRepository.class);
        ProductVariantRepository variantRepo = mock(ProductVariantRepository.class);
        ProductVariantInventoryRepository inventoryRepo = mock(ProductVariantInventoryRepository.class);
        SellerProfileRepository sellerRepo = mock(SellerProfileRepository.class);

        Product product = Product.createDraft(10L, "Name", "Desc", "name-2");

        when(productRepo.findById(1L))
            .thenReturn(Optional.of(product));

        SellerProfile seller = mock(SellerProfile.class);
        when(seller.getId()).thenReturn(10L);

        when(sellerRepo.findByUserIdAndStatus(1L, SellerProfileStatus.ACTIVE))
            .thenReturn(Optional.of(seller));

        when(variantRepo.existsByProductIdAndSkuIgnoreCase(1L, "ABC-123"))
            .thenReturn(false);

        when(productRepo.save(any(Product.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        ProductService service = new ProductService(
            productRepo,
            variantRepo,
            inventoryRepo,
            sellerRepo
        );

        AddVariantRequest request = new AddVariantRequest(
            "   ABC-123   ",
            new BigDecimal("5.00"),
            2,
            List.of()
        );

        ProductVariant saved = service.addVariant(1L, 1L, request);

        assertEquals("ABC-123", saved.getSku());

        verify(variantRepo).existsByProductIdAndSkuIgnoreCase(1L, "ABC-123");
        verify(productRepo).save(product);
        verify(variantRepo, never()).save(any());
        verify(inventoryRepo, never()).save(any());
    }

    @Test
    void addVariant_rejectsDuplicateSkuWithinProduct() {
        ProductRepository productRepo = mock(ProductRepository.class);
        ProductVariantRepository variantRepo = mock(ProductVariantRepository.class);
        ProductVariantInventoryRepository inventoryRepo = mock(ProductVariantInventoryRepository.class);
        SellerProfileRepository sellerRepo = mock(SellerProfileRepository.class);

        Product product = Product.createDraft(10L, "Name", "Desc", "name-2");

        when(productRepo.findById(1L))
            .thenReturn(Optional.of(product));

        SellerProfile seller = mock(SellerProfile.class);
        when(seller.getId()).thenReturn(10L);

        when(sellerRepo.findByUserIdAndStatus(1L, SellerProfileStatus.ACTIVE))
            .thenReturn(Optional.of(seller));

        when(variantRepo.existsByProductIdAndSkuIgnoreCase(1L, "SKU123"))
            .thenReturn(true);

        ProductService service = new ProductService(
            productRepo,
            variantRepo,
            inventoryRepo,
            sellerRepo
        );

        AddVariantRequest request = new AddVariantRequest(
            "SKU123",
            new BigDecimal("5.00"),
            1,
            List.of()
        );

        assertThrows(
            ConflictException.class,
            () -> service.addVariant(1L, 1L, request)
        );

        verify(variantRepo).existsByProductIdAndSkuIgnoreCase(1L, "SKU123");
        verify(productRepo, never()).save(any());
    }

    @Test
    void addVariant_mapsOptionRequestsOntoVariant() {
        ProductRepository productRepo = mock(ProductRepository.class);
        ProductVariantRepository variantRepo = mock(ProductVariantRepository.class);
        ProductVariantInventoryRepository inventoryRepo = mock(ProductVariantInventoryRepository.class);
        SellerProfileRepository sellerRepo = mock(SellerProfileRepository.class);

        Product product = Product.createDraft(10L, "Name", "Desc", "name-2");

        when(productRepo.findById(1L))
            .thenReturn(Optional.of(product));

        SellerProfile seller = mock(SellerProfile.class);
        when(seller.getId()).thenReturn(10L);

        when(sellerRepo.findByUserIdAndStatus(1L, SellerProfileStatus.ACTIVE))
            .thenReturn(Optional.of(seller));

        when(variantRepo.existsByProductIdAndSkuIgnoreCase(1L, "SK"))
            .thenReturn(false);

        when(productRepo.save(any(Product.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        ProductService service = new ProductService(
            productRepo,
            variantRepo,
            inventoryRepo,
            sellerRepo
        );

        List<VariantOptionRequest> options = List.of(
            new VariantOptionRequest("Color", "Green", 1)
        );

        AddVariantRequest request = new AddVariantRequest(
            "SK",
            new BigDecimal("5.00"),
            2,
            options
        );

        ProductVariant saved = service.addVariant(1L, 1L, request);

        assertEquals(1, saved.getOptions().size());
        assertEquals("Color", saved.getOptions().get(0).getOptionName());
        assertEquals("Green", saved.getOptions().get(0).getOptionValue());

        verify(productRepo).save(product);
        verify(variantRepo, never()).save(any());
        verify(inventoryRepo, never()).save(any());
    }

    @Test
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
        when(seller.getCode()).thenReturn("SHOP");

        when(sellerRepo.findByUserIdAndStatus(anyLong(), any()))
            .thenReturn(Optional.of(seller));

        when(variantRepo.existsByProductIdAndSkuIgnoreCase(anyLong(), anyString()))
            .thenReturn(false);

        when(productRepo.save(any(Product.class)))
            .thenAnswer(i -> i.getArgument(0));

        var service = new ProductService(
            productRepo,
            variantRepo,
            inventoryRepo,
            sellerRepo
        );

        var req = new AddVariantRequest(
            "SK",
            new BigDecimal("5.00"),
            42,
            List.of()
        );

        ProductVariant saved = service.addVariant(1L, 1L, req);

        assertNotNull(saved.getInventory());
        assertEquals(42, saved.getInventory().getOnHandQuantity());
        assertEquals(0, saved.getInventory().getReservedQuantity());
        assertEquals(42, saved.getInventory().getAvailableQuantity());

        assertEquals(1, product.getVariants().size());
        assertEquals(saved, product.getVariants().get(0));

        verify(productRepo).save(product);
        verify(inventoryRepo, never()).save(any());
        verify(variantRepo, never()).save(any());
    }

    // publishProduct tests

    @Test
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
        ReflectionTestUtils.setField(variant, "id", 5L);
        product.addVariant(variant);
        when(variantRepo.findByProductId(anyLong())).thenReturn(List.of(variant));

        when(inventoryRepo.findByProductVariant_Id(5L))
            .thenReturn(Optional.of(new ProductVariantInventory(0)));

        var service = new ProductService(productRepo, variantRepo, inventoryRepo, sellerRepo);

        assertThrows(BusinessRuleException.class,
            () -> service.publishProduct(1L, 1L));
    }

    @Test
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
        ReflectionTestUtils.setField(variant, "id", 5L);
        product.addVariant(variant);
        when(variantRepo.findByProductId(anyLong())).thenReturn(List.of(variant));

        when(inventoryRepo.findByProductVariant_Id(5L))
            .thenReturn(Optional.of(new ProductVariantInventory(3)));

        when(productRepo.save(any())).thenAnswer(i -> i.getArgument(0));

        var service = new ProductService(productRepo, variantRepo, inventoryRepo, sellerRepo);
        service.publishProduct(1L, 1L);
        assertEquals(ProductStatus.PUBLISHED, product.getStatus());
    }

    @Test
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
        ReflectionTestUtils.setField(variant, "id", 5L);
        product.addVariant(variant);
        when(variantRepo.findByProductId(anyLong())).thenReturn(List.of(variant));

        when(inventoryRepo.findByProductVariant_Id(5L))
            .thenReturn(Optional.of(new ProductVariantInventory(3)));

        when(productRepo.save(any())).thenAnswer(i -> i.getArgument(0));

        var service = new ProductService(productRepo, variantRepo, inventoryRepo, sellerRepo);
        service.publishProduct(1L, 1L);
        // call again - should be idempotent and not throw
        service.publishProduct(1L, 1L);
        assertEquals(ProductStatus.PUBLISHED, product.getStatus());
    }
}
