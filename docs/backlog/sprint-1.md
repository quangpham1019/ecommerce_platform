# Sprint 1 Backlog

## Sprint Goal

Deliver the first vertical slice:

Register user -> create seller profile -> create product -> publish product

## Stories

### Story 1: Register User

- Endpoint: `POST /api/register`
- Outcome: create a buyer-capable user account with unique email
- Acceptance criteria:
  - duplicate email is rejected
  - valid registration persists a user
  - password is stored securely

### Story 2: Login User

- Endpoint: `POST /api/login`
- Outcome: authenticated user can access protected endpoints
- Acceptance criteria:
  - valid credentials succeed
  - invalid credentials fail

### Story 3: Create Seller Profile

- Endpoint: `POST /api/seller-profiles`
- Outcome: user can create one seller profile
- Acceptance criteria:
  - user cannot create multiple active seller profiles
  - seller profile requires display name

### Story 4: Create Product Draft

- Endpoint: `POST /api/products`
- Outcome: seller can create an unpublished product
- Acceptance criteria:
  - product belongs to the authenticated seller
  - unpublished product is not visible for purchase

### Story 5: Add Product Variant

- Endpoint: `POST /api/products/{id}/variants`
- Outcome: seller can add a variant and initial stock data
- Acceptance criteria:
  - variant SKU is unique within seller scope
  - variant can store price and quantity

### Story 6: Publish Product

- Endpoint: `POST /api/products/{id}/publish`
- Outcome: valid product becomes purchasable
- Acceptance criteria:
  - publish fails without at least one variant with available inventory
  - publish succeeds when business rules are satisfied

## Test Tasks

- Unit tests for publish rules
- Unit tests for seller ownership rules
- Integration tests for registration and product persistence
- API tests for register, login, seller profile, product create, and publish

## Definition of Done

- API contract implemented for the Sprint 1 slice
- Flyway migrations created for the slice
- Unit and integration tests pass
- Documentation updated where behavior changed
