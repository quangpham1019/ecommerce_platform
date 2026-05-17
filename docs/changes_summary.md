# Summary of Recent Changes

This document summarizes the notable schema and business-rule changes observed in the latest uncommitted edits.

Key decision and rule changes

- Slug generation and uniqueness
  - Product slugs are generated and de-duplicated per seller profile. When a requested slug exists, the system appends a numeric suffix (e.g. `-2`) to create a unique slug for the same seller.
  - Database enforces unique slug per seller and the product domain persists `sellerProfileId` on save.

- Product status type
  - Product `status` has been converted to a `ProductStatus` enum (e.g. `DRAFT`, `PUBLISHED`) rather than raw strings.
  - Publishing a product sets the enum `PUBLISHED` and the `isPublished()` convenience method is used in tests.

- Seller profile ownership and activation checks
  - Service-layer operations now query seller profile by `userId` and require an `ACTIVE` `SellerProfileStatus` for mutations such as creating products or adding variants.

- SKU uniqueness and generation
  - SKU generation uses the seller code and product context; when an SKU is provided it is trimmed and stored normalized.
  - SKU uniqueness checks are scoped at the product level in repository methods (existsByProductIdAndSkuIgnoreCase). Duplicate SKUs within the same product are rejected; tests indicate the same SKU may be allowed across different products for the same seller.

- Variant and inventory behavior
  - Variants can be created and assigned an inventory object. Inventory stores `onHand`, `reserved`, and `available` quantities; `available` is derived (`onHand - reserved`).
  - Publishing requires at least one variant with positive available inventory.
  - Many service tests now prefer adding variants to the in-memory `Product` aggregate and saving the parent `Product` instead of persisting `ProductVariant` separately.

- Repository and migration changes
  - A DB migration file (`V1__create_canonical_schema.sql`) was added/updated to reflect the canonical schema changes (slug, variant options, inventory tables).
  - Integration tests were added to validate persistence of sellerProfileId, variant inventory, and SKU constraints.

Notes and recommended follow-ups

- Verify DB migration: review `db/migration/V1__create_canonical_schema.sql` and run migrations against a staging DB to confirm constraints and indexes.
- Confirm whether SKU uniqueness should be scoped to product or seller catalog; tests currently enforce per-product uniqueness and database constraints were updated accordingly.
- Ensure repository method names and JPA queries align with the intended scoping (existsByProductIdAndSkuIgnoreCase vs existsByProductSellerProfileIdAndSkuIgnoreCase).

Files touched (high level)

- backend/src/main/java/... (ProductService, SkuGenerator, domain classes)
- backend/src/test/java/... (many new/updated unit & integration tests)
- db/migration/V1__create_canonical_schema.sql

If you'd like, I can:

- run the test suite for the catalog module, or
- run the DB migration in a local test DB, or
- open a PR instead of pushing directly to `main`.

-- automated summary generated
