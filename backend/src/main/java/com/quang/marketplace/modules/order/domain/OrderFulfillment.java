package com.quang.marketplace.modules.order.domain;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(
    name = "order_fulfillments",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_order_fulfillment_seller_order",
            columnNames = "seller_order_id"
        )
    }
)
public class OrderFulfillment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "seller_order_id", nullable = false, unique = true)
    private SellerOrder sellerOrder;

    @Enumerated(EnumType.STRING)
    @Column(name = "fulfillment_status", nullable = false)
    private OrderFulfillmentStatus fulfillmentStatus = OrderFulfillmentStatus.PENDING;

    @Column(name = "carrier")
    private String carrier;

    @Column(name = "tracking_number")
    private String trackingNumber;

    @Column(name = "shipped_at")
    private Instant shippedAt;

    @Column(name = "delivered_at")
    private Instant deliveredAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected OrderFulfillment() {}

    public static OrderFulfillment create() {
        return new OrderFulfillment();
    }

    // ===== Business Transitions =====

    public void confirm() {
        assertStatus(OrderFulfillmentStatus.PENDING);
        this.fulfillmentStatus = OrderFulfillmentStatus.CONFIRMED;
    }

    public void startPacking() {
        assertStatus(OrderFulfillmentStatus.CONFIRMED);
        this.fulfillmentStatus = OrderFulfillmentStatus.PACKING;
    }

    public void markShipped(String carrier, String trackingNumber) {
        assertStatus(OrderFulfillmentStatus.PACKING);

        if (carrier == null || carrier.isBlank()) {
            throw new IllegalArgumentException("Carrier is required");
        }
        if (trackingNumber == null || trackingNumber.isBlank()) {
            throw new IllegalArgumentException("Tracking number is required");
        }

        this.fulfillmentStatus = OrderFulfillmentStatus.SHIPPED;
        this.carrier = carrier;
        this.trackingNumber = trackingNumber;
        this.shippedAt = Instant.now();
    }

    public void markDelivered() {
        assertStatus(OrderFulfillmentStatus.SHIPPED);

        this.fulfillmentStatus = OrderFulfillmentStatus.DELIVERED;
        this.deliveredAt = Instant.now();
    }

    public void cancel() {
        if (fulfillmentStatus == OrderFulfillmentStatus.DELIVERED ||
            fulfillmentStatus == OrderFulfillmentStatus.RETURNED) {
            throw new IllegalStateException("Cannot cancel after delivery or return");
        }

        this.fulfillmentStatus = OrderFulfillmentStatus.CANCELLED;
    }

    public void requestReturn() {
        assertStatus(OrderFulfillmentStatus.DELIVERED);
        this.fulfillmentStatus = OrderFulfillmentStatus.RETURN_REQUESTED;
    }

    public void markReturned() {
        assertStatus(OrderFulfillmentStatus.RETURN_REQUESTED);
        this.fulfillmentStatus = OrderFulfillmentStatus.RETURNED;
    }

    private void assertStatus(OrderFulfillmentStatus expected) {
        if (this.fulfillmentStatus != expected) {
            throw new IllegalStateException(
                "Invalid state transition. Expected: " + expected + " but was: " + fulfillmentStatus
            );
        }
    }

    // ===== Relationship =====

    void setSellerOrder(SellerOrder sellerOrder) {
        if (sellerOrder == null) {
            throw new IllegalArgumentException("Seller order is required");
        }
        this.sellerOrder = sellerOrder;
    }

    // ===== Lifecycle =====

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

    // ===== Getters =====

    public Long getId() { return id; }

    public SellerOrder getSellerOrder() { return sellerOrder; }

    public OrderFulfillmentStatus getFulfillmentStatus() { return fulfillmentStatus; }

    public String getCarrier() { return carrier; }

    public String getTrackingNumber() { return trackingNumber; }

    public Instant getShippedAt() { return shippedAt; }

    public Instant getDeliveredAt() { return deliveredAt; }

    public Instant getCreatedAt() { return createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
}