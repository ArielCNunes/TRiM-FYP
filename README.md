# TRiM - Barbershop Booking Platform

**Final Year Project** – BSc (Hons) Computing in Software Development  
**Student:** Ariel Nunes (G00418763) | **Supervisor:** Andrew Beatty  
**Institution:** Atlantic Technological University Galway | **Year:** 2025/2026

---

## Overview

TRiM is a full-stack booking system for barbershops. Customers can book appointments online (with or without an account), pay deposits via Stripe, and manage their bookings. Barbers manage their schedules and availability, while admins oversee services, staff, and business analytics.

**Phase 2** will implement **multi-tenant architecture**, enabling multiple barbershops to operate on a single platform, researching database isolation patterns like Row-Level Security for secure SaaS deployment.

---

## Features

| Role | Capabilities |
|------|--------------|
| **Customer** | Browse services, book appointments, pay deposits online, manage bookings |
| **Barber** | View schedule, manage availability & breaks, mark appointments complete/no-show |
| **Admin** | Manage services & categories, manage barbers, calendar view, analytics dashboard |

**Booking Highlights:**
- Guest checkout (no account required)
- Real-time availability based on barber schedules and existing bookings
- Stripe deposit payments to reduce no-shows
- Email & SMS reminder notifications

---

## Tech Stack

| Backend | Frontend |
|---------|----------|
| Java 21, Spring Boot 3.5 | React 19, TypeScript |
| Spring Security + JWT | Redux Toolkit |
| PostgreSQL, Spring Data JPA | Tailwind CSS, Vite |
| Stripe, Twilio, JavaMail | Axios, React Router 7 |

---

## Repository Structure

```
TRiM-FYP/
├── backend/trim-booking-api/   # Spring Boot API
├── frontend/src/               # React application
└── docs/                       # Meeting logs
```
