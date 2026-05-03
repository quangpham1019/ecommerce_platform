-- Seed users
INSERT INTO users (email, password_hash) VALUES
('seller1@example.com', '$2a$12$nEYFAeWNuxhCCvojoLeWR.V4OgEwkZANQrnYEWXmoYvP55eKCUKAu'),
('buyer1@example.com', '$2a$12$nEYFAeWNuxhCCvojoLeWR.V4OgEwkZANQrnYEWXmoYvP55eKCUKAu');

-- Note: replace password_hash placeholders in real environments. For tests the application encodes passwords on register.

-- Create seller profile for seller1
INSERT INTO seller_profiles (user_id, display_name, active) VALUES (1, 'Seller One', TRUE);

-- Create products and variants
INSERT INTO products (seller_profile_id, title, description, published) VALUES
(1, 'Red Widget', 'A red widget for testing', TRUE),
(1, 'Blue Widget', 'A blue widget draft', FALSE);

-- Variants
INSERT INTO product_variants (product_id, sku, price) VALUES
(1, 'RW-001', 19.99),
(2, 'BW-001', 12.50);

-- Inventory
INSERT INTO inventory_items (product_variant_id, quantity) VALUES
(1, 10),
(2, 0);
