import api from "./axios";
import type {
  LoginRequest,
  LoginResponse,
  RegisterRequest,
  User,
  Service,
  ServiceCategory,
  CategoryWithServices,
  Barber,
  BookingRequest,
  BookingResponse,
  DashboardStats,
  ForgotPasswordRequest,
  ResetPasswordRequest,
  PasswordResetResponse,
} from "../types";

/** Auth endpoints for login and registration. */
export const authApi = {
  /** Authenticate with email/password. */
  login: (credentials: LoginRequest) =>
    api.post<LoginResponse>("/auth/login", credentials),

  /** Create a new user account. */
  register: (userData: RegisterRequest) =>
    api.post<User>("/auth/register", userData),

  /** Save a guest account with password. */
  saveAccount: (data: { userId: number; password: string }) =>
    api.post("/auth/save-account", data),

  /** Request password reset email. */
  forgotPassword: (data: ForgotPasswordRequest) =>
    api.post<PasswordResetResponse>("/auth/forgot-password", data),

  /** Reset password with token. */
  resetPassword: (data: ResetPasswordRequest) =>
    api.post<PasswordResetResponse>("/auth/reset-password", data),

  /** Validate reset token. */
  validateResetToken: (token: string) =>
    api.get<{ valid: boolean }>(`/auth/validate-reset-token?token=${token}`),
};

/** Service category endpoints. */
export const categoriesApi = {
  /** Fetch all categories. */
  getAll: () => api.get<ServiceCategory[]>("/categories"),

  /** Fetch all categories with their services. */
  getWithServices: () =>
    api.get<CategoryWithServices[]>("/categories/with-services"),

  /** Get a single category by ID. */
  getById: (id: number) => api.get<ServiceCategory>(`/categories/${id}`),

  /** Create a new category (admin only). */
  create: (name: string) => api.post<ServiceCategory>("/categories", { name }),

  /** Update a category (admin only). */
  update: (id: number, name: string) =>
    api.put<ServiceCategory>(`/categories/${id}`, { name }),

  /** Delete a category (admin only). */
  delete: (id: number) => api.delete(`/categories/${id}`),
};

/** Service catalogue endpoints. */
export const servicesApi = {
  /** Fetch all active services. */
  getActive: () => api.get<Service[]>("/services/active"),

  /** Create a new service entry. */
  create: (service: {
    name: string;
    description: string;
    durationMinutes: number;
    price: number;
    depositPercentage: number;
    active: boolean;
    categoryId: number;
  }) => api.post<Service>("/services", service),
};

/** Barber management endpoints. */
export const barbersApi = {
  /** Fetch all active barbers. */
  getActive: () => api.get<Barber[]>("/barbers/active"),

  /** Create a new barber profile. */
  create: (barber: any) => api.post<Barber>("/barbers", barber),

  /** Get barber availability */
  getAvailability: (barberId: number) =>
    api.get(`/barber-availability/barber/${barberId}`),

  /** Set availability for a day */
  setAvailability: (data: {
    barberId: number;
    dayOfWeek: string;
    startTime: string;
    endTime: string;
    isAvailable: boolean;
  }) => api.post("/barber-availability", data),

  /** Update existing availability */
  updateAvailability: (id: number, data: any) =>
    api.put(`/barber-availability/${id}`, data),
};

/** Availability lookup endpoints. */
export const availabilityApi = {
  /** Get available time slots for a barber/service/date. */
  getSlots: (barberId: number, date: string, serviceId: number) =>
    api.get<string[]>(
      `/availability?barberId=${barberId}&date=${date}&serviceId=${serviceId}`
    ),
};

/** Booking flow endpoints. */
export const bookingsApi = {
  /** Create a new booking. */
  create: (booking: BookingRequest) =>
    api.post<BookingResponse>("/bookings", booking),

  /** Create a guest booking without authentication. */
  createGuest: (data: {
    firstName: string;
    lastName: string;
    email: string;
    phone: string;
    barberId: number;
    serviceId: number;
    bookingDate: string;
    startTime: string;
    paymentMethod: string;
  }) =>
    api.post<{
      bookingId: number;
      customerId: number;
      depositAmount: number;
      outstandingBalance: number;
    }>("/auth/guest-booking", data),

  /** Fetch bookings for the authenticated customer. */
  getCustomerBookings: (customerId: number) =>
    api.get<BookingResponse[]>(`/bookings/customer/${customerId}`),

  /** Cancel a booking by ID. */
  cancelBooking: (bookingId: number) =>
    api.patch<BookingResponse>(`/bookings/${bookingId}/cancel`),

  /** Update a booking's date and time (reschedule). */
  updateBooking: (
    bookingId: number,
    data: { bookingDate: string; startTime: string }
  ) => api.put<BookingResponse>(`/bookings/${bookingId}`, data),

  /** Fetch bookings for a specific barber. */
  getBarberBookings: (barberId: number) =>
    api.get(`/bookings/barber/${barberId}`),

  /** Mark a booking as complete. */
  markComplete: (bookingId: number) =>
    api.put<BookingResponse>(`/bookings/${bookingId}/complete`),

  /** Mark a booking as no-show. */
  markNoShow: (bookingId: number) =>
    api.put<BookingResponse>(`/bookings/${bookingId}/no-show`),
};

/** Payment endpoints for Stripe integration. */
export const paymentsApi = {
  /** Create a payment intent for a booking deposit. */
  createIntent: (bookingId: number) =>
    api.post<{
      clientSecret: string;
      paymentIntentId: string;
      depositAmount: number;
      outstandingBalance: number;
      bookingId: number;
    }>("/payments/create-intent", { bookingId }),
};

/** Dashboard endpoints for admin statistics. */
export const dashboardApi = {
  /** Get admin dashboard statistics. */
  getAdminStats: () => api.get<DashboardStats>("/dashboard/admin"),
};
