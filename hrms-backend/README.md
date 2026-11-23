# HRMS API

Spring Boot API for the HRMS app. Java 17, Spring Boot 3.3, JWT auth, MySQL.

## Run

```bash
./mvnw spring-boot:run
```

http://localhost:8081

Swagger: http://localhost:8081/swagger-ui.html

MySQL: `localhost:3306`, database `hrms`, user `hrms`, password `hrms`.

## Seeded accounts

Created on first startup if they are missing.

- `admin@company.com` / `Admin@123`
- `admin@example.com` / `changeme123`

Departments: Engineering, Human Resources, Finance.
Shift: 09:00–18:00, Monday to Friday.

PostgreSQL or MySQL can be used by setting `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_DRIVER`, and `SPRING_JPA_DATABASE_PLATFORM`.
