# API Draft

## Identity

- `POST /api/register`
- `POST /api/login`
- `GET /api/me`

## Seller

- `POST /api/seller-profiles`
- `GET /api/seller-profiles/me`
- `PATCH /api/seller-profiles/me`

## Catalog

- `POST /api/products`
- `GET /api/products/{id}`
- `PATCH /api/products/{id}`
- `POST /api/products/{id}/publish`
- `POST /api/products/{id}/variants`
- `PATCH /api/variants/{id}`

## Inventory

- `POST /api/variants/{id}/inventory-adjustments`
- `GET /api/variants/{id}/inventory`

## Cart

- `GET /api/cart`
- `POST /api/cart/items`
- `PATCH /api/cart/items/{itemId}`
- `DELETE /api/cart/items/{itemId}`

## Checkout and Order

- `POST /api/checkout`
- `GET /api/orders`
- `GET /api/orders/{id}`
- `PATCH /api/orders/{id}/fulfillment-status`

## Payment

- `POST /api/payments/mock-confirm`

## Checkout Flow

```mermaid
sequenceDiagram
    participant B as Buyer
    participant API as Spring API
    participant C as Cart Module
    participant I as Inventory Module
    participant P as Payment Module
    participant O as Order Module

    B->>API: POST /api/checkout
    API->>C: Load cart
    API->>I: Validate and reserve stock
    I-->>API: Reservation success
    API->>P: Mock authorize payment
    P-->>API: Payment success
    API->>O: Create checkout and seller orders
    O-->>API: Orders created
    API-->>B: Checkout completed
```
