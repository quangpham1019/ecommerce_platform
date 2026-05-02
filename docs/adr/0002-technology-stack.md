# ADR 0002: Technology Stack

## Status

Accepted

## Context

The project needs a backend stack that is widely recognized, production-relevant, and suitable for demonstrating:

- domain modeling
- API design
- transaction handling
- database migrations
- integration testing
- CI on GitHub

The preferred stack is Spring, Java, MySQL, GitHub, and Flyway. Cloud hosting can be decided later between AWS and Azure.

## Decision

The project will use:

- Spring Boot for the backend application framework
- Java 21 as the language baseline
- Maven as the build tool
- MySQL 8 as the primary relational database
- Flyway for schema migrations
- GitHub for source control and CI workflow hosting

The cloud deployment target remains intentionally undecided during Sprint 0.

## Consequences

Positive consequences:

- Strong alignment with common enterprise backend stacks
- Good fit for transaction-heavy marketplace behavior
- Clear path for Flyway-managed schema evolution
- Strong portfolio signal for backend and testing-focused roles

Tradeoffs:

- Local setup depends on Java and Maven being installed
- Spring Boot adds more framework structure than lighter Java options
- MySQL-specific behavior should be validated in tests rather than assumed

## Alternatives Considered

### Node.js Backend

Rejected because the chosen portfolio direction is stronger with Spring and Java for backend architecture and testing depth.

### PostgreSQL

Not selected because MySQL is the chosen target for this project.

### Immediate Cloud Commitment

Rejected because AWS versus Azure does not change Sprint 0 design decisions enough to justify locking it early.
