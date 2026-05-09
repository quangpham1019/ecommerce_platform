package com.quang.marketplace.modules.catalog.application;

import com.quang.marketplace.modules.catalog.domain.ProductVariantInventory;
import com.quang.marketplace.modules.catalog.domain.Product;
import com.quang.marketplace.modules.catalog.domain.ProductVariant;
import com.quang.marketplace.modules.catalog.infrastructure.InventoryRepository;
import com.quang.marketplace.modules.catalog.infrastructure.ProductRepository;
import com.quang.marketplace.modules.catalog.infrastructure.ProductVariantRepository;
import com.quang.marketplace.modules.seller.infrastructure.SellerProfileRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;

@Service
public class ProductService {

    private final ProductRepository productRepo;
    private final ProductVariantRepository variantRepo;
    private final InventoryRepository inventoryRepo;
    private final SellerProfileRepository sellerRepo;

    public ProductService(ProductRepository productRepo,
                          ProductVariantRepository variantRepo,
                          InventoryRepository inventoryRepo,
                          SellerProfileRepository sellerRepo) {
        this.productRepo = productRepo;
        this.variantRepo = variantRepo;
        this.inventoryRepo = inventoryRepo;
        this.sellerRepo = sellerRepo;
    }

    @Transactional
    public Product createProduct(Long userId, String title, String description) {
        var sellerOpt = sellerRepo.findByUserIdAndStatus(userId, com.quang.marketplace.modules.seller.domain.SellerProfileStatus.ACTIVE);
        if (sellerOpt.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User has no active seller profile");
        }

        Product p = new Product(sellerOpt.get().getId(), title, description);
        return productRepo.save(p);
    }

    @Transactional
    public ProductVariant addVariant(Long userId, Long productId, String sku, BigDecimal price, int quantity) {
        Product product = productRepo.findById(productId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));

        // ownership
        var sellerOpt = sellerRepo.findByUserIdAndStatus(userId, com.quang.marketplace.modules.seller.domain.SellerProfileStatus.ACTIVE);
        if (sellerOpt.isEmpty() || !sellerOpt.get().getId().equals(product.getSellerProfileId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not owner of product");
        }

        // SKU uniqueness within seller scope: check other variants of seller's products
        List<Product> sellerProducts = productRepo.findBySellerProfileId(product.getSellerProfileId());
        for (Product sp : sellerProducts) {
            var variants = variantRepo.findByProductId(sp.getId());
            for (var v : variants) {
                if (v.getSku().equalsIgnoreCase(sku)) {
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "SKU already exists for this seller");
                }
            }
        }

        ProductVariant variant = new ProductVariant(product, sku, price);
        ProductVariant saved = variantRepo.save(variant);

        ProductVariantInventory item = new ProductVariantInventory(saved.getId(), quantity);
        inventoryRepo.save(item);

        return saved;
    }

    @Transactional
    public void publishProduct(Long userId, Long productId) {
        Product product = productRepo.findById(productId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));

        var sellerOpt = sellerRepo.findByUserIdAndStatus(userId, com.quang.marketplace.modules.seller.domain.SellerProfileStatus.ACTIVE);
        if (sellerOpt.isEmpty() || !sellerOpt.get().getId().equals(product.getSellerProfileId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not owner of product");
        }

        // must have at least one variant with available inventory
        var variants = variantRepo.findByProductId(productId);
        boolean ok = false;
        for (var v : variants) {
            var invOpt = inventoryRepo.findByProductVariantId(v.getId());
            if (invOpt.isPresent() && invOpt.get().getOnHandQuantity() > 0) {
                ok = true; break;
            }
        }

        if (!ok) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot publish without at least one in-stock variant");
        }

        product.publish();
        productRepo.save(product);
    }

    @Transactional(readOnly = true)
    public java.util.List<Product> listPublishedProducts() {
        return productRepo.findByStatus("PUBLISHED");
    }
}
