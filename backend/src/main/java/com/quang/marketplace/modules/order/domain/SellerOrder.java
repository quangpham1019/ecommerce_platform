package com.quang.marketplace.modules.order.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
    name = "seller_orders",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_seller_order_number",
            columnNames = "seller_order_number"
        )
    }
)
public class SellerOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "seller_profile_id", nullable = false)
    private Long sellerProfileId;

    @Column(name = "seller_order_number", nullable = false, unique = true)
    private String sellerOrderNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private SellerOrderStatus status = SellerOrderStatus.PENDING;

    @Column(name = "subtotal_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal subtotalAmount = BigDecimal.ZERO;

    @Column(name = "shipping_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal shippingAmount = BigDecimal.ZERO;

    @Column(name = "tax_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal taxAmount = BigDecimal.ZERO;

    @Column(name = "total_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "marketplace_order_id", nullable = false)
    private MarketplaceOrder marketplaceOrder;

    @OneToMany(mappedBy = "sellerOrder", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items = new ArrayList<>();

    @OneToOne(mappedBy = "sellerOrder", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private OrderFulfillment fulfillment;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected SellerOrder() {}

    public SellerOrder(Long sellerProfileId, String sellerOrderNumber) {
        if (sellerProfileId == null) {
            throw new IllegalArgumentException("Seller profile is required");
        }

        if (sellerOrderNumber == null || sellerOrderNumber.isBlank()) {
            throw new IllegalArgumentException("Seller order number is required");
        }

        this.sellerProfileId = sellerProfileId;
        this.sellerOrderNumber = sellerOrderNumber;
    }

    public void addItem(OrderItem item) {
        if (item == null) {
            throw new IllegalArgumentException("Order item is required");
        }

        items.add(item);
        item.setSellerOrder(this);

        recalculateTotals();
    }

    public void setFulfillment(OrderFulfillment fulfillment) {
        if (fulfillment == null) {
            throw new IllegalArgumentException("Fulfillment is required");
        }

        this.fulfillment = fulfillment;
        fulfillment.setSellerOrder(this);
    }

    public void recalculateTotals() {
        this.subtotalAmount = items.stream()
                .map(OrderItem::getLineTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        this.totalAmount = subtotalAmount
                .add(shippingAmount)
                .add(taxAmount);
    }

    public void markConfirmed() {
        this.status = SellerOrderStatus.CONFIRMED;
    }

    public void markProcessing() {
        this.status = SellerOrderStatus.PROCESSING;
    }

    public void markCompleted() {
        this.status = SellerOrderStatus.COMPLETED;
    }

    public void markCancelled() {
        this.status = SellerOrderStatus.CANCELLED;
    }

    public void markPartialRefund() {
        this.status = SellerOrderStatus.PARTIALLY_REFUNDED;
    }

    public void markRefunded() {
        this.status = SellerOrderStatus.REFUNDED;
    }

    void setMarketplaceOrder(MarketplaceOrder marketplaceOrder) {
        this.marketplaceOrder = marketplaceOrder;
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

    public Long getSellerProfileId() { return sellerProfileId; }

    public String getSellerOrderNumber() { return sellerOrderNumber; }

    public SellerOrderStatus getStatus() { return status; }

    public BigDecimal getSubtotalAmount() { return subtotalAmount; }

    public BigDecimal getShippingAmount() { return shippingAmount; }

    public BigDecimal getTaxAmount() { return taxAmount; }

    public BigDecimal getTotalAmount() { return totalAmount; }

    public MarketplaceOrder getMarketplaceOrder() { return marketplaceOrder; }

    public Long getMarketplaceOrderId() {
        return marketplaceOrder == null ? null : marketplaceOrder.getId();
    }

    public List<OrderItem> getItems() {
        return List.copyOf(items);
    }

    public OrderFulfillment getFulfillment() { return fulfillment; }

    public Instant getCreatedAt() { return createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
}