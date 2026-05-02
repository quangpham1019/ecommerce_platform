# Business Rules

## Product Positioning

This project is **a small multi-seller marketplace inspired by Amazon**.

## Identity

- A user must register with a unique email address.
- A user can act as a buyer without creating a seller profile.
- A user cannot create more than one active seller profile.
- A user must be authenticated to manage a seller profile, products, cart, or checkout.

## Seller

- A seller profile must have a display name before it can be activated.
- A seller profile must belong to exactly one user account.
- Only the owner of a seller profile can create or manage that seller's products.

## Catalog

- A product must belong to exactly one seller profile.
- A product cannot be published unless it has at least one variant with available inventory.
- A published product must have a title, description, base media, and at least one variant.
- A product variant must have a unique SKU within the seller's catalog.
- An unpublished product cannot be added to a buyer cart.

## Inventory

- Inventory is tracked at the product variant level.
- Available inventory cannot drop below zero.
- Inventory must be reserved during checkout before payment is marked successful.
- If checkout fails before order creation completes, any reserved inventory must be released.

## Cart

- A cart belongs to exactly one buyer.
- A cart can contain items from multiple sellers.
- A cart item must reference a specific published product variant.
- A cart item quantity cannot exceed available inventory at validation time.
- Cart pricing is informational only until checkout confirms the final purchasable price.

## Order

- Checkout must snapshot product name, variant, price, and quantity so old orders do not change when products change later.
- A single checkout can produce multiple seller-specific orders.
- Each seller-specific order must contain only items sold by one seller.
- An order cannot be created with zero order items.
- An order status must reflect the payment and fulfillment lifecycle.
- Fulfillment status must be tracked independently per seller-specific order.

## Payment

- Payment is mocked in the first release but must still produce a payment record.
- Payment authorization must happen after inventory validation and before final order confirmation.
- A failed payment must not create confirmed orders.
- A successful payment must be linked to the checkout and resulting seller-specific orders.
