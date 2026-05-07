package com.quang.marketplace.modules.order.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
    name = "marketplace_orders",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_marketplace_order_order_number", columnNames = "order_number")
    }
)
public class MarketplaceOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "cart_id", nullable = false)
    private Long cartId;

    @Column(name = "order_number", nullable = false, unique = true)
    private String orderNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private MarketplaceOrderStatus status = MarketplaceOrderStatus.PENDING;

    @Column(name = "currency_code", nullable = false, length = 3, columnDefinition = "CHAR(3)")
    private String currencyCode = "USD";

    @Column(name = "subtotal_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal subtotalAmount = BigDecimal.ZERO;

    @Column(name = "shipping_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal shippingAmount = BigDecimal.ZERO;

    @Column(name = "tax_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal taxAmount = BigDecimal.ZERO;

    @Column(name = "total_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @Embedded
    private ShippingAddress shippingAddress;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @OneToMany(mappedBy = "marketplaceOrder", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SellerOrder> sellerOrders = new ArrayList<>();

    protected MarketplaceOrder() {}

    public MarketplaceOrder(
            Long userId,
            Long cartId,
            String orderNumber,
            ShippingAddress shippingAddress
    ) {
        if (userId == null) throw new IllegalArgumentException("User is required");
        if (cartId == null) throw new IllegalArgumentException("Cart is required");
        if (orderNumber == null || orderNumber.isBlank()) {
            throw new IllegalArgumentException("Order number is required");
        }
        if (shippingAddress == null) {
            throw new IllegalArgumentException("Shipping address is required");
        }

        this.userId = userId;
        this.cartId = cartId;
        this.orderNumber = orderNumber;
        this.shippingAddress = shippingAddress;
    }

    public void addSellerOrder(SellerOrder sellerOrder) {
        if (sellerOrder == null) {
            throw new IllegalArgumentException("Seller order is required");
        }

        sellerOrders.add(sellerOrder);
        sellerOrder.setMarketplaceOrder(this);

        recalculateTotals();
    }

    public void recalculateTotals() {
        this.subtotalAmount = sellerOrders.stream()
                .map(SellerOrder::getSubtotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        this.shippingAmount = sellerOrders.stream()
                .map(SellerOrder::getShippingAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        this.taxAmount = sellerOrders.stream()
                .map(SellerOrder::getTaxAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        this.totalAmount = subtotalAmount
                .add(shippingAmount)
                .add(taxAmount);
    }

    public void markPaid() {
        this.status = MarketplaceOrderStatus.PAID;
    }
    public void markPartiallyFulfilled() {
        this.status = MarketplaceOrderStatus.PARTIALLY_FULFILLED;
    }
    public void markFulfilled() {
        this.status = MarketplaceOrderStatus.FULFILLED;
    }
    public void markFailed() {
        this.status = MarketplaceOrderStatus.FAILED;
    }

    public void markCancelled() {
        this.status = MarketplaceOrderStatus.CANCELLED;
    }

    @PrePersist
    void onCreate() {
        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        this.updatedAt = Instant.now();
    }

    public Long getId() { return id; }

    public Long getUserId() { return userId; }

    public Long getCartId() { return cartId; }

    public String getOrderNumber() { return orderNumber; }

    public MarketplaceOrderStatus getStatus() { return status; }

    public String getCurrencyCode() { return currencyCode; }

    public BigDecimal getSubtotalAmount() { return subtotalAmount; }

    public BigDecimal getShippingAmount() { return shippingAmount; }

    public BigDecimal getTaxAmount() { return taxAmount; }

    public BigDecimal getTotalAmount() { return totalAmount; }

    public ShippingAddress getShippingAddress() { return shippingAddress; }

    public Instant getCreatedAt() { return createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }

    public List<SellerOrder> getSellerOrders() {
        return List.copyOf(sellerOrders);
    }
}