# TRiM Frontend

React single-page application for the TRiM barbershop booking platform.

---

## Tech Stack

| Technology | Purpose |
|-----------|---------|
| React 19 | UI framework |
| TypeScript 5.9 | Type-safe JavaScript |
| Vite 7 | Build tool & dev server |
| React Router 7 | Client-side routing |
| Redux Toolkit | State management (auth) |
| Axios | HTTP client |
| Tailwind CSS 3.4 | Utility-first styling |

---

## Prerequisites

- **Node.js 20+**
- **npm**
- The [backend API](../backend/README.md) running on port 8080

---

## Environment Setup

Copy the example env file and fill in your values:

```bash
cp .env.example .env
```

```env
VITE_STRIPE_PUBLISHABLE_KEY=pk_test_xxxxxxxxxxxxxxxxxxxx
VITE_APP_DOMAIN=localhost
```

| Variable | Purpose |
|----------|---------|
| `VITE_STRIPE_PUBLISHABLE_KEY` | Stripe publishable key for the payment form |
| `VITE_APP_DOMAIN` | Base domain used to detect dev vs production environment |

---

## Running the App

```bash
npm install
npm run dev
```

The app starts at **http://localhost:3000**.

### Available Scripts

| Command | Description |
|---------|-------------|
| `npm run dev` | Start Vite dev server with HMR (port 3000) |
| `npm run build` | Type-check with `tsc` then build for production |
| `npm run preview` | Preview the production build locally |

---

> The dev server proxies `/api` requests to `http://localhost:8080` (configured in `vite.config.ts`).

---

## Project Structure

```
frontend/src/
├── api/
│   ├── axios.ts                # Axios instance, interceptors, slug extraction
│   └── endpoints.ts            # All API call functions grouped by domain
├── components/
│   ├── admin/                  # Admin panel components
│   │   ├── AdminTabNavigation  #   Tab navigation for admin sections
│   │   ├── barbers/            #   Barber management
│   │   ├── calendar/           #   Calendar overview
│   │   ├── categories/         #   Service category management
│   │   ├── customers/          #   Customer list & blacklisting
│   │   ├── dashboard/          #   Analytics dashboard (charts)
│   │   └── services/           #   Service management
│   ├── auth/                   # Auth forms (login, signup, forgot/reset password)
│   ├── barber/                 # Barber dashboard components
│   │   ├── BarberAvailabilityManager   # Weekly availability editor
│   │   ├── BarberBookingsManager       # Upcoming bookings list
│   │   ├── BarberBreaksManager         # Break management
│   │   └── BarberCalendar              # Barber schedule calendar
│   ├── booking/                # Booking action buttons, reschedule modal
│   ├── bookingSteps/           # Multi-step booking flow
│   │   ├── ServiceSelectionStep
│   │   ├── BarberSelectionStep
│   │   ├── DateTimeSelectionStep
│   │   ├── PaymentForm
│   │   └── ConfirmationStep
│   ├── shared/                 # Reusable UI components
│   │   ├── EmptyState
│   │   ├── LoadingSpinner
│   │   ├── PhoneInput
│   │   ├── StatusBadge
│   │   └── StatusMessage
│   ├── BookingComponents.tsx   # Booking card components
│   └── Sidebar.tsx             # Navigation sidebar
├── config/
│   └── stripe.ts               # Stripe.js initialisation
├── context/
│   └── ThemeContext.tsx          # Theme provider
├── features/
│   └── auth/                    # Auth slice (Redux)
├── hooks/
│   └── useBookingFlow.ts        # Booking flow state & logic
├── pages/                       # Route-level page components
│   ├── Home.tsx
│   ├── BookingFlow.tsx
│   ├── Admin.tsx
│   ├── MyBookings.tsx
│   ├── BarberDashboard.tsx
│   ├── Auth.tsx
│   ├── ForgotPassword.tsx
│   ├── ResetPassword.tsx
│   └── RegisterBusiness.tsx
├── routes/
│   └── AppRoutes.tsx            # Route definitions
├── store/
│   ├── store.ts                 # Redux store configuration
│   └── hooks.ts                 # Typed useSelector / useDispatch
├── types/
│   └── index.ts                 # Shared TypeScript interfaces
└── utils/
    ├── phoneUtils.ts            # Phone number formatting
    └── statusUtils.ts           # Booking status helpers
```

---

## Routing

| Path | Page | Access |
|------|------|--------|
| `/` | Home | Public |
| `/booking` | BookingFlow | Public (auth required to confirm) |
| `/auth` | Auth (Login / Register) | Public |
| `/my-bookings` | MyBookings | Customer |
| `/barber` | BarberDashboard | Barber |
| `/admin` | Admin | Admin |
| `/forgot-password` | ForgotPassword | Public |
| `/reset-password/:token` | ResetPassword | Public |
| `/register-business` | RegisterBusiness | Public |

---

## Multi-Tenancy

The frontend resolves the current business (tenant) from the **subdomain**:

| URL | Resolved Slug |
|-----|---------------|
| `v7.localhost:3000` | `v7` |
| `topcuts.trim.com` | `topcuts` |
| `localhost:3000?business=shop2` | `shop2` (query param fallback) |

The slug is sent as an `X-Business-Slug` header on every API request via the Axios request interceptor. This allows a single frontend deployment to serve multiple barbershops.

---

## State Management

The Redux store contains a single **auth slice** managing:

- Current user object
- JWT token
- Authentication status

The token is persisted in `localStorage` and attached to requests automatically by the Axios interceptor. On 401/403 responses (from non-auth endpoints), the interceptor clears stored credentials and redirects to `/auth`.
