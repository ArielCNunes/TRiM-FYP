# TRiM - A Multi Tenant SaaS Application

**Final Year Project** — BSc (Hons) Computing in Software Development  
**Student:** Ariel Nunes (G00418763) | **Supervisor:** Andrew Beatty  
**College:** ATU Galway | **Year:** 2025/2026

---

## Overview

TRiM is a multi-tenant, full-stack booking platform for barbershops. Customers can browse services, book appointments, and pay deposits online. Barbers manage their own schedules, availability, and breaks. Admins oversee services, staff, customers, and business analytics through a dashboard.

The platform is designed as a **SaaS product**: multiple barbershops operate on a single deployment, each isolated by subdomain (e.g. `v7.trim.com`, `topcuts.trim.com`). Tenant data is secured using PostgreSQL **Row-Level Security (RLS)**.

---

## Features

| Role | Capabilities |
|------|-------------|
| **Customer** | Browse services by category, book appointments with a barber, pay deposits online or in-shop, view and cancel bookings |
| **Barber** | View upcoming schedule, set weekly availability and breaks, mark appointments as completed or no-show |
| **Admin** | Manage services and categories, manage barbers, calendar overview, customer management (blacklisting), analytics dashboard |

**Highlights:**
- Real-time slot availability computed from barber schedules and existing bookings
- Stripe integration for online deposit payments to reduce no-shows
- Email (Gmail SMTP) and SMS (Twilio) booking confirmations and reminders
- Subdomain-based multi-tenancy with Row-Level Security
- Swagger/OpenAPI documentation

---

## Tech Stack

| Layer | Technologies |
|-------|-------------|
| **Backend** | Java 21, Spring Boot 3.5, Spring Security, Spring Data JPA, PostgreSQL |
| **Auth** | JWT, role-based access (Customer / Barber / Admin) |
| **Payments** | Stripe (server + client) |
| **Notifications** | Spring Mail (Gmail SMTP), Twilio SMS |
| **Frontend** | React 19, TypeScript 5.9, Vite 7 |
| **State & Routing** | Redux Toolkit, React Router 7 |
| **Styling** | Tailwind CSS 3.4 |
| **Testing** | JUnit (unit/integration), Gatling (load testing) |
| **API Docs** | SpringDoc OpenAPI (Swagger UI) |

---

## Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│  Browser (React SPA)                                            │
│  subdomain → X-Business-Slug header                             │
└──────────────────────────┬──────────────────────────────────────┘
                           │ REST / JSON
┌──────────────────────────▼──────────────────────────────────────┐
│  Spring Boot API  (port 8080)                                   │
│  ┌────────────┐ ┌───────────┐ ┌────────────┐ ┌───────────────┐ │
│  │ TenantFilter│ │ JWT Auth  │ │ Controllers│ │ Services      │ │
│  └─────┬──────┘ └─────┬─────┘ └─────┬──────┘ └───────┬───────┘ │
│        │ sets tenant   │ validates   │                │         │
│        │ context       │ token       │                │         │
│  ┌─────▼───────────────▼─────────────▼────────────────▼───────┐ │
│  │                  Spring Data JPA                           │ │
│  └────────────────────────┬───────────────────────────────────┘ │
└───────────────────────────┼─────────────────────────────────────┘
                            │
┌───────────────────────────▼─────────────────────────────────────┐
│  PostgreSQL                                                     │
│  Row-Level Security policies filter rows by business_id         │
└─────────────────────────────────────────────────────────────────┘
```

Every request includes an `X-Business-Slug` header (derived from the subdomain). The `TenantFilter` resolves the slug to a `business_id` and sets it in `TenantContext`. When the RLS profile is active, a restricted database user and RLS policies enforce that queries only return data belonging to the current tenant.

---

## Repository Structure

```
booking-system-fyp/
├── backend/
│   └── trim-booking-api/        # Spring Boot REST API
│       ├── src/main/java/       #   Application source code
│       ├── src/main/resources/  #   Configuration & properties
│       ├── src/test/            #   Unit, integration & Gatling tests
│       ├── scripts/             #   RLS setup SQL scripts
│       └── pom.xml
├── frontend/                    # React SPA
│   ├── src/
│   │   ├── api/                 #   Axios client & endpoint functions
│   │   ├── components/          #   UI components (admin, barber, booking, shared)
│   │   ├── pages/               #   Page-level components
│   │   ├── store/               #   Redux store & slices
│   │   ├── routes/              #   Application routing
│   │   ├── hooks/               #   Custom React hooks
│   │   ├── types/               #   TypeScript type definitions
│   │   └── utils/               #   Utility functions
│   ├── package.json
│   └── vite.config.ts
└── docs/                        # Meeting logs & dissertation (LaTeX)
```

---

## API Documentation

When the backend is running, interactive API documentation is available via Swagger UI:

- **Swagger UI:** [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
- **OpenAPI JSON:** [http://localhost:8080/api-docs](http://localhost:8080/api-docs)

---

## Spring Profiles

| Profile | Purpose |
|---------|---------|
| *(default)* | Standard development mode: Hibernate `ddl-auto=update`, direct DB access |
| `rls` | Enables Row-Level Security: uses a restricted PostgreSQL user, disables DDL auto-update |
| `seed` | Generates large-scale test data (configurable businesses, customers, bookings) via `DataSeeder` |
| `test` | Used by automated tests: H2 in-memory database, dummy service credentials |

---

## Screenshots

*Coming soon.*
