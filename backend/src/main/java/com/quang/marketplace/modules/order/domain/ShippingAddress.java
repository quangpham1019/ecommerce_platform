package com.quang.marketplace.modules.order.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class ShippingAddress {

    @Column(name = "shipping_recipient_name", nullable = false)
    private String recipientName;

    @Column(name = "shipping_line1", nullable = false)
    private String line1;

    @Column(name = "shipping_line2")
    private String line2;

    @Column(name = "shipping_city", nullable = false)
    private String city;

    @Column(name = "shipping_state")
    private String state;

    @Column(name = "shipping_postal_code", nullable = false)
    private String postalCode;

    @Column(name = "shipping_country_code", nullable = false, columnDefinition = "CHAR(2)")
    private String countryCode;

    protected ShippingAddress() {}

    public ShippingAddress(
            String recipientName,
            String line1,
            String city,
            String postalCode,
            String countryCode,
            String line2,
            String state
    ) {
        if (recipientName == null || recipientName.isBlank())
            throw new IllegalArgumentException("Recipient name is required");

        if (line1 == null || line1.isBlank())
            throw new IllegalArgumentException("Address line1 is required");

        if (city == null || city.isBlank())
            throw new IllegalArgumentException("City is required");

        if (postalCode == null || postalCode.isBlank())
            throw new IllegalArgumentException("Postal code is required");

        if (countryCode == null || countryCode.isBlank())
            throw new IllegalArgumentException("Country code is required");

        this.recipientName = recipientName;
        this.line1 = line1;
        this.city = city;
        this.postalCode = postalCode;
        this.countryCode = countryCode;
        this.line2 = line2;
        this.state = state;
    }

    public String getRecipientName() { return recipientName; }
    public String getLine1() { return line1; }
    public String getLine2() { return line2; }
    public String getCity() { return city; }
    public String getState() { return state; }
    public String getPostalCode() { return postalCode; }
    public String getCountryCode() { return countryCode; }
    
}
