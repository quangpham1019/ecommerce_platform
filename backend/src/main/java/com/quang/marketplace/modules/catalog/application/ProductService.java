package com.quang.marketplace.modules.catalog.application;

import com.quang.marketplace.modules.catalog.domain.ProductVariantInventory;
import com.quang.marketplace.modules.catalog.api.AddVariantRequest;
import com.quang.marketplace.modules.catalog.api.CreateProductRequest;
import com.quang.marketplace.modules.catalog.api.VariantOptionRequest;
import com.quang.marketplace.modules.catalog.domain.Product;
import com.quang.marketplace.modules.catalog.domain.ProductStatus;
import com.quang.marketplace.modules.catalog.domain.ProductVariant;
import com.quang.marketplace.modules.catalog.infrastructure.ProductRepository;
import com.quang.marketplace.modules.catalog.infrastructure.ProductVariantInventoryRepository;
import com.quang.marketplace.modules.catalog.infrastructure.ProductVariantRepository;
import com.quang.marketplace.modules.seller.domain.SellerProfileStatus;
import com.quang.marketplace.modules.seller.infrastructure.SellerProfileRepository;
import com.quang.marketplace.shared.error.BusinessRuleException;
import com.quang.marketplace.shared.error.ConflictException;
import com.quang.marketplace.shared.error.ForbiddenOperationException;
import com.quang.marketplace.shared.error.ResourceNotFoundException;
import com.quang.marketplace.shared.util.SlugifyUtil;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProductService {

    private final ProductRepository productRepo;
    private final ProductVariantRepository variantRepo;
    private final ProductVariantInventoryRepository inventoryRepo;
    private final SellerProfileRepository sellerRepo;

    public ProductService(ProductRepository productRepo,
                          ProductVariantRepository variantRepo,
                          ProductVariantInventoryRepository inventoryRepo,
                          SellerProfileRepository sellerRepo) {
        this.productRepo = productRepo;
        this.variantRepo = variantRepo;
        this.inventoryRepo = inventoryRepo;
        this.sellerRepo = sellerRepo;
    }

    @Transactional
    public Product createProduct(Long userId, CreateProductRequest request) {
        var seller = sellerRepo
            .findByUserIdAndStatus(userId, SellerProfileStatus.ACTIVE)
            .orElseThrow(() -> new ForbiddenOperationException("User has no active seller profile"));

        String baseSlug = SlugifyUtil.slugify(request.title());

        String uniqueSlug = generateUniqueSlugForSeller(
            seller.getId(),
            baseSlug
        );

        Product product = Product.createDraft(
            seller.getId(),
            request.title(),
            request.description(),
            uniqueSlug
        );

        return productRepo.save(product);
    }

    @Transactional
    public ProductVariant addVariant(Long userId, Long productId, AddVariantRequest request) {
        Product product = productRepo.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        // ownership
        var seller = sellerRepo.findByUserIdAndStatus(userId, SellerProfileStatus.ACTIVE).orElseThrow(() -> new ForbiddenOperationException("Not owner of product"));
        if (!seller.getId().equals(product.getSellerProfileId())) {
            throw new ForbiddenOperationException("Not owner of product");
        }

        List<VariantOptionRequest> variantOptions =
        request.options() == null ? List.of() : request.options();
        validateVariantOptions(variantOptions);

        String sku = request.sku();
        if (sku == null || sku.isBlank()) {
            sku = SkuGenerator.generate(
                seller.getCode(),
                product.getId(),
                variantOptions
            );
        } else {
            sku = sku.trim();
        }

        // SKU uniqueness within seller scope: check other variants of seller's products
        if (variantRepo.existsByProductIdAndSkuIgnoreCase(product.getId(), sku)) {
            throw new ConflictException("SKU already exists for this product");
        }

        ProductVariant variant = new ProductVariant(sku, request.price());
        product.addVariant(variant);
        variantOptions.forEach(option -> variant.addOption(VariantOptionRequest.toDomain(option)));

        ProductVariantInventory item = new ProductVariantInventory(request.quantity());

        variant.assignInventory(item);
        
        productRepo.save(product); // cascade persist variant and inventory

        return variant;
    }

    @Transactional
    public void publishProduct(Long userId, Long productId) {
        Product product = productRepo.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException( "Product not found"));

        var sellerOpt = sellerRepo.findByUserIdAndStatus(userId, SellerProfileStatus.ACTIVE);
        if (sellerOpt.isEmpty() || !sellerOpt.get().getId().equals(product.getSellerProfileId())) {
            throw new ForbiddenOperationException("Not owner of product");
        }

        // must have at least one variant with available inventory
        var variants = variantRepo.findByProductId(productId);
        boolean haveAtleastOneVariantWithAvailableInventory = false;
        for (var v : variants) {
            var invOpt = inventoryRepo.findByProductVariant_Id(v.getId());
            if (invOpt.isPresent() && invOpt.get().getOnHandQuantity() > 0) {
                haveAtleastOneVariantWithAvailableInventory = true; 
                break;
            }
        }

        if (!haveAtleastOneVariantWithAvailableInventory) {
            throw new BusinessRuleException("Cannot publish without at least one in-stock variant");
        }

        product.publish();
        productRepo.save(product);
    }

    @Transactional(readOnly = true)
    public java.util.List<Product> listPublishedProducts() {
        return productRepo.findByStatus(ProductStatus.PUBLISHED);
    }


    private void validateVariantOptions(List<VariantOptionRequest> options) {

        if (options.size() > 3) {
            throw new BusinessRuleException("A variant cannot have more than 3 options");
        }
        
        long uniqueCount = options.stream().map(option -> {
            if (option.name() == null) {
                throw new BusinessRuleException("Option name is required");
            }

            return option.name().trim().toLowerCase();
        }).distinct().count();
        
        if (options.size() != uniqueCount) {
            throw new BusinessRuleException("Duplicate option names are not allowed");
        }
    }

    private String generateUniqueSlugForSeller(Long sellerId, String baseSlug) {
        String slug = baseSlug;
        int suffix = 2;

        while (productRepo.existsBySellerProfileIdAndSlug(sellerId, slug)) {
            slug = baseSlug + "-" + suffix;
            suffix++;
        }

        return slug;
    }
}
