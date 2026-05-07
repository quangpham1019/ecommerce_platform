-- Migration: create canonical marketplace schema
-- Legacy copy operations removed.
-- Table, index, unique key, and foreign key names use consistent plural table naming.

-- 1) user_accounts
CREATE TABLE IF NOT EXISTS user_accounts (
  id BIGINT NOT NULL AUTO_INCREMENT,
  email VARCHAR(255) NOT NULL,
  password_hash VARCHAR(255) NOT NULL,
  first_name VARCHAR(100) NULL,
  last_name VARCHAR(100) NULL,
  status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uq_user_accounts_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 2) seller_profiles
CREATE TABLE IF NOT EXISTS seller_profiles (
  id BIGINT NOT NULL AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  shop_name VARCHAR(150) NOT NULL,
  bio TEXT NULL,
  status VARCHAR(45) NOT NULL DEFAULT 'DRAFT',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uq_seller_profiles_user_id (user_id),
  CONSTRAINT fk_seller_profiles_user_accounts
    FOREIGN KEY (user_id)
    REFERENCES user_accounts(id)
    ON DELETE CASCADE
    ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 3) categories
CREATE TABLE IF NOT EXISTS categories (
  id BIGINT NOT NULL AUTO_INCREMENT,
  name VARCHAR(150) NOT NULL,
  slug VARCHAR(180) NOT NULL,
  parent_category_id BIGINT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uq_categories_slug (slug),
  INDEX idx_categories_parent_category_id (parent_category_id),
  CONSTRAINT fk_categories_parent_categories
    FOREIGN KEY (parent_category_id)
    REFERENCES categories(id)
    ON DELETE SET NULL
    ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 4) products
CREATE TABLE IF NOT EXISTS products (
  id BIGINT NOT NULL AUTO_INCREMENT,
  seller_profile_id BIGINT NOT NULL,
  name VARCHAR(255) NOT NULL,
  slug VARCHAR(280) NOT NULL,
  description TEXT NULL,
  status VARCHAR(50) NOT NULL DEFAULT 'DRAFT',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uq_products_seller_profile_id_slug (seller_profile_id, slug),
  INDEX idx_products_seller_profile_id (seller_profile_id),
  CONSTRAINT fk_products_seller_profiles
    FOREIGN KEY (seller_profile_id)
    REFERENCES seller_profiles(id)
    ON DELETE RESTRICT
    ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 5) product_images
CREATE TABLE IF NOT EXISTS product_images (
  id BIGINT NOT NULL AUTO_INCREMENT,
  product_id BIGINT NOT NULL,
  image_url VARCHAR(500) NOT NULL,
  alt_text VARCHAR(255) NULL,
  sort_order INT NOT NULL DEFAULT 0,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  INDEX idx_product_images_product_id (product_id),
  CONSTRAINT fk_product_images_products
    FOREIGN KEY (product_id)
    REFERENCES products(id)
    ON DELETE CASCADE
    ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 6) product_variants
CREATE TABLE IF NOT EXISTS product_variants (
  id BIGINT NOT NULL AUTO_INCREMENT,
  product_id BIGINT NOT NULL,
  sku VARCHAR(100) NOT NULL,
  variant_name VARCHAR(255) NOT NULL DEFAULT '',
  price DECIMAL(10,2) NOT NULL DEFAULT 0.00,
  currency_code CHAR(3) NOT NULL DEFAULT 'USD',
  status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uq_product_variants_sku (sku),
  INDEX idx_product_variants_product_id (product_id),
  CONSTRAINT fk_product_variants_products
    FOREIGN KEY (product_id)
    REFERENCES products(id)
    ON DELETE CASCADE
    ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 7) variant_images
CREATE TABLE IF NOT EXISTS variant_images (
  id BIGINT NOT NULL AUTO_INCREMENT,
  product_variant_id BIGINT NOT NULL,
  image_url VARCHAR(500) NOT NULL,
  alt_text VARCHAR(255) NULL,
  sort_order INT NOT NULL DEFAULT 0,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  INDEX idx_variant_images_product_variant_id (product_variant_id),
  CONSTRAINT fk_variant_images_product_variants
    FOREIGN KEY (product_variant_id)
    REFERENCES product_variants(id)
    ON DELETE CASCADE
    ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 8) product_categories
CREATE TABLE IF NOT EXISTS product_categories (
  id BIGINT NOT NULL AUTO_INCREMENT,
  product_id BIGINT NOT NULL,
  category_id BIGINT NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uq_product_categories_product_id_category_id (product_id, category_id),
  INDEX idx_product_categories_category_id (category_id),
  CONSTRAINT fk_product_categories_products
    FOREIGN KEY (product_id)
    REFERENCES products(id)
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT fk_product_categories_categories
    FOREIGN KEY (category_id)
    REFERENCES categories(id)
    ON DELETE RESTRICT
    ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 9) product_variant_inventories
CREATE TABLE IF NOT EXISTS product_variant_inventories (
  id BIGINT NOT NULL AUTO_INCREMENT,
  product_variant_id BIGINT NOT NULL,
  on_hand_quantity INT NOT NULL DEFAULT 0,
  reserved_quantity INT NOT NULL DEFAULT 0,
  reorder_threshold INT NOT NULL DEFAULT 0,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uq_product_variant_inventories_product_variant_id (product_variant_id),
  CONSTRAINT fk_product_variant_inventories_product_variants
    FOREIGN KEY (product_variant_id)
    REFERENCES product_variants(id)
    ON DELETE CASCADE
    ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 10) carts
CREATE TABLE IF NOT EXISTS carts (
  id BIGINT NOT NULL AUTO_INCREMENT,
  user_id BIGINT NULL,
  guest_token VARCHAR(50) NULL,
  status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uq_carts_user_id_status (user_id, status),
  CONSTRAINT fk_carts_user_accounts
    FOREIGN KEY (user_id)
    REFERENCES user_accounts(id)
    ON DELETE CASCADE
    ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 11) cart_items
CREATE TABLE IF NOT EXISTS cart_items (
  id BIGINT NOT NULL AUTO_INCREMENT,
  cart_id BIGINT NOT NULL,
  product_variant_id BIGINT NOT NULL,
  quantity INT NOT NULL,
  added_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uq_cart_items_cart_id_product_variant_id (cart_id, product_variant_id),
  INDEX idx_cart_items_product_variant_id (product_variant_id),
  CONSTRAINT fk_cart_items_carts
    FOREIGN KEY (cart_id)
    REFERENCES carts(id)
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT fk_cart_items_product_variants
    FOREIGN KEY (product_variant_id)
    REFERENCES product_variants(id)
    ON DELETE RESTRICT
    ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 12) marketplace_orders
CREATE TABLE IF NOT EXISTS marketplace_orders (
  id BIGINT NOT NULL AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  cart_id BIGINT NULL,
  order_number VARCHAR(50) NOT NULL,
  status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
  currency_code CHAR(3) NOT NULL DEFAULT 'USD',
  subtotal_amount DECIMAL(10,2) NOT NULL DEFAULT 0.00,
  shipping_amount DECIMAL(10,2) NOT NULL DEFAULT 0.00,
  tax_amount DECIMAL(10,2) NOT NULL DEFAULT 0.00,
  total_amount DECIMAL(10,2) NOT NULL DEFAULT 0.00,
  shipping_recipient_name VARCHAR(200) NOT NULL,
  shipping_line1 VARCHAR(255) NOT NULL,
  shipping_line2 VARCHAR(255) NULL,
  shipping_city VARCHAR(120) NOT NULL,
  shipping_state VARCHAR(120) NULL,
  shipping_postal_code VARCHAR(30) NOT NULL,
  shipping_country_code CHAR(2) NOT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uq_marketplace_orders_order_number (order_number),
  INDEX idx_marketplace_orders_user_id (user_id),
  INDEX idx_marketplace_orders_cart_id (cart_id),
  CONSTRAINT fk_marketplace_orders_user_accounts
    FOREIGN KEY (user_id)
    REFERENCES user_accounts(id)
    ON DELETE RESTRICT
    ON UPDATE CASCADE,
  CONSTRAINT fk_marketplace_orders_carts
    FOREIGN KEY (cart_id)
    REFERENCES carts(id)
    ON DELETE SET NULL
    ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 13) seller_orders
CREATE TABLE IF NOT EXISTS seller_orders (
  id BIGINT NOT NULL AUTO_INCREMENT,
  marketplace_order_id BIGINT NOT NULL,
  seller_profile_id BIGINT NOT NULL,
  seller_order_number VARCHAR(50) NOT NULL,
  status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
  subtotal_amount DECIMAL(10,2) NOT NULL DEFAULT 0.00,
  shipping_amount DECIMAL(10,2) NOT NULL DEFAULT 0.00,
  tax_amount DECIMAL(10,2) NOT NULL DEFAULT 0.00,
  total_amount DECIMAL(10,2) NOT NULL DEFAULT 0.00,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uq_seller_orders_seller_order_number (seller_order_number),
  INDEX idx_seller_orders_marketplace_order_id (marketplace_order_id),
  INDEX idx_seller_orders_seller_profile_id (seller_profile_id),
  CONSTRAINT fk_seller_orders_marketplace_orders
    FOREIGN KEY (marketplace_order_id)
    REFERENCES marketplace_orders(id)
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT fk_seller_orders_seller_profiles
    FOREIGN KEY (seller_profile_id)
    REFERENCES seller_profiles(id)
    ON DELETE RESTRICT
    ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 14) order_items
CREATE TABLE IF NOT EXISTS order_items (
  id BIGINT NOT NULL AUTO_INCREMENT,
  seller_order_id BIGINT NOT NULL,
  product_variant_id BIGINT NULL,
  quantity INT NOT NULL,
  unit_price_amount DECIMAL(10,2) NOT NULL,
  currency_code CHAR(3) NOT NULL DEFAULT 'USD',
  product_name_snapshot VARCHAR(255) NOT NULL,
  variant_name_snapshot VARCHAR(255) NOT NULL,
  sku_snapshot VARCHAR(255) NOT NULL,
  image_url_snapshot VARCHAR(500) NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  INDEX idx_order_items_seller_order_id (seller_order_id),
  INDEX idx_order_items_product_variant_id (product_variant_id),
  CONSTRAINT fk_order_items_seller_orders
    FOREIGN KEY (seller_order_id)
    REFERENCES seller_orders(id)
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT fk_order_items_product_variants
    FOREIGN KEY (product_variant_id)
    REFERENCES product_variants(id)
    ON DELETE SET NULL
    ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 15) order_fulfillments
CREATE TABLE IF NOT EXISTS order_fulfillments (
  id BIGINT NOT NULL AUTO_INCREMENT,
  seller_order_id BIGINT NOT NULL,
  fulfillment_status VARCHAR(50) NOT NULL DEFAULT 'UNFULFILLED',
  carrier VARCHAR(100) NULL,
  tracking_number VARCHAR(150) NULL,
  shipped_at DATETIME NULL,
  delivered_at DATETIME NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uq_order_fulfillments_seller_order_id (seller_order_id),
  CONSTRAINT fk_order_fulfillments_seller_orders
    FOREIGN KEY (seller_order_id)
    REFERENCES seller_orders(id)
    ON DELETE CASCADE
    ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 16) payments
CREATE TABLE IF NOT EXISTS payments (
  id BIGINT NOT NULL AUTO_INCREMENT,

  marketplace_order_id BIGINT NOT NULL,

  amount DECIMAL(19,2) NOT NULL,

  currency_code CHAR(3) NOT NULL DEFAULT 'USD',

  provider VARCHAR(100) NOT NULL DEFAULT 'MOCK',

  status VARCHAR(50) NOT NULL DEFAULT 'PENDING',

  provider_reference VARCHAR(255) NULL,

  failure_reason VARCHAR(500) NULL,

  authorized_at DATETIME NULL,

  failed_at DATETIME NULL,

  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
    ON UPDATE CURRENT_TIMESTAMP,

  PRIMARY KEY (id),

  INDEX idx_payments_marketplace_order_id (marketplace_order_id),

  INDEX idx_payments_status (status),

  CONSTRAINT fk_payments_marketplace_orders
    FOREIGN KEY (marketplace_order_id)
    REFERENCES marketplace_orders(id)
    ON DELETE RESTRICT
    ON UPDATE CASCADE

) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
-- End of migration
