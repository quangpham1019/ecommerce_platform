# ADR 0001: Modular Monolith

## Status

Accepted

## Context

The project is a small multi-seller marketplace inspired by Amazon. It needs to demonstrate:

- Strong backend design
- Clear business rules
- End-to-end feature delivery
- Testing depth
- Thoughtful boundaries without premature operational complexity

Microservices would add deployment, communication, observability, and consistency complexity too early for the current project goals.

## Decision

The system will be built as a modular monolith.

Each core domain module will own its own business rules:

- Identity
- Seller
- Catalog
- Inventory
- Cart
- Order
- Payment

The implementation should enforce module boundaries in code, tests, and data access conventions even though deployment remains a single application.

Key statement:

> I chose a modular monolith to avoid premature microservices while still enforcing domain boundaries.

## Consequences

Positive consequences:

- Lower operational complexity
- Faster development for a solo or small-team portfolio project
- Easier end-to-end debugging and testing
- Better architecture signal than a flat CRUD monolith
- Cleaner path to future extraction if scaling needs change

Tradeoffs:

- Boundaries rely on discipline instead of network isolation
- A single deployment unit can still become coupled if module rules are ignored
- Database ownership boundaries must be kept intentional

## Alternatives Considered

### Flat CRUD Monolith

Rejected because it is simpler to build but weaker at demonstrating domain thinking and boundary design.

### Microservices

Rejected because it introduces premature complexity for deployment, messaging, test setup, and local development.
