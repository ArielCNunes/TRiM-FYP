# TRiM Backend API

Spring Boot REST API powering the TRiM barbershop booking platform.

---

## Tech Stack

| Technology | Purpose |
|-----------|---------|
| Java 21 | Language |
| Spring Boot 3.5.6 | Application framework |
| Spring Security | Authentication & authorisation |
| Spring Data JPA | Database access (Hibernate) |
| PostgreSQL 15+ | Relational database |
| JWT (jjwt 0.11.5) | Stateless auth tokens |
| Twilio SDK 9.14.1 | SMS notifications |
| Spring Mail | Email notifications (Gmail SMTP) |
| SpringDoc OpenAPI 2.8 | Swagger UI / API docs |
| DataFaker 2.4.2 | Test data generation |
| Gatling 3.11.5 | Load / performance testing |
| H2 | In-memory database for tests |

---

## Prerequisites

- **Java 21**
- **Maven 3.9+** (or use the included `./mvnw` wrapper)
- **PostgreSQL 15+**
- **Stripe** account (secret key, publishable key, webhook secret)
- **Twilio** account (SID, auth token, phone number)
- **Gmail** app password (for SMTP email)

---

## Environment Setup

The application uses `dotenv-java` to load environment variables from a `.env` file in the project root (`backend/trim-booking-api/.env`).

Create the file with the following variables:

```env
# Email (Gmail SMTP)
MAIL_USERNAME=your-email@gmail.com
MAIL_PASSWORD=your-gmail-app-password

# Twilio SMS
TWILIO_ACCOUNT_SID=ACxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx
TWILIO_AUTH_TOKEN=your-twilio-auth-token
TWILIO_PHONE_NUMBER=+15551234567

# Stripe Payments
STRIPE_SECRET_KEY=sk_test_xxxxxxxxxxxxxxxxxxxx
STRIPE_PUBLISHABLE_KEY=pk_test_xxxxxxxxxxxxxxxxxxxx
STRIPE_WEBHOOK_SECRET=whsec_xxxxxxxxxxxxxxxxxxxx

# JWT
JWT_SECRET=your-secret-key-minimum-256-bits
```

> All of these are referenced in `application.properties` via `${VAR_NAME}` syntax.

---

## Database Setup

1. **Create the PostgreSQL database:**

```bash
createdb barbershop_db
```

2. **Configure connection** in `src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/barbershop_db
spring.datasource.username=your_pg_username
spring.datasource.password=your_pg_password
```

3. **Schema creation** is handled automatically by Hibernate (`ddl-auto=update`) in the default profile.

---

## Running the Application

```bash
cd backend/trim-booking-api

# Default profile (development)
./mvnw spring-boot:run

# With Row-Level Security enabled
./mvnw spring-boot:run -Dspring-boot.run.profiles=rls

# With data seeder (generates large test datasets)
./mvnw spring-boot:run -Dspring-boot.run.profiles=seed
```

The API starts at **http://localhost:8080**.

---

## Spring Profiles

| Profile | Purpose | Database | External Services |
|---------|---------|----------|-------------------|
| *(default)* | Development | PostgreSQL as schema owner, `ddl-auto=update` | Real credentials from `.env` |
| `rls` | Row-Level Security | Connects as `trim_app_user` (non-superuser), `ddl-auto=none`, autocommit disabled | Real credentials from `.env` |
| `seed` | Bulk test data generation | PostgreSQL as schema owner | Dummy/disabled — no emails, SMS, or payments sent |
| `test` | Automated tests | H2 in-memory (`create-drop`), PostgreSQL dialect compatibility mode | Dummy values |

---

## API Documentation

When the server is running, interactive docs are available at:

- **Swagger UI:** http://localhost:8080/swagger-ui.html
- **OpenAPI JSON:** http://localhost:8080/api-docs

---

## API Endpoints

| Domain | Base Path | Key Operations |
|--------|-----------|----------------|
| **Auth** | `/api/auth` | `POST /login`, `POST /register`, `POST /register-admin`, `POST /forgot-password`, `POST /reset-password`, `GET /validate-reset-token`, `POST /exchange-token` |
| **Services** | `/api/services` | `GET /`, `GET /active`, `POST /`, `PUT /{id}`, `PATCH /{id}/deactivate`, `DELETE /{id}` |
| **Categories** | `/api/categories` | `GET /`, `GET /with-services`, `POST /`, `PUT /{id}`, `PATCH /{id}/deactivate` |
| **Barbers** | `/api/barbers` | `GET /`, `GET /active`, `POST /`, `PUT /{id}`, `PATCH /{id}/deactivate`, `PATCH /{id}/activate` |
| **Availability** | `/api/barber-availability` | `GET /`, `GET /barber/{id}`, `POST /`, `PUT /{id}`, `DELETE /{id}` |
| **Breaks** | `/api/barber-breaks` | `GET /barber/{id}`, `POST /`, `PUT /{id}`, `DELETE /{id}` |
| **Slots** | `/api/availability` | `GET ?barberId=&date=&serviceId=` |
| **Bookings** | `/api/bookings` | `POST /`, `GET /all`, `GET /customer/{id}`, `GET /barber/{id}`, `PUT /{id}`, `PATCH /{id}/cancel`, `PUT /{id}/complete`, `PUT /{id}/no-show`, `PUT /{id}/mark-paid` |
| **Payments** | `/api/payments` | `POST /create-intent`, `POST /webhook` |
| **Dashboard** | `/api/dashboard` | `GET /admin` |
| **Customers** | `/api/admin/customers` | `GET ?page=&size=`, `GET /{id}`, `PUT /{id}/blacklist`, `PUT /{id}/unblacklist` |

**Authentication:** JWT Bearer tokens. Three roles: `CUSTOMER`, `BARBER`, `ADMIN`.  
Include the header `Authorization: Bearer <token>` on protected endpoints.  
Every request must also include `X-Business-Slug: <slug>` to identify the tenant.

---

## Project Structure

```
trim-booking-api/
├── src/main/java/com/trim/booking/
│   ├── TrimBookingApiApplication.java   # Entry point
│   ├── config/                          # Security, CORS, JWT, Stripe, Twilio, async, etc.
│   ├── controller/                      # REST controllers
│   │   ├── AuthController               #   Auth (login, register, password reset)
│   │   ├── BookingController            #   Booking CRUD & status transitions
│   │   ├── PaymentController            #   Stripe payment intents & webhooks
│   │   ├── AvailabilityController       #   Available time slots
│   │   ├── BarberController             #   Barber management
│   │   ├── BarberAvailabilityController #   Weekly schedule management
│   │   ├── BarberBreakController        #   Break management
│   │   ├── ServiceController            #   Service CRUD
│   │   ├── ServiceCategoryController    #   Category CRUD
│   │   ├── CustomerController           #   Customer list & blacklisting
│   │   └── DashboardController          #   Admin analytics
│   ├── dto/                             # Request/response DTOs
│   │   ├── auth/                        #   Login, register, password reset
│   │   ├── barber/                      #   Barber, availability, breaks
│   │   ├── booking/                     #   Booking request/response
│   │   ├── customer/                    #   Customer list, blacklist
│   │   ├── dashboard/                   #   Dashboard stats
│   │   └── service/                     #   Service & category
│   ├── entity/                          # JPA entities
│   │   ├── User                         #   User account (all roles)
│   │   ├── Business                     #   Tenant (barbershop)
│   │   ├── Barber                       #   Barber profile
│   │   ├── BarberAvailability           #   Weekly schedule slots
│   │   ├── BarberBreak                  #   Scheduled breaks
│   │   ├── Booking                      #   Appointment
│   │   ├── Payment                      #   Stripe payment record
│   │   ├── ServiceOffered               #   Service (name, price, duration)
│   │   ├── ServiceCategory              #   Service grouping
│   │   └── AuthTokenExchange            #   One-time token exchange
│   ├── exception/                       # Custom exceptions & global handler
│   ├── repository/                      # Spring Data JPA repositories
│   ├── service/                         # Business logic
│   │   ├── auth/                        #   Authentication & registration
│   │   ├── barber/                      #   Barber, availability, breaks
│   │   ├── booking/                     #   Booking creation, availability calc
│   │   ├── customer/                    #   Customer queries & blacklisting
│   │   ├── dashboard/                   #   Analytics aggregation
│   │   ├── notification/                #   EmailService, SmsService
│   │   ├── payment/                     #   Stripe payment processing
│   │   ├── reminder/                    #   Scheduled booking reminders
│   │   └── user/                        #   User lookups
│   ├── tenant/                          # Multi-tenancy
│   │   ├── TenantContext                #   ThreadLocal tenant holder
│   │   └── TenantFilter                 #   Servlet filter (slug → business_id)
│   └── util/                            # Utilities (phone number formatting)
├── src/main/resources/
│   ├── application.properties           # Default config
│   ├── application-rls.properties       # RLS profile overrides
│   └── application-seed.properties      # Seed profile overrides
├── src/test/
│   ├── java/com/trim/booking/           # Unit & integration tests
│   ├── java/gatling/                    # Gatling load test simulations
│   └── resources/application-test.properties
├── scripts/
│   ├── rls_setup.sql                    # RLS policy creation
│   └── rls_role_setup.sql              # Restricted DB role creation
└── pom.xml
```

---

## Multi-Tenancy

TRiM supports multiple barbershops on a single deployment. Tenancy is resolved per-request:

1. The frontend extracts a **business slug** from the subdomain (e.g. `v7.localhost` → `v7`)
2. Every API request includes an `X-Business-Slug` header
3. `TenantFilter` resolves the slug to a `business_id` and stores it in `TenantContext` (ThreadLocal)
4. Repositories and services use the tenant context to scope queries

### Row-Level Security (RLS)

For stronger isolation, activate the `rls` profile. This:

- Connects as `trim_app_user` (a non-superuser role), so PostgreSQL RLS policies are enforced
- Sets `app.current_business_id` via `SET LOCAL` at the start of each transaction
- RLS policies on all tenant-scoped tables (`users`, `barbers`, `bookings`, `barber_availability`, `barber_breaks`, `payments`, `services_offered`, `service_categories`) filter rows by `business_id`

**Setup (one-time):**

```bash
# As the PostgreSQL superuser / schema owner:
psql -d barbershop_db -f scripts/rls_role_setup.sql   # Create trim_app_user role
psql -d barbershop_db -f scripts/rls_setup.sql         # Enable RLS policies
```

Then run with:

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=rls
```

---

## Testing

### Unit & Integration Tests

```bash
./mvnw test
```

Tests run against an **H2 in-memory database** with dummy credentials for external services (no real emails, SMS, or payments are sent).

### Load Testing (Gatling)

```bash
./mvnw gatling:test -Pgatling
```

Runs the `BookingApiSimulation` against a running instance of the API. Results are written to `target/gatling/`.

### Data Seeding

To generate large-scale test data for performance testing:

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=seed
```

Configurable in `application.properties`:

| Property | Default |
|----------|---------|
| `seed.businesses` | 5 |
| `seed.customers-per-business` | 500,000 |
| `seed.bookings-per-business` | 400,000 |
| `seed.barbers-per-business` | 10 |
| `seed.categories-per-business` | 5 |
| `seed.services-per-category` | 4 |
