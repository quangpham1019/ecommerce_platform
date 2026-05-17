package com.quang.marketplace.modules.catalog.api;

import com.quang.marketplace.modules.catalog.application.ProductService;
import com.quang.marketplace.modules.catalog.domain.Product;
import com.quang.marketplace.modules.catalog.domain.ProductVariant;
import com.quang.marketplace.shared.security.CurrentUserProvider;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody CreateProductRequest req) {
        Long userId = currentUserProvider.getCurrentUserId();
        Product p = service.createProduct(userId, req);
        return ResponseEntity.created(URI.create("/api/products/" + p.getId())).body(p.getId());
    }

    @PostMapping("/{id}/variants")
    public ResponseEntity<?> addVariant(@PathVariable("id") Long productId,
                                        @Valid @RequestBody AddVariantRequest req) {
        Long userId = currentUserProvider.getCurrentUserId();
        ProductVariant v = service.addVariant(userId, productId, req);
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
        var views = products.stream().map(p -> new ProductView(p.getId(), p.getName(), p.getDescription(), p.isPublished())).toList();
        return ResponseEntity.ok(views);
    }
}
