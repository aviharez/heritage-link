# Heritage Link API

Integrated Inventory and Heirloom Management API for Heritage Transitions, a boutique Senior Move Management firm.

## Overview

HeritageLink replaces a manual spreadsheet system with a robust REST API that tracks physical household items through a four-stage disposition lifecycle:

```
IDENTIFIED -> APPRAISED -> ASSIGNED -> DISPOSED
```

Key capabilities:
- Full CRUD for inventory items with rich metadata (dimensions, sentimental scope, fragile flag)
- Disposition workflow enforcement with business-rule validation
- Heirloom claim management with automatic mediation flagging
- Mover's manifest generation filtered to RELOCATION items
- Immutable audit trail for every state change

---

## Tech Stack

| Layer      | Technology                                               |
|------------|----------------------------------------------------------|
| Framework  | Spring Boot 4.0.5                                        |
| Language   | Java 17                                                  |
| Database   | H2 (in-memory, auto-populated on startup)                |
| ORM        | Spring Data JPA / Hibernate                              |
| Validation | Jakarta Bean Validation                                  |
| Security   | Spring Security - HTTP Basic Auth (ADMIN / VIEWER roles) |
| API Docs   | SpringDoc OpenAPI 2.3 / Swagger UI                       |
| Build      | Maven                                                    |
| Tests      | JUnit 5 + Mockito + Spring Security Test                 |

---

## Prerequisites

- Java 17+ (`java -version`)
- Maven (`mvn -version`)
- No external database required. H2 runs in memory

---

## Setup & Running

### 1. Clone / open the project

```bash
cd heritage-link
```

### 2. Build

```bash
mvn clean package -DskipTests
```

### 3. Run

```bash
mvn spring-boot:run
```

or run the generated JAR:

```bash
java -jar target/heritage-link-1.0.0.jar
```

The server starts on **http://localhost:8080**.

> **Note:** The database is in-memory and pre-populated with 8 items, 4 claimants, and 3 claims every time the application starts.

---

## API Documentation

### Swagger UI (interactive)

Local SwaggerUI
```
http://localhost:8080/swagger-ui.html
```

Deployed Swagger UI
```
https://heritage-link-production.up.railway.app//swagger-ui.html
```

### OpenAPI JSON spec

```
http://localhost:8080/api-docs
```

### H2 Database Console

```
http://localhost:8080/h2-console
JDBC URL:  jdbc:h2:mem:heritagelink
Username:  sa
Password:  (blank)
```

---

## Authentication

All `/api/**` endpoints are protected with **HTTP Basic Authentication**.

| Username | Password | Role   | Access |
|----------|----------|--------|--------|
| `admin`  | `admin123`  | ADMIN  | Full read/write |
| `viewer` | `viewer123` | VIEWER | GET requests only |

### Swagger UI
Click the **Authorize** button at the top of the Swagger UI page and enter credentials before using Try It Out.

### curl example
```bash
# As admin
curl -u admin:admin123 http://localhost:8080/api/items

# As viewer (read-only)
curl -u viewer:viewer123 http://localhost:8080/api/items

# Write operation requires ADMIN
curl -u admin:admin123 -X POST http://localhost:8080/api/items \
  -H "Content-Type: application/json" \
  -d '{"name":"Antique Clock","roomOfOrigin":"Study","sentimentalScore":8}'
```

Every authenticated action is recorded in the audit log with the **real username**, replacing the prior `SYSTEM` placeholder.

---

## Pagination

All list endpoints return a paginated `Page<T>` response instead of a flat array.

### Query parameters

| Parameter | Default | Description |
|-----------|---------|-------------|
| `page`    | `0`     | Zero-based page index |
| `size`    | `20` (audit: `50`) | Items per page |
| `sort`    | endpoint-specific | Field name and direction, e.g. `sort=createdAt,desc` |

### Example requests
```
GET /api/items?page=0&size=10&sort=sentimentalScore,desc
GET /api/items?status=APPRAISED&room=Study&page=0&size=5
GET /api/claims/item/1?page=0&size=20&sort=claimedAt,asc
GET /api/audit?page=0&size=50&sort=timestamp,desc
```

### Response shape
```json
{
  "content": [ ...items... ],
  "pageable": { "pageNumber": 0, "pageSize": 20, "sort": { ... } },
  "totalElements": 14,
  "totalPages": 1,
  "last": true,
  "first": true,
  "numberOfElements": 14
}
```

---

## Item Lifecycle

Items move through a linear four-stage pipeline enforced by the API:

```
IDENTIFIED -> APPRAISED -> ASSIGNED -> DISPOSED
```

| Stage | Endpoint | Rules |
|---|---|---|
| Create | `POST /api/items` | Sets status to `IDENTIFIED` |
| Appraise | `PUT /api/items/{id}/appraise` | Sets `estimatedValue`; advances to `APPRAISED` |
| Assign | `PUT /api/items/{id}/assign` | Sets `dispositionType`; advances to `ASSIGNED`. **SALE requires value > $0** |
| Dispose | `PUT /api/items/{id}/dispose` | Advances to `DISPOSED` (terminal state) |

**Status filters** are available on `GET /api/items?status=APPRAISED&dispositionType=RELOCATION&room=Study`.

---

## Mediation Workflow

When a second (or subsequent) family member submits a claim on an item whose `sentimentalScore` is **>= 7**, the item is automatically flagged:

```json
"mediationRequired": true
```

While `mediationRequired` is `true`:
- All `appraise`, `assign`, and `dispose` calls return `409 CONFLICT`
- The error body clearly identifies the cause as `MEDIATION_REQUIRED`

### Resolving a conflict

```http
PUT /api/claims/{claimId}/resolve
{
  "resolution": "APPROVED",
  "resolutionNotes": "Family mediation session held. Margaret's claim accepted."
}
```

- **APPROVED**: auto-dismisses all other active claims and clears the mediation flag
- **DISMISSED**: clears the mediation flag once fewer than 2 active claims remain

---

## Logistics Manifest

```http
GET /api/manifest
```

Returns a structured JSON manifest of all items with `dispositionType = RELOCATION` in `ASSIGNED` or `DISPOSED` status.

**Fragile items** automatically receive a `[FRAGILE - HANDLE WITH EXTREME CARE]` prefix in their `specialHandlingNotes`.

### Planning preview (all RELOCATION items regardless of status)

```http
GET /api/manifest/preview
```

### Example response

```json
{
  "generatedAt": "2024-03-15T09:30:00",
  "totalItems": 3,
  "fragileItemCount": 2,
  "totalEstimatedValue": 8300.00,
  "items": [
    {
      "itemId": 3,
      "name": "Mahogany Grandfather Clock",
      "roomOfOrigin": "Living Room",
      "dimensions": { "widthCm": 55.0, "heightCm": 195.0, "depthCm": 30.0 },
      "estimatedValue": 4200.00,
      "fragile": true,
      "specialHandlingNotes": "[FRAGILE - HANDLE WITH EXTREME CARE] Pendulum and weights must be removed and packed separately.",
      "status": "ASSIGNED"
    }
  ]
}
```

---

## Audit Trail

Every state change is recorded immutably:

```http
GET /api/audit              # all audit records
GET /api/audit/item/{id}    # reverse-chronological trail for one item
```

Logged actions include: `ITEM_CREATED`, `ITEM_UPDATED`, `ITEM_DELETED`, `STATUS_CHANGE`, `CLAIM_SUBMITTED`, `MEDIATION_FLAGGED`, `CLAIM_APPROVED`, `CLAIM_DISMISSED`.

---