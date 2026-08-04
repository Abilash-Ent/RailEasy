# RailEasy — Reactive Train Ticket Reservation (POC)

A production-style Proof of Concept built with **Java 21**, **Spring Boot 4.x**, **Spring WebFlux**,
**Spring Data R2DBC (H2)**, **Reactive Spring Security** and **JWT** authentication.

## Tech Stack
- Spring WebFlux (fully reactive REST APIs — `Mono`/`Flux`)
- Spring Data R2DBC + H2 (in-memory), `ReactiveCrudRepository`
- Reactive Spring Security + JWT (JJWT), BCrypt password hashing
- Jakarta Bean Validation, centralized `@RestControllerAdvice`
- OpenAPI / Swagger UI (springdoc)
- OpenPDF (PDF e-ticket generation)
- MapStruct (compile-time, type-safe entity ⇄ DTO mapping)
- Lombok, externalized `application.yml`

## Architecture (Clean layered)
```
controller  -> REST endpoints (WebFlux)
service      -> business logic (interfaces + impl)
repository   -> ReactiveCrudRepository
dto          -> request / response models
entity       -> R2DBC tables
mapper       -> entity <-> dto
security     -> JWT filter chain, user details, handlers
config       -> security, openapi, h2 console, seed data
exception    -> custom exceptions + global handler
util         -> JwtService, PnrGenerator, SecurityUtils, TicketPdfGenerator
constants    -> AppConstants, Role, BookingStatus
```

## Modules & Endpoints
| Module | Method & Path | Access |
|--------|---------------|--------|
| Auth | `POST /api/v1/auth/register` | Public |
| Auth | `POST /api/v1/auth/login` | Public |
| Auth | `POST /api/v1/auth/logout` | Public |
| Schedule | `GET /api/v1/schedules?from=&to=&date=` | Public (train search) |
| Schedule | `GET /api/v1/schedules/{id}` | Public |
| Schedule | `GET /api/v1/schedules/{id}/seats?class=` | Public (seat map) |
| Schedule | `POST /api/v1/schedules` | ADMIN |
| Schedule | `PUT /api/v1/schedules/{id}` | ADMIN |
| Schedule | `DELETE /api/v1/schedules/{id}` | ADMIN |
| Train | `POST /api/v1/trains` | ADMIN |
| Train | `PUT /api/v1/trains/{id}` | ADMIN |
| Train | `DELETE /api/v1/trains/{id}` | ADMIN |
| Train | `GET /api/v1/trains` | Authenticated |
| Train | `GET /api/v1/trains/{id}` | Authenticated |
| Booking | `POST /api/v1/bookings` | Authenticated (USER/ADMIN) |
| Booking | `GET /api/v1/bookings/mine` | Authenticated (USER/ADMIN) |
| Booking | `GET /api/v1/bookings/{id}` | Authenticated (USER/ADMIN) |
| Booking | `PUT /api/v1/bookings/{id}/cancel` | Authenticated (USER/ADMIN) |
| Booking | `GET /api/v1/bookings/{id}/ticket` (PDF e-ticket) | Authenticated (USER/ADMIN) |

Travel classes: `SLEEPER`, `AC_3`, `AC_2`. Seat layout: **8×8 = 64 seats** per class
(labels `1A`..`8H`); 1–4 seats per booking; PNR = first 8 chars of a UUID.

## Entities & Relationships
- `Train (1) — (M) Schedule`
- `User (1) — (M) Booking`
- `Schedule (1) — (M) Booking`

## Running
```bash
./mvnw spring-boot:run
```
- App: http://localhost:8080
- Swagger UI: http://localhost:8080/swagger-ui.html
- OpenAPI JSON: http://localhost:8080/v3/api-docs
- H2 console (dev): http://localhost:8082
  - JDBC URL: `jdbc:h2:tcp://localhost:9092/mem:raileasy`
  - User: `sa` (no password)

> ⚠️ **H2 console tip:** use the **JDBC** URL above (`jdbc:h2:tcp://...`).
> Do **not** paste the app's **R2DBC** URL (`r2dbc:h2:mem:///raileasy?options=...`)
> into the console — the console uses `org.h2.Driver`, which only accepts
> `jdbc:h2:` URLs and will otherwise report
> *"Driver org.h2.Driver is not suitable for r2dbc:h2:... [08001]"*.

### Seeded default admin
The admin account is created by `src/main/resources/data.sql` (run at startup via
`R2dbcInitializerConfig`):
- email: `admin@raileasy.com`
- password: `password`

To change the admin password, replace the BCrypt hash in `data.sql`. Generate a hash for
any plaintext with:
```bash
# using Spring Boot CLI
spring encodepassword 'YourNewPassword'
# or a quick one-off with htpasswd (bcrypt)
htpasswd -bnBC 10 "" 'YourNewPassword' | tr -d ':\n'
```

## Typical flow
1. `POST /api/v1/auth/login` with the admin credentials to get a JWT.
2. Add trains via `POST /api/v1/trains` (send `Authorization: Bearer <token>`).
3. `POST /api/v1/auth/register` a normal user, login, then book via `POST /api/v1/bookings`.

## Configuration
All settings live in `src/main/resources/application.yml`. Override the JWT secret
in real environments via the `RAILEASY_JWT_SECRET` environment variable.

## Testing
Unit tests use **JUnit 5**, **Mockito** and Reactor's **`StepVerifier`** (no database
or Spring context needed for the service tests), covering the business logic:

- `AuthServiceImplTest` — register (duplicate/success), login (success/bad password/unknown user)
- `TrainServiceImplTest` — create (duplicate/success), not-found paths
- `ScheduleServiceImplTest` — seat availability, search enrichment, validation
- `BookingServiceImplTest` — seat validation (invalid label, 1–4 rule), clash detection,
  booking confirmation, cancel (owner/not-owner/already-cancelled), My Tickets,
  PDF ticket download (owner/not-owner)
- `TicketPdfGeneratorTest` — PDF e-ticket rendering (valid `%PDF` output for
  confirmed/cancelled/sparse bookings, null-input guard)
- `TrainMapperTest`, `ScheduleMapperTest`, `BookingMapperTest` — entity ⇄ DTO mapping,
  null-safe enrichment, seat CSV parsing
- `JwtServiceTest` — token generate/validate round-trip, expiry, issuer & malformed-token handling
- `GlobalExceptionHandlerTest` — status/body mapping for every handled exception type
- `SecurityUtilsTest` — reactive current-user resolution
- `SeatUtilsTest`, `PnrGeneratorTest` — seat layout and PNR format

Run them with:
```bash
./mvnw test
```

Code-coverage is measured with **JaCoCo** (via the `jacoco-maven-plugin`). After a test
run, open the HTML report at:
```
target/site/jacoco/index.html
```

## Postman collection
A ready-to-use collection lives at `docs/RailEasy.postman_collection.json`.

1. Import it into Postman.
2. (Optional) set the `baseUrl` variable — defaults to `http://localhost:8080`.
3. Run **Auth → Login (admin)** — the JWT is captured automatically into the `token`
   variable, so every secured request sends `Authorization: Bearer {{token}}`.
4. The **Book seats** request saves the new booking id into `bookingId` for the
   get/cancel requests; set `scheduleId` / `trainId` from search/list responses.
5. **Bookings → Download ticket (PDF)** returns `application/pdf`; use
   Postman's **Send and Download** to save the e-ticket file.

Folders: **Auth**, **Schedules (search & seats – public)**, **Schedules (admin)**,
**Trains**, **Bookings**.
