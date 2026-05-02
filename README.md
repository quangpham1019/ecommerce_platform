# Ecommerce Marketplace

This project is **a small multi-seller marketplace inspired by Amazon**.

It is being built as a modular monolith using:

- Spring Boot
- Java 21
- MySQL 8
- Flyway
- GitHub

The current state of the repository reflects **Sprint 0 foundations**:

- domain and planning documentation
- modular backend folder structure
- Spring-oriented project skeleton
- MySQL local development scaffold
- Flyway migration folder
- Sprint 1 backlog draft

## Architecture Direction

The project intentionally uses a modular monolith.

> I chose a modular monolith to avoid premature microservices while still enforcing domain boundaries.

Core modules:

- Identity
- Seller
- Catalog
- Inventory
- Cart
- Order
- Payment

## Repository Layout

```text
backend/
  pom.xml
  src/
    main/
      java/com/quang/marketplace/
      resources/
    test/
      java/com/quang/marketplace/
docs/
  adr/
  backlog/
compose.yml
```

## Local Setup Plan

Planned local development flow:

1. Install Java 21
2. Install Maven
3. Start MySQL with `compose.yml`
4. Configure environment variables
5. Run the Spring Boot application

## Documentation

- [Development Plan](docs/marketplace-development-plan.md)
- [Business Rules](docs/business-rules.md)
- [ERD](docs/erd.md)
- [API Draft](docs/api.md)
- [Test Strategy](docs/test-strategy.md)
- [Sprint 1 Backlog](docs/backlog/sprint-1.md)
- [ADR 0001: Modular Monolith](docs/adr/0001-modular-monolith.md)
- [ADR 0002: Technology Stack](docs/adr/0002-technology-stack.md)

## Current Constraint

The local machine currently does not have Java or Maven installed, so this Sprint 0 work focuses on structure and documentation rather than running the application.
