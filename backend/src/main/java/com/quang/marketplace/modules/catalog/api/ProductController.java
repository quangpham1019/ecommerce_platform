package com.quang.marketplace.modules.catalog.api;

import com.quang.marketplace.modules.catalog.application.ProductService;
import com.quang.marketplace.modules.catalog.domain.Product;
import com.quang.marketplace.modules.catalog.domain.ProductVariant;
import com.quang.marketplace.shared.security.CurrentUserProvider;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.net.URI;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService service;
    private final CurrentUserProvider currentUserProvider;

    public ProductController(ProductService service, CurrentUserProvider currentUserProvider) {
        this.service = service;
        this.currentUserProvider = currentUserProvider;
    }

    record CreateProductRequest(String title, String description) {}

    @PostMapping
    public ResponseEntity<?> create(@RequestBody CreateProductRequest req) {
        Long userId = currentUserProvider.getCurrentUserId();
        Product p = service.createProduct(userId, req.title(), req.description());
        return ResponseEntity.created(URI.create("/api/products/" + p.getId())).body(p.getId());
    }

    record AddVariantRequest(String sku, BigDecimal price, int quantity) {}

    @PostMapping("/{id}/variants")
    public ResponseEntity<?> addVariant(@PathVariable("id") Long productId,
                                        @RequestBody AddVariantRequest req) {
        Long userId = currentUserProvider.getCurrentUserId();
        ProductVariant v = service.addVariant(userId, productId, req.sku(), req.price(), req.quantity());
        return ResponseEntity.created(URI.create("/api/products/" + productId + "/variants/" + v.getId())).body(v.getId());
    }

    @PostMapping("/{id}/publish")
    public ResponseEntity<?> publish(@PathVariable("id") Long productId) {
        Long userId = currentUserProvider.getCurrentUserId();
        service.publishProduct(userId, productId);
        return ResponseEntity.ok().build();
    }

    record ProductView(Long id, String title, String description, boolean published) {}

    @GetMapping
    public ResponseEntity<?> listPublished() {
        var products = service.listPublishedProducts();
        var views = products.stream().map(p -> new ProductView(p.getId(), p.getTitle(), p.getDescription(), p.isPublished())).toList();
        return ResponseEntity.ok(views);
    }
}
