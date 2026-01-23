# Smart Incident & Issue Management Platform

A **full‑stack, enterprise‑style Incident & Issue Management System** inspired by real‑world tools like Jira and ServiceNow. This project is designed as a **production‑grade backend‑heavy application** suitable for college major projects, real‑world learning, and technical interviews.

---

## Table of Contents

* [Project Overview](#-project-overview)
* [Why This Project?](#-why-this-project)
* [Key Features](#-key-features)
* [Role-Based Access](#-role-based-access)
* [Issue Lifecycle](#-issue-lifecycle)
* [Smart Priority & SLA Management](#-smart-priority--sla-management)
* [System Architecture](#-system-architecture)
* [Tech Stack](#-tech-stack)
* [Database Design](#-database-design)
* [API Overview](#-api-overview)
* [Monitoring & Observability](#-monitoring--observability)
* [Security](#-security)
* [Installation & Setup](#-installation--setup)
* [Future Enhancements](#-future-enhancements)
* [Screenshots](#-screenshots)
* [Author](#-author)

---

## Project Overview

**Smart Incident & Issue Management Platform** is a multi‑user, multi‑role web application that allows organizations to:

* Report production issues or incidents
* Track issue status and resolution
* Enforce SLA rules automatically
* Escalate unresolved issues
* Monitor system health and application metrics

The system simulates how **real IT companies manage incidents in production environments**.

---

## Why This Project?

This project was built to:

* Demonstrate **real‑world backend engineering skills**
* Showcase **Spring Boot, Security, Monitoring, and Business Logic**
* Act as a **strong portfolio project for interviews**
* Teach enterprise concepts like:

  * SLA
  * Escalations
  * Role‑based access
  * Monitoring & alerts

---

## Key Features

### User & Authentication

* User Registration & Login
* JWT‑based Authentication (Stateless)
* Password Encryption using BCrypt
* Role‑Based Access Control (RBAC)
* Token Expiry & Logout Handling

### Role-Based Access

* **USER** – Raise and track issues
* **ENGINEER** – Resolve assigned issues
* **MANAGER** – Monitor SLA & escalations
* **ADMIN** – Manage users, roles & rules

### Issue Management

* Raise new issues with:

  * Title & description
  * Severity & category
  * Affected users count
  * Attachments (logs, screenshots)
* Auto‑generated Issue ID
* Issue History & Audit Trail
* Edit and update issues

---

## Issue Lifecycle

```
OPEN → IN_PROGRESS → RESOLVED → CLOSED
```

* Automatic status transitions
* Only authorized roles can change specific states
* Full audit logging for compliance

---

## Smart Priority & SLA Management

### Dynamic Priority Calculation

Priority is automatically calculated using:

* Severity level
* Number of affected users
* Issue category
* Time since issue creation

### SLA Enforcement

* SLA rules configurable by Admin
* Automatic SLA breach detection
* Escalation to Manager when breached

---

## System Architecture

```
Client (UI)
   ↓
Spring Boot REST APIs
   ↓
Business Logic Layer
   ↓
Database (MySQL / PostgreSQL)
   ↓
Monitoring (Prometheus + Grafana)
```

* Clean layered architecture
* Stateless REST APIs
* Scalable & maintainable design

---

## Tech Stack

### Backend

* Java 17+
* Spring Boot
* Spring Security (JWT)
* Spring Data JPA
* Hibernate

### Database

* MySQL / PostgreSQL

### Monitoring & Observability

* Prometheus
* Grafana
* Micrometer

### Build & Tools

* Maven
* Postman
* Docker (optional)

---

## Database Design

Main Entities:

* User
* Role
* Issue
* IssueHistory
* SLAConfig
* EscalationLog
* Attachment

Relationships are designed using:

* One‑to‑Many
* Many‑to‑Many
* Proper indexing for performance

---

## API Overview

Sample endpoints:

* `POST /auth/register`
* `POST /auth/login`
* `POST /issues`
* `GET /issues/{id}`
* `PUT /issues/{id}/status`
* `GET /metrics`

All APIs are secured using JWT authentication.

---

## Monitoring & Observability

The application exposes metrics for:

* API response times
* Error rates
* Issue creation rate
* SLA breaches
* System health

Metrics are visualized using **Grafana dashboards** powered by **Prometheus**.

---

## Security

* JWT‑based authentication
* BCrypt password hashing
* Role‑based authorization
* Secure API endpoints
* Centralized exception handling

---

## Installation & Setup

### Prerequisites

* Java 17+
* Maven
* MySQL / PostgreSQL
* Prometheus & Grafana (optional)

### Steps

```bash
# Clone the repository
git clone https://github.com/your-username/smart-incident-management.git

# Navigate to project directory
cd smart-incident-management

# Build the project
mvn clean install

# Run the application
mvn spring-boot:run
```

Application runs on:

```
http://localhost:8080
```

---

## Future Enhancements

* Email & Slack notifications
* UI dashboard (React / Angular)
* AI‑based incident prediction
* Auto‑assignment of engineers
* Kubernetes deployment
* Multi‑tenant support

---

## Author

**Dhrumil Trivedi**
Final Year Student | Java Backend Developer

---

If you like this project, consider giving it a star!
