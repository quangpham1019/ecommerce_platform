-- =========================================
-- Seed Data for Marketplace Canonical Schema
-- Updated to match plural canonical table names
-- =========================================

-- NOTE:
-- This seed uses explicit IDs for predictable local/dev data.
-- Run only on a fresh/reset development database, or clear existing seed data first.

-- -----------------------------------------
-- Users
-- password: password123
-- BCrypt hash example
-- -----------------------------------------

INSERT INTO user_accounts (
    id,
    email,
    password_hash,
    first_name,
    last_name,
    status
)
VALUES
(
    1,
    'buyer1@example.com',
    '$2a$10$DowJonesIndexExampleHash123456789012345678901234',
    'John',
    'Buyer',
    'ACTIVE'
),
(
    2,
    'seller1@example.com',
    '$2a$10$DowJonesIndexExampleHash123456789012345678901234',
    'Alice',
    'Seller',
    'ACTIVE'
),
(
    3,
    'seller2@example.com',
    '$2a$10$DowJonesIndexExampleHash123456789012345678901234',
    'Bob',
    'Merchant',
    'ACTIVE'
);

-- -----------------------------------------
-- Seller Profiles
-- -----------------------------------------

INSERT INTO seller_profiles (
    id,
    user_id,
    shop_name,
    bio,
    status
)
VALUES
(
    1,
    2,
    'Alice Tech Store',
    'Electronics and accessories',
    'ACTIVE'
),
(
    2,
    3,
    'Bob Home Goods',
    'Home and kitchen products',
    'ACTIVE'
);

-- -----------------------------------------
-- Categories
-- -----------------------------------------

INSERT INTO categories (
    id,
    name,
    slug
)
VALUES
(1, 'Electronics', 'electronics'),
(2, 'Home', 'home'),
(3, 'Accessories', 'accessories');

-- -----------------------------------------
-- Products
-- -----------------------------------------

INSERT INTO products (
    id,
    seller_profile_id,
    name,
    slug,
    description,
    status
)
VALUES
(
    1,
    1,
    'Mechanical Keyboard',
    'mechanical-keyboard',
    'RGB mechanical keyboard with blue switches',
    'PUBLISHED'
),
(
    2,
    1,
    'Wireless Mouse',
    'wireless-mouse',
    'Ergonomic wireless mouse',
    'PUBLISHED'
),
(
    3,
    2,
    'Coffee Mug',
    'coffee-mug',
    'Ceramic coffee mug',
    'PUBLISHED'
);

-- -----------------------------------------
-- Product Images
-- -----------------------------------------

INSERT INTO product_images (
    product_id,
    image_url,
    alt_text,
    sort_order
)
VALUES
(1, 'https://example.com/images/mechanical-keyboard-main.jpg', 'Mechanical keyboard product photo', 1),
(2, 'https://example.com/images/wireless-mouse-main.jpg', 'Wireless mouse product photo', 1),
(3, 'https://example.com/images/coffee-mug-main.jpg', 'Coffee mug product photo', 1);

-- -----------------------------------------
-- Product Categories
-- -----------------------------------------

INSERT INTO product_categories (
    product_id,
    category_id
)
VALUES
(1, 1),
(1, 3),
(2, 1),
(2, 3),
(3, 2);

-- -----------------------------------------
-- Product Variants
-- -----------------------------------------

INSERT INTO product_variants (
    id,
    product_id,
    sku,
    variant_name,
    price,
    currency_code,
    status
)
VALUES
(
    1,
    1,
    'KB-RED-001',
    'Red Switch',
    89.99,
    'USD',
    'ACTIVE'
),
(
    2,
    1,
    'KB-BLUE-001',
    'Blue Switch',
    89.99,
    'USD',
    'ACTIVE'
),
(
    3,
    2,
    'MOUSE-BLK-001',
    'Black',
    39.99,
    'USD',
    'ACTIVE'
),
(
    4,
    3,
    'MUG-WHITE-001',
    'White',
    14.99,
    'USD',
    'ACTIVE'
);

-- -----------------------------------------
-- Variant Inventory
-- -----------------------------------------

INSERT INTO product_variant_inventories (
    id,
    product_variant_id,
    on_hand_quantity,
    reserved_quantity,
    reorder_threshold
)
VALUES
(1, 1, 50, 0, 10),
(2, 2, 40, 0, 10),
(3, 3, 100, 0, 20),
(4, 4, 75, 0, 15);

-- -----------------------------------------
-- Variant Images
-- -----------------------------------------

INSERT INTO variant_images (
    product_variant_id,
    image_url,
    alt_text,
    sort_order
)
VALUES
(
    1,
    'https://example.com/images/kb-red.jpg',
    'Mechanical keyboard red switch',
    1
),
(
    2,
    'https://example.com/images/kb-blue.jpg',
    'Mechanical keyboard blue switch',
    1
),
(
    3,
    'https://example.com/images/mouse-black.jpg',
    'Wireless mouse black',
    1
),
(
    4,
    'https://example.com/images/mug-white.jpg',
    'White coffee mug',
    1
);

-- -----------------------------------------
-- Cart
-- -----------------------------------------

INSERT INTO carts (
    id,
    user_id,
    status
)
VALUES
(
    1,
    1,
    'ACTIVE'
);

-- -----------------------------------------
-- Cart Items
-- -----------------------------------------

INSERT INTO cart_items (
    cart_id,
    product_variant_id,
    quantity
)
VALUES
(1, 1, 1),
(1, 4, 2);

-- -----------------------------------------
-- Marketplace Order
-- -----------------------------------------

INSERT INTO marketplace_orders (
    id,
    user_id,
    cart_id,
    order_number,
    status,
    currency_code,
    subtotal_amount,
    shipping_amount,
    tax_amount,
    total_amount,
    shipping_recipient_name,
    shipping_line1,
    shipping_city,
    shipping_postal_code,
    shipping_country_code
)
VALUES
(
    1,
    1,
    1,
    'ORD-20260507-0001',
    'COMPLETED',
    'USD',
    119.97,
    10.00,
    8.00,
    137.97,
    'John Buyer',
    '123 Main St',
    'New Orleans',
    '70114',
    'US'
);

-- -----------------------------------------
-- Seller Orders
-- -----------------------------------------

INSERT INTO seller_orders (
    id,
    marketplace_order_id,
    seller_profile_id,
    seller_order_number,
    status,
    subtotal_amount,
    shipping_amount,
    tax_amount,
    total_amount
)
VALUES
(
    1,
    1,
    1,
    'SELL-ALICE-0001',
    'COMPLETED',
    89.99,
    5.00,
    6.00,
    100.99
),
(
    2,
    1,
    2,
    'SELL-BOB-0001',
    'COMPLETED',
    29.98,
    5.00,
    2.00,
    36.98
);

-- -----------------------------------------
-- Order Items
-- -----------------------------------------

INSERT INTO order_items (
    seller_order_id,
    product_variant_id,
    quantity,
    unit_price_amount,
    currency_code,
    product_name_snapshot,
    variant_name_snapshot,
    sku_snapshot,
    image_url_snapshot
)
VALUES
(
    1,
    1,
    1,
    89.99,
    'USD',
    'Mechanical Keyboard',
    'Red Switch',
    'KB-RED-001',
    'https://example.com/images/kb-red.jpg'
),
(
    2,
    4,
    2,
    14.99,
    'USD',
    'Coffee Mug',
    'White',
    'MUG-WHITE-001',
    'https://example.com/images/mug-white.jpg'
);

-- -----------------------------------------
-- Order Fulfillment
-- -----------------------------------------

INSERT INTO order_fulfillments (
    seller_order_id,
    fulfillment_status,
    carrier,
    tracking_number
)
VALUES
(
    1,
    'DELIVERED',
    'UPS',
    '1Z999AA10123456784'
),
(
    2,
    'DELIVERED',
    'FedEx',
    '999999999999'
);
