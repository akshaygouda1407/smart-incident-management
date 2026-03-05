# Data Dictionary (PostgreSQL)

Source of truth: backend JPA entities under `backend/src/main/java/com/smartims/entity`.

## Conventions

- **PK**: Primary key
- **FK**: Foreign key
- **UQ**: Unique constraint
- `created_at`, `received_at`, etc. are stored as `timestamp` (JPA `LocalDateTime`)
- Enums are stored as `varchar` (JPA `@Enumerated(EnumType.STRING)`)

## Enumerations

- `role` (`users.role`): `SUPER_ADMIN`, `ADMIN`, `MANAGER`, `ENGINEER`, `USER`
- `issues.severity`: `LOW`, `MEDIUM`, `HIGH`, `CRITICAL`
- `issues.status`: `OPEN`, `IN_PROGRESS`, `RESOLVED`, `CLOSED`
- `otp_verification.purpose`: `REGISTER`, `FORGOT_PASSWORD`

---

## Table: `users`

Represents a platform user (all roles).

| Column | Type | Null | Key / Constraints | Description |
|---|---|---:|---|---|
| `id` | `bigserial` | no | PK | User identifier |
| `full_name` | `varchar(255)` | no |  | Full name |
| `email` | `varchar(255)` | no | UQ | Login/email (unique) |
| `password` | `varchar(255)` | no |  | Password hash |
| `company` | `varchar(255)` | yes |  | Company/tenant label |
| `role` | `varchar(255)` | yes | enum | User role |
| `enabled` | `boolean` | no | default `true` (via `@PrePersist`) | Account enabled flag |
| `verified` | `boolean` | no | default `true` (via `@PrePersist`) | Email/identity verified flag |
| `locked` | `boolean` | no | default `false` (via `@PrePersist`) | Locked/soft-delete flag |
| `token_version` | `integer` | no | default `0` | Token invalidation version |
| `must_change_password` | `boolean` | no | default `false` | Forces password change on next login |

---

## Table: `projects`

Represents a project owned by a manager and associated to a company.

| Column | Type | Null | Key / Constraints | Description |
|---|---|---:|---|---|
| `id` | `bigserial` | no | PK | Project identifier |
| `name` | `varchar(255)` | no | UQ | Project name (unique) |
| `description` | `varchar(10000)` | yes |  | Project description |
| `manager_id` | `bigint` | yes | FK → `users.id` | Owning manager |
| `company` | `varchar(255)` | yes |  | Company/tenant label |
| `active` | `boolean` | yes | default `true` | Active flag |
| `created_at` | `timestamp` | yes | default now (entity default) | Created timestamp |

### Join table: `project_members`

Many-to-many between `projects` and `users`.

| Column | Type | Null | Key / Constraints | Description |
|---|---|---:|---|---|
| `project_id` | `bigint` | no | FK → `projects.id` | Project |
| `user_id` | `bigint` | no | FK → `users.id` | Member (engineer/user) |

---

## Table: `issues`

Represents an incident/issue within a project.

| Column | Type | Null | Key / Constraints | Description |
|---|---|---:|---|---|
| `id` | `bigserial` | no | PK | Issue identifier |
| `title` | `varchar(255)` | no |  | Issue title |
| `description` | `varchar(1000)` | no |  | Issue description |
| `severity` | `varchar(255)` | no | enum | Severity |
| `status` | `varchar(255)` | no | enum | Workflow status |
| `created_by` | `varchar(255)` | no |  | Creator email (from JWT) |
| `priority_level` | `varchar(255)` | yes |  | Priority label (ties to SLA policy) |
| `assigned_engineer_id` | `bigint` | yes | FK → `users.id` | Assigned engineer |
| `project_id` | `bigint` | no | FK → `projects.id` | Owning project |
| `created_at` | `timestamp` | yes |  | Created timestamp |
| `resolved_at` | `timestamp` | yes |  | Resolved timestamp |
| `sla_start_time` | `timestamp` | yes |  | SLA start time |
| `sla_due_time` | `timestamp` | yes |  | SLA due time |
| `sla_breached` | `boolean` | yes | default `false` (via `@PrePersist`) | SLA breached flag |
| `escalated` | `boolean` | yes | default `false` (via `@PrePersist`) | Escalation flag |
| `triaged` | `boolean` | yes | default `false` (via `@PrePersist`) | Triage completed flag |

---

## Table: `issue_comments`

User comments on an issue.

| Column | Type | Null | Key / Constraints | Description |
|---|---|---:|---|---|
| `id` | `bigserial` | no | PK | Comment identifier |
| `comment` | `varchar(255)` | no |  | Comment text |
| `issue_id` | `bigint` | no | FK → `issues.id` | Related issue |
| `user_id` | `bigint` | no | FK → `users.id` | Author user |
| `commented_by` | `bigint` | no | FK → `users.id` | Actor (can differ from `user_id`) |
| `created_at` | `timestamp` | no | default now (entity default) | Created timestamp |

---

## Table: `issue_activities`

Audit-like history events for an issue (status change, assignment, etc.).

| Column | Type | Null | Key / Constraints | Description |
|---|---|---:|---|---|
| `id` | `bigserial` | no | PK | Activity identifier |
| `issue_id` | `bigint` | no | FK → `issues.id` | Related issue |
| `action` | `varchar(255)` | no |  | Activity type (e.g., `STATUS_CHANGE`) |
| `description` | `varchar(255)` | no |  | Activity description |
| `performed_by` | `bigint` | no | FK → `users.id` | Actor |
| `created_at` | `timestamp` | yes | default now (via `@PrePersist`) | Created timestamp |

---

## Table: `issue_attachments`

Files attached to an issue.

| Column | Type | Null | Key / Constraints | Description |
|---|---|---:|---|---|
| `id` | `bigserial` | no | PK | Attachment identifier |
| `issue_id` | `bigint` | no | FK → `issues.id` | Related issue |
| `file_name` | `varchar(255)` | no |  | Original file name |
| `file_type` | `varchar(255)` | yes |  | MIME/type hint |
| `file_path` | `varchar(255)` | no |  | Storage path |
| `uploaded_by` | `bigint` | no | FK → `users.id` | Uploader |
| `uploaded_at` | `timestamp` | yes | default now (via `@PrePersist`) | Uploaded timestamp |

---

## Table: `sla_policies`

SLA rules per project + priority.

| Column | Type | Null | Key / Constraints | Description |
|---|---|---:|---|---|
| `id` | `bigserial` | no | PK | SLA policy identifier |
| `priority_level` | `varchar(255)` | yes | UQ (with `project_id`) | Priority label |
| `resolution_time_minutes` | `integer` | no |  | Resolution time target |
| `description` | `varchar(1000)` | yes |  | Policy notes |
| `project_id` | `bigint` | no | FK → `projects.id` | Project |

Constraints:
- Unique: (`project_id`, `priority_level`)

---

## Table: `sla_breaches`

Records SLA breach incidents for issues.

| Column | Type | Null | Key / Constraints | Description |
|---|---|---:|---|---|
| `id` | `bigserial` | no | PK | Breach identifier |
| `issue_id` | `bigint` | no | FK → `issues.id` | Related issue |
| `breached_at` | `timestamp` | yes |  | Time breached |
| `sla_due_time` | `timestamp` | yes |  | Due time at breach |
| `delay_minutes` | `bigint` | yes |  | Delay duration |

---

## Table: `notifications`

System notification templates/events (linked to users via `user_notifications`).

| Column | Type | Null | Key / Constraints | Description |
|---|---|---:|---|---|
| `id` | `bigserial` | no | PK | Notification identifier |
| `type` | `varchar(255)` | no |  | Type key (e.g., `ISSUE_ASSIGNED`) |
| `message` | `varchar(1000)` | no |  | Message text |
| `entity_type` | `varchar(255)` | yes |  | Context entity type (ISSUE/PROJECT/SLA) |
| `entity_id` | `bigint` | yes |  | Context entity id |
| `created_at` | `timestamp` | no |  | Created timestamp |

---

## Table: `user_notifications`

Per-user inbox entries for notifications.

| Column | Type | Null | Key / Constraints | Description |
|---|---|---:|---|---|
| `id` | `bigserial` | no | PK | Row identifier |
| `user_id` | `bigint` | no | FK → `users.id` | Recipient |
| `notification_id` | `bigint` | no | FK → `notifications.id` | Notification |
| `read` | `boolean` | no |  | Read flag |
| `received_at` | `timestamp` | no |  | Received timestamp |

Constraints:
- Unique: (`user_id`, `notification_id`)

---

## Table: `audit_logs`

Central audit log for system/user actions.

| Column | Type | Null | Key / Constraints | Description |
|---|---|---:|---|---|
| `id` | `bigserial` | no | PK | Audit row identifier |
| `actor_email` | `varchar(255)` | no |  | Actor email (or system) |
| `actor_role` | `varchar(255)` | no |  | Actor role (or `SYSTEM`) |
| `action` | `varchar(255)` | no |  | Action key |
| `entity_type` | `varchar(255)` | no |  | Entity type key |
| `entity_id` | `bigint` | yes |  | Entity id |
| `description` | `varchar(1000)` | yes |  | Optional details |
| `timestamp` | `timestamp` | no |  | Event timestamp |

---

## Table: `contact_message`

Messages submitted via the public contact form.

| Column | Type | Null | Key / Constraints | Description |
|---|---|---:|---|---|
| `id` | `bigserial` | no | PK | Message identifier |
| `name` | `varchar(255)` | yes |  | Sender name |
| `email` | `varchar(255)` | yes |  | Sender email |
| `subject` | `varchar(255)` | yes |  | Subject |
| `message` | `varchar(2000)` | yes |  | Message body |
| `created_at` | `timestamp` | yes |  | Created timestamp |

---

## Table: `otp_verification`

Email OTP records for registration and password reset.

| Column | Type | Null | Key / Constraints | Description |
|---|---|---:|---|---|
| `id` | `bigserial` | no | PK | OTP row identifier |
| `email` | `varchar(255)` | yes |  | Target email |
| `otp` | `varchar(255)` | yes |  | OTP code (6 digits stored as string) |
| `purpose` | `varchar(255)` | no | enum | Purpose |
| `verified` | `boolean` | yes |  | Whether OTP was used |
| `created_at` | `timestamp` | yes |  | Created timestamp |
| `expiry_time` | `timestamp` | yes |  | Expiry timestamp |

