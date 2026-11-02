# CDR Billing Service

Telco usage and rating engine: ingests Call Detail Records, rates them against the customer's
subscription plan and accumulates the cost within the current billing cycle.

Java 21, Spring Boot 3.5, PostgreSQL, Flyway, Testcontainers.

## Assignment

Build a microservice for a mobile operator that ingests Call Detail Records (calls in seconds, SMS
by count, data in MB) and rates them in real time against the customer's subscription plan:
STANDARD ($0.05/min, $0.01/SMS) or PREMIUM (100 free minutes and 20 free SMS per cycle, then
$0.03/min and $0.01/SMS), with data at $2.00/GB billed proportionally for all plans. Costs and
free quotas accumulate per calendar-month billing cycle (UTC) based on the event timestamp, not the
processing time. Processing must be idempotent by `eventId`, safe under concurrent CDRs for the same
customer, and precise for money. Reporting covers unbilled charges, remaining free quotas and the
top 10 customers by cost in the current cycle. The design should follow DDD with clear Rating and
Billing boundaries and allow new pricing rules with minimal changes.

## Highlights

- **Money precision**: `BigDecimal` end to end, costs accumulated at `NUMERIC(19, 8)` and rounded to
  currency precision only at the API boundary.
- **Exactly-once charging**: idempotency enforced by a database `UNIQUE` constraint on `event_id`,
  not by application-level checks.
- **No lost updates**: per-customer, per-cycle row lock (`SELECT ... FOR UPDATE`), verified by
  concurrency tests against real PostgreSQL.
- **Event-time billing**: usage is attributed to the billing cycle of the event timestamp, so late
  CDRs land in the correct month.
- **Extensible pricing**: Strategy pattern isolates rating rules from billing and persistence.
- **Contract-first API**: OpenAPI spec with generated interfaces, Clean Architecture layering.

## Running

The image packages an already built jar, so the application is built first:

```bash
./mvnw clean package -DskipTests
docker compose up --build -d
```

Starts PostgreSQL and the application on `http://localhost:8080`. Flyway creates the schema and
seeds the STANDARD and PREMIUM plans on startup.

Local development against the containerized database only:

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=demo
```

Tests (Docker must be running, integration tests use Testcontainers):

```bash
./mvnw test
```

## API requests

`rest-client/` contains an IntelliJ HTTP Client collection. Select the `local` environment and run
the files in order:

| File                            | Contents                                                    |
|---------------------------------|-------------------------------------------------------------|
| `1-plans.http`                  | Subscription plans                                          |
| `2-customers.http`              | Customer registration and lookup, error cases               |
| `3-cdr.http`                    | Rating, idempotency, backdated events, validation errors    |
| `4-reports.http`                | Unbilled charges, free quotas, top 10 customers             |
| `5-scenario-premium-quota.http` | Self-contained walk-through of the PREMIUM free quota rules |

The OpenAPI contract lives in [
`src/main/resources/openapi/cdr-billing-api.yaml`](src/main/resources/openapi/cdr-billing-api.yaml)
and the API interfaces are generated from it at build time.

## Architecture

Clean Architecture, with the dependency rule pointing inwards:

```
domain/          billing, cdr, customer, rating   - business rules, no framework
  port/                                           - interfaces owned by the domain
application/     services                         - use cases, transaction boundaries
infrastructure/  persistence, web                 - adapters implementing the ports
```

## Rating logic and extensibility

Rating uses the Strategy pattern. `RatingStrategy` declares the usage type it supports and rates a
`RatingContext`; `RatingEngine` dispatches to the matching strategy.

```java
public interface RatingStrategy {
    UsageType supportedType();

    RatedCharge rate(RatingContext context);
}
```

Current strategies: `CallRatingStrategy`,
`SmsRatingStrategy` and `DataRatingStrategy`.

A strategy returns a `RatedCharge` carrying both the cost and the free units it consumed, so the
aggregate updates the cost and the quota from a single result.

Adding a pricing rule means adding a strategy or extending an existing one; the engine, the
application service and the persistence layer stay untouched. `RatingContext` is a record rather
than a parameter list, so new inputs required by future rules (event time for night tariffs,
accumulated volume for volume-based discounts, promotion codes) are added without changing the
strategy signature.

## Concurrency

**Creating the billing cycle.** Two requests may find no billing cycle for the current month and
both try to insert one. This is resolved in the database with
`INSERT ... ON CONFLICT (customer_id, cycle) DO NOTHING` behind a `UNIQUE` constraint. The loser of
the race silently reads the winner's row.

**Accumulating the cost.** Without locking, two concurrent reads of the same total would both add
their charge to the stale value and one update would be lost. `CdrService` therefore loads the
aggregate with `SELECT ... FOR UPDATE` (`@Lock(PESSIMISTIC_WRITE)`) inside the transaction, which
serialises the read-modify-write per customer and cycle.

The lock is taken on a single row, reached through the unique index on `(customer_id, cycle)`, so
the unit of contention is one customer's billing cycle. Concurrent CDRs for different customers
never block each other.

Pessimistic locking was chosen over optimistic even though contention on a single row is rare in
practice: the critical section holds no external calls, only in-memory arithmetic between the
`SELECT ... FOR UPDATE` and the commit, so the lock is cheap, while optimistic locking would need
retry logic on a code path that moves money. It also degrades predictably: a genuinely busy
subscriber (an M2M SIM, a corporate gateway) queues up behind the lock instead of collapsing into a
retry storm. The default Read Committed isolation is sufficient because the explicit row lock, not
the isolation level, provides the guarantee.

Idempotency is enforced by the database as well: `event_id` carries a `UNIQUE` constraint and the
insert uses `ON CONFLICT DO NOTHING`. A check-then-insert in application code would itself be a
race. A duplicate returns `200 DUPLICATE` without side effects, a new event returns
`201 PROCESSED`.

`CdrServiceConcurrencyTest` runs against a real PostgreSQL container and covers both: parallel
distinct CDRs for one customer accumulate without lost updates, and the same event racing itself
across threads is charged exactly once.

## Roadmap

Not implemented, but the design leaves room for it.

**Billing and money**

- Close billing cycles and issue invoices; make a closed cycle immutable and route late CDRs to an
  adjustment record instead.
- Append-only ledger of rated charges (one entry per CDR) as the source of truth, with the cycle
  total as a derived balance. Enables audit, reconciliation and replay.
- Explicit `Money` value object with currency (ISO 4217) and a single rounding policy.
- Plan changes mid-cycle with proration, effective-dated plan versions.
- Time-based, promotional and volume-tiered pricing as additional strategies.

**Scale (target: 10k CDR/s)**

- Asynchronous ingestion via Kafka, partitioned by `customerId` so events of one customer are
  processed in order by a single consumer and row-lock contention disappears.
- Transactional outbox for publishing `UsageRated` events to downstream systems.
- Batch rating and batched inserts per partition.
- Monthly range partitioning of `cdr_events`, retention and archiving of old partitions.
- Separate read model (e.g. materialized view or Redis sorted set) for the Top 10 report.

**Operations**

- GitHub Actions pipeline: build, tests with Testcontainers, image publishing, dependency scanning.
- Observability: Micrometer metrics (rated CDRs, duplicates, lock wait time), OpenTelemetry
  tracing, structured JSON logs.
- Security: OAuth2 resource server, per-client rate limiting, audit log of API calls.
- Load tests with Gatling or k6, Helm chart and HPA for Kubernetes deployment.