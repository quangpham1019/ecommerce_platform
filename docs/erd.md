# ERD

## Logical ERD

```mermaid
erDiagram
    USER ||--o| SELLER_PROFILE : owns
    SELLER_PROFILE ||--o{ PRODUCT : lists
    PRODUCT ||--o{ PRODUCT_VARIANT : has
    PRODUCT_VARIANT ||--|| INVENTORY_ITEM : tracks
    PRODUCT_VARIANT ||--o{ PRODUCT_VARIANT_OPTION : has
    USER ||--|| CART : owns
    CART ||--o{ CART_ITEM : contains
    PRODUCT_VARIANT ||--o{ CART_ITEM : references
    USER ||--o{ CHECKOUT : performs
    CHECKOUT ||--o{ ORDER : creates
    ORDER ||--o{ ORDER_ITEM : contains
    SELLER_PROFILE ||--o{ ORDER : fulfills
    PRODUCT_VARIANT ||--o{ ORDER_ITEM : snapshot_source
    CHECKOUT ||--|| PAYMENT : uses
```

## Physical Table Direction

First expected tables:

- `users`
- `seller_profiles`
- `products`
- `product_variants`
- `inventory_items`
 - `product_variant_options` (new)
- `carts`
- `cart_items`
- `checkouts`
- `orders`
- `order_items`
- `payments`

## Flyway Plan

Planned initial migration order:

- `V1__create_identity_tables.sql`
- `V2__create_catalog_tables.sql`
- `V3__create_cart_tables.sql`
- `V4__create_order_tables.sql`
- `V5__create_payment_tables.sql`
