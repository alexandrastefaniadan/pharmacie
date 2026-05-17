# Pharmacie

Internal pharmacist helper app for a Moroccan officine.
Lets a pharmacist quickly search, filter and manage their medication catalog
(form, dosage, age group, therapeutic class, indications, …).

> Out of scope: payments, accounting, pricing — handled by Sobrus.

## Stack

| Layer    | Tech                                                       |
|----------|------------------------------------------------------------|
| Backend  | Spring Boot 3.5 · Java 21 · PostgreSQL 16 · Flyway · JPA   |
| Frontend | Angular 21 · PrimeNG                                       |
| Infra    | Docker Compose                                             |

## Project layout

```
.
├── backend/            Spring Boot REST API
├── frontend/           Angular SPA (to be created in Phase 5)
├── docker-compose.yml  Local dev infrastructure (Postgres for now)
└── README.md
```

## Quick start

```bash
# 1. Infrastructure
docker compose up -d

# 2. Backend
cd backend
./gradlew bootRun
# API:     http://localhost:8080
# Swagger: http://localhost:8080/swagger-ui.html
# Health:  http://localhost:8080/actuator/health

# 3. Frontend (added in Phase 5)
cd frontend
npm start
```

## Development conventions

- **Migrations**: Flyway only (`backend/src/main/resources/db/migration/V*__*.sql`).
  Never enable `ddl-auto=update` in any environment.
- **Layering**: `controller → service → repository`. Controllers stay thin.
- **DTOs**: never expose JPA entities through the API. Mapping via MapStruct.
- **Validation**: Jakarta `@Valid` on all request DTOs.
- **Errors**: global `@RestControllerAdvice` returns RFC 7807 Problem Details.
- **Audit**: `created_at`, `updated_at` set automatically via JPA auditing.
- **Soft delete**: business entities use `deleted_at` instead of hard delete.
- **API versioning**: all endpoints under `/api/v1/...`.
- **Commit style**: [Conventional Commits](https://www.conventionalcommits.org/)
  (`feat:`, `fix:`, `chore:`, `refactor:`, `docs:`, `test:`).

