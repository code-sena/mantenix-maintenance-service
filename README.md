# mantenix-maintenance-service

Mantenix microservice that keeps the **maintenance history of each vehicle**: what was done,
when, at what mileage, how much it cost, and when the next service is due.

| Item | Value |
|------|-------|
| Port | `8084` |
| Database | PostgreSQL `mantenix_maintenance` (own database — ADR-004) |
| Stack | Java 21 · Spring Boot 4 · Maven · Flyway · springdoc-openapi (ADR-003) |
| Owner | Johann Tafur Farfán |
| Design docs | [code-sena/mantenix-docs](https://github.com/code-sena/mantenix-docs) |

---

## Run locally

Requirements: JDK 21 or newer and Docker.

```bash
# 1. Start the database (PostgreSQL 17 on localhost:5434)
docker compose up -d

# 2. Start the service (Flyway creates the tables on startup)
./mvnw spring-boot:run          # Windows: mvnw.cmd spring-boot:run
```

| What | URL |
|------|-----|
| Swagger UI | http://localhost:8084/swagger-ui.html |
| OpenAPI JSON | http://localhost:8084/v3/api-docs |
| Health check | http://localhost:8084/actuator/health |

Run the unit tests (no database needed): `./mvnw test`

---

## Endpoints

| Method | Path | Description |
|--------|------|-------------|
| `POST` | `/maintenance-records` | Register a maintenance record |
| `GET` | `/maintenance-records?vehicleId=` | List records, newest first (optional filter by vehicle) |
| `GET` | `/maintenance-records/upcoming?until=` | Records whose next service date is on or before `until` (default: today + 30 days) |
| `GET` | `/maintenance-records/{id}` | Get a record |
| `PUT` | `/maintenance-records/{id}` | Update a record |
| `DELETE` | `/maintenance-records/{id}` | Delete a record |

Maintenance types: `OIL_CHANGE`, `TIRES`, `BRAKES`, `BATTERY`, `GENERAL_INSPECTION`, `OTHER`.

Example request:

```json
{
  "vehicleId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
  "type": "OIL_CHANGE",
  "description": "Synthetic oil 5W-30 and oil filter",
  "serviceDate": "2026-11-15",
  "mileage": 35000,
  "cost": 180000.00,
  "workshop": "Central Workshop",
  "nextServiceDate": "2027-05-15",
  "nextServiceMileage": 40000
}
```

Only `vehicleId`, `type`, `serviceDate` and `mileage` are required.

### Business rules

| Rule | Error |
|------|-------|
| The service date cannot be in the future | `400 VALIDATION_ERROR` |
| The next service date must be after the service date | `409 INVALID_NEXT_SERVICE_DATE` |
| The next service mileage must be greater than the mileage | `409 INVALID_NEXT_SERVICE_MILEAGE` |
| Mileage and cost cannot be negative | `400 VALIDATION_ERROR` |

Errors follow the shared format `{ "error", "message", "details" }`. A body that is not valid JSON or has
a wrong value (e.g. an unknown `type`) returns `400 MALFORMED_REQUEST`.

### Pending decisions

| Topic | Status |
|-------|--------|
| Check that `vehicleId` exists in vehicle-service before saving | To be decided (ADR-004: REST call or event, never a direct DB query) |
| Send reminders for upcoming services through notification-service | To be decided |

---

## Structure (layers — ADR-002)

```
src/main/java/com/mantenix/maintenance/
├── controller/   MaintenanceRecordController  → REST endpoints, DTOs only
├── dto/          MaintenanceRecordRequest, MaintenanceRecordResponse, ErrorResponse
├── service/      MaintenanceRecordService     → business rules
├── repository/   MaintenanceRecordRepository  → Spring Data JPA
├── model/        MaintenanceRecord, MaintenanceType → JPA entity
└── exception/    GlobalExceptionHandler and custom exceptions

src/main/resources/
├── application.properties                     → ddl-auto=validate
└── db/migration/V1__create_maintenance_records_table.sql
```

`spring.jpa.hibernate.ddl-auto=validate`: Hibernate only checks the schema. Tables are created
and changed **only** by Flyway migrations. An applied migration is never edited — add `V2__...`.
