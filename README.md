# Smart Incident & Issue Management Platform

Production-style full-stack Incident and Issue Management System with role-based workflows, SLA tracking, escalation monitoring, and observability.

## Project Overview

This platform supports real-world incident handling across multiple roles:
- Raise and manage issues
- Track lifecycle and audit history
- Apply SLA policies and breach detection
- Monitor reports, workload, and metrics

## Roles

- `SUPER_ADMIN`: platform-level governance
- `ADMIN`: company-level user/project/rule management
- `MANAGER`: team oversight, SLA monitoring, review and closure
- `ENGINEER`: issue execution, start/resolve flow
- `USER`: issue reporting and tracking

## Issue Lifecycle

`CREATED -> OPEN -> IN_PROGRESS -> RESOLVED -> CLOSED`

## Tech Stack

### Frontend
- React 19
- Vite
- Tailwind CSS
- Axios

### Backend
- Java 21
- Spring Boot
- Spring Security (JWT)
- Spring Data JPA / Hibernate
- Micrometer + Actuator + Prometheus registry

### Database
- PostgreSQL

## Architecture

`React UI -> Spring Boot REST APIs -> Service Layer -> PostgreSQL -> Metrics (Prometheus/Grafana optional)`

---

## Prerequisites

- Java 21 (JDK)
- Node.js 20+ (npm)
- PostgreSQL 14+ running locally or remotely

---

## One-Command Dependency Setup

From project root:

```powershell
powershell -ExecutionPolicy Bypass -File .\setup.ps1
```

This will:
1. Install frontend npm dependencies.
2. Download backend Maven dependencies.
3. Compile backend for sanity check.

---

## Database Behavior (Important)

The backend is configured to:
- auto-create PostgreSQL database if missing (when enabled)
- auto-create/update tables via Hibernate

Defaults in `backend/src/main/resources/application.properties`:
- `spring.datasource.url=jdbc:postgresql://localhost:5432/smartims`
- `spring.datasource.username=postgres`
- `spring.datasource.password=postgres`
- `app.db.auto-create.enabled=true`
- `spring.jpa.hibernate.ddl-auto=update`

If you do not want DB auto-create:
- set `APP_DB_AUTO_CREATE_ENABLED=false`

Note:
- DB auto-create requires DB user permission to create databases (`CREATEDB`).

---

## Run the Project

Open two terminals at project root.

### 1) Start Backend

```powershell
.\backend\mvnw.cmd -f backend\pom.xml spring-boot:run
```

Backend default URL:
- `http://localhost:8080`

### 2) Start Frontend

```powershell
npm --prefix frontend run dev
```

Frontend dev URL:
- `http://localhost:5173`

---

## Environment Variables (Optional Overrides)

### Database
- `SPRING_DATASOURCE_URL`
- `SPRING_DATASOURCE_USERNAME`
- `SPRING_DATASOURCE_PASSWORD`
- `APP_DB_AUTO_CREATE_ENABLED`

### Mail
- `MAIL_USERNAME`
- `MAIL_PASSWORD`
- `MAIL_FROM`

---

## Useful Commands

### Frontend
```powershell
npm --prefix frontend run lint
npm --prefix frontend run build
```

### Backend
```powershell
.\backend\mvnw.cmd -f backend\pom.xml -DskipTests compile
.\backend\mvnw.cmd -f backend\pom.xml test
```

---

## Monitoring

Actuator and Prometheus metrics are enabled in backend configuration.
Typical metrics endpoint:
- `http://localhost:8080/actuator/prometheus`

---

## Troubleshooting

- `DB connection/auth failed`:
  - Verify PostgreSQL is running and credentials are correct.
  - Update datasource env vars if needed.
- `DB not auto-created`:
  - Ensure `APP_DB_AUTO_CREATE_ENABLED=true`.
  - Ensure DB user has `CREATEDB` privilege.
- `Frontend fails to start`:
  - Run `npm --prefix frontend install` again.
- `Backend fails to start`:
  - Run `.\backend\mvnw.cmd -f backend\pom.xml -DskipTests compile` and check stack trace.

---

## Author

**Dhrumil Trivedi**  
Final Year Student | Java Backend Developer
