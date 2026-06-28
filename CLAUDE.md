# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Reactive order service built with Spring Boot 3.4.5, Kotlin, and WebFlux. Handles order creation with Redis-based stock checking and R2DBC for reactive PostgreSQL persistence.

## Build Commands

```bash
./gradlew build           # Build (skip tests)
./gradlew test            # Run tests
./gradlew bootRun         # Run locally on port 8080
./gradlew bootJar         # Create executable JAR
```

## Running Infrastructure

Docker Compose is managed automatically by Spring Boot via `spring-boot-docker-compose`. Run the app with:

```bash
./gradlew bootRun         # Starts PostgreSQL 17 + Redis 7 via Docker Compose, then app
./scripts/init-stock.sh   # Initialize stock:1 = 100000 (after app is up)
```

To stop containers: `docker compose down` (or Ctrl+C stops both app and containers).

## Architecture

```
controller/OrderController.kt  →  service/OrderService.kt  →  repository/OrderRepository.kt
                                        ↓
                              Redis (stock checks) + PostgreSQL (persistence)
```

**Order Flow:**
1. `POST /orders` → `OrderController.create()`
2. `OrderService.create()` checks Redis `stock:{productId}` for availability
3. If stock sufficient, saves to PostgreSQL via `OrderRepository` (CoroutineCrudRepository)
4. Returns `OrderResponse` or throws `SoldOutException` (HTTP 409)

**Reactive Stack:**
- `spring-boot-starter-webflux` for reactive web
- `spring-boot-starter-data-r2dbc` for reactive database access
- `spring-boot-starter-data-redis-reactive` for reactive Redis operations
- `kotlinx-coroutines-reactor` enables suspend functions via `.awaitSingle()`

## Key Patterns

- **Repository**: `CoroutineCrudRepository<Entity, Id>` - async CRUD with coroutines
- **Service**: Constructor injection, uses `suspend` functions with `.awaitSingle()` to bridge reactive and coroutine worlds
- **Exception Handling**: `@RestControllerAdvice` maps `SoldOutException` → HTTP 409 CONFLICT
- **Redis**: `ReactiveStringRedisTemplate` - all operations return `Mono`/`Flux`

## Configuration

Environment variables for Docker Compose:
- `POSTGRES_HOST`, `POSTGRES_PORT`, `POSTGRES_DB`, `POSTGRES_USER`, `POSTGRES_PASSWORD`
- `REDIS_HOST`, `REDIS_PORT`

Local defaults in `application.yml` connect to `localhost:5432` and `localhost:6379`.

Docker Compose auto-configuration (via `spring-boot-docker-compose`) overrides connection properties when containers are running.

## Benchmarking

```bash
./scripts/run-benchmark.sh [base-url]   # Requires k6 installed
```

k6 script in `benchmark/k6.js` runs load test against `POST /orders` with p95 < 100ms threshold.