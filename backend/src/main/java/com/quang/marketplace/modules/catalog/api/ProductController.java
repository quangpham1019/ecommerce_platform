package com.quang.marketplace.modules.catalog.api;

import com.quang.marketplace.modules.catalog.application.ProductService;
import com.quang.marketplace.modules.catalog.domain.Product;
import com.quang.marketplace.modules.catalog.domain.ProductVariant;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.net.URI;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService service;

    public ProductController(ProductService service) { this.service = service; }

    record CreateProductRequest(String title, String description) {}

    @PostMapping
    public ResponseEntity<?> create(@RequestHeader("X-User-Id") Long userId,
                                    @RequestBody CreateProductRequest req) {
        Product p = service.createProduct(userId, req.title(), req.description());
        return ResponseEntity.created(URI.create("/api/products/" + p.getId())).body(p.getId());
    }

    record AddVariantRequest(String sku, BigDecimal price, int quantity) {}

    @PostMapping("/{id}/variants")
    public ResponseEntity<?> addVariant(@RequestHeader("X-User-Id") Long userId,
                                        @PathVariable("id") Long productId,
                                        @RequestBody AddVariantRequest req) {
        ProductVariant v = service.addVariant(userId, productId, req.sku(), req.price(), req.quantity());
        return ResponseEntity.created(URI.create("/api/products/" + productId + "/variants/" + v.getId())).body(v.getId());
    }

    @PostMapping("/{id}/publish")
    public ResponseEntity<?> publish(@RequestHeader("X-User-Id") Long userId,
                                     @PathVariable("id") Long productId) {
        service.publishProduct(userId, productId);
        return ResponseEntity.ok().build();
    }
}
