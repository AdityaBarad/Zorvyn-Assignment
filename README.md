# Zorvyn — Finance Dashboard Backend

## Overview
Zorvyn is a role-based finance dashboard backend that helps individuals and small teams track income and expenses, review trends, and keep an auditable trail of financial activity. It provides secure authentication, fine-grained role permissions, and APIs for creating and analyzing records.

## Architecture
```
Client
  |
  v
Security Filter
  |
  v
Controllers
  |
  v
Services
  |
  v
Repositories
  |
  v
PostgreSQL

Cross-cutting: AOP Logging, Audit Trail, Rate Limiting
```

## Tech Stack
| Technology       | Version | Purpose                        |
|-----------------|---------|--------------------------------|
| Java            | 21      | Language                       |
| Spring Boot     | 4.0.5   | Framework                      |
| Spring Security | 6.x     | Authentication & Authorization |
| PostgreSQL      | 15      | Database                       |
| Flyway          | 10.x    | Database migration             |
| jjwt            | 0.12.6  | JWT token generation           |
| MapStruct       | 1.6.3   | DTO mapping                    |
| Bucket4j        | 8.10.1  | Rate limiting                  |
| SpringDoc       | 2.8.3   | API documentation (Swagger)    |
| Lombok          | latest  | Boilerplate reduction          |

## Prerequisites
- Java 21
- Maven 3.8+
- PostgreSQL 15+
- Docker & Docker Compose (optional)

## Getting Started

### 1. Clone the repository
```
git clone <repo-url>
cd zorvyn
```

### 2. Create the database
```
psql -U postgres -c "CREATE DATABASE finance_dashboard;"
```

### 3. Environment variables
| Variable     | Default                                         | Description        |
|-------------|-------------------------------------------------|--------------------|
| DB_URL      | jdbc:postgresql://localhost:5432/finance_dashboard | DB URL          |
| DB_USERNAME | postgres                                        | DB username        |
| DB_PASSWORD | postgres                                        | DB password        |
| JWT_SECRET  | (see properties)                                | JWT signing secret |

### 4. Run with Maven
```
mvn clean install
mvn spring-boot:run
```

### 5. Run with Docker Compose
```
docker-compose up --build
```

## API Documentation
- Swagger UI: http://localhost:8080/swagger-ui.html
- OpenAPI JSON: http://localhost:8080/v3/api-docs
- Health check: http://localhost:8080/actuator/health

## Authentication Flow
1. POST /api/v1/auth/login with email + password
2. Receive accessToken and refreshToken
3. Add to all requests: Authorization: Bearer <accessToken>
4. Refresh via POST /api/v1/auth/refresh with X-Refresh-Token header
5. Logout via POST /api/v1/auth/logout

## Role Permissions Matrix
| Endpoint                   | VIEWER | ANALYST | ADMIN |
|---------------------------|--------|---------|-------|
| POST /auth/login           |   ✓    |    ✓    |   ✓   |
| GET /records               |   ✗    |    ✓    |   ✓   |
| GET /records/{id}          |   ✗    |    ✓    |   ✓   |
| POST /records              |   ✗    |    ✗    |   ✓   |
| PUT /records/{id}          |   ✗    |    ✗    |   ✓   |
| DELETE /records/{id}       |   ✗    |    ✗    |   ✓   |
| GET /dashboard/summary     |   ✓    |    ✓    |   ✓   |
| GET /dashboard/recent      |   ✓    |    ✓    |   ✓   |
| GET /dashboard/categories  |   ✗    |    ✓    |   ✓   |
| GET /dashboard/trends/{y}  |   ✗    |    ✓    |   ✓   |
| GET /users/{id}            |   ✗    |    ✗    |   ✓   |
| GET /users                 |   ✗    |    ✗    |   ✓   |
| POST /users                |   ✗    |    ✗    |   ✓   |
| PUT /users/{id}            |   ✗    |    ✗    |   ✓   |
| DELETE /users/{id}         |   ✗    |    ✗    |   ✓   |

## Default Admin Credentials
| Field    | Value                |
|---------|----------------------|
| Email   | admin@finance.com    |
| Password| Admin@1234           |

## Sample curl Commands

### Login
```
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@finance.com","password":"Admin@1234"}'
```

### Create a financial record
```
curl -X POST http://localhost:8080/api/v1/records \
  -H "Authorization: Bearer <your_token>" \
  -H "Content-Type: application/json" \
  -d '{"amount":1500.00,"type":"INCOME","category":"Salary","recordDate":"2024-01-15","description":"Monthly salary"}'
```

### Get dashboard summary
```
curl http://localhost:8080/api/v1/dashboard/summary \
  -H "Authorization: Bearer <your_token>"
```

### Get records filtered by type and date range
```
curl "http://localhost:8080/api/v1/records?type=INCOME&startDate=2024-01-01&endDate=2024-12-31" \
  -H "Authorization: Bearer <your_token>"
```

## Assumptions and Design Decisions
- Refresh tokens are stored in-memory (ConcurrentHashMap). In production, these would be stored in Redis or the database.
- Soft delete is used for both users and financial records. Deleted records are filtered at the repository and query layer.
- VIEWER role can read records and summary but cannot create, modify, or access sensitive analytics.
- Audit logging runs asynchronously (@Async) so it never blocks request threads.
- Rate limiting is in-memory per JVM instance. For multi-instance deployments, a distributed store like Redis would be needed.
- Passwords are hashed with BCrypt strength 12.
- All monetary values use BigDecimal to avoid floating-point errors.
- JWT secret must be at least 512 bits in production.

## Future Improvements
- Redis-backed refresh token store and distributed rate limiting
- Email verification on user registration
- Password reset flow
- Export records to CSV/PDF
- Role hierarchy (ADMIN inherits ANALYST inherits VIEWER)
- Multi-tenancy support

## Final Verification Checklist
- [ ] mvn clean compile             — zero errors
- [ ] mvn test                      — all 4 test classes pass
- [ ] mvn spring-boot:run           — app starts on port 8080
- [ ] PostgreSQL running locally    — Flyway applies V1–V6 migrations
- [ ] http://localhost:8080/swagger-ui.html — Swagger UI loads
- [ ] http://localhost:8080/actuator/health — returns {"status":"UP"}
- [ ] POST /api/v1/auth/login with admin@finance.com / Admin@1234 — returns JWT
- [ ] docker-compose up --build     — both containers start and app is healthy

